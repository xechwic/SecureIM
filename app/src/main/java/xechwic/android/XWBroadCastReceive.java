package xechwic.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class XWBroadCastReceive extends BroadcastReceiver{

	private String TAG=XWBroadCastReceive.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
       
		String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);   
		 Log.e(TAG,"onReceive:"+phoneNumber);		
        if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {  
          ///去电
        }else{
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            switch(tm.getCallState()){  
            case TelephonyManager.CALL_STATE_RINGING:  
               ///来电响铃
                break;  
            case TelephonyManager.CALL_STATE_OFFHOOK:
                ////接通电话
            	try{
	            	if(XWDataCenter.xwContext!=null){
	            		if(XWDataCenter.xwContext instanceof FriendVideoDisplay){
	            			if(((FriendVideoDisplay)XWDataCenter.xwContext).xwDC.cameraRunning&&((FriendVideoDisplay)XWDataCenter.xwContext).xw_has_prepared&&((FriendVideoDisplay)XWDataCenter.xwContext).xwDC.remoteVideoRunning){
	            				((FriendVideoDisplay)XWDataCenter.xwContext).stopVideo();
	            			}
	    				}
	            	}
            	}catch(Exception e){
            		e.printStackTrace();
            	}
                break;  
            default:  
                break;  
            }  
        }  
	}
}

