package xechwic.android.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ydx.securephone.R;
import xechwic.android.bean.RecordBean;
import xechwic.android.util.ContactInfoService;
import xechwic.android.util.DateTimeUtil;

import static com.lzy.okgo.OkGo.getContext;


public class CallRecordAdapter extends BaseQuickAdapter<RecordBean> {

	public CallRecordAdapter(List<RecordBean> listData, int layoutId) {
		super(layoutId, listData);
	}


	@Override
	protected void convert(BaseViewHolder helper, RecordBean fni) {
		if (fni != null) {
				String contactName=fni.getContactName();
				if(TextUtils.isEmpty(contactName)){
					contactName=ContactInfoService.getInstance(getContext()).getContactNameFromPhoneBook(fni.getXwNumber());
				}
				String number = fni.getXwNumber(); // null
				if (TextUtils.isEmpty(number)) {
					number = fni.getContactHomePhone();
				}
			    if(TextUtils.isEmpty(contactName)){
					contactName=number;
				}
			    helper.setText(R.id.phonename,contactName);
				helper.setText(R.id.select_list_number,number);
				ImageView iv_head=helper.getView(R.id.contact_iv);

				Date mDate = new Date(fni.getDialStartTime());
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dstr=sdf.format(mDate);
				helper.setText(R.id.time,dstr);

			ImageView iv_flag=helper.getView(R.id.iv_record_flag);

				//未接通
				if(fni.getStartTime() == 0 || fni.getEndTime() == 0){
					helper.setText(R.id.call_time,xechwic.android.XWCodeTrans.doTrans("未接通"));
					iv_flag.setImageResource(R.drawable.ic_record_fail);
					///iv_head.setImageResource(R.drawable.ic_record_noactivi);
					///////////////2017-05-11,未接通，仍然要区分呼出还是呼入
					if(fni.isDial()){
						iv_head.setImageResource(R.drawable.ic_record_callout);
					}else{
						iv_head.setImageResource(R.drawable.ic_record_callin);
					}
				}else{
					long times = fni.getEndTime() - fni.getStartTime();
					helper.setText(R.id.call_time,xechwic.android.XWCodeTrans.doTrans("通话时长")+" "+ DateTimeUtil.secondsToString((int)times/1000));
			    iv_flag.setImageResource(R.drawable.ic_record_success);
					if(fni.isDial()){
						iv_head.setImageResource(R.drawable.ic_record_callout);
					}else{
						iv_head.setImageResource(R.drawable.ic_record_callin);
					}
					}


			}



	}
}