package xechwic.android.adapter;

import android.text.TextUtils;
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
import xechwic.android.FriendNodeInfo;
import xechwic.android.act.MainApplication;


public class FniListAdapter extends BaseQuickAdapter<FriendNodeInfo> {

	public FniListAdapter(List<FriendNodeInfo> listData, int layoutId) {
		super(layoutId, listData);
	}


	@Override
	protected void convert(BaseViewHolder helper, FriendNodeInfo fni) {
		if (fni != null) {

            helper.getView(R.id.tv_friend_content).setVisibility(View.GONE);
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
			Glide.with(MainApplication.getInstance())
					.load(pic)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.error(R.drawable.icon)
					.into(icon);

			TextView tvTips=helper.getView(R.id.tips_count);

			tvTips.setVisibility(View.GONE);

			String name=fni.getSignName();
			if(TextUtils.isEmpty(name)){
				name=fni.getLogin_name();
			}
			helper.setText(R.id.tv_friend_name,name);

		}
	}
}