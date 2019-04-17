package xechwic.android.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import xechwic.android.bean.ChatHistoryBean;
import xechwic.android.bean.ChatMsgEntity;
import xechwic.android.util.GsonUtil;

public class MessageDB {
	private SQLiteDatabase db;
    private static final String TABLE_PRE="chatmsg";
	public MessageDB(Context context,String sDBName) {
		db = context.openOrCreateDatabase(sDBName,
				Context.MODE_PRIVATE, null);
	}

	private void createTable(String tablename){
		db.execSQL("CREATE table IF NOT EXISTS "
				+tablename
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,no TEXT" +
				",friendAccount TEXT,name TEXT, img TEXT,date TEXT,comMeg TEXT,message TEXT" +
				",sendflag TEXT,progress TEXT,sendtype TEXT,filepath TEXT,read TEXT,snap TEXT)");
	}
	/**检测信息是否存在
	 */
	public boolean isExist(String account,String friendAccount, ChatMsgEntity entity){
		if(db==null||!db.isOpen()){
			return false;
		}
		if(entity==null){
			return false;
		}
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		boolean isExi=false;
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
        createTable(tablename);
		Cursor c = db.rawQuery("SELECT * from "+tablename+ " WHERE no="+entity.getNo(), null);
		if(c!=null){
			if(c.getCount()>0){
				isExi=true;
			}
			c.close();
		}
		return isExi;
	}

	
	
	/**保存信息
	 */
	public void saveMsg(String account,String friendAccount, ChatMsgEntity entity) {
		if(db==null||!db.isOpen()){
			return ;
		}
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
	    String strText=GsonUtil.GsonString(entity);
	    strText=new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(strText.getBytes()));
//		Log.e("messageDB","test encrypt:"+strText);
		db.execSQL(
				"insert into " + tablename
						+ " (no,message,read,snap,sendflag) values(?,?,?,?,?)",
				new Object[] { entity.getNo(), strText,entity.getRead(),entity.getSnap(),entity.getSendFlag()});
	}

	/**读取信息
	 */
	public List<String> getMsg(String account,String friendAccount) {
		if(db==null||!db.isOpen()){
			return null;
		}
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		List<String> list = new ArrayList<String>();
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
		Cursor c = db.rawQuery("SELECT * from " + tablename + " ORDER BY _id asc", null);
		if(c!=null){
			int i_message=c.getColumnIndex("message");
			while (c.moveToNext()) {
				String message = c.getString(i_message);
			    if(message!=null){
                  list.add(message);
			    }

			}
			c.close();
		}
		return list;
	}

	/**更新记录
	 */
	public boolean updateMsg(String account,String friendAccount,ChatMsgEntity entity){
		if(db==null||!db.isOpen()||entity==null){
			return false;
		}
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
	    String strText=GsonUtil.GsonString(entity);
	    strText=new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(strText.getBytes()));
		 ContentValues values = new ContentValues(); 
		 values.put("message",strText);

		return db.update(tablename, values, "no="+entity.getNo(), null)>0;
	}

	/**删除记录
	 */
	public void delMsg(String account,ChatMsgEntity entity){
		if(db==null||!db.isOpen()||entity==null){
			return ;
		}
		account=account.replace("+", "_");//替换+
		String friendAccount=entity.getFriendAccount().replace("+", "_");
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);

        db.delete(tablename,"no="+entity.getNo(),null);
	}

	/**删除记录
	 */
	public boolean deleteFriendNode(String account,String friendAccount){
		if(account==null||friendAccount==null){
			return false;
		}
		if(db==null||!db.isOpen()){
			return false;
		}
		account=account.replace("+", "_");//替换+
		friendAccount=friendAccount.replace("+", "_");
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
		return db.delete(tablename, null, null)>0;
	}

	/**
	 *获取要加入阅后即焚的文件消息
     */
	public List<ChatMsgEntity> getSnapFileEntitys(String account,String friendAccount){
			if(db==null||!db.isOpen()){
				return null;
			}
		    if(TextUtils.isEmpty(account)||TextUtils.isEmpty(friendAccount)){
				return null;
			}
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		List<ChatMsgEntity> list = new ArrayList<>();
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
		Cursor c = db.rawQuery("SELECT * from " + tablename + " ORDER BY _id asc", null);
		if(c!=null){
			int i_sendflag=c.getColumnIndex("sendflag");
			int i_no=c.getColumnIndex("no");
			while (c.moveToNext()) {
				int sendflag = c.getInt(i_sendflag);
				long no=c.getLong(i_no);
				if(sendflag==10){
					ChatMsgEntity entity=new ChatMsgEntity();
					entity.setNo(no);
					entity.setSendFlag(sendflag);
					list.add(entity);
				}

			}
			c.close();
		}
		return list;
	}
	/**获取所有已读
	 */
	public List<ChatMsgEntity> getHasreadEntitys(String account,String friendAccount){
		if(db==null||!db.isOpen()){
			return null;
		}
		if(account==null||friendAccount==null){
			return null;
		}
		String tempFriendAccount=friendAccount;
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
		Cursor c = db.rawQuery("SELECT * from "+tablename, null);
		List<ChatMsgEntity> list=new ArrayList<>();
		if(c!=null){
			int i_unread=c.getColumnIndex("read");
			int i_no=c.getColumnIndex("no");
			int i_sendflag=c.getColumnIndex("sendflag");
			while (c.moveToNext()) {
				int unread=c.getInt(i_unread);
				long no=c.getLong(i_no);
				int sendflag=c.getInt(i_sendflag);
				if(unread<1&&sendflag!=11){
					ChatMsgEntity entity=new ChatMsgEntity();
					entity.setNo(no);
					entity.setFriendAccount(tempFriendAccount);
					entity.setSnaptime(System.currentTimeMillis());
					list.add(entity);
				}
			}
			c.close();
		}
		return list;

	}
	/**获取所以未读
	 */
	public List<ChatMsgEntity> getUnreadEntitys(String account,String friendAccount){
		if(db==null||!db.isOpen()){
			return null;
		}
		if(account==null||friendAccount==null){
			return null;
		}
		String tempFriendAccount=friendAccount;
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
		Cursor c = db.rawQuery("SELECT * from "+tablename, null);
		List<ChatMsgEntity> list=new ArrayList<>();
		if(c!=null){
			int i_unread=c.getColumnIndex("read");
			int i_no=c.getColumnIndex("no");
			int i_sendflag=c.getColumnIndex("sendflag");
			while (c.moveToNext()) {
				int unread=c.getInt(i_unread);
				long no=c.getLong(i_no);
				int sendflag=c.getInt(i_sendflag);
				if(unread>0&&sendflag!=11){
					ChatMsgEntity entity=new ChatMsgEntity();
					entity.setNo(no);
					entity.setFriendAccount(tempFriendAccount);
					entity.setSnaptime(System.currentTimeMillis());
					list.add(entity);
				}
			}
			c.close();
		}
		return list;

	}

	//清理未读
	public void resetUnread(String account,String friendAccount){
		if(db==null||!db.isOpen()){
			return ;
		}
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
		ContentValues values = new ContentValues();
		values.put("read",0);
		db.update(tablename, values, null, null);
	}

	//清理阅后即焚的文件消息
	public void resetSnap(String account,String friendAccount){
		if(db==null||!db.isOpen()){
			return ;
		}
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
		ContentValues values = new ContentValues();
		values.put("snap",0);
		db.update(tablename, values, "sendflag!=11", null);
	}

	public ChatHistoryBean getLastMsg(String account, String friendAccount){
		ChatHistoryBean bean=null;
		if(db==null||!db.isOpen()){
			return null;
		}
		account=account.replace("+", "_");// 替换+
		friendAccount=friendAccount.replace("+", "_");
		List<String> list = new ArrayList<String>();
		String tablename=TABLE_PRE+"_"+account+"_"+friendAccount;
		createTable(tablename);
		Cursor c = db.rawQuery("SELECT * from " + tablename + " ORDER BY _id asc", null);
		int sum=0;
		if(c!=null){
			int i_message=c.getColumnIndex("message");
			int i_read=c.getColumnIndex("read");
			while (c.moveToNext()) {
				String message = c.getString(i_message);
				if(!TextUtils.isEmpty(message)){
					list.add(message);
				}
				int unread=c.getInt(i_read);
				if(unread<0){
					unread=0;
				}
				sum+=unread;
			}
			c.close();
		}
        if(list.size()>0){
			String message=list.get(list.size()-1);
			if(!TextUtils.isEmpty(message)){
//				Log.e("message","message："+message);
				String strText=new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_aes(message.getBytes()));
				if(!TextUtils.isEmpty(strText)){
					ChatMsgEntity entity =GsonUtil.GsonToBean(strText, ChatMsgEntity.class);
					bean=new ChatHistoryBean();
					bean.setLogin_name(entity.getFriendAccount());
					bean.setSignName(entity.getName());
					bean.setRecentChat(entity.getMessage());
					bean.setLastTime(""+entity.getNo());
//					bean.setIntroduction(entity.getFilePath());//文件路径
					bean.setUnread(sum);
				}
			}


		}
		return bean;
	}

	public void close() {
		if (db != null)
			db.close();
	}
}
