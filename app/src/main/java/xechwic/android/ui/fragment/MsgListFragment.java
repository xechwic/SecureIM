package xechwic.android.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ydx.securephone.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import xechwic.android.FriendControl;
import xechwic.android.FriendNodeInfo;
import xechwic.android.XWDataCenter;
import xechwic.android.adapter.MsgListAdapter;
import xechwic.android.base.BaseLazyFragment;
import xechwic.android.bean.ChatHistoryBean;
import xechwic.android.bus.event.FragmentRereshEvent;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.support.SupportRecyclerView;
import xechwic.android.util.ComparatorHistory;
import xechwic.android.util.GsonUtil;



/**
 * 聊天消息列表
 */
public class MsgListFragment extends BaseLazyFragment {

    private final String TAG="MsgListFragment";
    @BindView(R.id.recyclerview)
    SupportRecyclerView recyclerView;
    @BindView(R.id.emptyView)
    View emptyView;
    MsgListAdapter adapter;
    private List<ChatHistoryBean> data=new ArrayList<>();

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_normal_recyclerview);
        ButterKnife.bind(this,getContentView());
        initView();
//        refresh();
    }

    private void initView() {
        adapter = new MsgListAdapter(data,R.layout.friend_item);
        adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(data!=null&&!data.isEmpty()){
                    if(data.size()>position){
                        ChatHistoryBean chatNode = data.get(position);
                        if(chatNode!=null){
                            FriendNodeInfo node = FriendNodeDB.getAFriend(XWDataCenter.getCurAccount(), chatNode.getLogin_name());
                            FriendControl.directToChatRecord(node);
                        }

                    }

                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * 刷新聊天记录
     */
	private boolean bIsRefreshingChat=false;
    /**
     * 历史聊天加密记录
     */
    private List<String> getChatHistoryData(){
        return    XWDataCenter.getChatHistoryDB().getAllHistorys(XWDataCenter.getCurAccount());
    }
	public void refreshChatAdapter(){
		if(bIsRefreshingChat){
			return;
		}
		new AsyncTask<String,Integer,List<ChatHistoryBean>>(){
			@Override
			protected void onPreExecute() {
				bIsRefreshingChat=true;
			}

			@Override
			protected List<ChatHistoryBean> doInBackground(String... strings) {
                Log.e(TAG,"load data doInBackground");
				List<ChatHistoryBean> list=new ArrayList<>();
				try{
					List<String> strlist =getChatHistoryData();
					if(strlist!=null&&strlist.size()>0){
						for(String message:strlist){
							if(!TextUtils.isEmpty(message)){
								try {
                                    Log.e("msg","decrypt before:"+message);
									String strText=new String(com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_aes(message.getBytes()));
									if(!TextUtils.isEmpty(strText)){
                                        Log.e("msg","decrypt after:"+strText);
										ChatHistoryBean entity = GsonUtil.GsonToBean(strText, ChatHistoryBean.class);
										list.add(entity);
									}
								}catch (Exception e){
									e.printStackTrace();
								}

							}

						}
					}

				}catch (Exception e){
					e.printStackTrace();
				}
				return list;
			}

			@Override
			protected void onPostExecute(List<ChatHistoryBean> result) {
				bIsRefreshingChat=false;
				if(isVisible()){
					data.clear();
					if(result!=null&&result.size()>0){
                        data.addAll(result);
						//排序
						ComparatorHistory comparator = new ComparatorHistory();
						Collections.sort(data, comparator);
						//更新头像
						updateFriendIcon(data);
					}
					adapter.notifyDataSetChanged();
				}
			}
		}.execute("");
	}
    /**更新聊天历史记录头像
     */
    public void updateFriendIcon(List<ChatHistoryBean> friendList){
        if(friendList==null||friendList.isEmpty()||XWDataCenter.headBeanMap==null||XWDataCenter.headBeanMap.isEmpty()){
            return;
        }
        for(ChatHistoryBean node:friendList){
            if(node!=null&&node.getLogin_name()!=null){
                String icon =XWDataCenter.headBeanMap.get(node.getLogin_name());
                if(!TextUtils.isEmpty(icon)){
                    node.setIcon(icon);
                }
            }
        }

    }

    @Override
    protected void refresh(){
        if(mActivity!=null&&(mActivity instanceof FriendControl)) {
            if (((FriendControl) mActivity).getCurIndex() == FriendControl.MSGLIST_INDEX) {
                Log.e("MsgListFragment", "refresh");
                refreshChatAdapter();
            }
        }
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
        Log.e(TAG,"onFragmentRereshEvent:"+event);
        if(event!=null&&event.type==FriendControl.MSGLIST_INDEX){
//            if(isVisible()) {/////当前显示才刷新,当界面重启时初始化时可能出现is not visible
              postRefresh();
            }
//        }

    }
}
