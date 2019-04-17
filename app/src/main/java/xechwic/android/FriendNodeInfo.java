package xechwic.android;

/**
 * 好友信息
 *
 */
public class FriendNodeInfo {
	private int id;//好友id
	private String login_name;//登录账号
	private int online_type;//在线类型,0为不在线,1为其它,2为参加会议,3为外出就餐,4为接听电话,5为离开,6为马上回来,7为忙碌,8为联机,99为自己且非脱机
	private String signName;//昵称 签名
	private String online_status;//状态说明
	private int number;//数字短号
	private int sex;   //性别
	private int age;   //年龄
	private String area;//区域
	private String introduction;//简介 历史聊天记录用于存文件路径
	private int acceptType;     
	private String groupName;
	private int accountValue; 
	private String icon;         //图标名
	private String recentChat;//最近聊天记录
	private String lastTime;//最后聊天的记录
	private int unread;//未读信息
	private int updateTime;//更新时间

	public int getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(int updateTime) {
		this.updateTime = updateTime;
	}

	public int getUnread() {
		return unread;
	}
	public void setUnread(int unread) {
		this.unread = unread;
	}
	public String getLastTime() {
		return lastTime;
	}
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getRecentChat() {
		return recentChat;
	}
	public void setRecentChat(String recentChat) {
		this.recentChat = recentChat;
	}
	public int getOnline_type() {
		return online_type;
	}
	public void setOnline_type(int online_type) {
		this.online_type = online_type;
	}
	
	public int getAccountValue() {
		return accountValue;
	}
	public void setAccountValue(int accountValue) {
		this.accountValue = accountValue;
	}
	public boolean hasNoReadMsg=false;//有未读信息
	
	public boolean getHasNoReadMsg() {
		return hasNoReadMsg;
	}
	public void setHasNoReadMsg(boolean hasNoReadMsg) {
		this.hasNoReadMsg = hasNoReadMsg;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public int getAcceptType() {
		return acceptType;
	}
	public void setAcceptType(int acceptType) {
		this.acceptType = acceptType;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
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
	
	
	//////////////2014-08-11,解决online_status为null时显示不对的问题。
	public String getOnline_status() {
		/*if (online_status==null)
		{
			online_status=XWCodeTrans.doTrans("断开");
		}*/
		return online_status;
	}
	public void setOnline_status(String online_status) {
		/*if (online_status==null)
		{
			online_status=XWCodeTrans.doTrans("断开");
		}*/
		this.online_status = online_status;
		
	}
	
	
}
