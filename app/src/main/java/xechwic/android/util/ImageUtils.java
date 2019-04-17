package xechwic.android.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import xechwic.android.util.Http.OnDealConnection;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.Build;

/**
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public final class ImageUtils {

	/** 尝试打开图片次数 **/
	private static final int MAX_TRY_OPEN_IMAGE = 5;


	/** 
	 * 根据指定的图像路径和大小来获取缩略图 
	 * 此方法有两点好处： 
	 *     1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度， 
	 *        第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 
	 *     2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 
	 *        用这个工具生成的图像不会被拉伸。 
	 * @param imagePath 图像的路径 
	 * @param width 指定输出图像的宽度 
	 * @param height 指定输出图像的高度 
	 * @return 生成的缩略图 
	 */  
	public static Bitmap getImageThumbnail(String imagePath, int width, int height) {  
		Bitmap bitmap = null;  
		if(imagePath==null||imagePath.length()<1){
			return bitmap;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();  
		options.inJustDecodeBounds = true;  
		// 获取这个图片的宽和高，注意此处的bitmap为null  
		bitmap = BitmapFactory.decodeFile(imagePath, options);  
		options.inJustDecodeBounds = false; // 设为 false  
		// 计算缩放比  
		int h = options.outHeight;  
		int w = options.outWidth;  
		int beWidth = w / width;  
		int beHeight = h / height;  
		int be = 1;  
		if (beWidth < beHeight) {  
			be = beWidth;  
		} else {  
			be = beHeight;  
		}  
		if (be <= 0) {  
			be = 1;  
		}  
		options.inSampleSize = be;  
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false  
		bitmap = BitmapFactory.decodeFile(imagePath, options);  
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,  
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);  
		return bitmap;  
	}  

	/**
	 * 下载文件到指定路径
	 * 
	 * @param context
	 * @param urlStr
	 * @param absPath
	 * @param http
	 *            : 可通过http.setLetGo(false)停止处理流
	 * @return
	 */
	public static boolean downFile(Context context, final String urlStr,
			final String absPath, final Http http) {
		// 后缀
		final String suffix = "t";
		final File file = new File(absPath + suffix);

		FileUtil.openOrCreatDir(file.getParent());

		http.get(context, urlStr, null, new OnDealConnection() {
			@Override
			public void dealConnection(InputStream is) {
				try {
					final int BUFSIZE = 1024 * 4;
					byte[] buffer = new byte[BUFSIZE];

					OutputStream output = new FileOutputStream(file);
					int readed = 0;

					while ((readed = is.read(buffer)) != -1) {
						output.write(buffer, 0, readed);
					}
					output.flush();
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		if (200 == http.getRespondCode()) {
			return file.renameTo(new File(absPath));
		} else {
			file.delete();
			return false;
		}
	}

	/**
	 * 通过路径获取图片，屏蔽掉oom，并且获取图片进行oom重试机制
	 * 
	 * @param pathFile
	 * @param maxLength 期望大小，（长X宽）
	 * @return [参数说明]
	 * @return Bitmap [返回类型说明]
	 * @exception throws [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static Bitmap getBitmap(String pathFile, int maxLength) {
		if (pathFile==null || !FileUtil.isFileExist(pathFile)) {
			//            LogUtil.e(TAG, "不能获取到bitmap,pathFile=" + pathFile);
			//        	File file = new File(pathFile);
			return null;
		}

		BitmapFactory.Options option = new BitmapFactory.Options();

		option.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(pathFile, option);

		option.inJustDecodeBounds = false;

		//获取压缩值
		option.inSampleSize = computeSampleSize(option, -1, maxLength);

		Bitmap bitmap = null;

		//重试次数
		int tryCount = 1;

		//        LogUtil.d(TAG, "获取bitmap，pathFile=" + pathFile);

		do {
			if (tryCount > 1) {
				if (option.inSampleSize < 1) {
					option.inSampleSize = 1;
				}

				option.inSampleSize *= tryCount;
			}

			bitmap = getBitmap(pathFile, option);

			tryCount++;

			//            LogUtil.d(TAG, "尝试打开图片次数，tryCount=" + tryCount + ",压缩大小=" + option.inSampleSize);
		} while (bitmap == null && tryCount < MAX_TRY_OPEN_IMAGE);

		return bitmap;
	}

	private static Bitmap getBitmap(String pathFile, BitmapFactory.Options option) {
		Bitmap bitmap = null;
		if (pathFile!=null) {
			InputStream stream = null;
			try {
				stream = new FileInputStream(pathFile);

				bitmap = BitmapFactory.decodeStream(stream, null, option);
			} catch (FileNotFoundException e) {
				//                LogUtil.e(TAG, "没有文件，pathFile=" + pathFile, e);
			} catch (OutOfMemoryError oom) {
				long length = -1;
				try {
					length = stream != null ? stream.available() : -1;
				} catch (IOException e) {
					//                    LogUtil.e(TAG, e.toString(), e);
				}

				//                LogUtil.e(TAG, "获取图片内存溢出，option=" + option.inSampleSize + ",length=" + length);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						//                         LogUtil.e(TAG, "close InputStream is Error", e);
					}
				}
			}
		}

		return bitmap;
	}

	/**
	 * android源码提供的计算inSampleSize方法
	 * 
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return [参数说明]
	 * @return int [返回类型说明]
	 * @exception throws [违例类型] [违例说明]
	 * @see [类、类#方法、类#成员]
	 */
	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

		int roundedSize;

		if (initialSize <= 8) {
			roundedSize = 1;

			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}

		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;

		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int)Math.ceil(Math.sqrt(w * h / maxNumOfPixels));

		int upperBound = (minSideLength == -1) ? 128 : (int)Math.min(Math.floor(w / minSideLength), Math.floor(h
				/ minSideLength));

		if (upperBound < lowerBound) {

			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}


	/**图片变灰
	 * @param bitmap
	 * @return
	 */
	public static final Bitmap grey(Bitmap bitmap) {
		if(bitmap==null){
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Bitmap faceIconGreyBitmap = Bitmap
				.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(faceIconGreyBitmap);
		Paint paint = new Paint();
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);
		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
				colorMatrix);
		paint.setColorFilter(colorMatrixFilter);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return faceIconGreyBitmap;
	}

	//    从资源中获取Bitmap
	public static Bitmap getBmFromRes(Context context,int id){
		Resources res=context.getResources();
		Bitmap bmp=BitmapFactory.decodeResource(res, id);
		return bmp;
	}

	//bitmap转成数组
	public static byte[] Bitmap2Bytes(Bitmap bm){

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    bm.compress(Bitmap.CompressFormat.PNG, 100, baos);

	    return baos.toByteArray();   
	    }
	
	
	/** 
     * 根据一个网络连接(URL)获取bitmap图像 
     *  
     * @param imageUri 
     * @return 
     */  
    public static Bitmap getusericon(URL imageUri) {  
        // 显示网络上的图片  
        URL myFileUrl = imageUri;  
        Bitmap bitmap = null;  
        try {  
            HttpURLConnection conn = (HttpURLConnection) myFileUrl  
                    .openConnection();  
            conn.setDoInput(true);  
            conn.connect();  
            InputStream is = conn.getInputStream();  
            bitmap = BitmapFactory.decodeStream(is);  
            is.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return bitmap;  
    }  
}
