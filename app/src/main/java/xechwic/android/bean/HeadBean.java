package xechwic.android.bean;


/**
 * 头像信息
 *
 */
public class HeadBean {
	public static String data="frndata";//json KEY
	private String user_name;    //本账号
	private String friend_name;  //好 友账户
	private String fd_group;     //分组
	private String image_name;   //头像名
	private String message;      //信息
	private String lat;          //纬度
//	private String lng;           //经度
	private String location;      //位置
	private String updatetime;    //更新时间
	private String device_brand;   //设备名称
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getFriend_name() {
		return friend_name;
	}
	public void setFriend_name(String friend_name) {
		this.friend_name = friend_name;
	}
	public String getFd_group() {
		return fd_group;
	}
	public void setFd_group(String fd_group) {
		this.fd_group = fd_group;
	}
	public String getImage_name() {
		return image_name;
	}
	public void setImage_name(String image_name) {
		this.image_name = image_name;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
//	public String getLng() {
//		return lng;
//	}
//	public void setLng(String lng) {
//		this.lng = lng;
//	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}
	public String getDevice_brand() {
		return device_brand;
	}
	public void setDevice_brand(String device_brand) {
		this.device_brand = device_brand;
	}


}
