package xechwic.android.bean;

/**
 * 手机联系人结点
 *
 */
public class ContactBean implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String contactName = null;
	private String contactPhone;
	private String contactHomePhone;
	private int type;//类型：0未注册，1，已注册，2好友
	
	
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
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
}
