/*
 *UI基类
 * */
package xechwic.android.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.example.mcryptolmsdimpl_demo.MainActivity;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import ydx.securephone.R;
import xechwic.android.FriendControl;
import xechwic.android.FriendGroupInfo;
import xechwic.android.FriendLogin;
import xechwic.android.FriendNodeInfo;
import xechwic.android.FriendVideoDisplay;
import xechwic.android.MessageParamA;
import xechwic.android.XWCodeTrans;
import xechwic.android.XWDataCenter;
import xechwic.android.XWNetPhone;
import xechwic.android.act.MainApplication;
import xechwic.android.bean.ChatHistoryBean;
import xechwic.android.bean.ChatMsgEntity;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.AvatarUpdateEvent;
import xechwic.android.crop.CropUtil;
import xechwic.android.util.FileUtil;
import xechwic.android.util.IMEUtils;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.MIMEUtil;
import xechwic.android.view.LoadingDialog;
import xechwic.android.view.ToastUtil;

import static xechwic.android.util.FileUtil.deleteGuardFile;

public class BaseUI extends AppCompatActivity {

	/////好友邀请处理
	public String optionSelect = null;// 当前被选择中的登录状态
	public LinkedList<MessageParamA> msgParamList = null;// 系统消息队列

	private LoadingDialog loadlg =null;//加载进度圈
	private NormalDialog logoutDlg; //退出提示框
	private NormalDialog destroyDlg;//注销提示
	public BaseUI baseAct;
	public XWDataCenter xwDC;
	public boolean bIsFront=false;//当前Activity是否显示
	public boolean bIsDestroy=false;//是否执行了destroy
	////private PowerManager.WakeLock mWakeLock;//屏幕锁

	protected boolean bIsInterrupt=false;//界面回收重启恢复数据被打断
	protected boolean bIsRestore=false;//界面回收重启




	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		baseAct=this;
        checkRetoreInstanceState(savedInstanceState);
	}

	////恢复数据
	protected void doRetoreInstanceState(Bundle saveInstanceState){

	}

	//////检测是否可恢复数据
	protected void checkRetoreInstanceState(Bundle savedInstanceState){
		if (savedInstanceState != null
				&& savedInstanceState.getInt(JRSConstants.SAVE_STATE) != 0) {
			bIsRestore = true;
			////清理fragment状态
			String FRAGMENTS_TAG = "Android:support:fragments";
			savedInstanceState.remove(FRAGMENTS_TAG);
			/////清理Intent
			setIntent(null);
			if (XWDataCenter.xwDC != null && XWDataCenter.xwDC.isLogin) {
				//////已经登录并且数据还在则不跳转,否则进入XWNetPhone进行业务判断
				if (FriendControl.friendList.isEmpty() || !MainActivity.CheckSDCard(this)) {
					bIsInterrupt = true;
					baseAct.finish();
					startActivity(new Intent(this, XWNetPhone.class));
				} else {
					/////界面可恢复
                    doRetoreInstanceState(savedInstanceState);
				}
			} else {
				/////被系统回收后重新进入处理
				bIsInterrupt = true;
				finish();
				startActivity(new Intent(this, FriendLogin.class));
			}
		}
	}

	////保存数据
	protected void doSaveInstanceState(Bundle outState){
		outState.putInt(JRSConstants.SAVE_STATE, 1);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState); ////不保存fragment状态
		doSaveInstanceState(outState);
	}


	/**从headMap同步好友列表头像
	 */
	public void updateFriendIcon(List<FriendNodeInfo> friendList){
		if(friendList==null||friendList.isEmpty()||XWDataCenter.headBeanMap==null||XWDataCenter.headBeanMap.isEmpty()){
			return;
		}
		for(FriendNodeInfo node:friendList){
			if(node!=null&&node.getLogin_name()!=null){
				String icon =XWDataCenter.headBeanMap.get(node.getLogin_name());
				if(!TextUtils.isEmpty(icon)){
					node.setIcon(icon);
				}
			}
		}

	}


	/////打开文件目录
	public  void openAssignFolder(){
		try {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			startActivityForResult(intent, CropUtil.REQ_FILEDIR);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/////进行拨号前检测
	public boolean makeCallCheck(FriendNodeInfo fni){
		if(fni==null){
			return false;
		}
		/////判断当前连接状态和对方的在线状态
		if(!XWDataCenter.xwDC.isConnected){
			showToastTips(XWCodeTrans.doTrans("正在连接，请保持网络连接正常!"));
			return false;
		}
		String friendStatus=fni.getOnline_status()==null? XWCodeTrans.doTrans("断开"):fni.getOnline_status();
		if(friendStatus.contains(XWCodeTrans.doTrans("断开"))){
			showToastTips(XWCodeTrans.doTrans("对方未在线，请稍后再试!"));
			return false;
		}
		if(xwDC.remoteVideoRunning){
			//////已经在视频
			if(!TextUtils.isEmpty(xwDC.sCurrentPhoneNumber)){
				if(fni.getLogin_name().equals(xwDC.sCurrentPhoneNumber)){
					startActivity(new Intent(baseAct, FriendVideoDisplay.class));
					baseAct.finish();
					return false;
				}else{
					showToastTips(XWCodeTrans.doTrans("在和其他人视频中！"));
					return false;
				}
			}
		}
		//////发送一个在线消息
		sendOnlineMessage(fni, JRSConstants.MSG_CALL_PRE);
		return true;
	}


	/////使用WPS打开
	public static void openFileByWps(Context context,File file){
		Intent intent = new Intent();

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setClassName("cn.wps.moffice", "cn.wps.moffice.documentmanager.PreStartActivity");
		Uri uri = Uri.fromFile(file);
		intent.setData(uri);
		context.startActivity(intent);
	}



	/**打开文件
	 */
	public void openFile(final File f) {
			String type = MIMEUtil.getMIMEType(f);
			if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
				if(MIMEUtil.isOfficeFile(f)){
					try{
						//////office文件尝试使用wps打开
						openFileByWps(baseAct,f);
						return;
					}catch (Exception e){
						e.printStackTrace();
					}

				}

            /* 设置intent的file与MimeType */
				try {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setAction(android.content.Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(f), type);
					startActivity(intent);
				}catch (Exception e){
					e.printStackTrace();
					showToastTips(XWCodeTrans.doTrans("请安装相应软件打开"));
				}
			} else {
				// unknown MimeType
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
				dialogBuilder.setTitle("请选择文件类型");
				CharSequence[] menuItemArray = new CharSequence[] {
						XWCodeTrans.doTrans("文本"),
						XWCodeTrans.doTrans("音频"),
						XWCodeTrans.doTrans("视频"),
						XWCodeTrans.doTrans("图像") };
				dialogBuilder.setItems(menuItemArray,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String selectType = "*/*";
								switch (which) {
									case 0:
										selectType = "text/plain";
										break;
									case 1:
										selectType = "audio/*";
										break;
									case 2:
										selectType = "video/*";
										break;
									case 3:
										selectType = "image/*";
										break;
								}
								try {
									Intent intent = new Intent();
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.setAction(android.content.Intent.ACTION_VIEW);
									intent.setDataAndType(Uri.fromFile(f), selectType);
									startActivity(intent);
								}catch (Exception e){
									e.printStackTrace();
									showToastTips(XWCodeTrans.doTrans("请安装相应软件打开"));
								}
							}
						});
				dialogBuilder.show();
			}



	}
	/**
	 * 显示加载框
	 */
	public void showPlg(String tx){
		try
		{
			if(loadlg==null){
				loadlg= new LoadingDialog(this,tx);
			}
			loadlg.show();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 关闭加载框
	 */
	public void disPlg(){
		try
		{
			if(loadlg!=null&&loadlg.isShowing()&&this!=null&&!this.isFinishing()){
				loadlg.dismiss();
				loadlg=null;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 提示
	 */
	public void showToastTips(String text){
		if(bIsFront)
		ToastUtil.getInstance(baseAct).show(text);
	}


	/**
	 * 初始化好友列表
	 */
	public void executeSystemMessage() {
		this.msgParamList = xwDC.msgParamList;
		if (this.msgParamList!=null&&!this.msgParamList.isEmpty()) {
			this.getSystemMessageAlert();
		}
	}

	/**
	 * 处理系统消息
	 */
	public void exeSystemMessage(int msgType, final FriendNodeInfo fni,
								 final String groupName) {
		if (msgType == 1) {//好友邀请
			final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
			final LinearLayout buildLayout = new LinearLayout(this);
			builder.setTitle(this.getResources().getString(R.string.alert_from)
					+ fni.getLogin_name()           ////////////////2014-07-03,显示账号
					+ this.getResources().getString(
					R.string.alert_friend_request));
			StringBuffer sb = new StringBuffer();
			sb.append(this.getResources().getString(R.string.alert_remarks));
			sb.append(fni.getSignName());
			sb.append("\n");
			sb.append(this.getResources().getString(R.string.alert_message));
			sb.append(fni.getOnline_status()==null?XWCodeTrans.doTrans("断开"):fni.getOnline_status());
			builder.setMessage(sb.toString());
			ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			buildLayout.setLayoutParams(layoutParams);

			final Spinner groupSpin = new Spinner(this);
			ArrayList<String> groupList = new ArrayList<String>();
			for (int i = 0; i < xwDC.groupsInfo.size(); i++) {
				FriendGroupInfo fgi = (FriendGroupInfo) xwDC.groupsInfo.get(i);
				groupList.add(fgi.getGroupName());
			}
			groupSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> adpView, View view,
										   int id, long position) {
					String selected = adpView.getItemAtPosition(id).toString();
					optionSelect = selected;
				}

				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
			ArrayAdapter<String> group_adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, groupList);
			group_adapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			groupSpin.setAdapter(group_adapter);
			groupSpin.setLayoutParams(layoutParams);
			buildLayout.addView(groupSpin);
			builder.setView(buildLayout);
			builder.setPositiveButton(
					this.getResources().getString(R.string.alert_accept),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
											int whichButton) {
							try {

								int iret=xwDC.manageFN("2".getBytes("GBK"), fni
										.getId(), (fni.getLogin_name() + "\0")
										.getBytes("GBK"), (xechwic.android.XWCodeTrans.doTransInput(optionSelect) + "\0")
										.getBytes("GBK"), "".getBytes("GBK")) ;
								Log.v("XIM","xwDC.manageFN "+iret);
								if (iret==0) {
									try
									{
										XWDataCenter.xwDC.hasReceiveMessageByUserName(0,(fni.getLogin_name()),
												(xechwic.android.XWCodeTrans.doTrans("好友添加成功.")+"\0").getBytes("GBK"),
												(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\0").getBytes("GBK")
										);
										Log.v("XIM","Accept friend:"+fni.getLogin_name());
										///刷新
										BusProvider.getInstance().post(new AvatarUpdateEvent(fni));
										////同时下载头像
										XWDataCenter.xwDC.updateAHeadPic(XWDataCenter.headBeanMap, fni);
									}
									catch(Exception ex)
									{
										ex.printStackTrace();
									}

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							dialog.dismiss();
						}
					});
			builder.setNeutralButton(
					this.getResources()
							.getString(R.string.alert_request_reject),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
											int whichButton) {
							try {
								xwDC.manageFN("3".getBytes("GBK"), fni.getId(),
										(fni.getLogin_name() + "\0")
												.getBytes("GBK"),
										(xechwic.android.XWCodeTrans.doTransInput(groupName) + "\0").getBytes("GBK"), ""
												.getBytes("GBK"));
							} catch (Exception e) {
								e.printStackTrace();
							}
							dialog.dismiss();


							///////////////在20秒后开始协商,为了等待好友信息已经更新。
							xwDC.postSendCreditMessage(fni.getLogin_name(),20000);
						}
					});
			try
			{
				builder.show();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		} else if (msgType == 2) {//好友接受邀请
			final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
			final LinearLayout buildLayout = new LinearLayout(this);
			if ((fni.getSignName() == null) || (fni.getSignName().equals(""))) {
				builder.setTitle(fni.getLogin_name()
						+ this.getResources().getString(
						R.string.alert_request_res));
			} else {
				builder.setTitle(fni.getSignName()
						+ this.getResources().getString(
						R.string.alert_request_res));
			}
			ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			buildLayout.setLayoutParams(layoutParams);
			builder.setView(buildLayout);


			builder.setPositiveButton(
					this.getResources().getString(R.string.alert_confirm),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
											int whichButton) {

							dialog.dismiss();
						}
					});
			try
			{
				builder.show();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}


			try
			{
				XWDataCenter.xwDC.hasReceiveMessageByUserName(0,(fni.getLogin_name()),
						(xechwic.android.XWCodeTrans.doTrans("好友添加成功.")+"\0").getBytes("GBK"),
						(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"\0").getBytes("GBK")
				);
				Log.v("XIM","Accept invite from friend:"+fni.getLogin_name());
				///刷新
				BusProvider.getInstance().post(new AvatarUpdateEvent(fni));
				////同时下载头像
				XWDataCenter.xwDC.updateAHeadPic(XWDataCenter.headBeanMap, fni);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}


			///////////////在10秒后开始协商,为了等待好友信息已经更新。
			xwDC.postSendCreditMessage(fni.getLogin_name(),10000);


		} else {//好友拒绝邀请
			final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
			final LinearLayout buildLayout = new LinearLayout(this);

			if ( (fni.getSignName()==null) || (fni.getSignName().equals("") ) )
			{
				builder.setTitle(fni.getLogin_name()
						+ this.getResources().getString(R.string.alert_reject));
			}
			else
				builder.setTitle(fni.getSignName()
						+ this.getResources().getString(R.string.alert_reject));
			ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			buildLayout.setLayoutParams(layoutParams);
			builder.setView(buildLayout);
			builder.setPositiveButton(
					this.getResources().getString(R.string.alert_confirm),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
											int whichButton) {
							dialog.dismiss();
						}
					});
			try
			{
				builder.show();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

	}

	/**
	 * 检测好友邀请
	 */
	public void getSystemMessageAlert() {
		if(msgParamList==null){
			msgParamList=xwDC.msgParamList;
		}
		if (this.msgParamList.size() > 0) {
			MessageParamA mpa = msgParamList.removeFirst();
			this.exeSystemMessage(mpa.getMsgType(), mpa.getFni(),
					mpa.getGroupName());
		}
	}


	/**
	 * 退出
	 */
	public void logout_XWIM() {

		        logoutDlg = new NormalDialog(this).isTitleShow(false)
				.content(getResources().getString(R.string.alert_exit_ask))
				.btnNum(2).btnText(getResources().getString(R.string.alert_cancel),
						getResources().getString(R.string.alert_confirm));
		        logoutDlg.setOnBtnClickL(new OnBtnClickL() {
					@Override
					public void onBtnClick() {
						logoutDlg.dismiss();
					}
				}, new OnBtnClickL() {
					@Override
					public void onBtnClick() {
						logoutDlg.dismiss();
						deleteGuardFile();
						MainApplication.getInstance().clearServiceData();
						// 必须在UI线程中调用
						Glide.get(baseAct).clearMemory();
						new AsyncTask<String,String,String>(){
							@Override
							protected String doInBackground(String... params) {
								xwDC.clearData();
								return null;
							}

							@Override
							protected void onPreExecute() {
								super.onPreExecute();
								showPlg("");
							}

							@Override
							protected void onPostExecute(String s) {
								super.onPostExecute(s);
								disPlg();
								MainApplication.getInstance().onTerminate();
							}
						}.execute("");


					}
				});

		logoutDlg.show();

	}

	/**
	 * 注销
	 */
	public void destroyAccount() {

		destroyDlg = new NormalDialog(this).isTitleShow(true)
				.content(xechwic.android.XWCodeTrans.doTrans("该操作会清理所有保存的数据记录,是否继续?"))
				.btnNum(2).btnText(getResources().getString(R.string.alert_cancel),
						getResources().getString(R.string.alert_confirm));
		destroyDlg.setTitle(getResources().getString(R.string.alert_relogin));
		destroyDlg.setOnBtnClickL(new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				destroyDlg.dismiss();
			}
		}, new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				try
				{
					destroyDlg.dismiss();
					FileUtil.deleteGuardFile();
					// 必须在UI线程中调用
					Glide.get(baseAct).clearMemory();
					XWDataCenter.xwDC.destroyUserAccount(baseAct,true);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});

		destroyDlg.show();


	}


	/////发送一个在线消息
	public void sendOnlineMessage(FriendNodeInfo fni,String content){
		try {
			if (fni == null || TextUtils.isEmpty(content)) {
				return;
			}
			String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date());

			///////////////////////////////
			////////////说明:第四参数null,可传入 byte[33]字节。然后调用queryMessageStatus来跟踪它。
			byte[] traceNo = new byte[33];
			xwDC.sendOnlineMessage(xwDC.cid, fni.getId(), (content + "\0").getBytes("GBK"),
					(ctime + "\0").getBytes("GBK"), traceNo);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	////清理已读消息
    public void clearReadMsg(List<ChatMsgEntity> snapchatList){
		try {
			int totalSize = snapchatList.size();
			if(totalSize>0){
				List<ChatMsgEntity> tempList=new ArrayList<>();
				tempList.addAll(snapchatList);
				int count = 0;
				while (count < totalSize) {
					ChatMsgEntity entity = tempList.get(count);
					count++;
					if (entity != null) {
							//清理这条消息
							XWDataCenter.getMessageDB().delMsg(XWDataCenter.getCurAccount(), entity);
							//清理历史记录
							ChatHistoryBean bean = XWDataCenter.getMessageDB().getLastMsg(XWDataCenter.getCurAccount(), entity.getFriendAccount());
							if (bean == null) {
								XWDataCenter.getChatHistoryDB().deleteFriendNode(entity.getFriendAccount(), XWDataCenter.getCurAccount());
							} else {
								XWDataCenter.getChatHistoryDB().updateFriendNode(bean, XWDataCenter.getCurAccount());
							}

							//清理文件, 自己发的文件不清理
							if (!XWDataCenter.getCurAccount().equals(entity.getFriendAccount())) {
								XWDataCenter.clearMsgFile(entity);
							}
					}
				}
				tempList.clear();
			}

		}catch (Exception e){
			e.printStackTrace();
		}
	}






	@Override
	protected void onDestroy() {
		Log.e("BaseUI",getLocalClassName()+"onDestroy");
		bIsDestroy=true;

		super.onDestroy();

	}



	@Override
	protected void onResume() {
		Log.e("BaseUI",getLocalClassName()+"onResume");
		super.onResume();
		bIsFront=true;

		////////////////////////////
		xwDC=MainApplication.getInstance().getDC(this);
		MobclickAgent.onResume(this);

	}

	@Override
	protected void onPause() {
		Log.e("BaseUI",baseAct.getLocalClassName()+"onPause");
		super.onPause();
		bIsFront=false;
		MobclickAgent.onPause(this);
	}

	
	/**
	 * 更新UI
	 * @param obj
	 */
	public void updateUI(Object obj){}

	/**
	 * 保存屏幕常亮
	 */
	@SuppressWarnings("deprecation")
	/*public void enableKeepScreen(){
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MyTag"); 
		mWakeLock.acquire(); 
	}*/

	/**
	 * 保存屏幕常亮
	 */
	/*public void disableKeepScreen(){
		if(mWakeLock!=null){
			mWakeLock.release();
			mWakeLock=null;
		}
	}*/
	
	/**
	 * 回到桌面
	 */
	public void gotoHomeActivity() {
		try {
			moveTaskToBack(true);  ////修改为退出后台，而不是直接弹出到桌面 yangj 20161230
		} catch (Exception e) {
			e.printStackTrace();
			try{
				Intent home = new Intent(Intent.ACTION_MAIN);
				home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				home.addCategory(Intent.CATEGORY_HOME);
				startActivity(home);
			}catch (Exception e2){
				e2.printStackTrace();
				baseAct.finish();
			}

		}
		System.gc();//申请清理一次内存
	}

	@Override
	public void finish() {
		IMEUtils.fixInputMethodManagerLeak(this);
		MainApplication.getInstance().clearContext(this);
		super.finish();

	}
}
