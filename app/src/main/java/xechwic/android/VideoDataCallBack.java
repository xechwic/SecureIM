package xechwic.android;
import android.hardware.Camera;

import java.nio.ByteBuffer;

public class VideoDataCallBack implements Camera.PreviewCallback{

	private long oldTime=0;
	private int spaceing=0;

	private long lLastCost=0;
	private long lLastOver=0;

	private ByteBuffer videodata=null;
	

	
	
	public VideoDataCallBack(){
		int fps;
		try{
			fps=XWDataCenter.xw_video_fps;
		}catch(Exception e){
			fps=XWDataCenter.xw_video_fps;
		}
		if (fps<=0)
			fps=1;
		
		spaceing=1000/fps;
		
		if (fps/*XWDataCenter.xwDC.CPUFrequency*/<=1)
		{
			spaceing=500;			
		}

    }

	private boolean bIsInProcess=false;

	@Override
    public void onPreviewFrame(byte[] data,Camera camera){
		try {
			if (bIsInProcess)
				return;
			bIsInProcess=true;
			/////////////2014-06-30,只处理nv21
			if (XWDataCenter.video_preferred_format != 17)
				return;

			if (data.length != XWDataCenter.video_preferred_width * XWDataCenter.video_preferred_height * 3 / 2) {
				android.util.Log.e("XIM", "onPreviewFrame error video data.length" + data.length);
				return;
			}

			if ((videodata == null) || (videodata.capacity() < data.length)) {
				videodata = ByteBuffer.allocateDirect(data.length);
			}
			if (videodata == null)
				return;
			if (!XWDataCenter.video_is_open) return;
			try {
				if (!XWDataCenter.xwDC.cameraRunning) {
					return;
				}
				long currentTime = System.currentTimeMillis();
				//long tmp=(currentTime-oldTime);
				//Log.v("tag", "时间差:"+tmp+" "+spaceing);
				//////////////////满足时间差!!!!!!!!!!!!!!!!!!!!!!!
				if ((currentTime - oldTime >= (spaceing)) /*&& (currentTime - lLastOver > lLastCost * 2)*/) {

					{
						videodata.position(0);
						videodata.put(data);
					}

					//////竖屏前置摄像头
					if (XWDataCenter.bIsVerticalScreen) {
						XWDataCenter.xwDC.XWNetphoneSetIsRotate(1);
					} else {
						XWDataCenter.xwDC.XWNetphoneSetIsRotate(0);
					}


					///android.util.Log.v("XIM","fvd.xwDC.sendVideoData 1");
					XWDataCenter.xwDC.sendVideoData(videodata, data.length);
					//////android.util.Log.v("XIM","fvd.xwDC.sendVideoData 2");
					oldTime = currentTime;

					/////////////计算本次videodata编码时间
					lLastOver = System.currentTimeMillis();
					lLastCost = lLastOver - currentTime;

					////android.util.Log.v("XIM","Video code cost:"+lLastCost);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		finally
		{
			bIsInProcess=false;
		}
    }
    
    
    

    
    


}
