package xechwic.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import xechwic.android.act.MainApplication;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.PrefsUtils;


public class XWAlarmer extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.e("XIM", "XWAlarmer onReceive");

        ////XWDataCenter.xwDC.startVibrator();//振动
        ///////亮屏业务,弹出黑色背景界面
        if(PrefsUtils.getInstance().get(JRSConstants.KEY_SCREEN_SWITCH,false)){
            long lastTime= PrefsUtils.getInstance().get(JRSConstants.KEY_SCREEN_OFF,0);
            if(lastTime!=0) {
                if ((System.currentTimeMillis() - lastTime) > (JRSConstants.BLACK_SCREEN_TIME)) {////黑屏下5分钟
                    PowerManager pm = (PowerManager) MainApplication.getInstance().getSystemService(Context.POWER_SERVICE);
                    if(!pm.isScreenOn()) {
                        /////修改AlARM时间
                        XWDataCenter.xwDC.bWakeUpBlackScreen =true;//主动亮屏
                        MainApplication.TIME_ALARM=JRSConstants.LONG_TIME;
                        /////启动黑色界面
                        Intent keepIntent = new Intent(MainApplication.getInstance(), BlackKeepLiveActivity.class);
                        keepIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainApplication.getInstance().startActivity(keepIntent);
                    }
                    ////马上修改时间
                    PrefsUtils.getInstance().put(JRSConstants.KEY_SCREEN_OFF,System.currentTimeMillis());
                }
            }
        }

        try {
            {

                /*try
                {
                    AlarmManagerUtil.registerAlarm(context);
                }
                catch(Exception ex)
                {
                     ex.printStackTrace();
                }*/
            }
            Intent intentservice=new Intent(context, XWServices.class);
            intentservice.setAction("DO_CHECK");
            context.startService(intentservice);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }



}
