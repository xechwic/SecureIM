package xechwic.android;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import ydx.securephone.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import xechwic.android.act.MainApplication;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.ui.BaseUI;
import xechwic.android.util.Http;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.NotificationUtil;
import xechwic.android.util.PrefsUtils;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.XWDataCenterMessage;
import xechwic.android.view.CircleImageView;

/**
 *视频通话界面
 *
 */
public class FriendVideoDisplay extends BaseUI implements Callback,SensorEventListener{
	private String TAG=FriendVideoDisplay.class.getSimpleName();
	public XWDataCenter xwDC;
	public SurfaceView svLocal = null;
	public SurfaceHolder holderLocal = null;
	public TextView timeAlertView;
	public LinearLayout videoBigLayout=null;
	public LinearLayout videoSmallLayout=null;

	public LinearLayout textLayout=null;
	private VideoDataCallBack vdCallBack;
	public RelativeLayout layout;
	public Handler mHandler;
	boolean xw_is_first_draw=true;
	boolean xw_has_prepared=false;
	private RemoteVideoSurface remoteVideo;//对方图像
  private CircleImageView ivAvatar;


	public TextView button_hangup=null;

	public TextView friend_video_name=null;

    private boolean bLocalAvBig=false;


	ImageView friend_video_minimize =null;
	ImageView friend_video_micswitch=null;
	ImageView friend_video_videoswitch=null;
	ImageView friend_video_speakerswitch=null;
	ImageView friend_video_facingswitch=null;
	ImageView ivCameraSW;

	private Sensor mproximity =null;//感应器
	
	WakeLock wakeLock=null;
	private ImageView aeslock;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		xw_is_first_draw=true;

		Log.v("xim","FriendVideoDisplay OnCreate");		
		try
		{
			/////////////2012-03-11,禁止休眠!!!!!!!!!!		

			try
			{
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				getWindow().setFormat(PixelFormat.TRANSLUCENT);
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

				
				getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						
						);
				
				

			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}

			setContentView(R.layout.friend_video_display);
			initView();
			setupAvatar();
			mHandler=new FriendVideoDisplayHandle(this);
		((FriendVideoDisplayHandle)mHandler).showTimeView();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}





	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.e(TAG ,"onNewIntent");
	}

	/**
	 */
	private void addBigVideoLayout(SurfaceView surfaceView) {
		if (surfaceView.getParent() != null)
			((ViewGroup)surfaceView.getParent()).removeView(surfaceView);
		videoBigLayout.addView(surfaceView);
		surfaceView.setZOrderMediaOverlay(false);
	}

	/**
	 */
	private void addSmallVideoLayout(SurfaceView surfaceView) {
		if (surfaceView.getParent() != null) {
			((ViewGroup)surfaceView.getParent()).removeView(surfaceView);
		}
		videoSmallLayout.addView(surfaceView);
		surfaceView.setZOrderMediaOverlay(true);
	}

	private void setupAvatar(){
		if(!TextUtils.isEmpty(xwDC.sCurrentPhoneNumber)){
			 String pic= FriendNodeDB.getFriendHead(xwDC.sCurrentPhoneNumber);
			if(!TextUtils.isEmpty(pic)){
				String iconPath= Http.getHeadPicUrl()+pic;
				Glide.with(MainApplication.getInstance())
						.load(iconPath)
						.error(R.drawable.def_avatar)
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.into(ivAvatar);
			}
		}
	}
	/**
	 * 初始控件
	 */
	private void initView(){
		ivCameraSW=(ImageView)findViewById(R.id.iv_camera_sw);
    ivAvatar=(CircleImageView)findViewById(R.id.iv_avatar);
		this.xwDC = ((MainApplication)this.getApplication()).getDC(this);	

		this.layout=(RelativeLayout)this.findViewById(R.id.root_friend_video_display);


		this.videoBigLayout=(LinearLayout)this.findViewById(R.id.video_big);
		this.videoSmallLayout=(LinearLayout)this.findViewById(R.id.video_small);
		this.textLayout=(LinearLayout)this.findViewById(R.id.friend_video_display_textLayout);

		this.timeAlertView=(TextView)this.findViewById(R.id.friend_video_display_time);


		/////////////////////////
		friend_video_name=(TextView)this.findViewById(R.id.friend_video_name);
		button_hangup=(TextView) this.findViewById(R.id.hangup);

		button_hangup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				stopVideo();	
			}
		});

		timeAlertView.setPadding(0, 5, 0, 0);

		ivCameraSW.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(PrefsUtils.getInstance().get(JRSConstants.KEY_CAMERA_FACING,0)==0){
					PrefsUtils.getInstance().put(JRSConstants.KEY_CAMERA_FACING,1);
				}else{
					PrefsUtils.getInstance().put(JRSConstants.KEY_CAMERA_FACING,0);
				}
				startVideo();
			}
		});

		videoSmallLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				bLocalAvBig=!bLocalAvBig;
//				if(bLocalAvBig){
//					addBigVideoLayout(svLocal);
//					addSmallVideoLayout(remoteVideo);
//				}else{
//					addBigVideoLayout(remoteVideo);
//					addSmallVideoLayout(svLocal);
//				}
			}
		});
		
		remoteVideo=new RemoteVideoSurface(this);
		remoteVideo.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addBigVideoLayout(remoteVideo);
		
		
		svLocal = new SurfaceView(this);
		svLocal.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addSmallVideoLayout(svLocal);

		holderLocal = svLocal.getHolder();

		holderLocal.addCallback(this);
		holderLocal.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		vdCallBack =new VideoDataCallBack();


		friend_video_minimize =(ImageView )findViewById(R.id.friend_video_minimize);
		friend_video_micswitch=(ImageView)findViewById(R.id.friend_video_micswitch);
		friend_video_videoswitch=(ImageView)findViewById(R.id.friend_video_videoswitch);
		friend_video_speakerswitch=(ImageView)findViewById(R.id.friend_video_speakerswitch);
		friend_video_facingswitch=(ImageView)findViewById(R.id.friend_video_facingswitch);
		RedrawSwitchs();

		friend_video_minimize.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				///////////最小化
				Log.v("XIM","friend_video_minimize onclick");
				baseAct.finish();
				Intent intent=new Intent(baseAct,FriendControl.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				showToastTips(XWCodeTrans.doTrans("请点击[视频]图标返回通话界面！"));
			}
		}
				);

		friend_video_micswitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				///////////
				Log.v("XIM","friend_video_micswitch onclick");
				XWDataCenter.audio_is_open=!XWDataCenter.audio_is_open;
				RedrawSwitchs();
			}
		}
				);	


		friend_video_videoswitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				///////////
				Log.v("XIM","friend_video_videoswitch onclick");
				XWDataCenter.video_is_open=!XWDataCenter.video_is_open;
				ReOpenVideoCap();
				RedrawSwitchs();
			}
		}
				);				


		friend_video_speakerswitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AudioManager am = (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
				try
				{
					{
						{
							Log.e("fvd","am.isSpeakerphoneOn() "+am.isSpeakerphoneOn()+",turn "+!am.isSpeakerphoneOn());
							xwDC.bIsSpeakerOn=!am.isSpeakerphoneOn();
							am.setSpeakerphoneOn(xwDC.bIsSpeakerOn);
							restoreSpeakerOnVolumn();
						}
					}			

				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

				if (am.isSpeakerphoneOn()) //////如果打开免提,则将音量调到最小
				{
					Log.e("fvd","am.isSpeakerphoneOn() has turn"+am.isSpeakerphoneOn());
                    for(int i=0;i<8;i++)
					{
							AudioManager audio = (AudioManager) FriendVideoDisplay.this.getSystemService(Service.AUDIO_SERVICE);
							{
								try {
									/////调音量
									audio.adjustStreamVolume(
											AudioManager.STREAM_VOICE_CALL,
											AudioManager.ADJUST_LOWER,
											AudioManager.FLAG_SHOW_UI);
								} catch (Exception ex1) {
                                    ex1.printStackTrace();
								}

							}
						}

					restoreSpeakerOnVolumn();
				}



				RedrawSwitchs();
			}
		}
				);				

		
		//关闭界面
		aeslock=(ImageView)findViewById(R.id.friend_video_AESlock);
		aeslock.setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						 Toast.makeText(getApplicationContext(), XWCodeTrans.doTrans("数据加密保护生效中."), Toast.LENGTH_SHORT).show();
					}
				});

		friend_video_facingswitch.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
                 if(PrefsUtils.getInstance().get(JRSConstants.KEY_CAMERA_FACING,0)==0){
					 PrefsUtils.getInstance().put(JRSConstants.KEY_CAMERA_FACING,1);
				 }else{
					 PrefsUtils.getInstance().put(JRSConstants.KEY_CAMERA_FACING,0);
				 }
				startVideo();
			}
		});
	}

	private void RedrawSwitchs()
	{
		try
		{
			if (XWDataCenter.audio_is_open)
			{
				friend_video_micswitch.setImageResource(R.drawable.ic_micro_dark);
			}
			else
			{
				friend_video_micswitch.setImageResource(R.drawable.ic_mute_holo_dark);
			}

			if (XWDataCenter.video_is_open)
			{
				friend_video_videoswitch.setImageResource(R.drawable.ic_prefs_media_video);
				friend_video_facingswitch.setVisibility(View.GONE);


			}
			else
			{
				friend_video_facingswitch.setVisibility(View.GONE);
				friend_video_videoswitch.setImageResource(R.drawable.ic_prefs_media_novideo);
			}			

			try
			{
				AudioManager am = (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
				boolean	bIsSpeakerOn=am.isSpeakerphoneOn();
				if (bIsSpeakerOn)
				{
					friend_video_speakerswitch.setImageResource(R.drawable.ic_speaker_dark);
				}
				else
				{
					friend_video_speakerswitch.setImageResource(R.drawable.ic_sound_speakerphone_holo_dark);	
				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}



    @Override
	protected void onDestroy(){
		super.onDestroy();
		if(mHandler!=null){
			mHandler.removeCallbacksAndMessages(null);
			((FriendVideoDisplayHandle)mHandler).setContext(null);
		}
	}
	
	@Override
	protected void onResume(){
		super.onResume();

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
		
		regSensor();
		///////////画开关
		RedrawSwitchs();

		resetVideoSize();

		Log.e(TAG,"onResume");
		
		preparedConnect();
		
		
		if (XWDataCenter.getFriendAESPassword(XWDataCenter.xwDC.loginName, XWDataCenter.RegularPhoneNumber(XWDataCenter.xwDC.sCurrentPhoneNumber))!=null)
		{
			aeslock.setVisibility(View.VISIBLE);
		}
		else
		{
			aeslock.setVisibility(View.GONE);
		}
		
		startVideo();
		///清理状态栏通知
		NotificationUtil.cleanNotificationByID(JRSConstants.NOTICE_VIDEO_DISPLAY);
	}



	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG,"onPause");
		if (MainApplication.iCallState!=TelephonyManager.CALL_STATE_IDLE)
		{
			if(!xwDC.needOpenVideo){
				TaskExecutor.executeTask(new Runnable() {
					@Override
					public void run() {
						Log.e("Fvd","hangupNetPhone");
						xwDC.hangupNetPhone();
						xwDC.netPhoneTime=0;
					}
				});

			}
		}else if(xwDC.cameraRunning){
			NotificationUtil.notificationVideo(xwDC.sCurrentPhoneNumber);
		}

	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.e(TAG,"onStop");	
		unregSensor();

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
	

	/**
	 * 注册感应器
	 */
	private void regSensor(){
		SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		///////////////////proximity Sensor to auto-control the speakers
		if (mSensorManager!=null)
		{
			mproximity= mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY); 

			if (mproximity!=null)
				mSensorManager.registerListener(
						this
						,mproximity,SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		iLastClose=-1;
		
		
		fLastDistance=-1;
		
		Log.e(TAG,"regSensor");
	}



	/**
	 * 注销感应器
	 */
	private void unregSensor(){
		SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		///////////////////proximity Sensor to auto-control the speakers
		if (mSensorManager!=null)
		{
			mproximity= mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY); 

			if (mproximity!=null)
				mSensorManager.unregisterListener(this);
		}
		
		Log.e(TAG,"unregSensor");

	}

	/**
	 * 重置视频大小
	 */
	private void resetVideoSize(){
		
	}

	public synchronized void preparedConnect(){
		Log.v("tag", "preparedConnect");
		xw_is_first_draw=true;

		xw_has_prepared=true;

		{//非模拟器版本
			xwDC.startXWAudio();//音频服务线程启动
		}
		
		
		///////无论是否当前有,都启动视频服务!!!!!
		mHandler.sendEmptyMessage(XWDataCenterMessage.MSG_21);
	}

	synchronized public void ReOpenVideoCap()
	{
		Log.v("XIM", "ReOpenVideoCap");		

		if ((xwDC.mCamera==null)||(holderLocal==null))
			return;		

		try
		{

			try
			{
				xwDC.mCamera.stopPreview();		
			}
			catch(Exception e1)
			{
               e1.printStackTrace();
			}
			try
			{
				this.setCameraParam();
			}catch(Exception e2)
			{
                e2.printStackTrace();
			}
			Log.v("XIM", "ReOpenVideoCap 2");	

			////this.setCameraParam();
			try
			{
				holderLocal.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				xwDC.mCamera.setPreviewDisplay(holderLocal);		
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			///////////////////2012-04-24
			if (XWDataCenter.video_is_open)
			{
				xwDC.mCamera.startPreview();
				Log.v("XIM", "ReOpenVideoCap 3 startPreview");
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}

	}

	//////////////2012-03-19,解决休眠后回来本地显示消失的问题!!!!!!!!!!!
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height){
		holderLocal=holder;
		if(!xw_has_prepared)
		{//如果已经初始化过了
			this.preparedConnect();
			return;
		}

		if (xwDC.mCamera==null)
			return;		

		try
		{
			xwDC.mCamera.stopPreview();		
			this.setCameraParam();
			try
			{
				holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				xwDC.mCamera.setPreviewDisplay(holder);		
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			///////////////////2012-04-24
			if (XWDataCenter.video_is_open)
				xwDC.mCamera.startPreview();
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder){

		holderLocal=holder;


		if(!xw_has_prepared){//如果已经初始化过了
			this.preparedConnect();
			return;
		}

		if (xwDC.mCamera==null)
			return;		

		try
		{
			xwDC.mCamera.stopPreview();		
			this.setCameraParam();
			try
			{
				holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				xwDC.mCamera.setPreviewDisplay(holder);		
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			///////////////////2012-04-24
			if (XWDataCenter.video_is_open)
				xwDC.mCamera.startPreview();			
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder){
		if(XWDataCenter.xwDC.mCamera!=null){
			try
			{
				holder.removeCallback(this);
				XWDataCenter.xwDC.mCamera.setPreviewCallback(null);
				XWDataCenter.xwDC.mCamera.stopPreview();
				
				XWDataCenter.xwDC.mCamera.release();
				XWDataCenter.xwDC.mCamera=null;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}

		holderLocal=null;
	} 



	public void VideAudioSwitch()
	{
		try{
			TextView videoCaption=new TextView(this);
			CheckBox videoCheck=new CheckBox(this);
			TextView audioCaption=new TextView(this);
			CheckBox audioCheck=new CheckBox(this);
			videoCaption.setText(xechwic.android.XWCodeTrans.doTrans("视频发送:"));
			audioCaption.setText(xechwic.android.XWCodeTrans.doTrans("音频发送:"));
			videoCheck.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
					XWDataCenter.video_is_open=isChecked;

					///////////////////////重新打开
					ReOpenVideoCap();
				}
			});
			audioCheck.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
					XWDataCenter.audio_is_open=isChecked;
				}
			});
			videoCheck.setChecked(XWDataCenter.video_is_open);
			audioCheck.setChecked(XWDataCenter.audio_is_open);


			final Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(xechwic.android.XWCodeTrans.doTrans("音视频设置:"));
			TableLayout builderLayout=new TableLayout(this);
			builderLayout.setGravity(Gravity.CENTER);
			//builderLayout.setColumnStretchable(1, true);
			builderLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			TableRow row_1=new TableRow(this);
			row_1.setGravity(Gravity.CENTER);
			row_1.addView(videoCaption);
			row_1.addView(videoCheck);
			builderLayout.addView(row_1);
			TableRow row_2=new TableRow(this);
			row_2.setGravity(Gravity.CENTER);
			row_2.addView(audioCaption);
			row_2.addView(audioCheck);
			builderLayout.addView(row_2);
			builder.setView(builderLayout);
			builder.setPositiveButton(xechwic.android.XWCodeTrans.doTrans("确定"),new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
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
		}catch(Exception e ){
			e.printStackTrace();
		}		
	}


	
	synchronized public void startVideo(){
		Log.v("xim", "FriendVideoDisplay startVideo");

		{
			try{

				xwDC.openCamera();


				if(xwDC.cameraRunning){
					if(xwDC.mCamera!=null){
						xwDC.mCamera.stopPreview();
					}
				}
				if(xwDC.mCamera!=null){
					this.setCameraParam();


					holderLocal=svLocal.getHolder();
					xwDC.mCamera.setPreviewDisplay(holderLocal);

					///////////////////2012-04-24
					if (XWDataCenter.video_is_open)
						xwDC.mCamera.startPreview();
				}
				if (xwDC.remoteVideoThread==null)
					xwDC.remoteVideoThread=xwDC.startRemoteVideo();

			} catch(Exception e){
				e.printStackTrace();
				this.videoAudioException(new StringBuffer("camera open error!"));
			}
		}


		////////((Vibrator)getSystemService(Service.VIBRATOR_SERVICE)).vibrate(new long[]{0,10,200,50},-1);

		//throw new Exception();
		xwDC.cameraRunning=true;
		xwDC.remoteVideoRunning=true;

		try
		{
			xwDC.remoteVideoThread.start();
		}
		catch(Exception e1)
		{
             e1.printStackTrace();
		}


		xwDC.displayVideoTime();		



	}

	public void setCameraParam(){
		Camera.Parameters p;
		try
		{
			xwDC.setCameraFormat(XWDataCenter.video_preferred_format);
		}
		catch(Exception e)
		{
            e.printStackTrace();
		}


		try
		{
			p = xwDC.mCamera.getParameters();//p.set("camera-id",2);
			p.setPreviewFrameRate(XWDataCenter.xw_video_fps);
			xwDC.mCamera.setParameters(p);
		}
		catch(Exception e)
		{
           e.printStackTrace();
		}

		try
		{
			p = xwDC.mCamera.getParameters();//p.set("camera-id",2);
			p.setPreviewFormat(XWDataCenter.video_preferred_format);
			xwDC.mCamera.setParameters(p);
		}
		catch(Exception e)
		{
           e.printStackTrace();
		}

		try
		{
			p = xwDC.mCamera.getParameters();//p.set("camera-id",2);
			p.set("camera-id",2);
			xwDC.mCamera.setParameters(p);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}



		////////////////竖屏
		try
		{	

			if (XWDataCenter.bIsVerticalScreen)  /////竖屏
			{
				Camera.Parameters parameters = xwDC.mCamera.getParameters();


				try
				{
					parameters.set("orientation", "portrait");
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}

				try
				{
					parameters.setRotation(90);//去掉android2.0和之前的版本
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}


				try
				{
					xwDC.mCamera.setDisplayOrientation(90);
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}

				try
				{
					parameters.setPreviewSize(XWDataCenter.video_preferred_width,XWDataCenter.video_preferred_height );
					xwDC.mCamera.setParameters(parameters);
				}
				catch (Exception ex1)
				{
					ex1.printStackTrace();
				}
				Log.v("XIM","screen portrait, scroll camera.");
			}
			else /////横屏
			{
				Camera.Parameters parameters = xwDC.mCamera.getParameters();

				try
				{
					parameters.set("orientation", "landscape");
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}

				try
				{
					parameters.setRotation(0);//去掉android2.0和之前的版本
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}


				try
				{
					xwDC.mCamera.setDisplayOrientation(0);
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}



				try
				{
					parameters.setPreviewSize(XWDataCenter.video_preferred_width, XWDataCenter.video_preferred_height);
					xwDC.mCamera.setParameters(parameters);
				}
				catch(Exception e)
				{
                  e.printStackTrace();
				}

			}
		xwDC.mCamera.setPreviewCallback(vdCallBack);
		}
		catch(Exception ex)
		{
             ex.printStackTrace();
		}




	}
	public void videoAudioException(StringBuffer sb){
	}
	
	
	/////////////2014-10-21,
	synchronized public void stopVideo(){
		Log.v("tag", "stopVideo.................................");
		/////////////关闭扩音!!!!!!!!!!!!
		try
		{
			AudioManager am = (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
			{
				{
					am.setSpeakerphoneOn(false);
				}
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		xwDC.cameraRunning=false;
		xwDC.isAudioRunning=false;
		xwDC.bIsSpeakerOn=false;

		XWDataCenter.video_is_open=false;
		XWDataCenter.audio_is_open=false;	
		xwDC.needOpenVideo=true;

		xw_has_prepared=false;	
		
		
		if(xwDC.mCamera!=null){
			try
			{
				xwDC.mCamera.setPreviewCallback(null);
				xwDC.mCamera.stopPreview();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		
		try
		{
			xwDC.remoteVideoRunning=false;
			
			xwDC.calling_loginName="";//设为空闲
			xwDC.netPhoneTime=0;
			xwDC.timeSB.delete(0, xwDC.timeSB.length());
			xwDC.accountSB.delete(0, xwDC.accountSB.length());
			
			
			xwDC.netPhoneTime=0;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				try{
					if(XWDataCenter.EditionType>0){
						xwDC.stopXWAudio();
					}
					Log.v("tag", "stopVideo2.................................");

					if(xwDC.remoteVideoThread!=null){
						try{
							xwDC.remoteVideoThread.join();
						}catch(InterruptedException e){
							e.printStackTrace();
						}
						xwDC.remoteVideoThread=null;
					}

				}catch(Exception e){
					e.printStackTrace();
				}

				xwDC.hangupNetPhone();
			}
		});
		FriendVideoDisplay.this.finish();
	}


	/////对方视频的绘制
	public void drawRemoteData(byte []data,int Length){
         if(!bIsFront){////界面不在当前不绘制
			 return;
		 }
		if (data==null)
			return;

		try{
			{
				this.remoteVideo.drawBMP(data,Length);
				Log.v(TAG,"this.remoteVideo.drawBMP(data)");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		//////参考微信去除返回按钮响应
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    switch (keyCode) {
	    case KeyEvent.KEYCODE_VOLUME_UP:
	    	try
	    	{
	    		AudioManager audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
	    		{
		            /////调音量
		            audio.adjustStreamVolume(
		    	            AudioManager.STREAM_VOICE_CALL,
		    	            AudioManager.ADJUST_RAISE,
		    	            AudioManager.FLAG_SHOW_UI);
	    		}
	    	}
	    	catch(Exception ex)
	    	{
	    		ex.printStackTrace();
	    	}

			saveSpeakerOnVolumn();
            return true;
	    case KeyEvent.KEYCODE_VOLUME_DOWN:
	    	try
	    	{
	    		AudioManager audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
	    		{
		            try
		            {
			            /////调音量
			            audio.adjustStreamVolume(
			    	            AudioManager.STREAM_VOICE_CALL,
			    	            AudioManager.ADJUST_LOWER,
			    	            AudioManager.FLAG_SHOW_UI);
		            }
		            catch(Exception ex1)
		            {
		                ex1.printStackTrace();
		            }


	    		}
     	    }
	    	catch(Exception ex)
	    	{
	    		ex.printStackTrace();
	    	}
			saveSpeakerOnVolumn();
			return true;
	    default:
	        break;
	    }
	    return super.onKeyDown(keyCode, event);
	}



	public void drawRemoteData(byte []data,int width,int height){

	}

	/**
	 * 关闭屏幕
	 */
	private void disableScreen(){
		Log.e(TAG,"disableScreen");
		if (this.isFinishing())
			return;
		
		try
		{
			layout.setVisibility(View.GONE);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

	/**
	 * 点亮屏幕
	 */
	private void lightScreen(){
		Log.e(TAG,"lightScreen");
		try
		{
		    layout.setVisibility(View.VISIBLE);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {}



	private boolean bIsVideoSwitchOpen=false;
	private int iLastClose=-1;
	
	private float fLastDistance=-1;

	private boolean bSpeakerON=false;

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {  

			Log.e(TAG,"onSensorChanged "+event.values[0]+ ","+ mproximity.getMaximumRange());
			
			if (fLastDistance<0)
			{
				fLastDistance=event.values[0];
				return;
			}
			
			if (event.values[0] <   fLastDistance/* mproximity.getMaximumRange()*/) 
			{  
				
				Log.v(TAG,"onSensorChanged close screen");

				disableScreen();
				
				if (iLastClose!=1)
				{
					iLastClose=1;
				}
				else
					return;


				bIsVideoSwitchOpen=XWDataCenter.video_is_open;
				if (bIsVideoSwitchOpen) {
					XWDataCenter.video_is_open = false;
					ReOpenVideoCap();
				}


					try
					{
						AudioManager am = (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
					    bSpeakerON=am.isSpeakerphoneOn();
						if(bSpeakerON){
							am.setSpeakerphoneOn(false);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}

			}  
			else if  (event.values[0] >   fLastDistance/* mproximity.getMaximumRange()*/)   ////////////////离开,恢复
			{				
				Log.v(TAG,"onSensorChanged open screen");
				lightScreen();
				if (iLastClose!=0)
				{
					iLastClose=0;
				}
				else
					return;

				XWDataCenter.video_is_open=bIsVideoSwitchOpen;
				if(bIsVideoSwitchOpen)
			        ReOpenVideoCap();
				
				if (bSpeakerON)
				{
					try
					{
						AudioManager am = (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
						{

								am.setSpeakerphoneOn(bSpeakerON);
								restoreSpeakerOnVolumn();
						}			
				
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				this.RedrawSwitchs();
			}
			
			fLastDistance=event.values[0];

		}

	}
	

	private void saveSpeakerOnVolumn()
	{
			try
			{
				AudioManager am=(AudioManager) xwDC.xwApp.getSystemService(Context.AUDIO_SERVICE);
				if (am.isSpeakerphoneOn()) {
					am = (AudioManager) xwDC.xwApp.getSystemService(Context.AUDIO_SERVICE);
					//am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, seekBar.getProgress(), AudioManager.FLAG_PLAY_SOUND);
					int iVolume = am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

					SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);
					SharedPreferences.Editor editor = settings.edit();
					{//只有第一次安装才创建icon
						editor.putInt("VIDEO_SPEAKER_VOLUME", iVolume);
						editor.commit();
					}
				}
			}
			catch(Exception ex)
			{
               ex.printStackTrace();
			}
	}

	private void restoreSpeakerOnVolumn()
	{
			try
			{
				AudioManager am=(AudioManager) xwDC.xwApp.getSystemService(Context.AUDIO_SERVICE);
				if (am.isSpeakerphoneOn()) {
					SharedPreferences settings = getSharedPreferences(XWDataCenter.PackageName, 0);
					int iVolume = settings.getInt("VIDEO_SPEAKER_VOLUME", -1);

					if (iVolume > 0) {
						am = (AudioManager) xwDC.xwApp.getSystemService(Context.AUDIO_SERVICE);
						am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, iVolume, 0);
					}
				}
			}
			catch(Exception ex)
			{
                ex.printStackTrace();
			}
	}



}
