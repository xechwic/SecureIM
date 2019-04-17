package xechwic.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ydx.securephone.R;
import xechwic.android.XWCodeTrans;
import xechwic.android.bean.ContactBean;
import xechwic.android.ui.PhoneContactUI;

/**
 * 手机联系人适配器
 *
 */
public class PhoneContactUIAdapter extends BaseAdapter {

	
	/////////////////////XechWic,2012-01-04, Null problem
	private List<ContactBean> peoples =null;
	private PhoneContactUI context=null;
	private LayoutInflater inflater;
	public PhoneContactUIAdapter(List<ContactBean> peoples,
								 PhoneContactUI context) {
		this.peoples = peoples;
		this.context = context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if (peoples!=null)
		    return peoples.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		if (peoples==null)
			return null;
		return peoples.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (peoples==null)
			return -1;
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		//////////////////XechWic
		if (peoples==null)
			return null;
		final ContactBean bean = peoples.get(position);
		ViewHolder holder = null ;
		if(null == convertView){
			convertView = inflater.inflate(R.layout.contactui_list_item, null);
			holder = new ViewHolder();
			holder.tv_name = (TextView) convertView.findViewById(R.id.contactui_phonename);
			holder.tv_number = (TextView) convertView.findViewById(R.id.contactui_number);
			holder.type=(TextView)convertView.findViewById(R.id.contactui_type);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String name =bean.getContactName();
		if(name==null||name.trim().length()<1){
			String number = bean.getContactPhone(); // null
			if(null == number || "".equals(number)){
				number = bean.getContactHomePhone();	
			}
			holder.tv_name.setText(""+number);
			holder.tv_number.setVisibility(View.GONE);
		}else{
			holder.tv_name.setText(""+name);
			String number = bean.getContactPhone(); // null
			if(null == number || "".equals(number)){
				number = bean.getContactHomePhone();	
			}
			holder.tv_number.setText(number);
			holder.tv_number.setVisibility(View.VISIBLE);
		}
		
		int type = bean.getType();
		if(type==1){
			holder.type.setText(XWCodeTrans.doTrans("邀请"));
			holder.type.setTextColor(context.getResources().getColor(R.color.green));
		}else if(type==-2){
			holder.type.setText(XWCodeTrans.doTrans("添加"));
			holder.type.setTextColor(context.getResources().getColor(R.color.blue));
		}else if(type==-1){
			holder.type.setText(XWCodeTrans.doTrans("已添加"));
			holder.type.setTextColor(context.getResources().getColor(R.color.orange));
		}
		
		return convertView;
	}
	 class ViewHolder{
		 TextView tv_name;
    	 TextView tv_number;
    	 TextView type;
	}

}
