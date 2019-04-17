package xechwic.android.bean;

public class ChatRefreshBean {

	public int type ;//(1,增加，2更新）
	public ChatMsgEntity entity;
	public ChatRefreshBean(ChatMsgEntity bean,int type){
		this.entity=bean;
		this.type=type;
	}
}
