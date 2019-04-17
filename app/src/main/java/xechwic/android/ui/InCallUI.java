package xechwic.android.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ydx.securephone.R;
import xechwic.android.FriendNodeInfo;
import xechwic.android.FriendVideoDisplay;
import xechwic.android.XWAudioAlert;
import xechwic.android.XWCodeTrans;
import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.util.Http;
import xechwic.android.util.TaskExecutor;
import xechwic.android.view.CircleImageView;

/**
 * 来电界面
 *
 */
public class InCallUI extends BaseUI{
	private final static String TAG = InCallUI.class.getSimpleName();


 
	public TextView callStatus;   //拨号状态

	public static StringBuffer inputSB=new StringBuffer();


	//余额前缀
	public String balancePre;
	private CircleImageView img_call_head;//头像
	private TextView tx_call_name;//好友名字
	private TextView tx_call_number;//拨打号码
	private String callNumber;

	WakeLock wakeLock=null;
    private int strRingerMode;//响铃模式
    private Vibrator vibrator;//震动
    


	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.e(TAG,"InCallUI onCreate");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

		);
		setContentView(R.layout.ui_incall);




//		//初始界面
		initView();
//		//初始资源
		initData();
//		//初始化拨号
		initCall();

		///////////2017-03-13
		if(XWDataCenter.video_preferred_width==0){//如果摄像头数据初始失败,则在此再次初始化
			XWDataCenter.xwDC.initCameraParam();
		}
	}

	
	
	/**
	 * 初始化拨号
	 */
	public void initCall(){
		Log.e(TAG,"FriendCall initCall:");
		////////////////////应该是空闲状态
		if (XWDataCenter.iNetphoneStatus==0)
			callStatus.setText(this.getResources().getString(R.string.alert_status_free));
		else if (XWDataCenter.iNetphoneStatus==1)
			callStatus.setText(this.getResources().getString(R.string.alert_status_begin));
		else if (XWDataCenter.iNetphoneStatus==2)
			callStatus.setText(this.getResources().getString(R.string.alert_status_calling));
		else if (XWDataCenter.iNetphoneStatus==3){
			Log.e(TAG,"callstatus 3");
		}

		else if ((XWDataCenter.iNetphoneStatus>=4)&&(XWDataCenter.iNetphoneStatus<10))
			this.callStatus.setText(this.getResources().getString(R.string.alert_that_hungup));	
		else if (XWDataCenter.iNetphoneStatus==11)
			this.callStatus.setText(this.getResources().getString(R.string.alert_status_incoming));	
		else if (XWDataCenter.iNetphoneStatus==11)
			this.callStatus.setText(this.getResources().getString(R.string.alert_status_incoming));				
		else if ((XWDataCenter.iNetphoneStatus>=13)&& (XWDataCenter.iNetphoneStatus<15))
			this.callStatus.setText(this.getResources().getString(R.string.alert_that_hungup));   
	
	}
	
	/**初始好友资料
	 * @param number
	 */
	private FriendNodeInfo initFriendData(String number){
		Log.e(TAG,"initFriendData:"+number);
		FriendNodeInfo node=null;
		if(number!=null){
			node = getAFriend(number);
			if(node!=null){
				if ( (node.getSignName()!=null) && (node.getSignName().length()>0) )
				    tx_call_name.setText(node.getSignName());
				else
					tx_call_name.setText(XWCodeTrans.doTrans("未知联系人"));
				if ((node.getLogin_name()!=null)&&(!node.getLogin_name().equalsIgnoreCase(node.getSignName())))
				    tx_call_number.setText(node.getLogin_name());
				else
					tx_call_number.setText("");

				String pic=FriendNodeDB.getFriendHead(node);
				
				if (pic!=null)
				{
					if(pic.contains("+")){
						try {
							pic=URLEncoder.encode(pic, "gbk");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
					Glide.with(MainApplication.getInstance())
							.load(Http.getHeadPicUrl()+pic)
							.diskCacheStrategy(DiskCacheStrategy.ALL)
							.error(R.drawable.icon)
							.into(img_call_head);
				}
			}
			else  //////2014-07-03,非好友,显示号码!!!!!!!!!!!!
			{
				tx_call_number.setText(number);
				tx_call_name.setText(XWCodeTrans.doTrans("未知联系人"));
				
				Log.e(TAG,"initFriendData: node == null");
			}
		}
		return node;
	}

	/**获取好友结点
	 * @param number
	 * @return
	 */
	private FriendNodeInfo getAFriend(String number){
		if(TextUtils.isEmpty(number)){
			return null;
		}
		return FriendNodeDB.getAFriend(XWDataCenter.xwDC.loginName, number);
	}
	
	
	/**处理传递过来的bundle
	 */
	private void handleBundle(){
		String number = null;
		Intent intent = getIntent();
		if (intent!=null)
		{
			Bundle bundle = intent.getExtras();
			if(bundle!=null){
				number = bundle.getString("phone_number");
			}
		}
		
		callNumber=number;
		//初始化好友
		initFriendData(number);

		inputSB.delete(0, inputSB.length());
		inputSB.append(number);
	}
	
	/**
	 * 初始资源
	 */
	private void initData(){
		//余额前缀
		balancePre=getResources().getString(R.string.alert_call_account);
		
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				
				);
		
		if (wakeLock!=null)
		{
			try
			{
				wakeLock.release();
				wakeLock=null;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		/////////////在这里有新数据里开屏,2014-07-10
		try
		{
			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
	                | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE ,
	                TAG);
	        wakeLock.setReferenceCounted(false);
	        wakeLock.acquire();
	        
	        Log.v(TAG, "wakeLock.acquire()");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
				
		//获取传递数据
		handleBundle();
	}
	
	/**
	 * 初始界面
	 */
	private void initView(){

		//头像
		img_call_head=(CircleImageView)findViewById(R.id.img_incall_head);

		//好友名字
		tx_call_name=(TextView)findViewById(R.id.tx_incall_name);

		//拨打号码
		tx_call_number=(TextView)findViewById(R.id.tx_incall_number);


		
		callStatus=(TextView)findViewById(R.id.tx_incall_status);


		findViewById(R.id.ic_answer).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AcceptCall();
	            baseAct.finish();
			}
	    });

		findViewById(R.id.ic_decline).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finishCall();
			}
	    });	    

	}


	//////结束电话
	public void finishCall(){
		phoneDown();
		leaveFriendCall();
		baseAct.finish();
	}


	/**进入通话界面
	 */
	private void AcceptCall(){

		if(!XWDataCenter.xwDC.cameraRunning)
		{
			Log.e(TAG,"acceptNetPhoneReq");
			

			///////////////////2014-06-30,检测横屏，竖屏!!!!!!!!!!!!
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
		    int width = dm.widthPixels;
	        int height = dm.heightPixels;
	        //////XWDataCenter.bIsVerticalScreen=   (height>width)  == (XWDataCenter.video_preferred_width>XWDataCenter.video_preferred_height);
	        Log.v(TAG,"phoneCall bIsVerticalScreen"+XWDataCenter.bIsVerticalScreen);

			if(XWDataCenter.video_compress_width>0){//如果需要压缩,则传压缩后的尺寸
				if (XWDataCenter.bIsVerticalScreen)
				{
					XWDataCenter.xwDC.setVideoRect(XWDataCenter.video_compress_height,XWDataCenter.video_compress_width ,XWDataCenter.xw_video_fps);
				}
				else
					XWDataCenter.xwDC.setVideoRect(XWDataCenter.video_compress_width, XWDataCenter.video_compress_height,XWDataCenter.xw_video_fps);
				/////Log.e(TAG,"call compress video width" +XWDataCenter.video_compress_width+ " heigh" +XWDataCenter.video_compress_height+ " fps"+XWDataCenter.xw_video_fps );
			}else{
				
				/////////////如果竖屏,进行反转!!!!!!!!2014-06-30
				if (XWDataCenter.bIsVerticalScreen)
				{
					XWDataCenter.xwDC.setVideoRect(XWDataCenter.video_preferred_height, XWDataCenter.video_preferred_width,XWDataCenter.xw_video_fps);
					  Log.e(TAG,"call video width" +XWDataCenter.video_preferred_height+ " heigh" +XWDataCenter.video_preferred_width+ " fps"+XWDataCenter.xw_video_fps );
				}
				else
				{
					XWDataCenter.xwDC.setVideoRect(XWDataCenter.video_preferred_width, XWDataCenter.video_preferred_height,XWDataCenter.xw_video_fps);
					  Log.e(TAG,"call video width" +XWDataCenter.video_preferred_width+ " heigh" +XWDataCenter.video_preferred_height+ " fps"+XWDataCenter.xw_video_fps );
				}
			}

			XWDataCenter.xwDC.acceptNetPhoneReq();

			String sAESPassword=XWDataCenter.getFriendAESPassword(XWDataCenter.xwDC.loginName, XWDataCenter.RegularPhoneNumber(callNumber));

			if (sAESPassword!=null)
				XWDataCenter.xwDC.XWNetphoneSetPhoneAESPassword((sAESPassword+"\0").getBytes());

			
			XWDataCenter.xwDC.isAudioRunning=true;	

			///////////////根据对方是否有vodie codec
			XWDataCenter.xwDC.needOpenVideo=true/*(xwDC.remote_video_codec>=0)*/;
			
			XWDataCenter.xwDC.audio_is_open=true;
			XWDataCenter.xwDC.video_is_open=false;//////xwDC.needOpenVideo;

		
			///////////////////////////////////////////
			//if(xwDC.needOpenVideo)
			{
				/////////xwDC.startXWAudio();
				Log.e(TAG,"remote video width"+XWDataCenter.xwDC.remote_video_width+" height"+XWDataCenter.xwDC.remote_video_height+" videocodec"+XWDataCenter.xwDC.remote_video_codec+"audio"+XWDataCenter.xwDC.remote_audio_codec);
				Log.e(TAG,"incall callNumber:"+callNumber);
				Intent nextPage=new Intent();
				nextPage.putExtra("phone_number", callNumber);
				nextPage.setClass(InCallUI.this, FriendVideoDisplay.class);
				startActivity(nextPage);
				InCallUI.this.finish();
			}

		}

		
	}

	@Override
	public void onBackPressed() {
		/////////参照微信，不响应返回按钮
	}

	@Override
	public void finish() {
		super.finish();
		/////停止响铃
		XWAudioAlert.getAudioAlert().stoptAudioAlert();
	}

	/**
	 * 挂断通话
	 */
	public void phoneDown(){
		Log.e(TAG,"---->phoneDown");


		//XWDataCenter.xwDC.hangupNetPhone();
		XWDataCenter.threadHangupNetPhone();
		
		
		XWDataCenter.xwDC.netPhoneTime=0;
		Log.e(TAG,"---->phoneDown1");

		XWDataCenter.xwDC.stopXWAudio();
		Log.e(TAG,"---->phoneDown2");


		XWDataCenter.xwDC.netPhoneTime=0;
		XWDataCenter.xwDC.calling_loginName="";    	

		XWDataCenter.xwDC.isAudioRunning=false;   	

		XWDataCenter.video_is_open=false;
		XWDataCenter.audio_is_open=false;	
		XWDataCenter.xwDC.needOpenVideo=true;
	}
	
	
	/**
	 * 离开正在通话界面
	 */
	public void leaveFriendCall(){
		XWDataCenter.xwDC.stopVibrator();
		finish();
	}
	
	protected void onPause(){	
		if (MainApplication.iCallState!=TelephonyManager.CALL_STATE_IDLE)
		{
			if(!XWDataCenter.xwDC.needOpenVideo){
				TaskExecutor.executeTask(new Runnable() {
					@Override
					public void run() {
						XWDataCenter.xwDC.hangupNetPhone();
						XWDataCenter.xwDC.netPhoneTime=0;
					}
				});

			}
		}
		
		super.onPause();
	}
	
	protected void onDestroy(){
		super.onDestroy();
		if(vibrator!=null){
			vibrator.cancel();
		}
		if (wakeLock!=null)
		{
			try
			{
				wakeLock.release();
				wakeLock=null;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	
	@Override  
    protected void onNewIntent(Intent intent) {  
            // TODO Auto-generated method stub  
            super.onNewIntent(intent);  
              
            initData();
    }  

	@Override
	protected void onResume() {
		super.onResume();

		///////////2014-07-10,在onResume不能执行解锁操作，否则程序直接死。
		int mode=GetAudioManagerMode();
		Log.e(TAG,"mode :"+mode);
		if(mode==AudioManager.RINGER_MODE_NORMAL){//正常
			
		}else if(mode==AudioManager.RINGER_MODE_SILENT){//静音
			
		}else if(mode==AudioManager.RINGER_MODE_VIBRATE){//静音震动
			 vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);  
			 vibrator.vibrate(new long[]{300,10,300,2000},0);
		}

	}
	
	
	/**获取响铃模式
	 * @return
	 */
	private int  GetAudioManagerMode(){
	    try
	    {
	      
	      AudioManager audioManager = 
	      (AudioManager)getSystemService(Context.AUDIO_SERVICE);
	      
	      if (audioManager != null)
	      {
	        
	           
	        strRingerMode = audioManager.getRingerMode();
	      }
	    }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
	    
	    return strRingerMode;
	}
	
}
