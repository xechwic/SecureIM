package xechwic.android.util;

import xechwic.android.FriendControl;

/**
 * Created by luman on 2016/11/3 12:22
 * 好友信息节点操作
 */

public class FriendNodeUtil {


    ///获取好友列表位置作为id
    public static int getFriendId(String loginName){
        if(FriendControl.friendList.size()>0){
            for(int i=0;i<FriendControl.friendList.size();i++){
                if(FriendControl.friendList.get(i).getLogin_name().equals(loginName)){
                    if(FriendControl.friendList.get(i).getId()>0){
                        return FriendControl.friendList.get(i).getId();
                    }else{
                        return (i+100);/////加10避免跟其他冲突
                    }
                }
            }
        }
        return 0;
    }
}
