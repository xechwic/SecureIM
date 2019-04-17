package xechwic.android.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import xechwic.android.bean.ContactBean;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class ContactInfoService {

	private static final String TAG = "ConcactInfoService";
	private static ContactInfoService instance;
	Context context;

	public static final int COL_ID = 0;
	public static final int COL_NAME = 1;
	public static final int COL_HAS_PHONE = 2;  
	  
	final String[] selectCol = new String[]{  
			  ContactsContract.Contacts._ID,  
	        ContactsContract.Contacts.DISPLAY_NAME,  
	        ContactsContract.Contacts.HAS_PHONE_NUMBER 
	      
	    };
	final String[] selPhoneCols = new String[] {
			ContactsContract.CommonDataKinds.Phone._ID,
			ContactsContract.CommonDataKinds.Phone.NUMBER,
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Phone.TYPE,
			ContactsContract.CommonDataKinds.Phone.LABEL,
			ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY };
	public ContactInfoService(Context context) {
		this.context = context;
	}

	public static ContactInfoService getInstance(Context context) {
		if (null == instance) {
			instance = new ContactInfoService(context);
		}
		return instance;
	}


	/**
	 * 根据号码获取联系人姓名
	 * 
	 * @param name
	 * @return
	 */
	public String getContactNameFromPhoneBook(String phoneNum) {  
	    String contactName = "";  
	    
	    try
	    {
		    ContentResolver cr = context.getContentResolver();  
		    
		    ////////////////2012-01-04,XechWic,solve the null exception
		    if (cr==null)
		    	return "";
		    ///Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
		    ///        null, null, null);
		    ////if (cur!=null)
		    {
		    //if (Integer.parseInt(cur.getString(
		    ///		cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
		    {
		    	
			    Cursor pCur = cr.query(  
			            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,  
			            ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",  
			            new String[] { phoneNum }, null);  
			    if (pCur!=null)
			    {
			    	try
			    	{
				    if (pCur.moveToFirst()) {  
				        contactName = pCur  
				                .getString(pCur  
				                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));  
				      
				    } 
			    	}
			    	catch(Exception e)
			    	{
			    		e.printStackTrace();
			    	}
                    pCur.close();  
			    }
		
			    
		    }
		    
	    	///cur.close();
		    
		    }
		    
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    return contactName;  
	}  
	
	/**
	 * 获取全部联系人电话
	 * 
	 * @return
	 */
	public ArrayList<ContactBean> getContact() {
		ArrayList<ContactBean> peoples = null;
		final  String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("  
	       + Contacts.HAS_PHONE_NUMBER + "=1) AND ("  
	       + Contacts.DISPLAY_NAME + " != '' ))"; 
		Cursor cur = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				selectCol,
				select,
				null,
				ContactsContract.Contacts.DISPLAY_NAME
						+ " COLLATE LOCALIZED ASC");
		if(null == cur ){
			return peoples;
		}
		try
		{
		peoples = new ArrayList<ContactBean>();
		if (cur.moveToFirst()) {
			do {
				ContactBean cb = new ContactBean();
				// 联系人的ID
				String contactId = cur.getString(COL_ID);
				String name = cur.getString(COL_NAME);
				// 根据姓名 查看有多少个联系号码
				int numberCount = cur.getInt(COL_HAS_PHONE);
				if (numberCount > 0) {
					
					Cursor phone = context.getContentResolver().query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							selPhoneCols,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ "=" + contactId, null, null);
					try
					{
					if (null != phone && phone.moveToFirst()) {
						do {
							String phoneNumber = phone
									.getString(phone
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
							short phoneType = phone
									.getShort(phone
											.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
							if (phoneType == 1 ) {
								cb.setContactHomePhone(phoneNumber);
							} else {
								cb.setContactPhone(phoneNumber);
							}
							if(!name.equals(phoneNumber)){
								cb.setContactName(name);
							}
						} while (phone.moveToNext());
					}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					if (null != phone)
						phone.close();
				}
				peoples.add(cb);
			} while (cur.moveToNext());
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		cur.close();

		return peoples;
	}
}
