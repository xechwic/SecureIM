package xechwic.android.util;

/**
 * Created by luman on 2017/1/3 17:05
 * 时间工具类
 */

public class DateTimeUtil {

    /**
     * 将秒转化成00:00的格式字符串
     *
     * @param seconds
     * @return
     */
    public static String secondsToString(int seconds) {
        String s = "" + seconds / 60 + ":";
        int t = seconds % 60;
        s += t < 10 ? "0" + t : t;
        return s;
    }
}
