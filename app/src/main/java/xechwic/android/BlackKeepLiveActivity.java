package xechwic.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import ydx.securephone.R;
import xechwic.android.util.FileUtil;

/**
 黑背景activity
 *
 */
public class BlackKeepLiveActivity extends BaseKeepLiveActivity {




	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		);
        setContentView(R.layout.activity_blackkeeplive);
		FileUtil.createBlackLiveFile();

	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG,"onPause");
		/////////此界面只能在前端显示，否则关闭,黑屏亮起时会反复调用，需要延时处理
		mhandler.sendEmptyMessageDelayed(1,500);
	}

	@Override
	public void finish() {
		FileUtil.deleteBlackLiveFile();//不等destroy，先删除
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FileUtil.deleteBlackLiveFile();
	}

	Handler mhandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(!bIsFront){
				finish();
			}
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN){
			///////触屏关闭
			Log.e(TAG,"onTouchEvent");
			finish();
		}
		return super.onTouchEvent(event);
	}
}
