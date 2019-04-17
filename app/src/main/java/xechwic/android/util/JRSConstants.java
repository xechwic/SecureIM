package xechwic.android.util;

import xechwic.android.FriendControl;

/**
 * 常量
 * @author luman
 *
 */
public class JRSConstants {

	public static final int CHECK_LIVE_TIME=30000;//黑屏检测应用在前端时间间隔
	public static final int SHORT_TIME=30000;///亮屏ALARM时间
	public static final int LONG_TIME=600000;///黑屏ALARM时间,10分钟
	public static final int BLACK_SCREEN_TIME=5*60*1000;//亮起黑色屏幕时间

	public final static String DATA="data";//数据key
	public static final int REFRESH_DELAY=300;//刷新延时
	public static final int SECOND_VALUE=1000;//秒
	public static final int MAX_MSG_LENGTH=1000;//最多文字限制

	public static final String CMD_ACTION_SNAPCHAT="snapchat";//阅后即焚
	public static final String CMD_ACTION_STOPSNAPCHAT="stop_snapchat";//停止阅后即焚
	public static final String CMD_ACTION_STOP="STOP";//停止服务
	public static final String CMD_ACTION_START_IN_CALL="START_IN_CALL";//来电
	public static final String CMD_ACTION_DO_CHECK="DO_CHECK";//检测
	public static final String CMD_ACTION_START_REMOTE="START_REMOTE";//启动远程守护
	public static final String CMD_ACTION_NOTIFICATION_ON="notification_on";//生成应用通知图标
	public static final String CMD_ACTION_NOTIFICATION_OFF="notification_off";//关闭应用通知图标
	public static final String CMD_STARTUP_LOGINUI="START_LOGONUI";//启动登录界面
	public static final String CMD_STARTUP_FRIENDCONTROL="START_FRIENDCONTROL";//启动主界面
	public static final String CMD_ACTION_AUTOLOGIN="AUTO_LOGIN";//登录
    public static final String CMD_ACTION_FINISH="FINISH";//关闭界面
	public static final String CMD_ACTION_LOCALBROADCAST="xechwic.android.broadcast";//本地广播

	////请求忽略电池优化
	public static final int REQ_IGNORE_BATTERY_CODE=10;

	////16进制编码
    public static final String HEX_PRE="hex_";
	//加密文件标识
	public static final String ENCRYPT_END="_en";
	///界面数据保存
	public static final String SAVE_STATE="save_state";
	///界面index
	public static final String SAVE_INDEX="save_index";
	////列表展开组项
	public static final String EXPAND_INDEX ="expand_index";

	//输入状态 0文字输入，1语音输入
	public static final String KEY_INPUT_STATUS="key_input_status";
	////黑屏时间
	public static final String KEY_SCREEN_OFF ="key_screen_off";
    /////黑屏前网络连接
	public static final String KEY_CONNECT_STATUS="key_connect_status";
	/////用户账号
	public static final String KEY_USER_ACCOUNT="key_user_account";
	/////密码
	public static final String KEY_USER_PASSWORD="key_user_password";
	/////防止黑屏开关
	public static final String KEY_SCREEN_SWITCH="key_screen_switch";
	////进程名
	public static final String KEY_PROCESS_NAME="key_process_name";
	////摄像头前后置
	public static final String KEY_CAMERA_FACING="key_camera_facing";
	////自动登录
	public static final String KEY_AUTO_LOGIN="key_auto_login";


	////消息通知
	public static final int NOTICE_MSG= FriendControl.MSGLIST_INDEX;
    ////电话通知
	public static final int NOTICE_CALL=FriendControl.CALLRECORD_INDEX;
	////进入视频
	public static final int NOTICE_VIDEO_DISPLAY=3;

	/////电话消息前缀
	public static final String MSG_CALL_PRE="[incall]";
	public static final String VIDEO_URI_KEY="video_uri_key";


	public static final String KEY_XIM_IP="key_xim_ip";
}
