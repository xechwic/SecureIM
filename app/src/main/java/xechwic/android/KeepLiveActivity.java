package xechwic.android;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

/**
 1个像素的activity
 *
 */
public class KeepLiveActivity extends BaseKeepLiveActivity {



	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}

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
