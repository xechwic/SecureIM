package xechwic.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.squareup.otto.Subscribe;

import xechwic.android.act.MainApplication;
import xechwic.android.act.ServerConfig;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.LoginEvent;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.ui.BaseUI;
import xechwic.android.util.AlarmManagerUtil;
import xechwic.android.util.AppConfig;
import xechwic.android.util.Http;
import xechwic.android.util.IMEUtils;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.PrefsUtils;
import xechwic.android.util.TaskExecutor;
import xechwic.android.view.CircleImageView;
import xechwic.android.view.ToastUtil;
import ydx.securephone.R;

import static xechwic.android.util.FileUtil.deleteGuardFile;


/**
 *登陆界面
 *
 */
public class FriendLogin extends BaseUI implements OnClickListener{

	private String TAG=FriendLogin.class.getSimpleName();
	public XWDataCenter xwDC;
	private ImageView img_back;//后退

	public EditText numinput;       //账号
	public EditText passEdit;       //密码
	public TextView loginBtn;        //登录按钮
	private ImageView delete_img;     //清理输入
	private TextView register_new;   //注册新账号
	private TextView forget_pwd;
	private TextView actionbar_title_lab;
	private String xwLoginStatus;
  private RelativeLayout layout_logining;//正在登录界面
	private CircleImageView iv_logining_avatar;//登录头像
	private Button btn_cancel_login;//取消登录
	public Handler mHandler;
  private CheckBox cbAuto;
	ProgressDialog progressDg;//进度圈




	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_login);
		BusProvider.getInstance().register(this);

		MainApplication.getInstance().initXWDC();
		xwDC=XWDataCenter.xwDC;
//		MainApplication.getInstance().deleteNotification();
		mHandler=new FriendLoginHandle(this);

		initView();
		configView();
		setTitle(XWCodeTrans.doTrans("登录"));



		if(!checkThirdLogin()){////第三方登录检测

			if(PrefsUtils.getInstance().get(JRSConstants.KEY_AUTO_LOGIN,false)){
				autoLogin();//尝试自动登录
				//////登录检测
				mHandler.sendEmptyMessageDelayed(6,1000);
			}else {
				{
					xwDC.isLogin = false;
					TaskExecutor.executeTask(new Runnable() {
						@Override
						public void run() {
							/////获取缓存头像
							FriendNodeDB.restoreHeadMap();
						}
					});
				}
			}
		}



	}

	private boolean checkThirdLogin() {
		Intent intent = getIntent();
		if (intent != null) {
			String action = intent.getAction();
			if (!TextUtils.isEmpty(action) && action.equals(JRSConstants.CMD_ACTION_AUTOLOGIN)) {
				final String account = intent.getStringExtra(JRSConstants.KEY_USER_ACCOUNT);
				final String passwd = intent.getStringExtra(JRSConstants.KEY_USER_PASSWORD);
				if (account != null && passwd != null) {
					saveAccount(account);
          login(account,passwd);
					return true;
				}
			}
			setIntent(null);
		}
		return false;
	}
	public void enableLoginView(boolean enabled){
		if(enabled){
			this.loginBtn.setEnabled(true);
			this.passEdit.setEnabled(true);
			this.numinput.setEnabled(true);
			delete_img.setEnabled(true);
			register_new.setEnabled(true);
			forget_pwd.setEnabled(true);
			tvIP.setEnabled(true);
		}else{
			this.loginBtn.setEnabled(false);
			this.passEdit.setEnabled(false);
			this.numinput.setEnabled(false);
			delete_img.setEnabled(false);
			register_new.setEnabled(false);
			forget_pwd.setEnabled(false);
			tvIP.setEnabled(false);
		}
	}

	/////清理登录缓存信息
	private void clearLogin(){
		xwDC.cleanNodeList();
		FriendControl.friendList.clear();
		FriendNodeDB.removeBackupFriends();
	}

	private boolean checkSD(){
		if (!com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(this))
		{
			new  AlertDialog.Builder(baseAct)
					.setTitle(XWCodeTrans.doTrans("请插入正确的安全T卡!") )
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton( XWCodeTrans.doTrans("确定") ,

							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int whichButton){
									dialog.dismiss();

									finish();
								}
							}
					)
					.show();
			return false;
		}
		return true;
	}
	void login(final String account,final String passwd){
		final boolean bAutoLogin=XWDataCenter.getAutoLogin();
		xwDC.bIsLoginRunning=false;
		if ((account==null)||(account.length()==0))
			return;
		if(!checkAccount(account,passwd)){
			showToastTips(getResources().getString(R.string.alert_login_user_error));
			return;
		}
		if(checkAccountChange(account)){
			//			showToastTips(getResources().getString(R.string.alert_login_user_change));
			showUserChangeDialog();
			return;
		}
		this.loginBtn.setText(this.getResources().getString(R.string.alert_logging));
		enableLoginView(false);
		{
			////////////2017-01-07,在这里设置全局密钥文件
			xwDC.loginName=account;
		}
		xwLoginStatus=getResources().getString(R.string.status_online);
		this.numinput.setText(account);
		this.passEdit.setText(passwd);
		////显示登录中
		showLoginingUI(true);
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				/////清理之前的登录信息
				clearLogin();
				/////不保存账户密码
				//saveAccountPasswd(account,passwd,true);
				////切换主线程运行
				TaskExecutor.runOnUIThread(new Runnable() {
					@Override
					public void run() {
						AlarmManagerUtil.registerAlarm(baseAct);

						/////////////////2017-03-15,如果是自动登录则不重启

						loginTask(account,passwd,bAutoLogin);
						onLoginOutTime();
						//////登录检测
						mHandler.removeMessages(6);
						mHandler.sendEmptyMessageDelayed(6,1000);
					}
				});
			}
		});
	}
	/**
	 * 登录
	 */
	void login(){
		final String account =numinput.getText().toString();
		final String passwd=passEdit.getText().toString();
		final boolean bAutoLogin=XWDataCenter.getAutoLogin();

		xwDC.bIsLoginRunning=false;


		if ((account==null)||(account.length()==0))
			return;
		if(!checkAccount(account,passwd)){
			showToastTips(getResources().getString(R.string.alert_login_user_error));
			return;
		}
		if(checkAccountChange(account)){
//			showToastTips(getResources().getString(R.string.alert_login_user_change));
			showUserChangeDialog();
			return;
		}


		this.loginBtn.setText(this.getResources().getString(R.string.alert_logging));
    enableLoginView(false);

		{
			////////////2017-01-07,在这里设置全局密钥文件
			xwDC.loginName=account;
		}

		///////置启动标志
		XWDataCenter.xwDC.isLogin=true;


		xwLoginStatus=getResources().getString(R.string.status_online);


		try
		{
			if(progressDg==null){
				progressDg = new ProgressDialog(this);
			}
			progressDg.setTitle(XWCodeTrans.doTrans("正在更新加密数据,请等待..."));
			progressDg.show();

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}


		//if(xwDC.isLogin&&!TextUtils.isEmpty(xwDC.loginName)){
		//	if(xwDC.iServerConnectStatus==1){
		//		/////已经登录成功
		//		startHomeAct();
		//		return;
		//	}else if(xwDC.iServerConnectStatus==0){
		//		/////正在登录
		//		this.loginBtn.setText(this.getResources().getString(R.string.alert_logging));
		//		this.loginBtn.setEnabled(false);
    //
		//		this.passEdit.setEnabled(false);
		//		this.numinput.setEnabled(false);
		//		onLoginOutTime();
		//		return;
		//	}
    //
		//}

		////显示登录中
		showLoginingUI(true);
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				/////清理之前的登录信息
				clearLogin();
				/////保存账户密码
				saveAccountPasswd(account,passwd,true);
				////切换主线程运行
				TaskExecutor.runOnUIThread(new Runnable() {
					@Override
					public void run() {
						AlarmManagerUtil.registerAlarm(baseAct);

						/////////////////2017-03-15,如果是自动登录则不重启

						loginTask(account,passwd,bAutoLogin);
						onLoginOutTime();
						//////登录检测
						mHandler.removeMessages(6);
						mHandler.sendEmptyMessageDelayed(6,1000);
					}
				});
			}
		});

	}

	private void showLoginingUI(boolean show){
		if(show){
			layout_logining.setVisibility(View.VISIBLE);
			//设置头像
			String pic=FriendNodeDB.getFriendHead(numinput.getText().toString().trim());
      if(TextUtils.isEmpty(pic)){
				iv_logining_avatar.setImageResource(R.drawable.login_logo);
			}else{
				String iconPath= Http.getHeadPicUrl()+pic;
				Glide.with(MainApplication.getInstance())
						.load(iconPath)
						.error(R.drawable.login_logo)
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.into(iv_logining_avatar);
			}

		}else{
			layout_logining.setVisibility(View.GONE);
		}
	}

	private void showUserChangeDialog(){
	 final NormalDialog destroyDlg = new NormalDialog(this).isTitleShow(true)
			 .content(getResources().getString(R.string.alert_login_user_change)
					 +"\n"+xechwic.android.XWCodeTrans.doTrans("该操作会清理所有保存的数据记录,是否继续?"))
			 .btnNum(2).btnText(getResources().getString(R.string.alert_cancel),
					 getResources().getString(R.string.alert_confirm));
//		destroyDlg.setTitle(getResources().getString(R.string.alert_login_user_change));
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
					deleteGuardFile();
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

	//////检测账号变动
	public static boolean checkAccountChange(String account){
		if(!TextUtils.isEmpty(XWDataCenter.getCurAccount())){
			if(!XWDataCenter.getCurAccount().equals(account)){///账户变动，清理备份
				return true;
			}
		}
		return false;
	}

	/////检测账号正常
	private boolean checkAccount(String account,String passwd){
		return !(
				TextUtils.isEmpty(account)|| TextUtils.isEmpty(passwd)
						|| (account.length()>32)
						||(passwd.length()>32)   ////////2016-11-26,长度大于32可能是解密出错。
		);
	}

	private void loginTask(final String accout,final String passwd,final boolean bAutoLogin){
		Log.e("friendlogin","loginTask run");
		Intent intent=new Intent(this,XWServices.class);
		intent.setAction(JRSConstants.CMD_ACTION_AUTOLOGIN);
		intent.putExtra(JRSConstants.KEY_USER_ACCOUNT,accout);
		intent.putExtra(JRSConstants.KEY_USER_PASSWORD,passwd);
		startService(intent);
	}

	private void onLoginOutTime(){
		if(progressDg!=null&&progressDg.isShowing()){
			progressDg.dismiss();
		}
		////检查SD是否准备好
		if(checkSD()){
			///////15秒后检测登录
			mHandler.removeCallbacks(checkLoginRunnable);
			mHandler.postDelayed(checkLoginRunnable,30*1000);
		}
	}

	public static void saveAccount(String account) {
		SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);
		SharedPreferences.Editor editor = settings.edit();
		{

			editor.putString("LOGIN_USER", account);
		}
		editor.commit();
	}

	/////登录成功保存账户
	public static void saveAccountPasswd(String account,String passwd,boolean autoLogin){
		{
			SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);


			SharedPreferences.Editor editor = settings.edit();
			{//只有第一次安装才创建icon

				editor.putBoolean("AUTO_LOGIN", autoLogin);

				editor.putString("LOGIN_USER",account);

				///////////////加密保存的口令
				try
				{
					editor.putString("LOGIN_PASS",  new String ( com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_userpassword(
							account ,passwd.getBytes("iso-8859-1"))  ,"iso-8859-1") );
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				editor.commit();
			}
		}
	}


	 void clearSerivce() {
		deleteGuardFile();
		MainApplication.getInstance().clearServiceData();
	}
	//自动登录
	void autoLogin(){
		new AsyncTask<String,String,String>() {

			@Override
			protected String doInBackground(String... params) {
				SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);

				String sUser = settings.getString("LOGIN_USER", "");
				String sPass = settings.getString("LOGIN_PASS", "");

				xwDC.loginName=sUser;
				if(TextUtils.isEmpty(sUser)){
					return null;
				}

				///////////////解密保存的口令
				 try {
					 sPass = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_userpassword(sUser, sPass.getBytes("iso-8859-1")), "iso-8859-1");
				 } catch (Exception ex) {
					 ex.printStackTrace();
				 }
				return sPass;
			}

			@Override
			protected void onPostExecute(String sPass) {
				if(bIsDestroy){
					return;
				}
				passEdit.setText(sPass);
				SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);
				String sDONOTAUTOLOGIN = settings.getString("DONOTAUTOLOGIN", "");



				if ("1".equals(sDONOTAUTOLOGIN))  ///////
				{
					return ;
				}
				if (!TextUtils.isEmpty(sPass)) {
					login();
				}
			}
		}.execute("");
	}
	/**
	 * 设置标题
	 * @param title
	 */
	void setTitle(String title){
		try
		{
			TextView txTitle=	(TextView) findViewById(R.id.head_title);
			txTitle.setText(""+title);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	/**
	 *  修改IP
	 */
	private void modifyIP(){
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		final LinearLayout buildLayout = new LinearLayout(this);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final TextView signNameText = new TextView(this);
		String ipStr=getResources().getString(R.string.curip)+":"+XWDataCenter.getXIMIP();
		signNameText.setText(ipStr);

		final EditText signNameEdit = new EditText(this);

		signNameEdit.setHint("");
		signNameEdit.setLayoutParams(layoutParams);
		signNameEdit.setMaxLines(1);
		ViewGroup.LayoutParams tmpLP = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		signNameText.setLayoutParams(tmpLP);

		LinearLayout signNameLayout = new LinearLayout(this);
		signNameLayout.setOrientation(LinearLayout.VERTICAL);
		signNameLayout.setLayoutParams(layoutParams);
		signNameLayout.addView(signNameText);
		signNameLayout.addView(signNameEdit);

		buildLayout.setOrientation(LinearLayout.VERTICAL);

		buildLayout.addView(signNameLayout);
		builder.setView(buildLayout);
		builder.setPositiveButton(getResources()
						.getString(R.string.alert_confirm),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
										int whichButton) {
						String ip=signNameEdit.getText().toString().trim();
						if(!TextUtils.isEmpty(ip)){
							XWDataCenter.setXIMIP(ip);
							ServerConfig.config(ip);
							/////////////////////////2016-10-31,设置通讯ip和端口,通讯端口默认8899
							try {
								XWDataCenter.xwDC.setServerIPPort((XWDataCenter.getXIMIP() + "\0").getBytes("iso-8859-1"), 8899);
							}
							catch(Exception ex)
							{
								ex.printStackTrace();
							}
						}
					}
				});
		builder.setNeutralButton(getResources()
						.getString(R.string.alert_cancel),
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
	TextView tvIP;
	private void configView(){
		if (AppConfig.IP_CONFIG) {
			tvIP.setVisibility(View.VISIBLE);
			tvIP.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					modifyIP();
				}
			});
		} else {
			tvIP.setVisibility(View.GONE);
		}

		if(AppConfig.REG_CONFIG){
			register_new.setVisibility(View.VISIBLE);
		}else{
			register_new.setVisibility(View.GONE);
		}
	}
	/**
	 * 初始化控件
	 */
	private void initView() {
		cbAuto=(CheckBox) findViewById(R.id.cb_auto);
		if(PrefsUtils.getInstance().get(JRSConstants.KEY_AUTO_LOGIN,false)){
			cbAuto.setChecked(true);
		}else{
			cbAuto.setChecked(false);
		}
		cbAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				PrefsUtils.getInstance().put(JRSConstants.KEY_AUTO_LOGIN,isChecked);
			}
		});
		tvIP = (TextView) findViewById(R.id.tv_setip);
		layout_logining=(RelativeLayout)findViewById(R.id.layout_logining);
		iv_logining_avatar=(CircleImageView)findViewById(R.id.iv_logining_avatar);
		btn_cancel_login=(Button)findViewById(R.id.btn_cancel_login);
		numinput = (EditText) this.findViewById(R.id.loginuseraccount);


		this.passEdit = (EditText) this.findViewById(R.id.loginPassword);
		this.loginBtn = (TextView) this.findViewById(R.id.login_new);

		this.delete_img = (ImageView) this.findViewById(R.id.delete_img);
		this.register_new = (TextView) this.findViewById(R.id.register_new);
		this.img_back = (ImageView) this.findViewById(R.id.img_back);

		this.forget_pwd = (TextView) this.findViewById(R.id.forget_pwd);
		this.actionbar_title_lab = (TextView) this.findViewById(R.id.actionbar_title_lab);

		////////////
		actionbar_title_lab.setText(R.string.app_name);

		loginBtn.setOnClickListener(this);
		delete_img.setOnClickListener(this);
		register_new.setOnClickListener(this);
		img_back.setOnClickListener(this);
		forget_pwd.setOnClickListener(this);
		btn_cancel_login.setOnClickListener(this);

		String sUser = "";
//		String sPass = "";


		SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);
		///SharedPreferences.Editor editor = settings.edit();
//		String sLoginStatus = "";
		/////////boolean isFirstRun=false;

		sUser = settings.getString("LOGIN_USER", "");
//		sPass = settings.getString("LOGIN_PASS", "");
//		///////////////解密保存的口令
//		try {
//			sPass = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_userpassword(sUser, sPass.getBytes("iso-8859-1")), "iso-8859-1");
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}

		xwDC.loginName = sUser;


		this.numinput.setText(sUser);
		this.passEdit.setText("");

		Intent intent=getIntent();
		if (intent!=null)
		{
			handleIntent(intent);
		}

	}


	////密码未解码可能会很长，先不设置  20161125 yangj
	@Override
	protected void doSaveInstanceState(Bundle outState) {
		super.doSaveInstanceState(outState);
	}


	@Override
	public void onClick(View v){
		if(v==btn_cancel_login){
			xwDC.isLogin=false;
			mHandler.removeMessages(6);
			mHandler.removeCallbacks(checkLoginRunnable);
			TaskExecutor.executeTask(new Runnable() {
				@Override public void run() {
					xwDC.reActive(0);
				}
			});
			enableLoginView(true);
			loginBtn.setText(FriendLogin.this.getResources().getString(R.string.alert_login));
			showLoginingUI(false);
			ToastUtil.getInstance(baseAct).show(XWCodeTrans.doTrans("取消登录！"));
		}
		else if(v==loginBtn){
			//隐藏键盘
			IMEUtils.hideSoftInput(this);
			login();

		}else if(v==this.delete_img){
			this.numinput.setText("");
		}else if((v==this.register_new) || (v==this.forget_pwd)){
			try
			{
				String sLAN="";
				try
				{
					///////////////////////如果中文不需转换
					sLAN=getResources().getConfiguration().locale.getCountry();
					if ( (sLAN!=null) && (sLAN.length()>0) )
					{
						sLAN=sLAN.toLowerCase();
					}
					else
					{
						sLAN="tw";
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}

				String sURL="https://"+ServerConfig.XIM_SERVER_IP+"/a2badmin/signup/signup_mobile1.php";
				android.util.Log.v("XIM","regNumber url"+sURL);
				Uri uri=Uri.parse(sURL);
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(it);

			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}else if(v==this.img_back){
			this.finish();
		}

	}

	private Runnable checkLoginRunnable=new Runnable() {
		@Override
		public void run() {
			if(bIsDestroy){
				return;
			}
			xwDC.isLogin=false;
			xwDC.bIsLoginRunning=false;

			xwDC.reActive(0);
			//xwDC.logoutService(0);
			///////////禁止自己登录
			XWDataCenter.setAutoLogin(false);
			ToastUtil.getInstance(baseAct).show(xechwic.android.XWCodeTrans.doTrans("登录超时,请检查账号!"));
			showLoginingUI(false);
			enableLoginView(true);
			loginBtn.setText(FriendLogin.this.getResources().getString(R.string.alert_login));
			xwDC.isLogin=false;
			mHandler.removeMessages(6);
		}
	};

	///登录成功进入主界面
	public void startHomeAct(){
		finish();
		Intent nextPage=new Intent();
		nextPage.putExtra(JRSConstants.DATA,JRSConstants.NOTICE_MSG);
		nextPage.setClass(this,FriendControl.class);
		startActivity(nextPage);
	}



	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(progressDg!=null&&progressDg.isShowing()){
			progressDg.dismiss();
		}
		if(!xwDC.isLogin){
			clearSerivce();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.e(TAG,"onNewIntent()");
		handleIntent(intent);

	}

	private void handleIntent(Intent intent){
		if(intent==null){
			return;
		}
		if (("LOGIN_FAILED".equals(intent.getAction())))
		{
			/////////////////2014-06-16,指示是否通讯层初始化!!!!!!!!!!!!!!!!!
			xwDC.isLogin=false;
			xwDC.bIsLoginRunning=false;
			//////xwDC.logoutService(1);
			///////////禁止自己登录
			XWDataCenter.setAutoLogin(false);
			ToastUtil.getInstance(this).show(xechwic.android.XWCodeTrans.doTrans("登录超时,请检查账号!"));

		}
	}

	@Override
	public void finish() {
		cleanInputEdit();
		super.finish();
	}

	private void cleanInputEdit(){
		if(numinput!=null&&passEdit!=null){
			numinput.setText("");
			passEdit.setText("");
			numinput.clearFocus();
			passEdit.clearFocus();
			numinput.setInputType(InputType.TYPE_NULL);
			passEdit.setInputType(InputType.TYPE_NULL);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		BusProvider.getInstance().unregister(this);
		mHandler.removeCallbacks(checkLoginRunnable);

	}

	@Subscribe
	public void onLoginEvent(LoginEvent event){
		if(!bIsDestroy){
			if(!xwDC.isLogin){
				return;
			}
			if(event!=null){
				Log.e("login","onLoginevent"+event.type);
				if(event.type==3){///正在执行登陆
					onLoginOutTime();
				}else{
					mHandler.removeCallbacks(checkLoginRunnable);
					mHandler.sendEmptyMessage(event.type);
					if(event.type==1){
						Log.e("login","ConnectStatus==1 check:xim=="+XWDataCenter.xwDC.XIMGetConnectStatusToXIM());
            mHandler.removeMessages(1);
					}
				}

			}
		}
	}

}
