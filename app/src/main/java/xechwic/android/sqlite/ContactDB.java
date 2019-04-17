package xechwic.android.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import xechwic.android.bean.ContactBean;

/**
 *联系人信息
 *
 */
public class ContactDB {

	public static List<ContactBean> peoples=new ArrayList<>();/////手机联系人列表

	private SQLiteDatabase db;
	////private static final String DBNAME="xwcontact.db";
	public ContactDB(Context context,String sDBName) {
        db = context.openOrCreateDatabase(sDBName,
		 Context.MODE_PRIVATE, null);
	}

	/**保存信息
	 */
	public void saveNew(ContactBean bean,String table) {
		if(db==null||!db.isOpen()){
			return ;
		}
		if(bean==null||table==null){
			return;
		}
		table=table.replace("+", "_");//TODO 替换+
		////Log.e("contactDB","saveNew: "+ bean.getContactPhone());
		db.execSQL("CREATE table IF NOT EXISTS contact_"
				+table

				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT" +
				",contactName TEXT, contactPhone TEXT,contactHomePhone TEXT,type int)");

		db.execSQL(
				"insert into contact_"+table 
						+ " (contactName,contactPhone,contactHomePhone,type" +
						") values(?,?,?,?)",
						new Object[] {bean.getContactName(),bean.getContactPhone(),
						bean.getContactHomePhone(),bean.getType()});
	}





	/**检测信息是否存在
	 */
	public boolean isExistFriend(ContactBean bean,String table){
		if(db==null||!db.isOpen()){
			return false;
		}
		if(bean==null||table==null){
			return false;
		}
		table=table.replace("+", "_");//TODO 替换+
		////////Log.v("XIM","ContactDB isExistFriend");
		
		boolean isExi=false;
		
		try
		{
		db.execSQL("CREATE table IF NOT EXISTS contact_"
				+table

				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT" +
				",contactName TEXT, contactPhone TEXT,contactHomePhone TEXT,type int)");
		Cursor c = db.rawQuery("SELECT * from contact_"+table  + " WHERE contactPhone='"+bean.getContactPhone()+"'", null);
		if(c!=null){
			if(c.getCount()>0){
				isExi=true;
			}
			c.close();
		}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		////Log.v("XIM","ContactDB isExistFriend return "+isExi);
		return isExi;
	}

	/**读取信息
	 */
	public synchronized List<ContactBean> getAllFriends(String table) {
		if(db==null||!db.isOpen()){
			return null;
		}
		if(table==null||table.length()<1){
			return null;
		}
		table=table.replace("+", "_");//TODO 替换+
		List<ContactBean> list = new ArrayList<ContactBean>();
		db.execSQL("CREATE table IF NOT EXISTS contact_"
				+table

				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT" +
				",contactName TEXT, contactPhone TEXT,contactHomePhone TEXT,type int)");
		Cursor c = db.rawQuery("SELECT * from contact_"+table  + " ORDER BY type ASC ", null);
		while (c.moveToNext()) {
			String contactName = c.getString(c.getColumnIndex("contactName"));
			String contactPhone = c.getString(c.getColumnIndex("contactPhone"));
			String contactHomePhone = c.getString(c.getColumnIndex("contactHomePhone"));
			int type = c.getInt(c.getColumnIndex("type"));
			
			ContactBean bean = new ContactBean();
			bean.setContactName(contactName);
			bean.setContactPhone(contactPhone);
			bean.setContactHomePhone(contactHomePhone);
			bean.setType(type);
			
			
			/////Log.v("XIM","ContactDB getAllHistorys "+bean.getContactPhone()+" "+bean.getType());
			
			list.add(bean);
		}
		c.close();
		return list;
	}

	/**读取信息
	 */
	public ContactBean getAFriend(String table,String contactphone) {
		if(db==null||!db.isOpen()){
			return null;
		}
		if(table==null||table.length()<1||contactphone==null||contactphone.length()<1){
			return null;
		}
		table=table.replace("+", "_");//TODO 替换+
		db.execSQL("CREATE table IF NOT EXISTS contact_"
				+table

				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT" +
				",contactName TEXT, contactPhone TEXT,contactHomePhone TEXT,type int)");
		String sql = String.format("SELECT * from contact_"+table+" WHERE contactPhone='%s'", contactphone);
		Cursor c = db.rawQuery(sql, null);
		if(c==null||c.getCount()==0){
			return null;
		}
		ContactBean bean = new ContactBean();
		while (c.moveToNext()) {
			String contactName = c.getString(c.getColumnIndex("contactName"));
			String contactPhone = c.getString(c.getColumnIndex("contactPhone"));
			String contactHomePhone = c.getString(c.getColumnIndex("contactHomePhone"));
			int type = c.getInt(c.getColumnIndex("type"));
			
			bean.setContactName(contactName);
			bean.setContactPhone(contactPhone);
			bean.setContactHomePhone(contactHomePhone);
			bean.setType(type);
			
		}
		c.close();
		return bean;
	}

	/**更新记录
	 */
	public boolean updateFriendNode(ContactBean node,String table){
		//检测数据库打开情况
		if(db==null||!db.isOpen()){
			return false;
		}
		if(node==null||node.getContactPhone()==null||table==null||table.length()<1){
			return false;
		}
		
		////Log.v("XIM","updateFriendNode "+node.getContactPhone());
		
		table=table.replace("+", "_");//TODO 替换+
		
		/////Log.v("XIM","updateFriendNode table:"+table);
		
		ContentValues values = new ContentValues(); 
		if(node.getContactName()!=null){
			values.put("contactName", node.getContactName()); 
		}
		if(node.getContactPhone()!=null){
			values.put("contactPhone", node.getContactPhone()); 
		}
		if(node.getContactPhone()!=null){
			values.put("contactHomePhone", node.getContactHomePhone()); 
		}
		/*if(node.getType()==-1){
			values.put("type", 0);
		}else if(node.getType()!=0){     //-2已添加，1添加
			
		}*/
		values.put("type", node.getType());
		
		//////Log.v("XIM","updateFriendNode table value "+table+ "  "+ values);
		boolean bRet=
		 db.update("contact_"+table, values, "contactPhone='"+node.getContactPhone()+"'", null)>0;
		 
		//////Log.v("XIM","updateFriendNode  ok table value "+table+ "  "+ values);	 
		 return bRet;
	}

	
	/**删除记录
	 */
	public boolean deleteFriendNode(ContactBean node,String table){
		if(db==null||!db.isOpen()){
			return false;
		}
		if(node==null||table==null){
			return false;
		}
		table=table.replace("+", "_");//TODO 替换+
		return db.delete("contact_"+table, "contactPhone='"+node.getContactPhone()+"'", null)>0;
	}


	public void close() {
		if (db != null)
			db.close();
	}
}
