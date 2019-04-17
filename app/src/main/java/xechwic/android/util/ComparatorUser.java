package xechwic.android.util;

import java.util.Comparator;

import xechwic.android.FriendNodeInfo;

/**
 * 好友列表排序
 *
 */
public class ComparatorUser implements Comparator<Object>{

	/**
	 * //返回小于0，表示arg0小于arg1
	 * 等于0,表示arg0等于arg1
	 * 大于0,表示arg0大于arg1
	 * 默认排序从小到大
	 */
	@Override
	public int compare(Object arg0, Object arg1) {
		return ((FriendNodeInfo)arg1).getOnline_type()-((FriendNodeInfo)arg0).getOnline_type();

	}

}
