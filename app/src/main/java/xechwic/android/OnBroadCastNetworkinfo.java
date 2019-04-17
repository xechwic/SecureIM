package xechwic.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBroadCastNetworkinfo extends BroadcastReceiver{
	
	private boolean bIsLastNetworkOn=false;

	@Override
	public void onReceive(Context context, Intent intent) {

		/////////////////////////////通知service进行检查!!!!!!!!!!!!!!
		Log.v("XIM","OnBroadCastNetworkinfo onReceive");
		try
		{
			   Intent intentservice=new Intent(context, XWServices.class);
			   intentservice.setAction("DO_CHECK");
			   context.startService(intentservice);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}

