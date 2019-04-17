package xechwic.android.bean;

/**
 * 历史消息
 */
public class ChatHistoryBean {
	private int id;//好友id
	private String login_name;//登录账号
	private String signName;//昵称 签名
	private String recentChat;//最近聊天记录
	private String lastTime;//最后聊天的记录
	private String introduction;//简介 历史聊天记录用于存文件路径
	private int unread;//未读信息
	private String icon;//头像

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLogin_name() {
		return login_name;
	}

	public void setLogin_name(String login_name) {
		this.login_name = login_name;
	}

	public String getSignName() {
		return signName;
	}

	public void setSignName(String signName) {
		this.signName = signName;
	}

	public String getRecentChat() {
		return recentChat;
	}

	public void setRecentChat(String recentChat) {
		this.recentChat = recentChat;
	}

	public String getLastTime() {
		return lastTime;
	}

	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public int getUnread() {
		return unread;
	}

	public void setUnread(int unread) {
		this.unread = unread;
	}
}
