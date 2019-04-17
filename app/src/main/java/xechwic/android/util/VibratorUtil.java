package xechwic.android.util;

import android.content.Context;
import android.os.Vibrator;

/**
 * Created by luman on 2017/1/19 15:02
 * 震动器
 */

public class VibratorUtil {

    /////短震动一下
    public static void vibratorOnce(Context context){
        Vibrator  vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0,10,100,50},-1);
    }
}
