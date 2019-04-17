package xechwic.android.bean;

import java.util.List;

import xechwic.android.FriendNodeInfo;

public class GroupBean {
	private String groupName;
	private List<FriendNodeInfo> childList;
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public List<FriendNodeInfo> getChildList() {
		return childList;
	}
	public void setChildList(List<FriendNodeInfo> childList) {
		this.childList = childList;
	}
	
}
