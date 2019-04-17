package xechwic.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import xechwic.android.XWDataCenter;
import xechwic.android.XWNetPhone;
import xechwic.android.act.MainApplication;
import xechwic.android.util.JRSConstants;

/**
 * 第三方启动
 *
 */
public class ThirdStartUpUI extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


			handleIntent(getIntent());


	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent != null)
		{
			Uri uri = intent.getData();
			if (uri != null) {
				String ip = uri.getQueryParameter("ip");
				String account = uri.getQueryParameter("account");
				String passwd = uri.getQueryParameter("passwd");
				if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(account) || TextUtils.isEmpty(passwd)) {
					return;
				}
				Log.e("ThirdStartUpUI", "ip:" + ip + ",account:" + account + ",passwd:" + passwd);

				///////////////进行登录
				XWDataCenter.xwDC.isLogin=true;
				Intent i=new Intent(ThirdStartUpUI.this, XWNetPhone.class);
				i.setAction(JRSConstants.CMD_ACTION_AUTOLOGIN);
				i.putExtra(JRSConstants.KEY_USER_ACCOUNT,account);
				i.putExtra(JRSConstants.KEY_USER_PASSWORD,passwd);
				i.putExtra(JRSConstants.KEY_XIM_IP,ip);
				startActivity(i);
			}
			setIntent(null);
			}

		//}
		ThirdStartUpUI.this.finish();
	}
		/////登录成功保存账户
	public void saveAccountPasswd(String account,String passwd,boolean autoLogin){
		{
			SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);


			SharedPreferences.Editor editor = settings.edit();
			{//只有第一次安装才创建icon

				editor.putBoolean("AUTO_LOGIN", autoLogin);

				editor.putString("LOGIN_USER",account);

				///////////////加密保存的口令
				try
				{
					editor.putString("LOGIN_PASS",  new String ( com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_userpassword(
							account ,passwd.getBytes("iso-8859-1"))  ,"iso-8859-1") );
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				editor.commit();
			}
		}
	}
}
