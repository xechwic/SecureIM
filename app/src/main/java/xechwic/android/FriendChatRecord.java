package xechwic.android;

import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import ydx.securephone.BuildConfig;
import ydx.securephone.R;
import ydx.securephone.R.color;
import cn.hadcn.keyboard.ChatKeyboardLayout;
import cn.hadcn.keyboard.RecordingLayout;
import cn.hadcn.keyboard.emoticon.util.EmoticonHandler;
import cn.hadcn.keyboard.media.MediaBean;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.ui.ImagePreviewActivity;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import mabeijianxi.camera.MediaRecorderActivity;
import mabeijianxi.camera.VCamera;
import mabeijianxi.camera.model.AutoVBRMode;
import mabeijianxi.camera.model.BaseMediaBitrateConfig;
import mabeijianxi.camera.model.MediaRecorderConfig;
import mabeijianxi.camera.util.FileUtils;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import xechwic.android.act.MainApplication;
import xechwic.android.adapter.ChatMsgListAdapter;
import xechwic.android.bean.ChatHistoryBean;
import xechwic.android.bean.ChatMsgEntity;
import xechwic.android.bean.ChatRefreshBean;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.AvatarUpdateEvent;
import xechwic.android.bus.event.ChatMsgEvent;
import xechwic.android.bus.event.UpdateAESEvent;
import xechwic.android.crop.CropUtil;
import xechwic.android.ui.BaseUI;
import xechwic.android.ui.PhotoViewUI;
import xechwic.android.ui.SendSmallVideoActivity;
import xechwic.android.ui.VideoPlayerActivity;
import xechwic.android.util.ContentProviderUtil;
import xechwic.android.util.FileUtil;
import xechwic.android.util.GsonUtil;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.MessageUtil;
import xechwic.android.util.NotificationUtil;
import xechwic.android.util.PrefsUtils;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.UriConfig;
import xechwic.android.util.Util;
import xechwic.android.util.glide.GlideImageLoader;
import xechwic.android.view.ToastUtil;

import static xechwic.android.XWDataCenter.decodeFile;
import static xechwic.android.util.JRSConstants.HEX_PRE;

/**
 * 聊天界面
 *
 */
public class FriendChatRecord extends BaseUI implements
		MediaBean.MediaListener, ChatKeyboardLayout.OnChatKeyBoardListener {

	private static final String TAG=FriendChatRecord.class.getSimpleName();


	public XWDataCenter xwDC;
	public Handler mHandler;// 消息处理
	private String friendAccount;// 好友账号
	public FriendNodeInfo fni;//好友结点

	private ImageView aeslock;

	// 聊天记录显示改用TextView
	private ListView msgListView;    //聊天记录 listView
	private ChatMsgListAdapter mAdapter;// 消息视图的Adapter
	private List<ChatMsgEntity> mListData = new ArrayList<>();// 消息对象数组
	private  List<ChatMsgEntity> snapchatList=new ArrayList<>();//阅后即焚的消息
	ChatKeyboardLayout keyboardLayout = null;
	RecordingLayout rlRecordArea;


	private String friendName;//用户名


	//发送消息状态
	public static final int WAITING_STATUS=0x1001;//等待接受
	public static final int TRANSFER_STATUS=0x1002;//正在传输
	public static final int TRANSFER_STOP=0x1003;//停止
	public static final int TRANSFER_ERROR=0x1004;//出错
	public static final int TRANSFER_END=0x1005;//发送结束
	public static final int TRANSFER_NONE=0x1006;//不存在
	public static final int MSG_UPDATE=0x1007;//更新消息

	public static final int MSG_TOAST=0x1008;//提示消息

	//消息类型
	public static final int MSG_TEXT=0;//文本
	public static final int MSG_EMU=1;  //表情
	public static final int MSG_FILE=2;  //文件
	public static final int MSG_VOICE=3;//语音
	public static final int MSG_PHOTO=4;//图片
	public static final int MSG_VIDEO=5;//视频


	private TextView title;//标题栏
	public String friendImage;//好友头像
	public String myImage;//我的头像

	private boolean bIsWeiXin=false;
	private static final int PAGE_SIZE=6;
	private boolean bIsLoading=false;
	private boolean bIsInit=false;
	private ImagePicker imagePicker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewLayoutID());
		initViewsAndEvents();
		initSmallVideo(MainApplication.getInstance());
	}
	public static void initSmallVideo(Context context) {
		//视频保存目录
		VCamera.setVideoCachePath(UriConfig.getVideoSavePath());
		// 开启log输出,ffmpeg输出到logcat
		VCamera.setDebugMode(BuildConfig.DEBUG);
		// 初始化拍摄SDK，必须
		VCamera.initialize(context);
	}

	public void startRecVideo() {
		String width = "480";
		String height = "360";
		String maxFramerate = "20";
		String maxTime = "11000";
		String minTime = "1500";

		BaseMediaBitrateConfig recordMode;
		BaseMediaBitrateConfig compressMode = null;

		recordMode = new AutoVBRMode();
		compressMode = new AutoVBRMode();

		MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
				.doH264Compress(compressMode)
				.setMediaBitrateConfig(recordMode)
				.smallVideoWidth(Integer.valueOf(width))
				.smallVideoHeight(Integer.valueOf(height))
				.recordTimeMax(Integer.valueOf(maxTime))
				.maxFrameRate(Integer.valueOf(maxFramerate))
				.captureThumbnailsTime(1)
				.recordTimeMin(Integer.valueOf(minTime))
				.build();
		MediaRecorderActivity.goSmallVideoRecorder(this, SendSmallVideoActivity.class.getName(), config);
		//    不知道传入什么？用下面的参数就可以了
		//
        /*
        MediaRecorderConfig config = new MediaRecorderConfig.Buidler()
                .doH264Compress(new AutoVBRMode()
//                        .setVelocity(BaseMediaBitrateConfig.Velocity.ULTRAFAST)
                )
                .setMediaBitrateConfig(new AutoVBRMode()
//                        .setVelocity(BaseMediaBitrateConfig.Velocity.ULTRAFAST)
                )
                .smallVideoWidth(480)
                .smallVideoHeight(360)
                .recordTimeMax(6 * 1000)
                .maxFrameRate(20)
                .captureThumbnailsTime(1)
                .recordTimeMin((int) (1.5 * 1000))
                .build();
        MediaRecorderActivity.goSmallVideoRecorder(this, SendSmallVideoActivity.class.getName(), config);
        */

	}

	protected int getContentViewLayoutID() {
		return R.layout.friend_chat_record;
	}

	protected void initViewsAndEvents() {
		initActionBar();
		//获取xwdc并将xwdc的context指向本例
		this.xwDC = ((MainApplication) this.getApplication()).getDC(this);
		// 初始化传输文件数据
		if (MainApplication.getInstance().mFile == null) {
			MainApplication.getInstance().mFile = new HashMap<Long, ChatMsgEntity>();
		}
		//初始化控件
		initView();
		//初始化列表数据
		initData();
		loadAPageDataTask(true);
		BusProvider.getInstance().register(this);
	}

	private void initActionBar(){
//		ActionBar actionBar = getSupportActionBar();
//		View barView = LayoutInflater.from(this).inflate(R.layout.chat_titlebar, null);
//		actionBar.setCustomView(barView);
//		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//		actionBar.setDisplayShowCustomEnabled(true);
//		actionBar.setBackgroundDrawable(getResources().getDrawable(color.header_bg));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.e("chat","onNewIntent");
		if(intent!=null){
			String videoPath=intent.getStringExtra(JRSConstants.VIDEO_URI_KEY);
			if(!TextUtils.isEmpty(videoPath)){
				sendFile(videoPath,"video");
			}

		}
	}




	/**
	 * 更新头像名
	 */
	public void updateHeads(){
		if(friendAccount==null /*||XWDataCenter.fni==null||XWDataCenter.fni.getLogin_name()==null*/){
			return;
		}
		if(XWDataCenter.headBeanMap==null||XWDataCenter.headBeanMap.isEmpty()){
			return;
		}
		if(XWDataCenter.headBeanMap.containsKey(friendAccount)){
			friendImage=XWDataCenter.headBeanMap.get(friendAccount);
		}
		if(XWDataCenter.headBeanMap.containsKey(XWDataCenter.xwDC.loginName)){
			myImage=XWDataCenter.headBeanMap.get(XWDataCenter.xwDC.loginName);
		}

	}


	/**
	 * 开始对讲机
	 */
	public void startWeiXinAudio(){
		// 按住事件发生后执行代码的区域
		if (fni != null)
		{
			XWDataCenter.xwDC.XWStartWeiXinAudio(fni
					.getLogin_name());

			bIsWeiXin=true;

		}
	}

	/**
	 * 停止对讲机
	 */
	public void stopWeiXinAudio(boolean send){
		// 松开事件发生后执行代码的区域,取得userdata下的录音文件
		String sFilePath = XWDataCenter.xwDC.XWStopWeiXinAudio();
		bIsWeiXin=false;

        if(TextUtils.isEmpty(sFilePath)){
			showToastTips(XWCodeTrans.doTrans("录音保存失败,请打开录音权限和保持足够存储空间"));
			return;
		}
		/**
		 * 每次发送、接受文件，都将data/data下的文件，复制到sd卡里面，节约空间
		 * 路径如下：sdcard/voice/当前账号的用户ID/data下路径的哈希码.xwx
		 */
		if(send){
			try
			{
				// 发送语音文件
				sendFile(sFilePath, "voice");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}


	}



	/**
	 * 加载消息历史，从数据库中读出
	 */
	public void initData() {

		try
		{
			Intent intent = getIntent();
			Bundle bundle = intent.getExtras();

			//恢复fni
			if (bundle != null){
				if(fni==null){
					String str=bundle.getString("friendAccount");
					if(str!=null&&str.length()>0){
						friendAccount = bundle.getString("friendAccount");
						fni = XWDataCenter.xwDC.getANodeInfo(friendAccount);
					}
				}

				if(fni==null){
					if(bundle.containsKey("fni")){
						fni=(FriendNodeInfo) bundle.getSerializable("fni");
					}
				}
			}

			if ((friendAccount == null) || (fni==null)) {
				this.finish();
				if(xwDC.activityList.size()==0){
					startActivity(new Intent(baseAct, FriendControl.class));
				}
				return;
			}


			updateHeads();

			//设置ID
			mAdapter.setNodeAccount(friendAccount);
			//设置xwcenter
			mAdapter.setXWCenter(this.xwDC);

			msgListView.setAdapter(mAdapter);
			msgListView.setSelection(mAdapter.getCount() - 1);




			//设置聊天对象的名
			// ------------------------------------------------------------------------
			////if (fni != null)
			{
				friendName=fni.getSignName();
				String loginName=fni.getLogin_name();
				if(loginName!=null){
					friendAccount=loginName;//用户ID
				}
				if(friendName==null||friendName.trim().length()<1){
					friendName=loginName;
				}
				Log.e(TAG,"friendName:"+friendName);

				////////////////设置标题
				setTitle(fni.getOnline_status()==null?XWCodeTrans.doTrans("断开"):fni.getOnline_status());
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 加载一页记录
	 */
	private void loadAPageDataTask(final boolean showLoading){
		if(fni==null){
			return;
		}
		if(bIsLoading){
			return;
		}
		new AsyncTask<String, Integer, List<ChatMsgEntity>>() {

			@Override
			protected List<ChatMsgEntity> doInBackground(String... arg0) {
				List<ChatMsgEntity> list=null;
				try{
					list=getAPageData();
					if(!bIsInit){///清理未读
						bIsInit=true;
						//清理未读信息标识
						clearUnreadFlag(fni.getLogin_name());
					}
				}catch (Exception e){
					e.printStackTrace();
				}

				return list;
			}

			@Override
			protected void onPostExecute(java.util.List<ChatMsgEntity> result) {
				bIsLoading=false;
				if(!bIsDestroy){
					if(result!=null&&result.size()>0){
						if(mListData.size()>0){
							mListData.addAll(0,result);
							mAdapter.notifyDataSetChanged();
						}else{
							mListData.addAll(result);
							refreshData();
						}
					}
				}
			}

			@Override
			protected void onPreExecute() {
				bIsLoading=true;
			}

		}.execute("");
	}


	private List<ChatMsgEntity> getAPageData(){
		List<ChatMsgEntity> list =new ArrayList<ChatMsgEntity>();
		try{
			List<String> strlist= XWDataCenter.getMessageDB().getMsg(XWDataCenter.getCurAccount(),friendAccount);
			if(strlist!=null){
				long st=System.currentTimeMillis();
				int totalSize=strlist.size();
				if(totalSize>mListData.size()){
					int	listIndex=totalSize-mListData.size();
					List<String> temList=new ArrayList<String>();
					int count=0;
					for(int i=listIndex-1;i>=0;i--){
//						if(count>=PAGE_SIZE){
//							break;
//						}
						temList.add(strlist.get(i));
						count++;
					}
					Collections.reverse(temList);
					for(String str:temList){
						try {
							if(!TextUtils.isEmpty(str)){
//								Log.e("chat","message encry:"+str);
								String strText = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_aes(str.getBytes()));
//								Log.e("chat","message dencry:"+strText);
								if(!TextUtils.isEmpty(strText)){
									if (strText != null) {
										ChatMsgEntity entity = GsonUtil.GsonToBean(strText, ChatMsgEntity.class);
										list.add(entity);
									}
								}
							}
						}catch (Exception e){
							e.printStackTrace();
						}
					}
				}
				Log.e(TAG,"decode take time:"+(System.currentTimeMillis()-st)*0.001);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}


	/**
	 * 刷新数据
	 */
	public void refreshData(){
		if(mAdapter!=null){
			mAdapter.notifyDataSetChanged();
			msgListView.setSelection(mAdapter.getCount() - 1);
		}

	}

	public void refreshData(ChatRefreshBean bean){
		if(bean!=null&&bean.entity!=null){
			String entityAccount=bean.entity.getFriendAccount();
			if(entityAccount==null||friendAccount==null){
				return;
			}
			if(!entityAccount.equals(friendAccount)){
				return;
			}
			if(bean.type==1){
				//消息置为已读,界面当前显示才处理
				if(bIsFront) {
					XWDataCenter.getChatHistoryDB().resetUnread(XWDataCenter.getCurAccount(), friendAccount);
					XWDataCenter.getMessageDB().resetUnread(XWDataCenter.getCurAccount(), friendAccount);
				}
				mListData.add(bean.entity);
				mAdapter.notifyDataSetChanged();
				msgListView.setSelection(mAdapter.getCount() - 1);
//				//加入阅后即焚列表,要求聊天界面当前显示
//				if(bIsFront&&XWDataCenter.getReadSwitch()){
//					if(bean.entity.getSendFlag()!=11){
//						bean.entity.setSnaptime(System.currentTimeMillis());
//						FriendChatRecord.snapchatList.add(bean.entity);
//					}
//
//				}else{
//					tempMsgList.add(bean.entity);
//				}
			}else if(bean.type==2){
				for(ChatMsgEntity en:mListData){
					if(en.getNo()==bean.entity.getNo()){
						en.setProgress(bean.entity.getProgress());
						break;
					}
				}
				mAdapter.notifyDataSetChanged();
				msgListView.setSelection(mAdapter.getCount() - 1);
			}else if(bean.type==-1){
				ChatMsgEntity ent=null;
				for(ChatMsgEntity en:mListData){
					if(en.getNo()==bean.entity.getNo()){
						ent=en;
						break;
					}
				}
				if(ent!=null){
					mListData.remove(ent);
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	/**
	 * 初始化控件
	 */
	private void initView(){
		this.mHandler = new FriendChatRecordHandle(this);
	    findViewById(R.id.friend_chat_record_back).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				baseAct.finish();
			}
		});
		title=(TextView)findViewById(R.id.chat_title);
		aeslock=(ImageView)findViewById(R.id.friend_chat_record_AESlock);
		//聊天记录ListView
		msgListView = (ListView)findViewById(R.id.lv_friends);

		EmoticonHandler.getInstance(MainApplication.getInstance());
		keyboardLayout = (ChatKeyboardLayout)findViewById(R.id.kv_bar);
		keyboardLayout.showEmoticons();
		ImageView iv_call=(ImageView)keyboardLayout.findViewById(R.id.btn_call);
		iv_call.setVisibility(View.VISIBLE);
		iv_call.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				keyboardLayout.hideBottomPop();
				if (fni!=null)
				{
					boolean bOK=makeCallCheck(fni);
					if(bOK) {
						Intent intent = new Intent(FriendChatRecord.this,
								FriendCall.class);
						intent.putExtra("phone_number", fni.getLogin_name());
						intent.putExtra("tag", "1"); ////////2014-07-17,默认使用视频!!!!!!!!!!!
						startActivity(intent);
					}
				}
			}
		});
		ArrayList<MediaBean> popupModels = new ArrayList<>();
		popupModels.add(new MediaBean(1, R.drawable.em_chat_pic_selector, XWCodeTrans.doTrans("图片"), this));
		popupModels.add(new MediaBean(2, R.drawable.em_chat_file_selector, XWCodeTrans.doTrans("文件"), this));
		popupModels.add(new MediaBean(3, R.drawable.em_chat_video_selector, XWCodeTrans.doTrans("视频"), this));
		keyboardLayout.showMedias(popupModels);
		keyboardLayout.setOnKeyBoardBarListener(this);

		msgListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				keyboardLayout.hideBottomPop();
				return false;
			}
		});


		rlRecordArea = (RecordingLayout)findViewById(R.id.recording_area);
		initInputStatus();


		mAdapter = new ChatMsgListAdapter(this, mListData);

	}

	////设置初始输入状态
	private void initInputStatus(){
		long status=PrefsUtils.getInstance().get(JRSConstants.KEY_INPUT_STATUS,0);
		if(status==1){
			keyboardLayout.switchRecodingBar();
		}
	}

	/**设置标题内容
	 */
	public void setTitle(String status){
		if(title!=null&&status!=null){
			if (XWCodeTrans.doTrans("断开").equals(status)){
				title.setTextColor(getResources().getColor(color.gray));
				title.setText(friendName+"("+status+")");
			}else{
				title.setTextColor(getResources().getColor(color.white));
				title.setText(friendName+"("+status+")");
			}

		}

	}

	/**更新标题
	 */
	public void updateTitle(FriendNodeInfo node){
		if(node==null||title==null){
			return;
		}
		if(node.getLogin_name().equals(friendAccount)){
			setTitle(fni.getOnline_status()==null?XWCodeTrans.doTrans("断开"):fni.getOnline_status());
		}
	}








	/**保存消息
	 */
	public void saveMsg(ChatMsgEntity entity){
		if(XWDataCenter.getMessageDB()!=null){
			if(XWDataCenter.getMessageDB().isExist(XWDataCenter.xwDC.loginName,entity.getFriendAccount(), entity)){
				XWDataCenter.getMessageDB().updateMsg(XWDataCenter.xwDC.loginName,entity.getFriendAccount(), entity);
			}else{
				XWDataCenter.getMessageDB().saveMsg(XWDataCenter.xwDC.loginName,entity.getFriendAccount(), entity);
			}

		}

		if(XWDataCenter.getChatHistoryDB()!=null){
			if(fni!=null){
				if(fni.getLogin_name().equals(entity.getFriendAccount())){
					ChatHistoryBean bean=new ChatHistoryBean();
					bean.setLogin_name(fni.getLogin_name());
					bean.setSignName(fni.getSignName());//始终要显示对方的名称
//					bean.setIcon(fni.getIcon());
					bean.setRecentChat(entity.getMessage());
//					bean.setIntroduction(entity.getFilePath());//消息列表不用显示图片
					bean.setLastTime(""+System.currentTimeMillis());
					bean.setUnread(0);
					if(XWDataCenter.getChatHistoryDB().isExistFriend(fni.getLogin_name(),XWDataCenter.getCurAccount())){
						XWDataCenter.getChatHistoryDB().updateFriendNode(bean,XWDataCenter.getCurAccount());
					}else{
						XWDataCenter.getChatHistoryDB().saveMsg(bean,XWDataCenter.getCurAccount());
					}
				}

			}
		}
	}

	@Override
	public void finish() {
		if(XWDataCenter.getReadSwitch()){
			new AsyncTask<String,String,String>(){
				@Override
				protected String doInBackground(String... params) {
					for(ChatMsgEntity entity:mListData){
						if(entity!=null&&entity.getSendFlag()==10){
							snapchatList.add(entity);
						}
					}
					clearReadMsg(snapchatList);
					snapchatList.clear();
					////清理视频临时文件
					UriConfig.deleteInDir(UriConfig.getVideoSavePath());
					return null;
				}

				@Override
				protected void onPostExecute(String s) {
					super.onPostExecute(s);
					disPlg();
					FriendChatRecord.super.finish();
				}

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					showPlg("");
				}
			}.execute("");
		}else{
			super.finish();
		}

	}



	/**清理未读信息
	 */
	private void clearUnreadFlag(String friendAccount){
//		if(XWDataCenter.getReadSwitch()){
//			List<ChatMsgEntity> list= XWDataCenter.getMessageDB().getUnreadEntitys(XWDataCenter.getCurAccount(),friendAccount);
//			if(list!=null&&list.size()>0){
//				snapchatList.addAll(list);
//			}
//			List<ChatMsgEntity> listsnap= XWDataCenter.getMessageDB().getSnapFileEntitys(XWDataCenter.getCurAccount(),friendAccount);
//			if(listsnap!=null&&listsnap.size()>0){
//				snapchatList.addAll(listsnap);
//			}
//		}

		if(XWDataCenter.getChatHistoryDB().isExistFriend(friendAccount,XWDataCenter.getCurAccount())){
			XWDataCenter.getChatHistoryDB().resetUnread(XWDataCenter.getCurAccount(),friendAccount);
		}
		XWDataCenter.getMessageDB().resetUnread(XWDataCenter.getCurAccount(),friendAccount);
		XWDataCenter.getMessageDB().resetSnap(XWDataCenter.getCurAccount(),friendAccount);
	}


	public void resendText(ChatMsgEntity entity){
		if(entity==null){
			return;
		}
		String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date());
		entity.setDate(ctime);
		String conStr = entity.getMessage();

		try {


			/////////////////////2014-09-05,加密数据
			String sAESPassword=XWDataCenter.getFriendAESPassword(XWDataCenter.getCurAccount(), fni.getLogin_name());
			if (sAESPassword!=null)
			{
				byte[] btTemp=conStr.getBytes("GBK");
				byte[] btDestTemp=new byte[(btTemp.length+16)*2+1];
				int iLen=xwDC.XWNetphoneAESEncodeText(btTemp,btDestTemp,(sAESPassword+"\0").getBytes("GBK"));

				if (iLen>0)
				{
					byte[] btDestTemp2=new byte[iLen];
					System.arraycopy(btDestTemp, 0, btDestTemp2, 0, iLen);

					try
					{
						conStr=new String(btDestTemp2,"GBK");
					}
					catch(Exception ex1)
					{
						ex1.printStackTrace();
					}
				}
			}

			///////////////////////////////
			////////////说明:第四参数null,可传入 byte[33]字节。然后调用queryMessageStatus来跟踪它。
			byte[] traceNo=new byte[33];
			if (xwDC.sendMessage(xwDC.cid, fni.getId(), (conStr
							.replace("\n", "\r\n")+"\0").getBytes("GBK"),
					(ctime+"\0").getBytes("GBK"),traceNo)==0)
			{
				saveMsg(entity);
				////阅后即焚
//				if(XWDataCenter.getReadSwitch()){
//					entity.setSnaptime(System.currentTimeMillis());
//					snapchatList.add(entity);
//				}
			}
			else
			{
				ToastUtil.getInstance(baseAct).show(XWCodeTrans.doTrans("发送消息失败!"));
				if(conStr.length()>JRSConstants.MAX_MSG_LENGTH){
					ToastUtil.getInstance(baseAct).show(getResources().getString(R.string.msg_too_long));
				}
			}

			//刷新列表
			refreshData();
			//追踪状态
			queryStatusTask(traceNo,entity);
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	/**
	 * 发送文本消息
	 */
	public void sendText(ChatMsgEntity entity){
		if(entity==null){
			return;
		}
		String ctime = entity.getDate();
		String conStr = entity.getMessage();
		conStr += "\0";
		ctime += "\0";
		try {
			/////////////////////2014-09-05,加密数据
			String sAESPassword=XWDataCenter.getFriendAESPassword(xwDC.loginName, fni.getLogin_name());
			if (sAESPassword!=null)
			{
				byte[] btTemp=conStr.getBytes("GBK");
				byte[] btDestTemp=new byte[(btTemp.length+16)*2+1];
				int iLen=xwDC.XWNetphoneAESEncodeText(btTemp,btDestTemp,(sAESPassword+"\0").getBytes());

				if (iLen>0)
				{
					byte[] btDestTemp2=new byte[iLen];
					System.arraycopy(btDestTemp, 0, btDestTemp2, 0, iLen);

					try
					{
						conStr=new String(btDestTemp2,"GBK");
						Log.e("main","msg:"+conStr.length());
					}
					catch(Exception ex1)
					{
						ex1.printStackTrace();
					}
				}
			}



			///////////////////////////////
			///////////////////////////////
			////////////说明:第四参数null,可传入 byte[33]字节。然后调用queryMessageStatus来跟踪它。
			byte[] traceNo=new byte[33];
			int ret=xwDC.sendMessage(xwDC.cid, fni.getId(), (conStr
							.replace("\n", "\r\n")+"\0").getBytes("GBK"),
					(ctime+"\0").getBytes("GBK"),traceNo);
			if (ret==0)
			{
				saveMsg(entity);
				//刷新列表
				refreshData(new ChatRefreshBean(entity, 1));
				//追踪状态
				queryStatusTask(traceNo,entity);
			}else if(ret==-2){///文本过长
				ToastUtil.getInstance(baseAct).show(getResources().getString(R.string.msg_too_long));
				String savePath= MessageUtil.msg2File(entity.getMessage());
				sendFile(savePath,"file");
			}
			else
			{
				ToastUtil.getInstance(baseAct).show(XWCodeTrans.doTrans("发送消息失败!"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	//handler 处理消息发送状态通知
	Handler myHandler =new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(bIsDestroy){
				return;
			}
			Log.e("handler","handler get msg:"+msg.what);
			switch (msg.what) {
				case MSG_TOAST:
					if(msg.obj!=null){
						showToastTips((String)msg.obj);
					}
					break;
				case MSG_UPDATE:
					if(msg.obj!=null){
						if(msg.obj instanceof ChatMsgEntity)
							refreshData(new ChatRefreshBean((ChatMsgEntity)msg.obj,2));
					}
					break;
				case TRANSFER_END:
					if(msg.obj!=null){
						if(msg.obj instanceof ChatMsgEntity){
							ChatMsgEntity entity=(ChatMsgEntity)msg.obj;
							refreshData(new ChatRefreshBean(entity,2));

							////发送成功或失败都要进行加密并删除临时文件
							int type=entity.getMsgType();
							if(type==MSG_FILE||type==MSG_PHOTO||type==MSG_VOICE||type==MSG_VIDEO){
								//////16进制的文件名改回来HEX_PRE+filename;
								File file=new File(entity.getFilePath());
								if(file.exists()&&file.getName().startsWith(JRSConstants.HEX_PRE)){
									String srcHexName=file.getName().substring(JRSConstants.HEX_PRE.length());
									String srcName=FileUtil.toSrcName(srcHexName);
									FileUtil.renameFile(file.getParent(),file.getName(),srcName);
									if(FileUtil.isFileExist(file.getParent()+"/"+srcName)){
										/////更新文件名
										entity.setFilePath(file.getParent()+"/"+srcName);
										saveMsg(entity);
									}
								}
								//对发送文件加密
								XWDataCenter.encodeFileTask(entity.getFilePath());
								//删除发送时产生的AES文件
								if(!TextUtils.isEmpty(entity.sAESFilePath)){
									UriConfig.delete(entity.sAESFilePath);
								}
							}
						}

					}
					break;
				case TRANSFER_STOP:
					if(msg.obj!=null){
						if(msg.obj instanceof ChatMsgEntity)
							refreshData(new ChatRefreshBean((ChatMsgEntity)msg.obj,2));
					}
					break;
				case TRANSFER_NONE:
					if(msg.obj!=null){
						if(msg.obj instanceof ChatMsgEntity)
							refreshData(new ChatRefreshBean((ChatMsgEntity)msg.obj,2));
					}
					break;
				case TRANSFER_ERROR:
					if(msg.obj!=null){
						if(msg.obj instanceof ChatMsgEntity)
							refreshData(new ChatRefreshBean((ChatMsgEntity)msg.obj,2));
					}
					break;
				default:
					break;
			}
		}

	};

	/**发送状态
	 ////返回:	/// -1:不存在. 0:等待接受,1:正在传输,2:停止,3:出错,>=10:成功.
	 */
	private void queryStatusTask(final byte[] traceNo,final ChatMsgEntity entity){

		if(traceNo==null||entity==null){
			return ;
		}
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				boolean isWorking=true;
				while(isWorking){
					int status=xwDC.queryMessageStatus(traceNo);
					Log.e("status",""+status);
					if(status==0){
						/**
						 * obtainMessage将从MessagePool取得消息对象，不需要new
						 */
						//等待接受
						Message toMain =myHandler.obtainMessage();
						toMain.what=WAITING_STATUS;
						toMain.obj=entity;
						myHandler.sendMessage(toMain);

					}else if(status==1){
						//正在传输
						Message toMain =myHandler.obtainMessage();
						toMain.what=TRANSFER_STATUS;
						toMain.obj=entity;
						myHandler.sendMessage(toMain);

					}else if(status==2){
						//更新记录
						entity.setSendFlag(2);
						XWDataCenter.getMessageDB().updateMsg(XWDataCenter.getCurAccount(),friendAccount, entity);
						//停止
						Message toMain =myHandler.obtainMessage();
						toMain.what=TRANSFER_STOP;
						toMain.obj=entity;
						myHandler.sendMessage(toMain);
						isWorking=false;
					}else if(status==3){
						//更新记录
						entity.setSendFlag(3);
						XWDataCenter.getMessageDB().updateMsg(XWDataCenter.getCurAccount(),friendAccount, entity);
						//出差错
						Message toMain =myHandler.obtainMessage();
						toMain.what=TRANSFER_ERROR;
						toMain.obj=entity;
						myHandler.sendMessage(toMain);
						isWorking=false;
					}else if(status>=10){
						//更新记录
						entity.setSendFlag(10);
						XWDataCenter.getMessageDB().updateMsg(XWDataCenter.getCurAccount(),friendAccount, entity);
						//成功
						Message toMain =myHandler.obtainMessage();
						toMain.what= TRANSFER_END;
						toMain.obj=entity;
						myHandler.sendMessage(toMain);
						isWorking=false;
					}else if(status==-1){
						//更新记录
						entity.setSendFlag(-1);
						XWDataCenter.getMessageDB().saveMsg(XWDataCenter.getCurAccount(),friendAccount, entity);
						//不存在
						Message toMain =myHandler.obtainMessage();
						toMain.what=TRANSFER_NONE;
						toMain.obj=entity;
						myHandler.sendMessage(toMain);
						isWorking=false;
					}
					try {
						//线程等待3秒
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}




	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				try
				{
					AudioManager audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
					audio.adjustStreamVolume(
							AudioManager.STREAM_MUSIC,
							AudioManager.ADJUST_RAISE,
							AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				try
				{
					AudioManager audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
					audio.adjustStreamVolume(
							AudioManager.STREAM_MUSIC,
							AudioManager.ADJUST_LOWER,
							AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				return true;
			default:
				break;
		}

		return super.onKeyDown(keyCode, event);
	}

	// ------------------------监听文件传输状态------------------------------
	private void startViewRefreshListener() {
		///////if (!mIsViewRefreshing)
		///////////////2016-10-24,确保定时处理启动！
		{
			mIsViewRefreshing = true;
			mImeHideHandler.removeCallbacks(mImeHideRunnable);
			mImeHideHandler.postDelayed(mImeHideRunnable, 1);
		}
	}

	private void stopViewRefreshListener() {
		mIsViewRefreshing = false;
		mImeHideHandler.removeCallbacks(mImeHideRunnable);
		//		setUnCompleteTask();
	}

	private long lLastRequestCredit=0;
	////private String sLastOnlineStatus=null;

	private Handler mImeHideHandler = new Handler();
	public boolean mIsViewRefreshing = false;
	private Runnable mImeHideRunnable = new Runnable() {
		int sleep = 3000;

		@Override
		public void run() {
			Log.e(TAG,"mImeHideRunnable");
			if( (MainApplication.getInstance().mFile==null)||
					(MainApplication.getInstance().mFile.isEmpty())) {
				mIsViewRefreshing=false;
			}

			////////////下次运行!!!!
			if(mIsViewRefreshing /*|| !bIsAESOK*/)
				mImeHideHandler.postDelayed(this, sleep);

			if(mIsViewRefreshing )
			{

				ArrayList<Long> removeFile = null;
				// 设置所有传输文件的状态
				for (long key : MainApplication.getInstance().mFile.keySet()) {
					byte[] pStatus = new byte[4]; // 发送的状态
					byte[] pProgress = new byte[4]; // 发送的进度
					ChatMsgEntity bean = MainApplication.getInstance().mFile.get(key);


					int iRet = -1;
					if (bean == null)
						continue;

					if (bean.sAESFilePath == null) {
						iRet = xwDC.XWWeiXinQuerySendFileStatus(
								(bean.getFriendAccount() + "\0").getBytes(),
								(bean.getFilePath() + "\0").getBytes(), pStatus,
								pProgress);
					} else {
						iRet = xwDC.XWWeiXinQuerySendFileStatus(
								(bean.getFriendAccount() + "\0").getBytes(),
								(bean.sAESFilePath + "\0").getBytes(), pStatus,
								pProgress);
					}


					int status = XWDataCenter.XWBytesToInt(pStatus);
					int progress = XWDataCenter.XWBytesToInt(pProgress);
					Log.e("send","send stat:"+status+",pro"+progress+",ret"+iRet);
					if ((iRet != 0) || ((status != 0) && (status != 1))) {//status 2||3表示失败
						if (iRet != 0) {
							status = -1;
							progress = 0;
						} else if (status >= 10) {
							progress = 100;
						} else if (status == 3 || status == 2) {
							status = -1;                  //置为发送失败
						}

						bean.setSendFlag(status);
						bean.setProgress(progress);
						saveMsg(bean);

						// 通知改变UI
						Message toMain = myHandler.obtainMessage();
						toMain.what = TRANSFER_END;
						toMain.obj = bean;
						myHandler.sendMessage(toMain);
						//下载完的或失败的添加到删除队列
						if (removeFile == null) {
							removeFile = new ArrayList<Long>();
						}
						//添加下载完队列
						removeFile.add(key);
					} else {
						bean.setSendFlag(status);
						bean.setProgress(progress);
						saveMsg(bean);
						// 通知改变UI
						Message toMain = myHandler.obtainMessage();
						toMain.what = MSG_UPDATE;
						toMain.obj = bean;
						myHandler.sendMessage(toMain);
					}
				}

				if (removeFile != null && !removeFile.isEmpty()) {
					for (long key : removeFile) {
						if (MainApplication.getInstance().mFile != null && MainApplication.getInstance().mFile.containsKey(key)) {
							MainApplication.getInstance().mFile.remove(key);
						}

					}
					removeFile.clear();
				}

			}


		}
	};




	/**重发
	 */
	public void resendFile(final ChatMsgEntity entity){
		if(entity==null){
			return;
		}
		final String filepath=entity.getFilePath();
		if(TextUtils.isEmpty(filepath)){
			return;
		}

		String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date());

		entity.setDate(ctime);

		saveMsg(entity);
		//刷新界面
		refreshData();

		//////////////////////启动线程来发送微信!!!!!!!!!!!!!!!!!
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				//////发送文件路径
				String filesendpath=filepath;
				Log.e("resend","resend srcFile:"+filesendpath);
				//////先检查发送文件是否存在，或加密文件未解码
				if(!FileUtil.isFileExist(filepath)){
					////从加密文件解码
					String enPath=entity.getFilePath()+JRSConstants.ENCRYPT_END;
					if(FileUtil.isFileExist(enPath)){
						decodeFile(enPath,filepath);
					}
				}
				File srcFile=new File(filepath);
				if(!srcFile.exists()){
					/////源文件丢失了
					return;
				}else{
					///////对方文件重命名为16进制编码
					String hexname=FileUtil.toHexName(srcFile.getName());
					hexname=HEX_PRE+hexname;
					FileUtil.renameFile(srcFile.getParent(),srcFile.getName(),hexname);
					/////重设发送文件路径
					filesendpath=srcFile.getParent()+"/"+hexname;
					Log.e("resend","resend getHexFile:"+filesendpath);
				}


				int iOffLine=1;

				String sAESPassword=xwDC.getFriendAESPassword( fni.getLogin_name(),xwDC.loginName);
				if (sAESPassword!=null)
				{
					/////生成发送的aes文件放在aesfiles目录下，发送完要删除
					try
					{
						File f=new File(filesendpath);
						String aespath=UriConfig.getSavePath()+"/aesfiles/"+fni.getLogin_name();
						UriConfig.makeFileDirs(aespath);
						aespath+="/"+f.getName();

						if (xwDC.AESEncodeFile(filesendpath, aespath, sAESPassword))
						{
							//////////////////send aes encoded path!!!!
							filesendpath=aespath;
							entity.sAESFilePath=filesendpath;
						}else{
							//加密失败
							entity.setSendFlag(-1);
							saveMsg(entity);
							//清掉任务队列
							if (MainApplication.getInstance().mFile!=null&&MainApplication.getInstance().mFile.containsKey(entity.getNo())) {

								MainApplication.getInstance().mFile.remove(entity.getNo());
							}
							// 通知改变UI
							Message toMain =myHandler.obtainMessage();
							toMain.what=MSG_UPDATE;
							toMain.obj=entity;
							myHandler.sendMessage(toMain);

							Message msg=myHandler.obtainMessage();
							msg.what=MSG_TOAST;
							msg.obj="encrypt fail";
							myHandler.sendMessage(msg);
							return;

						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}

				Log.e("resend"," resend filepath:"+filesendpath);
				int iret=xwDC.XWWeiXinRequestSendFile((fni.getLogin_name() + "\0").getBytes(),
						(filesendpath + "\0").getBytes(), XWDataCenter.XWIntToBytes(iOffLine));

				////////////////////////////////加入跟踪,在调用发送之后
				if (!MainApplication.getInstance().mFile.containsKey(entity.getNo())) {
					MainApplication.getInstance().mFile.put(entity.getNo(), entity);
				}

				if(iret==0){
					startViewRefreshListener();
				}else{
					//调用发送失败
					entity.setSendFlag(-1);
					saveMsg(entity);
					// 通知改变UI
					Message toMain =myHandler.obtainMessage();
					toMain.what=TRANSFER_END;
					toMain.obj=entity;
					myHandler.sendMessage(toMain);
				}
			}
		});



	}


	private void compress2Send(String path){
		if(TextUtils.isEmpty(path)){
			return;
		}
		File srcFile=new File(path);
		Luban.get(MainApplication.getInstance())
				.load(srcFile)//传人要压缩的图片
				.putGear(Luban.THIRD_GEAR)      //设定压缩档次，默认三挡
				.setFilename(System.currentTimeMillis() + "")
				.setCompressListener(new OnCompressListener() { //设置回调

					@Override
					public void onStart() {}
					@Override
					public void onSuccess(File file) {
						sendFile(file.getAbsolutePath(),"image");
					}

					@Override
					public void onError(Throwable e) {}
				}).launch();    //启动压缩
	}
	/**
	 * 发送文件
	 */
	public int sendFile(final String filepath, final String type) {

		if (!com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(this))
		{
			new  AlertDialog.Builder(this)
					.setTitle(XWCodeTrans.doTrans("请插入正确的安全T卡!") )
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton( XWCodeTrans.doTrans("确定") ,

							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int whichButton){
									dialog.dismiss();

									finish();
								}
							}
					)
					.show();

		}


		if(TextUtils.isEmpty(filepath)){
			return 0;
		}
		//复制文件到本账户加密文件目录
		boolean delsrc=false;
		if(type.equals("voice")||type.equals("video")){
			delsrc=true;
		}
		final String aesgotPath=copySendFile(filepath,delsrc);
        Log.e("chat","aesgotpath:"+aesgotPath);
		if(type.equals("video")){////清理视频临时文件
			UriConfig.deleteInDir(UriConfig.getVideoSavePath());
		}
		String ctime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date());
		String conStr = "(:" + type + ")"; //+ name;//filepath;     //将文件名保存为内容
		//保存发送文件消息
		final ChatMsgEntity entity=new ChatMsgEntity();
		entity.setNo(System.currentTimeMillis());
		entity.setFriendAccount(friendAccount);
		entity.setName(XWDataCenter.getCurAccount());
		entity.setDate(ctime);
		entity.setMessage(conStr);
		entity.setImg(0);
		entity.setComMeg(0);
		entity.setSendFlag(1);
		entity.setFilePath(aesgotPath);
		entity.setRead(0);

		if(type.equals("file")){
			entity.setMsgType(MSG_FILE);
		}else if(type.equals("image")){
			entity.setMsgType(MSG_PHOTO);
		}else if(type.equals("voice")){
			entity.setMsgType(MSG_VOICE);
		}else if(type.equals("video")){
			entity.setMsgType(MSG_VIDEO);
		}
		saveMsg(entity);
		/////发送文件时不需操作
//		conStr += "\0";
//		ctime += "\0";
		//刷新界面
		refreshData(new ChatRefreshBean(entity, 1));

		//////////////////////启动线程来发送微信!!!!!!!!!!!!!!!!!
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				int iOffLine=1;
				String filesendpath=aesgotPath;
				////////////////////////////////2014-09-09,
				String sAESPassword=xwDC.getFriendAESPassword( fni.getLogin_name(),xwDC.loginName);
				if (sAESPassword!=null)
				{
					/////生成发送的aes文件放在aesfiles目录下，发送完要删除
					try
					{
						File f=new File(filesendpath);
						String aespath=UriConfig.getSavePath()+"/aesfiles/"+fni.getLogin_name();
						UriConfig.makeFileDirs(aespath);
						aespath+="/"+f.getName();
						if (xwDC.AESEncodeFile(filesendpath, aespath, sAESPassword))
						{
							//////////////////send aes encoded path!!!!
							filesendpath=aespath;
							entity.sAESFilePath=filesendpath;
						}else{
							//加密失败
							entity.setSendFlag(-1);
							saveMsg(entity);
							//清掉任务队列
							if (MainApplication.getInstance().mFile!=null&&MainApplication.getInstance().mFile.containsKey(entity.getNo())) {

								MainApplication.getInstance().mFile.remove(entity.getNo());
							}
							// 通知改变UI
							Message toMain =myHandler.obtainMessage();
							toMain.what=MSG_UPDATE;
							toMain.obj=entity;
							myHandler.sendMessage(toMain);

							Message msg=myHandler.obtainMessage();
							msg.what=MSG_TOAST;
							msg.obj="encrypt fail";
							myHandler.sendMessage(msg);
							return;

						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}

				int iret=xwDC.XWWeiXinRequestSendFile((friendAccount + "\0").getBytes(),
						(filesendpath + "\0").getBytes(), XWDataCenter.XWIntToBytes(iOffLine));
				if (!MainApplication.getInstance().mFile.containsKey(entity.getNo())) {
					MainApplication.getInstance().mFile.put(entity.getNo(), entity);
				}
				Log.e("send"," send filepath:"+filesendpath+",iret=="+iret);
				if(iret==0){
					startViewRefreshListener();
				}else{
					//调用发送失败
					entity.setSendFlag(-1);
					saveMsg(entity);
					//清掉任务队列
					if (MainApplication.getInstance().mFile!=null&&MainApplication.getInstance().mFile.containsKey(entity.getNo())) {

						MainApplication.getInstance().mFile.remove(entity.getNo());
					}
					//删除发送时产生的AES文件
					if(!TextUtils.isEmpty(entity.sAESFilePath)){
						UriConfig.delete(entity.sAESFilePath);
					}
					// 通知改变UI
					Message toMain =myHandler.obtainMessage();
					toMain.what=TRANSFER_END;
					toMain.obj=entity;
					myHandler.sendMessage(toMain);
				}
			}
		});

		return 0;
	}


	/**
	 * 图片选择、剪切回调/文件选择回调
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//////选择图片返回
		if (resultCode == ImagePicker.RESULT_CODE_ITEMS&&data != null) {
			boolean isOrigin = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
			Log.e(TAG,"发送原图："+isOrigin);
			if (requestCode == CropUtil.PHOTO_PICKED_WITH_DATA) {
				ArrayList<ImageItem> imageItems = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
				if (imageItems != null && imageItems.size() > 0) {
					if(isOrigin){
						sendFile(imageItems.get(0).path,"image");
					}else{
						///压缩上传
						compress2Send(imageItems.get(0).path);
					}

//					uploadPic(imageItems.get(0).path, XWDataCenter.xwDC.loginName,getSignature(),MSG_HEAD_UPDATE);
				}
			}
		}else{
			if (resultCode != RESULT_OK)
				return;
			switch (requestCode) {
				case CropUtil.OPEN_FILE_BROWSER_DATA://返回文件路径
                    if(data!=null){
						Uri myUri = data.getData();
						if(myUri!=null){
							String path= ContentProviderUtil.getPath(baseAct,myUri);
							if(!TextUtils.isEmpty(path)){
								Log.e("chat","get filepaht:"+path);
								String lowerPath  = path.trim().toLowerCase();
								if(lowerPath.endsWith("jpg") || lowerPath.endsWith("bmp")|| lowerPath.endsWith("png")|| lowerPath.endsWith("gif")){
									sendFile(path, "image");
								}else{
									sendFile(path, "file");
								}
							}
						}
					}
					break;
				case CropUtil.REQ_FILEDIR:
					if(data!=null){
						Uri myUri = data.getData();
						if(myUri!=null){
							String path= ContentProviderUtil.getPath(baseAct,myUri);
							if(!TextUtils.isEmpty(path)){
								Log.e("chat","get filepath:"+path);
								File file=new File(path);
								if(file.exists()){
									if(decodeEntity!=null){
										String fileName=FileUtil.getFileName(decodeEntity.getFilePath());
										String targetPath=file.getParent()+"/"+fileName;
										if(!TextUtils.isEmpty(fileName)){
											decode2OtherDir(decodeEntity,targetPath);
										}
									}
								}

							}
						}
					}


					break;
				default:
					break;

			}

		}




	}



	/**
	 * 解码文件到目标目录
	 */
	public ChatMsgEntity decodeEntity=null;//需要解压的消息
	boolean decodeLoading=false;
	public void decode2OtherDir(final ChatMsgEntity entity, final String targetPath){
		new AsyncTask<String,Integer, String>() {

			@Override
			protected String doInBackground(String... arg0) {
				String path=null;
				try{
					decodeLoading=true;
					////从加密文件解码
					String enPath=entity.getFilePath()+JRSConstants.ENCRYPT_END;
					if(FileUtil.isFileExist(enPath)){
						boolean bOK=XWDataCenter.decodeFile(enPath,targetPath);
						if(bOK){
							path=targetPath;
						}
					}

				}catch(Exception e){
					e.printStackTrace();
				}

				return path;
			}

			@Override
			protected void onPreExecute() {
				decodeLoading=true;
				showPlg("");
			};

			@Override
			protected void onPostExecute(String result) {
				decodeLoading=false;
				disPlg();
				if(entity.getComMeg()==1&&entity.getSendFlag()==11){///更新未读标识
					entity.setSendFlag(10);
					entity.setSnap(1);
					//更新未读标识
					saveMsg(entity);
					//更新UI
					refreshData();
				}

                if(TextUtils.isEmpty(result)){
					showToastTips(XWCodeTrans.doTrans("导出失败!"));
				}else{
					////判断解码文件存在
					File deFile=new File(result);
					if(!deFile.exists()){
						showToastTips("no file");
					}else{
						/////解码导出后不需要马上打开
						showToastTips(XWCodeTrans.doTrans("导出成功!"));
					}
				}


			};

		}.execute("");
	}

	////查看文件
	public void viewFile(ChatMsgEntity entity){
		try {
			String path = entity.getFilePath();
			int type = entity.getMsgType();
			if (type == FriendChatRecord.MSG_PHOTO) {
				Intent intent = new Intent(baseAct, PhotoViewUI.class);
				String picPath = "file://" + path;
				intent.putExtra(JRSConstants.DATA, picPath);
				startActivity(intent);
			} else if (type == FriendChatRecord.MSG_VOICE) {
				XWDataCenter.xwDC.XWPlayWeiXinAudioFile(path);
			} else if (type == FriendChatRecord.MSG_FILE) {
				File f = new File(path);
				if (f.exists()) {
					openFile(f);
				}
			}else if(type==FriendChatRecord.MSG_VIDEO){
				startActivity(new Intent(this, VideoPlayerActivity.class).putExtra(
						"path", path));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

    private void setViewDisplay(View view,int visible){
		if(view!=null){
			view.setVisibility(visible);
		}
	}
	public void setAeslockView(int visible){
		setViewDisplay(aeslock,visible);
	}
	private long lastShowTips=0;
	public void UpdateAESIcon()
	{
		////////////////协商密码
		if (fni!=null)
		{
			if (XWDataCenter.getFriendAESPassword(XWDataCenter.xwDC.loginName, fni.getLogin_name())!=null)
			{
				mHandler.sendEmptyMessage(9);
			}
			else
			{
				mHandler.sendEmptyMessage(10);
				if(System.currentTimeMillis()-lastShowTips>=10000) {
					mHandler.sendEmptyMessage(11);
					lastShowTips=System.currentTimeMillis();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		Log.e("XIM","FriendChatRecord onResume");
		super.onResume();

		startViewRefreshListener();

		////////////////设置标题
		if (fni!=null){
			setTitle(fni.getOnline_status()==null?XWCodeTrans.doTrans("断开"):fni.getOnline_status());
            cleanMsgNotice();
		}


		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				checkCredit();
			}
		},JRSConstants.SECOND_VALUE);
	}

	@Override
	protected void onPause() {
		super.onPause();
		///保存输入状态
		if(keyboardLayout.getInputArea().isShown()){
			PrefsUtils.getInstance().put(JRSConstants.KEY_INPUT_STATUS,0);
		}else{
			PrefsUtils.getInstance().put(JRSConstants.KEY_INPUT_STATUS,1);
		}
	}

	///清理消息提示
	private void cleanMsgNotice(){
		//消息置为已读
		XWDataCenter.getChatHistoryDB().resetUnread(XWDataCenter.getCurAccount(),fni.getLogin_name());
		XWDataCenter.getMessageDB().resetUnread(XWDataCenter.getCurAccount(),fni.getLogin_name());
		///清理状态栏通知
		NotificationUtil.cleanNotificationMsg(fni.getLogin_name());
	}

	/**
	 * 检查安全信息
	 */
	private void checkCredit(){
		final long lst=System.currentTimeMillis();

		if (!com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(this))
		{
			Log.e(TAG,"checkJRSSDK take time:"+(System.currentTimeMillis()-lst)*0.001);
			new  AlertDialog.Builder(this)
					.setTitle(XWCodeTrans.doTrans("请插入正确的安全T卡!") )
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton( XWCodeTrans.doTrans("确定") ,

							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int whichButton){
									dialog.dismiss();

									finish();
								}
							}
					)
					.show();

			return;
		}

		if (fni!=null)
		{
			TaskExecutor.executeTask(new Runnable() {
				@Override
				public void run() {
					XWDataCenter.SendCreditMessage(fni.getLogin_name(),XWDataCenter.CREDIT_REQUEST);
					Log.e(TAG,"sendCredit take time:"+(System.currentTimeMillis()-lst)*0.001);
					UpdateAESIcon();
					Log.e(TAG,"update take time:"+(System.currentTimeMillis()-lst)*0.001);
				}
			});
		}
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		//停止
		stopViewRefreshListener();
		BusProvider.getInstance().unregister(this);
		if(imagePicker!=null){
			imagePicker.clear();
		}
		if(keyboardLayout!=null){
			keyboardLayout.clearInputArea();
			keyboardLayout.clearInputAreaListener();
			keyboardLayout.setOnKeyBoardBarListener(null);
		}
		Luban.get(MainApplication.getInstance()).setCompressListener(null);
		clearDecryptFile();
		clearListData();
		if(mAdapter!=null){
			mAdapter.setContext(null);
		}
	}

	private void clearListData(){
		snapchatList.clear();
		mListData.clear();
	}
	//清理解码文件
    public void clearDecryptFile(){
		if(mAdapter!=null){
			for(ChatMsgEntity msgEntity:mAdapter.decryptList){
				if(msgEntity!=null)
					XWDataCenter.delDecrypt(XWDataCenter.getDecryptFilepath(msgEntity));
			}
			mAdapter.decryptList.clear();
		}
	}

	public void setOnePageRecord() {

		Log.e("XIM","setOnePageRecord");
		///////////2013-01-10，如果太近,会出现重复显示的问题!!!!!!!!!!!!!!!!



		Log.e("XIM","setOnePageRecord begin refresh!!!");

	}


	/**
	 * 添加记录到界面上显示
	 */
	public void addRecord(String nickname, String ctime, final String content,int flag) {// 本方法由JNI调用

		Log.e("XIM","FriendChatRecord addRecord  flag"+flag);

		//////////////////2013-01-10,表明是数据开始!!!!!!!!!!!!!!!!!
		if (flag ==3) {// 数据读到了最后



			return;
		}

		if (flag > 1) {// 数据读到了最后

			return;
		}
		if (flag == 0) {

		} else {


		}


	}










	/**
	 * 实现文本复制功能
	 */
	public static void copy(String content, Context context)
	{
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText( (content.trim()));

	}
	/**
	 * 实现粘贴功能
	 */
	public static String paste(Context context)
	{
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
		return cmb.getText().toString().trim();
	}


	@Override
	protected void onStop() {
		super.onStop();
		Log.e(TAG,"onStop");
		/////////////////////
		if (bIsWeiXin) //////如果正在录音,则取消!!!!!!2014-10-20
			XWDataCenter.xwDC.XWStopWeiXinAudio();

	}
	@Override
	public void onRecordingAction(ChatKeyboardLayout.RecordingAction action) {
		Log.e("main","onRecordingAction:"+action);
		switch (action) {
			case START:
				rlRecordArea.show(1);
				rlRecordArea.startVoiceAnim(R.drawable.anim_voicelevel);
				startWeiXinAudio();
				break;
			case RESTORE:
				rlRecordArea.show(1);
				break;
			case WILLCANCEL:
				rlRecordArea.show(0);
				break;
			case CANCELED:
				rlRecordArea.stopVoiceAnim();
				rlRecordArea.hide();
				stopWeiXinAudio(false);
				break;
			case COMPLETE:
				rlRecordArea.stopVoiceAnim();
				rlRecordArea.hide();
				stopWeiXinAudio(true);
				break;
		}
	}
	@Override
	public void onMediaClick(int id) {
		switch (id){
			case 1://图片
				selectImage(CropUtil.PHOTO_PICKED_WITH_DATA);
				break;
			case 2://文件
//				Intent intent = new Intent(FriendChatRecord.this,
//						MyFileManager.class);
//				startActivityForResult(intent, CropUtil.OPEN_FILE_BROWSER_DATA);
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				startActivityForResult(intent,CropUtil.OPEN_FILE_BROWSER_DATA);
				break;
			case 3://视频
				startRecVideo();
				break;
		}
	}

	@Override
	public void onSendBtnClick(String msg) {
		Log.e("main","msg:"+msg);
		keyboardLayout.clearInputArea();
		Log.e("main","msg:"+msg.length());
		ChatMsgEntity entity=createSendMsg(msg);
		if(entity!=null){
			sendText(entity);
		}
	}

	private ChatMsgEntity createSendMsg(String msg){
		if (!com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(FriendChatRecord.this))
		{
			new  AlertDialog.Builder(this)
					.setTitle(XWCodeTrans.doTrans("请插入正确的安全T卡!") )
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton( XWCodeTrans.doTrans("确定") ,

							new DialogInterface.OnClickListener(){
								public void onClick(DialogInterface dialog, int whichButton){
									dialog.dismiss();

									FriendChatRecord.this.finish();
								}
							}
					)
					.show();
			return null;
		}

		String conStr = msg;
		if(conStr.trim().length()<1){
			return null;// 不发送空消息
		}
		String ctime = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss").format(new Date());

		//创建发送消息
		ChatMsgEntity entity=new ChatMsgEntity();
		entity.setNo(System.currentTimeMillis());
		entity.setFriendAccount(friendAccount);
		entity.setName(XWDataCenter.getCurAccount());
		entity.setDate(ctime);
		entity.setMessage(conStr);
		entity.setImg(0);
		entity.setComMeg(0);
		entity.setSendFlag(10);
		entity.setFilePath("");
		entity.setRead(0);
		return entity;
	}

	@Override
	public void onUserDefEmoticonClicked(String tag, String uri) {
		Log.e("main","emoj tag:"+tag+",uri:"+uri);
	}

	@Subscribe
	public void onChatMsgEvent(ChatMsgEvent event){
		if(!bIsDestroy){
			Log.e(TAG,"onChatMsgEvent");
			if(event!=null&&event.bean!=null)
				refreshData(event.bean);
		}
	}

	@Subscribe
	public void onAvatarUpdateEvent(AvatarUpdateEvent event){
		if(event!=null){
			if(event.fni!=null){
				if(event.fni.getLogin_name().equals(friendAccount)){
					updateHeads();
					refreshData();
				}
			}
		}

	}

	@Subscribe
	public void onUpdateAESEvent(UpdateAESEvent event){
		if(!bIsDestroy){
			mHandler.removeMessages(12);
			mHandler.sendEmptyMessageDelayed(12,200);
		}
	}

	////复制文件到aesgotfiles加密目录,语音源文件要删除
	private String copySendFile(String filepath,boolean delsrc){
		String aespath=null;
		if(!TextUtils.isEmpty(filepath)){
			File f=new File(filepath);
			if(f.exists()){
				aespath=UriConfig.getSavePath()+"/aesgotfiles/"+XWDataCenter.getCurAccount();
				UriConfig.makeFileDirs(aespath);
				////文件名要去中文，避免乱码
				String filename=FileUtil.toHexName(f.getName());
				if(delsrc&&filename.endsWith(".mp4")){
					filename=filename.replace(".mp4",".mpx");
				}
				aespath+="/"+HEX_PRE+filename;
				Util.copyFile(f,aespath);
				if(delsrc){
					f.delete();
				}
			}
		}

		return aespath;
	}

	/**
	 * 选择图片
	 * @param type 选择类型，CropUtil.PHOTO_PICKED_WITH_DATA普通，
	 */
	private void selectImage(int type){
		imagePicker = ImagePicker.getInstance();
		imagePicker.setImageLoader(new GlideImageLoader());
		imagePicker.setMultiMode(false);   //多选
		imagePicker.setShowCamera(true);  //显示拍照按钮
		imagePicker.setSelectLimit(1);    //最多选择9张
		imagePicker.setCrop(false);
		Intent intent = new Intent(this, ImageGridActivity.class);
		startActivityForResult(intent, type);
	}


}
