package xechwic.android.util;

import java.util.Comparator;

import xechwic.android.bean.ContactBean;

/**
 * 聊天历史列表排序
 *
 */
public class ComparatorContact implements Comparator<Object>{

	/**
	 * //返回小于0，表示arg0小于arg1
	 * 等于0,表示arg0等于arg1
	 * 大于0,表示arg0大于arg1
	 * 默认排序从小到大
	 */
	@Override
	public int compare(Object arg1, Object arg2) {
		
		int int1=((ContactBean)arg1).getType();
		int int2=((ContactBean)arg2).getType();
		
		if (int2>int1)
			return -1;
		else if (int2<int1)
			return 1;
		else
		{
			if (((ContactBean)arg1).getContactName()==null)
			{
				return 1;
			}
			else if (((ContactBean)arg2).getContactName()==null)
			{
				return -1;
			}
			else
				return (((ContactBean)arg1).getContactName().compareTo(((ContactBean)arg2).getContactName()));
				
		}

		/*
		int result=int2-int1;
		int com;
		if(result>0){
			com=1;
		}else if(result==0){
			com=0;
		}else{
			com=-1;
		}*/
		///return com;

	}

}
