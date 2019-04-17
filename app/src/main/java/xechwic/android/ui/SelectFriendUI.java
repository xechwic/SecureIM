
package xechwic.android.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xechwic.android.FriendControl;
import xechwic.android.FriendGroupInfo;
import xechwic.android.FriendNodeInfo;
import xechwic.android.FriendQuery;
import xechwic.android.XWCodeTrans;
import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.adapter.ExFriendAdapter;
import xechwic.android.bean.GroupBean;
import xechwic.android.bus.BusProvider;
import xechwic.android.bus.event.AvatarUpdateEvent;
import xechwic.android.util.ComparatorUser;
import xechwic.android.view.expand.IphoneTreeView;
import ydx.securephone.R;

import static xechwic.android.FriendControl.friendList;
import static xechwic.android.FriendControl.queryFriendByNum;
import static xechwic.android.FriendControl.updateFriendGroupName;
import static xechwic.android.FriendControl.updateFriendSign;

public class SelectFriendUI extends BaseUI{
	String TAG=SelectFriendUI.class.getSimpleName();

	@BindView(R.id.iphone_tree_view)
	IphoneTreeView friendExListView;//好友listView
	private ExFriendAdapter mAdapter;//分组适配器
	private List<GroupBean> mData=new ArrayList<>();
	public List<String> groupNameList=new ArrayList<>() ;// 大组成员名
    @BindView(R.id.tv_title)
	TextView tvTitle;
	@BindView(R.id.iv_back)
	ImageView tvBack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_selectfriend);
		ButterKnife.bind(this);
        xwDC=MainApplication.getInstance().getDC(this);
		BusProvider.getInstance().register(this);
        
		//初始化控件
		initView();
	}


	@Override
	protected void onResume() {
		super.onResume();
		refresh();
	}

	private void initView() {
		tvTitle.setText(getResources().getString(R.string.my_good_friend));
		tvBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				baseAct.finish();
			}
		});
		initFriendList();
	}


	/**
	 *初始好友
	 */
	private void initFriendList(){
		mAdapter = new ExFriendAdapter(this, mData, friendExListView);
		friendExListView.setAdapter(mAdapter);
		friendExListView.setGroupIndicator(null);
		friendExListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2, int arg3, long arg4) {
				FriendNodeInfo fni=mData.get(arg2).getChildList().get(arg3);
				if(fni!=null){
					managerFriends(fni);
				}
				return true;
			}
		});



	}


	/**
	 * 好友排序
	 */
	private void sortFriendList(List<FriendNodeInfo> friendList){
		if(friendList==null||friendList.isEmpty()){
			return;
		}
		ComparatorUser comparator=new ComparatorUser();
		Collections.sort(friendList, comparator);
	}

	/**
	 *获取组数据
	 */
	private void getGroupData(List<FriendNodeInfo> friendList){
		if(mData==null||friendList==null){
			return;
		}
		mData.clear();
		for(String name:groupNameList){
			List<FriendNodeInfo> friendnodes= new ArrayList<>();
			for(FriendNodeInfo node:friendList){
				if(node!=null&&node.getGroupName()!=null){
					if(node.getGroupName().equals(name)){
						friendnodes.add(node);
					}
				}
			}
			sortFriendList(friendnodes);
			GroupBean group=new GroupBean();
			group.setGroupName(name);
			group.setChildList(friendnodes);
			mData.add(group);
		}
	}

	/**刷新组名列表
	 */
	public void refreshGroupName(List<FriendGroupInfo> groupsInfo){
		if(groupsInfo==null||groupsInfo.isEmpty()){
			String name;
			List<String> list=new ArrayList<>();
			for(FriendNodeInfo info: FriendControl.friendList){
				if(info!=null){
					name = info.getGroupName();
					if(!TextUtils.isEmpty(name)&&!list.contains(name)){
						list.add(name);
					}
				}
			}
			groupNameList.clear();
			groupNameList.addAll(list);
		}else{
			String name;
			List<String> list=new ArrayList<>();
			for(FriendGroupInfo info:groupsInfo){
				if(info!=null){
					name = info.groupName;
					if(!TextUtils.isEmpty(name)){
						list.add(name);
					}
				}
			}
			groupNameList.clear();
			groupNameList.addAll(list);
		}


	}

	private void refresh(){
		//刷新组名
		Log.e(TAG,"refresh");
		refreshGroupName(XWDataCenter.xwDC.groupsInfo);
		getGroupData(friendList);
		updateFriendIcon(friendList);
		if(mAdapter!=null){
			mAdapter.notifyDataSetChanged();
		}

	}

	private String optionSelect;
	public void managerFriends(final FriendNodeInfo csFn){
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		final LinearLayout buildLayout = new LinearLayout(this);
		final Spinner spin = new Spinner(this);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		if (csFn== null) {
			builder.setTitle(getResources().getString(
					R.string.alert_select_error));
		} else {
			builder.setTitle(getResources().getString(
					R.string.alert_select_friend)
					+ csFn.getSignName());
		}

		final EditText editName = new EditText(this);
		editName.setTextColor(getResources().getColor(R.color.black));
		final EditText editSignName = new EditText(this);
		editSignName.setTextColor(getResources().getColor(R.color.black));
		final EditText editEmail = new EditText(this);
		editEmail.setTextColor(getResources().getColor(R.color.black));

		final TextView labelName = new TextView(this);
		labelName.setTextColor(getResources().getColor(R.color.black));
		final TextView labelSignName = new TextView(this);
		labelSignName.setTextColor(getResources().getColor(R.color.black));
		final TextView labelEmail = new TextView(this);
		labelEmail.setTextColor(getResources().getColor(R.color.black));

		labelName.setText(getResources().getString(R.string.alert_number));
		labelSignName.setText(getResources().getString(
				R.string.alert_remarks));
		labelEmail.setText(getResources().getString(R.string.alert_email));

		final LinearLayout layoutName = new LinearLayout(this);
		final LinearLayout layoutSignName = new LinearLayout(this);
		final LinearLayout layoutNum = new LinearLayout(this);
		final LinearLayout layoutEmail = new LinearLayout(this);

		layoutName.setBackgroundColor(getResources().getColor(R.color.white));
		layoutSignName.setBackgroundColor(getResources().getColor(R.color.white));
		layoutNum.setBackgroundColor(getResources().getColor(R.color.white));
		layoutEmail.setBackgroundColor(getResources().getColor(R.color.white));


		layoutName.setOrientation(LinearLayout.HORIZONTAL);
		layoutSignName.setOrientation(LinearLayout.HORIZONTAL);
		layoutNum.setOrientation(LinearLayout.HORIZONTAL);
		layoutEmail.setOrientation(LinearLayout.HORIZONTAL);


		ViewGroup.LayoutParams layoutParams2 = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
		addFriendLayout.setVisibility(View.GONE);

		final Spinner queryTypeSpin = new Spinner(this);
		ArrayList<String> typeList = new ArrayList<String>();
		typeList.add(getResources().getString(
				R.string.alert_find_onlinefriend));
		typeList.add(getResources().getString(
				R.string.alert_find_conditionfriend));
		queryTypeSpin
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
							nextPage.setClass(baseAct,
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
									refresh();
									if(xwDC!=null){
										refreshGroupName(xwDC.groupsInfo);
									}
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
									refresh();
								}
								// csFn=null;
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {// 删除好友
							try {
								if (csFn != null) {
									new AlertDialog.Builder(baseAct).setTitle(xechwic.android.XWCodeTrans.doTrans("确定要删除")+"?")
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
															refresh();
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




	@Override
	protected void onDestroy() {
		Log.e(TAG,"onDestroy");
		super.onDestroy();
		BusProvider.getInstance().unregister(this);

	}

    public void showDelToast(FriendNodeInfo nodeInfo){
		if(bIsFront){
			String name=nodeInfo.getSignName();
			if(TextUtils.isEmpty(name)){
				name=nodeInfo.getLogin_name();
			}
			showToastTips(""+name+ xechwic.android.XWCodeTrans.doTrans("跟你解除了好友关系!"));
			refresh();
		}
	}

	Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(bIsFront){
				if(msg.what==1){
					refresh();
				}
			}
		}
	};

	@Subscribe
	public void onAvatarUpdateEvent(AvatarUpdateEvent event){
		mHandler.removeMessages(1);
		mHandler.sendEmptyMessageDelayed(1,1000);
	}








}