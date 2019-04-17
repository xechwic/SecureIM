package xechwic.android.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xechwic.android.act.ServerConfig;

import static xechwic.android.act.ServerConfig.GET_HEAD_PIC;

public class Http implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7584258937152369574L;

	private boolean mHaveNet = true; // 是否有网络
	private String mNetType = ""; // 网络类型
	private String mAPNType = ""; // 网络接入点
	private boolean mIsError = false; // 是否有错误
	private String mErrorMsg = ""; // 错误信息
	private String mIP = "";
	private int mRespondCode;

	private int mConnectTimeout = 10000; // 设置连接服务器超时时间
	private int mReadTimeout = 30000; // 设置从服务器读取数据超时时间

	private static final String BOUNDARY = java.util.UUID.randomUUID()
			.toString();
	private static final String PREFIX = "--";
	private static final String LINEND = "\r\n";
	private static final String MULTIPART_FROM_DATA = "multipart/form-data";
	private static final String CHARSET = "GBK";

	//http://www.ximvoip.net/a2buser/usericons/getFriendProfilePic.php


	
	
	public static String getIconoperBaseDir() {
		return ServerConfig.ICONOPER_BASE_DIR;
	}

	public static String getUploadHeadPic() {
		return ServerConfig.UPLOAD_HEAD_PIC;
	}
	
	/**获取上传头像地址
	 * @return
	 */
	public static String getUploadUrl(){
		return getIconoperBaseDir()+getUploadHeadPic();
	}
	
	/**获取头像目录地址
	 * @return
	 */
	public static String getHeadPicUrl(){
		return getIconoperBaseDir()+GET_HEAD_PIC;
	}

	/**获取所有好像的头像信息
	 * @return
	 */
	public static String getFpfPicUrl(){
		return getIconoperBaseDir()+ServerConfig.GET_FPF_PIC;
	}
	
	public static String getContactType(){
		return getIconoperBaseDir()+ServerConfig.GET_CONTACT_TYPE;
	}
	
	public static String getAHeadUrl(){
		return getIconoperBaseDir()+ServerConfig.GET_AHEAD;
	}
	
	/**
	 * 可在一些循环中加入此参数，以在其他线程中控制循环是否继续
	 */
	private boolean mIsLetGo = true;

	public Http() {
	}

	public Http(int connectTimeout, int readTimeout) {
		setConnectTimeout(connectTimeout);
		setRespondCode(readTimeout);
	}

	/*
	 * post文件较大时，设置超时长一点
	 */
	public void setDefaultPostTimeout() {
		final int timeout = 1800000;
		setReadTimeout(timeout);
		setConnectTimeout(timeout);
	}

	public static String Match(String content, String reg)
	{
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(content);
		String value = "";
		if(matcher.find())
			value = matcher.group();
		return value;
	}
	
	/**
	 * 获取 一个HttpURLConnection， 这里主要区分CMWAP类型和其他类型
	 * 
	 * @param context
	 * @param httpurl
	 * @param isCmwapType
	 * @return
	 * 
	 * 
	 */
	public HttpURLConnection getHttpURLConnection(Context context,
			String httpurl, boolean isCmwapType) {

		HttpURLConnection conn = null;
		URL url = null;

		try {
			if (mAPNType != null
					&& (mAPNType.equalsIgnoreCase("cmwap")
							|| mAPNType.equalsIgnoreCase("uniwap") || mAPNType
							.equalsIgnoreCase("3gwap"))) {
				String doMain = Match(httpurl, "//[^/]+").replace("//",
						"");
				url = new URL(httpurl.replace(doMain, "10.0.0.172"));
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("X-Online-Host", doMain);
			} else if (mAPNType != null && (mAPNType.equalsIgnoreCase("ctwap"))) {
				String doMain = Match(httpurl, "//[^/]+").replace("//",
						"");
				url = new URL(httpurl.replace(doMain, "10.0.0.200"));
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("X-Online-Host", doMain);
			} else {
				url = new URL(httpurl);
				conn = (HttpURLConnection) url.openConnection();
			}
		} catch (Exception e) {
			return null;
		}

		return conn;
	}





	/**
	 * 检查网络连接 ，这个给mHaveNet 和 mAPNType赋值！
	 * 
	 * @param context
	 */
	public void checkNetwork(Context context) {
		if (null == context)
			return;

		ConnectivityManager cwjManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cwjManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) // 有联网
		{
			mHaveNet = true;
			setNetType(info.getTypeName()); // cmwap/cmnet/wifi/uniwap/uninet/HSDPA
			mAPNType = info.getExtraInfo();
			// setIP(IpAddress.GetIP(context));
			return;
		}
		mHaveNet = false;
		return;
		// 如果为True则表示当前Android手机已经联网，可能是WiFi或GPRS、HSDPA等等，具体的可以通过
		// ConnectivityManager 类的getActiveNetworkInfo() 方法判断详细的接入方式。
	}

	/**
	 * 获取运营商
	 * 
	 * @param context
	 * @return
	 */
	public static String getMobileNet(Context context) {
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		/**
		 * 获取SIM卡的IMSI码 SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile
		 * Subscriber Identification Number）是区别移动用户的标志，
		 * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
		 * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，
		 * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。
		 * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
		 */
		String imsi = telManager.getSubscriberId();
		if (imsi != null) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
				return "中国移动";
			} else if (imsi.startsWith("46001")) {
				return "中国联通 ";
			} else if (imsi.startsWith("46003")) {
				return "中国电信 ";
			}
		}
		return "";
	}

	/**
	 * 是否CMWAP连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isCmwapType(Context context) {
		Http httpB = new Http();
		httpB.checkNetwork(context);
		return httpB.isCmwapType();
	}

	/**
	 * http请求Get
	 * 
	 * @param context
	 * @param httpurl
	 * @param params
	 * @param dealInputStream
	 *            : 在这个回调中处理 返回的流
	 * @return
	 */
	public void get(Context context, String httpurl, String params,
			OnDealConnection dealInputStream) {
		httpurl = httpurl.replaceAll(" ", "%20");
		if (params != null)
			params = params.replaceAll(" ", "%20");

		checkNetwork(context);
		if (!mHaveNet) {
			setIsError(true);
			// setErrorMsg("没有网络");
			return;
		}
		if (null != params && !params.equals("") && !params.equals(" ")) {
			httpurl = httpurl + "?" + params;
		}

		// CMessage.Show("HttpB get():" + httpurl);
		HttpURLConnection conn = getHttpURLConnection(context, httpurl,
				isCmwapType());
		if (null == conn) {
			return;
		}
		try {
			// conn.setDoInput(true);
			// conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			// conn.setRequestProperty("Referer", strUrl);
			conn.setRequestProperty("Charset", "GBK");
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");

			if (null != dealInputStream) {
				InputStream mInputStream = conn.getInputStream();
				dealInputStream.dealConnection(mInputStream);
			}

			setRespondCode(conn.getResponseCode());
			conn.disconnect();
			setIsError(false);
		} catch (Exception e) {
			setIsError(true);
			setErrorMsg(e.toString());
		}

		return;
	}


	/**
	 * 处理流
	 * 
	 * @author Administrator
	 * 
	 */
	public interface OnDealConnection {
		public void dealConnection(InputStream is);
	}

	/**
	 * 上传状态回调
	 * 
	 * @author Administrator
	 * 
	 */
	public interface OnPostedListener {
		public void posted(long postedSize, long totalSize);
	}

	/**
	 * 设置 HttpURLConnection的标准参数
	 * 
	 * @param conn
	 * @return
	 */
	private boolean setPostConn(HttpURLConnection conn) {
		conn.setReadTimeout(3600 * 1000); // 缓存的最长时间
		// conn.setDoInput(true);// 允许输入
		// conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false); // 不允许使用缓存
		try {
			conn.setRequestMethod("POST");
		} catch (ProtocolException e) {
			e.printStackTrace();

			return false;
		}
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Charsert", "GBK");
		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
				+ ";boundary=" + BOUNDARY);
		conn.setChunkedStreamingMode(1024 * 2014 * 2);

		return true;
	}

	/**
	 * 可在一些循环中加入此参数，以在其他线程中控制循环是否继续
	 * 
	 * @param isLetGo
	 */
	public void setLetGo(boolean isLetGo) {
		mIsLetGo = isLetGo;
	}

	public boolean isLetGo() {
		return mIsLetGo;
	}

	/**
	 * getter/setter
	 */
	public void setNetType(String netType) {
		this.mNetType = netType;
	}

	public String getNetType() {
		return mNetType;
	}

	public void setIsError(boolean isError) {
		this.mIsError = isError;
	}

	public boolean isError() {
		return mIsError;
	}

	public void setErrorMsg(String errorMsg) {
		this.mErrorMsg = errorMsg;
	}

	public String getErrorMsg() {
		return mErrorMsg;
	}

	public void setIP(String iP) {
		this.mIP = iP;
	}

	public String getIP() {
		return mIP;
	}

	public void setRespondCode(int respondCode) {
		this.mRespondCode = respondCode;
	}

	public int getRespondCode() {
		return mRespondCode;
	}

	public int getConnectTimeout() {
		return mConnectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.mConnectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return mReadTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.mReadTimeout = readTimeout;
	}

	public boolean haveNet() {
		return mHaveNet;
	}

	public void setHaveNet(boolean haveNet) {
		this.mHaveNet = haveNet;
	}

	public String getAPNType() {
		return mAPNType;
	}

	public void setAPNType(String APNType) {
		this.mAPNType = APNType;
	}

	public boolean isCmwapType() {
		return (null == mAPNType) ? false : mAPNType.equalsIgnoreCase("cmwap");
	}
}
