package xechwic.android;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

import ydx.securephone.R;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.FragmentRereshEvent;

public class FriendControlHandle extends Handler{
	String TAG=FriendControlHandle.class.getSimpleName();
	private FriendControl fc;
	public FriendControlHandle(FriendControl fc){
		this.fc=fc;
	}



    /**
     * 27,显示余额；8，收到一个好友结点；22，所有好友已下载完成;4,更新好友
     * 1, 收到好友邀请
     * 16,连接断开提示
     * 14,对方拨号后又挂断
     * 3,收到信息
     * 2,得到分组名
     * 28,更新通话记录
     * 29,更新头像
     */
	@Override
	public void handleMessage(Message msg) {
        super.handleMessage(msg);
		if(fc==null||fc.bIsDestroy){
			return;
		}
        FriendNodeInfo fni=null;

        FriendGroupInfo fgi=null;
        Log.e(TAG,"handle what:"+msg.what);
        
        switch (msg.what) {
	        case 1:                 //好友邀请
	  		  if(fc!=null){
	  		    //////////////////2014-07-09,立即显示!!!!!!!!!!!!!
	  		    fc.getSystemMessageAlert();
	  		  }
	            break;
	        case 2://收到好友分组处理
	        	fgi=(FriendGroupInfo)msg.obj;

//	        	new FriendGroup(fc,fgi.getGroupName());
	        	//保存
	        	if(fc!=null&&fgi!=null){
	        	  	fc.refreshGroupName(fgi.groupName);
	        	}
	      
	        	break;
	        case 3://收到消息

                if(fc!=null){
                  	//更新历史记录
//    	        	fc.refreshChatAdapter();
					BusProvider.getInstance().post(new FragmentRereshEvent(0));
    	        	fc.updateUnreadIcon(); ///////////更新未读图标!!!!!2014-11-14
                }
	      
	        	
	        case 4://下载好友节点,含头像
				//刷新界面
//                fc.repaintFriendControl();
                 fc.repaintContactUI();
	        


	        	break;
	        case 5://收到被查询到的好友

	        	break;
	        case 6://收到系统消息

	        	if(fc!=null){
	        		fc.getSystemMessageAlert();
	        	}
	        
	        	break;
	        case 7://删除好友

	        	fni=(FriendNodeInfo)msg.obj;
	        	if(fc!=null&&fni!=null){
	        	  	fc.updateControlChildView();
					String name =fni.getSignName();
					if(name==null||name.trim().length()<1){
						name=fni.getLogin_name();
					}
					fc.showToastTips(""+name+XWCodeTrans.doTrans("跟你解除了好友关系!"));
	        	}
	      

	        	break;
	        case 8://收到了一个好友节点
	        	fni=(FriendNodeInfo)msg.obj;
	        	if(fni==null){
	        		return;
	        	}
				if(fni.getLogin_name().equals(XWDataCenter.getCurAccount())){
					fni.setOnline_type(99);
				}

				if(fc!=null){
					//获取用户头像列表
					if(XWDataCenter.headBeanMap==null){
						XWDataCenter.headBeanMap= new HashMap<String, String>();
					}
					
		        	
                    Log.e("frian","fni:"+fni.getId()+","+XWDataCenter.xwDC.cid);
		        	
		        	//刷新组名
		        	fc.refreshGroupName(fni.getGroupName());
					////好友列表下载完成后，再下载头像列表
//		        	if(XWDataCenter.xwDC!=null&&XWDataCenter.xwDC.cid==fni.getId()){
//
//		        	    //开始获取所有好友头像
//		        	    fc.getHeadBeanTask(XWDataCenter.headBeanMap, XWDataCenter.xwDC.loginName);
//		        	}
//
		        	fc.repaintFriendControl();
		        	
		        	
	        		if (XWDataCenter.xwDC.loginName.equals(fni.getLogin_name()))
	        		{
	        	        fc.showUI();
	        		}
				}
			
	        	
	        	break;
	        case 9://有来电请求,目前做为收到消息处理
	        	break;
	        case 10://收到一帧视频数据
	        	
	        	break;
	        case 11:
	        	
	        	break;
	        case 12:
	        	
	        	break;
	        case 13://出现先收到好友后收到其分组信息的情况
	        	break;
	        case 14://对方拨号后又挂断
	        	/*当拨号做为好友普通消息时处理*/

	        	/*任何人都可以拨进来*/
	        	if(fc!=null){
					fc.showToastTips(fc.getResources().getString(R.string.alert_that_hungup));
					XWDataCenter.xwDC.netPhoneTime=0;
					if(fc.bIsFront){
	                    fc.updateVideoView();
					}
	        	}
	      
	        	break;
	        case 15:
	        	if(fc!=null){
	        		fc.updateControlChildView();
	        		fc.showUI();
	        	}
	        	
	        	break;
	        case 16://连接断开提示
                if(fc!=null){
                    fc.showToastTips((String)msg.obj);
    	        	fc.showUI();
                }
	    
	        	break;
	        case 17://已连上
	        	if(fc!=null){
                    fc.showToastTips(xechwic.android.XWCodeTrans.doTrans("已连上"));
		        	fc.showUI();
	        	}

	        	break;
	        case 18://有来电,所有用户都提示
	        	break;
	        case 19:
	        	break;
	        case 20://拨通处理
	       
	        	if(fc!=null){
	        	 	Intent nextPage=new Intent();
		    	    nextPage.setClass(fc, FriendVideoDisplay.class);
		    	    fc.startActivity(nextPage);
	        	}
	        	break;
	        case 21://拨号提示
				fc.showToastTips((String)msg.obj);
	        	break;
	        case 22://所有好友已下载完成, 开始下载头像列表//已在 XWDateCenter的manageFriendnode处理

				break;
	        case 23:
	        	String alertStr=null;
	        	if(fc!=null){
	        		switch(msg.arg1){
		        	case 1:alertStr=fc.getResources().getString(R.string.alert_communication_error);break;
		    		case 2:alertStr=fc.getResources().getString(R.string.alert_that_busy);break;
		    		case 3:alertStr=fc.getResources().getString(R.string.alert_that_reject);break;
		    		case 4:alertStr=fc.getResources().getString(R.string.alert_number_error);break;
		    		case 5:alertStr=fc.getResources().getString(R.string.alert_account_error);break;
		    		case 6:alertStr=fc.getResources().getString(R.string.alert_that_hungup);break;
		    		default:break;
		    		}
	        		Toast.makeText(fc, alertStr, Toast.LENGTH_SHORT).show();
	        	}
	        	
	        	
	        	
	        	break;
	        case 24:
	        	if(fc!=null){
	        		fc.finish();
	        	}
	        	
	        	break;
	        case 25:
	        	if(fc!=null){
	        	  	Toast.makeText(fc, fc.getResources().getString(R.string.menu_update_success), Toast.LENGTH_SHORT).show();//修改成功
		        	////fc.xwDC.logoutService(0);
		        	Intent reLogin=new Intent();
					reLogin.setClass(fc, FriendLogin.class);
					fc.startActivity(reLogin);
	        	}
	      
			    //fc.finish();
	        	break;
	        case 26:
	        	if(fc!=null){
	        		Toast.makeText(fc, fc.getResources().getString(R.string.menu_update_failed), Toast.LENGTH_SHORT).show();//修改失败	
	        	}
	        	
	        	break;
	        	
	        case 27: ///////////显示余额 !!!!!!!!!!!!!
	    		/////////////////显示余额!!!!!!!!!!!!!!!!
	        	if(fc!=null){
	        		fc.showUI();
	        		/////fc.friend_call_display_account.setText(fc.getResources().getString(R.string.alert_call_account)+" "+fc.xwDC.fformat.format((float)fc.xwDC.currentAccountValue/100));	
	        	}
	        	
	        	break;	        
	        	
	        	
	        case 28: ///////////更新通话记录!!!!!!!!!!!!!!
	        	if(fc!=null){
	        		try
	        		{
//		        		fc.mCallView.renewRecord();
//		        		fc.mRecordView.renew();
						BusProvider.getInstance().post(new FragmentRereshEvent(2));
	        		}
	        		catch(Exception ex)
	        		{
	        			ex.printStackTrace();
	        		}
	        	}
	        	
	        	break;	  	        	
	        case 29:///////更新当前界面好友头像

	        
	        	break;
	        	
	        case 30:///////发起密钥协商,2014-11-28  /////已经转到XWDataHandler 60处理

	        	break;  	
	        	
	        default:
	            break;
        }
    }



}
