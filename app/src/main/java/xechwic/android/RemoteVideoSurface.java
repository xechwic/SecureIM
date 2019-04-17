package xechwic.android;

import ydx.securephone.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class RemoteVideoSurface extends SurfaceView implements Callback{

	final String TAG="RemoteVideoSurface";
	
	//public Matrix m;
	public SurfaceHolder holder;
	public Rect destRect;
	public FriendVideoDisplay fvd;
	public RemoteVideoSurface(Context context) {
		super(context);
		fvd=(FriendVideoDisplay)context;
		// TODO Auto-generated constructor stub
		this.holder=this.getHolder();
		holder.addCallback(this);
		destRect=new Rect();
		destRect.left=0;
		destRect.top=0;
		destRect.right=XWDataCenter.video_preferred_width;
		destRect.bottom=XWDataCenter.video_preferred_height;
		//m=new Matrix();  
	}
	public void drawBMP(byte []data/*,BitmapFactory.Options opts*/,int iLength){
		
        ///////synchronized(holder)
		Log.e("RemoteVideoSurface","drawBMP 1");
		
        {
        	Canvas canvas=null;
			try
			{
			    if (!holder.getSurface().isValid()) return; 
					
				canvas=holder.lockCanvas();
				if(canvas==null) return;
				
				try
				{
				//can.drawBitmap(BitmapFactory.decodeByteArray(data,0,data.length,opts), m, null);
				canvas.drawBitmap(BitmapFactory.decodeByteArray(data,0,iLength,null), null, destRect, null);

				}
				catch(Exception ex3)
				{
					ex3.printStackTrace();
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			finally
			{
				if (canvas!=null)
				    holder.unlockCanvasAndPost(canvas);				
			}
        }
		Log.e("RemoteVideoSurface","drawBMP 2");
	}
	
	
	public void DrawDefaultBMP()
	{
		{
			
			try
			{
				Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.ic_contact_picture_180_holo_dark); 
				
				///bmp.
				////drawRemoteData(Bitmap_To_Bytes(bmp));
				///Log.v(TAG,"bmp "+ bmp + " " +remoteVideo + " "+remoteVideo.holder+" "+remoteVideo.destRect );
				
				////if ((bmp!=null)&&(remoteVideo!=null)&&(remoteVideo.holder!=null)&&(remoteVideo.destRect!=null))
				{
					Canvas canvas=holder.lockCanvas();
					if(canvas==null)
					{
						Log.v(TAG,"canvas==null" );
						return;
					}
					
					try
					{
					   /// can.drawBitmap(bmp, m, null);
					   canvas.drawBitmap(bmp, null, destRect, null);
					   
					   Log.e(TAG,"canvas.drawBitmap(bmp, null, remoteVideo.destRect, null)");
					}
					finally
					{
					    holder.unlockCanvasAndPost(canvas);
					}
				}
			}
			catch (Exception e1)
			{
               e1.printStackTrace();
			}
		}

		return;
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height){
		// TODO Auto-generated method stub
		destRect.right=width;
		destRect.bottom=height;
//		Log.v("tag", "***************:"+width);
//		Log.v("tag", "***************:"+height);
		
		this.holder=this.getHolder();
		holder.addCallback(this);
		
		DrawDefaultBMP();
	}
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub	
		
		this.holder=this.getHolder();
		holder.addCallback(this);
		
		DrawDefaultBMP();
		
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}
}
