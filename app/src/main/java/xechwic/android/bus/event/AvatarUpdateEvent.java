package xechwic.android.bus.event;

import xechwic.android.FriendNodeInfo;

/**
 * Created by luman on 2016/9/12 16:14
 * 头像更新事件
 */
public class AvatarUpdateEvent {

    public FriendNodeInfo fni;
    public AvatarUpdateEvent(FriendNodeInfo fni){
        this.fni=fni;
    }

}
