package xechwic.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ydx.securephone.R;
import xechwic.android.act.MainApplication;
import xechwic.android.act.ServerConfig;
import xechwic.android.bean.RecordBean;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.FragmentRereshEvent;
import xechwic.android.bus.event.LoginEvent;
import xechwic.android.bus.event.UpdateAESEvent;
import xechwic.android.ui.FriendDetailUI;
import xechwic.android.ui.InCallUI;
import xechwic.android.ui.SelectFriendUI;
import xechwic.android.util.DownloadTool;
import xechwic.android.util.FileManager;
import xechwic.android.util.FileUtil;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.ObjectIO;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.UriConfig;
import xechwic.android.util.Util;
import xechwic.android.util.XWDataCenterMessage;

import static xechwic.android.XWDataCenter.xwContext;
import static xechwic.android.XWDataCenter.xwDC;

/**
 * 去电、来电处理
 *0,空闲； 1，开始拨号；2，正在拨号；3，拨通处理 ；5，挂断；11，来电
 */
class XWDataCenterHandler extends Handler {

    private String TAG=XWDataCenterHandler.class.getSimpleName();

    private void handleResend(final Handler handler,final Message msg){
        if(msg==null){
            return;
        }
        Log.e("handleResend","handleResend:"+msg.what);
        final int msgWhat=msg.what;
        final String content=(String)msg.obj;
        if(!TextUtils.isEmpty(content)){
            TaskExecutor.executeTask(new Runnable() {
                @Override
                public void run() {
                    Log.e("handleResend","TaskExecutor run: content=="+content);
                    try
                    {
                        if (content !=null)
                        {
                            int iPos1,iPos2;
                            String sFromUser,sFilePath,sDateTime;
                            String sExt="";  ////文件扩展名


                            Log.e("xim", "Got WeiXin file 1:"+content);

                            iPos1=content.indexOf("\r", 0);

                            if (iPos1<0)
                                return;

                            Log.e("xim", "Got WeiXin file 2:"+content);

                            iPos2=content.indexOf("\r", iPos1+1);

                            if (iPos2<0)
                                return;

                            Log.e("xim", "Got WeiXin file 3:"+content);

                            sFromUser=content.substring(0, iPos1);
                            if (TextUtils.isEmpty(sFromUser)) {
                                return;
                            }

                            sFilePath=content.substring(iPos1+1, iPos2);
                            sDateTime=content.substring(iPos2+1, content.length());

                            //获取后缀名称
                            if(sFilePath.lastIndexOf("/") == -1){
                                return;
                            }
                            String fileName =sFilePath.substring(sFilePath.lastIndexOf("/")+1, sFilePath.length());
                            if(fileName.lastIndexOf(".") > 0){
                                sExt = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
                            }else{
                                //文件没有后缀
                                sExt = "";
                            }

                            /////////////优先处理 pubkey 文件将其转存到相应的目录
                            if (  ("rsa".compareToIgnoreCase(sExt)==0) )
                            {
                                if (fileName.indexOf("pubkey_"+sFromUser+".rsa")>=0)
                                {
                                    String rsapath = XWDataCenter.xwDC.sRASKeyPath + "/pubkey_" + sFromUser + ".rsa";


                                    synchronized (XWDataCenter.iCreditMutex) {

                                        {
                                            try {
                                                /////////////////////2014-09-19,先删除旧文件
                                                ////UriConfig.delete(rsapath);
                                                FileManager.moveOneFile(sFilePath, rsapath);
                                                ////////////////////////

                                                Log.v("xim", "Got new rsa file :" + rsapath);
                                                XWDataCenter.SendCreditMessage(sFromUser, XWDataCenter.CREDIT_REQUEST_REPLY);
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                return;
                            }


                            /////////////////////2014-09-05,对收到文本数据进行解压
                            String sAESPassword=XWDataCenter.getFriendAESPassword( sFromUser,XWDataCenter.xwDC.loginName);
                            if (sAESPassword!=null)
                            {
                                File f=new File(sFilePath);
                                String aespath= UriConfig.getSavePath()+"/aesgotfiles/"+sFromUser;
                                UriConfig.makeFileDirs(aespath);
                                ////对16进制文件名解码
                                String aesfileName=f.getName();
                                ///////////收到的文件名前面加了用户账号，需要去掉才能16进制解码
                                if(aesfileName.startsWith(sFromUser)){
                                    aesfileName=aesfileName.substring(sFromUser.length()+1);//用户名加_,
                                }
                                if(aesfileName.startsWith(JRSConstants.HEX_PRE)&&aesfileName.length()>JRSConstants.HEX_PRE.length()){
                                    String filename=aesfileName.substring(JRSConstants.HEX_PRE.length());
                                    aespath+="/"+ FileUtil.toSrcName(filename);
                                }else{
                                    aespath+="/"+f.getName();
                                }

                                boolean bRet=XWDataCenter.AESDecodeFile(sFilePath, aespath, sAESPassword);

                                //////////////////send aes encoded path!!!!
                                if (bRet)
                                {
                                    try
                                    {
                                        UriConfig.delete(sFilePath);
                                    }
                                    catch(Exception ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                    sFilePath=aespath;

                                    Log.e("xim", "Got AES WeiXin file :"+sFilePath);

                                }
                            }else{
                                //////没协商成功，直接拷贝到aesgotfiles
                                File f=new File(sFilePath);
                                String aespath= UriConfig.getSavePath()+"/aesgotfiles/"+sFromUser;
                                UriConfig.makeFileDirs(aespath);
//                    aespath+="/"+f.getName();
                                ////对16进制文件名解码
                                ////对16进制文件名解码
                                String aesfileName=f.getName();
                                ///////////收到的文件名前面加了用户账号，需要去掉才能16进制解码
                                if(aesfileName.startsWith(sFromUser)){
                                    aesfileName=aesfileName.substring(sFromUser.length()+1);//用户名加_,
                                }
                                if(aesfileName.startsWith(JRSConstants.HEX_PRE)&&aesfileName.length()>JRSConstants.HEX_PRE.length()){
                                    String filename=aesfileName.substring(JRSConstants.HEX_PRE.length());
                                    aespath+="/"+ FileUtil.toSrcName(filename);
                                }else{
                                    aespath+="/"+f.getName();
                                }

                                Util.copyFile(new File(sFilePath), aespath);
                                try
                                {
                                    UriConfig.delete(sFilePath);
                                }
                                catch(Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                                sFilePath=aespath;
                                Log.e("xim", "Got AES WeiXin file :"+sFilePath);
                            }

                            /////////////////////////////////////////////////在这里对收到的微信文件进行处理!!!!!!!!!!!!!!!!
                            ////////////////要判断当前Activity,根据 if (XWDataCenter.xwContext instanceof ???????),然后处理。
                            Log.e("xim", "Got WeiXin file:"+sFromUser+" "+sFilePath+" "+sDateTime);


                            //////////////要取一下本地用户信息,如果取不到10秒后再试，共取3次
                            FriendNodeInfo fni1=XWDataCenter.xwDC.getFNINfoFromLoginName(sFromUser);

                            if ((fni1==null)&&(msgWhat<=205))  ////////////////10秒后重发!!!!!!!!!!!!!!!!!防止有时还未登录就已收到文件!!!!
                            {
                                Message resend=handler.obtainMessage(msgWhat+100, content);
                                handler.sendMessageDelayed(resend,20000);
                                Log.e("xim", "getFNINfoFromLoginName:"+sFromUser+" not found!");

                                ///////////////////////////请求临时聊天
                                XWDataCenter.xwDC.XWRequestTempTalk((XWDataCenter.xwDC.loginName+"\0").getBytes(),0,(sFromUser+"\0").getBytes(),0);

                                return;
                            }


                            ///////////////////////////////////////////////////////////////////////////
                            {
                                ///////////////
                                {
                                    if (  "xwx".compareToIgnoreCase(sExt)==0 )  /////////////微信语音
                                    {

                                        {

                                            //////////////播放
                                            Log.e("xim", "XWPlayWeiXinAudioFile:"+sFilePath);

                                            /**
                                             * 每次发送、接受文件，都将data/data下的文件，复制到sd卡里面，节约空间
                                             * 路径如下：sdcard/voice/当前账号的用户ID/data下路径的哈希码.xwx
                                             */
                                            try
                                            {
                                                Log.e("xim", "hasReceiveMessage 1:"+sFromUser);

                                                ///////////////2014-06-26,对收到的文件不转存!!!!!!!!!!!!!!!
                                                sFilePath="(:voice)"+sFilePath;
                                                XWDataCenter.xwDC.hasReceiveMessage(0,fni1.getId(),sFilePath.getBytes("GBK"),sDateTime.getBytes());
                                            }
                                            catch(Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    else if ("xwn".compareToIgnoreCase(sExt)==0)     ////////////////////收到其它扩展名的文件!!!!!!!!!!!!!!
                                    {
                                        int iFromTerminalType=XWDataCenter.xwDC.XWGetTerminalType((sFromUser+"\0").getBytes());
                                        if (iFromTerminalType==21)   /////////////////////2013-07-17,在这里加入导航文件处理!!!!!!!!!
                                        {
                                            android.util.Log.e("XIM","Got xwn file:"+sFilePath);


                                            //////////////////////////////////////////////////////


                                        }
                                    }
                                    else
                                    {
                                        ////////////////////////////2012-12-29，在这里对收到其他文件进行显示!!!!!!!!!!!!!!!!
                                        //////////////////////////////////////////////////////////

                                        /**
                                         * 每次发送、接受文件，都将data/data下的文件，复制到sd卡里面，节约空间
                                         * 路径如下：sdcard/file/当前账号的用户ID/data下路径的哈希码+后缀名
                                         */
                                        final String sdcardPath = sFilePath;
                                        final String strExt=sExt;
                                        final FriendNodeInfo nodeInfo=fni1;
                                        final String strDateTime=sDateTime;
                                        TaskExecutor.runOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    //图片
                                                    if ("jpg".compareToIgnoreCase(strExt) == 0 || "bmp".compareToIgnoreCase(strExt) == 0 || "png".compareToIgnoreCase(strExt) == 0 || "gif".compareToIgnoreCase(strExt) == 0) {
                                                        XWDataCenter.xwDC.hasReceiveMessage(0, nodeInfo.getId(), (xechwic.android.XWCodeTrans.doTrans("(:image)" + sdcardPath)).getBytes("GBK"), strDateTime.getBytes());
                                                    }else if("mpx".compareToIgnoreCase(strExt)==0){///视频录制文件
                                                        XWDataCenter.xwDC.hasReceiveMessage(0, nodeInfo.getId(), (xechwic.android.XWCodeTrans.doTrans("(:video)" + sdcardPath)).getBytes("GBK"), strDateTime.getBytes());
                                                    }
                                                    else {
                                                        //文件
                                                        XWDataCenter.xwDC.hasReceiveMessage(0, nodeInfo.getId(), (xechwic.android.XWCodeTrans.doTrans("(:file)" + sdcardPath)).getBytes("GBK"), strDateTime.getBytes());
                                                    }
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                    }
                                }

                            }


                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            });
        }


    }





    /**
     * 消息处理：
     * 13，升级 ； 14,检测好友邀请;12,定位;10,激活网络;1,来电;2,空闲;3,自动挂断;4,启动音频；5/105/205,在这里对收到的微信文件进行处理;
     * 11,关闭底层通讯；
     * 18,显示视频
     *
     */
    // 子类必须重写此方法,接受数据

    @Override
    public void handleMessage(Message msg) {


        Log.e("XWDataCenterHandler", "handleMessage "+ msg.what);

        try
        {

            // 此处可以更新UI

            switch (msg.what) {
                case XWDataCenterMessage.MSG_1:  //////////////来电
                {
                    try {
                        Intent intent = new Intent(MainApplication.getInstance(), InCallUI.class);
                        intent.putExtra("phone_number", XWDataCenter.xwDC.calling_loginName);
                        intent.putExtra("tag", "3");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainApplication.getInstance().startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    //			}
                    break;
                }

                case XWDataCenterMessage.MSG_2:  ////////空闲
                {

                    if (xwContext == null)
                        break;

                    if (xwContext instanceof FriendCall) {
                        ((FriendCall) xwContext).backButtonDown(true);
                    } else if (xwContext instanceof InCallUI) {
                        ((InCallUI) xwContext).finishCall();
                    } else if (xwContext instanceof FriendVideoDisplay) {
                        ((FriendVideoDisplay) xwContext).stopVideo();
                    }
                    break;
                }
                case XWDataCenterMessage.MSG_3:  //////////重复检测挂断并自动挂断
                {
                    /////挂断
                    if (xwContext instanceof FriendCall) {
					/*Message msg1=((FriendCall)XWDataCenter.xwContext).mHandler.obtainMessage(5,(Object)XWDataCenter.xwContext.getResources().getString(R.string.alert_status_incoming));
						((FriendCall)XWDataCenter.xwContext).mHandler.sendMessage(msg1);
					 */
                        ((FriendCall) xwContext).backButtonDown(true);//挂断并退出
                    } else if (xwContext instanceof FriendVideoDisplay) {
						/*Message msg1=((FriendCall)XWDataCenter.xwContext).mHandler.obtainMessage(5,(Object)XWDataCenter.xwContext.getResources().getString(R.string.alert_status_incoming));
						((FriendCall)XWDataCenter.xwContext).mHandler.sendMessage(msg1);
						 */
                        ((FriendVideoDisplay) xwContext).stopVideo();
                    } else {
                        if(xwDC.remoteVideoRunning){/////视频还在跑
                            xwDC.stopVideo();
                        }else{
                            XWDataCenter.threadHangupNetPhone();
                        }

                    }

                    //////////////置当前号码为空.
                    XWDataCenter.xwDC.sCurrentPhoneNumber="";

                    XWDataCenter.xwDC.netPhoneTime = 0;
                    xwDC.friendHungup=0;
                    break;
                }

                ////////////////////用于启动音频!!!!!!!!!!!!!
                case XWDataCenterMessage.MSG_4: {
                    XWDataCenter.xwDC.startXWAudio();// 音频服务线程启动
                }
                break;
                ///////////////////////////////////////收到微信文件!!!,2012-11-12
                case XWDataCenterMessage.MSG_5:
                    Log.e("XWDataCenterHandler", "handleResend:first time");
                    handleResend(this, msg);    //重发第一次
                    break;
                case XWDataCenterMessage.MSG_105:  ////重发消息
                    Log.e("XWDataCenterHandler", "handleResend:second time");
                    handleResend(this, msg);    //重发第二次
                    break;
                case XWDataCenterMessage.MSG_205:  //////////重发消息
                    Log.e("XWDataCenterHandler", "handleResend:third time");
                    handleResend(this, msg);    //重发第三次
                    break;

                ///////////////////用于激活网络
                case XWDataCenterMessage.MSG_10: {
                    XWDataCenter.xwDC.reActive(1);
                }
                break;
                //////////////用于关闭底层通讯
                case XWDataCenterMessage.MSG_11: {
                    ////XWDataCenter.xwDC.reActive(0);
                }
                break;


                //////////////用于获取位置
                case XWDataCenterMessage.MSG_12: {
                    ///////////////2014-08-06,取消位置
                    //////getLocation();
                }
                break;

                case XWDataCenterMessage.MSG_13:  ///////////////////////////////////2013-03-05,有新版本!!!!!!!!!!!!!!提示下载
                {
                    if (xwContext != null) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(xwContext);
                        builder.setTitle("Version");
                        builder.setMessage(xechwic.android.XWCodeTrans.doTrans("有新版本") + "[" + MainApplication.sNewVerName + "][" + MainApplication.sNewVerDate + "]," + xechwic.android.XWCodeTrans.doTrans("是否更新?"));
                        builder.setPositiveButton(xechwic.android.XWCodeTrans.doTrans("确定"), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                String url = ServerConfig.XIM_SERVER_HOST + "/download/"+ServerConfig.server_app_name;
                                if (!TextUtils.isEmpty(MainApplication.sNewVerURL)) {
                                    url = MainApplication.sNewVerURL;
                                }
                                final String downUrl = url;
                                final DownloadTool downTool = new DownloadTool(xwContext, UriConfig.getFileSavePath() + "/"+ServerConfig.server_app_name);
                                downTool.setListener(new DownloadTool.DownLoadListener() {

                                    @Override
                                    public void DownLoadOver() {
                                        downTool.installApk();
                                    }
                                });
                                downTool.download(downUrl);
                            }
                        });
                        builder.setNeutralButton(xechwic.android.XWCodeTrans.doTrans("取消"), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                        try {
                            builder.show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                break;


                case XWDataCenterMessage.MSG_14:  ///////////////////////////////////2013-04-27,不断检测是否有好友邀请!!!!!!!!1
                {
                    if (xwContext != null) {
                        if (xwContext instanceof FriendControl) {
                            ((FriendControl) xwContext).getSystemMessageAlert();
                        }
                    }
                }
                break;


                case XWDataCenterMessage.MSG_15:  ///////////////////////////////////处理临时消息!!!!!!!!1
                {
                    if (xwContext != null) {
                        XWTextMessage textmsg = (XWTextMessage) msg.obj;
                        if (textmsg != null) {
                            if (textmsg.senderID != 0)
                                XWDataCenter.xwDC.hasReceiveMessage(textmsg.type, textmsg.senderID, textmsg.content, textmsg.btime);
                            else
                                XWDataCenter.xwDC.hasReceiveMessageByUserName(textmsg.type, textmsg.sSenderName, textmsg.content, textmsg.btime);
                        }
                    }
                }
                break;

                case XWDataCenterMessage.MSG_16:  ///////////////////////////////////自动关闭录音
                {
				/*if (XWDataCenter.xwDC.chatActivity!=null)
				{
					XWDataCenter.xwDC.chatActivity.stopWeiXinAudio();
				}*/
                }
                break;

                case XWDataCenterMessage.MSG_17:  ///////////////////////////////////显示提示
                {
                    if ((xwContext != null) && (msg.obj != null)) {
                        try {
                            Log.e(TAG, "msg.what:17" + "," + msg.obj);
                            Toast.makeText(xwContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
                case XWDataCenterMessage.MSG_18: //////////////显示视频
                {
                    if (xwContext instanceof FriendVideoDisplay) {

                        Log.e(TAG, "msg.what 18:--->FriendVideoDisplay");
                        FriendVideoDisplay fvd = ((FriendVideoDisplay) xwContext);

                        int iGotVideo = 0;
                        XWDataCenter.xwDC.NetPhoneClientDataLock();
                        try {
                            ///byte[] BytesGotVideo = null;

                            try {
                                iGotVideo = XWDataCenter.xwDC
                                        .getRemoteVideoData();// //.clone();

                                if (iGotVideo > 0) {
                                    XWDataCenter.xwDC.ivideoPicLen = iGotVideo;

                                    ///byte[] bytes = new byte[iGotVideo];
                                    if ((XWDataCenter.xwDC.videlLocalBuffer == null)
                                            || (XWDataCenter.xwDC.videlLocalBuffer.length < iGotVideo)
                                            ) {
                                        XWDataCenter.xwDC.videlLocalBuffer = new byte[iGotVideo];
                                    }
                                    XWDataCenter.xwDC.videoDataBuffer.position(0);
                                    XWDataCenter.xwDC.videoDataBuffer.get(XWDataCenter.xwDC.videlLocalBuffer, 0, iGotVideo);
                                    Log.e("xim", "BytesGotVideo 5");
                                }

                            } catch (Exception e1) {
                                Log.e(TAG, "get ByteGotVideo exception");
                                e1.printStackTrace();
                                ////BytesGotVideo = null;
                            }
                        } finally {
                            XWDataCenter.xwDC.NetPhoneClientDataUnlock();
                        }

                        ////////////////////在这里画图!!!!!!2016-12-02
                        if (iGotVideo > 0)
                            try {
                                fvd.drawRemoteData(XWDataCenter.xwDC.videlLocalBuffer, iGotVideo);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                    }

                }
                break;

                case XWDataCenterMessage.MSG_27: //////////////收到查询好友
                {
                    if (msg.obj != null)
                        if ((xwContext != null) && (xwContext instanceof FriendQuery)) {
                            Message msgfriend = ((FriendQuery) xwContext).mHandler.obtainMessage(1, msg.obj);
                            ((FriendQuery) xwContext).mHandler.sendMessage(msgfriend);
                        }

                }
                break;


                ///////////////////2014-08-02，把回调函数的界面处理都放到这里，避免界面操作同步冲突
                case XWDataCenterMessage.MSG_31: {
                    if (xwContext == null) break;
                    if (xwContext instanceof FriendVideoDisplay) {
                        ((FriendVideoDisplay) xwContext).mHandler.sendEmptyMessage(24);
                    } else if (xwContext instanceof FriendCall) {
                        ((FriendCall) xwContext).mHandler.sendEmptyMessage(6);
                    }
                }
                break;

                case XWDataCenterMessage.MSG_32: {
                    if (xwContext == null) break;
                    if (xwContext instanceof FriendControl) {
                        //				((SelectFriendUI)xwContext).mHandler.sendEmptyMessage(2);
                        Message msg1 = ((FriendControl) xwContext).mHandler.obtainMessage(2, msg.obj);
                        ((FriendControl) xwContext).mHandler.sendMessage(msg1);
                    }
                }
                break;


                case XWDataCenterMessage.MSG_33: {
                    if (xwContext == null) break;
                    if (xwContext instanceof FriendCall) {
                        ((FriendCall) xwContext).mHandler.sendEmptyMessage(6);
                    } else if (xwContext instanceof FriendVideoDisplay) {
                        ((FriendVideoDisplay) xwContext).mHandler.sendEmptyMessage(24);
                    } else if (xwContext instanceof FriendControl) {  ///////////显示当前余额
                        ((FriendControl) xwContext).mHandler.sendEmptyMessage(27);
                    }
                }
                break;

                case XWDataCenterMessage.MSG_34: {
                    if (xwContext == null) break;

                    if (xwContext instanceof FriendControl) {
                        Message msg1 = ((FriendControl) xwContext).mHandler.obtainMessage(8, msg.obj);
                        ((FriendControl) xwContext).mHandler.sendMessage(msg1);
                    }

                }
                break;

                case XWDataCenterMessage.MSG_35: {

                    if (xwContext instanceof FriendCall) {
                        ((FriendCall) xwContext).mHandler.sendEmptyMessage(6);/////拨号状态更新
                    } else if (xwContext instanceof FriendVideoDisplay) {
                        ((FriendVideoDisplay) xwContext).mHandler.sendEmptyMessage(24);////通话时间状态更新
                    } else if (xwContext instanceof FriendControl) {  ///////////状态更新
                        ((FriendControl) xwContext).mHandler.sendEmptyMessage(27);
                    }


                }
                break;

                case XWDataCenterMessage.MSG_36: {//////刷新好友信息
                    if (xwContext == null) break;
                    if (xwContext instanceof FriendControl) {

                            ((FriendControl) xwContext).mHandler.removeMessages(4);
                            ((FriendControl) xwContext).mHandler.sendEmptyMessageDelayed(4, 500);


                    } else if (xwContext instanceof FriendChatRecord) {
                        Message msg1 = ((FriendChatRecord) xwContext).mHandler.obtainMessage(6, msg.obj);

                            ((FriendChatRecord) xwContext).mHandler.removeMessages(6);
                            ((FriendChatRecord) xwContext).mHandler.sendMessageDelayed(msg1, 500);

                    } else if (xwContext instanceof FriendDetailUI) {

                            ((FriendDetailUI) xwContext).mHandler.removeMessages(FriendDetailUI.MSG_UPDATE_DETAIL);
                            ((FriendDetailUI) xwContext).mHandler.sendEmptyMessageDelayed(FriendDetailUI.MSG_UPDATE_DETAIL, 500);

                    }

                }
                break;

                case XWDataCenterMessage.MSG_37: {
                    ////清理好友
                    FriendNodeInfo fni = (FriendNodeInfo) msg.obj;
                    XWDataCenter.deleteFriend(fni);
                    //通知更新
                    if (xwContext != null) {
                        if (xwContext instanceof FriendControl) {
                            Message updatemsg = ((FriendControl) xwContext).mHandler.obtainMessage(7, fni);
                            ((FriendControl) xwContext).mHandler.sendMessage(updatemsg);
                        } else if (xwContext instanceof FriendChatRecord) {
                            Message updatemsg = ((FriendChatRecord) xwContext).mHandler.obtainMessage(8, fni);
                            ((FriendChatRecord) xwContext).mHandler.sendMessage(updatemsg);
                        }else if(xwContext instanceof SelectFriendUI){
                            ((SelectFriendUI) xwContext).showDelToast(fni);
                        }else{
                            ///toast ??
                        }
                    }

                }
                break;

                case XWDataCenterMessage.MSG_38: {/////更新好友列表头像
                    FriendControl.getHeadBeanTask(XWDataCenter.getCurAccount());
                }
                break;

                case XWDataCenterMessage.MSG_39: {
                    FriendNodeInfo fni = (FriendNodeInfo) msg.obj;

                    if (xwContext == null) break;//查询结果数据操作不保存
                    if (xwContext instanceof FriendQuery) {
                        Message msg1 = ((FriendQuery) xwContext).mHandler.obtainMessage(1, fni);
                        ((FriendQuery) xwContext).mHandler.sendMessage(msg1);
                    } else {
                        Message msg1 = XWDataCenter.xwDC.XWMsghandle.obtainMessage(27, fni);
                        XWDataCenter.xwDC.XWMsghandle.sendMessageDelayed(msg1, 2000);
                    }

                }
                break;

                case XWDataCenterMessage.MSG_40: {//////好友邀请消息处理 TODO ...
                    MessageParamA mpa = (MessageParamA) msg.obj;
                    if (XWDataCenter.xwContext == null) break;
                    if (xwContext instanceof FriendControl) {
                      if(xwContext.bIsFront){
                          Message msg1 = ((FriendControl) xwContext).mHandler.obtainMessage(6, mpa);
                          ((FriendControl) xwContext).mHandler.sendMessage(msg1);
                      }
                    } else if (XWDataCenter.iNetphoneStatus == 0) ////非电话状态,转好友界面。////TODO 需要避免FriendControl重新onCreate
                    {
                        /////直接弹出提示框
                        if(xwContext.bIsFront){
                           xwContext.getSystemMessageAlert();
                        }

//                        try {
//                            Intent nextPage = new Intent();
//                            nextPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            nextPage.setClass(MainApplication.getInstance(), FriendControl.class);
//                            MainApplication.getInstance().startActivity(nextPage);
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                        }
                    }

                }
                break;

                case XWDataCenterMessage.MSG_41: {
                    BusProvider.getInstance().post(new FragmentRereshEvent(FriendControl.CALLRECORD_INDEX));
                }
                break;

                case XWDataCenterMessage.MSG_42: {
                    if (xwContext != null) {
                        if (xwContext instanceof FriendCall) {
                            Message msg1 = ((FriendCall) xwContext).mHandler.obtainMessage(2, xwContext.getResources().getString(R.string.alert_status_begin));
                            ((FriendCall) xwContext).mHandler.sendMessage(msg1);
                        }else if (xwContext instanceof FriendControl) {
                            Message msg1 = ((FriendControl) xwContext).mHandler.obtainMessage(21,  xwContext.getResources().getString(R.string.alert_status_begin));
                            ((FriendControl) xwContext).mHandler.sendMessage(msg1);
                        }
                    }

                }
                break;

                case XWDataCenterMessage.MSG_43: {
                    if (xwContext != null) {
                        if (xwContext instanceof FriendCall) {
								/*XWAudioAlert.getAudioAlert().data=XWDataCenter.xwDC.xwCallAudio;
								XWAudioAlert.realFrequency=8000;
								XWAudioAlert.realBits=AudioFormat.ENCODING_PCM_16BIT;
								XWAudioAlert.getAudioAlert().startAudioAlert();//响铃*/
                            Message msg1 = ((FriendCall) xwContext).mHandler.obtainMessage(2, (Object) xwContext.getResources().getString(R.string.alert_status_calling));
                            ((FriendCall) xwContext).mHandler.sendMessage(msg1);
                        }else if (xwContext instanceof FriendControl) {
                            Message msg1 = ((FriendControl) xwContext).mHandler.obtainMessage(21, (Object) xwContext.getResources().getString(R.string.alert_status_calling));
                            ((FriendControl) xwContext).mHandler.sendMessage(msg1);
                        }
                    }

                }
                break;

                case XWDataCenterMessage.MSG_44: {
                    String loginName = (String) msg.obj;
                    if (!XWDataCenter.xwDC.calling_loginName.equals("")) return;
                    if (xwContext != null) {
                        if (xwContext instanceof FriendCall) {
                            //this.calling_fid=fni.getId();
                            //////XWDataCenter.xwDC.calling_loginName=loginName;
                            ((FriendCall) xwContext).mHandler.sendEmptyMessage(3);
                        } else if (xwContext instanceof FriendControl) {
                            ///////XWDataCenter.xwDC.calling_loginName=loginName;
                            ((FriendControl) xwContext).mHandler.sendEmptyMessage(20);
                        } else {
                            ///this.hangupNetPhone();
                        }
                    }

                }
                break;


                case XWDataCenterMessage.MSG_45: {
                    String loginName = (String) msg.obj;
                    xwDC.friendHungup=1;
                    if (xwContext != null) {
                        if (xwContext instanceof FriendVideoDisplay) {//在通话过程中挂断
                            XWDataCenter.xwDC.calling_loginName = "";
                            Message msg1 = ((FriendVideoDisplay) xwContext).mHandler.obtainMessage(5);
                            ((FriendVideoDisplay) xwContext).mHandler.sendMessage(msg1);
                        } else if (xwContext instanceof FriendCall) {//没有拨通情况下挂断,或主动拨号时被拒绝
                            XWDataCenter.xwDC.calling_loginName = "";
                            Message msg1 = ((FriendCall) xwContext).mHandler.obtainMessage(2, (Object) xwContext.getResources().getString(R.string.alert_that_hungup));
                            ((FriendCall) xwContext).mHandler.sendMessage(msg1);
                        } else if (xwContext instanceof FriendControl) {//没有拨通情况下挂断,或主动拨号时被拒绝
                            XWDataCenter.xwDC.calling_loginName = "";
                            ((FriendControl) xwContext).mHandler.sendEmptyMessage(14);
                        }else {
                            XWDataCenter.xwDC.calling_loginName = "";
                          xwContext.showToastTips(xechwic.android.XWCodeTrans.doTrans("对方已挂断"));
                        }
                    } else {
                        XWDataCenter.xwDC.calling_loginName = "";
                    }

                    XWDataCenter.xwDC.XWMsghandle.sendEmptyMessageDelayed(XWDataCenterMessage.MSG_3, 3000);

                }
                break;


                case XWDataCenterMessage.MSG_46: {
                    String loginName = (String) msg.obj;
                    if (xwContext != null) {
                        if (xwContext instanceof FriendCall) {
                            //this.calling_fid=fni.getId();
                            //////XWDataCenter.xwDC.calling_loginName=loginName;
                            ((FriendCall) xwContext).mHandler.sendEmptyMessage(3);
                        } else if (xwContext instanceof FriendControl) {
                            //////XWDataCenter.xwDC.calling_loginName=loginName;
                            ((FriendControl) xwContext).mHandler.sendEmptyMessage(20);
                        }else {
                            ///this.hangupNetPhone();
                        }
                    }
                }
                break;


                case XWDataCenterMessage.MSG_47: {
                    if (xwContext != null) {
                        if (xwContext instanceof FriendVideoDisplay) {
                            /////////if(XWDataCenter.xwDC.calling_loginName.equals("")) return;//已经挂断
                            XWDataCenter.xwDC.calling_loginName = "";
                            Message msg1 = ((FriendVideoDisplay) xwContext).mHandler.obtainMessage(5);
                            ((FriendVideoDisplay) xwContext).mHandler.sendMessage(msg1);
                        } else if (xwContext instanceof FriendCall) {//没有拨通情况下挂断,或主动拨号时被拒绝
                            XWDataCenter.xwDC.calling_loginName = "";
                            Message msg1 = ((FriendCall) xwContext).mHandler.obtainMessage(2, (Object) xwContext.getResources().getString(R.string.alert_that_hungup));
                            ((FriendCall) xwContext).mHandler.sendMessage(msg1);
                            //this.hangupNetPhone();
                        } else if (xwContext instanceof FriendControl) {
                            XWDataCenter.xwDC.calling_loginName = "";
                            //						Message msg=((SelectFriendUI)xwContext).mHandler.obtainMessage(14,fni);
                            //						((SelectFriendUI)xwContext).mHandler.sendMessage(msg);
                            ((FriendControl) xwContext).mHandler.sendEmptyMessage(14);
                            ///this.hangupNetPhone();
                        }else {
                            XWDataCenter.xwDC.calling_loginName = "";
                            ///this.hangupNetPhone();
                        }
                        //Toast.makeText(xwContext,"对方已挂断",1000);
                    } else {
                        XWDataCenter.xwDC.calling_loginName = "";
                        ////this.hangupNetPhone();
                    }
                    XWDataCenter.xwDC.XWMsghandle.sendEmptyMessageDelayed(3, 3000);
                }
                break;

                case XWDataCenterMessage.MSG_48: {
                    int type = ((Integer) msg.obj);
                    //Log.e("tag", "call error type:"+type);
                    if (xwContext != null) {
                        if (xwContext instanceof FriendControl) {
                            Message msg1 = ((FriendControl) xwContext).mHandler.obtainMessage(23);
                            msg1.arg1 = type;
                            ((FriendControl) xwContext).mHandler.sendMessage(msg1);
                        }else if (xwContext instanceof FriendCall) {
                            Message msg1 = ((FriendCall) xwContext).mHandler.obtainMessage(1);
                            msg1.arg1 = type;
                            ((FriendCall) xwContext).mHandler.sendMessage(msg1);
                        }
                    }
                }
                break;

                case XWDataCenterMessage.MSG_50: {
                    if (xwContext == null) break;
                    if (xwContext instanceof FriendVideoDisplay) {
                        ////if(type==1){
                        ((FriendVideoDisplay) xwContext).mHandler.sendEmptyMessage(21);
                        ///}else if(type==2){
                        //	((FriendVideoDisplay)xwContext).mHandler.sendEmptyMessage(22);
                        ///}
                    }
                }
                break;
                case XWDataCenterMessage.MSG_51: {
                    if (xwContext == null) break;
                    if (xwContext instanceof FriendVideoDisplay) {
                        ////if(type==1){
                        ///	((FriendVideoDisplay)xwContext).mHandler.sendEmptyMessage(21);
                        ///}else if(type==2){
                        ((FriendVideoDisplay) xwContext).mHandler.sendEmptyMessage(22);
                        ///}
                    }
                }
                break;

                case XWDataCenterMessage.MSG_52: {
                    int res = ((Integer) msg.obj);
                    if (res == 3) {//登录成功
                        BusProvider.getInstance().post(new LoginEvent(1));
                    } else {//失败
                        BusProvider.getInstance().post(new LoginEvent(2));
                    }

                }
                break;

                case XWDataCenterMessage.MSG_53: {
                    if (xwContext instanceof FriendControl) {
                        Message msg1 = ((FriendControl) xwContext).mHandler.obtainMessage(16, xwContext.getResources().getString(R.string.alert_reconnection));
                        ((FriendControl) xwContext).mHandler.sendMessage(msg1);
                    } else if (xwContext instanceof FriendCall) {
                        ((FriendCall) xwContext).mHandler.sendEmptyMessage(4);
                    }
                }
                break;

                case XWDataCenterMessage.MSG_54: {
                    FriendChatRecordInfo fcri = (FriendChatRecordInfo) msg.obj;
                    if (xwContext instanceof FriendChatRecord) {
                        Message msg1 = ((FriendChatRecord) xwContext).mHandler.obtainMessage(3, fcri);
                        ((FriendChatRecord) xwContext).mHandler.sendMessage(msg1);
                    }
                }
                break;

                case XWDataCenterMessage.MSG_55: //////////////更新加密锁图标状态
                {
//                    if(xwContext instanceof FriendChatRecord){
//
//                        ((FriendChatRecord) xwContext).UpdateAESIcon();
//                    }
                    BusProvider.getInstance().post(new UpdateAESEvent());
                }
                break;


                case XWDataCenterMessage.MSG_56: //////处理
                {
                    try {
                      final  RecordBean mRecordBean = (RecordBean) msg.obj;

                        if (mRecordBean == null)
                            break;
                        if (MainApplication.getInstance().getRecordList().size() > 128) {
                            MainApplication.getInstance().getRecordList().remove(MainApplication.getInstance().getRecordList().size() - 1);
                        }

                        MainApplication.getInstance().getRecordList().add(0, mRecordBean);
                        mRecordBean.setEndTime(System.currentTimeMillis());

                        TaskExecutor.executeTask(new Runnable() {
                            @Override
                            public void run() {

                                Log.e("xim","TaskExecutor.executeTask: mainthread "+(Looper.getMainLooper() == Looper.myLooper()));

                                //////////保存记录
                                ObjectIO.saveObject(MainApplication.getInstance().getRecordList(),
                                        UriConfig.getCallRecordPath());
                            }
                        });

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
                break;

                case XWDataCenterMessage.MSG_57: {
                    String sTemp = (String) msg.obj;

                    try {

                        int idx = sTemp.indexOf("\r");

                        if (idx > 0) {
                            final String sToUser = sTemp.substring(0, idx);
                            final String sRequestCommand = sTemp.substring(idx + 1);
                            TaskExecutor.executeTask(new Runnable() {
                                @Override
                                public void run() {
                                    XWDataCenter.SendCreditMessage2(sToUser, sRequestCommand);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;


                case XWDataCenterMessage.MSG_58://///显示消息
                {
                    try {
                        String sTemp = (String) msg.obj;
                        if(xwContext!=null){
                            xwContext.showToastTips(sTemp);
                        }
//                        Toast.makeText(MainApplication.getInstance(), sTemp, Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
                case XWDataCenterMessage.MSG_59://启动WakeUpActivity
                {
                    Intent intent=new Intent(MainApplication.getInstance(),WakeUpActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainApplication.getInstance().startActivity(intent);
                }
                break;
                case XWDataCenterMessage.MSG_60:////发送密钥协商
                {
                    try
                    {
                        final String sToUser=(String)msg.obj;
                        if(!TextUtils.isEmpty(sToUser)){

                                TaskExecutor.executeTask(new Runnable() {
                                    @Override
                                    public void run() {
                                        ///////////////////////////////
                                        String sAES=XWDataCenter.getFriendAESPassword(XWDataCenter.xwDC.loginName, XWDataCenter.RegularPhoneNumber(sToUser));
                                        if (sAES==null) {
                                            XWDataCenter.SendCreditMessage(XWDataCenter.RegularPhoneNumber(sToUser), XWDataCenter.CREDIT_REQUEST);
                                        }
                                    }
                                });
                            }


                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                break;
                case XWDataCenterMessage.MSG_61://///亮屏一次
                {
//                    if(NetWorkUtil.isNetworkConnected(MainApplication.getInstance())
//                            || PrefsUtils.getInstance().get(JRSConstants.KEY_CONNECT_STATUS,false)){
                     Log.e("xim","start up wake activity");
                    Intent intent=new Intent(MainApplication.getInstance(),WakeUpActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainApplication.getInstance().startActivity(intent);
                        /////增加一次亮屏时间间隔

//                }

                }
                break;
                case XWDataCenterMessage.MSG_62:////一个像素的activity
                {
                    if(!XWScreenOnOff.getScreenOn()){
                        Intent keepIntent = new Intent(MainApplication.getInstance(), KeepLiveActivity.class);
                        keepIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainApplication.getInstance().startActivity(keepIntent);
                    }

                }
                break;
                case XWDataCenterMessage.MSG_63:////重新登录
                {
                    MainApplication.getInstance().startUpXWService();

                }
                break;

                case XWDataCenterMessage.MSG_300:////收到群聊消息
                {
                    if (XWDataCenter.xwContext==null)
                        break;

                    // //////////////由底层填如下参数。
                    String sFromUser;
                    String sMsgType; // ///////// "1","0"
                    String sText = ""; // ////////收到的文字消息
                    String sFile = ""; // /// 可能是xwx语音,jpg/bmp/png/gif等图片,
                    double fLat = 0; // ///////经纬度
                    double fLon = 0;
                    Date senddt ; // ////////发送时间
                    String sGroup = ""; // ///来自的群组名,留言板为XWBOARD

                    try {
                        String sData = (String) msg.obj;

                        if (sData == null)
                            break;

                        Log.v("XIM", "Got group msg:" + sData);

                        int idx1, idx2;
                        idx1 = 0;
                        idx2 = sData.indexOf("\\", idx1);

                        if (idx2 < 0)
                            break;

                        idx1 = idx2 + 1;

                        idx2 = sData.indexOf("\\", idx1);
                        if (idx2 < 0)
                            break;

                        sMsgType = sData.substring(idx1, idx2);

                        idx1 = idx2 + 1;
                        idx2 = sData.indexOf("\\", idx1);
                        if (idx2 < 0)
                            break;

                        sGroup = sData.substring(idx1, idx2);

                        idx1 = idx2 + 1;
                        idx2 = sData.indexOf("\\", idx1);
                        if (idx2 < 0)
                            break;

                        sFromUser = sData.substring(idx1, idx2);

                        String sFromUserSignName = "";
                        idx1 = sFromUser.indexOf("(");

                        if (idx1 >= 0) {
                            sFromUserSignName = sFromUser.substring(idx1 + 1,
                                    sFromUser.length() - 1);
                            sFromUser = sFromUser.substring(0, idx1);
                        }

                        idx1 = idx2 + 1;
                        idx2 = sData.indexOf("\\", idx1);
                        if (idx2 < 0)
                            break;

                        String sCordinate = sData.substring(idx1, idx2);

                        int idx3 = sCordinate.indexOf(",");
                        if (idx3 < 0)
                            break;
                        try {
                            fLon = Double.valueOf(sCordinate.substring(0, idx3));
                            fLat = Double.valueOf(sCordinate.substring(idx3 + 1));
                        } catch (Exception ex) {

                        }

                        idx1 = idx2 + 1;
                        idx2 = sData.indexOf("\\", idx1);
                        if (idx2 < 0)
                            break;

                        try {
                            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                            senddt = df.parse(sData.substring(idx1, idx2));

                        } catch (Exception e) {
                            senddt = new Date(System.currentTimeMillis());
                        }

                        idx1 = idx2 + 1;

                        if (sMsgType.equalsIgnoreCase("1")) // //文件url
                        {
                            sFile = sData.substring(idx1);
                        } else {
                            sText = sData.substring(idx1);
                        }

                        // ////////////////////////2013-09-13,在这里处理收到的群消息

                       /// Log.e("XIM", "Got group msg from " + sFromUser + "," + sMsgType
                        //        + "," + sText + "," + sFile + ","
                        ///        + "," + sGroup);

                        //////////////////////在这里判断是否是群聊天消息
                        ///////////////2017-09-04
                        {
                            if (sGroup.startsWith(XWDataCenter.XIM_IM_GROUPCHAT_PREX))
                            {
                                sGroup=sGroup.substring(XWDataCenter.XIM_IM_GROUPCHAT_PREX.length());

                                if (sText.startsWith(XWDataCenter.XIM_IM_GROUPCHATSYSTEMNOTICE_PREX))
                                {
                                    sText=sText.substring(XWDataCenter.XIM_IM_GROUPCHATSYSTEMNOTICE_PREX.length());
                                    /////////////////处理群聊天系统通知!!!!!!!!!!!!!!!!!!
                                    /////////////sFromUser为动作发起者,如果为邀请通知，则为邀请人的账号。
                                    /*////////////sText为如下格式
                                    [用户账号][动作]
                                    动作为如下字符串
                                    enter:进入群
                                    leave:离开群
                                    kicked:被踢出
                                    new_owner:成为新群主
                                    update_info:群信息更新了
                                    update_key:加密key更新了
                                    change_name:更新群昵称
                                     */


                                    Log.e("XIM", "Got group chat msg notice from " + sFromUser + "," + sMsgType
                                            + "," + sText + "," + sFile + ","
                                            + "," + sGroup);

                                    Message msg1=obtainMessage(XWDataCenterMessage.MSG_17, "Got group chat msg notice from " + sFromUser + "," + sMsgType
                                                    + "," + sText + "," + sFile + ","
                                                    + "," + sGroup);
                                    sendMessage(msg1);


                                    //////////////////////////测试发送文字!!!!!!!!!!!!!!!!
                                    ////XWDataCenter.xwDC.XWXIMGroupChatRequestSendFile(0,"3\u0000".getBytes(),"hello!\u0000".getBytes(),"0\u0000".getBytes(),"0\u0000".getBytes(),new byte[33]);

                                }
                                else
                                {
                                    /////////////////////////////处理群聊天收到的消息!!!!!!!!!!!!!!2017-09-04

                                    Log.e("XIM", "Got group chat msg  from " + sFromUser + "," + sMsgType
                                            + "," + sText + "," + sFile + ","
                                            + "," + sGroup);

                                    Message msg1=obtainMessage(XWDataCenterMessage.MSG_17, "Got group chat msg  from " + sFromUser + "," + sMsgType
                                            + "," + sText + "," + sFile + ","
                                            + "," + sGroup);
                                    sendMessage(msg1);

                                }
                            }

                        }

                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }



                    }
                break;
                default:
                    break;
            }//switch

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        super.handleMessage(msg);

    }

}/////////class
