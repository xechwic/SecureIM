package xechwic.android.act;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.process.RemoteService;
import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.squareup.leakcanary.LeakCanary;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ydx.securephone.BuildConfig;
import ydx.securephone.R;
import cn.hadcn.keyboard.ChatKeyboardLayout;
import cn.hadcn.keyboard.EmoticonEntity;
import xechwic.android.SmileyParser;
import xechwic.android.XWCrashHandler;
import xechwic.android.XWDataCenter;
import xechwic.android.XWNetPhone;
import xechwic.android.XWScreenOnOff;
import xechwic.android.XWServices;
import xechwic.android.bean.ChatMsgEntity;
import xechwic.android.bean.RecordBean;
import xechwic.android.ui.BaseUI;
import xechwic.android.util.AlarmManagerUtil;
import xechwic.android.util.AppConfig;
import xechwic.android.util.AppConfig.Version;
import xechwic.android.util.FileUtil;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.ObjectIO;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.UriConfig;


/**
 * @author litao
 *
 */
public class MainApplication extends Application {

	private static MainApplication instance;

	public List<RecordBean> mRecord;

	public HashMap<Long,ChatMsgEntity> mFile ;

	public static  XWDataCenter xwDC=null;


	final public static int APP_ICON_ID=0X1976abcd;


	public static long TIME_ALARM = 30000;


//	static public Intent intentservice=null;

	static public  long uLastOffScreen=0;
	static public long uLastPosition=0;


	static public String sVersionURL="";
	static public String sNewVerName="";
	static public String sNewVerURL="";
	static public String sNewVerDate="";
	static public String sNewVerCode="";

	static public String sSelfVerCode="";

	static public TelephonyManager telephoneM;
	static public PhoneStateListener listner;

	static  public int iCallState=0;


	public static MainApplication getInstance() {
		return instance;
	}


	public void onMyCreate() {
		//初始化项目配置
		AppConfig.setVersion(Version.TW);
	}

	public List<RecordBean> getRecordList(){
		if(mRecord==null){
			try
			{
				mRecord = (List<RecordBean>) ObjectIO.readObject(UriConfig.getCallRecordPath());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(mRecord == null){
				mRecord = new ArrayList<>();
				try
				{
					ObjectIO.saveObject(mRecord,UriConfig.getCallRecordPath());
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}
		}
		return mRecord;
	}


	public void createShortCut(){
		//创建快捷方式的Intent

		//if (isShortcutInstalled())
		//	return;

		SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);
		SharedPreferences.Editor editor = settings.edit();
		boolean isFirstRun=false;

		if (!settings.getBoolean("FIRST_RUN", false)) {//只有第一次安装才创建icon
			editor.putBoolean("FIRST_RUN", true);
			isFirstRun=true;
		}
		editor.commit();

		if (!isFirstRun)
			return;

		try
		{
			Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
			//不允许重复创建
			shortcutintent.putExtra("duplicate", false);
			//需要现实的名称
			shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
			//快捷图片
			Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon);
			shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
			//点击快捷图片，运行的程序主入口
			shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext() , xechwic.android.XWNetPhone.class));
			//发送广播。OK
			sendBroadcast(shortcutintent);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	/////////////////出错捕获和上报!!!!!!!!!!!!!!!!!2014-06-16
	XWCrashHandler catchHandler=null;
	/////public static String sCrachReportURL="http://www.ximvoip.net/a2buser/crachlog/log.php";


	public XWScreenOnOff  screenStatReceiver=null;
	private String processName;
	@Override
	public void onCreate(){
		super.onCreate();
		instance = this;
		processName= getProcessName(this, android.os.Process.myPid());
		Log.e("mainapp","process:"+processName);
		if(!TextUtils.isEmpty(processName)){
			boolean defaultProcess = processName.equals(getPackageName());
			if(defaultProcess){
				Log.e("mainapp","processInit:"+processName);
				processInit();
			}
		}
		/////setupWakeLock();
		////startUpXWService();
	}

	////启动XWService
	public void startUpXWService(){
			Intent intent = new Intent(this, XWServices.class);
			intent.setAction(JRSConstants.CMD_ACTION_AUTOLOGIN);//不需要自动登录
			intent.putExtra(JRSConstants.KEY_USER_ACCOUNT, "");
			intent.putExtra(JRSConstants.KEY_USER_PASSWORD, "");
			intent.putExtra(JRSConstants.KEY_PROCESS_NAME, processName);
			startService(intent);
	}

	////////////创建一个时钟
	////private MyBroadcastReceive systemClockReceiver=new MyBroadcastReceive();
	private void processInit(){
		//初始应用版本相关配置
		AppConfig.init();
		initLeakCanary();
		initBlockCanary();
		initHttpRequest();
		initEmotions();
		///////////////禁止重复!!!!!!!!!2014-06-16
		Log.v("XIM","process name:"+this.getApplicationInfo().processName);

		if (!this.getApplicationInfo().processName.equals(getPackageName()))
		{
			//////////////////Not right process!!!!!!!!!
			Log.v("XIM","Not XechWic main process!!!!!");
			return;
		}

		onMyCreate();

		Log.v("XIM", "application onCreate");



		Log.v("XIM", "application was create........");
//		xwDC=XWDataCenter.getDataCenterForService();
		initXWDC();
		Log.v("XIM", "new XWDataCenter........");

		/////////////////////////初始化语言
		xechwic.android.XWCodeTrans.InitFromAssetsFile();
		Log.v("XIM","test multilan "+xechwic.android.XWCodeTrans.doTrans("测试"));


		InputStream call_is=this.getResources().openRawResource(R.raw.was_call);
		InputStream sys_is=this.getResources().openRawResource(R.raw.xwsys);
		InputStream msg_is=this.getResources().openRawResource(R.raw.xwmsg);
		try{
			xwDC.xwCallAudio=new byte[call_is.available()];
			call_is.read(xwDC.xwCallAudio);

			xwDC.xwMsgAudio=new byte[msg_is.available()];
			msg_is.read(xwDC.xwMsgAudio);

			xwDC.xwSysAudio=new byte[sys_is.available()];
			sys_is.read(xwDC.xwSysAudio);
			call_is.close();
			msg_is.close();
			sys_is.close();
		}catch(Exception e){
			e.printStackTrace();
		}


		XWServices.xwApp=this;
		XWServices.xwDC=xwDC;

		////////////创建桌面快捷方式
		createShortCut();

		/////////////////////////////////
		telephoneM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		listner = new PhoneStateListener()
		{
			@Override
			public void onCallStateChanged(int state, String incomingnumber) {
				Log.e("app","call state:"+state);


				switch(state) {
					case TelephonyManager.CALL_STATE_IDLE:
						if(iCallState!=TelephonyManager.CALL_STATE_IDLE){
							if (XWDataCenter.iNetphoneStatus!=0)
							{
								Log.e("app","CALL_STATE_IDLE iNetphoneStatus!=0");
								TaskExecutor.executeTask(new Runnable() {
									@Override
									public void run() {
										try {
											Thread.sleep(1000);
											TaskExecutor.runOnUIThread(new Runnable() {
												@Override
												public void run() {
													restoreSpeakerOnVolumn();
												}
											});
										}catch (Exception e){
											e.printStackTrace();
										}
									}
								});
							}
						}

						iCallState=state;
						break;
					case TelephonyManager.CALL_STATE_RINGING:
						iCallState=state;
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						iCallState=state;
						break;
				}

				///////////////电话状态变化,启动状态检查
				try {
					Log.v("XIM", "onCallStateChanged XWServices DO_CHECK");
					Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
					intentservice.setAction("DO_CHECK");
					MainApplication.getInstance().startService(intentservice);
				} catch (Exception e) {
					e.getStackTrace();
				}

			}
		};
		telephoneM.listen(listner, PhoneStateListener.LISTEN_CALL_STATE);



		/////////////2014-06-16,开启错误捕获功能!!!!!!!
		catchHandler = XWCrashHandler.getInstance();
		catchHandler.init(getApplicationContext());


		{
			// ----------------生成广播处理 ，开关屏幕 --------------------
			screenStatReceiver = new XWScreenOnOff();
			// 实例化过滤器并设置要过滤的广播
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_SCREEN_ON);
			intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
			// 注册广播
			registerReceiver(screenStatReceiver, intentFilter);
		}

		{
			//-------------------监听网络变化-------------------
			//IntentFilter intentFilter = new IntentFilter();
			///intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

			//registerReceiver(networkReceiver, intentFilter);
		}

		/*{
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_TIME_CHANGED);
			registerReceiver(systemClockReceiver, filter);
		}*/

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		try
		{
			Intent intent = new Intent();
			String packageName = getPackageName();
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			if (pm.isIgnoringBatteryOptimizations(packageName))
			{
				///////intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
			}
			else {
				intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
				startActivity(intent);
				intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
				intent.setData(Uri.parse("package:" + packageName));
				startActivity(intent);

			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		////////////重新注册一次监听!!!!!!!!!!!!!!!!!
		try
		{
			AlarmManagerUtil.registerAlarm(this);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		////////////////获取屏幕亮状态
		XWScreenOnOff.getScreenOn();
	}


	/////监听网络变化
	/*private BroadcastReceiver networkReceiver = new MyBroadcastReceive();*//*BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Log.e("XIM", "networkReceiver conntected:"+ NetWorkUtil.isNetworkConnected(context));
				Intent intentservice=new Intent(context, XWServices.class);
				intentservice.setAction("DO_CHECK");
				context.startService(intentservice);
				////MainApplication.getInstance().registerAlarm();
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	};*/

	private void restoreSpeakerOnVolumn()
	{
		try
		{
			if(xwDC.bIsSpeakerOn){
				Log.e("app","restoreSpeakerOnVolumn");
				AudioManager am=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
				am.setSpeakerphoneOn(true);
				SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);
				int iVolume = settings.getInt("VIDEO_SPEAKER_VOLUME", -1);

				if (iVolume > 0) {
					am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, iVolume, 0);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}
	////初始化内存检测工具
	private void initLeakCanary(){
		if (LeakCanary.isInAnalyzerProcess(this)) {
			// This process is dedicated to LeakCanary for heap analysis.
			// You should not init your app in this process.
			return;
		}
		LeakCanary.install(this);
		// Normal app init code...
	}

	////初始化卡顿检测
	private void initBlockCanary(){
		BlockCanary.install(this, new AppBlockCanaryContext()).start();
	}

	//初始化网络请求库
	private void initHttpRequest(){
		OkGo.init(this);
		try {
			//以下都不是必须的，根据需要自行选择,一般来说只需要 debug,缓存相关,cookie相关的 就可以了
			OkGo.getInstance()
					//如果使用默认的 60秒,以下三行也不需要传
					.setConnectTimeout(10*1000)  //全局的连接超时时间
					.setReadTimeOut(10*1000)     //全局的读取超时时间
					.setWriteTimeOut(10*1000)    //全局的写入超时时间

					//可以全局统一设置缓存模式,默认是不使用缓存,可以不传,具体其他模式看 github 介绍 https://github.com/jeasonlzy0216/
					.setCacheMode(CacheMode.DEFAULT);///** 按照HTTP协议的默认缓存规则，例如有304响应头时缓存 */
			if(BuildConfig.DEBUG){
				//打开该调试开关,控制台会使用 红色error 级别打印log,并不是错误,是为了显眼,不需要就不要加入该行
				OkGo.getInstance().debug("OkGo");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initEmotions(){
		if ( !ChatKeyboardLayout.isEmoticonInitSuccess(this) ) {
			List<EmoticonEntity> entities = new ArrayList<>();
			ChatKeyboardLayout.initEmoticonsDB(this, true, entities, SmileyParser.mSmileyTexts);
		}
	}

	public void initXWDC(){
		if(xwDC==null)
			xwDC=new XWDataCenter(this);
	}

	/////清理服务数据
	public void clearServiceData(){
		/////停止Alarm
		AlarmManagerUtil.cancelAlarm(this);
		//////停止远程守护进程
		stopService(new Intent(this, RemoteService.class));
		////停止本地服务
		stopService(new Intent(this, XWServices.class));
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.e("MainApplication","onLowMemory");
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Log.e("MainApplication","onTrimMemory level"+level);
	}


	@Override
	public void onTerminate(){

		try
		{
			///////releaseWakeLock();

			xwDC.clearAllActivity();
			if (screenStatReceiver!=null)
			{
				unregisterReceiver(screenStatReceiver);
				screenStatReceiver=null;
			}
			/*if(networkReceiver!=null){
				unregisterReceiver(networkReceiver);
			}*/
			/*if (systemClockReceiver!=null)
			{
				unregisterReceiver(systemClockReceiver);
			}*/
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		/////////////2012-11-06,完全退出程序
		{
			Intent startMain = new Intent(
					Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
		}

		////////////Absolutely exit!!!!!!!2014-08-11
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				com.example.mcryptolmsdimpl_demo.MainActivity.unMountSDCard();
				try
				{
					Thread.sleep(2000);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				System.exit(1);
			}
		});

		/////////////////关闭通讯应用
		XWDataCenter.xwDC.logoutService(0);

		super.onTerminate();
	}

	/**
	 * 清理界面引用
	 */
	public void clearContext(Activity xc){
		initXWDC();
		XWDataCenter.xwContext=null;
		////////////////确保只出现一次!!!!!!!!!!,2013-03-16!!!!!!,xechwic
		xwDC.activityList.remove(xc);
	}
	public XWDataCenter getDC(){
		initXWDC();
		return xwDC;
	}
	public XWDataCenter getDC(BaseUI xc){
		initXWDC();
		XWDataCenter.xwContext=xc;
		////////////////确保只出现一次!!!!!!!!!!,2013-03-16!!!!!!,xechwic
		xwDC.activityList.remove(xc);
		xwDC.activityList.add(xc);

		/////////////////////////界面切换要检查语言是否改变!!!!!
		xechwic.android.XWCodeTrans.InitFromAssetsFile();


		///////////////////界面切换执行一次service 检查
//		try
//		{
//			   Intent intentservice=new Intent(this, XWServices.class);
//			   intentservice.setAction("DO_CHECK");
//			   startService(intentservice);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}

    	return xwDC;
	}

	public void notificationOnline(){
		PowerManager pm = (PowerManager) MainApplication.getInstance().getSystemService(Context.POWER_SERVICE);
		if(pm.isScreenOn()&& FileUtil.isGuardFileExist()) {////黑屏不发通知
			initNotification(R.drawable.icon);
		}
		setAutoLogin();
	}
	public void notificationOutline(){
		PowerManager pm = (PowerManager) MainApplication.getInstance().getSystemService(Context.POWER_SERVICE);
		if(pm.isScreenOn()&&FileUtil.isGuardFileExist()) {////黑屏不发通知
			initNotification(R.drawable.icon_out_line);
		}
		/////////////离线进行doCheck,2017-01-23
		{
			try {
				Intent intentservice=new Intent(this, XWServices.class);
				intentservice.setAction("DO_CHECK");
				this.startService(intentservice);
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}

	////通知在线或离线
	public void noticeOnlineStatus(){
		if(XWDataCenter.xwDC
				.XIMGetConnectStatusToXIM() == 1){
			notificationOnline();
		}else {
			notificationOutline();
		}
	}

	//////生成应用绑定notification
	public void initNotification(int icon){
		Log.e("app","initNotification");
		Notification notificationicon;
		//设置通知的事件消息
		CharSequence contentTitle = getResources().getString(R.string.app_name);//通知栏标题
		CharSequence contentText = xechwic.android.XWCodeTrans.doTrans("正在运行");//通知栏内容
		Intent intent = new Intent();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
			intent.setPackage(getPackageName());
		}
		intent.setComponent(new ComponentName(getPackageName(), XWNetPhone.class.getCanonicalName()));
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Notification.Builder builder = new Notification.Builder(this);
			builder.setSmallIcon(icon);
			builder.setContentTitle(contentTitle);
			builder.setContentText(contentText);
			builder.setContentIntent(pendingIntent);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				notificationicon = builder.getNotification();
			} else {
				notificationicon = builder.build();
			}
		}else{
			notificationicon = new Notification.Builder(this)
					.setContentTitle(contentTitle)
					.setContentText(contentText)
					.setContentIntent(pendingIntent)
					.build();
//			notificationicon.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_SHOW_LIGHTS;
			notificationicon.defaults = Notification.DEFAULT_ALL;
			notificationicon.when = System.currentTimeMillis();
			notificationicon.iconLevel = icon;

		}

		//////////////////////////XechWic,2013-03-16,设置为前台,不被系统清理。
		try
		{
			deleteNotification();
			if(XWServices.xwService!=null){
				XWServices.xwService.startForeground(APP_ICON_ID,notificationicon);
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setAutoLogin(){
		////////////////登录成功后,设定自动登录!!!!!!!!!
		try
		{
			SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);
			SharedPreferences.Editor editor = settings.edit();
			{//只有第一次安装才创建icon
				editor.remove("DONOTAUTOLOGIN");
				editor.commit();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}


	public void deleteNotification(){
		if(XWServices.xwService!=null){
			XWServices.xwService.stopForeground(true);
		}
	}


	public static int getVerCode(Context context) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo(
					XWDataCenter.PackageName, 0).versionCode;
		} catch (Exception e) {
			Log.e("XIM", e.getMessage());
		}
		return verCode;
	}

	public static String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(
					XWDataCenter.PackageName, 0).versionName;
		} catch (Exception e) {
			Log.e("XIM", e.getMessage());
		}
		return verName;
	}





	public void setAlarmRunning(boolean bIsRunning)
	{
		try
		{
			SharedPreferences settings = getSharedPreferences(
					XWDataCenter.PackageName, 0);
			SharedPreferences.Editor editor = settings.edit();
			{// 只有第一次安装才创建icon
				editor.putBoolean("ALARM_RUNNING", bIsRunning);
				editor.apply();
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}





	public class AppBlockCanaryContext extends BlockCanaryContext {
		// override to provide context like app qualifier, uid, network type, block threshold, log save path
		// this is default block threshold, you can set it by phone's performance
		@Override
		public int getConfigBlockThreshold() {
			return 500;
		}

		// if set true, notification will be shown, else only write log file
		@Override
		public boolean isNeedDisplay() {
			return BuildConfig.DEBUG;
		}

		// path to save log file
		@Override
		public String getLogPath() {
			return "/blockcanary/performance";
		}
	}


    ////////////不锁定休眠
	public PowerManager.WakeLock mWakelock=null;
	public synchronized void setupWakeLock(){
		//////如果有锁一定要先释放掉!!!!!
		releaseWakeLock();

		try {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "xechwic.android.XWServices");
			mWakelock.acquire();
		}
		catch(Exception ex)
		{

		}
	}
	public synchronized void releaseWakeLock(){
		try {
			if (mWakelock != null) {
				mWakelock.release();
				mWakelock = null;
			}
		}
		catch(Exception ex)
		{

		}
	}
	public boolean bIsXIMConnectied=false;
}
