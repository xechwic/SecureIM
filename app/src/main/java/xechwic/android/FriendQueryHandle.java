package xechwic.android;

import android.os.Handler;
import android.os.Message;

public class FriendQueryHandle extends Handler{
	private FriendQuery fq;
	public FriendQueryHandle(FriendQuery fq){
		this.fq=fq;
	}
	public void handleMessage(Message msg) {
        // TODO Auto-generated method stub
        super.handleMessage(msg);
        FriendNodeInfo fni=null;
        switch (msg.what) {
	        case 1:
	        	fni=(FriendNodeInfo)msg.obj;
	        	if(fq!=null){
	        			fq.addFriendTextView(fni);
	        		
	        	}
	        
	  		    break;
	        case 2:
	            break;
	        default:
	            break;
        }
    }
}
