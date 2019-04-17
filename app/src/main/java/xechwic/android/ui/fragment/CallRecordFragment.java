package xechwic.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import android.widget.TextView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import ydx.securephone.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import xechwic.android.FriendCall;
import xechwic.android.FriendControl;
import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.adapter.CallRecordAdapter;
import xechwic.android.base.BaseLazyFragment;
import xechwic.android.bean.RecordBean;
import xechwic.android.bus.event.FragmentRereshEvent;
import xechwic.android.sqlite.FriendNodeDB;
import xechwic.android.support.SupportRecyclerView;
import xechwic.android.ui.BaseUI;

import static xechwic.android.FriendControl.CALLRECORD_INDEX;

/**
 * 电话记录界面
 */
public class CallRecordFragment extends BaseLazyFragment {


    @BindView(R.id.recyclerview)
    SupportRecyclerView recyclerView;
    @BindView(R.id.emptyView)
    View emptyView;

    private CallRecordAdapter adapter;
    private List<RecordBean> data=new ArrayList<>();

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_normal_recyclerview);
        ButterKnife.bind(this,getContentView());
        initView();
//        refresh();
    }

    private void initView() {

        adapter = new CallRecordAdapter(data,R.layout.record_list_item);
        adapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(data!=null&&!data.isEmpty()){
                    if(data.size()>position){
                        final RecordBean bean = data.get(position);

                        String number = bean.getXwNumber(); // null
                        if (null == number || "".equals(number)) {
                            number = bean.getContactPhone(); // null
                            if (null == number || "".equals(number)) {
                                number = bean.getContactHomePhone();
                            }
                        }

                        Log.e("XIM","RecordView onItemClick position"+position +"  number"+number);
                        boolean bOK=((BaseUI)mActivity).makeCallCheck(FriendNodeDB.getAFriend(XWDataCenter.getCurAccount(),number));
                        if(bOK) {
                            Intent intent = new Intent(mActivity, FriendCall.class);
                            intent.putExtra("phone_number", number);
                            intent.putExtra("tag", "1");
                            mActivity.startActivity(intent);
                        }

                    }

                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    @Override
    protected void refresh(){
        try
        {
            if(mActivity!=null&&(mActivity instanceof FriendControl)) {
                if (((FriendControl) mActivity).getCurIndex() == FriendControl.CALLRECORD_INDEX) {
                    Log.e("CallRecordFragment", "refresh");
                    data.clear();
                    if(com.example.mcryptolmsdimpl_demo.MainActivity.CheckSDCard(mActivity)){//SD检测通过
                        data.addAll(MainApplication.getInstance().getRecordList());
                    }
                    if(adapter!=null)
                        adapter.notifyDataSetChanged();
                }
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
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
        if(event!=null&&event.type==CALLRECORD_INDEX){
//            if(isVisible()) {/////当前显示才刷新
            postRefresh();

//            }

        }

    }
}
