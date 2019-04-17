/* 数据存储类
 * 存储在单例的Application中
 * 负责供应通讯数据给View 
 * */
package xechwic.android;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sinowave.ddp.Apm;
import com.sinowave.ddp.ApmModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ydx.securephone.R;
import xechwic.android.act.MainApplication;
import xechwic.android.act.ServerConfig;
import xechwic.android.bean.BeanOperate;
import xechwic.android.bean.ChatHistoryBean;
import xechwic.android.bean.ChatMsgEntity;
import xechwic.android.bean.ChatRefreshBean;
import xechwic.android.bean.HeadBean;
import xechwic.android.bean.RecordBean;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.AvatarUpdateEvent;
import xechwic.android.bus.event.LoginEvent;
import xechwic.android.sqlite.ChatHistoryDB;
import xechwic.android.sqlite.ContactDB;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.sqlite.MessageDB;
import xechwic.android.ui.BaseUI;
import xechwic.android.ui.CommandUI;
import xechwic.android.ui.InCallUI;
import xechwic.android.util.ApmConfig;
import xechwic.android.util.AppConfig;
import xechwic.android.util.Base64Util;
import xechwic.android.util.FileUtil;
import xechwic.android.util.FileUtils;
import xechwic.android.util.Http;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.NetTaskUtil;
import xechwic.android.util.NotificationUtil;
import xechwic.android.util.PrefsUtils;
import xechwic.android.util.RSAUtil;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.UriConfig;
import xechwic.android.util.Util;
import xechwic.android.util.XWDataCenterMessage;

import static com.sinowave.ddp.Apm.AGC_Mode.AdaptiveDigital;
import static com.sinowave.ddp.Apm.VAD_Likelihood.LowLikelihood;
import static xechwic.android.util.NotificationUtil.notificationMsg;
import static xechwic.android.util.XWDataCenterMessage.MSG_3;
import static xechwic.android.util.XWDataCenterMessage.MSG_300;
import static xechwic.android.util.XWDataCenterMessage.MSG_4;
import static xechwic.android.util.XWDataCenterMessage.MSG_47;

public class XWDataCenter {
	private String TAG=XWDataCenter.class.getSimpleName();
	/////////////////////包名,用于创建文件访问目录
	public static String PackageName= "xechwic.android";


	private static MessageDB msgDB=null;//聊天信息数据库
	 static ChatHistoryDB chatHistoryDB=null;//聊天历史数据库
	private static FriendNodeDB friendDB;//好友数据库
	private static ContactDB contactDB=null;



	public static XWDataCenter xwDC=null;
	public final static int CPUPartition=900;//低于此主频的CPU远程跟本地图像显示为1秒1帧
	static final int frequency = 8000;//每秒采样8000次,40毫秒采样320次,每次2个字节
	 static int EditionType=1;//1为真机版,0为模拟器版,区别在于是否打开音频设备及摄像头操作相关
	public int default_login_status=-1;//默认登录状态,联机,离开,脱机
	public boolean isLogin=false;//是否已登录过
	public boolean isAudioRunning=false;//音频是否运行
	public boolean needOpenVideo=true;//是否打开视频功能
	public boolean bIsSpeakerOn=false;//扩音器打开
//	public int SDK_Version=7;
    private float CPUFrequency=800.00f;//当前CPU主频
	//////public boolean isFriendLoadFinish=false;//用于是否完成下载所有好友信息
	public boolean bIsLoginRunning=false;
	public boolean isConnected=true;//是否已连接,用于在用户使用过程中与服务器断开的相关提示
	public boolean cameraRunning=false;//当前是否在进行音视频通话,用于当正在音视频通话时用户按了Home键,再返回视频通话界面时的相关判断
	public boolean remoteVideoRunning=false;//用于控制远程图像线程
	public String calling_loginName="";//当前正在进行网络通话的好友号码(loginName),0表示空闲
	public List<FriendGroupInfo> groupsInfo;//所有的分组数据信息
	 List<FriendNodeInfo> nodesInfo;//所有的好友节点信息
	public LinkedList<MessageParamA> msgParamList=null;//系统消息队列

	 static long lLoginBeginTime=0;

	 String sRASKeyPath="";
	public String sPubkeyFile=null;
	private String sPrivatekeyFile=null;



	//////////////////2014-07-09,解决本地为空,不能保存这个!!!!!!!!!!2014-07-09
	//////public static FriendNodeInfo fni;//本账户信息点

	public static  HashMap<String, String> headBeanMap= null;//头像名列表
	////////////////////////
	public String loginName=null;//当前用户登录账号,在登录完成后赋值
	 String password=null;//当前用户登录账号,在登录完成后赋值

	public int cid;//当前用户id,下载好友时赋值
	 int currentAccountValue=0;//保存当前用户的余额
	private XWAudioPlay audioPlay=null;//声音播放设备
	 XWAudioRecord xwAudioRecord=null;//声音录制设备
	 Camera mCamera=null;//摄像头设备
	//public RemoteVideoSurface remoteVideo=null;
	public long netPhoneTime=0;//音视频通话时长,单位秒
	 Thread remoteVideoThread=null;
	 SystemInfo sysInfo=new SystemInfo();
	public native void initSystem(ByteBuffer audioBuffer,ByteBuffer videoBuffer);// 初始化系统
	public native int updateFNInfo(int id,byte[] signName,byte []status,byte []groupName,byte []flag);
	public native int queryOnlineFriend();
	public native int queryFriendForCondition(byte []name,byte []signName,int number,byte []email);
	public native int manageFN(byte []opType,int id,byte []name,byte []groupName,byte sReason[]);//管理好友
	public native int remarkFNSignName(int id,byte []groupName,byte []newGroup,byte []loginName);
	//聊天记录相关函数
	public native void clearRecord(int uid,int fid);//删除聊天记录
	public native void insertRecord(int uid,int fid,byte[] nickname,byte[] ctime,byte[] content,int flag);
	public native int getTotalRecordCount(int uid,int nodeID);
	public native String getCurrentPageHTML(int uid,int nodeID,int page,int eachPageCount);
	public native void beginQueryRecord(int uid,int nodeID,int page,int eachPageCount);//开始查询记录

	public synchronized native void logoutService(int needCallBack);
	public synchronized native int videoRequest(int fid,byte []loginName);
	public synchronized native void hangupNetPhone();
	public synchronized native void acceptNetPhoneReq();

	////////////////////2014-05-05,增加traceIndex参数,是33字节byte,null结尾的字串
	public native int sendMessage(int sender,int receiver,byte []content,byte []sendTime,byte []traceIndex);

	////////////////////2014-05-05,增加traceIndex参数,是33字节byte,null结尾的字串
	public native int sendOnlineMessage(int sender,int receiver,byte []content,byte []sendTime,byte []traceIndex);

	////////////////////2014-05-05,增加traceIndex来异步查询消息发送状态的字串.
	////返回:	/// -1:不存在. 0:等待接受,1:正在传输,2:停止,3:出错,>=10:成功.
	public native int queryMessageStatus(byte []traceIndex);

	////////////////////2014-05-05,增加查询发送通道是否空闲.
	////返回:	1：空闲, 0:忙
	public native int IsCommIdle(byte []sToUser);

	////////////////////2014-05-05,增加设置是否开屏关屏.
	////参数:iMode, 1:亮屏. 0:关屏
	public native int setScreenOpen(int iMode);

	////////////////////2014-06-10,增加设置视频对方图像尺寸.
	////参数:iMode, 1:亮屏. 0:关屏
	public native int XWNetphoneSetShowVideoSize(int iwidth,int iheight);

	////////////设置是否旋转图像,iRotate为1则旋转。
	public native int XWNetphoneSetIsRotate(int iRotate);

	////////////////////获取web认证序列号 http://xxx?username=xxx&serial=xxx
	/////sAuthenSerial是33字节的字节数组
	public native int getWEBAuthenSerial(byte []sAuthenSerial);


	public native void sendVideoData(ByteBuffer data /*byte []data*/,int len);//发送视频数据
	////public native int getLoginUser(byte []number,byte passwd[]);
	//	public native void saveLoginUser(byte []number,byte passwd[],int login_status);
	public synchronized native int loginServer(byte []address,byte []port,byte number[],byte passwd[]);
	public synchronized native void cancelLogin();
	public native int groupManager(int userid,byte []groupName,byte []newGroupName,byte []opType);
	public native int getVAideoStatus(int type);//检查音视频状态,1为检查视频,2为检查音频
	public native void setCameraFormat(int pixMat);//设置摄像头格式
	public native void setVideoRect(int width,int height,int rate);//设置要发送的视频数据尺寸
	///public native void sendAudioData(byte []data,int len);

	public native void setAudioDataBuffer(ByteBuffer data/*byte []data*/);

	public native void sendAudioDataForSplit(/*ByteBuffer data*/byte []data,int len);

	public native int getAudioPlayBufferLen();//返回音频缓冲实际长度
	public native void setAudioPlayBufferLen();//设置音频缓冲实际长度为0
	public native int getRemoteVideoData();// 获取远程图像数据
	//public native void writeAudioDataToBuffer(byte [] data,byte [] bufArea,int cLen,int dataLen);
	////public native void readAudioDataFromBuffer(byte[] bufArea,int cLen,int eachLen);
	public native void getSystemInfo();
	public native void updateSystemInfoServer(byte []server_addr,int server_port);
	public native void updateSystemInfoVs(int audio_volume,int record_save_days);
	public native void retainRecord(int save_days);//删除过期的聊天记录
	public native void dialKeyPress(byte []ch);
	public native void initCompress(int srcWidth,int srcHeight,int destWidth,int destHeight);//初始化图像压缩
	public native void destroyCompress();//释放图像压缩
	public native void changPasswdSignName(int uid,byte []loginName,byte []signName,byte []passwd);
	public synchronized native void reActive(int iOn);
	public native void NetPhoneClientDataLock();
	public native void NetPhoneClientDataUnlock();

	////////////////设置cpu是否忙
	public native void SetCPUBusy(int iOn);

	public native void SetLoginStatus(byte []loginstatus);

	public String sLoginStatus="";



	////////////////////微信函数,2012-11-12

	public native int xwg729ainitencoder();
	public native int xwg729adestroyencoder(int state);
	public native int xwg729aencoder(int state,byte[] speech, byte[] bitstream);

	public native int xwg729ainitdecoder();
	public native int xwg729adestroydecoder(int state);
	public native int xwg729adecoder(int state,byte[] bitstream,byte[] speech);

	//////////////////////VAD detection
	public native int xwVADinit();
	public native int xwVADdestroy(int state);
	public native int xwVAD(int state,byte[] speech);


	////////////////////////请求临时对话,2013-07-16	
	public native int XWRequestTempTalk(byte[] sFromUser,int FromUserID,byte[] sToUser,int ToUserID);

	public native int XWRequestHelper(byte[] sHelperID);

	public native int XWGetTerminalType(byte[] sTerminalID);

	//////////////////////////说明,取当前是否与XIM服务器连接, 1为已连接，其它为错误。
	synchronized public  native int XIMGetConnectStatusToXIM();

	//////////////////////////2016-01-31,设置服务器通讯服务ip和端口
	public native int setServerIPPort(byte[] sIP,int iPort);

	private String sHelperID="";

//	public FriendChatRecord chatActivity=null;


	//////////////跟踪与服务器连接状态
	 int iServerConnectStatus=-1; ///////0断开,1正在连,2已连接

	////////主动亮起黑色屏幕
	 boolean bWakeUpBlackScreen =false;

	 List listGotMsg=null;

	////public List listSaveMsg=null;

	public long tLastRefreshRecord=0;

	 static String sProgPath="";
	/////////////////////设置当前程序路径
	public native int  setcurrentPackPath(byte[] sPackPah);


	/////////////////////设置是否打开日志，在setcurrentPackPath之后设置。2017-10-16
	public native int  setOpenLogOn();

	public static boolean bIsVerticalScreen=true;

	public String sCurrentPhoneNumber="";////当前与我通话的人（包括通话中各种状态，拨打，接通，挂断)


	//////////////////传递参数时所有整型都要转成bytes, 调用完后byte[]中存int结果的，要转成int.

	 static byte[] XWIntToBytes(int n)
	{
		byte[] b = new byte[4];
		for(int i = 0;i < 4;i++){
			b[i] = (byte)( (n >> (24 - i * 8))  & 0xff);
		}
		return b;
	}


	 static int XWBytesToInt(byte[] b)
	{
		return (((int)b[0]) << 24) | (((int)b[1]) << 16) | (((int)b[2]) << 8) | b[3];
	}

	//////////////////发送微信文件. sToUser为目标用户userid,sFilePath为文件地址, pOfflineFlag是一个整型的内存存放,为1表示是离线发送
	////////////////////pOfflineFlag是整型的内存存放,输入时要用byte[4], 返回后用XWBytesToInt函数转换成int型
	public native int XWWeiXinRequestSendFile(byte[] sToUser,byte[] sFilePath,byte[]  pOfflineFlag);


	//////////////////发送微信文件. sToUser为目标用户userid,sFilePath为文件地址,
	////////////////////pStatus,pProgress是整型的内存存放,输入时要用byte[4], 返回后用XWBytesToInt函数转换成int型
	/////////////返回的pStatus中存的是发送状态代码:
	////////////                              0:等待接受
	////////////                              1:正在发送,pProgress会变化
	////////////                              2:对方停止
	////////////                              3:出错
	////////////                              >10, 10，11，12等表示成功发送
	////////////                              其它,未知错误
	public native int XWWeiXinQuerySendFileStatus(byte[] sToUser,byte[] sFilePath,byte[]  pStatus,byte[]  pProgress);



	//////////////////报告位置, sLan字串格式的Lantitude,sLon字串格式的Longtitude,sAddress地址
	public native int XWReportPosition(byte[]  sLan,byte[]  sLon,byte[]  sAddress);

	//////////////////查询位置附近的人, sLan字串格式的Lantitude,sLon字串格式的Longtitude
	public native int XWRequetQueryNearFriends(byte[]  sLan,byte[]  sLon);

	// /////////////////////留言板
	// ////////////////发送留言板.
	// // iType: 0,文字 ; 1:文件
	// // sContent: 文字消息或文件名.
	// /// fLon，fLat: 当前的经度和纬度
	// //// byte sTraceIndex[33] , 33字节字串,用于追踪发送的状态
	public native int XWBroadRequestSendFile(int iType, byte[] sGroup, byte[] sContent,
			byte[] sLon, byte[] sLat, byte[] sTraceIndex);

	// ////////////////查询留言板发送状态,
	// ////////////////sTraceIndex, 33字节字串。由 XWBroadRequestSendFile 在发送时得到。
	// //////////////////pStatus,pProgress是整型的内存存放,输入时要用byte[4],
	// 返回后用XWBytesToInt函数转换成int型
	// ///////////返回的pStatus中存的是发送状态代码:
	// ////////// 0:等待接受
	// ////////// 1:正在发送,pProgress会变化
	// ////////// 2:对方停止
	// ////////// 3:出错
	// ////////// >10, 10，11，12等表示成功发送
	// ////////// 其它,未知错误
	public native int XWBoardQuerySendFileStatus(byte[] sTraceIndex,
			byte[] pStatus, byte[] pProgress);




	// /////////////////////留言板
	// ////////////////开始发送实时留言
	/////xwRequestSendRealTimeFile
	////sFileName,文件名
	// // iType: 0,文字 ; 1:文件
	// /// fLon，fLat: 当前的经度和纬度
	// //// byte sTraceIndex[33] , 33字节字串,用于追踪发送的状态
	public native int xwRequestSendRealTimeFile(byte sFileName[],byte sLon[],byte sLat[],byte sTraceIndex[]);

	// ////////////////
	///xwSendRealTimeFileData，发送留言数据
	// ////////////////sTraceIndex, 33字节字串。由 XWBroadRequestSendFile 在发送时得到。
	///Data,发送的数据
	public native int xwSendRealTimeFileData(byte[] sTraceIndex,
			byte[] Data);

	// ////////////////
	///xwStopRealTimeFile.数据结束
	// ////////////////sTraceIndex, 33字节字串。由 XWBroadRequestSendFile 在发送时得到。
	///Data,发送的数据
	public native int xwStopRealTimeFile(byte[] sTraceIndex);



	// /////////////////////群聊天
	// ////////////////发送群聊天消息.
	// // iType: 0,文字 ; 1:文件
	// // sContent: 文字消息或文件名.
	// /// fLon，fLat: 当前的经度和纬度
	// //// byte sTraceIndex[33] , 33字节字串,用于追踪发送的状态
	public native int XWXIMGroupChatRequestSendFile(int iType, byte[] sGroup, byte[] sContent,
											 byte[] sLon, byte[] sLat, byte[] sTraceIndex);

	// ////////////////查询群聊消息发送状态,
	// ////////////////sTraceIndex, 33字节字串。由 XWBroadRequestSendFile 在发送时得到。
	// //////////////////pStatus,pProgress是整型的内存存放,输入时要用byte[4],
	// 返回后用XWBytesToInt函数转换成int型
	// ///////////返回的pStatus中存的是发送状态代码:
	// ////////// 0:等待接受
	// ////////// 1:正在发送,pProgress会变化
	// ////////// 2:对方停止
	// ////////// 3:出错
	// ////////// >10, 10，11，12等表示成功发送
	// ////////// 其它,未知错误
	public native int XWXIMGroupChatQuerySendFileStatus(byte[] sTraceIndex,
												 byte[] pStatus, byte[] pProgress);


////////////////////2017-09-01,增加群组聊天功能,如果群名以如下开始，则是群组聊天消息。

	public static final String XIM_IM_GROUPCHAT_PREX="\u0002GROUPCHAT\u0003";    /////////群名前缀,如果组名前缀为它，则为群聊天消息
	public static final String XIM_IM_GROUPCHATSYSTEMNOTICE_PREX="\u0002GROUPNOTICE\u0003";  ///////群通知内容前缀,如果聊天消息内容前缀为它，则为群通知


	/////////////////设置通话时密钥32位以内字符
	public native int XWNetphoneSetPhoneAESPassword(byte[] sPassword);
	///////////////加密,sDst长度要大于sSrc长度+16 *2 ,输出为字符串
	public native int XWNetphoneAESEncodeText(byte[] sSrc,byte[] sDst,byte[] sPassword);
	///////////////解密
	public native int XWNetphoneAESDecodeText(byte[] sSrc,byte[] sDst,byte[] sPassword);

	///////////////加密,对字节加密，输出为二进制字节
	public native int XWNetphoneAESEncodeBytes(byte[] sSrc,byte[] sDst,byte[] sPassword);
	///////////////解密，输出为二进制字节
	public native int XWNetphoneAESDecodeBytes(byte[] sSrc,byte[] sDst,byte[] sPassword);

	//////////////////2016-10-18要保持和网关连接.这样才能保证离线文件传输.
	public native int keepconnecttogateway();



	////////////////////////2014-10-15
	public static String sCallerNetphoneKey=null;
	public static String sCallerNetphoneKeyTimeStamp=null;
	public static String sCalleeNetphoneKey=null;
	public static long lLastRequestNetphoneKey=0;


	public AcousticEchoCanceler audio_echocanceler=null;


	public synchronized static void NewNetPhoneAESTalk()
	{
		sCallerNetphoneKeyTimeStamp=RSAUtil.getTimeStamp();
		sCallerNetphoneKey=RSAUtil.generateAESKey(null);
		sCalleeNetphoneKey=null;
	}

	////////////////////交换密钥文字消息前缀
	public final static String NETPHONEAES_PREFIX="\u0003NETPHONEAES\u0003";
	public synchronized static void RequesNetPhoneTalk(String sToUser)
	{
		///////////////
		String sCommand=NETPHONEAES_PREFIX+"\r";
		///////////////
	}

	public native int XWNetphoneEchoPlayed(byte[] sPlayedData,int iPos,int iLen);


	public native int setIsUseEchoCancel(int isUseSoftEchoCancel);

	/////////////2017-03-10,设置底层是否要倒置图像
	public native int setCamaraIsToUPDown(int isCameraUpDown);

	//////////////////开始录微信音频,最多能录60秒长度.
	public synchronized int XWStartWeiXinAudio(String sToUser)
	{
		boolean bNeedStartAudioRecord=false;

		/*if ((audioPlay!=null)||(xwAudioRecord!=null))
		{
			return;
			///stopXWAudio();			
		}*/
		bNeedStartAudioRecord= (xwAudioRecord==null);

		Log.e("xim", "StartWeiXinAudio");

		if (bNeedStartAudioRecord)
		{
			xwAudioRecord=new XWAudioRecord(this);

			xwAudioRecord.sWeiXinToUser=sToUser;
			xwAudioRecord.bIsWeiXin=true;
			xwAudioRecord.start();

			Log.e("XIM","XWStartWeiXinAudio");
		}
		else
			return -1;

		return 0;
	}


	//////////////////开始录微信音频,最多能录60秒长度.
	public synchronized int XWStartWeiXinAudioWithTimeLimit(String sToUser,int iSecLimit)
	{
		boolean bNeedStartAudioRecord=false;

		/*if ((audioPlay!=null)||(xwAudioRecord!=null))
		{
			return;
			///stopXWAudio();			
		}*/
		bNeedStartAudioRecord= (xwAudioRecord==null);

		Log.e("xim", "StartWeiXinAudio");

		if (bNeedStartAudioRecord)
		{
			xwAudioRecord=new XWAudioRecord(this);

			xwAudioRecord.sWeiXinToUser=sToUser;
			xwAudioRecord.bIsWeiXin=true;
			xwAudioRecord.imaxSecond=iSecLimit;
			xwAudioRecord.start();

			Log.e("XIM","XWStartWeiXinAudio");
		}
		else
			return -1;

		return 0;
	}

	//////////////停止微信录音并进行发送.
	////////////////////返回微信文件路径
	public synchronized String XWStopWeiXinAudio()
	{
		String sWeiXinFile=null;
		/////////////////音频退出.
		isAudioRunning=false;

		Log.e("xim", "StopWeiXinAudio");
		try{
			if(xwAudioRecord!=null)
			{
				try
				{
					xwAudioRecord.stopAudioRecord();

					sWeiXinFile=xwAudioRecord.sWeiXinFilePath;


				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}

				xwAudioRecord=null;
			}

		}catch(Exception e){
			e.printStackTrace();
			xwAudioRecord=null;
		}


		return sWeiXinFile;
	}

	synchronized public int XWPlayWaveBuffer(byte[] btPlay,int Len)
	{
		{
			XWPlayThread playthread=new XWPlayThread(btPlay,Len);
			playthread.start();
		}

		return 0;
	}

	public int XWPlayWeiXinAudioFile(String sFilePath)
	{
		try
		{
			int iDecoder;
			byte[] btWeiXinToDecode=new byte[8000*2*60/16];
			int    iToDecodeLen=0;
			byte[] btWeiXinDecoded=new byte[8000*2*60];
			int    iDecodedLen=0;
			byte[] btWeiXinDecodeBuff=new byte[10];
			byte[] btWeiXinDecodedBuff=new byte[160];


			File file = new File(sFilePath);
			FileInputStream inStream=null;
			if (!file.exists())
			{
				Log.e("XIM","XWPlayWeiXinAudioFile error not found :"+sFilePath);
				return -1;
			}

			inStream = new FileInputStream(file);

			try
			{
				Log.e("XIM","XWPlayWeiXinAudioFile 1 "+sFilePath);
				iToDecodeLen=inStream.read(btWeiXinToDecode, 0, btWeiXinToDecode.length);
				Log.e("XIM","XWPlayWeiXinAudioFile 2"+sFilePath);

				Log.e("XIM","XWPlayWeiXinAudioFile "+sFilePath);

				if (iToDecodeLen>=10)
				{
					int i;
					iDecoder=xwg729ainitdecoder();


					for (i=0;i<iToDecodeLen/10;i++)
					{
						System.arraycopy(btWeiXinToDecode, i*10, btWeiXinDecodeBuff, 0, 10);

						xwg729adecoder( iDecoder,btWeiXinDecodeBuff,btWeiXinDecodedBuff);

						System.arraycopy(btWeiXinDecodedBuff, 0, btWeiXinDecoded, i*160, 160);

						iDecodedLen+=160;

						if (iDecodedLen+160>btWeiXinDecoded.length)
							break;
					}

					xwg729adestroydecoder(iDecoder);

				}
			}
			finally
			{
				if (inStream!=null)
					inStream.close();
			}


			///////////播放
			if (iDecodedLen>0)
			{
				XWPlayWaveBuffer(btWeiXinDecoded,iDecodedLen);
				Log.e("XIM","XWPlayWaveBuffer "+iDecodedLen);
			}
		}
		catch(Exception e)
		{
			Log.e("XIM","XWPlayWeiXinAudioFile error "+e.getMessage());
		}
		return 0;
	}


	public static BaseUI xwContext=null;

	final public static int video_basic_width=320;//本地视频宽
	final public static int video_basic_height=240;//本地视频高

	public static int video_preferred_width=0;//本地视频宽
	public static int video_preferred_height=0;//本地视频高


	public static int video_compress_width=0;//本地压缩后宽
	public static int video_compress_height=0;//本地压缩后高
	public static int video_preferred_rate=0;//本地摄像头支持的最低帧频
	public static int xw_video_fps=10;//本地实际发送帧频
	public static int video_preferred_format=0;//本地摄像头支持的视频数据编码格式
	public static float xw_video_scale=0.75f;
	public static boolean video_is_open=false;//视频,默认打开
	public static boolean audio_is_open=true;//音频,默认打开
	public int remote_video_width=0;//远程视频图像宽
	public int remote_video_height=0;//远程视频图像高
	private int remote_video_fps=10;//远程图像帧频

	public int remote_video_codec=-1; //////////////对方视频编码

	public int remote_audio_codec=-1;

	// public byte[] audioDataBuffer;//音频播放缓冲区
	 ByteBuffer audioDataBuffer;// 音频播放缓冲区

	 ByteBuffer videoDataBuffer;// 音频播放缓冲区
	 int ivideoPicLen=0;

	 byte[] videlLocalBuffer=null;

	public List<Activity> activityList;
	public byte []xwSysAudio;
	public byte []xwMsgAudio;
	public byte []xwCallAudio;

	 MainApplication xwApp;
	public SmileyParser parser;
	public int []recordSaveDays=new int[]{3,5,7};
	 StringBuffer timeSB;
	 StringBuffer accountSB;
	 DecimalFormat fformat;
	public int sendMessageID=0;


	private static boolean displayVideoTimeRunning=false;

	public static int iNetphoneStatus=0;//0空闲，1，开始拨号，2正在拨号，3拨通，5挂断，11来电，12,13

	 public XWDataCenterHandler XWMsghandle=null;






	private float getCPUFrequency(){
		ProcessBuilder cmd;
		float frequency=400.00f;
		try {
			String[] args={"/system/bin/cat","/proc/cpuinfo"};
			cmd = new ProcessBuilder(args);
			java.lang.Process process = cmd.start();
			InputStream in = process.getInputStream();
			int c;
			StringBuffer sb=new StringBuffer();

			while((c=in.read())!=-1){
				sb.append((char)c);
			}

			//Log.e("tag", sb.toString());
			String cpuInfo=sb.toString();
			String cpuInfos[]=cpuInfo.split("\n");
			for(int i=0;i<cpuInfos.length;i++){
				//Log.e("tag", cpuInfos[i]);
				String tmp=cpuInfos[i];
				if(tmp.contains("BogoMIPS")){
					String frequ=(tmp.split(":")[1]).trim();

					frequency=Float.parseFloat(frequ);
					//Log.e("tag", "frequ:"+frequ);
					break;
				}
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return frequency;
	}

	synchronized  void openCamera(){

		if(PrefsUtils.getInstance().get(JRSConstants.KEY_CAMERA_FACING,0)==0){
			Log.e("xim","front");
			XWDataCenter.xwDC.setCamaraIsToUPDown(0);//  前置,
			XWDataCenter.xwDC.openFrontCamera();
		}else{
			Log.e("xim","back");
			XWDataCenter.xwDC.setCamaraIsToUPDown(1); // 后置。
			XWDataCenter.xwDC.openBackCamera();
		}
	}



	synchronized  void openFrontCamera(){
		Class<?> cameraInfoClass=null;
		Class<?> cameraClass =null;
		if (this.mCamera != null) {
			try {
				xwDC.mCamera.setPreviewCallback(null);
				this.mCamera.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.mCamera=null;
		// 检查是否有 Camera 类

		try
		{
			cameraClass=Class.forName("android.hardware.Camera");
		}
		catch (Exception e)
		{
			return;
		}

		try {
			cameraInfoClass=Class.forName("android.hardware.Camera$CameraInfo");
		} catch (ClassNotFoundException e) {
			try
			{
				this.mCamera=Camera.open();
			}
			catch (Exception e1)
			{
				return;
			}
			return;
		}
		try {//Camera.CameraInfo info=new Camera.CameraInfo();
			//注意getNumberOfCameras为静态方法
			Method numberCamerasMethod = cameraClass.getMethod("getNumberOfCameras",new Class[]{});

			if (numberCamerasMethod!=null)
			{
				Integer count=(Integer)numberCamerasMethod.invoke(Camera.class,new Object[]{});
				//Log.e("tag", "camera count:"+count);
				Object cameraInfoObj=cameraInfoClass.newInstance();
				Method getCamerasInfoMethod = Camera.class.getMethod("getCameraInfo",new Class[]{int.class,cameraInfoClass});
				Field frontField=null;
				///if(PrefsUtils.getInstance().get(JRSConstants.KEY_CAMERA_FACING,0)==0)
				{
					frontField=cameraInfoClass.getDeclaredField("CAMERA_FACING_FRONT");
				}
				/*else{
					frontField=cameraInfoClass.getDeclaredField("CAMERA_FACING_BACK");
				}*/

				Field facField=cameraInfoClass.getDeclaredField("facing");
				int frontValue=frontField.getInt(null);//注意是静态field
				for(int i=0;i<count;i++){
					getCamerasInfoMethod.invoke(Camera.class,new Object[]{i,cameraInfoObj});
					int fac=facField.getInt(cameraInfoObj);//该field属于某对象
					//Log.e("tag", "CAMERA_FACING_FRONT:"+frontValue+" facing:"+fac);
					if(frontValue==fac){
						//this.mCamera=Camera.open(i);
						Method openMethod = Camera.class.getMethod("open",new Class[]{int.class});
						this.mCamera=(Camera)openMethod.invoke(Camera.class,new Object[]{i});
						break;
					}
				}
				if(this.mCamera==null){
					this.mCamera=Camera.open();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			try
			{
				this.mCamera=Camera.open();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}



	synchronized  void openBackCamera(){
		Class<?> cameraInfoClass=null;
		Class<?> cameraClass =null;
		if (this.mCamera != null) {
			try {
				xwDC.mCamera.setPreviewCallback(null);
				this.mCamera.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		this.mCamera=null;
		// 检查是否有 Camera 类

		try
		{
			cameraClass=Class.forName("android.hardware.Camera");
		}
		catch (Exception e)
		{
			return;
		}

		try {
			cameraInfoClass=Class.forName("android.hardware.Camera$CameraInfo");
		} catch (ClassNotFoundException e) {
			try
			{
				this.mCamera=Camera.open();
			}
			catch (Exception e1)
			{
				return;
			}
			return;
		}
		try {//Camera.CameraInfo info=new Camera.CameraInfo();
			//注意getNumberOfCameras为静态方法
			Method numberCamerasMethod = cameraClass.getMethod("getNumberOfCameras",new Class[]{});

			if (numberCamerasMethod!=null)
			{
				Integer count=(Integer)numberCamerasMethod.invoke(Camera.class,new Object[]{});
				//Log.e("tag", "camera count:"+count);
				Object cameraInfoObj=cameraInfoClass.newInstance();
				Method getCamerasInfoMethod = Camera.class.getMethod("getCameraInfo",new Class[]{int.class,cameraInfoClass});
				Field frontField=null;
				///if(PrefsUtils.getInstance().get(JRSConstants.KEY_CAMERA_FACING,0)==0)
				/*{
					frontField=cameraInfoClass.getDeclaredField("CAMERA_FACING_FRONT");
				}*/
				{
					frontField=cameraInfoClass.getDeclaredField("CAMERA_FACING_BACK");
				}

				Field facField=cameraInfoClass.getDeclaredField("facing");
				int frontValue=frontField.getInt(null);//注意是静态field
				for(int i=0;i<count;i++){
					getCamerasInfoMethod.invoke(Camera.class,new Object[]{i,cameraInfoObj});
					int fac=facField.getInt(cameraInfoObj);//该field属于某对象
					//Log.e("tag", "CAMERA_FACING_FRONT:"+frontValue+" facing:"+fac);
					if(frontValue==fac){
						//this.mCamera=Camera.open(i);
						Method openMethod = Camera.class.getMethod("open",new Class[]{int.class});
						this.mCamera=(Camera)openMethod.invoke(Camera.class,new Object[]{i});
						break;
					}
				}
				if(this.mCamera==null){
					this.mCamera=Camera.open();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			try
			{
				this.mCamera=Camera.open();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}



	public void initCameraParam(){

		Log.e("XIM","initCameraParam 1");

		Log.e("xim","XWDataCenter initSystem 2");

		try{
			//this.getCamera();
			this.CPUFrequency=this.getCPUFrequency();

			/*if (this.CPUFrequency<1000)
			{
				this.SetCPUBusy(1);				
			}*/
			Log.e("XIM","initCameraParam 2");


			if(XWDataCenter.EditionType>0){

				List backSizeList=null;
				List frontSizeList=null;


				this.openFrontCamera();

				if(this.mCamera==null){
					return;
				}

				try
				{

					Log.e("XIM","initCameraParam 3");

					Camera.Parameters p = this.mCamera.getParameters();

					List pslist=SupportedPreviews.getSupportedPreviewSizes(p);
					frontSizeList=pslist;
					if(pslist!=null){
						Collections.sort(pslist, new PreviewSizeComparator());

						for(int i=0;i<pslist.size();i++){
							Size psSize=(Size)pslist.get(i);
							Log.e("xim", "support Width:"+psSize.width+" Height:"+psSize.height);
							if ((video_preferred_width==0)||(video_preferred_height==0) )
							{
								video_preferred_width=psSize.width;
								video_preferred_height=psSize.height;
							}
							else //////选最小的		//////2014-12-04,取最低分辨率					
								if( ( (psSize.width>=video_basic_width) && (psSize.height>=video_basic_height)) /*&& (psSize.width*psSize.height<video_preferred_width*video_preferred_height   )*/ ){//保证width大于150
									video_preferred_width=psSize.width;
									video_preferred_height=psSize.height;

									break;
									//////break;
								}
							//Log.e("tag", "support Width:"+psSize.width+" Height:"+psSize.height);
						}
						if(video_preferred_width==0){//摄像头不支持大于150宽的尺寸,则取最小的
							Size psSize=(Size)pslist.get(0);
							video_preferred_width=psSize.width;
							video_preferred_height=psSize.height;
						}
					}else{
						video_preferred_width=p.getPreviewSize().width;
						video_preferred_height=p.getPreviewSize().height;
					}
					///////////////////////////
					Log.e("xim","initCameraParam video_preferred_width"+video_preferred_width +" video_preferred_height"+video_preferred_height);

					List fmlist=SupportedPreviews.getSupportedPreviewFormats(p);
					if(fmlist!=null){
						for(int i=0;i<fmlist.size();i++){
							Integer fm=(Integer)fmlist.get(i);
							Log.e("xim", "support format:"+fm);
							if(fm==17){
								video_preferred_format=fm;
								break;
							}
						}
						if(video_preferred_format==0){
							video_preferred_format=p.getPreviewFormat();
						}
					}else{
						video_preferred_format=p.getPreviewFormat();
					}
					///////////////////////////
					Log.e("xim","initCameraParam video_preferred_format"+video_preferred_format);


					this.setCameraFormat(video_preferred_format);


					List ratelist=SupportedPreviews.getSupportedPreviewFrameRates(p);
					if(ratelist!=null){
						Collections.sort(ratelist, new PreviewFrameRateComparator());
						video_preferred_rate=((int)ratelist.get(0));
					}else{
						video_preferred_rate=p.getPreviewFrameRate();
					}

					///////////////////////////
					Log.e("XIM","initCameraParam video_preferred_rate"+video_preferred_rate);


					//////////////////如果是竖屏,则交换坐标,2014-06-30
					/*if (  (bIsVerticalScreen && (video_preferred_width>video_preferred_height))
						 ||
						 (!bIsVerticalScreen && (video_preferred_width<video_preferred_height))
					  )
					{
						int ikeepwidth=video_preferred_width;
						video_preferred_width=video_preferred_height;
						video_preferred_height=ikeepwidth;
						
						Log.v("XIM","Vertical screen , switch width and height");
					}*/

					p.setPreviewSize(video_preferred_width, video_preferred_height);
					///////////////////////////

					p.setPreviewFrameRate(video_preferred_rate);///	



					/*if(this.CPUFrequency<=XWDataCenter.CPUPartition)
					{
						video_preferred_rate=5;		
						/////p.setPreviewFrameRate(video_preferred_rate);///			
					}*/

					/////2012-06-11,实际帧频参数,一般取出的帧频大于15
					///xw_video_fps=  (video_preferred_rate <=5) ? video_preferred_rate : 10;

					/////////////////2012-06-12为特定厂家提供高的fps
					///////实际通讯用的fps为5,最低。
					xw_video_fps=10;

					//p.set("camera-id",3);
					//p.set("video_input","secondary");
					//p.set("samsungcamera", 1);
					//某些厂商并不按标准来,所以要再取
					this.mCamera.setParameters(p);
					this.mCamera.startPreview();
					p=this.mCamera.getParameters();
					if((p.getPreviewSize().width!=video_preferred_width)||(p.getPreviewSize().height!=video_preferred_height)){
						video_preferred_width=p.getPreviewSize().width;
						video_preferred_height=p.getPreviewSize().height;
					}

					Log.e("XIM","initCameraParam set video_preferred_width"+video_preferred_width +" video_preferred_height"+video_preferred_height);

				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					this.mCamera.stopPreview();
					this.mCamera.release();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				this.mCamera=null;

				////////////如果前置取不到,退出
				if (frontSizeList==null)
					return;

				/////打开后置摄像头
				this.openBackCamera();
				if(this.mCamera==null){
					return;
				}
				try
				{
					Log.e("XIM","initCameraParam back camera.");
					Camera.Parameters p = this.mCamera.getParameters();
					List pslist=SupportedPreviews.getSupportedPreviewSizes(p);
					backSizeList=pslist;
					if(pslist!=null){
						Collections.sort(pslist, new PreviewSizeComparator());

						boolean bFound=false;
						for(int i=0;i<pslist.size();i++){
							Size psSize=(Size)pslist.get(i);
							Log.e("xim", "Back camera support Width:"+psSize.width+" Height:"+psSize.height);

								if( ( (psSize.width>=video_basic_width) && (psSize.height>=video_basic_height)) /*&& (psSize.width*psSize.height<video_preferred_width*video_preferred_height   )*/ ){//保证width大于150
									for (int j=0;j<frontSizeList.size();j++)
									{
										Size psSizeFront=(Size)pslist.get(j);
										if ( (psSizeFront.height==psSize.height) && (psSizeFront.width==psSize.width))
										{
											video_preferred_width=psSize.width;
											video_preferred_height=psSize.height;
											bFound=true;

											Log.e("xim", "Back camera support found size Width:"+psSize.width+" Height:"+psSize.height);
											break;
										}
									}
									//////break;
								}
							if (bFound)
								break;
							//Log.e("tag", "support Width:"+psSize.width+" Height:"+psSize.height);
						}
					}
					///////////////////////////
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					this.mCamera.stopPreview();
					this.mCamera.release();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				this.mCamera=null;

				//只要是宽大于320或高大于240一律转成320*240
				video_compress_width=0;
				video_compress_height=0;

			}else{//如果是模拟器
				video_preferred_rate=15;
				video_preferred_width=176;
				video_preferred_height=144;
				if(Build.VERSION.SDK_INT>=9){
					video_preferred_format=17;
				}else{
					video_preferred_format=16;
				}
				/*if(this.CPUFrequency<=XWDataCenter.CPUPartition){
					xw_video_fps=1;
				}*/
				this.setCameraFormat(video_preferred_format);

				Log.e("XIM","initCameraParam simulator set video_preferred_width"+video_preferred_width +" video_preferred_height"+video_preferred_height);

			}


		}catch(Exception e){
			e.printStackTrace();
			/*if(xwContext instanceof FriendLogin){
				((FriendLogin)xwContext).mHandler.sendEmptyMessage(3);
			}*/
			if (this.mCamera!=null)
			{
				try {
					xwDC.mCamera.setPreviewCallback(null);
					this.mCamera.release();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}
			this.mCamera=null;
		}
	}


	public XWDataCenter(MainApplication xwApp){
		if (XWDataCenter.xwDC!=null)
			return;

		this.xwApp=xwApp;
		XWDataCenter.xwDC=this;

		try
		{
			PackageName = xwApp.getPackageName();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		sProgPath=UriConfig.getSavePath();

		File file = new File(XWDataCenter.sProgPath+ UriConfig.USER_DATA_DIR);
		if (!file.isDirectory()) {
			try {
				file.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		file = new File(XWDataCenter.sProgPath+ UriConfig.USER_DATA_DIR);
		if (!file.exists()) {
			sProgPath=UriConfig.getPackPath();
		}

		///////如果是第一次安装,则清理该目录!!!!!

		try
		{
			SharedPreferences settings = xwApp.getSharedPreferences(XWDataCenter.PackageName, 0);
			SharedPreferences.Editor editor = settings.edit();

			if (!settings.getBoolean("NEW_INSTALL", false)) {//只有第一次安装才创建icon
				editor.putBoolean("NEW_INSTALL", true);
				editor.putInt("APP_TYPE",AppConfig.APP_TYPE);//保存当前版本类型
				try
				{
					////清理账户
					editor.putString("LOGIN_USER","");
					UriConfig.deleteAll();
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}
			} else if(settings.getInt("APP_TYPE",0)!=AppConfig.APP_TYPE){
				editor.putInt("APP_TYPE",AppConfig.APP_TYPE);
				////////当tf和非tf版本切换时要清理缓存
				try
				{
					////清理账户
					editor.putString("LOGIN_USER","");
					UriConfig.deleteAll();
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}
			}

			editor.apply();
		}
		catch(Exception ex3)
		{
			ex3.printStackTrace();
		}




		file = new File(XWDataCenter.sProgPath);
		if (!file.isDirectory()) {
			try {
				file.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		file = new File(XWDataCenter.sProgPath+ UriConfig.USER_DATA_DIR);
		if (!file.isDirectory()) {
			try {
				file.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Log.e("XIM","current path:"+sProgPath);

		Log.e("xim", "XWDataCenter to load lib xwimsdk");
		{
			System.loadLibrary("xwimsdk");
		}
		Log.e("xim", "XWDataCenter loaded lib xwimsdk");


		/////////////////////////2016-10-31,设置通讯ip和端口,通讯端口默认8899
		try {
			Log.e("xim","setip"+XWDataCenter.getXIMIP());
			////配置应用WEB IP
			ServerConfig.config(XWDataCenter.getXIMIP());
			////配置底层通讯 IP
			setServerIPPort((XWDataCenter.getXIMIP() + "\0").getBytes("iso-8859-1"), 8899);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		this.XWMsghandle = new XWDataCenterHandler();


		////////////////////
		setcurrentPackPath((XWDataCenter.sProgPath+"\0").getBytes());

		// /////////2015-09-08,改为10秒缓冲
		audioDataBuffer = ByteBuffer.allocateDirect(16000*10);// new byte[8000];/////
		// /////500ms
		/////////最大320*240视频
		videoDataBuffer= ByteBuffer.allocateDirect(800*640*3+54);// new byte[8000];


		if (videoDataBuffer==null)
		{
			Log.e("XIM","Error videoDataBuffer");
		}

		// /////////////2014-06-30
		initSystem(audioDataBuffer,videoDataBuffer);

		/////////////////摄像头参数检测
		XWDataCenter.xwDC.initCameraParam();

		listGotMsg=new ArrayList<Message>();

//		//获取用户头像列表
		if(XWDataCenter.headBeanMap==null){
			XWDataCenter.headBeanMap= new HashMap<String, String>();
		}

		this.groupsInfo=new ArrayList<FriendGroupInfo>();
		this.nodesInfo=new ArrayList<FriendNodeInfo>();
		this.msgParamList=new LinkedList<MessageParamA>();
		this.activityList=new ArrayList<>();
		FriendGroupInfo fgi=new FriendGroupInfo();
		fgi.setGroupName(xwApp.getResources().getString(R.string.my_good_friend));
		this.groupsInfo.add(fgi);
		parser = new SmileyParser();

		/////this.initCameraParam();
		timeSB=new StringBuffer();
		fformat=new DecimalFormat("0.00");
		accountSB=new StringBuffer();

	}

	 static void initDB()
	{
		try
		{

			if (friendDB==null){
				friendDB=getFriendDB();
			}

			if(chatHistoryDB==null){
				chatHistoryDB=getChatHistoryDB();
			}
			if (msgDB==null){
				msgDB= getMessageDB();
			}

			if(contactDB==null){
				contactDB=getContactDB();
			}



		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 关闭数据库
	 */
	synchronized public static void closeDB(){
		try
		{

			if(contactDB!=null){
				contactDB.close();
				contactDB=null;
			}

			if(friendDB!=null){
				friendDB.close();
				friendDB=null;
			}
			if(chatHistoryDB!=null){
				chatHistoryDB.close();
				chatHistoryDB=null;
			}

			if(msgDB!=null){
				msgDB.close();
				msgDB=null;
			}


		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static FriendNodeDB getFriendDB(){
		if(friendDB==null){
			friendDB=new FriendNodeDB();
		}
		return  friendDB;
	}
	public static MessageDB getMessageDB(){
		if(msgDB==null){
			msgDB=new MessageDB(MainApplication.getInstance(),XWDataCenter.sProgPath+"/"+"jrs_"+"MessageDB.db");
		}
		return  msgDB;
	}

	public static ChatHistoryDB getChatHistoryDB(){
		if(chatHistoryDB==null){
			chatHistoryDB=new ChatHistoryDB(MainApplication.getInstance(), XWDataCenter.sProgPath+"/"+"jrs_"+"historyDB.db");
		}
		return  chatHistoryDB;
	}

	public static ContactDB getContactDB(){
		if(contactDB==null){
			contactDB=new ContactDB(MainApplication.getInstance(), XWDataCenter.sProgPath+"/"+"jrs_"+"ContactDB.db");
		}
		return contactDB;
	}
	/**
	 * 清理activity
	 */
	public void clearAllActivity(){
		this.isConnected=false;
		this.isLogin=false;

		//this.logoutService();
		xwApp.deleteNotification();
		this.destroyCompress();
		clearActList();
	}

	public void clearActList(){
		if(activityList!=null){
			for(Activity act:activityList){
				if(act!=null){
					act.finish();
				}
			}
			activityList.clear();
		}
	}

	/**
	 * 设置重发的文件消息
	 */
	public void setResendMsg(){
		if(MainApplication.getInstance().mFile==null||
				MainApplication.getInstance().mFile.isEmpty()){
			return;
		}
		for (long key : MainApplication.getInstance().mFile.keySet()) {
			ChatMsgEntity bean = MainApplication.getInstance().mFile.get(key);
			bean.setSendFlag(-1);
			updateMsg(XWDataCenter.xwDC.loginName, bean.getFriendAccount(), bean);
		}
	}


	 void removeFriendGroupInfo(FriendGroupInfo fgi){
		List<FriendNodeInfo> temList=new ArrayList<>();
		for(int i=0;i<this.nodesInfo.size();i++){//同时删除属于该分组的好友节点
			FriendNodeInfo fni=this.nodesInfo.get(i);
			if(fni!=null&&(fni.getGroupName()!=null)&&fni.getGroupName().equals(fgi.getGroupName())){
				temList.add(fni);
			}
		}
		if(temList.size()>0){
			for(FriendNodeInfo node:temList){
				this.nodesInfo.remove(node);
			}
			temList.clear();
		}
		this.groupsInfo.remove(fgi);
	}
	private void removeNodesInfo(String loginName){
		FriendNodeInfo fni=getFNINfoFromLoginName(loginName);
		if(fni!=null)
			this.nodesInfo.remove(fni);
	}
	 synchronized FriendGroupInfo getFGInfoFromName(String name){//根据分组名称得到FriendGroupInfo对象
		FriendGroupInfo fgi=null;
		for(int i=0;i<this.groupsInfo.size();i++){
			FriendGroupInfo fgi_tmp=this.groupsInfo.get(i);
			if( (fgi_tmp.getGroupName()!=null)&&fgi_tmp.getGroupName().equals(name)){
				fgi=fgi_tmp;
				break;
			}
		}
		return fgi;
	}
	public synchronized FriendNodeInfo getFNInfoFromID(int id){
		FriendNodeInfo fni=null;
		int nodesSize=this.nodesInfo.size();
		for(int i=0;i<nodesSize;i++){
			FriendNodeInfo fni_tmp=this.nodesInfo.get(i);
			if(fni_tmp.getId()==id){
				fni=fni_tmp;
				break;
			}
		}
		return fni;

	}
	 synchronized FriendNodeInfo getFNINfoFromLoginName(String loginName){

		FriendNodeInfo fni=null;
		try
		{
			if(this.nodesInfo==null||loginName==null){
				return null;
			}
			int nodeS=this.nodesInfo.size();
			for(int i=0;i<nodeS;i++){
				FriendNodeInfo fni_tmp=this.nodesInfo.get(i);
				if( (fni_tmp.getLogin_name()!=null) && fni_tmp.getLogin_name().equals(loginName)){
					fni=fni_tmp;
					break;
				}
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return fni;

	}
	public  void startVibrator(){
		((Vibrator)xwApp.getSystemService(Service.VIBRATOR_SERVICE)).vibrate(new long[]{0,10,100,50},-1);
	}
	public void stopVibrator(){

	}

	static private Thread thDisplayVideoTime=null;
	synchronized  void  displayVideoTime(){
		if (thDisplayVideoTime!=null)
			return;

		thDisplayVideoTime=new Thread(){//通话时长及余额显示线程
			public void run(){

				try{

					long lBeginTime=System.currentTimeMillis();

					if (displayVideoTimeRunning)
						return;

					displayVideoTimeRunning=true;

					/////////////////if(needOpenVideo)
					{
						long lLastgc=0;

						while(cameraRunning || isAudioRunning ){

							try{

								xwDC.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_31);

								Thread.sleep(50);



								/////////2017-02-24,不gc!!!!!!!
								/////////////////////2014-06-11,主动回收内存!!!!!!!!!!!!!!!!
								if (System.currentTimeMillis()-lLastgc>5000)
								{
									lLastgc=System.currentTimeMillis();
									/*try
									{
										System.gc();
									}
									catch(Exception ex)
									{
										ex.printStackTrace();
									}*/
								}

							}catch(InterruptedException e){
								e.printStackTrace();
							}
							netPhoneTime=(System.currentTimeMillis()-lBeginTime)/1000;
						}
					}

					displayVideoTimeRunning=false;

				}
				finally
				{
					thDisplayVideoTime=null;
				}
			}


		};

		thDisplayVideoTime.start();
	}


	/**
	 * 主动拨打
	 * @return
	 */
	//启动显示远程图像
	 Thread startRemoteVideo(){
		//final int sleepTime=1000/this.remote_video_fps;

		remoteVideoRunning=true;

		Thread remoteVideoThread=new Thread(){
			public void run(){
				////byte[] BytesGotVideo=null;

				Log.v("xim", "startRemoteVideo run...");

				////////////设置最高权限
				/////this.setPriority(Thread.MAX_PRIORITY);

				while(remoteVideoRunning){
					try{

						Log.v("XIM","startRemoteVideo thread loop");

						XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_18);


						if(XWDataCenter.xw_video_fps<=1){
							try
							{
								Thread.sleep(500);
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
						}else{
							try
							{
								Thread.sleep(1000/(XWDataCenter.xw_video_fps+1));
							}
							catch(Exception e1)
							{
								e1.printStackTrace();
							}
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		};
		return remoteVideoThread;
	}

	/////////////////////回声消除
	////////public AudioProcess mAudioEchoCancelProcess=null;

	/////////////APM回声消除
	public Apm _apm=null ;
	public int out_analog_level=200;
	public ApmModel vm=null;


	/////////////private JNISoundTouch soundtouch = null;
	synchronized public void startwebRTCEchoCancel()
	{
		try
		{
			vm = new ApmModel(MainApplication.getInstance());
			////getLevels();

			_apm =new Apm( vm.getAecExtendFilter(), vm.getSpeechIntelligibilityEnhance(), vm.getDelayAgnostic(), vm.getBeamForming(),
					vm.getNextGenerationAEC(), vm.getExperimentalNS(), vm.getExperimentalAGC(),
					8000,1
			);


			int ret = _apm.HighPassFilter(vm.getHighPassFilter());

			/*if (vm.getAecPC()) {
				ret = _apm.AECClockDriftCompensation(false);
				ret = _apm.AECSetSuppressionLevel(Apm.AEC_SuppressionLevel.values()[ApmConfig._aecPCLevel]);
				ret = _apm.AEC(true);
			}else */
			if (vm.getAecMobile() )
			{
				_apm.AECMSetSuppressionLevel(Apm.AECM_RoutingMode.values()[ApmConfig._aecMobileLevel]);
				_apm.AECM(true);
			}

			ret = _apm.NSSetLevel(Apm.NS_Level.High);
			ret = _apm.NS(vm.getNs());

			///////////不用webrtc的vad
			vm.setVad(true);
			ret = _apm.VAD(vm.getVad());
			if (vm.getVad()) //////最低敏感的声音检测
			{
				_apm.VADSetLikeHood(LowLikelihood);
			}

			if (vm.getAgc())
			{
				ret = _apm.AGCSetAnalogLevelLimits(0, 255);
				ret = _apm.AGCSetMode(AdaptiveDigital/*Apm.AGC_Mode.values()[ApmConfig._agcLevel]*/);
				ret = _apm.AGCSetTargetLevelDbfs(3/*vm.getTargetLevelInt()*/);
				/////ret = _apm.AGCSetcompressionGainDb(60/*vm.getCompressionGainInt()*/);
				ret = _apm.AGCEnableLimiter(true);
				ret = _apm.AGC(true);
				/////XWDataCenter.xwDC._apm.AGCSetStreamAnalogLevel(out_analog_level);
			}

			vm.setStart(true);



			/*soundtouch = new JNISoundTouch();

			soundtouch.setSampleRate(8000);
			soundtouch.setChannels(1);*/
		} catch (Exception ex) {
			ex.printStackTrace();
		}


	}
	synchronized public void stopwebRTCEchoCancel()
	{

		try
		{
			try {
				vm.setStart(false);
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			vm=null;
			if(_apm != null) {
				try {
					_apm.close();
				}
				catch(Exception e2)
				{
					e2.printStackTrace();
				}
				_apm=null;
			}
			///soundtouch.close();
			/////soundtouch=null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	//启动音频
	synchronized public void startXWAudio(){
		/////boolean bNeedStartAudioRecord=false,bNeedStartAudioPlay=false;

		if ((audioPlay!=null)||(xwAudioRecord!=null))
		{
			return;
			///stopXWAudio();			
		}
		if(_apm!=null){
			return;
		}
		///bNeedStartAudioRecord= (xwAudioRecord==null);
		/////bNeedStartAudioPlay=(audioPlay==null);

		Log.e("xim", "startXWAudio");

		try{
			/////////////////禁用底层的speex回声消除
			setIsUseEchoCancel(0);
			/////////////使用web rtc 回声消除
			/*if (audio_echocanceler==null)
			{
//				mAudioEchoCancelProcess=new AudioProcess();
//				mAudioEchoCancelProcess.init(8000, 2, 1);
			}*/

			///////////使用APM
			startwebRTCEchoCancel();


			xwAudioRecord=new XWAudioRecord(this);
			if(xwAudioRecord.audioRecord!=null){

				Log.e("xim", "xwAudioRecord.start()..............");
				try
				{
					xwAudioRecord.start();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}

			audioPlay=new XWAudioPlay(this);
			if(audioPlay!=null){
				try
				{
					audioPlay.start();
				}
				catch(Exception e1){
					e1.printStackTrace();
				}
				Log.e("xim", "audioPlay.start()..............");
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
	synchronized public void stopXWAudio(){

		/////////////////音频退出.
		isAudioRunning=false;

		Log.e("xim", "stopXWAudio");

		try{
			if(xwAudioRecord!=null)
			{
				try
				{
					xwAudioRecord.stopAudioRecord();
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
				xwAudioRecord=null;
			}
			if(audioPlay!=null)
			{
				try
				{
					audioPlay.stopAudioPlay();
				}
				catch(Exception e2)
				{
					e2.printStackTrace();
				}
				audioPlay=null;
			}
		}catch(Exception e){
			e.printStackTrace();
			xwAudioRecord=null;
			audioPlay=null;
		}



		stopwebRTCEchoCancel();
	}


	private String onlineStatusGBKChange(byte []source){
		String res=null;
		if(source.length==4){
			if((int)source[0]==-51&&(int)source[1]==-47&&(int)source[2]==-69&&(int)source[3]==-6){
				res=xwApp.getResources().getString(R.string.status_outline);
			}else if((int)source[0]==-63&&(int)source[1]==-86&&(int)source[2]==-69&&(int)source[3]==-6){
				res=xwApp.getResources().getString(R.string.status_online);
			}else if((int)source[0]==-61&&(int)source[1]==-90&&(int)source[2]==-62&&(int)source[3]==-75){
				res=xwApp.getResources().getString(R.string.status_busy);
			}else if((int)source[0]==-64&&(int)source[1]==-21&&(int)source[2]==-65&&(int)source[3]==-86){//离开
				res=xwApp.getResources().getString(R.string.status_leave);
			}else if((int)source[0]==-74&&(int)source[1]==-49&&(int)source[2]==-65&&(int)source[3]==-86){//断开
				res=xwApp.getResources().getString(R.string.status_cutout);
			}
		}else if(source.length==8){
			if((int)source[0]==-78&&(int)source[1]==-50&&(int)source[2]==-68&&(int)source[3]==-45&&(int)source[4]==-69&&(int)source[5]==-31&&(int)source[6]==-46&&(int)source[7]==-23){
				res=xwApp.getResources().getString(R.string.status_meet);
			}else if((int)source[0]==-51&&(int)source[1]==-30&&(int)source[2]==-77&&(int)source[3]==-10&&(int)source[4]==-66&&(int)source[5]==-51&&(int)source[6]==-78&&(int)source[7]==-51){
				res=xwApp.getResources().getString(R.string.status_toeat);
			}else if((int)source[0]==-67&&(int)source[1]==-45&&(int)source[2]==-52&&(int)source[3]==-3&&(int)source[4]==-75&&(int)source[5]==-25&&(int)source[6]==-69&&(int)source[7]==-80){
				res=xwApp.getResources().getString(R.string.status_forcall);
			}else if((int)source[0]==-62&&(int)source[1]==-19&&(int)source[2]==-55&&(int)source[3]==-49&&(int)source[4]==-69&&(int)source[5]==-40&&(int)source[6]==-64&&(int)source[7]==-76){
				res=xwApp.getResources().getString(R.string.status_become);
			}else if((int)source[0]==-50&&(int)source[1]==-46&&(int)source[2]==-75&&(int)source[3]==-60&&(int)source[4]==-70&&(int)source[5]==-61&&(int)source[6]==-45&&(int)source[7]==-47){
				res=xwApp.getResources().getString(R.string.my_good_friend);
			}
		}else if(source.length==10){
			if((int)source[0]==-49&&(int)source[1]==-44&&(int)source[2]==-54&&(int)source[3]==-66&&(int)source[4]==-50&&(int)source[5]==-86&&(int)source[6]==-51&&(int)source[7]==-47&&(int)source[7]==-69&&(int)source[7]==-6){
				res=xwApp.getResources().getString(R.string.status_display_outline);
			}
		}
		return res;
	}

	/**
	 * 更新node
	 */
	public static FriendNodeInfo updateNodeInfo(FriendNodeInfo src,FriendNodeInfo node){
		if(src==null||node==null){
			return null;
		}
		if(node.getOnline_type()!=-1){//-1表示不更新
			src.setOnline_type(node.getOnline_type());
		}
		if(node.getSignName()!=null){
			src.setSignName(node.getSignName());
		}
		if(node.getOnline_status()!=null){
			src.setOnline_status(node.getOnline_status());
		}
		if(node.getNumber()!=0){
			src.setNumber(node.getNumber());
		}
		if(node.getSex()!=0){
			src.setSex(node.getSex());
		}

		if(node.getAge()!=0){
			src.setAge(node.getAge());
		}


		if(node.getArea()!=null){
			src.setArea(node.getArea());
		}

		if(node.getIntroduction()!=null){
			src.setIntroduction(node.getIntroduction());
		}

		if(node.getAcceptType()!=0){
			src.setAcceptType(node.getAcceptType());
		}

		if(node.getGroupName()!=null){
			src.setGroupName(node.getGroupName());
		}

		if(node.getAccountValue()!=0){
			src.setAccountValue(node.getAccountValue());
		}

		if(node.getIcon()!=null){
			src.setIcon(node.getIcon());
		}
		if(node.getRecentChat()!=null){
			src.setRecentChat(node.getRecentChat());
		}
		if(node.getLastTime()!=null){
			src.setLastTime(node.getLastTime());
		}
		if(node.getUpdateTime()!=0){
			src.setUpdateTime(node.getUpdateTime());
		}
		return src;
	}
	/**保存好友
	 */
	private void saveNode(FriendNodeInfo node,String table){
		if(node==null||table==null){
			return;
		}
		if(node.getLogin_name().equals(XWDataCenter.getCurAccount())){
			node.setOnline_type(99);
		}
		FriendNodeDB.saveMsg(node);


	}

	/////延时发送密钥协商.
	public void postSendCreditMessage(String sToUser,int delay){
		/////////////////2014-11-28,在好友协商成功后,开始密钥协商.
		Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_60,sToUser);
		XWMsghandle.sendMessageDelayed(msg,delay);
	}

	/**更新一个好友
	 */
	public void updateAHeadPic(final Map<String,String> beanList,final FriendNodeInfo node){

		if(beanList==null||node==null){
			return;
		}
		Log.e("xim","updateAHeadPic");
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				try{
					String httpUrl = Http.getAHeadUrl()+"?user_id="+URLEncoder.encode(node.getLogin_name(),"gbk");
					//取得返回的字符串
					String strResult = NetTaskUtil.getDataTaskSync(httpUrl);
					if(!TextUtils.isEmpty(strResult)){
						List<HeadBean> list=BeanOperate.getHeadBeanList(strResult);
						if(list!=null&&!list.isEmpty()){
							for(HeadBean bean:list){
								String friendName=bean.getFriend_name();
								String imageName=bean.getImage_name();
								imageName=XWDataCenter.XWdecodeurl(imageName, "GBK");
								friendName=XWDataCenter.XWdecodeurl(friendName, "GBK");
								if(imageName!=null&&friendName!=null){
									node.setIcon(imageName);
									FriendNodeInfo nodeInfo=FriendNodeDB.getAFriend(XWDataCenter.getCurAccount(),node.getLogin_name());
									if(nodeInfo!=null){
										nodeInfo.setIcon(imageName);
									}
									beanList.put(friendName, imageName);
								}
							}
							BusProvider.getInstance().post(new AvatarUpdateEvent(node));
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});

	}

	 boolean isFriendLoadFinish()
	{

		try
		{
			Log.e(TAG,"isFriendLoadFinish: isLogin"+isLogin+",loginName=="+loginName);
			return !((!isLogin)||(this.loginName==null)) && getFNINfoFromLoginName(this.loginName)!=null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}


	////获取头像列表
	public boolean bHasHeadRun=false;
	////开始获取好友头像列表
	 void startGetFriendsHeads(){
		if(!bHasHeadRun) {
			bHasHeadRun=true;
			XWMsghandle.removeMessages(XWDataCenterMessage.MSG_38);
			XWMsghandle.sendEmptyMessageDelayed(XWDataCenterMessage.MSG_38,500);
		}
	}

	/**
	 *
	 * 删除所有解码文件
	 */
	public static void clearAllDecrypt(){
		UriConfig.delete(UriConfig.getDecryptFileDir());
	}

	///获取某个加密文件的解密路径
	public static String getDecryptFilepath(ChatMsgEntity entity){
		String filePath=entity.getFilePath();
		if(filePath.lastIndexOf("/")>0){
			String fileName=filePath.substring(filePath.lastIndexOf("/")+1);
			filePath= UriConfig.getDecryptFileDir()+"/"+fileName;
			Log.e("xwdc","getDecryptFilepath:"+filePath);
			return filePath;
		}
		return null;
	}

	//删除某个加密文件的解码文件
	public static void delDecrypt(String path){
		try{
			if(path==null){
				return;
			}
			File file=new File(path);
			if(file.exists()){
				file.delete();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 加密文件
	 */
	public static void encodeFileTask(final String src){
		Log.e("xw","encodeFileTask");
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				long lst=System.currentTimeMillis();
				try{
					boolean ret=com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_aes_file(src,src+ JRSConstants.ENCRYPT_END);
					if(ret){
						FileUtil.deleteFile(src);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				Log.e("XWDataCenter","encodeFile take time:"+(System.currentTimeMillis()-lst)*0.001);
			}
		});


	}

	/**
	 * 解密文件到指定目录
	 */
	public static boolean decodeFile(String src,String target){

		try{
			if(src.endsWith(JRSConstants.ENCRYPT_END)){
				File file =new File(src);
				if(file.exists()){
					return  com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_aes_file(src,target);
				}

			}

		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}


	//收到消息处理
	public void hasReceiveMessage(int type,int senderID,byte []content,byte [] btime){
		Log.e(TAG,"hasReceiveMessage:"+
				"type:"+type+","+"senderID:"+senderID+","+new String(content));

		String ctime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		ctime+="\0";

		Date gotDate=new Date();

		if ((btime!=null)&&(btime.length>=19))
		{
			try
			{
				ctime=new String(btime,"GBK")+"\0";
				////Log.e(TAG, "ctime:"+ctime);

				gotDate=TimeConvertFromShangHaiToLocal2(ctime);
				ctime=TimeConvertFromShangHaiToLocal(ctime);

				////Log.e(TAG, "local ctime:"+ctime);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}


		String text=null;
		try{
			text=new String(content,"GBK");
			////Log.e(TAG,"text:"+text);
		}catch(Exception e){
			e.printStackTrace();
		}


		if(senderID==0)
		{
			/////////////////////系统消息
			Log.e(TAG,"hasReceiveMessage got system message:"+
					"type:"+type+","+ctime+","+text);

			///////////////////////////////2017-09-27,在这里处理收到的系统消息!!!!!!!!!!!!!!!!
			/*********************************
			 * 即时通讯服务器可对外提供发送消息接口，用于向某个终端发送消息。
			 *
			 */
		    ///////提示!!!!!!!!!!!!
			Message msg1=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_17, "hasReceiveMessage got system message:"+
					"type:"+type+","+text);
			XWMsghandle.sendMessage(msg1);

			//////////////只处理5分钟内发的消息。
			if (Math.abs(new Date().getTime()-gotDate.getTime())<=300000)
			{
                ///////////////////////
				/*****************************
				 * 在这里处理系统发来的控制消息。
				 *
				 *
				 */


			}
			return;
		}

		//////////////2017-05-18,用于激活service唤醒处理
		try {
			Log.e("XIM", "XWScree on off");
			Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
			intentservice.setAction("DO_CHECK");
			MainApplication.getInstance().startService(intentservice);
		} catch (Exception e) {
			e.getStackTrace();
		}

		boolean bRehandle=true;

		if (type==3)
		{
			type=0;
			bRehandle=false;
		}
		if(type==0){

			FriendNodeInfo fni=this.getFNInfoFromID(senderID);

			if ((fni==null)||(fni.getLogin_name()==null)||(fni.getLogin_name().length()==0))
			{
				if (bRehandle)
				{
					///////////////////////////请求临时聊天
					XWDataCenter.xwDC.XWRequestTempTalk((XWDataCenter.xwDC.loginName+"\0").getBytes(),0,("\0").getBytes(),senderID);

					XWTextMessage savemsg=new XWTextMessage();
					savemsg.type=3;
					savemsg.senderID=senderID;
					savemsg.content=content;
					savemsg.btime=btime;

					//////listSaveMsg.add(savemsg);
					Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_15,savemsg);
					XWMsghandle.sendMessageDelayed(msg,20000);

				}
				/////listSaveMsg.add(savemsg);
				return;
			}

			////Log.e("XIM"," hasReceiveMessage username:"+fni.getLogin_name());
			//////////////2016-09-10,密钥协商处理限定有效时间差在1小时内
			if(( fni.getLogin_name()!=null) && /*( Math.abs((new Date()).getTime()-gotDate.gTime())<=3600000   )  &&*/ (HandleCreditMessage( fni.getLogin_name(),text,gotDate)))
			{
				/////////////////////////////////////
				/////Log.v("XIM","HandleCreditMessage "+text);
				return;
			}

            ///////先判断是否来电消息
			if(!TextUtils.isEmpty(text)&&text.startsWith(JRSConstants.MSG_CALL_PRE)){
				Log.e(TAG,"incall:"+text);
				////唤醒屏幕
				if(xwContext==null||!xwContext.bIsFront){
					XWMsghandle.sendEmptyMessageDelayed(XWDataCenterMessage.MSG_59,500);
				}
				return;
			}

			/////////////////////2014-09-05,对收到文本数据进行解压
			String sAESPassword=getFriendAESPassword( fni.getLogin_name(),xwDC.loginName);
			if (sAESPassword!=null)
			{
				try
				{
					byte[] btTemp=text.getBytes("GBK");
					byte[] btDestTemp=new byte[btTemp.length/2+1];
					int iLen=xwDC.XWNetphoneAESDecodeText(btTemp,btDestTemp,(sAESPassword+"\0").getBytes());

					if (iLen>0) ////////AES解出数据。
					{
						byte[] btDestTemp2=new byte[iLen];
						System.arraycopy(btDestTemp, 0, btDestTemp2, 0, iLen);

						text=getANSIStringFromBytes(btDestTemp2);
					}
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}
			}


			if (!com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(MainApplication.getInstance()))
			{
				text=XWCodeTrans.doTrans("无法解读加密数据.");
			}

			//存储收到的消息
			//			((SelectFriendUI)xwContext).mHandler.s
			String filePath="";
			int msgType=0;
			if(text.contains("(:voice)")||text.contains("(:image)")||text.contains("(:file)")){
				Log.e(TAG,"收到文件:"+text);
			}
			if(text.contains("(:voice)")){
				msgType=FriendChatRecord.MSG_VOICE;
				filePath=text.replace("(:voice)", "");
				text="(:voice)";
			}else if(text.contains("(:image")){
				msgType=FriendChatRecord.MSG_PHOTO;
				filePath=text.replace("(:image)", "");
				text="(:image)";
			}else if(text.contains("(:file)")){
				msgType=FriendChatRecord.MSG_FILE;
				filePath=text.replace("(:file)", "");
				text="(:file)";
			}else if(text.contains("(:video)")){
				msgType=FriendChatRecord.MSG_VIDEO;
				filePath=text.replace("(:video)", "");
				text="(:video)";
			}


			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setComMeg(1);
			entity.setDate(ctime);
			entity.setFilePath(filePath);
			entity.setImg(0);
			entity.setMessage(text);
			entity.setMsgType(msgType);
			entity.setName(fni.getSignName());
			entity.setFriendAccount(fni.getLogin_name());  //好友账号
			entity.setNo(System.currentTimeMillis());       // 统一用时间戳作为流水号
			if(filePath!=null&&filePath.length()>0) {
				entity.setSendFlag(11);  //10表示成功，11表示未打开
				entity.setSnap(1);//阅后即焚
			}else{
				entity.setSendFlag(10);
			}
			entity.setRead(1);
			saveMsg(XWDataCenter.getCurAccount(),fni.getLogin_name(), entity);
			Log.e(TAG,"get msg to save:"+fni.getLogin_name());


			//更新历史消息
			ChatHistoryBean bean=new ChatHistoryBean();
			bean.setLogin_name(fni.getLogin_name());
			bean.setSignName(fni.getSignName());
			bean.setRecentChat(text);
			bean.setLastTime(""+System.currentTimeMillis());
//				bean.setIntroduction(filePath);//文件路径
			bean.setUnread(1);
			//信息保存到当前用户账号下
			refreshHistoryMsg(XWDataCenter.getCurAccount(), bean);



			fni.setHasNoReadMsg(true);



			////处理消息通知包括声音提示
			notificationMsg(new ChatRefreshBean(entity, 1));


			//加密文件
			if(filePath!=null&&filePath.length()>0){
				encodeFileTask(filePath);
			}

		}
	}


	//收到消息处理
	public void hasReceiveMessageByUserName(int type,String sSender,byte []content,byte [] btime){
		Log.e(TAG,"hasReceiveMessageByUserName:"+
				"type:"+type+","+"senderID:"+sSender+","+new String(content)+","+btime.length);

		//////////////2017-05-18,用于激活service唤醒处理
		try {
			Log.e("XIM", "XWScree on off");
			Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
			intentservice.setAction("DO_CHECK");
			MainApplication.getInstance().startService(intentservice);
		} catch (Exception e) {
			e.getStackTrace();
		}

		boolean bRehandle=true;

		if (type==3)
		{
			type=0;
			bRehandle=false;
		}
		if(type==0){

			FriendNodeInfo fni=this.getFNINfoFromLoginName(sSender);

			if (fni==null)
			{
				if (bRehandle)
				{
					///////////////////////////请求临时聊天
					XWDataCenter.xwDC.XWRequestTempTalk((XWDataCenter.xwDC.loginName+"\0").getBytes(),0,(sSender+"\0").getBytes(),0);

					XWTextMessage savemsg=new XWTextMessage();
					savemsg.type=3;
					savemsg.senderID=0;
					savemsg.sSenderName=sSender;
					savemsg.content=content;
					savemsg.btime=btime;

					//////listSaveMsg.add(savemsg);
					Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_15,savemsg);
					XWMsghandle.sendMessageDelayed(msg,20000);

				}
				/////listSaveMsg.add(savemsg);
				return;
			}



			String ctime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			ctime+="\0";

			Date gotDate=new Date();

			if ((btime!=null)&&(btime.length>=19))
			{
				try
				{
					ctime=new String(btime,"GBK")+"\0";
					Log.e(TAG, "ctime:"+ctime);

					ctime=TimeConvertFromShangHaiToLocal(ctime);
					gotDate=TimeConvertFromShangHaiToLocal2(ctime);

					Log.e(TAG, "local ctime:"+ctime);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

			}


			String text=null;
			try{
				text=new String(content,"GBK");
				Log.e(TAG,"text:"+text);
			}catch(Exception e){
				e.printStackTrace();
			}

			if (HandleCreditMessage( fni.getLogin_name(),text,gotDate))
			{
				/////////////////////////////////////
				Log.v("XIM","HandleCreditMessage "+text);
				return;
			}


			/////////////////////2014-09-05,对收到文本数据进行解压
			String sAESPassword=XWDataCenter.getFriendAESPassword( fni.getLogin_name(),XWDataCenter.getCurAccount());
			if (sAESPassword!=null&&text!=null)
			{
				byte[] btTemp=text.getBytes();
				byte[] btDestTemp=new byte[btTemp.length/2+1];
				int iLen=xwDC.XWNetphoneAESDecodeText(btTemp,btDestTemp,(sAESPassword+"\0").getBytes());

				if (iLen>0) ////////AES解出数据。
				{
					byte[] btDestTemp2=new byte[iLen];
					System.arraycopy(btDestTemp, 0, btDestTemp2, 0, iLen);

					text=getANSIStringFromBytes(btDestTemp2);
				}
			}



			if (!com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(MainApplication.getInstance()))
			{
				text=XWCodeTrans.doTrans("无法解读加密数据.");
			}

			Log.v("XIM", "收到消息:"+text);
			//存储收到的消息
			//			((SelectFriendUI)xwContext).mHandler.s
			String filePath="";
			int msgType=0;
			if(text.contains("(:voice)")||text.contains("(:image)")||text.contains("(:file)")){
				Log.e(TAG,"收到文件:"+text);
			}
			if(text.contains("(:voice)")){
				msgType=FriendChatRecord.MSG_VOICE;
				filePath=text.replace("(:voice)", "");
				text="(:voice)";
			}else if(text.contains("(:image")){
				msgType=FriendChatRecord.MSG_PHOTO;
				filePath=text.replace("(:image)", "");
				text="(:image)";
			}else if(text.contains("(:file)")){
				msgType=FriendChatRecord.MSG_FILE;
				filePath=text.replace("(:file)", "");
				text="(:file)";
			}else if(text.contains("(:video)")){
				msgType=FriendChatRecord.MSG_VIDEO;
				filePath=text.replace("(:video)", "");
				text="(:video)";
			}

			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setComMeg(1);
			entity.setDate(ctime);
			entity.setFilePath(filePath);
			entity.setImg(0);
			entity.setMessage(text);
			entity.setMsgType(msgType);
			entity.setName(fni.getSignName());
			entity.setFriendAccount(fni.getLogin_name());  //好友账号
			entity.setNo(System.currentTimeMillis());       //统一用时间戳作为流水号
			if(filePath!=null&&filePath.length()>0) {
				entity.setSendFlag(11);  //10表示成功，11表示未打开
				entity.setSnap(1);//阅后即焚
			}else{
				entity.setSendFlag(10);
			}
			entity.setRead(1);
			saveMsg(XWDataCenter.getCurAccount(),fni.getLogin_name(), entity);
			Log.e(TAG,"get msg to save:"+fni.getLogin_name());


			//更新历史消息
//			if(chatHistoryDB!=null){
				ChatHistoryBean bean=new ChatHistoryBean();
				bean.setLogin_name(fni.getLogin_name());
				bean.setSignName(fni.getSignName());
				bean.setRecentChat(text);
				bean.setLastTime(""+System.currentTimeMillis());
//				bean.setIntroduction(filePath);//文件路径
				bean.setUnread(1);
				//信息保存到当前用户账号下
				refreshHistoryMsg(XWDataCenter.getCurAccount(), bean);

//			}



			fni.setHasNoReadMsg(true);

			////处理消息通知包括声音提示
			notificationMsg(new ChatRefreshBean(entity, 1));

			//加密文件
			if(filePath!=null&&filePath.length()>0){
				encodeFileTask(filePath);
			}

		}
	}

	/**删除好友
	 */
	public static void deleteFriend(FriendNodeInfo node){
		//删除好友结点
		if(FriendNodeDB.isExistFriend(node,XWDataCenter.getCurAccount())){
			FriendNodeDB.deleteFriendNode(node,XWDataCenter.getCurAccount());
		}
		//删除历史记录
		deleteChatHistory(node.getLogin_name());
		//删除nodelist的节点
		if(xwDC!=null)
			xwDC.removeNodesInfo(node.getLogin_name());

	}

	/**删除历史记录
	 */
	public static void deleteChatHistory(String friendAccount){
		//删除历史记录
//		if(XWDataCenter.chatHistoryDB!=null){
			if(XWDataCenter.getChatHistoryDB().isExistFriend(friendAccount,XWDataCenter.getCurAccount())){
				XWDataCenter.getChatHistoryDB().deleteFriendNode(friendAccount,XWDataCenter.getCurAccount());
			}
//		}

		//删除所有信息
//		if(XWDataCenter.msgDB!=null){
			XWDataCenter.getMessageDB().deleteFriendNode(XWDataCenter.getCurAccount(),friendAccount);
//		}
	}
	//保存信息
	public void saveMsg(String account,String friendAccount,ChatMsgEntity entity){
		if(account==null||friendAccount==null||entity==null){
			return;
		}
		//保存消息
//		if(msgDB!=null){
		getMessageDB().saveMsg(account,friendAccount, entity);
//		}
	}

	/**更新消息
	 */
	public void updateMsg(String account,String friendAccount,ChatMsgEntity entity){
		if(account==null||friendAccount==null||entity==null){
			return;
		}
		//保存消息
//		if(msgDB!=null){
			if(getMessageDB().isExist(account, friendAccount, entity)){
				getMessageDB().updateMsg(account, friendAccount, entity);
			}
//		}
	}

	//保存历史信息
	public static void refreshHistoryMsg(String table, ChatHistoryBean bean){
		if(table==null||bean==null){
			return;
		}

		ChatHistoryBean nodeInfo=XWDataCenter.getChatHistoryDB().getAChatBean(table, bean.getLogin_name());
		if(nodeInfo!=null){
			int unreads=nodeInfo.getUnread();
			if(unreads<0){
				unreads=0;
			}
			if(bean.getUnread()>0){
				unreads=unreads+bean.getUnread();
			}
			bean.setUnread(unreads);
			XWDataCenter.getChatHistoryDB().updateFriendNode(bean, table);
		}else{
			XWDataCenter.getChatHistoryDB().saveMsg(bean, table);
		}

	}

	/**获取状态文字
	 * @param status
	 * @return
	 */
	public int getOnlineType(String status){
		if(status==null||xwApp==null){
			return 1;
		}
		if(status.equals(xwApp.getResources().getString(R.string.status_online))){
			return 8;
		}else if(status.equals(xwApp.getResources().getString(R.string.status_busy))){
			return 7;
		}else if(status.equals(xwApp.getResources().getString(R.string.status_become))){
			return 6;
		}else if(status.equals(xwApp.getResources().getString(R.string.status_leave))){
			return 5;
		}else if(status.equals(xwApp.getResources().getString(R.string.status_forcall))){
			return 4;
		}else if(status.equals(xwApp.getResources().getString(R.string.status_toeat))){
			return 3;
		}else if(status.equals(xwApp.getResources().getString(R.string.status_meet))){
			return 2;
		}else if(status.equals(xwApp.getResources().getString(R.string.status_display_outline))||status.equals(xwApp.getResources().getString(R.string.status_cutout))){
			return 0;
		}else{
			return 1;
		}
	}
	//***********************************************************************************************************/
	//***********************************************************************************************************/
	//****************************************以下方法为被JNI调用:**************************************************/
	//***********************************************************************************************************/
	//***********************************************************************************************************/
	public void addFriendGroup(byte []groupByte){
		Log.e(TAG,"addFriendGroup");
		String groupName=null;
		groupName=this.onlineStatusGBKChange(groupByte);
		if(groupName==null){
			try{
				groupName = new String(groupByte,"GBK");//指定原生编码为GBK
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		/////////////////////2012-09-06,对"我的好友"组转换
		groupName=xechwic.android.XWCodeTrans.doTrans(groupName);

		Log.e(TAG, "收到分组:"+groupName);
		FriendGroupInfo fgi=null;
		if(this.getFGInfoFromName(groupName)==null){
			fgi=new FriendGroupInfo();
			fgi.setGroupName(groupName);
			this.groupsInfo.add(fgi);
			
			/*
			if(xwContext==null) return;
			if(xwContext instanceof SelectFriendUI){
				//				((SelectFriendUI)xwContext).mHandler.sendEmptyMessage(2);
				Message msg=((SelectFriendUI)xwContext).mHandler.obtainMessage(2,fgi);
				((SelectFriendUI)xwContext).mHandler.sendMessage(msg);
			}*/

			Message msg=this.XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_32, fgi);
			XWMsghandle.sendMessage(msg);

		}
	}

	/**
	 * 管理好友结点
	 */
	//////2017-01-11,增加了DetailUpdateTime参数,用于表示用户的头像信息更改时间
	public void manageFriendNode(int opType,byte []node,int id,byte []group,byte []status,byte []sign,int terminalType,int accountValue,int DetailUpdateTime){
		String nodeName=null;
		String groupName=null;
		String statusLogin=null;
		String signName=null;
		/////boolean bNeedSendCredittoNewUser=false;

		try{
			nodeName=new String(node,"GBK").trim();
			groupName=this.onlineStatusGBKChange(group);
			if(groupName==null){
				groupName=new String(group,"GBK").trim();
			}
			groupName=xechwic.android.XWCodeTrans.doTrans(groupName.trim());
			statusLogin=this.onlineStatusGBKChange(status);
			if(statusLogin==null){
				statusLogin=new String(status,"GBK").trim();
			}
			statusLogin=xechwic.android.XWCodeTrans.doTrans(statusLogin.trim());
			//Log.e("tag", statusLogin);
			signName=new String(sign,"GBK").trim();
			if(groupName.equals("")){
				groupName=xwApp.getResources().getString(R.string.my_good_friend);
			}
			if(statusLogin.equals("")){
				statusLogin=xwApp.getResources().getString(R.string.status_online);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		Log.e("XIM","manageFriendNode "+opType+ " "+nodeName
				+ " "+groupName+ " "+statusLogin+" "+signName+" terminalType"+terminalType+"DetailUpdateTime"+DetailUpdateTime);
		////// opType 0:下载好友列表  2:更新好友
		if((opType==0)||(opType==1) || (opType==2)||(opType==4)){//下载或新增好友
			FriendNodeInfo fni1=this.getFNInfoFromID(id);
			boolean bIsUpdate=false;////标识信息更新
			if (fni1==null)  //////////不存在,2012-03-26,解决不能更新好友状态的问题
			{
				Log.e("XIM","manageFriendNode fni1==null");
				if(XWDataCenter.getCurAccount().equals(nodeName)){//如果是自己
					this.cid=id;
					this.currentAccountValue=accountValue;


					Log.e("XIM","currentAccountValue:"+currentAccountValue);

					if (this.currentAccountValue<0) //////充值为负
					{
						destroyUserAccount(xwContext,false);
						return;
					}

					this.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_33);

					try{
						this.updateFNInfo(this.cid,signName.getBytes("GBK"),(  xechwic.android.XWCodeTrans.doTransInput(this.sLoginStatus)+"\0").getBytes("GBK"),"\0".getBytes("GBK"),"2".getBytes("GBK"));
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				else  //////////2014-06-16,对隐身状态进行处理!!!!将其作为断开处理!!!!!
				{
					if(statusLogin.equals(xwApp.getResources().getString(R.string.status_display_outline))){
						statusLogin=xwApp.getResources().getString(R.string.status_cutout);
					}
				}
				FriendNodeInfo fni=new FriendNodeInfo();
				fni.setId(id);
				fni.setOnline_type(this.getOnlineType(statusLogin));
				fni.setLogin_name(nodeName);
				fni.setOnline_status(statusLogin);
				fni.setUpdateTime(DetailUpdateTime);
				if((this.cid==id)&&(fni.getOnline_type()>0)){//如果是自己且非脱机状态,则显示在组的最上方
					fni.setOnline_type(99);
					Log.e(TAG,"得到我的账户");
				}
				fni.setSignName(signName);
				fni.setGroupName(groupName);
				if(this.getFNInfoFromID(id)==null){
					this.nodesInfo.add(fni);
				}

				saveNode(fni, XWDataCenter.getCurAccount());

				FriendGroupInfo fgi=null;
				if(this.getFGInfoFromName(groupName)==null){//出现了没有收到分组信息的情况下收到了好友信息
					fgi=new FriendGroupInfo();
					fgi.setGroupName(groupName);
					this.groupsInfo.add(fgi);
				}

				Message msg=this.XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_34,fni);
				XWMsghandle.sendMessage(msg);


			}
			else   /////////////已存在
			{
				Log.v("XIM","manageFriendNode update");
				if(id==this.cid){
					this.currentAccountValue=accountValue;

					Log.e("XIM","currentAccountValue:"+currentAccountValue);

					if (this.currentAccountValue<0) //////充值为负
					{
						destroyUserAccount(xwContext,false);
						return;
					}


					this.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_35);

				}
				else ////////friend update status
				{
					if(statusLogin.equals(xwApp.getResources().getString(R.string.status_display_outline))){
						statusLogin=xwApp.getResources().getString(R.string.status_cutout);
					}
				}

				fni1.setOnline_type(this.getOnlineType(statusLogin));
				if((this.cid==id)&&(fni1.getOnline_type()>0)){//如果是自己且非脱机状态,则显示在组的最上方
					fni1.setOnline_type(99);
				}
				fni1.setLogin_name(nodeName);
				fni1.setOnline_status(statusLogin);
				fni1.setSignName(signName);
				fni1.setAccountValue(accountValue);
				if(fni1.getUpdateTime()!=DetailUpdateTime){
					bIsUpdate=true;///信息已更新
				}
				fni1.setUpdateTime(DetailUpdateTime);

				//////////////////2014-06-26,分组信息由客户端维护!!!!!!!!
				//////fni.setGroupName(null);

				if ( (!TextUtils.isEmpty(groupName)) && ((opType==4)||((fni1.getGroupName()!=null)&&(fni1.getGroupName().equals(xechwic.android.XWCodeTrans.doTrans("临时聊天"))))) )
				{
					fni1.setGroupName(groupName);

					FriendGroupInfo fgi;
					if(this.getFGInfoFromName(groupName)==null){//出现了没有收到分组信息的情况下收到了好友信息
						fgi=new FriendGroupInfo();
						fgi.setGroupName(groupName);
						this.groupsInfo.add(fgi);
					}
				}

				//更新好友信息
				saveNode(fni1, XWDataCenter.getCurAccount());
				/////下载头像列表
				if(id==this.cid){
					if(isFriendLoadFinish()){
						startGetFriendsHeads();
					}
				}else{
					Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_36, fni1);
					XWMsghandle.sendMessage(msg);
					if(bIsUpdate){
						//////更新某个好友的信息
						XWDataCenter.xwDC.updateAHeadPic(XWDataCenter.headBeanMap, fni1);
					}
				}

			}

		}

		else if(opType==3){//删除好友(主动或被动删除都会执行)
			//FriendGroupInfo tmpFGI=this.getFGInfoFromName(groupName);

			try /////////////删除本地数据表中的好友!!!!!!2014-07-03
			{
				FriendNodeInfo tmpFNI=new FriendNodeInfo();
				tmpFNI.setLogin_name(nodeName);
				FriendNodeDB.deleteFriendNode(tmpFNI,this.loginName);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}


			FriendNodeInfo tmpFNI=this.getFNInfoFromID(id);

			Log.v("XIM","mangeFriend delete friend 1");
			//			//Log.e("tag","remove:"+tmpFNI.getLogin_name()+"  "+sendGroup.hashCode()+" "+tmpFNI.getGroupName().hashCode());
			//			this.nodesInfo.remove(tmpFNI);
			//			tmpFNI=null;
			if (tmpFNI==null)
				return;

			Log.v("XIM","mangeFriend delete friend 2");
			this.clearRecord(cid,id);

			Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_37, tmpFNI);
			XWMsghandle.sendMessage(msg);
			
			/*
			if(xwContext==null) return;
			if(xwContext instanceof SelectFriendUI){
				Message msg=((SelectFriendUI)xwContext).mHandler.obtainMessage(7,tmpFNI);
				((SelectFriendUI)xwContext).mHandler.sendMessage(msg);
				//				((SelectFriendUI)xwContext).mHandler.sendEmptyMessage(7);
			}*/

			{
				/////////////清理内存保留数据。2014-07-21
				try
				{
					nodesInfo.remove(tmpFNI);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}

			Log.v("XIM","mangeFriend delete friend 3");
		}
	}

	/**
	 *
	 */
	//查询好友时收到数据处理
	public void receiveQueryNodeInfo(byte []name,byte []signName,int number,int sex,int age,byte []area,byte []introduction,int test,int id,byte []status){
		FriendNodeInfo fni=new FriendNodeInfo();
		try{
			fni.setLogin_name(new String(name,"GBK"));
			fni.setSignName(new String(signName,"GBK"));
			fni.setArea(new String(area,"GBK"));
			fni.setIntroduction(new String(introduction,"GBK"));
			fni.setAge(age);
			fni.setNumber(number);
			fni.setSex(sex);
			fni.setAcceptType(test);
			fni.setId(id);


			fni.setOnline_status(xechwic.android.XWCodeTrans.doTrans(this.onlineStatusGBKChange(status)));
			if(fni.getOnline_status()==null){
				fni.setOnline_status(xechwic.android.XWCodeTrans.doTrans(new String(status,"GBK")));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if( (fni.getOnline_status()!=null) && fni.getOnline_status().equals("")){
			fni.setOnline_status(xwApp.getResources().getString(R.string.status_online));
		}
		fni.setOnline_type(this.getOnlineType(fni.getOnline_status()));


		Log.v("XIM","receiveQueryNodeInfo "+fni.getLogin_name());

		{

			Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_39, fni);
			XWMsghandle.sendMessage(msg);


		}

	}


	//接收到系统消息
	public void receiveSysMsg(byte []name,byte []signName,int number,int sex,int age,byte []onlineStatus,int id,byte []groupName,int msgType){
		MessageParamA mpa=new MessageParamA();
		FriendNodeInfo fni=new FriendNodeInfo();
		try{
			fni.setLogin_name(new String(name,"GBK"));
			fni.setSignName(new String(signName,"GBK"));
			//fni.setArea(new String(area,"GBK"));
			//fni.setIntroduction(new String(introduction,"GBK"));
			fni.setOnline_status(new String(onlineStatus,"GBK"));
			////Log.e("tag","status:"+fni.getOnline_status());
			fni.setAge(age);
			fni.setNumber(number);
			fni.setSex(sex);
			fni.setId(id);
			String groupStr=null;
			groupStr=this.onlineStatusGBKChange(groupName);
			if(groupStr==null){
				groupStr=new String(groupName,"GBK");
			}
			mpa.setGroupName(groupStr);
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.e("XIM", "收到系统消息:"+msgType+" loginName:"+fni.getLogin_name());
		mpa.setFni(fni);
		mpa.setMsgType(msgType);
		this.msgParamList.add(mpa);

		Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_40, mpa);
		XWMsghandle.sendMessage(msg);


		XWAudioAlert.PlayMessageAlert();

	}

	RecordBean mRecordBean=null;


	/**音视频通讯
	 * @param login
	 * @param type
	 * 0,空闲（我方挂断来电或去电）； 1，开始拨号；2，正在拨号；3，拨通处理 ；5，拨号时对方挂断；11，来电,12来电接通,13来电对方挂断
	 */
	public int inCallStatus=0;//1来电，2，接通 ，///用于标识来电是否有接通
	public int myHungup =0;// 1我方挂断
	public int friendHungup=0;//1对方挂断


	////////////////控制如果电话挂断后，5秒钟内不允许重拨。防止信号错乱问题。
	public long lPhoneIdleTime=0;
	synchronized public void receiveNetPhoneReq(byte []login,int type){

		Log.e(TAG,"receiveNetPhoneReq:"+type);

		//////////////2017-05-18,用于激活service唤醒处理
		try {
			Log.e("XIM", "XWScree on off");
			Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
			intentservice.setAction("DO_CHECK");
			MainApplication.getInstance().startService(intentservice);
		} catch (Exception e) {
			e.getStackTrace();
		}


		boolean bNeedHangup=false;
		String loginName=null;
		try{
			loginName=new String(login,"GBK");
		}catch(Exception e){
			e.printStackTrace();
		}

		//////////////////微信消息.在这里处理,2012-11-27
		//String sGotFileLine=loginName; ////loginname包含收到文件的信息。
		///String sFromUser,sFilePath,sSendTime;
		if (type==101)
		{
			String str =new String(loginName);
			Log.e(TAG,"receiveNetPhoneReq content:"+str);
			Message msg=this.XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_5,str);
			this.XWMsghandle.sendMessage(msg);
			return;
		}

		//////////////////群聊消息.在这里处理,2017-09-04
		//String sGotFileLine=loginName; ////loginname包含收到文件的信息。
		///String sFromUser,sFilePath,sSendTime;
		if (type==102)
		{
			String str =new String(loginName);
			Log.e(TAG,"receiveNetPhoneReq content:"+str);
			Message msg=this.XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_300,str);
			this.XWMsghandle.sendMessage(msg);
			return;
		}

		// 开始拨号
		Log.e(TAG,"receiveNetPhoneReq status : "+type+" number:"+loginName+" iNetphoneStatus"+XWDataCenter.iNetphoneStatus+
				" sCurrentPhoneNumber:"+sCurrentPhoneNumber);

		if ((XWDataCenter.iNetphoneStatus==0)||(sCurrentPhoneNumber==null)||(sCurrentPhoneNumber.length()==0)) {
			sCurrentPhoneNumber = loginName;
		}
		/////////////解除非当前号码干扰
		if ((XWDataCenter.iNetphoneStatus!=0)&&(loginName!=null)&&(loginName.length()!=0)&&(!loginName.equals(sCurrentPhoneNumber)))
			return;
		//////////////////////////////////在这里进入记录,2012-09-14
		if (XWDataCenter.iNetphoneStatus!=type)
		{
			//////////////记录空闲时间,在5秒后才能再重拨。
			if (type==0)
			{
				lPhoneIdleTime=System.currentTimeMillis();
			}
			if ( (type >0)&& (type<10)) {  ////拨出

				if (XWDataCenter.iNetphoneStatus==0) ////第一次呼出
				{
					String signName = "";
					FriendNodeInfo mFriendNodeInfo = getFNINfoFromLoginName(loginName);
					if (mFriendNodeInfo != null) {
						signName = mFriendNodeInfo.getSignName();
					}
					mRecordBean = new RecordBean(loginName, signName);
					mRecordBean.setDial(true);
				}

				if (type==3) ////呼通
				{
					if (mRecordBean!=null)
						//开始拨号时间
						mRecordBean.setStartTime(System.currentTimeMillis());
				}
			}
			else if  ( type >10) ////来电
			{
				if (XWDataCenter.iNetphoneStatus==0) ////第一次呼出
				{
					String signName = "";
					FriendNodeInfo mFriendNodeInfo = getFNINfoFromLoginName(loginName);
					if (mFriendNodeInfo != null) {
						signName = mFriendNodeInfo.getSignName();
					}
					mRecordBean = new RecordBean(loginName, signName);
					//开始拨号时间
					//////mRecordBean.setStartTime(System.currentTimeMillis());
					//拨通持续时间（应该是由拨通 到 挂断的时间差）
					//mRecordBean.setCallTime();
					//是否主动外拨（true是主动外拨，false是来电）
					mRecordBean.setDial(false);
				}

				if (type==12) ////呼通
				{
					if (mRecordBean!=null)
						//开始拨号时间
						mRecordBean.setStartTime(System.currentTimeMillis());
				}


				//////////////////2016-10-31,在这里判断来电未接.
				if ((XWDataCenter.iNetphoneStatus==11)&& (type==13))
				{
					//////////////////////////////未接来电处理
					Log.e("XIM","Unaccept incoming call!!!!");

					///////////////////////////////////在这里用消息处理方式，将界面调到电话记录界面。
					{
						if(xwContext!=null&&(xwContext instanceof InCallUI)){
							///////先挂断
							((InCallUI) xwContext).finishCall();
						}
						XWMsghandle.postDelayed(new Runnable() {
							@Override
							public void run() {
								Intent intent = new Intent();
								Bundle data = new Bundle();
								data.putInt(JRSConstants.DATA, JRSConstants.NOTICE_CALL);
								intent.putExtras(data);
								intent.setClass(MainApplication.getInstance(), CommandUI.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								MainApplication.getInstance().startActivity(intent);
							}
						},500);



					}
				}
			}
			else if ( type==0)  ////////////空闲
			{
				if (mRecordBean != null) {

					///增加记录
					Message msgRecord=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_56,mRecordBean);
					XWMsghandle.sendMessage(msgRecord);
					///////////////////更新通话记录!!!!!!!!!!
					XWMsghandle.sendEmptyMessageDelayed(XWDataCenterMessage.MSG_41,300);
					/////////清理通知栏，和添加未接通知
					NotificationUtil.cleanNotificationByID(JRSConstants.NOTICE_VIDEO_DISPLAY);
					if(myHungup !=1) {/////不是我方主动挂断
						if (inCallStatus == 1) {////来电未接通
							inCallStatus = 0;
							NotificationUtil.notificationCall(mRecordBean);
						}
					}else {
						myHungup =0;
					}
					mRecordBean=null;

				}
			}


			if (type==0)
			{
				bNeedHangup=true;
			}

			//////////////
			XWDataCenter.iNetphoneStatus=type;
		}

		Log.e(TAG,"receiveNetPhoneReq status: "+type);
		Log.e(TAG,"iNetphoneStatus status: "+XWDataCenter.iNetphoneStatus);

		if (bNeedHangup)
		{
			this.XWMsghandle.sendEmptyMessageDelayed(XWDataCenterMessage.MSG_3, 3000);
		}

		Log.e(TAG,"bNeedHangup: "+bNeedHangup);
		switch(type){
			case 0://空闲
			{
				if (XWMsghandle!=null)
				{
					XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_2);
				}
			}

			break;
			case 1://开始拨号
				XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_42);

				break;
			case 2://正在拨号
				XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_43);

				break;
			case 3://拨通处理
			{
				Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_44, loginName);
				XWMsghandle.sendMessage(msg);
			}

			break;
			case 5://对方挂断
			{

				Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_45, loginName);
				XWMsghandle.sendMessage(msg);

			}

			break;
			case 11://有来电请求

				if ( MainApplication.iCallState!=TelephonyManager.CALL_STATE_IDLE)
				{
					Log.v("XIM","There is phone call");
					this.hangupNetPhone();
					break;
				}

				///////////////如果没有卡，则不接听!!!!!
				if (!com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(MainApplication.getInstance()))
				{
					Log.v("XIM","sd card not ok" );
					///text=XWCodeTrans.doTrans("无法解读加密数据.");

					this.hangupNetPhone();
					break;
				}

				try{
					inCallStatus=1;///来电
					this.calling_loginName=loginName;
					this.startVibrator();//振动
					XWAudioAlert.getAudioAlert().startAudioAlert();//响铃
					try {
						Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
						intentservice.setAction("START_IN_CALL");
						intentservice.putExtra("phone_number", loginName);
						MainApplication.getInstance().startService(intentservice);
					} catch (Exception e) {
						e.getStackTrace();
					}
				}catch(Exception e){
					e.printStackTrace();
					XWDataCenter.threadHangupNetPhone();
					xwDC.netPhoneTime=0;
				}
				break;
			case 12:
			{
				inCallStatus=2;///接通了
				Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_46, loginName);
				XWMsghandle.sendMessage(msg);
			}
			break;
			case 13://挂断,与5保持一致
			{
				Message msg=XWMsghandle.obtainMessage(XWDataCenterMessage.MSG_47, loginName);
				XWMsghandle.sendMessage(msg);
			}


			break;

			default:
				break;
		}
	}


	//音视频出错提示
	public void netPhoneCallErrorType(int type){

		Message msg=this.XWMsghandle.obtainMessage(48, new Integer(type));
		this.XWMsghandle.sendMessage(msg);
		
		/*
		//Log.e("tag", "call error type:"+type);
		if(xwContext!=null){
			if(xwContext instanceof SelectFriendUI){
				Message msg=((SelectFriendUI)xwContext).mHandler.obtainMessage(23);
				msg.arg1=type;
				((SelectFriendUI)xwContext).mHandler.sendMessage(msg);
			}else if(xwContext instanceof FriendSendMessage){
				Message msg=((FriendSendMessage)xwContext).mHandler.obtainMessage(4);
				msg.arg1=type;
				((FriendSendMessage)xwContext).mHandler.sendMessage(msg);
			}else if(xwContext instanceof FriendCall){
				Message msg=((FriendCall)xwContext).mHandler.obtainMessage(1);
				msg.arg1=type;
				((FriendCall)xwContext).mHandler.sendMessage(msg);
			}
		}*/


	}
	//收到视频数据
	public void videoDataReceive(byte []data,int width,int height){
		Log.e(TAG,"data:"+data.length+",width:"+width+",height:"+height);
		if(xwContext==null) return;

		//////////////不理会

		if(xwContext instanceof FriendVideoDisplay){
			//((FriendVideoDisplay)xwContext).mHandler.sendEmptyMessage(10);
			try{
				Message msg=((FriendVideoDisplay)xwContext).mHandler.obtainMessage(20,width,height,data);
				((FriendVideoDisplay)xwContext).mHandler.sendMessage(msg);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

	}


	//开始/结束音视频
	public void beginFriendVideo(int type){

		if (type==1)
			this.XWMsghandle.sendEmptyMessage(50);
		else
			this.XWMsghandle.sendEmptyMessage(51);
		
		
		/*
		if(xwContext==null) return;
		if(xwContext instanceof FriendVideoDisplay){
			if(type==1){
				((FriendVideoDisplay)xwContext).mHandler.sendEmptyMessage(21);
			}else if(type==2){
				((FriendVideoDisplay)xwContext).mHandler.sendEmptyMessage(22);
			}
		}*/


	}


	//聊天记录中,收到一条数据,从数据库中逐条读出
	public void addRecord(byte[] nickname,byte[] ctime,byte[] content,int flag){
		if(xwContext==null) return;
		String nickStr=null;
		String ctimeStr=null;
		String contentStr=null;
		if(nickname!=null)
			nickStr=new String(nickname);
		if(ctime!=null)
			ctimeStr=new String(ctime);
		if(content!=null)
			contentStr=new String(content);


		FriendChatRecordInfo fcri=new FriendChatRecordInfo();
		fcri.setNickname(nickStr);
		fcri.setCtime(ctimeStr);
		fcri.setContent(contentStr);
		fcri.setFlag(flag);

		Message msg=this.XWMsghandle.obtainMessage(54, fcri);
		this.XWMsghandle.sendMessage(msg);
		
		/*if(xwContext instanceof FriendChatRecord){
			Message msg=((FriendChatRecord)xwContext).mHandler.obtainMessage(3,fcri);
			((FriendChatRecord)xwContext).mHandler.sendMessage(msg);
		}*/

	}
	//登录结果返回
	public void loginResult(int res){
		Log.e(TAG,"loginResult:"+res);
		if(res==3){//登录成功
			BusProvider.getInstance().post(new LoginEvent(1));
		}else{//失败
			BusProvider.getInstance().post(new LoginEvent(2));
		}
	}

	//系统连接状态
	synchronized public void sysConnectStatus(int type,byte []loginInfo){
		Log.e("XIM","sysConnectStatus:"+type+";");

		this.iServerConnectStatus=type;

		String sysLoginInfo=null;
		try{
			if(loginInfo!=null)
				sysLoginInfo=new String(loginInfo,"GBK");
		}catch(Exception e){
			e.printStackTrace();
		}

		if (type==1)//////登录清0
		{
			XWDataCenter.lLoginBeginTime=0;
		}

		Log.e("XIM","Got sysConnectStatus type"+type+" name"+sysLoginInfo);
		if(type==0){//连接断开

			MainApplication.getInstance().bIsXIMConnectied=false;

			FriendNodeInfo fni=this.getFNINfoFromLoginName(sysLoginInfo);

			/////this.iServerConnectStatus=0;//////////////与服务器断开

			if(fni==null){
				//延时检查登录
				if(XWMsghandle!=null){
					XWMsghandle.postDelayed(new Runnable() {

						@Override
						public void run() {
							if(!isConnected){
								BusProvider.getInstance().post(new LoginEvent(2));
							}

						}
					}, 15*1000);
				}
				return;
			}
			if(this.cid==fni.getId()){
				this.isConnected=false;
				if((xwContext!=null)) {
					if ((xwContext instanceof FriendControl)) {
						Message msg = ((FriendControl) xwContext).mHandler.obtainMessage(16, xwContext.getResources().getString(R.string.alert_reconnection));
						((FriendControl) xwContext).mHandler.sendMessage(msg);
					} else if (xwContext instanceof FriendCall) {
						((FriendCall) xwContext).mHandler.sendEmptyMessage(4);
					}
				}else{
					BusProvider.getInstance().post(new LoginEvent(2));
				}

				if(XWMsghandle!=null){
					XWMsghandle.post(new Runnable() {
						@Override
						public void run() {
							xwApp.notificationOutline();
						}
					});
				}


				bIsLoginRunning=false;
			}
		}else if(type==1){//连接建立

			MainApplication.getInstance().bIsXIMConnectied=true;

			this.isConnected=true;
			////通知图标由service启动时生成

			if(XWMsghandle!=null){
				XWMsghandle.post(new Runnable() {
					@Override
					public void run() {
						xwApp.notificationOnline();
					}
				});
			}

			///////显示在线图标,同时登陆跳转
			BusProvider.getInstance().post(new LoginEvent(1));
			bIsLoginRunning=false;

		}
		//////////////2017-05-18,用于激活service唤醒处理
		try {
			Log.e("XIM", "XWScree on off");
			Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
			intentservice.setAction("DO_CHECK");
			MainApplication.getInstance().startService(intentservice);
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	//修改签名/密码结果
	public void signNamePasswdRes(int res){
		Log.e("xim","signNamePasswdRes"+res);
		if(res==1){//修改成功
			if(xwContext==null){
				/////this.logoutService(1);
			}else if(xwContext instanceof FriendControl){
				((FriendControl)xwContext).mHandler.sendEmptyMessage(25);
			}else if(xwContext instanceof FriendCall){
				((FriendCall)xwContext).mHandler.sendEmptyMessage(8);
			}else if(xwContext instanceof FriendChatRecord){
				((FriendChatRecord)xwContext).mHandler.sendEmptyMessage(5);
			}
		}else{
			if((xwContext!=null)&&(xwContext instanceof FriendControl)){
				((FriendControl)xwContext).mHandler.sendEmptyMessage(26);
			}
		}
	}




	public void fillSystemInfo(byte server_addr[],int server_port,int audio_volume ,int record_save_days){
		String server_addrStr=new String(server_addr);
		this.sysInfo.setServer_addr(server_addrStr);
		this.sysInfo.setServer_port(server_port);
		this.sysInfo.setAudio_volume(audio_volume);
		this.sysInfo.setRecord_save_days(record_save_days);
	}
	/////////////////////通话音视频参数回调!!!!	
	public void setRemoteVideoParam(int width,int height,int fps,int videocodec,int audiocodec){
		boolean bNeedEnterVideoWindow=(this.remote_video_codec ==-1) && (videocodec>=0) ;////*(width>0)&&(height>0)&&((this.remote_video_width==0)||(this.remote_video_height==0))*/;

		boolean bNeedStartAudio=(this.remote_audio_codec ==-1) && (audiocodec>=0);
		////boolean bNeedStopAudio=(this.remote_audio_codec !=-1) && (audiocodec==-1);	

		this.remote_video_width=width;
		this.remote_video_height=height;
		this.remote_video_fps=fps;
		this.remote_video_codec=videocodec;
		this.remote_audio_codec=audiocodec;
		Log.v("XIM","setRemoteVideoParam"+ width+","+height+","+fps+","+videocodec+","+audiocodec);



		/////////////////先启音频!!!!!!,2012-11-07
		if (bNeedStartAudio)
		{
			XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_4);
		}

		/*if (bNeedStopAudio)
		{
			XWMsghandle.sendEmptyMessage(6);			
		}*/
	}
	///////////////2015-05-23,使用安卓SharedPreferences处理账号口令保存，解决jni操作可能的问题。
	public int getLoginUser_new(byte[] number, byte passwd[])
	{
		try
		{
			SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(
					XWDataCenter.PackageName, 0);
			// /SharedPreferences.Editor editor = settings.edit();
			String sUser = "";
			String sPass = "";
			// ///////boolean isFirstRun=false;

			// ///if (!settings.getBoolean("FIRST_RUN", false))
			{// 只有第一次安装才创建icon
				sUser = settings.getString("LOGIN_USER", "");

				xwDC.loginName=sUser;

				System.arraycopy((sUser+"\0").getBytes(),0, number, 0, Math.min(sUser.length()+1, number.length));
			}

			// ///if (!settings.getBoolean("FIRST_RUN", false))
			{// 只有第一次安装才创建icon
				sPass = settings.getString("LOGIN_PASSWORD", "");

				System.arraycopy((sPass+"\0").getBytes(),0, passwd, 0, Math.min(sPass.length()+1, passwd.length));
			}

			///////////2015-05-23,如果取不到，则尝试调用jni接口，以兼容老的版本.
			/*if ((sUser.length()==0)||(sPass.length()==0))
			{
				getLoginUser(number, passwd);
			}*/

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return 0;
	}
	public void saveLoginUser(byte[] number, byte passwd[],
							  int login_status)
	{
		try
		{
			String sUser=new String(number).trim();
			String sPass =new String(passwd).trim();
			SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(
					XWDataCenter.PackageName, 0);
			// /SharedPreferences.Editor editor = settings.edit();
			SharedPreferences.Editor editor = settings.edit();
			{// 只有第一次安装才创建icon
				editor.putString("LOGIN_USER", sUser);
				editor.putString("LOGIN_PASSWORD", sPass);
				editor.commit();
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}


	//***********************************************************************************************************/
	//***********************************************************************************************************/
	//****************************************以上方法为被JNI调用:**************************************************/
	//***********************************************************************************************************/
	//***********************************************************************************************************/




	///////恢复好友列表和头像列表数据
	public boolean restoreFriendData(){
		if(FriendNodeDB.getAllFriends().size()>0){
		  FriendNodeDB.restoreHeadMap();
			return true;
		}else if(FriendNodeDB.existsBackupFriend()
				&&com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(MainApplication.getInstance())){
			///////解码备份
			if(FriendNodeDB.restoreFriends()){
				FriendNodeDB.restoreHeadMap();
				return true;
			}
		}
		return false;
	}

	//清理内存用户列表
	 void cleanNodeList(){
		if(nodesInfo!=null){
			nodesInfo.clear();
		}
		bHasHeadRun=false;
	}

	/**
	 *清理消息对应的文件
	 */
	public static  void clearMsgFile(ChatMsgEntity entity){
		if(entity!=null){
			String path=entity.getFilePath();
			if(!TextUtils.isEmpty(path)){
				File encryptF=new File(path+JRSConstants.ENCRYPT_END);//加密文件
				if(encryptF.exists()){
					encryptF.delete();
				}
				File decryptF=new File(getDecryptFilepath(entity));//解码文件
				if(decryptF.exists()){
					decryptF.delete();
				}
				File aesF=new File(path+"_aes");  // TODO ?
				if(aesF.exists()){
					UriConfig.delete(path+"_aes");
				}
			}
		}
	}

	/**
	 * 清理所有已读文件/消息
	 */
	public static void clearALLReadFile(){
		//清理history
		if(chatHistoryDB!=null){
			chatHistoryDB.clearAll(XWDataCenter.getCurAccount());
		}
		//清理msg
		if(msgDB!=null){
			for(FriendNodeInfo node:FriendControl.friendList){
				if(node!=null){
					msgDB.deleteFriendNode(XWDataCenter.getCurAccount(),node.getLogin_name());
				}
			}
		}
		//清理文件
		for(FriendNodeInfo node:FriendControl.friendList) {
			if(node!=null) {
				String path = UriConfig.getAesFilePath(node.getLogin_name());
				File dir=new File(path);
				if(dir.exists()){
					FileUtils.deleteFileDir(path);
					dir.delete();
				}
			}

		}
	}

	/**
	 * 获取阅后即焚
	 */
	public static boolean getReadSwitch(){
		SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(
				XWDataCenter.PackageName, 0);
		return settings.getBoolean("read_switch", false);
	}
	/**
	 * 设置阅后即焚
	 * @return
	 */
	public static void setReadSwitch(boolean read){
		SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(
				XWDataCenter.PackageName, 0);
		settings.edit().putBoolean("read_switch", read).commit();
	}

	///自动登录状态
	public static boolean getAutoLogin(){
		SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);
		return settings.getBoolean("AUTO_LOGIN",false) ;
	}

	///自动登录状态设置
	public static void setAutoLogin(boolean isAuto){
		SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);
		settings.edit().putBoolean("AUTO_LOGIN",isAuto).apply();
	}

	///设置服务器IP
	public static void setXIMIP(String ip){
		SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);
		settings.edit().putString("xim_ip",ip).commit();
	}

	////获取服务器IP,
	public static String getXIMIP(){
		if(!AppConfig.IP_CONFIG){
			return ServerConfig.XIM_SERVER_IP;
		}
		SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);
		String ip=settings.getString("xim_ip",ServerConfig.XIM_TEST_DEF_IP);
		if(TextUtils.isEmpty(ip)){
			ip= ServerConfig.XIM_SERVER_IP;
		}
		return ip;
	}

	/**
	 * 获取当前用户账号
	 */
	public static String getCurAccount(){
		String account=null;

		if(xwDC!=null){
			if(xwDC.loginName!=null&&xwDC.loginName.length()>0){
				return xwDC.loginName;
			}else{
				byte nums[] = new byte[33];
				byte pass[] = new byte[33];
				xwDC.getLoginUser_new(nums, pass);
				account=new String(nums).trim();
			}

		}
		return account;
	}
	/**
	 * 获取一个好友的信息（从数据库或内存）
	 * @return
	 */
	public FriendNodeInfo getANodeInfo(String loginName){
		FriendNodeInfo nodeInfo =null;
		if(loginName==null){
			return nodeInfo;
		}
		if(friendDB!=null){
			nodeInfo= friendDB.getAFriend(XWDataCenter.getCurAccount(), loginName);
		}
		if(nodeInfo==null){
			nodeInfo=xwDC.getFNINfoFromLoginName(loginName);
		}
		return nodeInfo;
	}







	synchronized public static void ShowToastMsg(String sMsg)
	{
		if ((XWDataCenter.xwDC!=null)&&(XWDataCenter.xwDC.XWMsghandle!=null))
		{
			Message msg=XWDataCenter.xwDC.XWMsghandle.obtainMessage(17,new String(sMsg));
			XWDataCenter.xwDC.XWMsghandle.sendMessage(msg);
		}
	}


	////////////////////////////////////////////启动人工坐席服务!!!!!!!!!!!!!!
	public static void startXWHelper(final Context ctx)
	{
		if (XWDataCenter.xwDC==null)
			return;

		Log.e("XIM","startXWHelper");
		if (XWDataCenter.xwDC.XWStartWeiXinAudioWithTimeLimit("",30)!=0)
		{
			ShowToastMsg(xechwic.android.XWCodeTrans.doTrans("Mic设备打开出错!"));
			return;
		}

		Log.e("XIM","startXWHelper start Thread");
		//////////////////////启动线程来发送微信!!!!!!!!!!!!!!!!!
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				{
					byte[] helper=new byte[33];
					int iRet=-1;

					for (int i=0;i<3;i++)
					{
						iRet=XWDataCenter.xwDC.XWRequestHelper(helper);
						Log.e("XIM","startXWHelper XWRequestHelper "+iRet);
						if (iRet==0)
							break;
					}
					if (iRet!=0)
					{
						XWDataCenter.xwDC.XWStopWeiXinAudio();
						ShowToastMsg(xechwic.android.XWCodeTrans.doTrans("无法连接人工客服!"));
						return;
					}

					XWDataCenter.xwDC.sHelperID=(new String(helper)).trim();
					Log.e("XIM","startXWHelper sHelperID "+XWDataCenter.xwDC.sHelperID);

					if ((XWDataCenter.xwDC.sHelperID==null)||(XWDataCenter.xwDC.sHelperID.equals("")))
					{
						XWDataCenter.xwDC.XWStopWeiXinAudio();
						ShowToastMsg(xechwic.android.XWCodeTrans.doTrans("无法连接人工客服!"));
						return;
					}

					XWDataCenter.xwDC.XWRequestTempTalk((XWDataCenter.xwDC.loginName+"\0").getBytes(),0,(XWDataCenter.xwDC.sHelperID+"\0").getBytes(),0);
					long lLastTime=System.currentTimeMillis();
					//					boolean bHasGotFriend=false;
					FriendNodeInfo fni=null;
					while (System.currentTimeMillis()-lLastTime<=60000)
					{
						try
						{
							Thread.sleep(100);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}

						fni=XWDataCenter.xwDC.getFNINfoFromLoginName(XWDataCenter.xwDC.sHelperID);
						if (fni!=null)
							break;
					}

					if (fni==null)
					{
						Log.e("XIM","startXWHelper fni==null ");
						XWDataCenter.xwDC.XWStopWeiXinAudio();

						ShowToastMsg(xechwic.android.XWCodeTrans.doTrans("网络通讯出错!"));
						return;
					}
					while ((XWDataCenter.xwDC.xwAudioRecord!=null)&& (XWDataCenter.xwDC.xwAudioRecord.isRecording))
					{
						try
						{
							Thread.sleep(100);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					////////////////////////Send!!!!!!!!!!!!!
					{
						String sFilePath = XWDataCenter.xwDC.XWStopWeiXinAudio();
						/*Util.copyFile(new File(sFilePath), UriConfig.getSavePath()
								+ "/voice/" + xwDC.cid + "/" + sFilePath.hashCode()
								+ ".xwx");*/
						String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date());
						Log.e("XIM","startXWHelper XWStopWeiXinAudio ");
						{
							String conStr = "(:" + "voice" + ")" + sFilePath;
							conStr += "\0";
							ctime += "\0";
							Log.e("XIM","startXWHelper xwDC.insertRecord");
							Log.e("XIM","save voice:"+conStr);
						}

						Log.e("XIM","startXWHelper XWWeiXinRequestSendFile ");
						int iOffLine=1;
						int iret=xwDC.XWWeiXinRequestSendFile((fni.getLogin_name() + "\0").getBytes(),
								(sFilePath + "\0").getBytes(), XWDataCenter.XWIntToBytes(iOffLine));
						Log.e("XIM","XWWeiXinRequestSendFile "+sFilePath+" "+fni.getLogin_name()+" "+iret);

						Log.e("XIM","startXWHelper XWWeiXinRequestSendFile end.");
						if (iret==0)
							ShowToastMsg(xechwic.android.XWCodeTrans.doTrans("正在提交人工客服..."));
						else
							ShowToastMsg(xechwic.android.XWCodeTrans.doTrans("提交人工客服出错!"));
					}

				}
			}
		});

	}


	/////////////去掉保存时号码的空格等。。。。
	public static String RegularPhoneNumber(String sMsg)
	{
		if (sMsg==null)
			return "";


		sMsg=sMsg.trim();
		sMsg=sMsg.replaceAll(" ", "");
		sMsg=sMsg.replaceAll("-", "");
		sMsg=sMsg.replaceAll("_", "");
		return sMsg;
	}

	///////////////获取访问页面的口令串
	public static String getWEBAccessPassword()
	{
		String sPass="";

		if (xwDC!=null)
		{
			byte[] bts=new byte[33];
			int iRet=xwDC.getWEBAuthenSerial(bts);

			////////Log.v("XIM","getWEBAccessPassword iRet:"+iRet);

			if (iRet==0)
			{
				try
				{
					sPass=new String(bts,"GBK").trim();

					/////Log.v("XIM","getWEBAccessPassword:"+sPass);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		return sPass;
	}


	////////////////////////////
	static public byte[] gbk2utf8(String chenese){
		char c[] = chenese.toCharArray();
		byte [] fullByte =new byte[3*c.length];
		for(int i=0; i<c.length; i++){
			int m = (int)c[i];
			String word = Integer.toBinaryString(m);
			// System.out.println(word);

			StringBuffer sb = new StringBuffer();
			int len = 16 - word.length();
			//补零
			for(int j=0; j<len; j++){
				sb.append("0");
			}
			sb.append(word);
			sb.insert(0, "1110");
			sb.insert(8, "10");
			sb.insert(16, "10");

//         System.out.println(sb.toString());

			String s1 = sb.substring(0, 8);
			String s2 = sb.substring(8, 16);
			String s3 = sb.substring(16);

			byte b0 = Integer.valueOf(s1, 2).byteValue();
			byte b1 = Integer.valueOf(s2, 2).byteValue();
			byte b2 = Integer.valueOf(s3, 2).byteValue();
			byte[] bf = new byte[3];
			bf[0] = b0;
			fullByte[i*3] = bf[0];
			bf[1] = b1;
			fullByte[i*3+1] = bf[1];
			bf[2] = b2;
			fullByte[i*3+2] = bf[2];

		}
		return fullByte;
	}

	////////////////////////////
	static public String gbk2utf8str(String chenese){
		try
		{
			return new String(gbk2utf8(chenese),"UTF-8");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return "";
	}


	////////////////////////////2014-07-24,处理繁体转gbk后 urldecode可能出乱码的问题!!!!
	static public String XWdecodeurl(String surl,String sCharSet){

		if (surl==null)
			return null;
		if ((sCharSet==null)||(sCharSet.length()==0))
		{
			sCharSet="GBK";
		}
		try
		{
			String message=URLDecoder.decode(surl, "iso-8859-1");
			message=new String(message.getBytes("iso-8859-1"),sCharSet);
			return message;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return "";
		}
	}


	///////////////////2014-08-18,将服务器的北京时区时间转成本地的时间字串
	/////时间串格式 yyyy-MM-dd HH:mm:ss
	public static String TimeConvertFromShangHaiToLocal(String sShangHaiTime)
	{
		String ctime="";
		if ((sShangHaiTime==null)||(sShangHaiTime.length()==0))
			return "";
		try
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//////df.setTimeZone();
			Date date = df.parse(sShangHaiTime.trim());

			Calendar cal = Calendar.getInstance();
			int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);

			//3、取得夏令时差：
			int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);

			//4、从本地时间里扣除这些差量，即可以取得UTC时间：
			////cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

			Date localTime=new Date(date.getTime()-8*3600000+(zoneOffset+dstOffset));
			///new Date(dateUTC.getTime()+(zoneOffset + dstOffset));

			ctime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(localTime);
			ctime+="\0";

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return ctime;
	}



	/////时间串格式 yyyy-MM-dd HH:mm:ss
	public static Date TimeConvertFromShangHaiToLocal2(String sShangHaiTime)
	{
		////String ctime="";
		if ((sShangHaiTime==null)||(sShangHaiTime.length()==0))
			return new Date();
		try
		{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//////df.setTimeZone();
			Date date = df.parse(sShangHaiTime.trim());

			Calendar cal = Calendar.getInstance();
			int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);

			//3、取得夏令时差：
			int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);

			//4、从本地时间里扣除这些差量，即可以取得UTC时间：
			////cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

			Date localTime=new Date(date.getTime()-8*3600000+(zoneOffset+dstOffset));
			///new Date(dateUTC.getTime()+(zoneOffset + dstOffset));

			//ctime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(localTime);
			//ctime+="\0";
			return localTime;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return new Date();
		///return ctime;
	}

	public static String getANSIStringFromBytes(byte[] bt)
	{
		int i;
		for (i=0;i<bt.length;i++)
		{
			if (bt[i]==0)
				break;
		}

		byte[] btnew=new byte[i];
		System.arraycopy(bt,0, btnew, 0, i);


		String sRet=null;
		try
		{
			sRet=(new String(btnew,"GBK"));
		}
		catch(Exception ex)
		{

		}
		return sRet;
	}


	///////////////////加密一个文件,
	public static boolean AESEncodeFile(String sFile,String sToFile,String sAESPassword)
	{
		boolean bRet=false;

		if ((sAESPassword==null)||(sAESPassword.length()==0))
		{
			try {
				Util.copyFile(new File(sFile), sToFile);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
			return true;
		}

		if ((sFile==null)||(sToFile==null))
			return false;

		File file =null;
		FileInputStream inStream=null;


		File file2 =null;
		FileOutputStream inStream2=null;

		try
		{
			try
			{
				file = new File(sFile);

				file2 = new File(sToFile) ;

				if (!file.exists())
				{
					Log.e("XIM","AESEncodeFile error not found :"+sFile);
					return false;
				}

				inStream = new FileInputStream(file);
				inStream2 = new FileOutputStream(file2);

				byte[] byteSrc=new byte[1024];
				byte[] byteDst=new byte[1024+16];
				int iReadLen;
				try
				{
					while (true)
					{
						iReadLen=0;
						try
						{
							iReadLen=inStream.read(byteSrc, 0, byteSrc.length);
						}
						catch(Exception ex1)
						{
							ex1.printStackTrace();
						}

						if (iReadLen<=0)
						{
							break;
						}

						if (iReadLen==byteSrc.length)
						{
							int iLen=xwDC.XWNetphoneAESEncodeBytes(byteSrc,byteDst,(sAESPassword+"\0").getBytes("iso-8859-1"));
							if (iLen>0)
								inStream2.write(byteDst, 0, iLen);
						}
						else
						{
							if (iReadLen>0)
							{
								byte bytetemp[]=new byte[iReadLen];

								System.arraycopy(byteSrc,0, bytetemp,  0, iReadLen);

								int iLen=xwDC.XWNetphoneAESEncodeBytes(bytetemp,byteDst,(sAESPassword+"\0").getBytes("iso-8859-1"));
								if (iLen>0)
									inStream2.write(byteDst, 0, iLen);
							}

							break;
						}

					}
					bRet=true;
				}

				catch(Exception ex)
				{

				}

			}
			catch(Exception ex1)
			{

			}
		}
		finally
		{
			if (inStream!=null)
			{
				try
				{
					inStream.close();
				}
				catch(Exception ex2)
				{

				}
			}

			if (inStream2!=null)
			{
				try
				{
					inStream2.close();
				}
				catch(Exception ex2)
				{

				}
			}
		}

		return bRet;
	}


	/////////////////解压一个文件
	public static boolean AESDecodeFile(String sFile,String sToFile,String sAESPassword)
	{
		boolean bRet=false;

		if ((sAESPassword==null)||(sAESPassword.length()==0))
		{
			try {
				Util.copyFile(new File(sFile), sToFile);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
			return true;
		}

		if ((sAESPassword==null)||(sFile==null)||(sToFile==null))
			return false;

		File file =null;
		FileInputStream inStream=null;


		File file2 =null;
		FileOutputStream inStream2=null;
		try
		{
			try
			{
				file = new File(sFile);

				file2 = new File(sToFile) ;

				if (!file.exists())
				{
					Log.e("XIM","AESDecodeFile error not found :"+sFile);
					return false;
				}

				inStream = new FileInputStream(file);
				inStream2 = new FileOutputStream(file2);

				byte[] byteSrc=new byte[1024+16];
				byte[] byteDst=new byte[1024+16];
				int iReadLen;

				while (true)
				{
					iReadLen=0;
					try
					{
						iReadLen=inStream.read(byteSrc, 0, byteSrc.length);
					}
					catch(Exception ex1)
					{
						ex1.printStackTrace();
					}

					if (iReadLen<=0)
					{
						break;
					}
					else
					if (iReadLen==byteSrc.length)
					{
						int iLen=xwDC.XWNetphoneAESDecodeBytes(byteSrc,byteDst,(sAESPassword+"\0").getBytes("iso-8859-1"));
						if (iLen>0)
							inStream2.write(byteDst, 0, iLen);
						else
						{
							return false;
						}
					}
					else
					{
						if (iReadLen>0)
						{
							byte bytetemp[]=new byte[iReadLen];

							System.arraycopy(byteSrc,0, bytetemp,  0, iReadLen);

							int iLen=xwDC.XWNetphoneAESDecodeBytes(bytetemp,byteDst,(sAESPassword+"\0").getBytes("iso-8859-1"));
							if (iLen>0)
								inStream2.write(byteDst, 0, iLen);
							else
								return false;
						}
						break;
					}

				}
				bRet=true;
			}
			catch(Exception ex1)
			{

			}
		}
		finally
		{
			if (inStream!=null)
			{
				try
				{
					inStream.close();
				}
				catch(Exception ex2)
				{

				}
			}

			if (inStream2!=null)
			{
				try
				{
					inStream2.close();
				}
				catch(Exception ex2)
				{

				}
			}
		}

		return bRet;
	}


	public static String getFriendAESPassword(String sFrom,String sTo)
	{

		if ((sFrom==null)||(sTo==null)||(sFrom.equals(sTo)))
			return null;
		try
		{

			///////////////取本地存储
			String sUser1PassTimeStamp=null;
			String sUser1Pass=null;
			String sUser2PassTimeStamp=null;
			String sUser2Pass=null;
			String sOK=null;

			String sToUser;
			if (!xwDC.loginName.equals(sFrom))
				sToUser=sFrom;
			else
				sToUser=sTo;

			sToUser=XWDataCenter.RegularPhoneNumber(sToUser);

			////////////////2014-09-22,对于非好友,或者好友不在线,不使用AES加密!!!!
			{
				//XWDataCenter.xwDC.getFNINfoFromLoginName(sToUser); 可能会引起阻塞 yangj 20170110
				FriendNodeInfo fni= XWDataCenter.xwDC.getFNINfoFromLoginName(sToUser);
				if ((fni==null))
				{
					return null;
				}
			}


			boolean bIsKeyOK=true;

			String sAESKeyFile=xwDC.sRASKeyPath+"/aeskey_"+xwDC.loginName+"_"+sToUser+".key";

			synchronized (iCreditMutex)
			{
				if (!FileUtil.isFileExist(sAESKeyFile))
				{
					bIsKeyOK=false;
				}
				else
				{
					sUser1PassTimeStamp=RSAUtil.readFileLine(sAESKeyFile, 1);
					if (sUser1PassTimeStamp==null)
						bIsKeyOK=false;
					sUser1Pass=RSAUtil.readFileLine(sAESKeyFile, 2);
					if (sUser1Pass==null)
						bIsKeyOK=false;

					/////////////////对AES密钥进行sd卡解密保护
					byte[] btSDCardDecode=RSAUtil.decryptByPrivateKey(sUser1Pass.getBytes("ISO-8859-1"),XWDataCenter.xwDC.sPrivatekeyFile);///com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data(sUser1Pass.getBytes("ISO-8859-1"));
					sUser1Pass=new String (btSDCardDecode,"ISO-8859-1");


					sUser2PassTimeStamp=RSAUtil.readFileLine(sAESKeyFile, 3);
					if (sUser2PassTimeStamp==null)
						bIsKeyOK=false;
					sUser2Pass=RSAUtil.readFileLine(sAESKeyFile, 4);
					if (sUser2Pass==null)
						bIsKeyOK=false;

					/////////////////对AES密钥进行sd卡解密保护
					btSDCardDecode=RSAUtil.decryptByPrivateKey(sUser2Pass.getBytes("ISO-8859-1"),XWDataCenter.xwDC.sPrivatekeyFile);///com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data(sUser2Pass.getBytes("ISO-8859-1"));
					sUser2Pass=new String (btSDCardDecode,"ISO-8859-1");


					sOK=RSAUtil.readFileLine(sAESKeyFile, 5);
					if (sOK==null)
						bIsKeyOK=false;
				}
			}

			if (!bIsKeyOK)
			{
				/////////XWDataCenter.SendCreditMessage(sToUser,XWDataCenter.CREDIT_REQUEST);
				return null;
			}

			byte[] btKey=new byte[33];

			sUser1Pass=sUser1Pass.trim();
			sUser2Pass=sUser2Pass.trim();
			if (sUser1PassTimeStamp.length()>0
					&& sUser1Pass.length()>0
					&& sUser2PassTimeStamp.length()>0
					&& sUser2Pass.length()>0

					&& ("1".equals(sOK))
					)
			{
				MessageDigest md5;
				try{
					//////fis = new FileInputStream(filename);
					md5 = MessageDigest.getInstance("MD5");

					if (xwDC.loginName.compareTo(sToUser)<0)
					{
						md5.update(sUser1Pass.getBytes("ISO-8859-1"),0,sUser1Pass.length());
						md5.update(sUser2Pass.getBytes("ISO-8859-1"),0,sUser2Pass.length());
					}
					else
					{
						md5.update(sUser2Pass.getBytes("ISO-8859-1"),0,sUser2Pass.length());
						md5.update(sUser1Pass.getBytes("ISO-8859-1"),0,sUser1Pass.length());
					}

					///////fis.close();
					String sKey=  RSAUtil.toHexString(md5.digest());
					return sKey;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
			else
			{
				///////XWDataCenter.SendCreditMessage(sToUser,XWDataCenter.CREDIT_REQUEST);
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}


	////////////////////交换密钥文字消息前缀
	public final static String CREDIT_PREFIX="\u0003CREDIT\u0003";


	public final static String CREDIT_REQUEST="REQUEST";
	public final static String CREDIT_REQUEST_REPLY="REPLY";

	public final static int  AES_UPDATE_DAYS=7;  //////AES每七天一换
	public final static int  RSA_KEY_UPDATE_DAYS=28;  //////RSA每一周一换


	public final static Integer iCreditMutex=new Integer(1);


	/////////2014-10-28
	public static void SendCreditMessage(String sToUser,String sRequestCommand)
	{
		/*
		try
		{
		  Message msg=XWDataCenter.xwDC.XWMsghandle.obtainMessage(57, new String(sToUser+"\r"+sRequestCommand));
		  XWDataCenter.xwDC.XWMsghandle.sendMessage(msg);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	    */
		SendCreditMessage2(sToUser,sRequestCommand);
	}

	public static void SendCreditMessage2(final String sToUser,String sRequestCommand)
	{
		Log.v("XIM","SendCreditMessage:"+sToUser);

		//////////////增加检查
		if ((sToUser==null)||(sToUser.length()==0))
			return;

		RSAUtil.generateAESKey(null);
		try
		{
			/////////////////2015-01-15,只有当对方在线时才协商
			FriendNodeInfo fni=xwDC.getFNINfoFromLoginName(sToUser);

			if (fni==null) {
				Log.e("XIM","SendCreditMessage fni==null");
				return;
			}


			
			/*if ( xwDC.xwApp.getResources().getString(R.string.status_display_outline).equals(fni.getOnline_status())
	          || xwDC.xwApp.getResources().getString(R.string.status_cutout).equals(fni.getOnline_status())
	          )*/
			/////////////////2016-10-10
			if (XWCodeTrans.doTrans("断开").equals(fni.getOnline_status())
					||
					xwDC.xwApp.getResources().getString(R.string.status_display_outline).equals(fni.getOnline_status())
					|| xwDC.xwApp.getResources().getString(R.string.status_cutout).equals(fni.getOnline_status())
					) {
				Log.e("XIM","SendCreditMessage user offline ");
				return;
			}

			/////////////////////////
			if ((sToUser==null)||(sToUser.equals(xwDC.loginName)))
			{
				Log.e("XIM","SendCreditMessage sTouser invalid.");
				return ;
			}
			String sCreditMsg=CREDIT_PREFIX+"\r"+sRequestCommand+"\r";


			String sAESKeyFile=xwDC.sRASKeyPath+"/aeskey_"+xwDC.loginName+"_"+sToUser+".key";



			///////////pubkey
			String sSelfPubkeyTimeStamp=RSAUtil.readFileLine(xwDC.sPubkeyFile, 1);
			String sToUserPubkeyTimeStamp=RSAUtil.readFileLine(xwDC.sRASKeyPath+"/pubkey_"+sToUser+".rsa",1);


			if ((sSelfPubkeyTimeStamp==null)||(sSelfPubkeyTimeStamp.length()==0)
					|| (!FileUtil.isFileExist(xwDC.sPubkeyFile))
					)
			{
				/*synchronized (iCreditMutex)
				{
                    UriConfig.delete(sAESKeyFile);
				}*/
				//Log.v("XIM","SendCreditMessage no sSelfPubkeyTimeStamp");
				////return;

				sSelfPubkeyTimeStamp="";

				////////////////////如果本地pubkey无效,置对方pubkey也为空.
				sToUserPubkeyTimeStamp="";

				Log.e("XIM","SendCreditMessage no sSelfPubkeyTimeStamp or no valid file!");
				return;
			}

			if ((sToUserPubkeyTimeStamp==null) || (sToUserPubkeyTimeStamp.length()==0)
					||  (!FileUtil.isFileExist(xwDC.sRASKeyPath+"/pubkey_"+sToUser+".rsa"))
					)
			{
				/*synchronized (iCreditMutex)
				{
                    UriConfig.delete(sAESKeyFile);
				}*/

				sToUserPubkeyTimeStamp="";


				if (fni!=null)
				{

					///////////
					sCreditMsg=sCreditMsg+"PubKey:\r"+sSelfPubkeyTimeStamp+"\r"+sToUserPubkeyTimeStamp+"\r";

					String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date());

					///////////////////////////////
					////////////说明:第四参数null,可传入 byte[33]字节。然后调用queryMessageStatus来跟踪它。
					byte[] traceNo=new byte[33];
					xwDC.sendOnlineMessage(xwDC.cid, fni.getId(), (sCreditMsg+"\0").getBytes("GBK"),
							(ctime+"\0").getBytes("GBK"),traceNo);


					Log.v("XIM","xwDC.sendMessage:"+sCreditMsg);

				}

				return;
			}

			String sUser1PassTimeStamp;
			String sUser1Pass;
			String sUser2PassTimeStamp;
			String sUser2Pass;

			//////String sOK="1";

			synchronized (iCreditMutex)
			{
				//////
				{
					if (!FileUtil.isFileExist(sAESKeyFile))
					{
						sUser1PassTimeStamp="";
						sUser1Pass="";
						sUser2PassTimeStamp="";
						sUser2Pass="";
					}
					else
					{
						sUser1PassTimeStamp=RSAUtil.readFileLine(sAESKeyFile, 1);
						if (sUser1PassTimeStamp==null)
							sUser1PassTimeStamp="";
						sUser1Pass=RSAUtil.readFileLine(sAESKeyFile, 2);
						if (sUser1Pass==null)
							sUser1Pass="";

						byte[] btSDCardDecode=RSAUtil.decryptByPrivateKey(sUser1Pass.getBytes("ISO-8859-1"),XWDataCenter.xwDC.sPrivatekeyFile);///com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data(sUser1Pass.getBytes("ISO-8859-1"));
						if(btSDCardDecode!=null){
							sUser1Pass=new String (btSDCardDecode,"ISO-8859-1");
						}


						sUser2PassTimeStamp=RSAUtil.readFileLine(sAESKeyFile, 3);
						if (sUser2PassTimeStamp==null)
							sUser2PassTimeStamp="";
						sUser2Pass=RSAUtil.readFileLine(sAESKeyFile, 4);
						if (sUser2Pass==null)
							sUser2Pass="";

						btSDCardDecode=RSAUtil.decryptByPrivateKey(sUser2Pass.getBytes("ISO-8859-1"),XWDataCenter.xwDC.sPrivatekeyFile);///com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data(sUser2Pass.getBytes("ISO-8859-1"));
						if(btSDCardDecode!=null)
						sUser2Pass=new String (btSDCardDecode,"ISO-8859-1");
					}
				}
			}

			{
				boolean bNeedToUpdate=false;

				if ( (sUser1PassTimeStamp.length()==0) || (sUser1Pass.length()==0))
				{
					bNeedToUpdate=true;
				}
				else
				{
					{
						Date dtCreditTime=RSAUtil.ParseTimeStamp(sUser1PassTimeStamp);
						if (dtCreditTime==null)
						{
							bNeedToUpdate=true;
						}
						else
						if (Math.abs(new Date().getTime()-dtCreditTime.getTime())>(long)86400*1000*AES_UPDATE_DAYS) /////7 days
						{
							bNeedToUpdate=true;
						}
					}
				}


				if (bNeedToUpdate)
				{
					sUser1PassTimeStamp=RSAUtil.getTimeStamp();
					sUser1Pass=RSAUtil.generateAESKey(sUser1Pass);

					synchronized (iCreditMutex)
					{
						RSAUtil.SaveAESKeysToFile(sAESKeyFile, sUser1PassTimeStamp, sUser1Pass, sUser2PassTimeStamp, sUser2Pass
								,"0");

						Log.v("XIM","RSAUtil.SaveAESKeysToFile");
					}
				}

				//////////////////////////
			}

			//////////////////////send message

			{
				/////FriendNodeInfo fni=xwDC.getFNINfoFromLoginName(sToUser);

				if (fni!=null)
				{
					///////////
					sCreditMsg=sCreditMsg+"PubKey:\r"+sSelfPubkeyTimeStamp+"\r"+sToUserPubkeyTimeStamp+"\r";


					if (sToUserPubkeyTimeStamp.length()>0) //////
					{
						String sAESKey="AESKey:\r"+sUser1PassTimeStamp
								+"\r"+sUser1Pass
								+"\r"+sUser2PassTimeStamp
								+"\r" ////////+sUser2Pass
								;


						String sPubkeyFile=xwDC.sRASKeyPath+"/pubkey_"+sToUser+".rsa";

						synchronized (iCreditMutex)
						{
							byte[] btEncoded=Base64Util.encode(RSAUtil.encryptByPubKey(sAESKey.getBytes("ISO-8859-1"),sPubkeyFile));
							if (btEncoded!=null)
							{
								try
								{
									sCreditMsg+=new String(btEncoded,"ISO-8859-1");
								}
								catch(Exception ex)
								{

								}
							}
						}
					}
					String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date());

					///////////////////////////////
					////////////说明:第四参数null,可传入 byte[33]字节。然后调用queryMessageStatus来跟踪它。
					byte[] traceNo=new byte[33];
					xwDC.sendOnlineMessage(xwDC.cid, fni.getId(), (sCreditMsg+"\0").getBytes("GBK"),
							(ctime+"\0").getBytes("GBK"),traceNo);


					Log.v("XIM","xwDC.sendMessage aes key ");
				}

			}


		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}


	public Hashtable htSendPublicRecord=new Hashtable();
	public Hashtable htCreditMsgRecord=new Hashtable();
	public static long lLastSendWeiXinFailed=0;
	////////////if true 
	public static boolean HandleCreditMessage(final String sToUser,String sCommand,Date dt)
	{
		Log.v("XIM","HandleCreditMessage: sToUser:"+sToUser+" sCommand:"+sCommand);

		if ((sCommand==null)||(sCommand.length()<CREDIT_PREFIX.length()))
			return false;
		int iPos=sCommand.indexOf(CREDIT_PREFIX);
		if (iPos<0)
			return false;
		/////////////不能是null
		if (sToUser==null)
			return true;

		try
		{
			iPos+=((CREDIT_PREFIX+"\r").length());

			int iPos2=-1;
			iPos2=sCommand.indexOf("\r", iPos);
			if (iPos2<0) {
				return false;
			}

			Log.v("XIM","HandleCreditMessage sRequest: iPos"+ iPos+ " iPos2 "+iPos2);


			String sRequest=sCommand.substring(iPos, iPos2); //////////
			iPos=iPos2+1;
			Log.v("XIM","HandleCreditMessage sRequest: "+sRequest);


			iPos2=sCommand.indexOf("PubKey:\r", iPos);
			if (iPos2<0) {
				////SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return false;
			}

			iPos=iPos2+"PubKey:\r".length();


			iPos2=sCommand.indexOf("\r", iPos);
			if (iPos2<0) {
				////SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return false;
			}
			String sFriendPubkeyTimeStamp=sCommand.substring(iPos, iPos2);
			iPos=iPos2+1;

			iPos2=sCommand.indexOf("\r", iPos);
			if (iPos2<0) {
				///SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return false;
			}
			String sMyPubkeyTimeStamp=sCommand.substring(iPos, iPos2);
			iPos=iPos2+1;


			/////////////////////判断上一次收到的消息时间,时间晚的不管。
			if (dt!=null)
			{
				Long LToUserTime=(Long)XWDataCenter.xwDC.htCreditMsgRecord.get(sToUser);
				if (LToUserTime!=null)
				{
					if ( dt.getTime()<LToUserTime.longValue())////////60秒内不重发pubkey
					{
						Log.e("XIM","HandleCreditMessage obsolete credit msg.");
						return true;/////不理会!!!!!!!!!!!2016-10-27
					}
				}
				XWDataCenter.xwDC.htCreditMsgRecord.remove(sToUser);
				XWDataCenter.xwDC.htCreditMsgRecord.put(sToUser,new Long(dt.getTime()));
			}



			boolean bPubKeyOK=true;
			////boolean bNeedReply=false;

			String sSelfPubkeyTimeStamp=null;
			String sToUserPubkeyTimeStamp=null;

			String sAESKeyFile=xwDC.sRASKeyPath+"/aeskey_"+xwDC.loginName+"_"+sToUser+".key";
			String sRSAPubKeyFile=xwDC.sRASKeyPath+"/pubkey_"+sToUser+".rsa";

			//////////////////////////////////////
			synchronized(iCreditMutex)
			{
				///////////pubkey
				sSelfPubkeyTimeStamp=RSAUtil.readFileLine(xwDC.sPubkeyFile, 1);
				sToUserPubkeyTimeStamp=RSAUtil.readFileLine(sRSAPubKeyFile,1);
			}


			Log.v("XIM","HandleCreditMessage sSelfPubkeyTimeStamp:"+sSelfPubkeyTimeStamp+" sFriendPubkeyTimeStamp:"+sFriendPubkeyTimeStamp);


			////////本地pubkey无效!!!!!
			if ((sSelfPubkeyTimeStamp==null)||(sSelfPubkeyTimeStamp.length()==0))
			{
				Log.e("XIM","HandleCreditMessage: Invalid self pub key!");
				return true;
			}

			boolean bNeedSendPublicKey=true;
			try
			{
				Long LToUserTime=(Long)XWDataCenter.xwDC.htSendPublicRecord.get(sToUser);
				if (LToUserTime!=null)
				{
					if ( (System.currentTimeMillis()-LToUserTime.longValue())<=30*1000)////////60秒内不重发pubkey
					{
						bNeedSendPublicKey=false;
					}
				}
			}
			catch(Exception e)
			{

			}

			//////////////////////////////////////发送自己的pub key
			synchronized(iCreditMutex)
			{

				if ((sSelfPubkeyTimeStamp==null)||(sSelfPubkeyTimeStamp.length()==0) || (!sMyPubkeyTimeStamp.equals(sSelfPubkeyTimeStamp) ))
				{
					//////////////////////启动线程来发送微信!!!!!!!!!!!!!!!!!
					if (bNeedSendPublicKey && (System.currentTimeMillis()-lLastSendWeiXinFailed>30000))////////可以发送
					{
						TaskExecutor.executeTask(new Runnable() {
							@Override
							public void run() {
								/////////////Send my pubkey!!!!!!!!!!
								int iOffLine = 1;
								synchronized (XWDataCenter.xwDC.htSendPublicRecord) {
									try {
										try {
											XWDataCenter.xwDC.htSendPublicRecord.remove(sToUser);
										} catch (Exception e) {
											e.printStackTrace();
										}
										XWDataCenter.xwDC.htSendPublicRecord.put(sToUser, new Long(System.currentTimeMillis()));

									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}

								int iret = xwDC.XWWeiXinRequestSendFile((sToUser + "\0").getBytes(),
										(xwDC.sPubkeyFile + "\0").getBytes(), XWDataCenter.XWIntToBytes(iOffLine));

								Log.e("XIM", "XWWeiXinRequestSendFile:" + xwDC.sPubkeyFile+" "+iret);
								if (iret == 0) {
									lLastSendWeiXinFailed=0;
								}
								else  ///////////////如果上次发微信失败,则要等30秒后再发.防止通讯一直处于重连状态。
									lLastSendWeiXinFailed=System.currentTimeMillis();
							}
						});

					}

					/////return true;
					bPubKeyOK=false;
				}

				if (bPubKeyOK && (!sFriendPubkeyTimeStamp.equals(sFriendPubkeyTimeStamp) || (sFriendPubkeyTimeStamp.length()==0)))
				{
					bPubKeyOK=false;
					////bNeedReply=true;
				}
			}


			if (!bPubKeyOK  ////////////////////pubkey not ok!!!!!!!!!!!
					|| (iPos>=sCommand.length())  ///////// no aes key
					)
			{
				synchronized(iCreditMutex)
				{
					UriConfig.delete(sAESKeyFile);
				}
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				////////////////
				{
					XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(55);
				}


				Log.e("XIM","HandleCreditMessage Pubkey not ok!");

				return true;
			}

			Log.e("XIM","HandleCreditMessage Pubkey ok!");

			/////////////////////
			String sAESKey=sCommand.substring(iPos);

			/////Log.v("XIM","HandleCreditMessage sAESKey:"+sAESKey);

			byte[] btAES=null;
			synchronized(iCreditMutex)
			{
				btAES=RSAUtil.decryptByPrivateKey((Base64Util.decode(sAESKey.getBytes("ISO-8859-1"))),XWDataCenter.xwDC.sPrivatekeyFile);
			}
			if (btAES==null)
			{
				Log.v("XIM","HandleCreditMessage   btAES==null");
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return true;
			}

			sCommand=XWDataCenter.getANSIStringFromBytes(btAES);

			//////Log.v("XIM","HandleCreditMessage sCommand"+sCommand);

			iPos=0;
			iPos2=sCommand.indexOf("AESKey:\r", iPos);
			if (iPos2<0)
			{
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return true;
			}
			iPos=iPos2+"AESKey:\r".length();

			///////Log.v("XIM","HandleCreditMessage 2 sCommand"+sCommand);

			///////AES
			//////////////////////////
			iPos2=sCommand.indexOf("\r", iPos);
			if (iPos2<0) {
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return true;
			}
			String sFriendAESTimeStamp=sCommand.substring(iPos, iPos2);
			iPos=iPos2+1;

			Log.v("XIM","HandleCreditMessage sFriendAESTimeStamp:"+sFriendAESTimeStamp);

			if (sFriendAESTimeStamp.length()==0) {
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return true;
			}

			//////////////////////////
			iPos2=sCommand.indexOf("\r", iPos);
			if (iPos2<0) {
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return true;
			}
			String sFriendAESKey=sCommand.substring(iPos, iPos2);
			iPos=iPos2+1;

			//////Log.v("XIM","HandleCreditMessage sFriendAESKey:"+sFriendAESKey);

			if (sFriendAESKey.length()==0) {
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return true;
			}

			//////////////////////////
			iPos2=sCommand.indexOf("\r", iPos);
			if (iPos2<0) {
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
				return true;
			}
			String sMyAESTimeStamp=sCommand.substring(iPos, iPos2);
			iPos=iPos2+1;

			Log.v("XIM","HandleCreditMessage sMyAESTimeStamp:"+sMyAESTimeStamp);
			//////////////////////////
			/*iPos2=sCommand.indexOf("\r", iPos);
			if (iPos2<0)
				return true;
			String sMyAESKey=sCommand.substring(iPos, iPos2);
			iPos=iPos2+1;*/


			///////////////取本地存储
			String sUser1PassTimeStamp;
			String sUser1Pass;
			String sUser2PassTimeStamp;
			String sUser2Pass;

			///String sAESKeyFile=xwDC.sRASKeyPath+"/aeskey_"+xwDC.loginName+"_"+sToUser+".key";


			////////////////////对比!!!!!!!!!!!!!!
			boolean bNeedUpdate=false;

			synchronized (iCreditMutex)
			{
				if (!FileUtil.isFileExist(sAESKeyFile))
				{
					sUser1PassTimeStamp="";
					sUser1Pass="";
					sUser2PassTimeStamp="";
					sUser2Pass="";
				}
				else
				{
					sUser1PassTimeStamp=RSAUtil.readFileLine(sAESKeyFile, 1);
					if (sUser1PassTimeStamp==null)
						sUser1PassTimeStamp="";
					sUser1Pass=RSAUtil.readFileLine(sAESKeyFile, 2);
					if (sUser1Pass==null)
						sUser1Pass="";


					byte[] btSDCardDecode=RSAUtil.decryptByPrivateKey(sUser1Pass.getBytes("ISO-8859-1"),XWDataCenter.xwDC.sPrivatekeyFile);///com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data(sUser1Pass.getBytes("ISO-8859-1"));
					sUser1Pass=new String (btSDCardDecode,"ISO-8859-1");

					sUser2PassTimeStamp=RSAUtil.readFileLine(sAESKeyFile, 3);
					if (sUser2PassTimeStamp==null)
						sUser2PassTimeStamp="";
					sUser2Pass=RSAUtil.readFileLine(sAESKeyFile, 4);
					if (sUser2Pass==null)
						sUser2Pass="";

					btSDCardDecode=RSAUtil.decryptByPrivateKey(sUser2Pass.getBytes("ISO-8859-1"),XWDataCenter.xwDC.sPrivatekeyFile);///com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data(sUser2Pass.getBytes("ISO-8859-1"));
					sUser2Pass=new String (btSDCardDecode,"ISO-8859-1");
				}

				if ((sUser1PassTimeStamp==null)||(sUser1PassTimeStamp.length()==0)
						|| (sUser1Pass==null)||(sUser1Pass.length()==0)
						)
				{  ///////////////////////////
					sUser1PassTimeStamp=RSAUtil.getTimeStamp();
					sUser1Pass=RSAUtil.generateAESKey(sUser1Pass);

					//////synchronized (iCreditMutex)
					/*{
					    RSAUtil.SaveAESKeysToFile(sAESKeyFile, sUser1PassTimeStamp, sUser1Pass, sUser2PassTimeStamp, sUser2Pass
							,"0");
					    
						Log.v("XIM","RSAUtil.SaveAESKeysToFile");
					}*/

					bNeedUpdate=true;

				}
			}

			String sOK="0";

			if (!sUser1PassTimeStamp.equals(sMyAESTimeStamp) || (sUser1PassTimeStamp.length()==0))
			{
				bNeedUpdate=true;
			}
			else
			{
				sOK="1";
			}


			if (!sUser2PassTimeStamp.equals(sFriendAESTimeStamp) || (sUser2PassTimeStamp.length()==0)   )
			{
				/////////if (sUser2PassTimeStamp.compareTo(sFriendAESTimeStamp)<0)
				{
					sUser2PassTimeStamp=sFriendAESTimeStamp;
					sUser2Pass=sFriendAESKey;
				}
				bNeedUpdate=true;
			}

			/////////if (bNeedUpdate)
			{
				synchronized (iCreditMutex)
				{
					RSAUtil.SaveAESKeysToFile(sAESKeyFile, sUser1PassTimeStamp, sUser1Pass, sUser2PassTimeStamp, sUser2Pass,sOK
					);
				}
			}
			Log.v("XIM","HandleCredit "+sRequest);

			/////////////如果对方发起，或者不匹配,则回应!!!!
			if (bNeedUpdate || (CREDIT_REQUEST.equals(sRequest)))
			{
				Log.v("XIM","SendCreditMessage");
				SendCreditMessage(sToUser,CREDIT_REQUEST_REPLY);
			}

			////////////////
			{
				XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(55);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return true;
	}


	public static void StartUpdateRSAKeys()
	{
		if ((XWDataCenter.xwDC.loginName==null)||(XWDataCenter.xwDC.loginName.length()==0)||"null".equals(XWDataCenter.xwDC.loginName)) {
			Log.e("XIM","StartUpdateRSAKeys error! XWDataCenter.xwDC.loginName==null");
			return;
		}


		xwDC.sRASKeyPath= UriConfig.getSavePath()+"/RSAKeys";
		FileUtil.openOrCreatDir(xwDC.sRASKeyPath);
		String sPubkeyFile=xwDC.sRASKeyPath+"/pubkey_"+XWDataCenter.xwDC.loginName+".rsa";
		String sPrivatekeyFile=xwDC.sRASKeyPath+"/privatekey_"+XWDataCenter.xwDC.loginName+".rsa";
		XWDataCenter.xwDC.sPubkeyFile=sPubkeyFile;
		XWDataCenter.xwDC.sPrivatekeyFile=sPrivatekeyFile;

		//////////////////2016-07-06,检查tf卡是否正常
		//////测试加密卡
		{

			Log.e("XIM", "com.example.mcryptolmsdimpl_demo.MainActivity.doCheckSDCard!");
			try
			{
				//////com.example.mcryptolmsdimpl_demo.MainActivity.removekeys(XWDataCenter.xwDC.loginName);
				com.example.mcryptolmsdimpl_demo.MainActivity.setKeyID(XWDataCenter.xwDC.loginName);
				com.example.mcryptolmsdimpl_demo.MainActivity.repairTFCard(MainApplication.getInstance());
				com.example.mcryptolmsdimpl_demo.MainActivity.doCheckSDCard(MainApplication.getInstance());
				Log.e("XIM", "com.example.mcryptolmsdimpl_demo.MainActivity.doCheckSDCard! over");
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				Log.e("XIM", "com.example.mcryptolmsdimpl_demo.MainActivity.doCheckSDCard! error:"+ex.getMessage());
			}

			///////////test
			/*String sText="01234567890123456789\r01234567890123456789\r01234567890123456789\r01234567890123456789\r01234567890123456789\r";
			try {
				byte[] btEncoded = com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_data(sText.getBytes("ISO-8859-1"));
				android.util.Log.e("XIM", "tfcard encoded :" + (new String(btEncoded, "ISO-8859-1")).trim());
				byte[] btDecoded = com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data(btEncoded);
				android.util.Log.e("XIM", "tfcard Decoded :" + (new String(btDecoded, "ISO-8859-1")).trim());
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.e("XIM", "com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data error!");
			}*/

			////////////////////测试私钥
			if (AppConfig.SD_ENCODE) {  ////////////tfcard版,必须要能获得私钥
				if (RSAUtil.readPrivateKey(sPrivatekeyFile) == null) {
					///////////////////////
					Log.e("XIM", "RSAUtil.readPrivateKey error!");
					//////////////////更新rsa key要清理所有的aes key
					UriConfig.delete(xwDC.sRASKeyPath);
					FileUtil.openOrCreatDir(xwDC.sRASKeyPath);
				}
			}
		}

		{
			boolean bNeedToUpdate=false;
			////public void run()
			{
				Log.v("XIM","Begin to generate RSA key....");
				{

					/////////////尝试取日期
					String sTimeStampPubkey=RSAUtil.readFileLine(sPubkeyFile, 1);
					String sTimeStampPrivatekey=RSAUtil.readFileLine(sPrivatekeyFile, 1);

					Log.v("XIM","sTimeStampPubkey:"+sTimeStampPubkey+" sTimeStampPrivatekey:"+sTimeStampPrivatekey);

					if  ((sTimeStampPubkey==null)	 || 	(sTimeStampPrivatekey==null)
							|| (!sTimeStampPubkey.equals(sTimeStampPrivatekey))
							)
					{
						bNeedToUpdate=true;
					}
					else
					{
						///////////test
						String sText="01234567890123456789";
						//////synchronized (iCreditMutex)
						{
							try
							{
								byte[] btEncoded=RSAUtil.encryptByPubKey(sText.getBytes("ISO-8859-1"),sPubkeyFile);
								android.util.Log.e("XIM","encoded :"+(new String(btEncoded,"ISO-8859-1")).trim());
								byte[] btDecoded=RSAUtil.decryptByPrivateKey(btEncoded,sPrivatekeyFile);
								android.util.Log.e("XIM","Decoded :"+(new String(btDecoded,"ISO-8859-1")).trim());
								if ( (new String(btEncoded,"ISO-8859-1")).equals((new String(btDecoded,"ISO-8859-1")))
										|| (!sText.equals(new String(btDecoded,"ISO-8859-1"))))
								{
									Log.e("XIM","RSA key check error!");
									bNeedToUpdate=true;
								}

							}
							catch(Exception ex)
							{
								bNeedToUpdate=true;
								ex.printStackTrace();
							}
						}
					}
				}
		        	/*else   ///////////////RSAkey永不失效。2016-09-28
		        	{
		        		Date dtCreditTime=RSAUtil.ParseTimeStamp(sTimeStampPubkey);
						if (new Date().getTime()-dtCreditTime.getTime()>(long)86400*1000*RSA_KEY_UPDATE_DAYS) /////30 days
						{
							bNeedToUpdate=true;
						}
		        	}*/

				if(bNeedToUpdate)
				{
					//////////////////更新rsa key要清理所有的aes key
					UriConfig.delete(xwDC.sRASKeyPath);
					FileUtil.openOrCreatDir(xwDC.sRASKeyPath);

					////synchronized (iCreditMutex)
					{
						if (RSAUtil.generateKey(sPubkeyFile, sPrivatekeyFile,iCreditMutex)) /////成功生成key!!!!!
						{

						}
						else
						{
							android.util.Log.e("XIM","Can not create RSA key!");
							return;
						}
					}
				}


				Log.v("XIM","Check RSA key OK!");
			}
		}//////.start();

	}


	//////////////使用线程来挂断
	public static void threadHangupNetPhone()
	{
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				try
				{
					XWDataCenter.xwDC.hangupNetPhone();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});


	}

	//////停止视频通讯，当前视频界面退出后台时
	 void stopVideo(){
		Log.v("xim", "stopVideo.................................");
		try
		{
			AudioManager am = (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
			{
				{
					am.setSpeakerphoneOn(false);
				}
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}



		xwDC.cameraRunning=false;
		xwDC.isAudioRunning=false;
		 xwDC.bIsSpeakerOn=false;

		XWDataCenter.video_is_open=false;
		XWDataCenter.audio_is_open=false;
		xwDC.needOpenVideo=true;

//		xw_has_prepared=false;


		if(xwDC.mCamera!=null){
			try
			{
				xwDC.mCamera.setPreviewCallback(null);
				xwDC.mCamera.stopPreview();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}


		try
		{
			xwDC.remoteVideoRunning=false;

			xwDC.calling_loginName="";//设为空闲
			xwDC.netPhoneTime=0;
			xwDC.timeSB.delete(0, xwDC.timeSB.length());
			xwDC.accountSB.delete(0, xwDC.accountSB.length());


			xwDC.netPhoneTime=0;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				try{
					if(XWDataCenter.EditionType>0){
						xwDC.stopXWAudio();
					}
					Log.v("tag", "stopVideo2.................................");
					if(xwDC.remoteVideoThread!=null){
						try{
							xwDC.remoteVideoThread.join();
						}catch(InterruptedException e){
							e.printStackTrace();
						}
						xwDC.remoteVideoThread=null;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				xwDC.hangupNetPhone();
			}
		});

		NotificationUtil.cleanNotificationByID(JRSConstants.NOTICE_VIDEO_DISPLAY);


//		FriendVideoDisplay.this.finish();
	}
	/**
	 * 清理数据
	 */
	public void clearData(){
		try {
			if (xwDC != null) {
				//重置未完成发送信息
				xwDC.setResendMsg();
				//删除所有解码文件
				XWDataCenter.clearAllDecrypt();
				////清理视频临时文件
				UriConfig.deleteInDir(UriConfig.getVideoSavePath());
				//关闭数据库
				XWDataCenter.closeDB();
				/////////////////2014-06-16,指示是否通讯层初始化!!!!!!!!!!!!!!!!!
				xwDC.isLogin = false;
				////////xwDC.logoutService(0);
				Log.e("xim","logoutService ok");
				////////////////2014-09-17,清理加密的文件
				{
					String aespath = UriConfig.getSavePath() + "/aesfiles";
					UriConfig.delete(aespath);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	public void destroyUserAccount(final BaseUI baseContext,final boolean showDg)
	{
		try
		{

			try
			{
				SharedPreferences settings = MainApplication.getInstance().getSharedPreferences(XWDataCenter.PackageName, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.clear();
				editor.apply();

			}
			catch(Exception ex1)
			{
				ex1.printStackTrace();
			}
			MainApplication.getInstance().clearServiceData();

			new AsyncTask<String,String,String>(){
				@Override
				protected String doInBackground(String... params) {
					com.example.mcryptolmsdimpl_demo.MainActivity.removekeys(loginName);
					UriConfig.deleteAll();
					clearData();
					return null;
				}

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					if(baseContext!=null&&showDg)
						baseContext.showPlg("");
				}

				@Override
				protected void onPostExecute(String s) {
					super.onPostExecute(s);
					if(baseContext!=null&&showDg)
						baseContext.disPlg();
					MainApplication.getInstance().onTerminate();
				}
			}.execute("");


		}catch(Exception ex)
		{
			ex.printStackTrace();
		}


	}

}
