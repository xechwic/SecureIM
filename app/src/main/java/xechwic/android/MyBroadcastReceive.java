package xechwic.android;

/**
 * Created by luman on 2017/1/21 15:57
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import xechwic.android.act.MainApplication;
import xechwic.android.util.JRSConstants;

/**
 * 开机启动广播
 */
public class MyBroadcastReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("MyBroadcastReceive",intent.getAction());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            ///////置启动标志
            XWDataCenter.xwDC.isLogin=true;
            ///context.startService(new Intent(context,XWServices.class));
            Intent it=new Intent(MainApplication.getInstance(),XWServices.class);
            intent.setAction(JRSConstants.CMD_ACTION_AUTOLOGIN);
            intent.putExtra(JRSConstants.KEY_USER_ACCOUNT,"");
            intent.putExtra(JRSConstants.KEY_USER_PASSWORD,"");
            MainApplication.getInstance().startService(intent);
        }
        else
        //////if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE"))
        {
            Intent intentservice=new Intent(context, XWServices.class);
            intentservice.setAction("DO_CHECK");
            context.startService(intentservice);
        }
    }
}