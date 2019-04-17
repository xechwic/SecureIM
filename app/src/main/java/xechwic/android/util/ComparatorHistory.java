package xechwic.android.util;

import java.util.Comparator;

import xechwic.android.bean.ChatHistoryBean;

/**
 * 聊天历史列表排序
 *
 */
public class ComparatorHistory implements Comparator<Object>{

	/**
	 * //返回小于0，表示arg0小于arg1
	 * 等于0,表示arg0等于arg1
	 * 大于0,表示arg0大于arg1
	 * 默认排序从小到大
	 */
	@Override
	public int compare(Object arg1, Object arg2) {
		String str1=((ChatHistoryBean)arg1).getLastTime();
		String str2=((ChatHistoryBean)arg2).getLastTime();
		if(str1==null||str2==null){
			return 0;
		}
		long l1=Long.parseLong(str1);
		long l2=Long.parseLong(str2);
		long result=l2-l1;
		int com;
		if(result>0){
			com=1;
		}else if(result==0){
			com=0;
		}else{
			com=-1;
		}
		return com;

	}

}
