package xechwic.android;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import ydx.securephone.R;

public class FriendCallHandle extends Handler {
	private String TAG=FriendCallHandle.class.getSimpleName();
	private FriendCall fc;

//	RecordBean mRecordBean;

	public FriendCallHandle(FriendCall fc) {
		this.fc = fc;
	}

	public void setContext(FriendCall fc){
		this.fc = fc;
	}
/**
 * 6,显示时间、余额
 */
     @Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if(fc==null||fc.bIsDestroy){
			return;////界面已经关闭不处理
		}

		Intent nextPage;
		int h, m, s;

		Log.e("FriendCallHandle: what:", msg.what
				+ "  msg.arg1:"+msg.arg1+",iNetphoneStatus:"+XWDataCenter.iNetphoneStatus);
		switch (msg.what) {
		case 1:
			String alertStr = null;
			switch (msg.arg1) {
			case 1:
				alertStr = fc.getResources().getString(
						R.string.alert_communication_error);
				break;
			case 2:
				alertStr = fc.getResources()
				.getString(R.string.alert_that_busy);
				break;
			case 3:
				alertStr = fc.getResources().getString(
						R.string.alert_that_reject);
				break;
			case 4:
				alertStr = fc.getResources().getString(
						R.string.alert_number_error);
				break;
			case 5:
				alertStr = fc.getResources().getString(
						R.string.alert_account_error);
				break;
			case 6:
				alertStr = fc.getResources().getString(
						R.string.alert_that_hungup);
				break;
			default:
				break;
			}
			if(fc!=null){
//				fc.callStatus.setText(alertStr);
				Log.w("FriendCallHandle", "case 1 ================>" + alertStr);
			}
		
			break;
		case 2:                                           /////我方开始拨号，正在拨号，对方挂断
			if(fc!=null){
				String text = (String) msg.obj;
				Log.e(XWDataCenter.iNetphoneStatus + "   <<<FriendCallHandle",
						"case 2 ================>" + text);
				if(!TextUtils.isEmpty(text)){
					fc.setCallViewStatus(text);
					if(text.contains(fc.getResources().getString(R.string.alert_that_hungup))){
						//挂断关闭界面
						fc.showToastTips(fc.getResources().getString(R.string.alert_that_hungup));
						fc.backButtonDown(true);
					}else if(text.contains(fc.getResources().getString(R.string.alert_status_begin))){
						////开始拨号
					}else if(text.contains(fc.getResources().getString(R.string.alert_status_begin))){
						////正在拨号
					}
				}


				//来电接听
//				fc.phoneCall();
			}
		
		
			break;
		case 3:// 拨通

			// ///////////////////////////////////
			// ////////////////在这里要根据对方的videocodec来决定是否进入视频对话界面!!!!!!!!!!!
			// /////////////根据对方是否有codec
			Log.e("XIM", "remote video width" + fc.xwDC.remote_video_width
					+ " height" + fc.xwDC.remote_video_height + " codec"
					+ fc.xwDC.remote_video_codec);
			// /////////////根据对方是否有vodie codec
			/*fc.xwDC.needOpenVideo = (fc.xwDC.remote_video_width > 0)
					&& (fc.xwDC.remote_video_height > 0);*/
			///////if (fc.xwDC.needOpenVideo) 
			{
				Log.e(TAG,"needOpenVideo");
				nextPage = new Intent();
				nextPage.putExtra("phone_number", fc.callNumber);
				nextPage.setClass(fc, FriendVideoDisplay.class);
				fc.startActivity(nextPage);
				fc.closeUI();
			} 
			/*else {
				if (XWDataCenter.EditionType > 0) {// 非模拟器版本
					fc.callStatus.setText(fc.getResources().getString(
							R.string.alert_status_incalling));
//					try {
//						fc.seekLayout.removeAllViews();
//						fc.seekLayout.addView(fc.fad);
//						//显示
//						fc.seekLayout.setVisibility(View.VISIBLE);
//					} catch (Exception e) {
//					}
					fc.xwDC.startXWAudio();// 音频服务线程启动

					if (!fc.xwDC.isAudioRunning) {
						fc.xwDC.isAudioRunning = true;
						fc.xwDC.displayVideoTime();
					}
				}
			}*/
			break;
		case 4:// 连接断开
//			fc.phoneDown();// finish();
			fc.backButtonDown(true);//挂断并退出

			break;
		case 5:
//			fc.callStatus.setText((String) msg.obj);
			FriendCall.inputSB.delete(0, FriendCall.inputSB.length());
			FriendCall.inputSB.append(this.fc.xwDC.calling_loginName);
//			fc.numCall.setText(FriendCall.inputSB.toString());
			break;
		case 6:
			fc.xwDC.timeSB.delete(0, fc.xwDC.timeSB.length());
			h = (int) (fc.xwDC.netPhoneTime / 3600);
			if (h < 10) {
				fc.xwDC.timeSB.append("0");
			}
			fc.xwDC.timeSB.append(h);
			fc.xwDC.timeSB.append(":");
			m = (int) ((fc.xwDC.netPhoneTime % 3600) / 60);
			if (m < 10) {
				fc.xwDC.timeSB.append("0");
			}
			fc.xwDC.timeSB.append(m);
			fc.xwDC.timeSB.append(":");
			s = (int) (fc.xwDC.netPhoneTime % 60);
			if (s < 10) {
				fc.xwDC.timeSB.append("0");
			}
			fc.xwDC.timeSB.append(s);
//			fc.timeAlertView.setText(fc.xwDC.timeSB);
			fc.xwDC.accountSB.delete(0, fc.xwDC.accountSB.length());
			fc.xwDC.accountSB.append(fc.xwDC.fformat
					.format((float) fc.xwDC.currentAccountValue / 100));
			//显示余额
//			fc.accountView.setText(fc.balancePre+fc.xwDC.accountSB);
			break;
		case 7:
//			fc.timeLayout.setVisibility(View.VISIBLE);
			break;
		case 8:
			Toast.makeText(fc,
					fc.getResources().getString(R.string.menu_update_success),
					Toast.LENGTH_SHORT).show();// 修改成功
			// /fc.xwDC.logoutService(0);
			Intent reLogin = new Intent();
			reLogin.setClass(fc, FriendLogin.class);
			fc.startActivity(reLogin);
			// fc.finish();
			break;

		case 9:
			// ///Toast.makeText(fc,
			// fc.getResources().getString(R.string.menu_update_success),
			// Toast.LENGTH_SHORT).show();//修改成功
			// /fc.xwDC.logoutService(0);
			Log.e("xim", "enter video mode");

			XWDataCenter.xwDC.needOpenVideo = true;
			if ( (XWDataCenter.iNetphoneStatus==3) || (XWDataCenter.iNetphoneStatus==12) ) 
			{
				Intent videowin = new Intent();
				videowin.setClass(fc, FriendVideoDisplay.class);
				fc.startActivity(videowin);
			}
			// fc.finish();
			break;
			
		case 10:
			if ((XWDataCenter.iNetphoneStatus==0) || (XWDataCenter.iNetphoneStatus==1))
			{
				fc.backButtonDown(true);//挂断并退出
			}
			
			break;
		default:
			break;
		}
	}
}
