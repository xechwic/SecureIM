
package xechwic.android.ui;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import ydx.securephone.R;
import xechwic.android.act.MainApplication;
import xechwic.android.util.JRSConstants;
import xechwic.android.view.photoview.PhotoView;
import xechwic.android.view.photoview.PhotoViewAttacher;
import xechwic.android.view.photoview.PhotoViewAttacher.OnPhotoTapListener;
/**
 * 图片展示
 */
public class PhotoViewUI extends BaseUI {
	private String TAG = PhotoViewUI.class.getSimpleName();

	PhotoView iv_photoview;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		setContentView(R.layout.ui_photoview);

		initView();
        getIntentData();

	}


    private void initView(){
    	iv_photoview=(PhotoView)findViewById(R.id.iv_photoview);
    	
    	PhotoViewAttacher mAttacher = new PhotoViewAttacher(iv_photoview);  
    	  
    	mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
			
			@Override
			public void onPhotoTap(View view, float x, float y) {
				baseAct.finish();
				
			}
		});

		mAttacher.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				Log.e(TAG,"onlongclick");
				return false;
			}
		});
		
		
    }

    

    private void getIntentData(){
    	Intent intent=getIntent();
    	if(intent!=null&&intent.getExtras()!=null){
    		Bundle data=intent.getExtras();
    		if(data.containsKey(JRSConstants.DATA)){
    			String url=data.getString(JRSConstants.DATA);
    			if(url!=null&&url.trim().length()>0){
					Glide.with(MainApplication.getInstance())
							.load(url)
							.into((new GlideDrawableImageViewTarget(iv_photoview) {
								@Override
								public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
									super.onResourceReady(drawable, anim);

								}

								@Override
								public void onLoadFailed(Exception e, Drawable errorDrawable) {
									super.onLoadFailed(e, errorDrawable);
									showToastTips("open fail");
									PhotoViewUI.this.finish();
								}
							}));
    			}
    		}
    	}
    }


}
