package xechwic.android.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import ydx.securephone.R;
import xechwic.android.FriendNodeInfo;
import xechwic.android.XWCodeTrans;
import xechwic.android.act.MainApplication;
import xechwic.android.bean.GroupBean;
import xechwic.android.util.Http;
import xechwic.android.view.CircleImageView;
import xechwic.android.view.expand.IphoneTreeView;

public class ExFriendAdapter extends BaseExpandableListAdapter implements
		IphoneTreeView.IphoneTreeHeaderAdapter {

	private Context mContext;
	private List<GroupBean> groupList;
	private IphoneTreeView mIphoneTreeView;
	private HashMap<Integer, Integer> groupStatusMap;

	@SuppressLint("UseSparseArrays")
	public ExFriendAdapter(Context context, List<GroupBean> groupList, IphoneTreeView mIphoneTreeView) {
		this.mContext = context;
		this.groupList = groupList;
		this.mIphoneTreeView = mIphoneTreeView;
		groupStatusMap = new HashMap<>();
	}

	public FriendNodeInfo getChild(int groupPosition, int childPosition) {
		if(groupList.size()>groupPosition){
			return groupList.get(groupPosition).getChildList().get(childPosition);
		}else{
			return null;
		}

	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	/**
	 * 分组中的总人数
	 * @param groupPosition
	 * @return
	 */
	public int getChildrenCount(int groupPosition) {
		if(groupList.size()>groupPosition){
			return groupList.get(groupPosition).getChildList().size();
		}else{
			return 0;
		}

	}
	
	/**
	 * 分组中的在线人数
	 * @param groupPosition
	 * @return
	 */
	private int getChildrenOnlineCount(int groupPosition) {
		int count=0;
		if(groupList.size()>groupPosition){
			List<FriendNodeInfo> childList=groupList.get(groupPosition).getChildList();
			for(int i=0;i<childList.size();i++){
				FriendNodeInfo child=childList.get(i);
				String status=child.getOnline_status();
				if(!TextUtils.isEmpty(status)&&!status.equals(XWCodeTrans.doTrans("断开"))){
					count++;
				}
			}
		}
		return count;
	}

	public Object getGroup(int groupPosition) {
		return groupList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupList.size();
	}
    @Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}

	/**
	 * Child
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
		ChildHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_ex_child, null);
			holder = new ChildHolder();
			holder.nameView = (TextView) convertView.findViewById(R.id.tv_name);//昵称
			holder.onLineView = (TextView) convertView.findViewById(R.id.tv_status);//心情
			holder.iconView = (CircleImageView) convertView.findViewById(R.id.icon);//头像
			convertView.setTag(holder);
		} else {
			holder = (ChildHolder) convertView.getTag();
		}
		final FriendNodeInfo fni=getChild(groupPosition, childPosition);
		setupView(holder,fni);
		return convertView;
	}


	private void setupView(final ChildHolder holder,final FriendNodeInfo fni){
		 String name = TextUtils.isEmpty(fni.getSignName())?fni.getLogin_name():fni.getSignName();
		holder.nameView.setText(name);
		final String status = fni.getOnline_status()==null? XWCodeTrans.doTrans("断开"):fni.getOnline_status();
		holder.onLineView.setText(status);
		String pic=fni.getIcon();
		if(!TextUtils.isEmpty(pic)){
			if(pic.contains("+")){
				try {
					pic= URLEncoder.encode(pic, "gbk");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
            String avatarUrl=Http.getHeadPicUrl()+pic;
//			Log.e("main","avatar:"+avatarUrl);
			Glide.with(MainApplication.getInstance())
					.load(avatarUrl)
					.error(R.drawable.icon)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.into((new GlideDrawableImageViewTarget(holder.iconView) {
						@Override
						public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
							super.onResourceReady(drawable, anim);
							onlineDisplay(holder.iconView,status);
						}
					}));


		}else{
			holder.iconView.setImageResource(R.drawable.icon);
			onlineDisplay(holder.iconView,status);
		}


	}


	/**
	 *  在线、离线显示
	 */
	private void onlineDisplay(ImageView icon,String status){
		if (XWCodeTrans.doTrans("断开").equals(status)){

			if (Build.VERSION.SDK_INT>=11)
			{
				try
				{
					icon.setAlpha(0.5f);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				///////////////2014-08-06
				try
				{
					icon.setColorFilter(Color.GRAY,android.graphics.PorterDuff.Mode.DARKEN);

				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}else{

			if (Build.VERSION.SDK_INT>=11)
			{
				try
				{
					icon.setAlpha(1.0f);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				try
				{
					icon.clearColorFilter();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}

		}
	}
	/**
	 * Group
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,View convertView, ViewGroup parent) {
		GroupHolder holder ;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_ex_group, null);
			holder = new GroupHolder();
			holder.nameView = (TextView) convertView.findViewById(R.id.group_name);
			holder.onLineView = (TextView) convertView.findViewById(R.id.online_count);
			holder.iconView = (ImageView) convertView.findViewById(R.id.group_indicator);
			convertView.setTag(holder);
		} else {
			holder = (GroupHolder) convertView.getTag();
		}
		holder.nameView.setText(groupList.get(groupPosition).getGroupName());
		holder.onLineView.setText(getChildrenOnlineCount(groupPosition) + "/"+ getChildrenCount(groupPosition));
		if (isExpanded) {
			holder.iconView.setImageResource(R.drawable.qb_down);
		} else {
			holder.iconView.setImageResource(R.drawable.qb_right);
		}
		return convertView;
	}

	@Override
	public int getTreeHeaderState(int groupPosition, int childPosition) {
		if(groupPosition>=0){
			final int childCount = getChildrenCount(groupPosition);
			if (childPosition == childCount - 1) {
				//mSearchView.setVisibility(View.GONE);
				return PINNED_HEADER_PUSHED_UP;
			} else if (childPosition == -1&& !mIphoneTreeView.isGroupExpanded(groupPosition)) {
				//mSearchView.setVisibility(View.VISIBLE);
				return PINNED_HEADER_GONE;
			} else {
				//mSearchView.setVisibility(View.GONE);
				return PINNED_HEADER_VISIBLE;
			}
		}else{
			return PINNED_HEADER_VISIBLE;
		}

	}

	@Override
	public void configureTreeHeader(View header, int groupPosition,int childPosition, int alpha) {
		((TextView) header.findViewById(R.id.group_name)).setText(groupList.get(groupPosition).getGroupName());//组名
		((TextView) header.findViewById(R.id.online_count)).setText(getChildrenOnlineCount(groupPosition) + "/"+ getChildrenCount(groupPosition));//好友上线比例
	}

	@Override
	public void onHeadViewClick(int groupPosition, int status) {
		groupStatusMap.put(groupPosition, status);
		////组项点击
		Log.e("onHeadViewClick","onHeadViewClick:"+groupPosition+","+status);
	}

	/////-1无展开，其他数字是展开组项
	public int getExpandIndex(){
		for (Integer key : groupStatusMap.keySet()) {
			if(groupStatusMap.get(key)==1){
				return key;
			}
		}
		return -1;
	}

	@Override
	public int getHeadViewClickStatus(int groupPosition) {
		if (groupStatusMap.containsKey(groupPosition)) {
			return groupStatusMap.get(groupPosition);
		} else {
			return 0;
		}
	}



	private class GroupHolder {
		TextView nameView;
		TextView onLineView;
		ImageView iconView;
	}

	private class ChildHolder {
		TextView nameView;
		TextView onLineView;
		CircleImageView iconView;
	}


}
