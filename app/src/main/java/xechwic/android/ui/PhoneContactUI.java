package xechwic.android.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ydx.securephone.R;
import xechwic.android.FriendQuery;
import xechwic.android.XWCodeTrans;
import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.adapter.PhoneContactUIAdapter;
import xechwic.android.bean.ContactBean;
import xechwic.android.sqlite.ContactDB;
import xechwic.android.util.ComparatorContact;
import xechwic.android.util.NumberUtil;


/**
 * 手机联系人界面
 *
 */
public class PhoneContactUI extends BaseUI {

	private static String TAG=PhoneContactUI.class.getSimpleName();



    
	
	private ListView contactListView;//listView

	private PhoneContactUIAdapter adapter;//适配器

	ProgressDialog progressDg;//进度圈
	private EditText etNumber;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_phonecontact);

		//初始控件
		initView();
		initDataTask();
	}
	
	@Override 
	protected void onResume() {
		super.onResume();

	}

	/**
	 * 初始控件
	 */
	private void initView(){
		etNumber=(EditText)findViewById(R.id.et_number);
		TextView tvAdd=(TextView)findViewById(R.id.tv_add);
		tvAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String number=etNumber.getText().toString().trim();
				if(!TextUtils.isEmpty(number)){
					ContactBean bean=new ContactBean();
					bean.setContactPhone(number);
					addAFriend(bean);
				}

			}
		});


		contactListView=(ListView)findViewById(R.id.contact_list);
		contactListView.setSelector(R.drawable.transblock);
		contactListView.setDivider(getResources().getDrawable(R.color.linecolor));
		contactListView.setDividerHeight(1);
		contactListView.setCacheColorHint(0x00000000);
		contactListView.setFadingEdgeLength(0);


		findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				PhoneContactUI.this.finish();
			}
		});
		TextView tvTitle=(TextView)findViewById(R.id.tv_title);
		tvTitle.setText(getResources().getString(R.string.contact_title));
		//初始列表
		initListView();
	}


	@Override
	protected void onPause() {
		super.onPause();
		if(progressDg!=null){
			progressDg.dismiss();
			progressDg=null;
		}
	}

	/**
	 * 初始资料任务
	 */
	  public synchronized void initDataTask(){

		{
			try
			{
				progressDg = new ProgressDialog(this);
				progressDg.setTitle(XWCodeTrans.doTrans("正在获取通讯录好友信息"));
				
				if(progressDg!=null){
					progressDg.show();
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			

			new AsyncTask<String, String, String>() {
				@Override
				protected String doInBackground(String... arg0) {
					
					Log.v(TAG,"doInBackground");
					
					//初始化资料
					try
					{
						loadContactBeans();
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
	
					return null;
				}
	
				@Override
				protected void onPostExecute(String result) {
					super.onPostExecute(result);
					if(bIsDestroy){
						return;
					}
					try
					{
						if(progressDg!=null){
							progressDg.dismiss();
							progressDg=null;
						}

					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}

					Log.v(TAG,"onPostExecute");
					updateListView();

				}
	
				@Override
				protected void onPreExecute() {
				}
	
	
			}.execute("");
			
		}
	}


	private void updateListView(){
		if(ContactDB.peoples.size()>0){
			sortData(ContactDB.peoples);
			adapter.notifyDataSetChanged();
		}
	}

	/**按类型排序
	 */
	public static List<ContactBean> sortData(List<ContactBean> peoples){
		List<ContactBean> list=peoples;
		if(list!=null&&!list.isEmpty()){
			ComparatorContact compare = new ComparatorContact();
			Collections.sort(list,compare);
		}
		return list;
	}



	
	



	/**
	 * 生成listView
	 */
	private void initListView(){

		adapter=new PhoneContactUIAdapter(ContactDB.peoples, this);
		contactListView.setAdapter(adapter);
		contactListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
									int position, long arg3) {
				Log.e(TAG,"onItemClick:"+position);
				if(ContactDB.peoples.size()>position){
					ContactBean bean = ContactDB.peoples.get(position);
					if(bean!=null){
						if(bean.getContactPhone().equals(XWDataCenter.xwDC.loginName)){
							showToastTips(XWCodeTrans.doTrans("不能添加自己"));
						}else{
							addAFriend(bean);
						}
					}
				}

			}

		});
	}


	final String[] selPhoneCols = new String[]{
			ContactsContract.CommonDataKinds.Phone._ID,
			ContactsContract.CommonDataKinds.Phone.NUMBER,
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Phone.TYPE,
			ContactsContract.CommonDataKinds.Phone.LABEL,
			ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY};
	/**
	 * 初始资料
	 */
	public void loadContactBeans() {
		long st = System.currentTimeMillis();
		List<ContactBean> peoples = new ArrayList<>();
		try {

			Cursor phone = MainApplication.getInstance().getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
					selPhoneCols,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
					null,
					ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

			if (null != phone) {
				int i_number=phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				int i_name=phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
				while (phone.moveToNext()) {
					String phoneNumber = phone.getString(i_number);
					String name=phone.getString(i_name);
					if(phoneNumber!=null&&phoneNumber.trim().length()>0){
						//过滤号码
						phoneNumber = NumberUtil.getNumber(phoneNumber);
						ContactBean cb = new ContactBean();
						cb.setContactName(name);
						cb.setContactPhone(phoneNumber);
						peoples.add(cb);

					}

				}
				phone.close();
			}
			Log.e(TAG, "query take time:" + (System.currentTimeMillis() - st) * 0.001);
			if ( peoples.size() > 0) {
				Log.e(TAG, "query listSize:" + peoples.size());
                ContactDB.peoples.clear();
				ContactDB.peoples.addAll(peoples);
				peoples.clear();
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}




	@Override
	protected void onDestroy() {
		super.onDestroy();
		try
		{
			if(progressDg!=null){
				progressDg.dismiss();
				progressDg=null;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}



	}

	/**打开短信编辑器发送
	 * @param text
	 */
	private void sendSMS(String text,String number){
		if(text==null){
			return;
		}
		Uri uri = Uri.parse("smsto:"+number);          
		Intent it = new Intent(Intent.ACTION_SENDTO, uri);          
		it.putExtra("sms_body", text);          
		startActivity(it);
	}

	/**
	 * 添加好友
	 */
	private void addAFriend(final ContactBean bean){
		if(XWDataCenter.xwDC==null||XWDataCenter.xwDC.groupsInfo==null){
			return;
		}
		try {
			
			Intent nextPage = new Intent();
			nextPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			nextPage.setClass(this,
					FriendQuery.class);
			startActivity(nextPage);
			
			
			int ret = XWDataCenter.xwDC
					.queryFriendForCondition(
							(XWDataCenter.RegularPhoneNumber(bean.getContactPhone()) + "\0")
							.getBytes("GBK"),
							("" + "\0")
							.getBytes("GBK"),
							0, (""+"\0").getBytes("GBK"));
			/////ADD_FRIEND=true;

			//更新
			////bean.setType(-1);
			/////saveContact(bean);
			
			/////ContactUI.this.handler.sendEmptyMessage(MSG_UPDATE_TYPE);
			
			this.finish();
			
			Log.e(TAG,"queryFriendForCondition:"+ret);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onStop() {	
		try
		{
			if(progressDg!=null){
				progressDg.dismiss();
				progressDg=null;
			}
		}catch(Exception ex)
		{
			
		}

		super.onStop();
	}

	@Override
	public void finish() {
		super.finish();
		MainApplication.getInstance().clearContext(this);
	}
}