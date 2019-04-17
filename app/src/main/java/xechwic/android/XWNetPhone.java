package xechwic.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.umeng.analytics.MobclickAgent;
import java.io.File;
import xechwic.android.act.MainApplication;
import xechwic.android.act.ServerConfig;
import xechwic.android.ui.BaseUI;
import xechwic.android.util.FileUtil;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.PrefsUtils;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.UriConfig;
import ydx.securephone.BuildConfig;
import ydx.securephone.R;

import static xechwic.android.util.FileUtil.deleteGuardFile;

/**界面入口
 * @author luman
 *
 */
public class XWNetPhone extends BaseUI {



	private XWDataCenter xwDC;



	private Handler myHandler  = new Handler(){

		@Override
		public void handleMessage(Message msg) {
		    switch (msg.what) {
				case 1:
					if(autoLogin){//需要自动登录
						///先关闭之前的界面
						xwDC.activityList.remove(baseAct);
						xwDC.clearActList();

						Intent i=new Intent(XWNetPhone.this, FriendLogin.class);
						i.setAction(JRSConstants.KEY_AUTO_LOGIN);
						i.putExtra(JRSConstants.KEY_USER_ACCOUNT,mAccount);
						i.putExtra(JRSConstants.KEY_USER_PASSWORD,mPasswd);
						startActivity(i);
					}else{
						Intent nextPage=new Intent();
						nextPage.setClass(XWNetPhone.this, FriendLogin.class);
						startActivity(nextPage);
					}

					baseAct.finish();
					break;
				case 2:
					returnActivity();
					break;
			default:
				break;
			}
		}
		
	};


	private String getStartUpCMD(){
		if(xwDC==null){
			MainApplication.getInstance().initXWDC();
			xwDC=XWDataCenter.xwDC;
		}
		if(xwDC.isLogin&& FileUtil.isGuardFileExist()){
            boolean brestore=xwDC.restoreFriendData();
			if(brestore){
				return JRSConstants.CMD_STARTUP_FRIENDCONTROL;
			}
		}
		return JRSConstants.CMD_STARTUP_LOGINUI;
	}

	/////恢复界面
	private void returnActivity(){
		Intent nextPage=new Intent();
		try
		{
			xwDC.activityList.remove(baseAct);
			Log.e("xwnetphone","xwDC.activityList.size"+xwDC.activityList.size());
			for(Activity act:xwDC.activityList){
				if(act!=null){
					Log.e("xwnetphone","xwnetphone:"+act.getLocalClassName());
				}
			}
			if(xwDC.activityList.size()>1){
				Activity act=xwDC.activityList.get(xwDC.activityList.size()-1);
				if(act!=null){
					System.gc();
					nextPage.setClass(XWNetPhone.this, act.getClass());
					nextPage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					baseAct.finish();
					startActivity(nextPage);
				}else{
					xwDC.clearActList();
					baseAct.finish();
					startUpFriendControl();
				}
			}else if(xwDC.activityList.size()==1){
				baseAct.finish();
				startUpFriendControl();
			}
			else{
				baseAct.finish();
				startUpFriendControl();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/////启动主界面
	private void startUpFriendControl(){
		System.gc();
		Intent nextPage=new Intent();
		nextPage.setClass(XWNetPhone.this, FriendControl.class);
		nextPage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(nextPage);
	}
	
	private TextView label_version;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ui_xwnetphone);
        
        label_version=(TextView)this.findViewById(R.id.label_version);
        {
        	PackageManager manager = this.getPackageManager();
            try { PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            label_version.setText(info.versionName); //版本名
             } catch (Exception e) {
             e.printStackTrace();
             }
        }


        Log.v("XIM","Activity XWNetPhone onCreate.");
		if(checkThirdChangeLogin()){
			TaskExecutor.executeTask(new Runnable() {
				@Override public void run() {
					try {
						if(xwDC==null){
							MainApplication.getInstance().initXWDC();
							xwDC=XWDataCenter.xwDC;
						}
						deleteGuardFile();
						xwDC.isLogin=false;
						////先断开连接
						xwDC.reActive(0);
						////关闭数据库
						XWDataCenter.closeDB();
						com.example.mcryptolmsdimpl_demo.MainActivity.removekeys(XWDataCenter.getCurAccount());
						UriConfig.deleteAll();
						//重置未完成发送信息
						XWDataCenter.xwDC.setResendMsg();
						//删除所有解码文件
						XWDataCenter.clearAllDecrypt();
						////清理视频临时文件
						UriConfig.deleteInDir(UriConfig.getVideoSavePath());
						/////////////////2014-06-16,指示是否通讯层初始化!!!!!!!!!!!!!!!!!
						xwDC.isLogin = false;
						////////xwDC.logoutService(0);
						Log.e("xim", "logoutService ok");
						////////////////2014-09-17,清理加密的文件
						{
							String aespath = UriConfig.getSavePath() + "/aesfiles";
							UriConfig.delete(aespath);
						}
						/////重建文件夹
						File file = new File(XWDataCenter.sProgPath);
						if (!file.isDirectory()) {
							try {
								file.mkdirs();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						file = new File(XWDataCenter.sProgPath+ UriConfig.USER_DATA_DIR);
						if (!file.isDirectory()) {
							try {
								file.mkdirs();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						///重建数据库文件
						XWDataCenter.initDB();
						///重设IP
						XWDataCenter.setXIMIP(mIP);
						ServerConfig.config(mIP);
						/////////////////////////2016-10-31,设置通讯ip和端口,通讯端口默认8899
						try {
							XWDataCenter.xwDC.setServerIPPort((XWDataCenter.getXIMIP() + "\0").getBytes("iso-8859-1"), 8899);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}catch (Exception e){
						e.printStackTrace();
					}
					FriendLogin.saveAccountPasswd(mAccount,mPasswd,true);
					PrefsUtils.getInstance().put(JRSConstants.KEY_AUTO_LOGIN,true);
					myHandler.sendEmptyMessage(1);
				}
			});
		}else{
			initData();
		}


    }

    private boolean autoLogin=false;
	  private String mAccount,mPasswd,mIP;
    private boolean checkThirdChangeLogin(){
			boolean isChange=false;
			Intent intent=getIntent();
			if(intent!=null){
				String action=intent.getAction();
				if(!TextUtils.isEmpty(action)&&action.equals(JRSConstants.CMD_ACTION_AUTOLOGIN)){
					 String account=intent.getStringExtra(JRSConstants.KEY_USER_ACCOUNT);
					 String passwd=intent.getStringExtra(JRSConstants.KEY_USER_PASSWORD);
					 String ip=intent.getStringExtra(JRSConstants.KEY_XIM_IP);
					Log.e("XWNetPhone","ip"+ip+ ",account:" + account + ",passwd:" + passwd);
					autoLogin=true;
					mAccount=account;
					mPasswd=passwd;
          mIP=ip;
					//save ip
					if (!XWDataCenter.getXIMIP().equals(ip)) {
						isChange=true;
					}
					if(!TextUtils.isEmpty(account)&&!TextUtils.isEmpty(passwd)){
						if(FriendLogin.checkAccountChange(account)) {//账户变化清理
							Log.e("XW","account change");
							isChange=true;
						}
					}
				}
				setIntent(null);
			}
				return isChange;
		}


	private void initData(){
		final long startTime=System.currentTimeMillis();
		new AsyncTask<String,Integer,String>(){
			@Override
			protected String doInBackground(String... params) {
				/////初始化XWDC
				MainApplication.getInstance().initXWDC();
				xwDC=XWDataCenter.xwDC;
				// //////////初始化数据库!!!!!!
				XWDataCenter.initDB();
				////////初始化通话记录///////
				MainApplication.getInstance().getRecordList();
				//////初始化友盟
				initUmeng();
				//////判断启动方式
				String cmd=getStartUpCMD();
				return cmd;
			}

			@Override
			protected void onPostExecute(String s) {
				if(bIsDestroy){
					return;
				}
				int type=1;
				if(JRSConstants.CMD_STARTUP_FRIENDCONTROL.equals(s)){
					type=2;
				}
				if(((System.currentTimeMillis()-startTime)*0.001)<1){
					Log.e("XWNetPhone","deley");
					myHandler.sendEmptyMessageDelayed(type, JRSConstants.SECOND_VALUE);
				}else{
					Log.e("XWNetPhone","not deley");
					myHandler.sendEmptyMessage(type);
				}
			}
		}.execute("");

	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

	}



	//初始化友盟统计
	private void initUmeng(){
		//统计日志传输加密
		MobclickAgent.enableEncrypt(true);
		//打开调试模式
		MobclickAgent.setDebugMode(BuildConfig.DEBUG);
		// SDK在统计Fragment时，需要关闭Activity自带的页面统计，
		// 然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
		MobclickAgent.openActivityDurationTrack(false);
		MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
	}
}