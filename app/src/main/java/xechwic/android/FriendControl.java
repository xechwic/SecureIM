/*
 * 主界面 吴潇 
 * 20110929修改
 * */
package xechwic.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.squareup.otto.Subscribe;
import com.zbar.lib.CaptureActivity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xechwic.android.act.MainApplication;
import xechwic.android.act.ServerConfig;
import xechwic.android.adapter.VPHomeAdapter;
import xechwic.android.base.BaseLazyFragment;
import xechwic.android.bean.BeanOperate;
import xechwic.android.bean.HeadBean;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.AvatarUpdateEvent;
import xechwic.android.bus.event.BackUpEvent;
import xechwic.android.bus.event.ChatMsgEvent;
import xechwic.android.bus.event.FragmentRereshEvent;
import xechwic.android.bus.event.GroupUpdateEvent;
import xechwic.android.bus.event.HeadListEvent;
import xechwic.android.bus.event.LogoutXWIMEvent;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.ui.BaseUI;
import xechwic.android.ui.FriendDetailUI;
import xechwic.android.ui.PhoneContactUI;
import xechwic.android.ui.fragment.CallRecordFragment;
import xechwic.android.ui.fragment.ContactFragment;
import xechwic.android.ui.fragment.MsgListFragment;
import xechwic.android.ui.fragment.SettingFragment;
import xechwic.android.util.AppConfig;
import xechwic.android.util.AudioRecordUtil;
import xechwic.android.util.BatteryUtil;
import xechwic.android.util.FileUtil;
import xechwic.android.util.Http;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.NetTaskUtil;
import xechwic.android.util.NotificationUtil;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.XWDataCenterMessage;
import xechwic.android.view.MorePopupView;
import xechwic.android.view.XViewPager;
import ydx.securephone.R;
import ydx.securephone.R.color;

public class FriendControl extends BaseUI implements OnClickListener{
	String TAG=FriendControl.class.getSimpleName();
    private static final int MENU_ITEM_NUM=4;

	public FriendNodeInfo csFn = null;// 当前被单击选中时的节点,即应该高亮的好友



	private MorePopupView menuWindow; //弹出框
	private Dialog  builderDg=null;//好友选择列表对话框
	public Handler mHandler;
	public List<String> groupNameList=new ArrayList<>() ;// 大组成员名
	public static List<FriendNodeInfo> friendList=new ArrayList<>();//好友list
	private ImageView[] imgs;//菜单视图
	private TextView[]  txs; //菜单文字

	private TextView tx_tips;//消息提示
	private int[] imgsDefault;//默认图片资源的Id
	private int[] imgsSelected;//选择的图片资源id
	private ImageView iv_back2_video;//返回视频按钮
  public static final int CALLRECORD_INDEX=0;
	public static final int CONTACT_INDEX=1;
	public static final int MSGLIST_INDEX=2;
	public static final int SYS_SETTING=3;

	@BindView(R.id.viewpager)
	XViewPager mViewPager;
	private VPHomeAdapter mPagerAdapter;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(bIsInterrupt){////界面恢复被打断
			Log.e(TAG,"onCreate interrupt");
			return;
		}
		Log.e(TAG,"onCreate");
		setContentView(R.layout.friend_control);
		ButterKnife.bind(this);
        xwDC=MainApplication.getInstance().getDC();

		/////请求忽略电池优化
		BatteryUtil.isIgnoreBatteryOption(this,JRSConstants.REQ_IGNORE_BATTERY_CODE);

		//初始化控件
		initView();
		initTitleBarView();
		//默认显示
		selectBottomItem(defIndex,false);
//		if(AppConfig.getVersion()==Version.TW){
//			selectBottomItem(0,false);//聊天
//		}else if(AppConfig.getVersion()==Version.BJ){
//			selectBottomItem(2,false);//拨号盘
//		}
		handleIntent(getIntent());
		FileUtil.createGuardFile();
		/////启动双进程
		startService(new Intent(this, XWServices.class).setAction(JRSConstants.CMD_ACTION_START_REMOTE));
		doCreateAfter();
	}


	private ImageView ivBack;
	private ImageView ivAvatar;
	private TextView tvOLStatus;
	private TextView tvTitle;
	private ImageView ivAdd;
	private void initTitleBarView(){
		ivBack=(ImageView)findViewById(R.id.iv_back);
		ivAvatar=(ImageView)findViewById(R.id.iv_avatar);
		tvOLStatus=(TextView) findViewById(R.id.tv_onlinestatus);
		tvTitle=(TextView) findViewById(R.id.tv_title);
		ivAdd=(ImageView)findViewById(R.id.iv_add);
		ivBack.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				//退出提示
				gotoHomeActivity();
			}
		});
		ivAdd.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				//更多选项
				menuWindow.showAsDropDown(ivAdd, 0, 0);
			}
		});
	}

	private void refreshSmallAvatar(){
		if(getCurIndex()==CALLRECORD_INDEX){
			return;
		}
    String pic=FriendNodeDB.getMyHead();
    if(!TextUtils.isEmpty(pic)){
      Glide.with(MainApplication.getInstance())
          .load(Http.getHeadPicUrl()+pic)
          .diskCacheStrategy(DiskCacheStrategy.ALL)
          .error(R.drawable.def_avatar)
          .into(ivAvatar);
    }
  }

  private void showTitleBarView(boolean show){
		if(show){
			ivBack.setVisibility(View.VISIBLE);
			ivAvatar.setVisibility(View.VISIBLE);
			tvOLStatus.setVisibility(View.VISIBLE);
			ivAdd.setVisibility(View.VISIBLE);
		  refreshSmallAvatar();
		}else{
			ivBack.setVisibility(View.INVISIBLE);
			ivAvatar.setVisibility(View.INVISIBLE);
			tvOLStatus.setVisibility(View.INVISIBLE);
			ivAdd.setVisibility(View.INVISIBLE);
		}
	}

	private void setUpTitleView(int index){
		 switch (index){
			 case CALLRECORD_INDEX:
			 	tvTitle.setText(XWCodeTrans.doTrans("通话记录"));
				 showTitleBarView(false);
			 	break;
			 case CONTACT_INDEX:
				 tvTitle.setText(XWCodeTrans.doTrans("联系人"));
				 showTitleBarView(true);
			 	break;
			 case MSGLIST_INDEX:
				 tvTitle.setText(XWCodeTrans.doTrans("消息"));
				 showTitleBarView(true);
			 	break;
			 case SYS_SETTING:
				 tvTitle.setText(XWCodeTrans.doTrans("设置"));
				 showTitleBarView(true);
			 	break;
		 }
	}

	private void showRECPermision(){
		final NormalDialog permissionDg = new NormalDialog(this).isTitleShow(false)
				.content(getRECPromtTips())
				.btnNum(2).btnText(getResources().getString(R.string.alert_cancel),
						getResources().getString(R.string.alert_confirm));
		permissionDg.setOnBtnClickL(new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				permissionDg.dismiss();
			}
		}, new OnBtnClickL() {
			@Override
			public void onBtnClick() {
				permissionDg.dismiss();


			}
		});

		permissionDg.show();
	}

	private String getRECPromtTips(){
	   String appName=MainApplication.getInstance().getResources().getString(R.string.app_name);
		 String strs=
				 XWCodeTrans.doTrans("请尝试按以下路径开启录音权限：")
				 +"\n"
		         +XWCodeTrans.doTrans("方法一：设置->权限管理->应用程序->")
				 +appName
				 +XWCodeTrans.doTrans("->录音->允许。")
				 +"\n"
				 +XWCodeTrans.doTrans("方法二：在系统应用管理中卸载本应用，重新安装，在安装时选择信任本应用，遇到权限提示时均选择允许。")
				 ;
	   return strs;
	}

	protected void doCreateAfter(){
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(!bIsDestroy) {
					if (xwDC.netPhoneTime == 0) {////不在视频通讯中，检测录音权限
						if (!AudioRecordUtil.isHasPermission(baseAct)) {
							showToastTips(XWCodeTrans.doTrans("请打开录音权限"));
							showRECPermision();
						}
					}
				}
			}
		},1000);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if(!bIsDestroy){
					if(XWDataCenter.headBeanMap==null||XWDataCenter.headBeanMap.isEmpty()){
						xwDC.startGetFriendsHeads();
					}
				}
			}
		},3000);
		//检测升级
		if(AppConfig.UPDATE_CONFIG){
			checkUpdate();
		}
	}

	private int defIndex;
	private int expandIndex=-1;
	@Override
	protected void doRetoreInstanceState(Bundle saveInstanceState) {
		if (saveInstanceState != null){
			defIndex=saveInstanceState.getInt(JRSConstants.SAVE_INDEX,0);
			expandIndex=saveInstanceState.getInt(JRSConstants.EXPAND_INDEX,0);

			Log.e(TAG,"doRetoreInstanceState index:"+defIndex+",expand:"+expandIndex);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.e(TAG,"onNewIntent");
		handleIntent(intent);
	}

	private void handleIntent(Intent intent){
			if(intent!=null){
				Bundle data=intent.getExtras();
				if(data!=null&&data.containsKey(JRSConstants.DATA)){
					int index=data.getInt(JRSConstants.DATA);
					Log.e(TAG,"handleIntent: index=="+index);
					selectBottomItem(index,false);
					////清理除了主界面的其他界面
					clearOtherAct();
				}
				////清理intent
				setIntent(null);
			}
	}

	///清理其他界面
	private void clearOtherAct(){
		if(xwDC==null||xwDC.activityList==null){
			return;
		}
		List<Activity> actList=new ArrayList<>();
		actList.addAll(xwDC.activityList);
		for(Activity act:actList){
			if(act!=null){
				if(act!=this){
					Log.e("main","finish:"+act.getLocalClassName());
					act.finish();
				}
			}
		}
		actList.clear();
		xwDC.activityList.clear();
		xwDC.activityList.add(this);
	}

	/**获取头像列表
	 */
	public static void getHeadBeanTask(final String name){
		Log.e("friendControl","getHeadBeanTask");
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				try
				{
					// http地址
					String httpUrl = Http.getFpfPicUrl()+"?user_id="+URLEncoder.encode(name,"gbk")+"&pwd="+XWDataCenter.getWEBAccessPassword();
					String strResult=NetTaskUtil.getDataTaskSync(httpUrl);
					if(!TextUtils.isEmpty(strResult)){
						List<HeadBean> list=BeanOperate.getHeadBeanList(strResult);
						if(list!=null&&!list.isEmpty()){
							for(HeadBean bean:list){
								String friendName=bean.getFriend_name();
								String imageName=bean.getImage_name();
								friendName=XWDataCenter.XWdecodeurl(friendName, "gbk");
								imageName=XWDataCenter.XWdecodeurl(imageName, "gbk");
								if(imageName!=null&&friendName!=null){
									//更新用户列表
									FriendNodeInfo node  = FriendNodeDB.getAFriend(XWDataCenter.getCurAccount(),friendName);
									if(node!=null){
										node.setIcon(imageName);//更新头像
									}
                                  if(XWDataCenter.headBeanMap==null){
									  XWDataCenter.headBeanMap=new HashMap<>();
								  }
									XWDataCenter.headBeanMap.put(friendName, imageName);
								}
							}
							/////备份好友数据
							BusProvider.getInstance().post(new BackUpEvent(1));
							//更新UI
							BusProvider.getInstance().post(new HeadListEvent(1));
						}

					}
                 XWDataCenter.xwDC.bHasHeadRun=false;
				}catch(Exception e){
					e.printStackTrace();
					XWDataCenter.xwDC.bHasHeadRun=false;
				}
			}
		});

	}




	
	/**
	 * 刷新头像
	 */
	public void refreshListIcon(){
		if(friendList.size()>0&&XWDataCenter.headBeanMap!=null){
			for(FriendNodeInfo node:friendList){
				String icon=XWDataCenter.headBeanMap.get(node.getLogin_name());
				if(icon!=null){
					node.setIcon(icon);
				}
			}
		}
	}
	
	/**
	 * 更新子界面
	 */
	public void updateControlChildView(){
		if(!bIsFront){////界面没显示，不刷新
			return;
		}
		int position=getCurIndex();
		Log.e(TAG,"updateControlChildView:"+position);
		try
		{
			//更新未读
			updateUnreadIcon();
            if(mPagerAdapter!=null){
				BaseLazyFragment fragment=(BaseLazyFragment)mPagerAdapter.getItem(position);
				if(fragment!=null){
					Log.e(TAG,"fragment status: visible"+fragment.isVisible());
					if(!fragment.isVisible()){
						if(expandIndex>-1){
							((ContactFragment)mPagerAdapter.getItem(1)).setDefExpandIndex(expandIndex);
						}
						fragment.postRefresh();
					}else{
						switch(position){
							case CALLRECORD_INDEX://通话记录
								BusProvider.getInstance().post(new FragmentRereshEvent(CALLRECORD_INDEX));
								break;
							case CONTACT_INDEX://好友列表
								BusProvider.getInstance().post(new FragmentRereshEvent(CONTACT_INDEX));
								break;
							case MSGLIST_INDEX://///聊天记录
							{
								BusProvider.getInstance().post(new FragmentRereshEvent(MSGLIST_INDEX));
							}
							break;
							case SYS_SETTING:
							{
								BusProvider.getInstance().post(new FragmentRereshEvent(SYS_SETTING));
							}
							break;
							default:
								break;
						}
					}

				}else{
					Log.e(TAG,"fragment ==null");
				}
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}




	/**
	 * 初始化控件
	 */
	private void initView(){
		iv_back2_video=(ImageView)findViewById(R.id.iv_back2_video);
		iv_back2_video.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/////返回视频界面
				if(xwDC.remoteVideoRunning){
					Intent nextPage=new Intent();
					nextPage.setClass(baseAct, FriendVideoDisplay.class);
					startActivity(nextPage);
				}
			}
		});

		this.mHandler = new FriendControlHandle(this);


		//初始化下部菜单
		initLayoutBottom();

		//初始化PopupWindow
		menuWindow=new MorePopupView(FriendControl.this, itemsOnClick);

		List<BaseLazyFragment> fragments=getPagerFragments();
		mPagerAdapter=new VPHomeAdapter(getSupportFragmentManager(), fragments);
		mViewPager.setEnableScroll(false);
		mViewPager.setOffscreenPageLimit(fragments.size());
		mViewPager.setAdapter(mPagerAdapter);
		int index=getCurIndex();
		Log.e(TAG,"onCreate pager index:"+index);

	}


	//为弹出窗口实现监听类
	private OnClickListener  itemsOnClick = new OnClickListener(){

		public void onClick(View v) {
			menuWindow.dismiss();
			Intent intent = new Intent();
			if(v.getId()==R.id.ll_add){
				//手机联系人
				intent.setClass(FriendControl.this, PhoneContactUI.class);
				startActivity(intent);
			}else if(v.getId()==R.id.ll_qrcode){
				//扫一扫
				intent.setClass(FriendControl.this, CaptureActivity.class);
				startActivity(intent);
			}
			else if(v.getId()==R.id.ll_searchfriend){
				managerAddFriend();
			}


		}
	};


	/**
	 * 初始化下部菜单栏
	 */
	private void initLayoutBottom(){
		/**下部菜单栏
		 * --------------------------主功能条----------------------------
		 */
		//
		imgsDefault=new int[]{R.drawable.ic_social_bright,R.drawable.ic_contacts_bright
				,R.drawable.ic_chats_bright,R.drawable.ic_setting_bright};
		imgsSelected=new int[]{R.drawable.ic_social_selected,R.drawable.ic_contacts_selected
				,R.drawable.ic_chats_selected,R.drawable.ic_setting_selected};
		//消息提示
		tx_tips =(TextView) findViewById(R.id.tx_main_chat_item_tips);
		//初始化按钮ID
		int[] frameItems =new int[]{R.id.fl_main_record,R.id.fl_main_contact,
				R.id.fl_main_chat,R.id.fl_main_setting};
		//图片ViewID
		int[] imgMainItems =new int[]{R.id.img_main_record_item,R.id.img_main_contact_item,
				R.id.img_main_chat_item,R.id.img_main_setting_item};
		//文字ViewID
		int[] txMainItems = new int[]{R.id.tx_main_record_item,R.id.tx_main_contact_item,
				R.id.tx_main_chat_item,R.id.tx_main_setting_item};

		//初始化按钮布局
		LinearLayout[] frames = new LinearLayout[MENU_ITEM_NUM];
		for(int i=0;i<frameItems.length;i++){
			Log.e(TAG,"frameItems :"+i);
			final int choice =i;
			frames[i]=(LinearLayout)findViewById(frameItems[i]);
			frames[i].setClickable(true);
			frames[i].setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Log.e(TAG,"onClick:"+v.getId());
					selectBottomItem(choice,true);
				}
			});
		}

		//初始化按钮视图
		imgs=new ImageView[MENU_ITEM_NUM];
		for(int i=0;i<imgMainItems.length;i++){
			imgs[i]=(ImageView)findViewById(imgMainItems[i]);
		}

		//初始化文字视图
		txs=new TextView[MENU_ITEM_NUM];
		for(int i=0;i<txMainItems.length;i++){
			txs[i]=(TextView)findViewById(txMainItems[i]);
		}

	}

	/**
	 * 重置底部菜单栏
	 */
	private void resetBottomAll(){
		if(imgs!=null&&txs!=null&&imgsDefault!=null){
			for(int i=0;i<imgs.length;i++){
				imgs[i].setImageResource(imgsDefault[i]);
				txs[i].setTextColor(getResources().getColor(R.color.main_item_bright));
			}

		}
	}

	///当前页面位置
	public int getCurIndex(){
		if(mViewPager!=null){
			return mViewPager.getCurrentItem();
		}
		return 0;
	}

	/**
	 * 选择一个底部菜单项
	 */
	private void selectBottomItem(int i,boolean updateUI){
		if(mViewPager!=null){
			if(updateUI&&getCurIndex()==i){
				Log.e("friendControl","is currentItem");
				return;
			}
			resetBottomAll();
			if(imgs!=null&&txs!=null){
				imgs[i].setImageResource(imgsSelected[i]);
				txs[i].setTextColor(getResources().getColor(R.color.main_item_selected));
			}
			mViewPager.setCurrentItem(i, false);
			if(updateUI)
				BusProvider.getInstance().post(new FragmentRereshEvent(i));
		}
		setUpTitleView(i);
	}





	private String ParseValue(String sverinfo, String sIndex) {
		int idx1, idx2, idx3;
		String sValue = "";
		idx1 = sverinfo.indexOf(sIndex);
		if (idx1 >= 0) {
			int idxTail = -1;
			sValue = sverinfo.substring(idx1 + sIndex.length(), sverinfo.length());

			idx2 = sValue.indexOf("\r");
			idx3 = sValue.indexOf("\n");

			if (idx2 >= 0) {
				if ((idx3 >= 0) && (idx3 < idx2))
					idxTail = idx3;
				else
					idxTail = idx2;
			} else {
				idxTail = idx3;
			}

			if (idxTail >= 0) {
				sValue = sValue.substring(0, idxTail);
			}

		}
		return sValue.trim();
	}
	/**
	 * 检测升级
	 */
	private void checkUpdate(){
		//////////////////////检查版本
		MainApplication.sVersionURL= ServerConfig.XIM_SERVER_HOST+"/download/"+ServerConfig.server_versiontxt;

		MainApplication.sSelfVerCode=String.valueOf(MainApplication.getVerCode(this));

		if (!TextUtils.isEmpty(MainApplication.sVersionURL)&&!TextUtils.isEmpty(MainApplication.sSelfVerCode)) {
			TaskExecutor.executeTask(new Runnable() {
				@Override
				public void run() {
					String sverinfo = NetTaskUtil.getDataTaskSync(MainApplication.sVersionURL);
					if (sverinfo != null) {
						MainApplication.sNewVerCode = ParseValue(sverinfo, "vercode=");
						MainApplication.sNewVerName = ParseValue(sverinfo, "vername=");
						MainApplication.sNewVerDate = ParseValue(sverinfo, "verdate=");
						MainApplication.sNewVerURL = ParseValue(sverinfo, "verurl=");
						if (!TextUtils.isEmpty(MainApplication.sNewVerCode) && !MainApplication.sSelfVerCode.equals(MainApplication.sNewVerCode)) {
							try {
								if ((Integer.valueOf(MainApplication.sNewVerCode)) > Integer.valueOf(MainApplication.sSelfVerCode)) {
									XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_13);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
		}
	}








	/**
	 * 更新底部栏未读信息图标
	 */
	public void updateUnreadIcon(){
		if(tx_tips==null){
			return;
		}

		if(XWDataCenter.chatHistoryDB!=null){
			int unreads=XWDataCenter.chatHistoryDB.getAllUnreads(XWDataCenter.getCurAccount());

			if(unreads>0){
				tx_tips.setVisibility(View.VISIBLE);
				tx_tips.setText(""+unreads);
			}else{
				tx_tips.setVisibility(View.GONE);
			}
		}

	}



	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");
        if(bIsInterrupt){
			return;
		}
		BusProvider.getInstance().register(this);



		////////////2016-10-11,检测后台死,如果长时间未操作,则退出应用
		if (
				(XWDataCenter.xwDC==null)||
				((XWServices.lLastDoCheck!=0)&&
				(
						(XWServices.bIsInCheck && (System.currentTimeMillis()-XWServices.lLastDoCheck>=120000))
				))
				)

			/////////////2012-11-06,完全退出程序
			{
				Log.e("friendcontrol","XWServices.bIsInCheck is too long");
				Intent startMain = new Intent(
						Intent.ACTION_MAIN);
				startMain.addCategory(Intent.CATEGORY_HOME);
				startMain
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(startMain);
				////////////Absolutely exit!!!!!!!2014-08-11
				TaskExecutor.executeTask(new Runnable() {
					@Override
					public void run() {
						com.example.mcryptolmsdimpl_demo.MainActivity.unMountSDCard();
						System.exit(1);
					}
				});
				System.exit(0);
				return;
		}
		////显示标题栏信息
		showUI();
    refreshSmallAvatar();
		////执行系统消息
		executeSystemMessage();
		/////更新当前界面
		updateControlChildView();
		///清理状态栏电话通知
		NotificationUtil.cleanNotificationByID(JRSConstants.NOTICE_CALL);
		////清理状态栏消息通知
		NotificationUtil.cleanAllMsgNotification();
		/////显示视频返回按钮
		updateVideoView();
	}


	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "onPause");
		if(bIsInterrupt){
			return;
		}
		BusProvider.getInstance().unregister(this);
	}

	public void updateVideoView(){
            if(xwDC.netPhoneTime==0){  /////通话时间为0说明已经停止
				iv_back2_video.setVisibility(View.GONE);
				return;
			}
			if(xwDC.remoteVideoRunning&&(!TextUtils.isEmpty(xwDC.sCurrentPhoneNumber))&&xwDC.friendHungup==0){
				iv_back2_video.setVisibility(View.VISIBLE);
			}else{
				iv_back2_video.setVisibility(View.GONE);
			}
	}

	@Override
	protected void onStop() {
		Log.e(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,resultCode,data);
		if (resultCode == RESULT_OK) {
			if (requestCode == JRSConstants.REQ_IGNORE_BATTERY_CODE){
				//TODO something
			}
		}else if (resultCode == RESULT_CANCELED){
			if (requestCode == JRSConstants.REQ_IGNORE_BATTERY_CODE){
				showToastTips(XWCodeTrans.doTrans("请开启忽略电池优化~"));
			}
		}
	}

	/**
	 * 界面显示
	 */
	public void showUI(){

		//////////2018-07-08，更新图标!!!!
		refreshSmallAvatar();

		if (XWDataCenter.xwDC.iServerConnectStatus==0)// 提示正在登陆   改为标题栏显示
		{
			if(tvOLStatus!=null){
				tvOLStatus.setText(xechwic.android.XWCodeTrans.doTrans("登陆中"));
			}
		}else{
			if(tvOLStatus!=null){
				FriendNodeInfo fni=FriendNodeDB.getAFriend(XWDataCenter.getCurAccount(),XWDataCenter.getCurAccount());
				if ( (fni!=null) && (fni.getOnline_status()!=null) )
				{

					if (XWCodeTrans.doTrans("断开").equals(fni.getOnline_status())){
						tvOLStatus.setTextColor(getResources().getColor(color.tx_gray_color));
					}else{
						tvOLStatus.setTextColor(getResources().getColor(color.white));
					}
					tvOLStatus.setText(fni.getOnline_status());
				}
				else
					tvOLStatus.setText(getResources().getString(R.string.alert_connected));
			}
		}

		/////////////////////显示消息
		{
			///////////////////////显示文字消息,存在xwDC的队列中,现在重发。
			while (xwDC.listGotMsg.size()>0)
			{
				Message msg=(Message)xwDC.listGotMsg.get(0);

				Message msg2=mHandler.obtainMessage(msg.what, msg.obj);
				mHandler.sendMessage(msg2);
				xwDC.listGotMsg.remove(0);
			}
		}		

	}




	@Override
	protected void onDestroy() {
		Log.e(TAG,"onDestroy");
		super.onDestroy();


	}

	




	/**
	 * 备份数据
	 */
	boolean bBackupRunning=false;
	public void backupData(){
		if(!bBackupRunning){
			Log.e(TAG,"backupData");
			bBackupRunning=true;
			TaskExecutor.executeTask(new Runnable() {
				@Override
				public void run() {
					FriendNodeDB.backupFriends();
					bBackupRunning=false;
				}
			});
		}

	}
	

	
	/**
	 * 刷新有头像的相关界面
	 */
	public void repaintIconUI(){

		refreshListIcon();
		int position=mViewPager.getCurrentItem();
		if(position==CONTACT_INDEX){
			BusProvider.getInstance().post(new FragmentRereshEvent(CONTACT_INDEX));
		}else if(position==MSGLIST_INDEX){
			BusProvider.getInstance().post(new FragmentRereshEvent(MSGLIST_INDEX));
		}

		//////////2018-07-08，更新图标!!!!
		refreshSmallAvatar();

	}

	public void repaintContactUI(){
		if(bIsFront) {
			int position = mViewPager.getCurrentItem();
			if (position == CONTACT_INDEX) {
				BusProvider.getInstance().post(new FragmentRereshEvent(CONTACT_INDEX));
			}
		}
	}

	public void repaintFriendControl() {//改为刷新
		Log.e(TAG,"repaintFriendControl");
		if(!bIsFront){
			return;
		}
//		if(getShowPosition()!=1){
//			return;
//		}
//		updateFriendListView();
	}

	private void updateFriendListView(){
		//刷新组名
		refreshGroupName(xwDC.groupsInfo);
//		//刷新分组
//		getGroupData(friendList);
//		if(mAdapter!=null){
//			mAdapter.notifyDataSetChanged();
//		}
	}
	
	
	/**刷新组名列表
	 */
	public void refreshGroupName(List<FriendGroupInfo> groupsInfo){
		Log.e(TAG,"refreshGroupName groupsInfo");
		if(groupsInfo==null||groupNameList==null){
			return;
		}
		groupNameList.clear();
		String name = null;
		for(FriendGroupInfo info:groupsInfo){
			name = info.groupName;
			if(name!=null){
				Log.e(TAG,"groupNameList add:"+name);
				groupNameList.add(name);
			}

		}

	}


	

	
	/**刷新组名列表
	 * @param name
	 */
	public void refreshGroupName(String name){
		Log.e(TAG,"refreshGroupName name");
		if(name==null||groupNameList==null){
			return;
		}
		boolean isExist=false;

		for(String str:groupNameList){
			if(name.equals(str)){
				isExist=true;
				break;
			}
		}

		if(!isExist){
			groupNameList.add(name);

		}

	}

	/**
	 * 进行聊天
	 */
	public static void directToChatRecord(FriendNodeInfo fni) {
		try
		{

			if((fni==null)||(fni.getLogin_name()==null)/*||XWDataCenter.fni==null*/){
				return;
			}
			if (fni.getLogin_name().equals(XWDataCenter.getCurAccount())) {
				return;
			}


			/* 以下是只处理普通消息 */
			Intent nextPage = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("friendAccount", fni.getLogin_name());
			bundle.putString("friendImage", fni.getIcon());//添加传递好友头像
			bundle.putString("myImage", XWDataCenter.headBeanMap.get(XWDataCenter.getCurAccount()));
			nextPage.putExtras(bundle);
			nextPage.setClass(MainApplication.getInstance(), FriendChatRecord.class);
			nextPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MainApplication.getInstance().startActivity(nextPage);


		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {

	}

	//////重写备份
	@Override
	protected void doSaveInstanceState(Bundle outState) {
		super.doSaveInstanceState(outState);
		////保存当前页面index
		outState.putInt(JRSConstants.SAVE_INDEX,getCurIndex());
		////保存好友列表的展开状态
		ContactFragment fragment=(ContactFragment)mPagerAdapter.getItem(1);
		int index=fragment.getExpandIndex();
		outState.putInt(JRSConstants.EXPAND_INDEX,index);

		/////备份好友数据
		BusProvider.getInstance().post(new BackUpEvent(1));
	}

	@Override
	public void onBackPressed() {
		gotoHomeActivity();
	}

	public Handler getmHandler() {
		return mHandler;
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}



	/**
	 * 关闭
	 */
	public void closeFriendListView(){
		if(this.builderDg!=null){
			builderDg.dismiss();
		}
	}

	/**
	 * 好友管理
	 */
	public void managerFriends(){
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		final LinearLayout buildLayout = new LinearLayout(this);
		final Spinner spin = new Spinner(this);
		final Builder builder = new AlertDialog.Builder(this);

		if (this.csFn == null) {
			builder.setTitle(getResources().getString(
					R.string.alert_select_error));
		} else {
			builder.setTitle(getResources().getString(
					R.string.alert_select_friend)
					+ this.csFn.getSignName());
		}

		final EditText editName = new EditText(this);
		final EditText editSignName = new EditText(this);
		/////final EditText editNum = new EditText(this);
		final EditText editEmail = new EditText(this);

		final TextView labelName = new TextView(this);
		final TextView labelSignName = new TextView(this);
		/////final TextView labelNum = new TextView(this);
		final TextView labelEmail = new TextView(this);

		labelName.setText(getResources().getString(R.string.alert_number));
		labelSignName.setText(getResources().getString(
				R.string.alert_remarks));
		//labelNum.setText(getResources().getString(
		//		R.string.alert_short_number));
		labelEmail.setText(getResources().getString(R.string.alert_email));
		labelName.setTextColor(Color.WHITE);
		labelSignName.setTextColor(Color.WHITE);
		///labelNum.setTextColor(Color.WHITE);
		labelEmail.setTextColor(Color.WHITE);

		final LinearLayout layoutName = new LinearLayout(this);
		final LinearLayout layoutSignName = new LinearLayout(this);
		final LinearLayout layoutNum = new LinearLayout(this);
		final LinearLayout layoutEmail = new LinearLayout(this);

		layoutName.setOrientation(LinearLayout.HORIZONTAL);
		layoutSignName.setOrientation(LinearLayout.HORIZONTAL);
		layoutNum.setOrientation(LinearLayout.HORIZONTAL);
		layoutEmail.setOrientation(LinearLayout.HORIZONTAL);


		LayoutParams layoutParams2 = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		labelName.setLayoutParams(layoutParams2);
		labelSignName.setLayoutParams(layoutParams2);
		////labelNum.setLayoutParams(layoutParams2);
		labelEmail.setLayoutParams(layoutParams2);

		editName.setLayoutParams(layoutParams);
		editSignName.setLayoutParams(layoutParams);
		///editNum.setLayoutParams(layoutParams);
		editEmail.setLayoutParams(layoutParams);

		layoutName.addView(labelName);
		layoutName.addView(editName);
		layoutSignName.addView(labelSignName);
		layoutSignName.addView(editSignName);
		//layoutNum.addView(labelNum);
		///layoutNum.addView(editNum);
		layoutEmail.addView(labelEmail);
		layoutEmail.addView(editEmail);

		final LinearLayout addFriendLayout = new LinearLayout(this);
		addFriendLayout.setLayoutParams(layoutParams);
		addFriendLayout.setOrientation(LinearLayout.VERTICAL);
		addFriendLayout.addView(layoutName);
		addFriendLayout.addView(layoutSignName);
		addFriendLayout.addView(layoutNum);
		addFriendLayout.addView(layoutEmail);
		addFriendLayout.setVisibility(View.GONE);

		final Spinner queryTypeSpin = new Spinner(this);
		ArrayList<String> typeList = new ArrayList<String>();
		typeList.add(getResources().getString(
				R.string.alert_find_onlinefriend));
		typeList.add(getResources().getString(
				R.string.alert_find_conditionfriend));
		queryTypeSpin
		.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> adpView,
					View view, int id, long position) {
				if (id == 1) {
					queryTypeSpin.setVisibility(View.GONE);
					addFriendLayout.setVisibility(View.VISIBLE);
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		ArrayAdapter<String> type_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, typeList);
		type_adapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		queryTypeSpin.setAdapter(type_adapter);
		queryTypeSpin.setLayoutParams(layoutParams);
		queryTypeSpin.setVisibility(View.GONE);
		/***************** 更换组 begin *********************/
		final Spinner updateGroupSpin = new Spinner(this);
		ArrayList<String> groupList = new ArrayList<String>();
		for (int i = 0; i < xwDC.groupsInfo.size(); i++) {
			FriendGroupInfo fgi = (FriendGroupInfo) xwDC.groupsInfo.get(i);
			groupList.add(fgi.getGroupName());
			// //Log.e("tag", fg.getGroup_name());
		}
		updateGroupSpin
		.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> adpView,
					View view, int id, long position) {
				String selected = adpView.getItemAtPosition(id)
						.toString();
				optionSelect = selected;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		ArrayAdapter<String> group_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, groupList);
		group_adapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		updateGroupSpin.setAdapter(group_adapter);
		updateGroupSpin.setLayoutParams(layoutParams);
		updateGroupSpin.setVisibility(View.GONE);
		/***************** 更换组 end *********************/


		/***************** 修改备注名 begin *********************/
		final LinearLayout remarkLayout = new LinearLayout(this);
		remarkLayout.setLayoutParams(layoutParams);
		final EditText remarkEdit = new EditText(this);
		remarkEdit.setLayoutParams(layoutParams);
		remarkLayout.addView(remarkEdit);
		remarkLayout.setVisibility(View.GONE);
		/***************** 修改备注名 end ***********************/
		final TextView alertView = new TextView(this);
		alertView.setLayoutParams(layoutParams2);
		// alertView.setText("");
		alertView.setVisibility(View.GONE);
		alertView.setTextSize(15);
		ArrayList<String> fmOptionList = new ArrayList<String>();
		fmOptionList.add(getResources().getString(
				R.string.alert_please_select));
		fmOptionList.add(getResources().getString(
				R.string.alert_change_group));
		fmOptionList.add(getResources().getString(
				R.string.alert_update_remark));
		fmOptionList.add(getResources().getString(
				R.string.alert_addition_friend));
		fmOptionList.add(getResources().getString(
				R.string.alert_delete_friend));
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> adpView, View view,
					int id, long position) {
				// String selected=adpView.getItemAtPosition(id).toString();
				switch (id) {
				case 1:
					if (csFn != null) {
						spin.setVisibility(View.GONE);
						updateGroupSpin.setVisibility(View.VISIBLE);
					} else {
						spin.setVisibility(View.GONE);
						alertView.setText(getResources().getString(
								R.string.alert_must_select));
						alertView.setTextColor(Color.RED);
						alertView.setVisibility(View.VISIBLE);
					}
					break;
				case 2:
					if (csFn != null) {
						spin.setVisibility(View.GONE);
						remarkLayout.setVisibility(View.VISIBLE);
					} else {
						spin.setVisibility(View.GONE);
						alertView.setText(getResources().getString(
								R.string.alert_must_select));
						alertView.setTextColor(Color.RED);
						alertView.setVisibility(View.VISIBLE);
					}
					break;
				case 3:
					spin.setVisibility(View.GONE);
					queryTypeSpin.setVisibility(View.VISIBLE);
					break;
				case 4:
					break;
				default:
					break;
				}
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		ArrayAdapter<String> fm_adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, fmOptionList);
		fm_adapter
		.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(fm_adapter);
		spin.setLayoutParams(layoutParams);
		buildLayout.setOrientation(LinearLayout.VERTICAL);
		buildLayout.setLayoutParams(layoutParams);



		//		friendListView.seta

		buildLayout.addView(addFriendLayout);// 好友布局
		buildLayout.addView(queryTypeSpin);// 添加好友操作选项
		buildLayout.addView(spin);// 好友操作选项
		buildLayout.addView(updateGroupSpin);// 更换分组控件
		buildLayout.addView(remarkLayout);// 修改备注名
		buildLayout.addView(alertView);// 提示
		builder.setView(buildLayout);
		// 对话框,确定或取消按钮处理
		builder.setPositiveButton(
				getResources().getString(R.string.alert_confirm),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						if (View.VISIBLE == addFriendLayout.getVisibility()) {// 如果是按条件查找
							String number =editName.getText().toString();
							String signName = editSignName.getText().toString();
							String email=editEmail.getText().toString();

							queryFriendByNum(number, signName, email);
						} else if (View.VISIBLE == queryTypeSpin
								.getVisibility()) {                    //查找添加好友
							Intent nextPage = new Intent();
							nextPage.setClass(FriendControl.this,
									FriendQuery.class);
							startActivity(nextPage);
							xwDC.queryOnlineFriend();

						} else if (View.VISIBLE == updateGroupSpin
								.getVisibility()) {// 如果是更换组
							try {
								Log.e(TAG,"updateGroup:id"+csFn.getId()+",login_name:"+csFn.getLogin_name());
								int ret = xwDC.manageFN(
										"4".getBytes("GBK"), csFn
										.getId(), (csFn
												.getLogin_name() + "\0")
												.getBytes("GBK"),
												(  xechwic.android.XWCodeTrans.doTransInput (optionSelect) + "\0")
												.getBytes("GBK"), "\0"
												.getBytes("GBK"));
								if (ret == 0) {
									csFn.setGroupName(optionSelect);
									updateFriendGroupName(csFn);
                                    repaintFriendControl();								
									if(xwDC!=null){
										refreshGroupName(xwDC.groupsInfo);
									}
                                    
							
									closeFriendListView();
								}
								// csFn=null;
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (View.VISIBLE == remarkLayout
								.getVisibility()) {// 修改备注名
							try {
								String signName = remarkEdit.getText()
										.toString();
								// int
								// ret=updateFNInfo(csFn.fni.getId(),(signName+"\0").getBytes("GBK"),"\0".getBytes("GBK"),"\0".getBytes("GBK"),"2".getBytes("GBK"));
								int ret = xwDC.remarkFNSignName(csFn
										.getId(),
										(csFn.getSignName() + "\0")
										.getBytes("GBK"),
										(signName + "\0").getBytes("GBK"),
										(csFn.getLogin_name() + "\0")
										.getBytes("GBK"));
								if (ret == 0) {
									csFn.setSignName(signName);
									updateFriendSign(csFn);
									repaintFriendControl();
									closeFriendListView();
								}
								// csFn=null;
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {// 删除好友
							try {
								if (csFn != null) {
									new AlertDialog.Builder(FriendControl.this).setTitle(xechwic.android.XWCodeTrans.doTrans("确定要删除")+"?") 
									.setMessage(csFn.getLogin_name() + ( (csFn.getSignName()!=null)&&(!csFn.getSignName().equals(csFn.getLogin_name())) ? ("("+csFn.getSignName()+")")  : "" ))
									.setIcon(android.R.drawable.ic_dialog_info) 
									.setPositiveButton(xechwic.android.XWCodeTrans.doTrans("确定"), new DialogInterface.OnClickListener() { 

										@Override 
										public void onClick(DialogInterface dialog, int which) { 

											try
											{
												int ret = XWDataCenter.xwDC.manageFN("5"
														.getBytes("GBK"), csFn
														.getId(), (csFn
																.getLogin_name() + "\0")
																.getBytes("GBK"), "\0"
																.getBytes("GBK"), "\0"
																.getBytes("GBK"));
												if (ret == 0) {
													XWDataCenter.deleteFriend(csFn);
													updateFriendListView();
													closeFriendListView();
												}
											}
											catch(Exception ex)
											{
												ex.printStackTrace();
											}

										} 
									}) 
									.setNegativeButton(xechwic.android.XWCodeTrans.doTrans("取消"), new DialogInterface.OnClickListener() { 

										@Override 
										public void onClick(DialogInterface dialog, int which) { 
											// 点击“返回”后的操作,这里不设置没有任何操作 
										} 
									}).show(); 

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						dialog.dismiss();
					}
				});
		builder.setNeutralButton(
				getResources().getString(R.string.alert_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {

						dialog.dismiss();
					}
				});
		try
		{
			builder.show();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 好友管理
	 */
	public void managerAddFriend(){
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		final LinearLayout buildLayout = new LinearLayout(this);
		final Spinner spin = new Spinner(this);
		final Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(getResources().getString(
				R.string.friend_query));


		final EditText editName = new EditText(this);
		editName.setTextColor(getResources().getColor(R.color.black));
		final EditText editSignName = new EditText(this);
		editSignName.setTextColor(getResources().getColor(color.black));
		final EditText editEmail = new EditText(this);
		editEmail.setTextColor(getResources().getColor(color.black));

		final TextView labelName = new TextView(this);
		labelName.setTextColor(getResources().getColor(color.black));
		final TextView labelSignName = new TextView(this);
		labelSignName.setTextColor(getResources().getColor(color.black));
		final TextView labelEmail = new TextView(this);
		labelEmail.setTextColor(getResources().getColor(color.black));

		labelName.setText(getResources().getString(R.string.alert_number));
		labelSignName.setText(getResources().getString(R.string.alert_remarks));
		labelEmail.setText(getResources().getString(R.string.alert_email));

		final LinearLayout layoutName = new LinearLayout(this);
		final LinearLayout layoutSignName = new LinearLayout(this);
		final LinearLayout layoutNum = new LinearLayout(this);
		final LinearLayout layoutEmail = new LinearLayout(this);

		layoutName.setOrientation(LinearLayout.HORIZONTAL);
		layoutSignName.setOrientation(LinearLayout.HORIZONTAL);
		layoutNum.setOrientation(LinearLayout.HORIZONTAL);
		layoutEmail.setOrientation(LinearLayout.HORIZONTAL);

		layoutName.setBackgroundColor(getResources().getColor(color.white));
		layoutSignName.setBackgroundColor(getResources().getColor(color.white));
		layoutNum.setBackgroundColor(getResources().getColor(color.white));
		layoutEmail.setBackgroundColor(getResources().getColor(color.white));

		LayoutParams layoutParams2 = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		labelName.setLayoutParams(layoutParams2);
		labelSignName.setLayoutParams(layoutParams2);
		labelEmail.setLayoutParams(layoutParams2);

		editName.setLayoutParams(layoutParams);
		editSignName.setLayoutParams(layoutParams);
		editEmail.setLayoutParams(layoutParams);

		layoutName.addView(labelName);
		layoutName.addView(editName);
		layoutSignName.addView(labelSignName);
		layoutSignName.addView(editSignName);
		layoutEmail.addView(labelEmail);
		layoutEmail.addView(editEmail);

		final LinearLayout addFriendLayout = new LinearLayout(this);
		addFriendLayout.setLayoutParams(layoutParams);
		addFriendLayout.setOrientation(LinearLayout.VERTICAL);
		addFriendLayout.addView(layoutName);
		addFriendLayout.addView(layoutSignName);
		addFriendLayout.addView(layoutNum);
		addFriendLayout.addView(layoutEmail);
		addFriendLayout.setVisibility(View.VISIBLE);
		buildLayout.addView(addFriendLayout);// 好友布局
		builder.setView(buildLayout);
		// 对话框,确定或取消按钮处理
		builder.setPositiveButton(
				getResources().getString(R.string.alert_confirm),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						String number =editName.getText().toString();
						String signName = editSignName.getText().toString();
						String email=editEmail.getText().toString();
						if ((number.length()>0)||(signName.length()>0)||(email.length()>0))
						{// 如果是按条件查找
							queryFriendByNum(number, signName, email);
						} 
						else
						{
							xwDC.queryOnlineFriend();
							queryFriendByNum("", "", "");
						}
						dialog.dismiss();
					}
				});
		builder.setNeutralButton(
				getResources().getString(R.string.alert_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {

						dialog.dismiss();
					}
				});
		try
		{
			builder.show();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}



	/**根据条件查找好友
	 */
	public static void queryFriendByNum(String number,String signame,String email){
		if(number==null||signame==null||email==null){
			return;
		}

		Log.v("XIM","queryFriendByNum "+number+" "+ signame +" "+email);	
		Intent nextPage = new Intent(); 
		nextPage.setClass(MainApplication.getInstance(),
				FriendQuery.class);
		nextPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainApplication.getInstance().startActivity(nextPage);
		try {

			int ret = XWDataCenter.xwDC
					.queryFriendForCondition(
							(number + "\0")
							.getBytes("GBK"),
							(signame + "\0")
							.getBytes("GBK"),
							0, (email + "\0")
							.getBytes("GBK"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**更新好友组名
	 */
	public static void updateFriendGroupName(FriendNodeInfo node){
		if(node==null||friendList==null||friendList.isEmpty()){
			return;
		}
		for(FriendNodeInfo info:friendList){
			if(info.getLogin_name().equals(node.getLogin_name())){
				info.setGroupName(node.getGroupName());
				break;
			}
		}
	}


	/**更新好友备注名称
	 */
	public static void updateFriendSign(FriendNodeInfo node){
		if(node==null||friendList==null||friendList.isEmpty()){
			return;
		}
		for(FriendNodeInfo info:friendList){
			if(info.getLogin_name().equals(node.getLogin_name())){
				info.setSignName(node.getSignName());
				break;
			}
		}
	}




	/**打开好友详细资料
	 * 
	 */
	public static void openFriendDetail(String  name){
		Intent nextPage = new Intent();
		Bundle bundlenew = new Bundle();
		bundlenew.putString("friendAccount", name);
		nextPage.putExtras(bundlenew);
		nextPage.setClass(MainApplication.getInstance(),
				FriendDetailUI.class);
		nextPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MainApplication.getInstance().startActivity(nextPage);
	}

	@Subscribe
	public void onChatMsgEvent(ChatMsgEvent event){
		if(bIsFront){
			Log.e(TAG,"onChatMsgEvent");
			//更新消息提醒
			updateUnreadIcon();
			//更新消息列表
			int position=mViewPager.getCurrentItem();
			if(position==MSGLIST_INDEX){
				BusProvider.getInstance().post(new FragmentRereshEvent(MSGLIST_INDEX));
			}

		}
	}

	@Subscribe
	public void onGroupUpdateEvent(GroupUpdateEvent event){
		if(xwDC!=null){
			//刷新组名
			refreshGroupName(xwDC.groupsInfo);
			//刷新分组
//			getGroupData(friendList);
//			if(mAdapter!=null){
//				mAdapter.notifyDataSetChanged();
//			}
		}

	}


	/////只刷新头像相关界面
	Runnable updateAvatarRunnable =new Runnable() {
		@Override
		public void run() {
			if(bIsFront){
				int index=getCurIndex();
				if(index==CONTACT_INDEX||index==MSGLIST_INDEX){
					Log.e(TAG,"updateAvatarRunnable run");
					updateControlChildView();
				}
			}

		}
	};

	@Subscribe
	public void onAvatarUpdateEvent(AvatarUpdateEvent event){
			mHandler.removeCallbacks(updateAvatarRunnable);
			mHandler.postDelayed(updateAvatarRunnable,1000);//延时刷新整个界面
	}



	public List<BaseLazyFragment> getPagerFragments() {
		List<BaseLazyFragment> fragments = new ArrayList<BaseLazyFragment>() {{
			add(new CallRecordFragment());
			add(new ContactFragment());
			add(new MsgListFragment());
			add(new SettingFragment());
		}};
		return fragments;
	}

	@Subscribe
	public void onLogoutEvent(LogoutXWIMEvent event){
		if(event!=null){
			if(event.type==1){
				logout_XWIM();
			}else if(event.type==2){
				destroyAccount();
			}
		}
	}



	@Subscribe
	public void onHeadListEvent(HeadListEvent event){
		if(event!=null){
			if(event.type==1){
	                if(bIsFront) {
						repaintIconUI();
					}
				}
		}
	}

	@Subscribe
	public void onBackUpEvent(BackUpEvent event){
		if(event!=null){
			if(event.type==1){
				backupData();
			}
		}
	}
}