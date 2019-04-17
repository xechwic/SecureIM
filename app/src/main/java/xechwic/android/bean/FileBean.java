package xechwic.android.bean;

/**
 * 记录所有传输文件（音频、图片、文件）的详细信息
 * @author Administrator
 *
 */
public class FileBean implements java.io.Serializable{

	private static final long serialVersionUID = 1L;

	//文件发送或接收时间（这个时间，也是查找文件信息的key值）
	private String time ;
	
	//文件接收后，其data路径
	private String dataPath;
	
	//文件本地保存路径
	private String sdcardPath;
	
	/**
	 * 文件状态
	 * 0等待接收
	 * 1正在发送
	 * 2对方停止
	 * 3错
	 * >=10(10,11,12)发送成功
	 */
	private int status;
	
	//文件类型(image/file)
	private String type;
	
	//文件进度
	private int progress;
	
	//当前用户id
	private int cid;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public String getSdcardPath() {
		return sdcardPath;
	}

	public void setSdcardPath(String sdcardPath) {
		this.sdcardPath = sdcardPath;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
