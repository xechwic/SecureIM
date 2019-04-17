package xechwic.android.crop;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CropUtil {
	private static String TAG=CropUtil.class.getSimpleName();
	
	/** 从媒体库选择图片 */
	public static final int PHOTO_PICKED_WITH_DATA = 0x10;
	/** 剪切图片 */
	public static final int PHOTO_CROP_DATA = 0x11;
	
	/** 打开相机 */
	public static final int TAKE_PHOTO_WITH_DATA = 0x12;
	
	/**打开文件浏览器*/
    public static final int OPEN_FILE_BROWSER_DATA=0x13;
	//////获取文件目录
	public static final int REQ_FILEDIR=0x14;
	
    
	

	/**
	 */
	public static byte[] compressPhotoByte(Bitmap b, int len, int maxSize) {
		int w = b.getWidth();
		int h = b.getHeight();
		float s;
		if (w < len && h < len) {
			s = 1;
		}
		if (w > h) {
			s = (float) len / w;
		} else {
			s = (float) len / h;
		}
		if(s>1){
			s = 1;
		}
		Matrix matrix = new Matrix();
		matrix.postScale(s, s);
		// 压缩图片
		Bitmap newB = Bitmap.createBitmap(b, 0, 0, w, h, matrix, false);
		// 将压缩后的图片转换为字节数组，如果字节数组大小超过200K，继续压缩
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int qt = 70;
		newB.compress(CompressFormat.JPEG, qt, bos);
		int size = bos.size();
		while (qt != 0 && size > maxSize) {
			if (qt < 0)
				qt = 0;
			bos.reset();
			newB.compress(CompressFormat.JPEG, qt, bos);
			size = bos.size();
			qt -= 10;
		}
		return bos.toByteArray();
	}

	/**
	 * 关闭IO流
	 * 
	 * @param in
	 * @param out
	 */
	public static void closeIO(InputStream in, OutputStream out) {
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 缓存图片到本地存储卡

	 *            ：文件名称
	 */
	public static File makeTempFile(Bitmap photo, String nameKey) {
		// 判断是否有存储卡
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED))
			throw new RuntimeException(xechwic.android.XWCodeTrans.doTrans("没有存储卡"));
		// 等比例压缩图片，将较长的一边压缩到600px一下，最大容量不超过200K
		byte[] tempData = CropUtil.compressPhotoByte(photo, 600, 200 * 1024);
		// 将压缩后的图片缓存到存储卡根目录下（权限）
		File bFile = new File(Environment.getExternalStorageDirectory(),
				nameKey);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(bFile);
			fos.write(tempData);
			fos.flush();
			if (bFile.exists() && bFile.length() > 0)
				return bFile;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CropUtil.closeIO(null, fos);
		}
		return null;
	}

	/**
	 * 缓存图片到本地存储卡

	 */
	public static File makeTempFile(Bitmap photo, String path, int size) {
		if(photo==null||path==null){
			return null;
		}
		// 判断是否有存储卡
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED))
			throw new RuntimeException(xechwic.android.XWCodeTrans.doTrans("没有存储卡"));

		/**
		 * 文件初始化
		 */
		File bFile = new File(path);
		if (bFile.isDirectory()) {
			bFile.delete();
		}
		if (!bFile.isFile()) {
			bFile.getParentFile().mkdirs();// 创建目录
			try {
				bFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// 创建文件
		}

		// 等比例压缩图片，将较长的一边压缩到600px一下，最大容量不超过200K
		byte[] tempData = CropUtil.compressPhotoByte(photo, 600, size);
		// 将压缩后的图片缓存到存储卡根目录下（权限）

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(bFile);
			fos.write(tempData);
			fos.flush();
			if (bFile.exists() && bFile.length() > 0){
				Log.e(TAG,"get crop file success");
				return bFile;
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CropUtil.closeIO(null, fos);
		}
		return null;
	}
}
