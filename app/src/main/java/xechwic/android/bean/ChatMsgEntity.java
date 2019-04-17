package xechwic.android.bean;

/**
 * 一个聊天消息的JavaBean
 *
 */
public class ChatMsgEntity {
	private long no;//流水号
	private String friendAccount;//好友账号
	private String name;// 消息来自(昵称)
	private String date;// 消息日期
	private String message;// 消息内容
	private int img;       //图标
	private int comMeg = 0;// 是否为收到的消息(0,发，1接收)
	private int read=0;//1未读，0已读
	private int snap=0;//1未加入阅后即焚，0已阅后即焚
    private int sendFlag=1;//发送状态 (>=10发送完成)
    private int progress=0;//发送进度
    private int msgType;//消息类型
    private String filePath;//文件路径
    private long snaptime=0;//启动阅后即焚时间
    public String sAESFilePath;
    
	public ChatMsgEntity() {
		sAESFilePath=null;
	}

	public long getSnaptime() {
		return snaptime;
	}

	public void setSnaptime(long snaptime) {
		this.snaptime = snaptime;
	}

	public int getSnap() {
		return snap;
	}

	public void setSnap(int snap) {
		this.snap = snap;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}



	public int getImg() {
		return img;
	}

	public void setImg(int img) {
		this.img = img;
	}

	public int getComMeg() {
		return comMeg;
	}

	public void setComMeg(int comMeg) {
		this.comMeg = comMeg;
	}

	public int getSendFlag() {
		return sendFlag;
	}

	public void setSendFlag(int sendFlag) {
		this.sendFlag = sendFlag;
	}

	public long getNo() {
		return no;
	}

	public void setNo(long no) {
		this.no = no;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getFriendAccount() {
		return friendAccount;
	}

	public void setFriendAccount(String friendAccount) {
		this.friendAccount = friendAccount;
	}

	
	
}
