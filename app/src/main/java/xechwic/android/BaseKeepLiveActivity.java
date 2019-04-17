package xechwic.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import xechwic.android.util.JRSConstants;

/**
 1个像素的activity
 *
 */
public class BaseKeepLiveActivity extends AppCompatActivity {

	protected IntentFilter intentFilter;
	protected LocalReceiver localReceiver;
    protected String TAG="keeplive";
	protected Context mContext;
	static public  boolean bIsFront=false;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		TAG=getClass().getSimpleName();
		Log.e(TAG,"onCreate");
		if (savedInstanceState != null
				&& savedInstanceState.getInt(JRSConstants.SAVE_STATE) != 0) {
			finish();
		}
		mContext=this;

		intentFilter = new IntentFilter();
		intentFilter.addAction(JRSConstants.CMD_ACTION_LOCALBROADCAST);
		localReceiver = new LocalReceiver();
		registerReceiver(localReceiver, intentFilter);
		handleIntent(getIntent());
	}



	@Override
	protected void onResume() {
		super.onResume();
		/////开屏状态,退出
		if (XWScreenOnOff.getScreenOn())
		{
			bIsFront=false;
			finish();
			return;
		}
		bIsFront=true;
		Log.e(TAG,"onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		bIsFront=false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.e(TAG,"onNewIntent");
		handleIntent(intent);
	}

	protected void handleIntent(Intent intent){
		if(intent!=null&&JRSConstants.CMD_ACTION_FINISH.equals(intent.getAction())){
			Log.e(TAG,"onNewIntent finish");
			finish();
			setIntent(null);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(JRSConstants.SAVE_STATE, 1);
		Log.e(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG,"onDestroy");
		unregisterReceiver(localReceiver);
	}

	public class LocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(TAG,"received local broadcast");
			if(intent!=null&&JRSConstants.CMD_ACTION_LOCALBROADCAST.equals(intent.getAction())){
				Bundle data=intent.getExtras();
				if(data!=null){
					String actName=data.getString(JRSConstants.DATA);
					String className=mContext.getClass().getSimpleName();
					Log.e(TAG,"broadcast actName:"+actName+",className:"+className);
					if(!TextUtils.isEmpty(actName)){
						if(actName.equals(className)){
							finish();
						}
					}
				}
			}
		}
	}
}
