package xechwic.android;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ydx.securephone.R;
import xechwic.android.act.MainApplication;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.GroupUpdateEvent;
import xechwic.android.ui.BaseUI;



/**
 * 
 * 分组管理
 *
 */
public class FriendManGroup extends BaseUI implements OnClickListener,OnItemSelectedListener{
//	private XWServices xwService;
	private XWDataCenter xwDC;
	private LinearLayout layout=null;
	private Button updateBtn=null;
	private Button addBtn=null;
	private Button deleteBtn=null;
	private Spinner groupSpin=null;
	private EditText editText=null;
	private int isSelect=0;
	private String selectName;
	@BindView(R.id.tv_title)
	TextView tvTitle;
	@BindView(R.id.iv_back)
	ImageView tvBack;


	@Override
    public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setContentView(R.layout.friend_man_group);
			ButterKnife.bind(this);
			this.xwDC = ((MainApplication) this.getApplication()).getDC(this);
			initView();


	}

	private void initView(){
		tvBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				baseAct.finish();
			}
		});
		tvTitle.setText(getResources().getString(R.string.friend_man_group));

		this.layout=(LinearLayout)this.findViewById(R.id.friend_man_group_layout);
		this.layout.setBackgroundResource(R.drawable.skin_chat_background);
		this.groupSpin=(Spinner)this.findViewById(R.id.groupSpinner);
		this.updateBtn=(Button)this.findViewById(R.id.groupUpdateBtn);
		this.addBtn=(Button)this.findViewById(R.id.groupAddBtn);
		this.deleteBtn=(Button)this.findViewById(R.id.groupDeleteBtn);
		this.editText=(EditText)this.findViewById(R.id.groupEditText);
		editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
		this.addBtn.setOnClickListener(this);
		this.updateBtn.setOnClickListener(this);
		this.deleteBtn.setOnClickListener(this);
		//this.groupSpin.setOnItemClickListener(this);
		this.groupSpin.setOnItemSelectedListener(this);

		ArrayList<String> list=new ArrayList<>();
		list.add( xechwic.android.XWCodeTrans.doTrans("请选择...") );
		for(int i=0;i<xwDC.groupsInfo.size();i++){
			String str=(xwDC.groupsInfo.get(i)).getGroupName();
			list.add(str);
		}
		ArrayAdapter<String> status_adapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list );
		status_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		groupSpin.setAdapter(status_adapter);



	}


	@Override
	public void onClick(View v) {
		int res=0;
		if(v==this.addBtn){
			if((this.editText.getText()==null)||(this.editText.getText().toString().trim().equals(""))
					||(this.editText.getText().toString().trim().equals(xechwic.android.XWCodeTrans.doTrans("临时聊天")))
								||(this.editText.getText().toString().trim().equals(xechwic.android.XWCodeTrans.doTrans("我的好友")))
					){
				Toast.makeText(this, xechwic.android.XWCodeTrans.doTrans("请输入有效分组"), Toast.LENGTH_SHORT).show();
				return;
			}
			String tmpStr=this.editText.getText().toString().trim();
			if(xwDC.getFGInfoFromName(tmpStr)!=null){
				Toast.makeText(this, xechwic.android.XWCodeTrans.doTrans("该分组已存在"), Toast.LENGTH_SHORT).show();
				return;
			}
			try{
				Log.e("FriendManGroup","Add group:"+editText.getText().toString());
				res=xwDC.groupManager(xwDC.cid,(this.editText.getText().toString()+"\0").getBytes("GBK"),"".getBytes("GBK"),"1".getBytes());
				Log.e("FriendManGroup","Add group:"+editText.getText().toString()+" "+res);
				if(res==0){
					FriendGroupInfo fgi=new FriendGroupInfo();
					fgi.setGroupName(  this.editText.getText().toString().trim());
					xwDC.groupsInfo.add(fgi);
					BusProvider.getInstance().post(new GroupUpdateEvent());
					showPlg("");
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							disPlg();
						}
					},1000);
				}else{
    				Toast.makeText(FriendManGroup.this, xechwic.android.XWCodeTrans.doTrans("添加失败"), Toast.LENGTH_SHORT).show();
    			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(v==this.updateBtn) {
			if ((this.editText.getText() == null) || (this.editText.getText().toString().trim().equals(""))) {
				Toast.makeText(this, xechwic.android.XWCodeTrans.doTrans("请输入分组"), Toast.LENGTH_SHORT).show();
				return;
			}
			String tmpStr = this.editText.getText().toString().trim();
			if (xwDC.getFGInfoFromName(tmpStr) != null) {
				Toast.makeText(this, xechwic.android.XWCodeTrans.doTrans("该分组已存在"), Toast.LENGTH_SHORT).show();
				FriendManGroup.this.finish();
				return;
			}
			FriendGroupInfo fgi = xwDC.getFGInfoFromName(selectName);
			if (fgi == null)
			{
				Toast.makeText(this, xechwic.android.XWCodeTrans.doTrans("选定的分组名不存在!"), Toast.LENGTH_SHORT).show();
				FriendManGroup.this.finish();
				return;
		    }
			try {
				Log.e("FriendManGroup","update group:"+selectName);
				res=xwDC.groupManager(xwDC.cid,(selectName+"\0").getBytes("GBK"),(tmpStr+"\0").getBytes("GBK"),"2".getBytes());
				Log.e("FriendManGroup","update group:"+selectName+" "+res);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			android.util.Log.v("XIM","friend chanage groupname 1 " + selectName+"--> "+tmpStr);
			
			
			if(res==0)
			{
				android.util.Log.v("XIM","friend chanage groupname 2 " + selectName+"--> "+tmpStr);
				
				
				//String newGroupName=this.editText.getText().toString().trim();
				for(int i=0;i<xwDC.nodesInfo.size();i++){
					FriendNodeInfo fni=xwDC.nodesInfo.get(i);
					
					android.util.Log.v("XIM","fni  " + fni.getLogin_name()+"  "+fni.getGroupName());

					
					if(fni!=null&&fni.getGroupName().equals(selectName)){
						fni.setGroupName(tmpStr);
						
						android.util.Log.v("XIM","friend chanage groupname 3 " + selectName+"--> "+tmpStr);
								
						
						try
						{
						XWDataCenter.getFriendDB().updateFriendNode(fni,XWDataCenter.getCurAccount());
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
						android.util.Log.v("XIM","friend chanage groupname " + fni.getLogin_name()+" "+tmpStr);
						
					}
				}
				fgi.setGroupName(tmpStr);
				BusProvider.getInstance().post(new GroupUpdateEvent());
				showPlg("");
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						disPlg();
					}
				},1000);
			}else{
				Toast.makeText(FriendManGroup.this, xechwic.android.XWCodeTrans.doTrans("更新失败,请检查网络"), Toast.LENGTH_SHORT).show();
			}
		}else if(v==this.deleteBtn){
			final FriendGroupInfo fgi=xwDC.getFGInfoFromName(selectName);
			if(fgi!=null){
				Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(xechwic.android.XWCodeTrans.doTrans("信息提示"));
				builder.setMessage(xechwic.android.XWCodeTrans.doTrans("确定要删除")+" "+selectName+xechwic.android.XWCodeTrans.doTrans("分组")+"?");
				builder.setPositiveButton(xechwic.android.XWCodeTrans.doTrans("删除"),new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                    	dialog.dismiss();
                    	int res=-1;

							try {
								Log.e("FriendManGroup","delete group:"+selectName);
								res=xwDC.groupManager(xwDC.cid,(selectName+"\0").getBytes("GBK"),"\0".getBytes("GBK"),"3".getBytes());
								Log.e("FriendManGroup","delete group:"+selectName+" "+res);
								if(res==0){
									xwDC.removeFriendGroupInfo(fgi);
									removeGroupFriend(selectName);
									BusProvider.getInstance().post(new GroupUpdateEvent());
									showPlg("");
									new Handler().postDelayed(new Runnable() {
										@Override
										public void run() {
											disPlg();
										}
									},1000);
								}else{
									Toast.makeText(FriendManGroup.this, xechwic.android.XWCodeTrans.doTrans("删除失败"), Toast.LENGTH_SHORT).show();
								}

							} catch (Exception e) {
								e.printStackTrace();
							}


                    }
                });
				builder.setNeutralButton(xechwic.android.XWCodeTrans.doTrans("取消"),new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
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
		}
	}
	
	public void onItemSelected(AdapterView<?> adpView, View view, int id, long position) {
		// TODO Auto-generated method stub
		////Log.v("tag",id+" "+position);
		String selected=adpView.getItemAtPosition(id).toString();    
		if(selected.equals(xechwic.android.XWCodeTrans.doTrans("请选择..."))||selected.equals(getResources().getString(R.string.my_good_friend))){
			this.updateBtn.setEnabled(false);
			this.deleteBtn.setEnabled(false);
			this.editText.setText("");
		}else{
			this.isSelect=id-1;
			this.updateBtn.setEnabled(true);
			this.deleteBtn.setEnabled(true);
			//String str=((FriendGroup)SelectFriendUI.fc_out.fgs.get(id-1)).getGroup_name();
			this.editText.setText(selected);
			this.selectName=selected;
		}
		//this.updateBtn.setEnabled(true);
	}
	
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Log.v("tag", "SelectFriendUI onResume");
		
		this.xwDC = ((MainApplication) this.getApplication()).getDC(this);
	}

    ///刷新组名
	private void removeGroupFriend(String oldName) {
		List<FriendNodeInfo> temList=new ArrayList<>();
		for (int i = 0; i < FriendControl.friendList.size(); i++) {
			FriendNodeInfo fni =  FriendControl.friendList.get(i);
			if (fni!=null&&fni.getGroupName()!=null&&fni.getGroupName().equals(oldName)) {
				temList.add(fni);
			}
		}
		if(temList.size()>0){
			for(FriendNodeInfo node:temList){
				FriendControl.friendList.remove(node);
			}
			temList.clear();
		}
	}

}
