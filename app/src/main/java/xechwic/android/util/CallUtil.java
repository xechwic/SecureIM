package xechwic.android.util;

import android.app.KeyguardManager;
import android.content.Intent;
import android.text.TextUtils;

import xechwic.android.act.MainApplication;
import xechwic.android.ui.InCallUI;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by luman on 2017/1/11 14:09
 * 电话操作
 */

public class CallUtil {

    /////处理来电
    public static void incomingNetCall(String number){
        if(TextUtils.isEmpty(number)){
            return;
        }
        try
        {
            // 下面的代码用来屏幕解锁
            KeyguardManager keyguardManager = (KeyguardManager) MainApplication.getInstance().getSystemService(KEYGUARD_SERVICE);
            ///////@SuppressWarnings("deprecation")
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
            keyguardLock.disableKeyguard();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        try
        {

            Intent icall = new Intent(MainApplication.getInstance(),InCallUI.class);
            icall.putExtra("phone_number", number);
            icall.putExtra("tag", "3");
            icall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MainApplication.getInstance().startActivity(icall);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
