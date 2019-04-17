package xechwic.android;

import java.util.Comparator;

import android.hardware.Camera.Size;

public class PreviewSizeComparator implements Comparator{

	public int compare(Object object1, Object object2) {
		// TODO Auto-generated method stub
		Size s1=(Size)object1;
		Size s2=(Size)object2;
		return s1.height*s1.width-s2.height*s2.width;
	}

}
