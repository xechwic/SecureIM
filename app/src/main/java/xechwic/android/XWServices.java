package xechwic.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.android.process.RemoteService;
import com.android.process.aidl.IProcessService;

import java.io.File;
import java.lang.reflect.Method;

import xechwic.android.act.MainApplication;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.LoginEvent;
import xechwic.android.util.AlarmManagerUtil;
import xechwic.android.util.CallUtil;
import xechwic.android.util.FileUtil;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.NetWorkUtil;
import xechwic.android.util.PrefsUtils;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.XWDataCenterMessage;
import ydx.securephone.R;

import static xechwic.android.XWScreenOnOff.getScreenOn;

///import static u.aly.av.R;


public class XWServices extends Service{
	//定义个一个Tag标签   
	private static final String TAG = "XWService";  
	public static XWServices xwService=null;  	

	public static  XWDataCenter xwDC=null;
	public static MainApplication xwApp=null;

	private LocalBinder mLocalBinder;

	private RemoteServiceConnection mRemoteServiceConn;

	private boolean bIsLastNetworkOn=false;    
	
	long lLastCheckNetWorkState=0;
	
    boolean bIsSceenOn=true;
    
    long lLastConnected=0;
    
    boolean bLastActive=false;
    
    static private boolean bIsExit=false;
    
	boolean bLastConnectXIM = false;	
	
	private long lLastDoActive=0;

	//////////////////限制执行active时间。
	private long lLastReactive=0;


	public static long lLastDoCheck=0;
    


	//////尝试亮屏一下
	private void tryActivity(){
		/////早上7点到晚上7点才起作用
//		if(!TimeUtil.isWorkTime()){
		    if(!PrefsUtils.getInstance().get(JRSConstants.KEY_SCREEN_SWITCH,false)){
				return;
			}
			PowerManager pm = (PowerManager) MainApplication.getInstance().getSystemService(Context.POWER_SERVICE);
			if(!pm.isScreenOn()){
				Log.e(TAG,"tryActivity");
				long lastTime=PrefsUtils.getInstance().get(JRSConstants.KEY_SCREEN_OFF,0);
				if(lastTime!=0){
					if((System.currentTimeMillis()-lastTime)>(60*1000)){////黑屏下1分钟
						Log.e(TAG,"start wake activity");
						/////启动亮屏
						xwDC.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_61);
					}
				}
//			}
		}


	}


	public  boolean isIPOK(String ip)
	{
		return NetWorkUtil.isNetworkConnected(this);
		//if (! NetWorkUtil.isNetworkConnected(this))
		//	return false;

		/*
		{
			Socket socket ;
			socket = new Socket();
			SocketAddress address = new InetSocketAddress(InetAddress.getByName(ServerConfig.XIM_SERVER_IP), Integer.parseInt(ServerConfig.XIM_SERVER_PORT));/////连网站
			try {
				socket.connect(address, 10000);
				socket.close();
				Log.e("XWService","isIPOK: ok！");
				/////保存这个时间戳
				/////PrefsUtils.getInstance().put(JRSConstants.KEY_SCREEN_OFF,System.currentTimeMillis());
				return true;
			} catch (Exception e) {
				e.printStackTrace();
//				Log.e(TAG,"connect error try activity screen");
//				tryActivity();
			}
		}
		return false;
		*/
	}

	public  boolean isNetworkConnected() {
		/////return NetWorkUtil.isNetworkConnected(this);
     	return isIPOK(XWDataCenter.getXIMIP());/////黑屏可能会被屏蔽http连接
	}


	@Override
    public void onCreate(){  
        super.onCreate();
        Log.e(TAG, "start onCreate~~~"+"     XWServices       hashCode:"+this.hashCode());
		xwService=this;
		MainApplication.getInstance().initXWDC();
		xwDC=XWDataCenter.xwDC;
		//////绑定通知
		MainApplication.getInstance().initNotification(R.drawable.icon);
		// 绑定远程守护服务
		mLocalBinder = new LocalBinder();

		startUpRemoteService();

		XWDataCenter.xwDC.setScreenOpen(1);

        setWIFISleep();

		///////应用在后台，服务重启，恢复用户数据
		Log.e(TAG,"XIM connect status==1?"+xwDC.XIMGetConnectStatusToXIM());
		if(XWDataCenter.xwContext==null||!XWDataCenter.xwContext.bIsFront) {
			if (!xwDC.isLogin){////没有登录则登录
				Log.e(TAG, "start onCreate~~~"+"   !xwDC.isLogin  XWServices autologin    ");
				restoreData();
		   }
		}
		///setupWakeLock();
		///////startRemoteServiceTask();
    }

	private void startRemoteServiceTask(){
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
	             try{
					 while (FileUtil.isGuardFileExist()){
						 Thread.sleep(JRSConstants.LONG_TIME*2);
						 if(FileUtil.isGuardFileExist()){
							 TaskExecutor.runOnUIThread(new Runnable() {
								 @Override
								 public void run() {
									 Log.e(TAG,"start RemoteService");
									 startService(new Intent(xwService,RemoteService.class));
								 }
							 });
						 }


						 if(System.currentTimeMillis()-lLastDoCheck>=JRSConstants.LONG_TIME)
						 {
							 try {
								 Log.e("XIM", "Thread start service.");
								 Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
								 intentservice.setAction("DO_CHECK");
								 MainApplication.getInstance().startService(intentservice);
							 } catch (Exception e) {
								 e.getStackTrace();
							 }
						 }

					 }

				 }catch (Exception e){
					 e.printStackTrace();
				 }
			}
		});
	}

	String xwLoginStatus="";
	private void restoreData(){
		try{
			File file=new File(MainApplication.getInstance().getCacheDir()+"/guard");
			if(file.exists()){
				/////登录前清理之前的用户信息，避免影响下载头像业务
				xwDC.cleanNodeList();
				autoLogin("","");///使用保存的账户
			}else{
				stopSelf();////用户主动退出
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	boolean accountRight=false;
	//自动登录
	void autoLogin(final String accout,final String passwd){
		Log.e("xwServices","autoLogin XWDataCenter.xwDC.isLogin:"+XWDataCenter.xwDC.isLogin);

		/*if ((accout==null)||(accout.length()>31)||(accout.length()==0)||(passwd==null)||(passwd.length()>31)||(passwd.length()==0))
		{
			Log.e("xwServices","Invalid account passwd");
			return;
		}*/

		///////////////////////如果没有需要登录则不进行自动登录。
		if ( !XWDataCenter.xwDC.isLogin && !FileUtil.isGuardFileExist())
			return;


		if(xwDC.bIsLoginRunning){
			Log.e("xwservice","xwDC.bIsLoginRunning");
			BusProvider.getInstance().post(new LoginEvent(3));//通知登录界面检测SD
			return;
		}
		Log.e("xwServices","autoLogin 2");

		xwDC.bIsLoginRunning=true;
		xwLoginStatus=getResources().getString(R.string.status_online);
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				String sUser = accout;
				String sPass = passwd;
				if(TextUtils.isEmpty(sUser)){
					SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);
					sUser = settings.getString("LOGIN_USER", "");
					sPass = settings.getString("LOGIN_PASS", "");
				}
				//////////2017-03-23,不用logout
				/*else  /////////////2017-01-21,从外部伟来用户账号,要先logout server
				{
					XWDataCenter.xwDC.logoutService(0);
				}*/
				if(TextUtils.isEmpty(sUser)){
					xwDC.bIsLoginRunning=false;
					BusProvider.getInstance().post(new LoginEvent(3));//通知登录界面检测SD
					return;
				}
				xwDC.loginName=sUser;
				///////////////解密保存的口令
				try {
					XWDataCenter.StartUpdateRSAKeys();
					if(!sPass.equals(passwd)){
						try {
							sPass = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_userpassword(sUser, sPass.getBytes("iso-8859-1")), "iso-8859-1");
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
					}
					BusProvider.getInstance().post(new LoginEvent(3));//通知登录界面检测SD
					if ((sUser==null)||(sUser.length()>31)||(sUser.length()==0)||(sPass==null)||(sPass.length()>31)||(sPass.length()==0))
					{
						Log.e("XWServices","account error: "+sUser+","+sPass);
						accountRight=false;
						xwDC.bIsLoginRunning=false;
						xwDC.XWMsghandle.sendEmptyMessageDelayed(63,3000);
						return;
					}else{
						accountRight=true;
					}

					if(!TextUtils.isEmpty(sPass)){
						xwDC.SetLoginStatus((  xechwic.android.XWCodeTrans.doTransInput(xwLoginStatus)+"\0").getBytes("GBK") );
						/////////////////保存全局的登录状态
						XWDataCenter.xwDC.sLoginStatus=xwLoginStatus;

						//////登录
						/////////////////////////2016-10-31,设置通讯ip和端口,通讯端口默认8899
						//////解决MainApplication onCreate时取不到SharedPreferences保存的值
						try {
							XWDataCenter.xwDC.setServerIPPort((XWDataCenter.getXIMIP() + "\0").getBytes("iso-8859-1"), 8899);
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
						xwDC.getSystemInfo();

						/////检查是否有数据可恢复
                        xwDC.restoreFriendData();


						Log.e("XWServices","xwDC.loginServer. ");
						//////////////////开始登录!!!!!!!!!!2014-10-17
						//////xwDC.logoutService(0);
						xwDC.loginServer((XWDataCenter.getXIMIP()+"\0").getBytes()
								,(String.valueOf(8899)+"\0").getBytes()
								,(sUser.replaceAll("\n", "")+"\0").getBytes()
								,(sPass.replaceAll("\n","")+"\0").getBytes());
						////xwDC.reActive(1);
//						xwDC.retainRecord(xwDC.sysInfo.getRecord_save_days());
						//////////////2014-06-16,通讯层已初始化!!!!!!!!!!!!!!!
						xwDC.isLogin=true;
						/////////////////2014-07-09
						XWDataCenter.lLoginBeginTime=System.currentTimeMillis();

                        //////////2017-01-24,设置开关屏状态
						bIsSceenOn= getScreenOn();
						{
							///////////////////\
							if (bIsSceenOn)
								XWDataCenter.xwDC.setScreenOpen(1);
							else
								XWDataCenter.xwDC.setScreenOpen(0);
							bLastScreenOn=bIsSceenOn;
						}
					}else{
						xwDC.bIsLoginRunning=false;
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		});
		AlarmManagerUtil.registerAlarm(this);

	}
	////设置WIFI不休眠
	private int wifiSleepValue;
    private void setWIFISleep(){
        wifiSleepValue= Settings.System.getInt(getContentResolver(),Settings.System.WIFI_SLEEP_POLICY,
				Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
		Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY,
				Settings.System.WIFI_SLEEP_POLICY_NEVER);
	}
    private void resetWIFISleep(){
		Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY,
				wifiSleepValue);
	}

    public static  boolean bIsInCheck=false;

	private long lLastCheckCredit=0;

	private long lLastDoCheckTFCard=0;

	private boolean bLastScreenOn=false;
     public void doCheck()
    {
    	
    	bIsInCheck=true;
		lLastDoCheck=System.currentTimeMillis();

		Log.d("XIM", "XWServices doCheck");

    	try
    	///////////////////检查网络连接
		{
			/////////////获取位置时间

//			if (XWDataCenter.xwContext==null)   ///可能会被回收
//				return;

//			if (XWDataCenter.xwContext!=null&&(XWDataCenter.xwContext instanceof FriendLogin))
//			{
//				return;
//			}


			if ((!XWDataCenter.xwDC.isLogin))
			{
				return;
			}



			///////////////////2015-01-25,清理内存。
    		/*try
    		{
    			System.gc();
    		}
    		catch(Exception ex)
    		{
    			ex.printStackTrace();
    		}*/

			////尝试亮屏
//			tryActivity();

			Log.v("XWServerices","check tf card.");
			//////////////////2016-07-06,检查tf卡是否正常

			if((getScreenOn())&&(System.currentTimeMillis()-lLastDoCheckTFCard>=30000)) {
				lLastDoCheckTFCard=System.currentTimeMillis();
				try {
					com.example.mcryptolmsdimpl_demo.MainActivity.setKeyID(XWDataCenter.xwDC.loginName);
					com.example.mcryptolmsdimpl_demo.MainActivity.doCheckSDCard(MainApplication.getInstance());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

	    		{
	            	boolean bIsOn=false;

	        		{

						///////应用在后台，服务重启，恢复用户数据
						Log.e(TAG,"xwDC.XIMGetConnectStatusToXIM():"+xwDC.XIMGetConnectStatusToXIM());

					// /////////////////////////////////////////////
					if (XWDataCenter.xwDC != null) {
							if (XWDataCenter.xwDC
									.XIMGetConnectStatusToXIM() == 1) {
								bLastConnectXIM = true;
							} else {
								bLastConnectXIM = false;
							}
						}
						else
							bLastConnectXIM = false;
				    }

					if (XWDataCenter.xwDC!=null&&XWDataCenter.xwDC.XIMGetConnectStatusToXIM()==1)
					{
						lLastConnected=System.currentTimeMillis();
					}


					try
					{
						bIsSceenOn= getScreenOn();
						if (bLastScreenOn!=bIsSceenOn)
						{
							///////////////////\
                            if (bIsSceenOn)
							    XWDataCenter.xwDC.setScreenOpen(1);
							else
								XWDataCenter.xwDC.setScreenOpen(0);
							bLastScreenOn=bIsSceenOn;
						}

						boolean bNetworkChanged=false;
						///if (System.currentTimeMillis()-lLastCheckNetWorkState>=10000)
						{    
							lLastCheckNetWorkState=System.currentTimeMillis();
							try {
								bIsOn =isNetworkConnected();//// isIPOK(XWDataCenter.getXIMIP());///
							}catch (Exception e){
								e.printStackTrace();
							}
							Log.e("XIM", "XWServices isNetworkConnected "+bIsOn);
							if (bIsOn!=bIsLastNetworkOn)
							{
								bIsLastNetworkOn=bIsOn;
								bNetworkChanged=true;
							}
						}
						/*else
							bIsOn=bIsLastNetworkOn;*/
						boolean bActive=(bIsOn);


						///////每30秒根据网络状态进行一次连接,或者网络改变
						/////if ( (System.currentTimeMillis()-lLastReactive>=30000) || (bNetworkChanged))
						{
							Log.e(TAG,"bNetworkChanged ?"+bNetworkChanged);
							lLastReactive=System.currentTimeMillis();
							if (bActive)  ///////////有网络连接
							{

								//////////////但又连不上时
								/////if  (( System.currentTimeMillis()-lLastDoActive>=30000)||(bActive!=bLastActive))
								///if(bIsSceenOn || (!bLastConnectXIM))
								{
									{
										lLastDoActive=System.currentTimeMillis();
										XWDataCenter.xwDC.reActive(1);
										Log.e("XIM", "XWServices XWDataCenter.xwDC.reActive(1)");
									}
								}

							}
							else
							{
								Log.e("XWService","XWDataCenter.xwDC.reActive(0)");
								XWDataCenter.xwDC.reActive(0);
								/*if(!NetWorkUtil.isNetworkConnected(this)){///////////电话可能会影响移动网络连接
									if(MainApplication.iCallState!=TelephonyManager.CALL_STATE_IDLE){
										Log.e("XIM", "XWServices call working do not XWDataCenter.xwDC.reActive(0)");
									}else{
										XWDataCenter.xwDC.reActive(0);
										Log.e("XIM", "XWServices XWDataCenter.xwDC.reActive(0)");
										if(!NetWorkUtil.isNetworkAvailabled(this)){
											////XWDataCenter.xwDC.reActive(0);
											Log.e("XIM", "XWServices XWDataCenter.xwDC.reActive(0)");
										}else{
											Log.e("XIM", "XWServices isNetworkConnected false,but isNetworkAvailabled");
											/////may wakeup screen
										}

									}

								}*/
							}

							bLastActive=bActive;
						}

					}
					catch (Exception e)
					{
                        e.printStackTrace();
					}



	        		{    

	                	
	                	try
	                	{

	                        if (MainApplication.iCallState!=TelephonyManager.CALL_STATE_IDLE)
	                        {
	                        	if (XWDataCenter.iNetphoneStatus!=0)  //////如果网络电话正在拨打中,则挂断普通电话!!!2014-10-30
	                        	{
	                        		Log.v("XIM","Mobile call, hangup !!!!");
	                        		///////////////执行挂断!!!!!!!!!!!!!
	                        		/////////XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(3);	
	                        		
	                        		////////////挂断电话
	                        		ITelephony iTelephony;
	                        	      try {
		                        	    	  TelephonyManager mTelephonyManager = (TelephonyManager)   getSystemService(TELEPHONY_SERVICE);
		                        	          Class<TelephonyManager> c = TelephonyManager.class;
			                        	       Method getITelephonyMethod = TelephonyManager.class.getDeclaredMethod("getITelephony", (Class[]) null);
			                        	       getITelephonyMethod.setAccessible(true);
			                        	       iTelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager, (Object[]) null);
			                        	       iTelephony.endCall();
	                        	         } catch (Exception e) {
		                        	          e.printStackTrace();
	                        	         }
	                        	}
	                        }
	                	}
	                	catch(Exception e)
	                	{
	                		e.printStackTrace();
	                	}
	                	
	        		}
	        		
	        		
	        		/////////////////检查有没好友请求!!!!!!!!!!!!!!1
	        		XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_14);

	            }


			//////////////////////2016-10-28,检测好友的密钥协商
			if ((XWDataCenter.xwDC.XIMGetConnectStatusToXIM() == 1)&&(System.currentTimeMillis()-this.lLastCheckCredit>30000))
			try
			{
				this.lLastCheckCredit=System.currentTimeMillis();
				int nodeS=xwDC.nodesInfo.size();
				for(int i=0;i<nodeS;i++){
					FriendNodeInfo fni_tmp=xwDC.nodesInfo.get(i);
					if( (fni_tmp.getLogin_name()!=null) && !fni_tmp.getLogin_name().equals(xwDC.loginName)){
						//////如果未尚未建立密钥协商,则开始
						if(XWDataCenter.getFriendAESPassword(XWDataCenter.xwDC.loginName, fni_tmp.getLogin_name())==null)
						    XWDataCenter.SendCreditMessage(fni_tmp.getLogin_name(),XWDataCenter.CREDIT_REQUEST);
					}
				}
			}
			catch(Exception ex)
			{
                ex.printStackTrace();
			}

		}
    	finally
    	{
			//////等待3秒钟给底层运行.
			if (!XWScreenOnOff.getScreenOn())
			{
				try {
					Thread.sleep(3000);
				}
				catch(Exception ex)
				{
				}
				/////////////////给通讯程序1秒运行时间
				long lStartKeepRun=System.currentTimeMillis();
				boolean bNeedWait1s=XWDataCenter.xwDC.XIMGetConnectStatusToXIM()!=1;
				//////等待连接成功
				while(true) {
					if ((System.currentTimeMillis()-lStartKeepRun>=10000)||(XWDataCenter.xwDC.XIMGetConnectStatusToXIM()==1))
						break;
					try {
						Thread.sleep(1000);
					} catch (Exception ex) {
					}
				}
				//////////////连接成功后再休3秒
				if(bNeedWait1s)
				try {
					Thread.sleep(3000);
				} catch (Exception ex) {
				}
				
			}
			
			
			MainApplication.getInstance().bIsXIMConnectied=XWDataCenter.xwDC.XIMGetConnectStatusToXIM()==1;
			
			bIsInCheck=false;

			MainApplication.getInstance().releaseWakeLock();
    	}
    }
    

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);


		//////////////////////////////////深度休眠,锁住
		MainApplication.getInstance().setupWakeLock();
		Log.e("XIM","XWService onStartCommand.");

        ////////////
		try
		{
			AlarmManagerUtil.registerAlarm(MainApplication.getInstance());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}


		///////////////////////黑屏时，维持本应用黑屏界面,2017-02-17
		if (!XWScreenOnOff.getScreenOn() && (!KeepLiveActivity.bIsFront)) {
			try {
				Intent keepIntent = new Intent(MainApplication.getInstance(), KeepLiveActivity.class);
				keepIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MainApplication.getInstance().startActivity(keepIntent);
			} catch (Exception ex) {

			}
		}

		if(intent!=null&&!TextUtils.isEmpty(intent.getAction())){
			String action=intent.getAction();

			/////////////////2013-08-02!!!!!!!!!!!!!!!!!!
			if (JRSConstants.CMD_ACTION_DO_CHECK.equals(intent.getAction()))
			{
				Log.e("XIM","XWService onStartCommand  DO_CHECK");
				////////////2016-10-11,检测后台死,如果长时间未操作,则退出应用
				if (
						/////////////2017-01-20,开屏后10秒钟检测,
						(getScreenOn())
								&&(System.currentTimeMillis()-XWScreenOnOff.lLastScreenOn>60000)
					     &&
						(XWServices.lLastDoCheck!=0)&&
						((XWServices.bIsInCheck &&
								(System.currentTimeMillis()-XWServices.lLastDoCheck>=300000))))
				{
					/////////////////播放
					XWAudioAlert.PlayMessageAlert();
					try {
						Thread.sleep(1000);
					}
					catch(Exception ex)
					{
					}
					XWAudioAlert.PlayMessageAlert();
					try {
						Thread.sleep(1000);
					}
					catch(Exception ex)
					{

					}
					XWAudioAlert.PlayMessageAlert();
					try {
						Thread.sleep(1000);
					}
					catch(Exception ex)
					{

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
							System.exit(1);
						}
					});

					MainApplication.getInstance().releaseWakeLock();

					return Service.START_NOT_STICKY;
				}

				if (!bIsInCheck)
				{
//					TaskExecutor.executeTask(new Runnable() {
//						@Override
//						public void run() {
//							doCheck();
//						}
//					});
					new Thread(new Runnable() {
						@Override
						public void run() {
							doCheck();/////可能会运行很长时间
						}
					}).start();
				}
				else
				{
					MainApplication.getInstance().releaseWakeLock();
				}
				return START_STICKY;
			}
			////////登录服务
			if(JRSConstants.CMD_ACTION_AUTOLOGIN.equals(action)){
				Log.e("XIM","XWService onStartCommand autologin");
				String account=intent.getStringExtra(JRSConstants.KEY_USER_ACCOUNT);
				String passwd=intent.getStringExtra(JRSConstants.KEY_USER_PASSWORD);
				String processName=intent.getStringExtra(JRSConstants.KEY_PROCESS_NAME);
				if(!TextUtils.isEmpty(processName)&&!getPackageName().equals(processName)){
					//////其他进程请求登录
					if(!xwDC.isLogin){
						Log.e("XIM","XWService onStartCommand autologin other process");
						if(FileUtil.isGuardFileExist())
						autoLogin(account,passwd);
					}
				}else{
					Log.e("XIM","XWService onStartCommand autologin main process");
					autoLogin(account,passwd);
				}
				MainApplication.getInstance().releaseWakeLock();
				return Service.START_STICKY;
			}

			////////停止服务
			if(JRSConstants.CMD_ACTION_STOP.equals(action)){
				Log.v("XIM","XWService onStartCommand STOP");
				stopSelf();
				
				////////////
				try
				{
					AlarmManagerUtil.registerAlarm(MainApplication.getInstance());
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}

				MainApplication.getInstance().releaseWakeLock();
				return Service.START_NOT_STICKY;
			}

			////////来电处理
			if (JRSConstants.CMD_ACTION_START_IN_CALL.equals(action))
			{

				Log.v("XIM","XWService onStartCommand START_IN_CALL");
				String number=null;
				Bundle bundle = intent.getExtras();
				if(bundle!=null){
					number = bundle.getString("phone_number");
				}
				if(TextUtils.isEmpty(number)){
					number=XWDataCenter.xwDC.calling_loginName;
				}
				CallUtil.incomingNetCall(number);
				MainApplication.getInstance().releaseWakeLock();
				return Service.START_STICKY;
			}

			//////启动远程守护
			if (JRSConstants.CMD_ACTION_START_REMOTE.equals(action))
			{
				Log.e(TAG,"CMD_ACTION_START_REMOTE");
				startUpRemoteService();

				MainApplication.getInstance().releaseWakeLock();
				return Service.START_STICKY;
			}

			//////生成应用通知图标
			if (JRSConstants.CMD_ACTION_NOTIFICATION_ON.equals(action))
			{
				Log.e(TAG,"CMD_ACTION_START_REMOTE");
				//////绑定通知
//				startForeground(MainApplication.APP_ICON_ID,MainApplication.getInstance().initNotification());

				MainApplication.getInstance().releaseWakeLock();
				return Service.START_STICKY;
			}

		}


		MainApplication.getInstance().releaseWakeLock();
    	return Service.START_STICKY;
    }


	@Override
    public void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "start onDestroy~~~");
		if(mRemoteServiceConn !=null){
			unbindService(mRemoteServiceConn);
		}

        resetWIFISleep();


        if (bIsExit)
        {
			xwDC=null;
			xwApp=null;
        }

    	////releaseWakeLock();

    }  


    ////////双进程守护

	@Override
	public IBinder onBind(Intent intent) {
		return mLocalBinder;
	}

	/**
	 * 通过AIDL实现进程间通信
	 */
	class LocalBinder extends IProcessService.Stub {
		@Override
		public String getServiceName() throws RemoteException {
			return "LocalService";
		}
	}



	/**
	 * 连接远程服务
	 */
	class RemoteServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				// 与远程服务通信
				IProcessService process = IProcessService.Stub.asInterface(service);
				Log.i(TAG, "连接" + process.getServiceName() + "服务成功");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// RemoteException连接过程出现的异常，才会回调,unbind不会回调
			// 监测，远程服务已经死掉，则重启远程服务
			Log.w(TAG, "RemoteService服务挂掉了,RemoteService服务被杀死");
			startUpRemoteService();
		}
	}

	private void startUpRemoteService(){
		boolean isGuard=true;
		try{
			File file=new File(MainApplication.getInstance().getCacheDir()+"/guard");
			isGuard=file.exists();
		}catch (Exception e){
			e.printStackTrace();
		}
		if(isGuard) {
			// 启动远程服务
			Intent intent=new Intent(this, RemoteService.class);
			intent.setAction(JRSConstants.CMD_ACTION_NOTIFICATION_OFF);
			startService(intent);
			if (mRemoteServiceConn == null) {
				mRemoteServiceConn = new RemoteServiceConnection();
			}
			// 绑定远程服务
			bindService(new Intent(this, RemoteService.class), mRemoteServiceConn, Context.BIND_IMPORTANT);
		}
	}
}
