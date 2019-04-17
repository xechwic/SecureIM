package xechwic.android;

/**
 * 好友邀请消息
 *
 */
public class MessageParamA {
	private FriendNodeInfo fni;
	private String groupName;
	private int msgType;
	public FriendNodeInfo getFni() {
		return fni;
	}
	public void setFni(FriendNodeInfo fni) {
		this.fni = fni;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public int getMsgType() {
		return msgType;
	}
	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}
}
