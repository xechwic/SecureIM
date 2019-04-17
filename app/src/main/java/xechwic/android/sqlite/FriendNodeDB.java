package xechwic.android.sqlite;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xechwic.android.FriendControl;
import xechwic.android.FriendNodeInfo;
import xechwic.android.XWDataCenter;
import xechwic.android.bean.ChatHistoryBean;
import xechwic.android.bean.FriendBackUpBean;
import xechwic.android.util.GsonUtil;
import xechwic.android.util.ObjectIO;
import xechwic.android.util.UriConfig;

/**
 * 好友信息  
 *
 */
public class FriendNodeDB {


	/////存在备份数据
	public static boolean existsBackupFriend(){
		if(TextUtils.isEmpty(XWDataCenter.getCurAccount())){
			return false;
		}
		try {
			String str =(String)ObjectIO.readObject(UriConfig.getFriendListPath());
			if (!TextUtils.isEmpty(str)) {
			     return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * 恢复备份的数据
	 */
	public static boolean restoreFriends() {
		if(FriendControl.friendList.size()>0){/////列表还存在
			return true;
		}
		long lst = System.currentTimeMillis();
		try {
			String str =(String)ObjectIO.readObject(UriConfig.getFriendListPath());
			if (!TextUtils.isEmpty(str)) {
				String gsons = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_aes(str.getBytes()));
				Log.e("tag", "list gsons:" + gsons);
				FriendBackUpBean bean = GsonUtil.GsonToBean(gsons, FriendBackUpBean.class);
				if(bean.getList()!=null){
					if(FriendControl.friendList.isEmpty()){
						FriendControl.friendList.addAll(bean.getList());
					}


				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e("friendDB", "getbackupFriends take time:" + (System.currentTimeMillis() - lst) * 0.001);
		if(FriendControl.friendList.size()>0){
			return true;
		}
		return false;
	}

	public static void restoreHeadMap(){
    if(XWDataCenter.headBeanMap==null){
      XWDataCenter.headBeanMap=new HashMap<>();
    }
    if(XWDataCenter.headBeanMap.isEmpty()){
      for(FriendNodeInfo info:FriendControl.friendList){
        if(info!=null){
          XWDataCenter.headBeanMap.put(info.getLogin_name(),info.getIcon());
        }
      }
    }
  }


 public static void removeBackupFriends(){

	  try{

		 ObjectIO.saveObject("", UriConfig.getFriendListPath());

	 }catch (Exception e){
			e.printStackTrace();
		}
 }
	/**
	 * 备份好友信息
	 */
	public static void backupFriends() {
		long lst = System.currentTimeMillis();
		try {
			if (FriendControl.friendList.size() > 0) {
				FriendBackUpBean bean = new FriendBackUpBean();
				bean.setList(FriendControl.friendList);
				String gsons = GsonUtil.GsonString(bean);
				if (gsons != null) {
//					Log.e("friendControl","encrypt_aes before:"+gsons);
					String str = new String(com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes(gsons.getBytes()));
//					Log.e("friendControl","encrypt_aes:"+str);
					ObjectIO.saveObject(str, UriConfig.getFriendListPath());
//					String de = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_aes(str.getBytes()));
//					Log.e("friendControl","decrypt_aes:"+de);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e("friendDB", "backupFriends take time:" + (System.currentTimeMillis() - lst) * 0.001);
	}

	/**
	 * 保存信息
	 */
	public static void saveMsg(FriendNodeInfo node) {
		refreshFriendNodeInfo(node);
	}

	/**
	 * 刷新列表
	 *
	 * @param fni
	 */
	public static void refreshFriendNodeInfo(FriendNodeInfo fni) {
		if (fni != null) {
			FriendNodeInfo node = null;
			if (FriendControl.friendList.size() > 0) {
				node=getAFriend(fni.getLogin_name(),fni.getLogin_name());

				if (node == null) {
					FriendControl.friendList.add(fni);
				} else {
					XWDataCenter.updateNodeInfo(node, fni);
				}
			} else {
				FriendControl.friendList.add(fni);
			}
		}
	}

	/**
	 * 检测信息是否存在
	 *
	 * @param node
	 * @return
	 */
	public static boolean isExistFriend(FriendNodeInfo node, String table) {
		boolean bIs = false;
		FriendNodeInfo info=getAFriend(table,node.getLogin_name());
		if(info!=null){
			bIs=true;
		}
		return bIs;
	}


	/**
	 * 读取信息
	 */
	public static FriendNodeInfo getAFriend(String table, String friendName) {
		FriendNodeInfo nodeInfo=null;
		if(FriendControl.friendList.size()>0){
			List<FriendNodeInfo> list=new ArrayList<>();
			list.addAll(FriendControl.friendList);
			int size=list.size();
			for(int i=0;i<size;i++){
				FriendNodeInfo nodet=list.get(i);
				if(nodet!=null&&(nodet.getLogin_name().equals(friendName))){
					nodeInfo=nodet;
					break;
				}
			}
		}
		return nodeInfo;
	}

	/**
	 * 读取信息
	 */
	public static List<FriendNodeInfo> getAllFriends() {
		return FriendControl.friendList;
	}

	public static String getMyHead(){
		String account=XWDataCenter.getCurAccount();
		if(TextUtils.isEmpty(account)){
			return null;
		}
		if (XWDataCenter.headBeanMap != null) {
			String icon = XWDataCenter.headBeanMap.get(account);
			if (!TextUtils.isEmpty(icon)) {
				return icon;
			}
		}
		FriendNodeInfo nodeInfo=getAFriend(account,account);
		if(nodeInfo!=null){
			return nodeInfo.getIcon();
		}
		return null;
	}

	////头像地址
	public static String getFriendHead(FriendNodeInfo fni) {
		if(fni==null){
			return null;
		}
		if (XWDataCenter.headBeanMap != null) {
			String icon = XWDataCenter.headBeanMap.get(fni.getLogin_name());
			if (!TextUtils.isEmpty(icon)) {
				return icon;
			}
		}
		return fni.getIcon();
	}
  ////头像地址
  public static String getFriendHead(String account) {
    if(TextUtils.isEmpty(account)){
      return null;
    }
    if (XWDataCenter.headBeanMap != null) {
      String icon = XWDataCenter.headBeanMap.get(account);
      if (!TextUtils.isEmpty(icon)) {
        return icon;
      }
    }
    return null;
  }
	/**更新记录
	 */
	public static void updateFriendNode(FriendNodeInfo node,String table){
		refreshFriendNodeInfo(node);
	}

	/**删除记录
	 */
	public static void deleteFriendNode(FriendNodeInfo node,String table){
		FriendNodeInfo nodeInfo=getAFriend(table,node.getLogin_name());

		if(nodeInfo!=null){
			FriendControl.friendList.remove(nodeInfo);
		}
	}


	public void close() {
		
	}
}
