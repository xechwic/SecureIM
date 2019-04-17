/*
 * 查询好友Activity
 * 包括查询在线好友,按条件查询
 * */
package xechwic.android;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import ydx.securephone.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import xechwic.android.act.MainApplication;
import xechwic.android.adapter.FniListAdapter;
import xechwic.android.support.SupportRecyclerView;
import xechwic.android.ui.BaseUI;

/**
 * 查找好友
 *
 */
public class FriendQuery extends BaseUI {
	private XWDataCenter xwDC;
	public Handler mHandler;

	@BindView(R.id.recyclerview)
	SupportRecyclerView recyclerView;
	@BindView(R.id.emptyView)
	View emptyView;
	FniListAdapter adapter;
	private List<FriendNodeInfo> data=new ArrayList<>();

	private LayoutParams layoutParams=null;
	boolean needReq=false;
	@BindView(R.id.tv_title)
	TextView tvTitle;
	@BindView(R.id.iv_back)
	ImageView tvBack;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_friendquery);
		ButterKnife.bind(this);
        this.xwDC = ((MainApplication)this.getApplication()).getDC(this);
        this.mHandler=new FriendQueryHandle(this);
		initView();
	}

	private void initView(){
		layoutParams=new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		tvBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				baseAct.finish();
			}
		});
		tvTitle.setText(getResources().getString(R.string.alert_find_friend));

		adapter = new FniListAdapter(data,R.layout.friend_item);
		adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				if(data!=null&&!data.isEmpty()){
					if(data.size()>position){
						FriendNodeInfo fni = data.get(position);
						if(fni!=null){
				         requestFriend(fni);
						}

					}

				}
			}
		});
		recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
		recyclerView.setAdapter(adapter);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
	}

	public void addFriendTextView(FriendNodeInfo fni){
         data.add(fni);
		 adapter.notifyDataSetChanged();
	}

	/**请求好友列表
	 */
	private String csSelected;
	public void requestFriend(final FriendNodeInfo fni){
		Builder builder = new AlertDialog.Builder(this);
		LinearLayout buildLayout=new LinearLayout(this);
        InputMethodManager im=((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE));
        im.showSoftInput(buildLayout,0);
        buildLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText leaveEdit=new EditText(this);
       
        if(fni.getId()!=xwDC.cid){
        	FriendNodeInfo tmpFNI=xwDC.getFNInfoFromID(fni.getId());
        	if(tmpFNI==null)
			{//该用户不在好友列表中
		        if(fni.getAcceptType()==0){//任何人都可以添加为好友
		        	builder.setTitle(xechwic.android.XWCodeTrans.doTrans("添加好友")+","+xechwic.android.XWCodeTrans.doTrans("请选择分组"));
		    		final Spinner groupSpin=new Spinner(this);
		    		ArrayList<String> groupList=new ArrayList<String>();
		    		for(int i=0;i<xwDC.groupsInfo.size();i++){
		    			FriendGroupInfo fgi=(FriendGroupInfo)xwDC.groupsInfo.get(i);
		    			groupList.add(fgi.getGroupName());
		    		}
		    		groupSpin.setOnItemSelectedListener(new OnItemSelectedListener(){
		    			
		    			public void onItemSelected(AdapterView<?> adpView, View view, int id, long position){
		    				csSelected=adpView.getItemAtPosition(id).toString();
		    			}
		    			
		    			public void onNothingSelected(AdapterView<?> arg0){
		    			}
		    		});
		            ArrayAdapter<String> group_adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, groupList );
		            group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		            groupSpin.setAdapter(group_adapter);
		            groupSpin.setLayoutParams(layoutParams);
		            buildLayout.addView(groupSpin);
		            needReq=true;
		        }else if(fni.getAcceptType()==1){//需要验证
					builder.setTitle(xechwic.android.XWCodeTrans.doTrans("添加好友")+","+xechwic.android.XWCodeTrans.doTrans("请选择分组"));
					final Spinner groupSpin=new Spinner(this);
					ArrayList<String> groupList=new ArrayList<String>();
					for(int i=0;i<xwDC.groupsInfo.size();i++){
						FriendGroupInfo fgi=(FriendGroupInfo)xwDC.groupsInfo.get(i);
						groupList.add(fgi.getGroupName());
					}
					groupSpin.setOnItemSelectedListener(new OnItemSelectedListener(){
						
						public void onItemSelected(AdapterView<?> adpView, View view, int id, long position){
							csSelected=adpView.getItemAtPosition(id).toString();
						}
						
						public void onNothingSelected(AdapterView<?> arg0){
						}
					});
			        ArrayAdapter<String> group_adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, groupList );
			        group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
			        groupSpin.setAdapter(group_adapter);
			        groupSpin.setLayoutParams(layoutParams);
			        
			        TextView leaveLabel=new TextView(this);
			        LinearLayout leaveLayout=new LinearLayout(this);
			        leaveLabel.setText(xechwic.android.XWCodeTrans.doTrans("留言:"));
			        leaveEdit.setLayoutParams(layoutParams);
			        leaveLayout.setLayoutParams(layoutParams);
			        leaveLayout.setOrientation(LinearLayout.HORIZONTAL);
			        leaveLayout.addView(leaveLabel);
			        leaveLayout.addView(leaveEdit);
			        
			        buildLayout.addView(groupSpin);
			        buildLayout.addView(leaveLayout);
			        needReq=true;
	        	}else{//任何人都不能添加为好友
	        		builder.setTitle(xechwic.android.XWCodeTrans.doTrans("对不起,该用户不接受任何人添加"));
	        		needReq=false;
		        }
	        }else if(tmpFNI.getGroupName()!=null&&tmpFNI.getGroupName().equals(xechwic.android.XWCodeTrans.doTrans("临时聊天"))){
				builder.setTitle(xechwic.android.XWCodeTrans.doTrans("添加好友")+","+xechwic.android.XWCodeTrans.doTrans("请选择分组"));
				final Spinner groupSpin=new Spinner(this);
				ArrayList<String> groupList=new ArrayList<String>();
				for(int i=0;i<xwDC.groupsInfo.size();i++){
					FriendGroupInfo fgi=(FriendGroupInfo)xwDC.groupsInfo.get(i);
					groupList.add(fgi.getGroupName());
				}
				groupSpin.setOnItemSelectedListener(new OnItemSelectedListener(){

					public void onItemSelected(AdapterView<?> adpView, View view, int id, long position){
						csSelected=adpView.getItemAtPosition(id).toString();
					}

					public void onNothingSelected(AdapterView<?> arg0){
					}
				});
				ArrayAdapter<String> group_adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, groupList );
				group_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				groupSpin.setAdapter(group_adapter);
				groupSpin.setLayoutParams(layoutParams);
				buildLayout.addView(groupSpin);
				needReq=true;
			}else{//任何人都不能添加为好友
	        	builder.setTitle(xechwic.android.XWCodeTrans.doTrans("对不起,该用户已经是您的好友了"));
	        	needReq=false;
	        }
	        builder.setPositiveButton(xechwic.android.XWCodeTrans.doTrans("确定"),new DialogInterface.OnClickListener(){
	            public void onClick(DialogInterface dialog, int whichButton){
	            	dialog.dismiss();
	            	if(needReq){
	            	switch(fni.getAcceptType()){
		            	case 0:
		            		break;
		            	case 1:
		            		try{
		            			xwDC.manageFN("1".getBytes("GBK"), fni.getId(), (fni.getLogin_name()+"\0").getBytes("GBK"), (  xechwic.android.XWCodeTrans.doTransInput(csSelected)+"\0").getBytes("GBK"), (leaveEdit.getText()+"\0").getBytes("GBK"));
		            		}catch(Exception e){
		            			e.printStackTrace();
		            		}
							finish();
//		            		Intent nextPage=new Intent();
//		        	        nextPage.setClass(FriendQuery.this, FriendControl.class);
//		        	        startActivity(nextPage);
		            		break;
		            	case 2:
		            		break;
		            	}
	            	}
	            }
	        });
			builder.setNeutralButton(xechwic.android.XWCodeTrans.doTrans("取消"),new DialogInterface.OnClickListener(){
	            public void onClick(DialogInterface dialog, int whichButton){
	            	dialog.dismiss();
	            }
	        });
        }else{
        	builder.setTitle(xechwic.android.XWCodeTrans.doTrans("对不起,不能加自己为好友"));
        }
		builder.setView(buildLayout);
		try
		{
		builder.show();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	



}