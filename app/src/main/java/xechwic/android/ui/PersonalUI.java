package xechwic.android.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.lzy.okgo.OkGo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Response;
import xechwic.android.FriendNodeInfo;
import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.act.ServerConfig;
import xechwic.android.bean.BeanOperate;
import xechwic.android.bean.HeadBean;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.util.Http;
import xechwic.android.util.NetTaskUtil;
import xechwic.android.util.TaskExecutor;
import xechwic.android.util.glide.GlideImageLoader;
import ydx.securephone.R;


public class PersonalUI extends BaseUI implements OnClickListener {

	private String TAG=PersonalUI.class.getSimpleName();
	private RelativeLayout user_pic ;
	private  ImageView btn_back;         //返回
	private ImageView img_head;    //头像
	private TextView tv_nickname;           //昵称
	private String   signatureStr;       //个性签名字符串

	private TextView tv_account;   //账号


	FriendNodeInfo fni;//账号信息
	private RelativeLayout qrcode_rl;//二维码

	private  final int SELECT_IMAGE=100;//选择一张头像

	private final int MSG_HEAD_UPDATE=0x11001;//上传头像
	private final int MSG_UPDATE_DETAIL=0x11003;//更新资料
	private boolean isModify=false;//修改标识
	private ImagePicker imagePicker;
	@BindView(R.id.tv_title)
	TextView tvTitle;
  @BindView(R.id.iv_add)
	ImageView ivAdd;
	@BindView(R.id.btn_avatar_edit)
	ImageView btnEdit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_personal);
		ButterKnife.bind(this);
		//初始控件
		initView();

		//初始化资料
		initData();


	}

	@Override
	protected void onPause() {
		super.onPause();
		disPlg();
	}

	/**
	 * 初始资料
	 */
	private void initData(){

		setDetail();
		//在线更新
		if(XWDataCenter.headBeanMap.isEmpty())
		{
			getDetailTask(XWDataCenter.headBeanMap, XWDataCenter.xwDC.loginName);
		}

	}


	/**
	 * 设置详情
	 */
	private void setDetail(){

			fni =FriendNodeDB.getAFriend(XWDataCenter.getCurAccount(),XWDataCenter.getCurAccount());
           
			if(fni!=null){
				String icon=fni.getIcon();
				if(XWDataCenter.headBeanMap!=null)
				{
					if(XWDataCenter.headBeanMap.containsKey(fni.getLogin_name())){
						String name =XWDataCenter.headBeanMap.get(fni.getLogin_name());
						if(!TextUtils.isEmpty(name)){
							icon=name;
						}
					}
				}
				setHeadPic(icon);

				//设置昵称
				setNickName(fni.getSignName());

				//设置账号
				setAccount(fni.getLogin_name());
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

		tvTitle.setText(getResources().getString(R.string.personal_title));
		qrcode_rl=(RelativeLayout)findViewById(R.id.qrcode_rl);
		qrcode_rl.setOnClickListener(this);

		btn_back=(ImageView) findViewById(R.id.iv_back);
		btn_back.setOnClickListener(this);

		img_head=(ImageView)findViewById(R.id.iv_avatar);

		tv_nickname=(TextView)findViewById(R.id.nickname);
		tv_nickname.setOnClickListener(this);

		user_pic=(RelativeLayout)findViewById(R.id.layout_head);
		user_pic.setOnClickListener(this);
		btnEdit.setOnClickListener(this);
		tv_account=(TextView)findViewById(R.id.account);


	}




	/**
	 * 关闭UI
	 */
	private void closeUI(){
		if(isModify){
			PersonalUI.this.setResult(RESULT_OK);
			PersonalUI.this.finish();
		}else{
			PersonalUI.this.finish();
		}
	}



	@Override
	public void onBackPressed() {
		closeUI();
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			closeUI();
			break;
		case R.id.nickname://昵称
             modifySign();
			break;
		case R.id.btn_avatar_edit:
            selectImage();
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



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG,"onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
			if (data != null && requestCode == SELECT_IMAGE) {
				ArrayList<ImageItem> imageItems = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
				if (imageItems != null && imageItems.size() > 0) {
					uploadPic(imageItems.get(0).path, XWDataCenter.xwDC.loginName,"",MSG_HEAD_UPDATE);
				}
			}
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
	Handler myHander = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(bIsDestroy){
				return;//界面关闭不进行处理
			}
			switch(msg.what){
			case MSG_HEAD_UPDATE:
				String response = (String) msg.obj;
				Log.e(TAG,"response:"+response);
				if(response!=null&&response.contains("1")){
					showToastTips(xechwic.android.XWCodeTrans.doTrans("头像上传成功"));
					{
						getDetailTask(XWDataCenter.headBeanMap, XWDataCenter.xwDC.loginName);
					}

				}

				break;
			case MSG_UPDATE_DETAIL:
				Log.e(TAG,"getDetailTask success");
				setDetail();
				break;
			}
		}

	};

	/**
	 * 上传头像或个性签名
	 */
	AsyncTask uploadTask=null;
	private void uploadPic(final String sourceFileUri,final String loginname,final String message,final int type){
         if(uploadTask!=null){
			 return;
		 }
		uploadTask=new AsyncTask<String,Integer,String>() {
			@Override
			protected String doInBackground(String[] params) {
				try{
					signatureStr=message.trim();
					//先进行编码
					String API_imgURL=Http.getUploadUrl()+"?"+"devicename="+URLEncoder.encode(loginname.trim(),"GBK")
							+"&message="+URLEncoder.encode(signatureStr, "GBK" )
							+"&pwd="+XWDataCenter.getWEBAccessPassword();
					String upLoadServerUri = API_imgURL;
					//////////2017-12-11,使用https
					upLoadServerUri=upLoadServerUri.replace("http://","https://");
					upLoadServerUri=upLoadServerUri.replace(":"+ ServerConfig.XIM_SERVER_PORT,"");

					File sourceFile=null;
					if(sourceFileUri!=null){
						String fileName = sourceFileUri.substring(sourceFileUri.lastIndexOf("/")+1);
						sourceFile = new File(sourceFileUri);
					}
					List<File> files=new ArrayList<File>();
					files.add(sourceFile);
					Response response= OkGo.getInstance().setCertificates().post(upLoadServerUri).addFileParams("uploaded_file",files).execute();
					return response.body().string();

				}
				catch (Exception e) {

					e.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPreExecute() {
				showPlg("");
			}

			@Override
			protected void onPostExecute(String s) {
				if(!bIsDestroy){
					disPlg();
					Message msg= myHander.obtainMessage();
					msg.what=type;
					msg.obj=s;
					myHander.sendMessage(msg);
				}
				uploadTask=null;
			}
		}.execute("");


	}


	/**获取本账号个人资料
	 */
	public void getDetailTask(final Map<String,String> beanList,final String name){

		if(beanList==null||name==null){
			return;
		}
		TaskExecutor.executeTask(new Runnable() {
			@Override
			public void run() {
				try
				{
					// http地址
					String httpUrl = Http.getAHeadUrl()+"?user_id="+URLEncoder.encode(name,"gbk");
					//取得返回的字符串
					String strResult = NetTaskUtil.getDataTaskSync(httpUrl);
					if(!TextUtils.isEmpty(strResult)){
						List<HeadBean> list=BeanOperate.getHeadBeanList(strResult);
						if(list!=null&&!list.isEmpty()){
							isModify=true;
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
									//更新map
									beanList.put(friendName, imageName);
								}

								if(bean.getMessage()!=null){
									String message = bean.getMessage();
									try{
										message=XWDataCenter.XWdecodeurl(message, "gbk");

									}
									catch(Exception ex)
									{
										ex.printStackTrace();
									}
									node.setIntroduction(message);

								}
								saveNode(node);
							}
							//更新UI
							myHander.sendEmptyMessage(MSG_UPDATE_DETAIL);
						}
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});

	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(imagePicker!=null){
			imagePicker.clear();
		}
	}


	/**
	 *  修改昵称
	 */
	private void modifySign(){
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		final LinearLayout buildLayout = new LinearLayout(this);
		final Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(this.getResources().getString(
				R.string.menu_update_name_passwd));
		if(XWDataCenter.xwDC==null){
			return;
		}
		final XWDataCenter xwDC=XWDataCenter.xwDC;
		FriendNodeInfo fni = xwDC.getFNInfoFromID(xwDC.cid);
		if (fni == null)
			return ;
		final TextView signNameText = new TextView(this);
		final TextView passText = new TextView(this);
		final TextView passAgainText = new TextView(this);
		signNameText.setText(this.getResources().getString(
				R.string.alert_new_signName));
		passText.setText(this.getResources().getString(
				R.string.alert_new_passwd));
		passAgainText.setText(this.getResources().getString(
				R.string.alert_new_passwd_again));
		final EditText signNameEdit = new EditText(this);
		final EditText passEdit = new EditText(this);
		final EditText passAgainEdit = new EditText(this);
		signNameEdit.setText(fni.getSignName());
		signNameEdit.setLayoutParams(layoutParams);
		passEdit.setLayoutParams(layoutParams);
		passAgainEdit.setLayoutParams(layoutParams);
		passEdit.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		passAgainEdit.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		passEdit.setMaxLines(1);
		passAgainEdit.setMaxLines(1);
		signNameEdit.setMaxLines(1);
		LayoutParams tmpLP = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		signNameText.setLayoutParams(tmpLP);
		passText.setLayoutParams(tmpLP);
		passAgainText.setLayoutParams(tmpLP);
		LinearLayout signNameLayout = new LinearLayout(this);
		signNameLayout.setOrientation(LinearLayout.HORIZONTAL);
		signNameLayout.setLayoutParams(layoutParams);
		LinearLayout passLayout = new LinearLayout(this);
		passLayout.setOrientation(LinearLayout.HORIZONTAL);
		passLayout.setLayoutParams(layoutParams);
		LinearLayout passAgainLayout = new LinearLayout(this);
		passAgainLayout.setOrientation(LinearLayout.HORIZONTAL);
		passAgainLayout.setLayoutParams(layoutParams);
		signNameLayout.addView(signNameText);
		signNameLayout.addView(signNameEdit);
		passLayout.addView(passText);
		passLayout.addView(passEdit);
		passAgainLayout.addView(passAgainText);
		passAgainLayout.addView(passAgainEdit);
		buildLayout.setOrientation(LinearLayout.VERTICAL);

		buildLayout.addView(signNameLayout);
		buildLayout.addView(passLayout);
		buildLayout.addView(passAgainLayout);
		builder.setView(buildLayout);
		builder.setPositiveButton(PersonalUI.this.getResources()
				.getString(R.string.alert_confirm),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int whichButton) {

				if ((passEdit.getText() != null)
						&& (passAgainEdit.getText() != null)
						) 
				{    
					byte nums[]=new byte[33];
					byte pass[]=new byte[33];

					int status=xwDC.getLoginUser_new(nums, pass);

					String sUserPass=new String(pass).trim();
					try {
						sUserPass = new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_userpassword(XWDataCenter.xwDC.loginName, sUserPass.getBytes("iso-8859-1")), "iso-8859-1");
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					if ( passEdit.getText().length() == 0)
					{
						passEdit.setText(sUserPass);
					}
					if (passAgainEdit.getText().length() == 0)
					{
						passAgainEdit.setText(sUserPass);
					}

					// Log.e("tag",
					// passEdit.getText()+" "+passAgainEdit.getText());
					if (passEdit
							.getText()
							.toString()
							.equals(passAgainEdit.getText()
									.toString())) {
						try {
							xwDC.changPasswdSignName(xwDC.cid,
									(xwDC.loginName + "\0")
									.getBytes("GBK"),
									(signNameEdit.getText() + "\0")
									.getBytes("GBK"),
									(passEdit.getText() + "\0")
									.getBytes("GBK"));
							//设置昵称
							setNickName(signNameEdit.getText().toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						Toast.makeText(
								PersonalUI.this,
								PersonalUI.this
								.getResources()
								.getString(
										R.string.alert_password_again),
										Toast.LENGTH_SHORT).show();
					}
				}
				dialog.dismiss();
			}
		});
		builder.setNeutralButton(PersonalUI.this.getResources()
				.getString(R.string.alert_cancel),
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


	/**保存本用户资料
	 */
	public void saveNode(FriendNodeInfo node){
		if(node==null||node.getLogin_name().length()<1){
			return;
		}
		FriendNodeInfo nodeInfo=XWDataCenter.getFriendDB().getAFriend(XWDataCenter.getCurAccount(),node.getLogin_name());
		if(nodeInfo!=null){
			node.setOnline_type(-1);
			XWDataCenter.getFriendDB().updateFriendNode(node,XWDataCenter.getCurAccount());
		}

	}

	private void selectImage(){
		imagePicker = ImagePicker.getInstance();
		imagePicker.setImageLoader(new GlideImageLoader());
		imagePicker.setMultiMode(false);   //多选
		imagePicker.setShowCamera(true);  //显示拍照按钮
		imagePicker.setSelectLimit(1);    //最多选择9张
		imagePicker.setCrop(true);       //不进行裁剪
		imagePicker.setStyle(CropImageView.Style.RECTANGLE);
		imagePicker.setFocusHeight(512);
		imagePicker.setFocusWidth(512);
		Intent intent = new Intent(this, ImageGridActivity.class);
		startActivityForResult(intent, SELECT_IMAGE);
	}
}