package xechwic.android;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class FriendVideoDisplayHandle extends Handler{
	private FriendVideoDisplay fvd;
	public FriendVideoDisplayHandle(FriendVideoDisplay fvd){
		this.fvd=fvd;
	}
	public void setContext(FriendVideoDisplay fvd){
		this.fvd=fvd;
	}

	@Override
	public void handleMessage(Message msg) {
        super.handleMessage(msg);
		if(fvd==null||fvd.bIsDestroy){
			return;////界面已经关闭不处理
		}
        byte []data;

        switch (msg.what) {
	        case 5:
//	        	Toast.makeText(fvd, xechwic.android.XWCodeTrans.doTrans("对方已挂断"), Toast.LENGTH_LONG).show();
	            if(XWDataCenter.xwContext!=null&&XWDataCenter.xwContext.bIsFront){
					fvd.showToastTips(xechwic.android.XWCodeTrans.doTrans("对方已挂断"));
				}
	        	fvd.stopVideo();
	  		    break;
	        case 13:
//	        	Toast.makeText(fvd, xechwic.android.XWCodeTrans.doTrans("对方已挂断"), Toast.LENGTH_LONG).show();
				if(XWDataCenter.xwContext!=null&&XWDataCenter.xwContext.bIsFront){
					fvd.showToastTips(xechwic.android.XWCodeTrans.doTrans("对方已挂断"));
				}
	        	fvd.stopVideo();
	            break;
	        case 20:
	        	data=(byte [])msg.obj;
	        	fvd.drawRemoteData(data,msg.arg1,msg.arg2);
	        	break;
	        case 21:
	        	fvd.startVideo();
	        	break;
	        case 22:
	        	fvd.stopVideo();
	        	break;
	        case 23:
	        	Toast.makeText(fvd,xechwic.android.XWCodeTrans.doTrans("对不起,打开视频失败") , Toast.LENGTH_LONG).show();
				break;
	        case 24://时间提示
                showTimeView();

	        	break;
	        case 25:
	        	////data=(byte [])msg.obj;
	        	/////if(data!=null)
	        	{ 
	        		try
	        		{
	        		    XWDataCenter.xwDC.NetPhoneClientDataLock();
	        		    fvd.drawRemoteData(XWDataCenter.xwDC.videoDataBuffer.array(),XWDataCenter.xwDC.ivideoPicLen);
	        		}
	        		catch(Exception ex)
	        		{
	        			ex.printStackTrace();
	        		}
	        		finally
	        		{
	        		    XWDataCenter.xwDC.NetPhoneClientDataUnlock();
	        		}
	        	}
	        	break;
	        default:
	            break;
        }
    }


	public void showTimeView( ){
		fvd.xwDC.timeSB.delete(0, fvd.xwDC.timeSB.length());
		int h=(int)(fvd.xwDC.netPhoneTime/3600);
		if(h<10){
			fvd.xwDC.timeSB.append("0");
		}
		fvd.xwDC.timeSB.append(h);
		fvd.xwDC.timeSB.append(":");
		int m=(int)((fvd.xwDC.netPhoneTime%3600)/60);
		if(m<10){
			fvd.xwDC.timeSB.append("0");
		}
		fvd.xwDC.timeSB.append(m);
		fvd.xwDC.timeSB.append(":");
		int s=(int)(fvd.xwDC.netPhoneTime%60);
		if(s<10){
			fvd.xwDC.timeSB.append("0");
		}
		fvd.xwDC.timeSB.append(s);
		fvd.timeAlertView.setText(fvd.xwDC.timeSB);
		fvd.xwDC.accountSB.delete(0, fvd.xwDC.accountSB.length());
		fvd.xwDC.accountSB.append(fvd.xwDC.fformat.format((float)fvd.xwDC.currentAccountValue/100));
//		fvd.accountView.setText(fvd.xwDC.accountSB);

		/////////////////显示用户号!!!!!!!!!!!!!!
		fvd.friend_video_name.setText(fvd.xwDC.sCurrentPhoneNumber);
	}
}
