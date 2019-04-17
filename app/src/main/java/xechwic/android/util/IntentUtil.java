package xechwic.android.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;

public class IntentUtil {

	private static IntentUtil instance;
	private Intent intent ;
	public static IntentUtil getInstance(){
		if(null == instance){
			instance = new IntentUtil();
		}
		return instance;
	}
	public void callPhoneActivity(Context context,Uri data){
		intent = new Intent();
		intent.setAction(Intent.ACTION_CALL);
		intent.setData(data);
		context.startActivity(intent);
	}
	
	
	@SuppressWarnings("deprecation")
	public void startEditContactActivity(Context context,String name,String number){
		Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(People.CONTENT_ITEM_TYPE);
        intent.putExtra(Contacts.Intents.Insert.NAME, "My Name");
        intent.putExtra(Contacts.Intents.Insert.PHONE, "+1234567890");
        intent.putExtra(Contacts.Intents.Insert.PHONE_TYPE,Contacts.PhonesColumns.TYPE_MOBILE);
        intent.putExtra(Contacts.Intents.Insert.EMAIL, "com@com.com");
        intent.putExtra(Contacts.Intents.Insert.EMAIL_TYPE,                    Contacts.ContactMethodsColumns.TYPE_WORK);
        context.startActivity(intent);
//		context.startActivity(it); 
	}
	
	public void sendSMSActivity(Context context,Uri data){
		Intent it = new Intent(Intent.ACTION_SENDTO,data); 
//		it.putExtra("sms_body", "TheSMS text");
//		it.setType("vnd.android-dir/mms-sms"); 
		context.startActivity(it); 
	}
	
	public void send2SMSActivity(Context context,Uri data){
		Intent it = new Intent(Intent.ACTION_VIEW,data); 
		it.putExtra("sms_body", "TheSMS text");
		it.setType("vnd.android-dir/mms-sms"); 
		context.startActivity(it); 
	}
}
