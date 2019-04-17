package xechwic.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Util {
	
	/**
	 * 复制文件(以超快的速度复制文件)
	 *	将/data/data/xechwic.android/userdata/XXX.xwx音频文件，转移到sd卡
	 */
	public static long copyFile(File srcFile, String destFilePath) {
		long copySizes = 0;
		InitFile(destFilePath);

		try {
			FileInputStream fis=new FileInputStream(srcFile);
			FileOutputStream fos=new FileOutputStream(destFilePath);
			FileChannel fcin = fis.getChannel();
			FileChannel fcout = fos.getChannel();

			long size = fcin.size();
			fcin.transferTo(0, size, fcout);

			fcin.close();
			fcout.close();
			fis.close();
			fos.close();
			copySizes = size;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return copySizes;
	}
	
	/**
	 * 文件初始化
	 */
	public static void InitFile(String destFilePath) {
		File mFile = new File(destFilePath);
		if (mFile.isDirectory()) {
			mFile.delete();
		}
		if (!mFile.isFile()) {
			mFile.getParentFile().mkdirs();// 创建目录
			try {
				mFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}// 创建文件
		}
	}

	
	/**
	 * 格式转换，最多截取小数点后面2位
	 */
	public static String format(float bt) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(2);
		return df.format(bt);
	}


	

	



}
