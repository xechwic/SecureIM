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

package xechwic.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import xechwic.android.util.AlarmManagerUtil;
import xechwic.android.util.FileUtil;

/**
 * 监听屏幕解锁
 */
public class KeepAlarmLiveReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            if(FileUtil.isGuardFileExist()) {
                Log.e("KeepAlarmLiveReceiver","KeepAlarmLiveReceiver ACTION_USER_PRESENT isGuardFileExist registerAlarm");
                AlarmManagerUtil.registerAlarm(context);
            }else{
                Log.e("KeepAlarmLiveReceiver","KeepAlarmLiveReceiver ACTION_USER_PRESENT isGuardFileExist false");
            }
        }
    }
}
