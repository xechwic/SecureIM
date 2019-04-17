package xechwic.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Response;
import xechwic.android.FriendCall;
import xechwic.android.FriendChatRecord;
import xechwic.android.FriendControl;
import xechwic.android.FriendNodeInfo;
import xechwic.android.XWCodeTrans;
import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.act.ServerConfig;
import xechwic.android.bean.BeanOperate;
import xechwic.android.bean.HeadBean;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.util.Http;
import ydx.securephone.R;


public class FriendDetailUI extends BaseUI implements OnClickListener {

	private String TAG=FriendDetailUI.class.getSimpleName();
	public XWDataCenter xwDC;     

	private  ImageView btn_back;         //返回
	private ImageView img_head;    //头像
	private TextView tv_nickname;           //昵称
	private TextView tv_account;   //账号
	private RelativeLayout btn_detail_chat;//消息聊天按钮
	private RelativeLayout btn_video_chat;//视频聊天

	FriendNodeInfo fni;//账号信息

	public static final int MSG_UPDATE_DETAIL=0x11003;//更新资料
	private String  friendAccount;//好友账号



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_friend_detail);
		ButterKnife.bind(this);
		//获取xwdc并将xwdc的context指向本例
		this.xwDC = ((MainApplication) this.getApplication()).getDC(this);
		XWDataCenter.xwContext = this;

		//初始控件
		initView();

		//初始化资料
		initData();


	}


	/**获取好友id
	 * @return
	 */
	private String  getFriendAccount(){
		String account = null;
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if(bundle!=null){
			account = bundle.getString("friendAccount");
		}
		return account;
	}
	/**
	 * 初始资料
	 */
	private void initData(){
		friendAccount=getFriendAccount();
		if(TextUtils.isEmpty(friendAccount)){
			/////数据丢失
			baseAct.finish();
			if(xwDC.activityList.size()==0){
				startActivity(new Intent(baseAct, FriendControl.class));
			}
		}else{
			setDetail();
			if(XWDataCenter.headBeanMap.isEmpty()){
				//在线更新
				getDetailTask(XWDataCenter.headBeanMap, friendAccount);
			}

		}


	}


	/**
	 * 设置详情
	 */
	private void setDetail(){
		if(XWDataCenter.xwDC!=null){
			fni =XWDataCenter.getFriendDB().getAFriend(XWDataCenter.xwDC.loginName,friendAccount);

			if(fni!=null){
                setHeadPic(FriendNodeDB.getFriendHead(fni));

				//设置昵称
				setNickName(fni.getSignName());
				//设置账号
				setAccount(fni.getLogin_name());
			}
		}
	}

	/**
	 * 设置账号
	 * @param str
	 */
	private void setAccount(String str){
		if(str!=null){
			tv_account.setText(str);
		}
	}





	/**设置昵称
	 * @param name
	 */
	private void setNickName(String name){
		if(name!=null){
			tv_nickname.setText(name);
		}
	}

	/**
	 * 初始化控件
	 */
	private void initView(){

		btn_video_chat=(RelativeLayout)findViewById(R.id.btn_detail_video_chat);
		btn_video_chat.setOnClickListener(this);


		btn_detail_chat=(RelativeLayout)findViewById(R.id.btn_detail_chat);
		btn_detail_chat.setOnClickListener(this);


		btn_back=(ImageView) findViewById(R.id.iv_back);
		btn_back.setOnClickListener(this);

		img_head=(ImageView)findViewById(R.id.iv_avatar);

		tv_nickname=(TextView)findViewById(R.id.nickname);



		tv_account=(TextView)findViewById(R.id.account);

	}




	/**
	 * 关闭UI
	 */
	private void closeUI(){
		baseAct.finish();
	}



	@Override
	public void onBackPressed() {
		closeUI();
	}



	/**进入拨号界面
	 * @param node
	 */
	private void callFriend(FriendNodeInfo node){
		if(node==null){
			return;
		}
	    
		Intent intent = new
				Intent(getApplicationContext(),FriendCall.class);
		String number = node.getLogin_name(); // null
		if(node.getLogin_name().equals(XWDataCenter.xwDC.loginName)){
			showToastTips(XWCodeTrans.doTrans("不能拨打本号码"));
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putString("phone_number", number);
		bundle.putString("tag", ""+1);	
		intent.putExtras(bundle);
		Log.e(TAG,"phone_number:"+number+",tag:"+1);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_detail_chat://信息聊天
			if(fni!=null){
				FriendDetailUI.this.finish();
				directToChatRecord(fni);

			}

			break;
		case R.id.btn_detail_video_chat://语音视频聊天
			if(fni!=null){
				boolean bOK=makeCallCheck(fni);
				if(bOK) {
					FriendDetailUI.this.finish();
					callFriend(fni);
				}
			}

			break;
		case R.id.iv_back:

			closeUI();

			break;
		case R.id.qrcode_rl:
			//二维码
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), MyQrcode.class);
			startActivity(intent);
			break;



		default:
			break;
		}	



	}


	/**设置头像
	 */
	private void setHeadPic(String name){
		if(name!=null){
			Log.e(TAG,"setHeadPic："+name);
			if(name.contains("+")){
				try {
					name=URLEncoder.encode(name, "gbk");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		Glide.with(MainApplication.getInstance())
				.load(Http.getHeadPicUrl()+name)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.error(R.drawable.icon)
				.into(img_head);
	}


	/**
	 * UI处理
	 */
	public Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(bIsDestroy){
				return;//界面关闭不进行处理
			}

			switch(msg.what){
			case MSG_UPDATE_DETAIL:
				Log.e(TAG,"getDetailTask success");
				setDetail();
				break;
			}
		}

	};




	/**获取本账号个人资料
	 */
	public void getDetailTask(final Map<String,String> beanList,final String name){
		if(beanList==null||name==null){
			return;
		}
		try {
			// http地址
			String httpUrl = Http.getAHeadUrl() + "?user_id=" + URLEncoder.encode(name, "gbk");

			httpUrl=httpUrl.replace("http://","https://");
			httpUrl=httpUrl.replace(":"+ ServerConfig.XIM_SERVER_PORT,"");


			OkGo.getInstance().get(httpUrl)
					.setCertificates()
					.tag(this)//
					.execute(new AbsCallback<Object>() {
						@Override
						public Object convertSuccess(Response response) throws Exception {
							return null;
						}

						@Override
						public void onSuccess(Object o, Call call, Response response) {
							Log.e("okgo","onSuccess");
							try{
								if(response!=null&&response.body()!=null){
									String result= new String(response.body().bytes(),"gbk");
									Log.e("okgo","result:"+result);
									if(!TextUtils.isEmpty(result)){
										List<HeadBean> list=BeanOperate.getHeadBeanList(result);
										if(list!=null&&!list.isEmpty()){
											for(HeadBean bean:list){
												String friendName=bean.getFriend_name();
												String imageName=bean.getImage_name();


												friendName=XWDataCenter.XWdecodeurl(friendName, "gbk");
												imageName=XWDataCenter.XWdecodeurl(imageName, "GBK");

												FriendNodeInfo node  = new FriendNodeInfo();
												if(friendName!=null){
													node.setLogin_name(friendName);
												}
												if(imageName!=null){
													node.setIcon(imageName);
													beanList.put(friendName, imageName);
												}

												if(bean.getMessage()!=null){
													String message = bean.getMessage();
													try
													{
														message=XWDataCenter.XWdecodeurl(message, "gbk");
													}
													catch(Exception ex)
													{
														ex.printStackTrace();
													}
													node.setIntroduction(message);
												}
												saveNode(node);
												mHandler.sendEmptyMessage(MSG_UPDATE_DETAIL);

											}

										}


									}
								}
							}catch (Exception e){
								e.printStackTrace();
							}

						}

						@Override
						public void onError(Call call, Response response, Exception e) {
							super.onError(call, response, e);
							Log.e("okgo","onError");
						}
					});
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		//Activity销毁时，取消网络请求
		OkGo.getInstance().cancelTag(this);
	}


	/**
	 * 进行聊天
	 */
	public void directToChatRecord(FriendNodeInfo fni) {
		if(fni==null){
			return;
		}
		if (fni.getLogin_name().equals(XWDataCenter.xwDC.loginName)) {
			showToastTips(getResources().getString(
					R.string.alert_self_conn_error));
			return;
		}

		String friend_icon =XWDataCenter.headBeanMap.get(fni.getLogin_name());
		String my_icon=XWDataCenter.headBeanMap.get(XWDataCenter.xwDC.loginName);
		/* 以下是只处理普通消息 */
		Intent nextPage = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("friendAccount", fni.getLogin_name());
		bundle.putString("friendImage", friend_icon);//添加传递好友头像
		bundle.putString("myImage", my_icon);
		nextPage.putExtras(bundle);
		nextPage.setClass(FriendDetailUI.this, FriendChatRecord.class);
		startActivity(nextPage);
	}

	/**保存本用户资料
	 */
	public void saveNode(FriendNodeInfo node){
		if(node==null||node.getLogin_name().length()<1/*||XWDataCenter.fni==null*/){
			return;
		}
		FriendNodeInfo nodeInfo=XWDataCenter.getFriendDB().getAFriend(XWDataCenter.getCurAccount(),node.getLogin_name());
		if(nodeInfo!=null){
			node.setOnline_type(-1);//表示不更新在线状态
			XWDataCenter.getFriendDB().updateFriendNode(node,XWDataCenter.getCurAccount());
		}

	}

	@Override
	public void finish() {
		super.finish();
		MainApplication.getInstance().clearContext(this);
	}


}