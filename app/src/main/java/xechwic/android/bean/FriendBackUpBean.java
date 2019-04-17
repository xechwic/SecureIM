package xechwic.android.bean;

import java.io.Serializable;
import java.util.List;

import xechwic.android.FriendNodeInfo;

/**
 * Created by luman on 2016/10/10 11:44
 */

public class FriendBackUpBean implements Serializable{

    private static final long serialVersionUID = 1L;

    private List<FriendNodeInfo> list=null;

    public List<FriendNodeInfo> getList() {
        return list;
    }

    public void setList(List<FriendNodeInfo> list) {
        this.list = list;
    }
}
