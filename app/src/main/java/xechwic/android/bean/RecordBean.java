package xechwic.android.bean;

public class RecordBean implements java.io.Serializable{

	/**
	 * 通话记录
	 */
	private static final long serialVersionUID = 1L;
	//xw网络电话号码
	private String xwNumber ;
	//名称
	private String contactName = null;
	//电话
	private String contactPhone;
	//家庭电话
	private String contactHomePhone;
	//拨号开始、来电开始时间
	private long dialStartTime ;
	
	//通话拨出(true)、接入(false)
	private boolean isDial = true;
	//通话开始时间
	private long startTime;
	//通话结束时间
	private long endTime;
	//通话时长
	private long callTime;
	
	
	public RecordBean(String xwNumber,String contactName){
		this.dialStartTime = System.currentTimeMillis();
		this.xwNumber = xwNumber;
		this.contactName = contactName;
	}
	
	public RecordBean(String contactName,String contactPhone,String contactHomePhone){
		this.dialStartTime = System.currentTimeMillis();
		this.contactPhone = contactPhone;
		this.contactName = contactName;
		this.contactHomePhone = contactHomePhone;
	}
	
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactPhone() {
		return contactPhone;
	}
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public String getContactHomePhone() {
		return contactHomePhone;
	}
	public void setContactHomePhone(String contactHomePhone) {
		this.contactHomePhone = contactHomePhone;
	}
	public boolean isDial() {
		return isDial;
	}
	public void setDial(boolean isDial) {
		this.isDial = isDial;
	}
	
	public long getDialStartTime() {
		return dialStartTime;
	}

	public void setDialStartTime(long dialStartTime) {
		this.dialStartTime = dialStartTime;
	}

	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getCallTime() {
		return callTime;
	}
	public void setCallTime(long callTime) {
		this.callTime = callTime;
	}
	public String getXwNumber() {
		return xwNumber;
	}
	public void setXwNumber(String xwNumber) {
		this.xwNumber = xwNumber;
	}
	
}
