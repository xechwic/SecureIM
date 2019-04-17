package xechwic.android;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import ydx.securephone.R;
import xechwic.android.util.TaskExecutor;

public class FriendChatRecordHandle extends Handler{
	private String TAG=FriendChatRecordHandle.class.getSimpleName();
	private FriendChatRecord fcr;
	public FriendChatRecordHandle(FriendChatRecord fcr){
		this.fcr=fcr;
	}
	
	
	/**
	 * 6:有好友更新(上线、离线)
	 * 2:刷新数据
	 */
	@Override
	public void handleMessage(Message msg) {
		 super.handleMessage(msg);
		 if(fcr==null||fcr.bIsDestroy){
			 return;
		 }

	        switch (msg.what) {
		        case 1:
		        	Log.e(TAG,"get msg:"+msg.what);
//		        	fcr.solutePage.setText(String.valueOf(fcr.getPp().getPage()));
//		        	fcr.getPageText().setText(String.valueOf(fcr.getPp().getPage())+"/"+fcr.getPp().getTotalPage());
		        	break;
		        case 2://刷新当前页面,滚到最新记录
//		        	fcr.pp.setPage(1);
//					fcr.setOnePageRecord();
		        	fcr.refreshData();
		        	break;
		        case 3://从数据库中收到一条记录
//		        	FriendChatRecordInfo fcri=(FriendChatRecordInfo)msg.obj;
//		        	fcr.addRecord(fcri.getNickname(), fcri.getCtime(), fcri.getContent(), fcri.getFlag());
		        	fcr.refreshData();
		        	break;
		        case 4://有来电
		        	Intent nextPage=new Intent();
		    		nextPage.setClass(fcr, FriendCall.class);
		    	    fcr.startActivity(nextPage);
		    	    fcr.finish();
		        	break;
		        case 5:
		        	Toast.makeText(fcr, fcr.getResources().getString(R.string.menu_update_success), Toast.LENGTH_SHORT).show();//修改成功
		        	////fcr.xwDC.logoutService(0);
		        	Intent reLogin=new Intent();
					reLogin.setClass(fcr, FriendLogin.class);
				    fcr.startActivity(reLogin);
				    //fcr.finish();
		        	break;
		        case 6:
		        	
		        	FriendNodeInfo fni =(FriendNodeInfo)msg.obj;
		        	if(fcr!=null&&fni!=null){
		        		//////////////////2014-08-01,判断更新的好友是否是当前好友
		        		if ((fni.getLogin_name()!=null)&& (fcr.fni!=null)&& (fni.getLogin_name().equals(fcr.fni.getLogin_name())))
		        		{
			        		//更新好友
	//		        		fcr.saveNode(fni, XWDataCenter.fni.getLogin_name());   //xwdc已经保存
			        		//更新标题栏
			        	    fcr.updateTitle(fni);
			        		//更新头像
			        		fcr.updateHeads();
			        		//刷新数据
			        		fcr.refreshData();

		        		}
		        	}
		        	break;
		        	
		        case 7:
			        {
			        	if( (fcr!=null)&& (fcr.fni!=null)){
                            
			        	    fcr.updateTitle(fcr.fni);
			        	}
			        }
			        break;
				case 8://  好友关系解除
					fni =(FriendNodeInfo)msg.obj;
					if(fni!=null){
						String name =fni.getSignName();
						if(name==null||name.trim().length()<1){
							name=fni.getLogin_name();
						}
						fcr.showToastTips(""+name+XWCodeTrans.doTrans("跟你解除了好友关系!"));
						fcr.finish();
					}

					break;
				case 9://显示锁图标
					fcr.setAeslockView(View.VISIBLE);
					break;
				case 10://隐藏锁图标
					fcr.setAeslockView(View.GONE);
					break;
				case 11://提示消息
					fcr.showToastTips(XWCodeTrans.doTrans("端到端加密密钥正在协商中..."));
					break;
				case 12://处理更新锁事件
					TaskExecutor.executeTask(new Runnable() {
						@Override
						public void run() {
							Log.e("chat","TaskExecutor UpdateAESIcon");
							fcr.UpdateAESIcon();
						}
					});
					break;
		        default:break;
	        }
	}
}
