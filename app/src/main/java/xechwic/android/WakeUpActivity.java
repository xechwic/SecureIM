package xechwic.android;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import xechwic.android.ui.BaseUI;

/**
 * 用于唤醒屏幕的activity
 *
 */
public class WakeUpActivity extends BaseUI {

	 private Handler mHandler=new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState){	
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

		);
		Window window=getWindow();
		window.setGravity(Gravity.LEFT|Gravity.TOP);
		WindowManager.LayoutParams params=window.getAttributes();
		params.x=0;
		params.y=0;
		params.width=1;
		params.height=1;
		window.setAttributes(params);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				finish();
			}
		}, 300);

	}

}
