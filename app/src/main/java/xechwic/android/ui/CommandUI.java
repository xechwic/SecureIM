package xechwic.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import xechwic.android.FriendControl;
import xechwic.android.FriendLogin;
import xechwic.android.FriendVideoDisplay;
import xechwic.android.util.JRSConstants;


/**
 * 命令中转界面
 *
 */
public class CommandUI extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(this)){
			handleIntent(getIntent());
		}else{
			/////SD卡检测不通过需要重新初始化
			finish();
			startActivity(new Intent(this,FriendLogin.class));
		}

	}

	private void handleIntent(Intent intent){
		if(intent!=null){
			Bundle data=intent.getExtras();
			if(data!=null&&data.containsKey(JRSConstants.DATA)){
				int type=data.getInt(JRSConstants.DATA);
				if(type==JRSConstants.NOTICE_VIDEO_DISPLAY){
					intent.setClass(this,FriendVideoDisplay.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
				}else{
					intent.setClass(this,FriendControl.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
				}

			}
		}
        CommandUI.this.finish();
	}
	
}
