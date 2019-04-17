package xechwic.android.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ydx.securephone.R;
import xechwic.android.FriendChatRecord;
import xechwic.android.XWCodeTrans;
import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.bean.ChatMsgEntity;
import xechwic.android.ui.FriendDetailUI;
import xechwic.android.ui.PersonalUI;
import xechwic.android.ui.PhotoViewUI;
import xechwic.android.ui.VideoPlayerActivity;
import xechwic.android.util.FileUtil;
import xechwic.android.util.Http;
import xechwic.android.util.JRSConstants;

import static xechwic.android.XWDataCenter.getDecryptFilepath;

/**
 * 当前聊天界面ListView的Adapter
 * 
 * 
 */
public class ChatMsgListAdapter extends BaseAdapter{
	

	private int[] imgs = { R.drawable.icon };
	
	private FriendChatRecord context;//  绑定FriendChatRecord
	private XWDataCenter xwCenter;
	
    private boolean bIsLoading=false;

	private List<ChatMsgEntity> coll;// 消息对象数组

    private String friendAccount;
	public void setXWCenter(XWDataCenter xw){
		this.xwCenter=xw;
	}
	public void setContext(FriendChatRecord context){
		this.context=context;
	}
	public ChatMsgListAdapter(FriendChatRecord context, List<ChatMsgEntity> coll) {
		this.context=context;
		this.coll = coll;
	}

	
	public void setNodeAccount(String  account){
		this.friendAccount=account;
	}
	
	@Override
	public int getCount() {
		return coll.size();
	}

	@Override
	public Object getItem(int position) {
		return coll.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(context==null){
			return null;
		}
		final ChatMsgEntity entity = coll.get(position);
		final int isComMsg = entity.getComMeg();
		int sendProgress=entity.getProgress();//发送进度
        int sendflag=entity.getSendFlag();//发送状态（1发送不成功，10发送成功)
        int sendtype=entity.getMsgType();//消息类型
		ViewHolder viewHolder = null;

			if (isComMsg==1) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.chatting_item_msg_text_left, null);
			} else {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.chatting_item_msg_text_right, null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.icon = (ImageView) convertView
					.findViewById(R.id.iv_userhead);
			viewHolder.imgSend=(ImageView)convertView.findViewById(R.id.img_resend);
			viewHolder.txProgress=(TextView)convertView.findViewById(R.id.tx_send_progress);
			convertView.setTag(viewHolder);
		
		viewHolder.tvSendTime.setText(entity.getDate());
		viewHolder.tvUserName.setText(entity.getName());
		
		//头像
		if (isComMsg==1) {
			if(context.friendImage!=null&&context.friendImage.length()>0){
				Glide.with(MainApplication.getInstance())
						.load(Http.getHeadPicUrl()+context.friendImage)
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.error(R.drawable.icon)
						.into(viewHolder.icon);
			}else{
				viewHolder.icon.setImageResource(imgs[0]);
			}
			//对方头像点击
			viewHolder.icon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(friendAccount!=null){
						Intent nextPage = new Intent();
						Bundle bundlenew = new Bundle();
						bundlenew.putString("friendAccount", friendAccount);
						nextPage.putExtras(bundlenew);
						nextPage.setClass(context,
								FriendDetailUI.class);
						nextPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(nextPage);
					}
						
				
					
				}
			});
		}else{
			if(context.myImage!=null&&context.myImage.length()>0){
				Glide.with(MainApplication.getInstance())
						.load(Http.getHeadPicUrl()+context.myImage)
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.error(R.drawable.icon)
						.into(viewHolder.icon);
			}else{
				viewHolder.icon.setImageResource(imgs[0]);
			}
			//头像点击
			viewHolder.icon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.setClass(context,PersonalUI.class);
					context.startActivity(intent);
					
				}
			});
		}
		
	
		
		//显示发送进度
		if(sendtype==FriendChatRecord.MSG_PHOTO||
				sendtype==FriendChatRecord.MSG_FILE||
				sendtype==FriendChatRecord.MSG_VOICE||
				sendtype==FriendChatRecord.MSG_VIDEO){
			if(sendflag==11){
				viewHolder.imgSend.setVisibility(View.VISIBLE);
				viewHolder.imgSend.setImageResource(R.drawable.file_unread);
			}else if(sendflag>=10){
				viewHolder.txProgress.setVisibility(View.GONE);
				viewHolder.imgSend.setVisibility(View.GONE);
			}else if(sendflag==-1){
				viewHolder.txProgress.setVisibility(View.GONE);
				viewHolder.imgSend.setVisibility(View.VISIBLE);
				viewHolder.imgSend.setImageResource(R.drawable.resend_normal);
			}else{
				viewHolder.txProgress.setVisibility(View.VISIBLE);
				viewHolder.txProgress.setText(""+sendProgress+"%");
			}
		}else{
			//普通未发送标识显示
			if(sendflag>=10){
				viewHolder.imgSend.setVisibility(View.GONE);
			}else{
				viewHolder.imgSend.setVisibility(View.VISIBLE);
			}
		}

        ////重发点击
		viewHolder.imgSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int flag=entity.getSendFlag();
			   if(flag==-1||flag==3||flag==4) {        //失败、停止、出错的信息
				   showToast(context, entity);
			   }
			}
		});


		String content=entity.getMessage();
         ////显示文件后缀名
		if(entity.getMsgType()==FriendChatRecord.MSG_FILE){
		      content=content+FileUtil.getFileExt(entity.getFilePath());
		}
		viewHolder.tvContent.setCompoundDrawables(null, null, null, null);
		//显示表情和文本
		viewHolder.tvContent.setText(xwCenter.parser.replace(content, this.context));


		//设置可以点击
		viewHolder.tvContent.setClickable(true);
		//设置点击事件
		viewHolder.tvContent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				   if(entity.getMsgType()==FriendChatRecord.MSG_TEXT
						   ||entity.getMsgType()==FriendChatRecord.MSG_EMU){
				   }else{
					   openFileTask(entity);
				   }
				
			}
		});
		
		viewHolder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {

				   //复制内容
				   if(entity.getMsgType()==FriendChatRecord.MSG_TEXT
						   ||entity.getMsgType()==FriendChatRecord.MSG_EMU){
					   FriendChatRecord.copy(entity.getMessage(), context);
					   Toast.makeText(context, XWCodeTrans.doTrans("内容已经复制"), Toast.LENGTH_SHORT).show();
				   }else{
					   //////解码到用户指定位置
					   android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(context);
//					   dialogBuilder.setTitle("请选择文件类型");
					   CharSequence[] menuItemArray = new CharSequence[] {
							   XWCodeTrans.doTrans("导出")};
					   dialogBuilder.setItems(menuItemArray,
							   new DialogInterface.OnClickListener() {
								   @Override
								   public void onClick(DialogInterface dialog, int which) {
									   switch (which) {
										   case 0:
											   context.decodeEntity=entity;
											   context.openAssignFolder();
											   context.showToastTips("选择文件所在目录作为解码文件的存储目录！");
											   break;
									   }

								   }
							   });
					   dialogBuilder.show();

				   }
				 
//			   }
					
				
				return false;
			}
		});
		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public ImageView imgSend;
		public ImageView icon;
		public TextView txProgress;
	}

	/**重发提示
	 */
	private void showToast(final FriendChatRecord context,final ChatMsgEntity entity){
       if(context==null||entity==null){
    	   return;
       }


		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(xechwic.android.XWCodeTrans.doTrans("重发提示"));
		builder.setMessage(xechwic.android.XWCodeTrans.doTrans("您确定重发消息吗？"));

		builder.setPositiveButton(xechwic.android.XWCodeTrans.doTrans("确定"), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				int type= entity.getMsgType();
				
				//失败消息处理 重发
				if(type==FriendChatRecord.MSG_PHOTO||
						type==FriendChatRecord.MSG_FILE||
						type==FriendChatRecord.MSG_VOICE||
						type==FriendChatRecord.MSG_VIDEO){
					if(entity.getSendFlag()==-1){
					String content = entity.getMessage();
					content=content.replace("(:", "");
					content=content.replace(")", "");
					content=content.replace("\0", "");
					context.resendFile(entity);}
				}else{
					if(entity.getSendFlag()<10){
						context.resendText(entity);
					}
					
				}
			}
			
		}).setNegativeButton(XWCodeTrans.doTrans("取消"), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();	
			}
			
		}).create().show();
	
	
	}
	public  List<ChatMsgEntity> decryptList=new ArrayList<>();//解码过的文件
	/**
	 * 文件解码并打开
	 * @param entity 文件
	 */
	private void openFileTask(final ChatMsgEntity entity){
		//////更新未读标识
		if(entity.getComMeg()==1&&entity.getSendFlag()==11){
			entity.setSendFlag(10);
			entity.setSnap(1);
			//更新未读标识
			context.saveMsg(entity);
			//更新UI
			context.refreshData();
		}
		///////检测是否解码
		String path=entity.getFilePath();
		if(FileUtil.isFileExist(path)) {
			context.viewFile(entity);
		}else{
			decodeFileTask(entity);
		}


	}

	/////解码文件
	private void decodeFileTask(final ChatMsgEntity entity){
		new AsyncTask<String,Integer, String>() {

			@Override
			protected String doInBackground(String... arg0) {
				String targetPath=null;
				try{
					////清理之前解码的文件
					for(ChatMsgEntity msgEntity:decryptList){
						if(msgEntity.getNo()!=entity.getNo()){
							XWDataCenter.delDecrypt(getDecryptFilepath(msgEntity));
						}
					}
					decryptList.clear();
					targetPath=getDecryptFilepath(entity);
					if(targetPath==null){
						return null;
					}
					////解码文件已经存在
					if(FileUtil.isFileExist(targetPath)){
						return targetPath;
					}
					////从加密文件解码
					String enPath=entity.getFilePath()+JRSConstants.ENCRYPT_END;
					if(FileUtil.isFileExist(enPath)){
						XWDataCenter.decodeFile(enPath,targetPath);
					}
					////判断解码文件存在
					File deFile=new File(targetPath);
					if(!deFile.exists()) {
						//返回原始路径
						targetPath=entity.getFilePath();
					}

				}catch(Exception e){
					e.printStackTrace();
				}

				return targetPath;
			}

			@Override
			protected void onPreExecute() {
				bIsLoading=true;
				context.showPlg("");
			};

			@Override
			protected void onPostExecute(String result) {
				bIsLoading=false;
				context.disPlg();


				if(entity.getComMeg()==1&&entity.getSendFlag()==11){

					entity.setSendFlag(10);
					entity.setSnap(1);
					//更新未读标识
					context.saveMsg(entity);
					//更新UI
					context.refreshData();
				}

				int type= entity.getMsgType();
				String path=result;
				if(TextUtils.isEmpty(path)){
					context.showToastTips("no file");
					return;
				}
				////判断解码文件存在
				File deFile=new File(path);
				if(!deFile.exists()){
					context.showToastTips("no file");
					return;
				}else{
					////加入已解码列表
					decryptList.add(entity);
				}

				if(type==FriendChatRecord.MSG_PHOTO){
					Intent intent=new Intent(context,PhotoViewUI.class);
					String picPath="file://"+path;
					intent.putExtra(JRSConstants.DATA, picPath);
					context.startActivity(intent);
				}else if(type==FriendChatRecord.MSG_VOICE){
					XWDataCenter.xwDC.XWPlayWeiXinAudioFile(path);

				}else if(type==FriendChatRecord.MSG_FILE){
					File f=new File(path);
					if(f.exists()){
						context.openFile(f);
					}
				}else if(type==FriendChatRecord.MSG_VIDEO){
					context.startActivity(new Intent(context, VideoPlayerActivity.class).putExtra(
							"path", path));
				}


			};

		}.execute("");
	}

}
