package xechwic.android.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import ydx.securephone.R;
import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.bean.ChatHistoryBean;
import xechwic.android.util.Http;
import xechwic.android.util.MessageUtil;


public class MsgListAdapter extends BaseQuickAdapter<ChatHistoryBean> {

	public MsgListAdapter(List<ChatHistoryBean> listData, int layoutId) {
		super(layoutId, listData);
	}


	@Override
	protected void convert(BaseViewHolder helper, ChatHistoryBean fni) {
		if (fni != null) {
			String content = fni.getRecentChat();
			String text=MessageUtil.getNoticeMsg(content);
			if(XWDataCenter.xwDC!=null&&XWDataCenter.xwDC.parser!=null){
				helper.setText(R.id.tv_friend_content, XWDataCenter.xwDC.parser.replace(text, mContext));
			}else{
				helper.setText(R.id.tv_friend_content, text);
			}


			//设置头像
            String pic=fni.getIcon();
			ImageView icon=helper.getView(R.id.iv_friendhead);
			if(!TextUtils.isEmpty(pic)){
				if(pic.contains("+")){
					try {
						pic= URLEncoder.encode(pic, "gbk");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}

			}
			String iconPath=Http.getHeadPicUrl()+pic;
//			Log.e("iconPath","iconPath:"+iconPath);
			Glide.with(MainApplication.getInstance())
					.load(iconPath)
					.error(R.drawable.icon)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.into(icon);

			//设置未读信息
			int count=fni.getUnread();
			TextView tvTips=helper.getView(R.id.tips_count);
			if(count>0){
				tvTips.setVisibility(View.VISIBLE);
				String sCount=""+count;
				tvTips.setText(sCount);
			}else{
				tvTips.setVisibility(View.GONE);
			}
			String name=fni.getSignName();
			if(TextUtils.isEmpty(name)){
				name=fni.getLogin_name();
			}
			helper.setText(R.id.tv_friend_name,name);

		}
	}
}