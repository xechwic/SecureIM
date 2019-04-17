package xechwic.android;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import ydx.securephone.R;

public class FriendLoginHandle extends Handler{
	private FriendLogin fl;
	public FriendLoginHandle(FriendLogin fl){
		this.fl=fl;
	}
	@Override
	public void handleMessage(Message msg) {
		
		Log.e("FriendLoginHandle","handleMessage:"+msg.what);
        super.handleMessage(msg);
		if(fl==null||fl.bIsDestroy){
			return;
		}
        switch (msg.what) {
	        case 1:
	        	fl.xwDC.loginName=fl.numinput.getText().toString().trim();
	        	fl.xwDC.password=fl.passEdit.getText().toString().trim();
				//////保存账户
				/////fl.saveAccountPasswd(fl.xwDC.loginName,fl.xwDC.password,true);
                fl.startHomeAct();
	  		    break;
	        case 2:
        fl.enableLoginView(true);
				fl.loginBtn.setText(fl.getResources().getString(R.string.alert_login_failed));
				fl.xwDC.isLogin=false;

				if(msg.arg1==-1){
					Toast.makeText(fl, fl.getResources().getString(R.string.alert_login_param_error), Toast.LENGTH_LONG).show();
				}else if(msg.arg1==-2){
					Toast.makeText(fl, fl.getResources().getString(R.string.alert_login_net_error), Toast.LENGTH_LONG).show();
				}else if(msg.arg1==-3){
					Toast.makeText(fl, fl.getResources().getString(R.string.alert_login_user_error), Toast.LENGTH_LONG).show();
				}else if(msg.arg1==-4){
					Toast.makeText(fl, fl.getResources().getString(R.string.alert_login_passwd_error), Toast.LENGTH_LONG).show();
				}
	            break;
	        case 3://系统初始化时失败,必需先获取摄像头
	        	break;
	        case 4:
	        	fl.finish();
	        	break;
	        case 5:  //////// auto login
	        {
	        	android.util.Log.v("XIM","Auto login .");
	            if ( XWDataCenter.getAutoLogin() && (!"".equals(fl.numinput.getText())) &&(!"".equals(fl.passEdit.getText())))
	            {
	            	fl.onClick(fl.loginBtn);
	            }
	        }
	        	
	        	break;
					case 6:////判断是否已经登录
						Log.e("login","check:xim=="+XWDataCenter.xwDC.XIMGetConnectStatusToXIM());

						XWDataCenter.xwDC.reActive(1);
						if (XWDataCenter.xwDC!=null&&XWDataCenter.xwDC.XIMGetConnectStatusToXIM()==1){
							fl.startHomeAct();
						}else{
							this.sendEmptyMessageDelayed(6,1000);
						}
						break;
	        default:
	            break;
        }
    }
}
