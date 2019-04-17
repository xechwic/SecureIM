package xechwic.android.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import xechwic.android.bean.ChatHistoryBean;
import xechwic.android.util.GsonUtil;

/**
 * 聊天历史信息
 *
 */
public class ChatHistoryDB {
	private SQLiteDatabase db;
	private static final String TABLE_PRE="chathistory";
	public ChatHistoryDB(Context context,String sDBName) {
		db = context.openOrCreateDatabase(sDBName,
				Context.MODE_PRIVATE, null);
	}

	private void createTable(String tablename){
		db.execSQL("CREATE table IF NOT EXISTS "
				+tablename
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,id TEXT" +
				",login_name TEXT,signName TEXT" +
				",introduction TEXT" +
				",icon TEXT,recentChat TEXT,lastTime TEXT,unread TEXT)");
	}
	/**保存信息
	 */
	public void saveMsg(ChatHistoryBean bean, String table) {
		if(db==null||!db.isOpen()){
			return ;
		}
		if(bean==null||table==null){
			return;
		}
		table=table.replace("+", "_");// 替换+
		String tablename=TABLE_PRE+"_"+table;
		createTable(tablename);
	    String loginname=new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(bean.getLogin_name().getBytes()));
	    String strText=GsonUtil.GsonString(bean);
	    strText=new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(strText.getBytes()));
		db.execSQL(
				"insert into " + tablename
						+ " (login_name,recentChat,unread) values(?,?,?)",
				new Object[] { loginname, strText,bean.getUnread()});

	}





	/**检测信息是否存在
	 */
	public boolean isExistFriend(String friendAccount,String table){
		if(db==null||!db.isOpen()){
			return false;
		}
		if(friendAccount==null||table==null){
			return false;
		}
		table=table.replace("+", "_");// 替换+
		String tablename=TABLE_PRE+"_"+table;
		boolean isExi=false;
		createTable(tablename);
		String loginname= friendAccount;
		loginname=new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(loginname.getBytes()));
		String where = String.format("login_name= '%s'", loginname);
		Cursor c = db.rawQuery("SELECT * from "+tablename  + " WHERE "+where, null);
		if(c!=null){
			if(c.getCount()>0){
				isExi=true;
			}
			c.close();
		}
		return isExi;
	}

	/**读取信息
	 */
	public List<String> getAllHistorys(String table) {
		if(db==null||!db.isOpen()){
			return null;
		}
		if(table==null||table.length()<1){
			return null;
		}
		table=table.replace("+", "_");// 替换+
		String tablename=TABLE_PRE+"_"+table;
		createTable(tablename);
		List<String> list = new ArrayList<>();
		Cursor c = db.rawQuery("SELECT * from "+tablename, null);
		if(c!=null){
			int i_recentChat=c.getColumnIndex("recentChat");
		while (c.moveToNext()) {
			String message = c.getString(i_recentChat);
		    if(message!=null){
				list.add(message);
		    }
		}
		c.close();
		}
		return list;
	}

	
	/**获取所以未读
	 */
	public int getAllUnreads(String table){
		if(db==null||!db.isOpen()){
			return 0;
		}
		if(table==null||table.length()<1){
			return 0;
		}
		table=table.replace("+", "_");// 替换+
		String tablename=TABLE_PRE+"_"+table;
		createTable(tablename);
		int sum=0;
		Cursor c = db.rawQuery("SELECT * from "+tablename, null);
		if(c!=null){
			int i_unread=c.getColumnIndex("unread");
			while (c.moveToNext()) {
				int unread=c.getInt(i_unread);
                if(unread<0){
                	unread=0;
                }
				sum+=unread;
		}
			c.close();
		}
		return sum;
	
	}
	
	/**读取信息
	 */
	public ChatHistoryBean getAChatBean(String table,String friendAccount) {
		if(db==null||!db.isOpen()){
			return null;
		}
		if(table==null||friendAccount==null){
			return null;
		}
		ChatHistoryBean bean=null;
		try {
			table = table.replace("+", "_");// 替换+
			String tablename = TABLE_PRE + "_" + table;
		    createTable(tablename);
			String friendname = new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(friendAccount.getBytes()));
			String sql = String.format("SELECT * from " + tablename + " WHERE login_name='%s'", friendname);
			Cursor c = db.rawQuery(sql, null);
			if (c != null) {
				int i_recentChat = c.getColumnIndex("recentChat");
				while (c.moveToNext()) {
					String message = c.getString(i_recentChat);
					if (!TextUtils.isEmpty(message)) {
						String strText = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_aes(message.getBytes()));
						if (!TextUtils.isEmpty(strText)) {
							bean = GsonUtil.GsonToBean(strText, ChatHistoryBean.class);
							break;
						}

					}

				}
				c.close();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return bean;
	}

	/**更新记录
	 */
	public void updateFriendNode(ChatHistoryBean bean,String table){
		//检测数据库打开情况
		if(db==null||!db.isOpen()){
			return ;
		}
		if(bean==null||bean.getLogin_name()==null||table==null||table.length()<1){
			return ;
		}
		table=table.replace("+", "_");// 替换+
		String tablename=TABLE_PRE+"_"+table;
		ContentValues values = new ContentValues(); 
		
		if(bean.getRecentChat()!=null){
			String recentChat=GsonUtil.GsonString(bean);
			recentChat=new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(recentChat.getBytes()));
			values.put("recentChat", recentChat);
		}
		String loginname= bean.getLogin_name();
		loginname=new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(loginname.getBytes()));
		values.put("login_name", loginname);
		if(bean.getUnread()>=0){
			values.put("unread", bean.getUnread());
		}else if(bean.getUnread()<0){
			values.put("unread", 0);
		}
			
		
		String where = String.format("login_name= '%s'", loginname);
		 db.update(tablename, values, where, null);
	}
	


	/**unread
	 */
	public void resetUnread(String account,String friendaccount){
		if(db==null||!db.isOpen()){
			return ;
		}
		account=account.replace("+", "_");//
		friendaccount=friendaccount.replace("+","_");
		ChatHistoryBean nodeT= getAChatBean(account, friendaccount);
		if(nodeT!=null){
			nodeT.setUnread(0);
			updateFriendNode(nodeT, account);
		}
	}
	
	/**删除记录
	 */
	public boolean deleteFriendNode(String friendAccount,String table){
		if(db==null||!db.isOpen()){
			return false;
		}
		table=table.replace("+", "_");// 替换+
		String tablename=TABLE_PRE+"_"+table;
		String loginname= friendAccount;
		loginname=new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(loginname.getBytes()));
		String where = String.format("login_name= '%s'",loginname);
        
		return db.delete(tablename, where, null)>0;
	}

    public void clearAll(String table){
		if(db==null||!db.isOpen()){
			return;
		}
		if(table==null){
			return ;
		}
		table=table.replace("+", "_");// 替换+
		String tablename=TABLE_PRE+"_"+table;
		db.delete(tablename, null, null);
	}

	public void close() {
		if (db != null)
			db.close();
	}
}
