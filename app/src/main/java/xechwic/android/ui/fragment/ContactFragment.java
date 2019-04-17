package xechwic.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ydx.securephone.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import xechwic.android.FriendControl;
import xechwic.android.FriendGroupInfo;
import xechwic.android.FriendNodeInfo;
import xechwic.android.XWDataCenter;
import xechwic.android.adapter.ExFriendAdapter;
import xechwic.android.base.BaseLazyFragment;
import xechwic.android.bean.GroupBean;
import xechwic.android.bus.event.FragmentRereshEvent;
import xechwic.android.ui.PersonalUI;
import xechwic.android.util.ComparatorUser;
import xechwic.android.view.expand.IphoneTreeView;

import static xechwic.android.FriendControl.openFriendDetail;

/**
 * 联系人界面
 */
public class ContactFragment extends BaseLazyFragment {

    private final String TAG="ContactFragment";

    @BindView(R.id.iphone_tree_view)
    IphoneTreeView friendExListView;//好友listView
	private ExFriendAdapter mAdapter;//分组适配器
	private List<GroupBean> mData=new ArrayList<>();
    public List<String> groupNameList=new ArrayList<>() ;// 大组成员名
    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_contact);
        ButterKnife.bind(this,getContentView());
        initView();
//        refresh();
    }

    private void initView() {
        initFriendList();
    }

    /////-1无展开，其他数字是展开组项
    public int getExpandIndex(){
      if(mAdapter!=null){
          return  mAdapter.getExpandIndex();
      }
        return -1;
    }


    private int defExpandIndex=-1;
    public void setDefExpandIndex(int expandIndex){
          defExpandIndex=expandIndex;
    }


    /**
     *初始好友
     */
	private void initFriendList(){
		mAdapter = new ExFriendAdapter(mActivity, mData, friendExListView);
		friendExListView.setAdapter(mAdapter);
		friendExListView.setGroupIndicator(null);
		friendExListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2,int arg3, long arg4) {
				FriendNodeInfo fni=mData.get(arg2).getChildList().get(arg3);
				if(fni!=null){
					if (fni.getLogin_name().equals(XWDataCenter.xwDC.loginName)) {
						// 个人资料
						Intent intentP = new Intent();
						intentP.setClass(mActivity, PersonalUI.class);
						startActivity(intentP);
					}else{
						openFriendDetail(fni.getLogin_name());
					}
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
            for(FriendNodeInfo info:FriendControl.friendList){
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

    boolean bIsFreshing=false;
    @Override
    protected void refresh(){
        Log.e(TAG,"refresh bIsFreshing=="+bIsFreshing);
        if(bIsFreshing){
            return;
        }
        bIsFreshing=true;
        if(mActivity!=null&&(mActivity instanceof FriendControl)){
            if(((FriendControl) mActivity).getCurIndex()==FriendControl.CONTACT_INDEX){
                Log.e("ContactFragment", "refresh");
                refreshGroupName(XWDataCenter.xwDC.groupsInfo);
                getGroupData(FriendControl.friendList);
                ((FriendControl) mActivity).updateFriendIcon(FriendControl.friendList);
                if(mAdapter!=null){
                    mAdapter.notifyDataSetChanged();
                }
                if(defExpandIndex>-1){
                    friendExListView.expandGroup(defExpandIndex);
                    mAdapter.onHeadViewClick(defExpandIndex,1);
                    defExpandIndex=-1;
                }
            }else{
                Log.e("ContactFragment", "not refresh,index=="+((FriendControl) mActivity).getCurIndex());
            }
        }else{

            if(mActivity!=null){
                Log.e(TAG,"mActivity =="+mActivity.getLocalClassName());
            }else{
                Log.e(TAG,"mActivity == null");
            }
        }
        bIsFreshing=false;
    }

    @Override
    protected void onPauseLazy() {
        super.onPauseLazy();
    }

    @Override
    public void onDestroyViewLazy() {
        super.onDestroyViewLazy();
    }

    @Subscribe
    public void onFragmentRereshEvent(FragmentRereshEvent event){
        if(event!=null&&event.type==FriendControl.CONTACT_INDEX){
//            if(isVisible()) {/////当前显示才刷新
            postRefresh();

//            }
        }

    }
}
