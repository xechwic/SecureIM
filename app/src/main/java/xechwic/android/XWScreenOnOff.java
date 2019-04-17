package xechwic.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.android.process.RemoteService;

import xechwic.android.act.MainApplication;
import xechwic.android.util.FileUtil;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.PrefsUtils;
import xechwic.android.util.XWDataCenterMessage;


public class XWScreenOnOff extends BroadcastReceiver {
	////private static  boolean bScreenOn;
	public static long lLastScreenOn=System.currentTimeMillis();
    private boolean bThreadRunning=false;

	private static boolean bIsScreenOn=checkScreenOn();

	public static boolean getScreenOn() {
        return bIsScreenOn;
	}

	public static boolean checkScreenOn()
	{
		try
		{
			PowerManager pm = (PowerManager) MainApplication.getInstance().getSystemService(Context.POWER_SERVICE);
			return ((pm != null) && pm.isScreenOn());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
	@Override
	public void onReceive(final Context context, Intent intent) {
		if(intent!=null&&intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
			///bScreenOn=false;
			bIsScreenOn=false;
			PrefsUtils.getInstance().put(JRSConstants.KEY_SCREEN_OFF, System.currentTimeMillis());


				////////黑屏时生成一个1像素的acitity在前端
			/*if(XWDataCenter.xwDC!=null){
				XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_62);
			}*/
				///////检测
			checkLiveActivity();

			////生成通知
			Intent intentservice=new Intent(context, XWServices.class);
			intentservice.setAction(JRSConstants.CMD_ACTION_NOTIFICATION_ON);
			context.startService(intentservice);
		}/*else if(intent!=null&&intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
			Log.e("screen","user present");
		}*/

	    if ( (intent!=null) && (Intent.ACTION_SCREEN_ON.equals(intent.getAction())  ) )
	    {
			bIsScreenOn=true;
			if(XWDataCenter.xwDC!=null) {
				XWDataCenter.xwDC.XWMsghandle.removeMessages(XWDataCenterMessage.MSG_62);
			}
			lLastScreenOn=System.currentTimeMillis();
			/////亮屏清理黑屏时间记录
			PrefsUtils.getInstance().put(JRSConstants.KEY_SCREEN_OFF, 0);

			if(FileUtil.isBlackLiveExist()){/////黑屏状态
				MainApplication.TIME_ALARM=JRSConstants.LONG_TIME;
			}else{
				MainApplication.TIME_ALARM=JRSConstants.SHORT_TIME;
			}


			//////关闭LiveActivity
			Intent localIntent=new Intent(JRSConstants.CMD_ACTION_LOCALBROADCAST);
			localIntent.putExtra(JRSConstants.DATA,KeepLiveActivity.class.getSimpleName());
			context.sendBroadcast(localIntent);
			////取消通知
			Intent intentservice=new Intent(context, RemoteService.class);
			intentservice.setAction(JRSConstants.CMD_ACTION_NOTIFICATION_OFF);
			context.startService(intentservice);
			////更新应用通知
			MainApplication.getInstance().noticeOnlineStatus();


	    }

        /*try
        {
			AlarmManagerUtil.registerAlarm(context);
        }
        catch(Exception ex)
        {
       	    ex.printStackTrace();
        }*/
		try {
		   android.util.Log.e("XIM", "XWScree on off");
		   Intent intentservice=new Intent(context, XWServices.class);
		   intentservice.setAction("DO_CHECK");
		   context.startService(intentservice);
		} catch (Exception e) {
			e.getStackTrace();
		}
	}


	private void checkLiveActivity(){
//		if(XWDataCenter.xwDC!=null){
//			XWDataCenter.xwDC.XWMsghandle.removeMessages(XWDataCenterMessage.MSG_62);
//			XWDataCenter.xwDC.XWMsghandle.sendEmptyMessageDelayed(XWDataCenterMessage.MSG_62,JRSConstants.CHECK_LIVE_TIME);
//		}
		try {
			Intent keepIntent = new Intent(MainApplication.getInstance(), KeepLiveActivity.class);
			keepIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MainApplication.getInstance().startActivity(keepIntent);
		}
		catch(Exception ex)
		{

		}

		/*
		if(!bThreadRunning){
			TaskExecutor.executeTask(new Runnable() {
				@Override
				public void run() {
					bThreadRunning=true;
					try {
						/////////监测
						//////while (!getScreenOn())
						{
							try {
								Thread.sleep(JRSConstants.CHECK_LIVE_TIME);
							} catch (Exception ex) {
							}

							if (!getScreenOn() && (!KeepLiveActivity.bIsFront)) {
								try {
									if (!getScreenOn() && (!KeepLiveActivity.bIsFront)) {
										TaskExecutor.runOnUIThread(new Runnable() {
											@Override
											public void run() {
												if (!getScreenOn() && (!KeepLiveActivity.bIsFront)) {

													try {
														Intent keepIntent = new Intent(MainApplication.getInstance(), KeepLiveActivity.class);
														keepIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
														MainApplication.getInstance().startActivity(keepIntent);
													} catch (Exception ex) {

													}
												}
											}
										});
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}///while
					}
					finally {
						bThreadRunning = false;
					}
				}
			});
		}*/
	}
}
