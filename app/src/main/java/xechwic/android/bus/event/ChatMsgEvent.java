package xechwic.android.bus.event;

import xechwic.android.bean.ChatRefreshBean;

public class ChatMsgEvent {

	public ChatRefreshBean bean;
	public ChatMsgEvent(ChatRefreshBean bean){
		this.bean=bean;
	}
}
