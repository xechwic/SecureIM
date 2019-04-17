package xechwic.android;

import java.util.Comparator;

public class PreviewFrameRateComparator implements Comparator{

	public int compare(Object object1, Object object2) {
		// TODO Auto-generated method stub
		return ((Integer)object1).intValue()-((Integer)object2).intValue();
	}

}
