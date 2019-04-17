/*
 * Copyright (C) 2015 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of Meizhi
 *
 * Meizhi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Meizhi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Meizhi.  If not, see <http://www.gnu.org/licenses/>.
 */

package xechwic.android.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

import xechwic.android.XWScreenOnOff;
import xechwic.android.XWServices;
import xechwic.android.act.MainApplication;

import static xechwic.android.act.MainApplication.TIME_ALARM;

/**
 * 闹钟管理
 */
public class AlarmManagerUtil {

    private static final int ALARMER_ID=2014101118;

    ///////////////////2014-08-29,解决定时器可能退出的问题.
     public static void  registerAlarm(Context context) {
         if (XWScreenOnOff.getScreenOn()) //////亮屏10秒
         {
             if (MainApplication.getInstance().bIsXIMConnectied)
                 TIME_ALARM=20000;
             else
                 TIME_ALARM=5000;
         }
         else  //////////////
         {
             /////关屏下40秒一次
             /////if (XWDataCenter.xwDC.XIMGetConnectStatusToXIM()!=1) //////关屏未连接
             {
                 TIME_ALARM=40000;
             }
             /*else  /////////3分钟一次
             {
                 TIME_ALARM=180000;
             }*/
         }

        Log.e("XIM","registerAlarm:"+TIME_ALARM);
        try
        {
            AlarmManager alarmMgr = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);


            /*Intent intent = new Intent("xechwic.android.ALARM_CLOCK");
            intent.setClass(context, XWAlarmer.class);   ////指定具体接受器
            PendingIntent pendIntent = PendingIntent.getBroadcast(context, ALARMER_ID,
                    intent,PendingIntent.FLAG_UPDATE_CURRENT);*/
            Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
            intentservice.setAction("DO_CHECK");
            PendingIntent pendingIntent = PendingIntent.getService(MainApplication.getInstance(), ALARMER_ID, intentservice, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.MILLISECOND, (int)TIME_ALARM);

            //////////////高版本23安卓,在黑屏下使用setAndAllowWhileIdle
            if (  (!XWScreenOnOff.getScreenOn()) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) ) {
                ///alarmMgr.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent), pendingIntent);
                ///alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                alarmMgr.setWindow(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),TIME_ALARM/2, pendingIntent);
            }
            /*else if ( (!XWScreenOnOff.getScreenOn()) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)){
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }*/
            else {
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public static void cancelAlarm(Context context) {

        Log.v("XIM","cancelAlarm");

        try
        {
            AlarmManager alarmMgr = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            /*Intent intent = new Intent("xechwic.android.ALARM_CLOCK");
            intent.setClass(context, XWAlarmer.class);   ////指定具体接受器
            PendingIntent pendIntent = PendingIntent.getBroadcast(context, ALARMER_ID,
                    intent,PendingIntent.FLAG_UPDATE_CURRENT);*/
            Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
            intentservice.setAction("DO_CHECK");
            PendingIntent pendingIntent = PendingIntent.getService(MainApplication.getInstance(), ALARMER_ID, intentservice, PendingIntent.FLAG_UPDATE_CURRENT);

            // 先取消
            alarmMgr.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

}
