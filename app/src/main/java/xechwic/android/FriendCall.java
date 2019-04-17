package xechwic.android;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ydx.securephone.R;
import xechwic.android.act.MainApplication;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.ui.BaseUI;
import xechwic.android.util.Http;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.XWDataCenterMessage;
import xechwic.android.view.CircleImageView;

/**
 * 正在通话界面  ,  CallView子界面进行拨打跳转到这里
 *
 */
public class FriendCall extends BaseUI implements OnClickListener{
	private final static String TAG = "FriendCall";


	public XWDataCenter xwDC;



	public static StringBuffer inputSB=new StringBuffer();
	public String callNumber;
	public Handler mHandler;

	private CircleImageView img_call_head;//头像
	private TextView tx_call_name;//好友名字
	private TextView tx_call_number;//拨打号码
	private ImageView img_call_handup;//挂断
	private ImageView ivSpeaker;//扩音器
	private ImageView img_back;//退出
	private TextView tx_call_status;//拨号状态

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

		);

		Log.e(TAG,"FriendCall onCreate");
		setContentView(R.layout.friend_call);
		//		//Log.e("tag", "SelectFriendUI onResume");
		this.xwDC = ((MainApplication) this.getApplication()).getDC(this);
		//初始界面
		initView();
		//初始化拨号
		initCall(1);

	}

	private void initView(){
		Log.e(TAG,"FriendCall initView");
        //拨号状态
		tx_call_status=(TextView)findViewById(R.id.friend_call_alert);
		//头像
		img_call_head=(CircleImageView)findViewById(R.id.img_friend_call_head);
		img_call_head.setOnClickListener(this);

		//好友名字
		tx_call_name=(TextView)findViewById(R.id.tx_call_name);
		tx_call_name.setOnClickListener(this);

		//拨打号码
		tx_call_number=(TextView)findViewById(R.id.tx_call_number);
		tx_call_number.setOnClickListener(this);



		//挂断
		img_call_handup=(ImageView) findViewById(R.id.img_call_handup);
		img_call_handup.setOnClickListener(this);
		ivSpeaker=(ImageView)findViewById(R.id.iv_speaker);
		ivSpeaker.setOnClickListener(this);
		//退出按钮
		img_back=(ImageView)findViewById(R.id.img_back);
		img_back.setOnClickListener(this);



		mHandler=new FriendCallHandle(this);



		//////////显示余额和时间
//		this.mHandler.sendEmptyMessage(6);

		
		
		
		///////10秒后,如果仍为空闲,则关闭,2014-10-20,处理有时拨号不操作的现象,10秒如果还为空闲则退出。
		mHandler.sendEmptyMessageDelayed(10, 10000);
		
		
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.e("chat","onNewIntent");
	}

	public void setCallViewStatus(String status){
		tx_call_status.setText(status);
	}


	public void initCall(int p){
		Log.e(TAG,"FriendCall initCall:"+p);

		//处理bundle
		handleBundle(p);
	}

	/**获取好友结点
	 * @param number
	 * @return
	 */
	private FriendNodeInfo getAFriend(String number){
		FriendNodeInfo node=null;
		if(number==null||number.length()<1){
			return node;
		}
		////////if(xwDC!=null&&xwDC.friendDB!=null&&XWDataCenter.fni!=null)
		if (xwDC!=null)
		{
			node=XWDataCenter.getFriendDB().getAFriend(XWDataCenter.getCurAccount(), number);
		}

		return node;
	}

	/**初始好友资料
	 * @param number
	 */
	private void initFriendData(String number){
		Log.e(TAG,"initFriendData:"+number);
		try
		{
		if(number!=null){
			FriendNodeInfo node = getAFriend(number);
			if(node!=null){
				
				if (node.getSignName()!=null)
				    tx_call_name.setText(""+node.getSignName());
				
				if ( (node.getLogin_name()!=null) && (!node.getLogin_name().equalsIgnoreCase(node.getSignName())))
				    tx_call_number.setText(""+node.getLogin_name());
				else
					tx_call_number.setText("");

				String pic= FriendNodeDB.getFriendHead(node);
				if (pic!=null)
				{
					//////if(pic!=null && (pic.contains("+")))
					{
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
			}else{
				tx_call_name.setText(""+XWCodeTrans.doTrans("未知联系人"));
				tx_call_number.setText(""+number);
			}
		}
		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**处理传递过来的bundle
	 * @param p
	 */
	private void handleBundle(int p){
		Log.e(TAG,"handleBundle:"+p);
		Intent intent = getIntent();
		Bundle bundle=intent.getExtras();
		//int node_id=bundle.getInt("NodeID");
		if(bundle!=null){//如果是从FriendSendMessage页面过来的则直接拨号
			if(bundle.getString("phone_number")!=null && p == 1){
				callNumber = bundle.getString("phone_number");

				//初始化好友
				initFriendData(callNumber);

				String tag = bundle.getString("tag");
				Log.e(TAG,"FriendCall:"+callNumber+",tag:"+tag);
				if(tag==null||tag.length()<1){
					//////////////////////////防止二次拨号
					bundle.clear();
					return;
				}
				inputSB.delete(0, inputSB.length());
				inputSB.append(callNumber);

				

				if(tag.equals("1")){
					
					XWDataCenter.xwDC.needOpenVideo = true; //////无论如何都允许视频
					XWDataCenter.video_is_open=false;
					XWDataCenter.audio_is_open=true;
					phoneCall();
				}else if(tag.equals("2")){
					XWDataCenter.video_is_open=true;
					XWDataCenter.audio_is_open=true;	

					xwDC.needOpenVideo=true;
					///to_call(inputSB.toString());       


					phoneCall();

					////this.mHandler.sendEmptyMessage(9);				
				}
				else if(tag.equals("3")){        //来电
					XWDataCenter.video_is_open=false;
					XWDataCenter.audio_is_open=true;	

					XWDataCenter.xwDC.needOpenVideo = true;

					phoneCall();
				}


			}
			//////////////////////////防止二次拨号
			bundle.clear();
		}

	}

	@Override
	protected void onPause(){
		super.onPause();
		Log.e(TAG,"FriendCall onPause");

		if (MainApplication.iCallState!=TelephonyManager.CALL_STATE_IDLE)
		{
			if(!xwDC.needOpenVideo){
				TaskExecutor.executeTask(new Runnable() {
					@Override
					public void run() {
						xwDC.hangupNetPhone();
						xwDC.netPhoneTime=0;
					}
				});
			}
		}

	}





	@Override
	protected void onResume(){
		Log.e(TAG,"FriendCall onResume");
		super.onResume();
		resetSpeakerView();
	}

	private void resetSpeakerView(){
		try
		{
			AudioManager am = (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
			boolean	 bIsSpeakerOn=am.isSpeakerphoneOn();
			if (bIsSpeakerOn)
			{
				ivSpeaker.setImageResource(R.drawable.icon_speaker_press);
			}
			else
			{
				ivSpeaker.setImageResource(R.drawable.icon_speaker_nor);
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);        
		//Log.e("--Main--", "onConfigurationChanged");
		if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){            
			//textView.setText("当前屏幕为横屏");
		}else{
			//textView.setText("当前屏幕为竖屏");        
		}
	}

	/**
	 * 进行视频通话
	 */
	private void videoCall(){
		if ((XWDataCenter.iNetphoneStatus!=0)&&(XWDataCenter.iNetphoneStatus!=11))
		{
			XWDataCenter.video_is_open=true;
			xwDC.needOpenVideo=true;		
			if (xwDC.getVAideoStatus(1)==1) 
				this.mHandler.sendEmptyMessage(9);
		}
		else ////拨号,并启视频
		{
			XWDataCenter.video_is_open=true;
			XWDataCenter.audio_is_open=true;	
			xwDC.needOpenVideo=true;
			phoneCall();

			/////xwDC.needOpenVideo=true;					
			///////this.mHandler.sendEmptyMessage(9);
		}
	}

	@Override
	public void onClick(View v) {
          if(v==this.img_back){
				backButtonDown(true);
			} else if(v==img_call_handup){//挂断
			  /////未接电话，主动挂断
			  XWDataCenter.xwDC.myHungup =1;
			backButtonDown(true);
		}else if(v==ivSpeaker){
						AudioManager am = (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
						try
						{
									am.setSpeakerphoneOn(!am.isSpeakerphoneOn());
									resetSpeakerView();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
		}

		((Vibrator)getSystemService(Service.VIBRATOR_SERVICE)).vibrate(new long[]{0,10,0,0},-1);
	}


	/**
	 * 设置静音
	 */
	private void setCallQuiet(boolean quiet){


		String res_quiet=getString(R.string.tx_quiet);
		String res_hasQ=getString(R.string.tx_has_quiet);



	}



	/**主拨
	 * @param number
	 */
	public void to_call(final String number){
		if (!com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(this))
		{
			new  AlertDialog.Builder(this)
			.setTitle(XWCodeTrans.doTrans("请插入正确的安全T卡!") )  
			.setIcon(android.R.drawable.ic_dialog_info)  
			.setPositiveButton( XWCodeTrans.doTrans("确定") , 

					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int whichButton){
					dialog.dismiss();
					
					FriendCall.this.finish();
				}
			}
			)  
			.show();  
			
			return;

		}

		if (XWDataCenter.iNetphoneStatus!=0)
			return;

		/////////////////////2017-01-15,控制在刚挂断3秒内不能拨号
		if (System.currentTimeMillis()-XWDataCenter.xwDC.lPhoneIdleTime<=3000) {
			finish();
			return;
		}

		xwDC.calling_loginName="";
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				try {
					xwDC.videoRequest(xwDC.cid,(XWDataCenter.RegularPhoneNumber(number)+"\0").getBytes("GBK"));
					String sAESPassword=XWDataCenter.getFriendAESPassword(XWDataCenter.xwDC.loginName, XWDataCenter.RegularPhoneNumber(number));
					////////////////2014-09-22,对于非好友,或者好友不在线,不使用AES加密!!!!
					if (sAESPassword!=null)
					{
						xwDC.XWNetphoneSetPhoneAESPassword((sAESPassword+"\0").getBytes());
					}
					XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_4); //////打开音频!!!!2014-10-20
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});


	}




	/**
	 * 离开正在通话界面
	 */
	public void leaveFriendCall(){
		xwDC.stopVibrator();
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
		finish();
	}




	@Override
	public void onBackPressed() {
//		backButtonDown(true);  /////参照微信，拨号时不响应返回按键
	}

	/**
	 * 直接拨号
	 */
	public void phoneCall(){
		///////////////默认视频先关闭
		if(xwDC==null){
			return;
		}
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				XWDataCenter.SendCreditMessage(XWDataCenter.RegularPhoneNumber(callNumber),XWDataCenter.CREDIT_REQUEST);
			}
		});


		///////////////////2014-06-30,检测横屏，竖屏!!!!!!!!!!!!
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		///////XWDataCenter.bIsVerticalScreen=   (height>width)  == (XWDataCenter.video_preferred_width>XWDataCenter.video_preferred_height);
		Log.v(TAG,"phoneCall bIsVerticalScreen"+XWDataCenter.bIsVerticalScreen);


		Log.e(TAG,"---->phoneCall");
		///////////if(xwDC.needOpenVideo)
		{

			if(XWDataCenter.video_preferred_width==0){//如果摄像头数据初始失败,则在此再次初始化
				xwDC.initCameraParam();
			}

			if(XWDataCenter.video_compress_width>0){//如果需要压缩,则传压缩后的尺寸
				if (XWDataCenter.bIsVerticalScreen)
				{
					xwDC.setVideoRect(XWDataCenter.video_compress_height,XWDataCenter.video_compress_width ,XWDataCenter.xw_video_fps);
				}
				else
					xwDC.setVideoRect(XWDataCenter.video_compress_width, XWDataCenter.video_compress_height,XWDataCenter.xw_video_fps);
				/////Log.e(TAG,"call compress video width" +XWDataCenter.video_compress_width+ " heigh" +XWDataCenter.video_compress_height+ " fps"+XWDataCenter.xw_video_fps );
			}else{

				/////////////如果竖屏,进行反转!!!!!!!!2014-06-30
				if (XWDataCenter.bIsVerticalScreen)
				{
					xwDC.setVideoRect(XWDataCenter.video_preferred_height, XWDataCenter.video_preferred_width,XWDataCenter.xw_video_fps);
					Log.e(TAG,"call video width" +XWDataCenter.video_preferred_height+ " heigh" +XWDataCenter.video_preferred_width+ " fps"+XWDataCenter.xw_video_fps );
				}
				else
				{
					xwDC.setVideoRect(XWDataCenter.video_preferred_width, XWDataCenter.video_preferred_height,XWDataCenter.xw_video_fps);
					Log.e(TAG,"call video width" +XWDataCenter.video_preferred_width+ " heigh" +XWDataCenter.video_preferred_height+ " fps"+XWDataCenter.xw_video_fps );
				}
			}
		}


		if(!this.xwDC.calling_loginName.equals(""))  {  //来电判断

			if(!xwDC.cameraRunning){
				Log.e(TAG,"acceptNetPhoneReq");
				xwDC.acceptNetPhoneReq();
			
				xwDC.isAudioRunning=true;	

				///////////////根据对方是否有vodie codec
				//////xwDC.needOpenVideo=(xwDC.remote_video_width>0)&&(xwDC.remote_video_height>0);


				///////////////////////////////////////////
				//if(xwDC.needOpenVideo)
				{
					/////////xwDC.startXWAudio();
					Log.e(TAG,"remote video width"+xwDC.remote_video_width+" height"+xwDC.remote_video_height+" videocodec"+xwDC.remote_video_codec+"audio"+xwDC.remote_audio_codec);
					Log.e(TAG,"incall callNumber:"+callNumber);
					Intent nextPage=new Intent();
					nextPage.putExtra("phone_number", callNumber);
					nextPage.setClass(FriendCall.this, FriendVideoDisplay.class);
					startActivity(nextPage);
				}

			}
		}
		else{  ///////////呼出
			if(!xwDC.cameraRunning){
				if(inputSB.toString().equals(xwDC.loginName)){//是自己
//					callStatus.setText(this.getResources().getString(R.string.alert_call_notme));
					showToastTips(getResources().getString(R.string.alert_call_notme));
				}else{
					to_call(inputSB.toString());
				}
			}
		}
	}


	public void closeUI(){
		FriendCall.this.finish();
	}

	/**
	 * 挂断通话
	 */
	public void phoneDown(){
		Log.e(TAG,"---->phoneDown");


		////xwDC.hangupNetPhone();
		XWDataCenter.threadHangupNetPhone();
		
		
		xwDC.netPhoneTime=0;
		Log.e(TAG,"---->phoneDown1");

		xwDC.stopXWAudio();
		Log.e(TAG,"---->phoneDown2");


		xwDC.netPhoneTime=0;
		xwDC.calling_loginName="";    	

		xwDC.isAudioRunning=false;   	

		XWDataCenter.video_is_open=false;
		XWDataCenter.audio_is_open=false;	
		xwDC.needOpenVideo=true;
	}



	/**返回按键事件
	 * @param isIrKeyDown
	 */
	public void backButtonDown(boolean isIrKeyDown){
		Log.e(TAG,"---->backButtonDown: ");
		///////////手机版!!!!!!!!!!!
		{
			TaskExecutor.executeTask(new Runnable() {
				@Override public void run() {
					phoneDown();
					TaskExecutor.runOnUIThread(new Runnable() {
						@Override public void run() {
							leaveFriendCall();
						}
					});
				}
			});


		}

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mHandler!=null){
			mHandler.removeMessages(10);
			mHandler.removeCallbacksAndMessages(null);
			((FriendCallHandle)mHandler).setContext(null);
		}

	}
}
