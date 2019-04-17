package xechwic.android.util;

import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

import xechwic.android.act.MainApplication;

public class UriConfig {

	public static final String USER_DATA_DIR="/userdata";////临时数据文件夹，录音产生的源文件，接收文件临时存放

	////UserData目录
	public static String getUserDataDir(){

		FileUtil.openOrCreatDir(getSavePath() + USER_DATA_DIR);

		return getSavePath() + USER_DATA_DIR;
	}

	// 图片路径
	public static String getImageSavePath() {

		FileUtil.openOrCreatDir(getSavePath() + "/image/");

		return getSavePath() + "/image/";
	}
	//文件保存路径
	public static String getFileSavePath(){
		String dir=getSavePath()+"/file";
		makeFileDirs(dir);
		return dir;
	}
	//视频文件保存路径
	public static String getVideoSavePath(){
		String dir=getSavePath()+"/video/";
		makeFileDirs(dir);
		return dir;
	}
	/**
	 * 记录文件操作
	 */
	public static String mfileSeedSavePath = getPackPath() + "/record.dat";


	////////////////2014-06-26
	// SD卡保存路径
	public static String getSavePath() {
		FileUtil.openOrCreatDir(getExternalStoragePath() + "/"+MainApplication.getInstance().getPackageName());

		return getExternalStoragePath() + "/"+MainApplication.getInstance().getPackageName();
	}

	/////获取解密文件路径
	public static String getDecryptFileDir(){
		String path=UriConfig.getSavePath()+"/decryptfiles";
		File file=new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return path;
	}

	////AES加密文件路径
	public static String getAesFilePath(String sFromUser){
		String aespath=UriConfig.getSavePath()+"/aesgotfiles/"+sFromUser;
		return aespath;
	}

	/////通话记录
	public static String getCallRecordPath(){
		String path=UriConfig.getSavePath()+"/record.dat";
		return path;
	}

	/////通话记录
	public static String getFriendListPath(){
		String path=UriConfig.getSavePath()+"/friend.dat";
		return path;
	}

	// 包路径
	public static String getPackPath() {

		return MainApplication.getInstance().getFilesDir().toString();
	}

	public static String getExternalStoragePath() {
		// 获取SdCard状态
		String state = android.os.Environment.getExternalStorageState();
		// 判断SdCard是否存在并且是可用的
		if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
			if (android.os.Environment.getExternalStorageDirectory().canWrite()) {

				//////////////////////////
				String sdPath=android.os.Environment.getExternalStorageDirectory()
						.getPath();
				try
				{
					////android.util.Log.v("XIM","getExternalStoragePath sdPath"+sdPath);
					////File file=new File(sdPath); 
					StatFs fs = new StatFs(sdPath);
					if(fs!=null){
						long availableBlocks = fs.getAvailableBlocks();
						long blocksSize = fs.getBlockSize();
						long result = availableBlocks*blocksSize;
						Log.e("start","available:"+result/1024/1024);
						if(result>10*1024*1024){
							return sdPath;
						}
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		return getPackPath();
	}

	public static String getFileName(String sPath)
	{
		String sName="";
		if (sPath==null)
			return "";

		try
		{
			File file1 = new File(sPath);
			sName=file1.getName();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

		return sName;

	}

	public static void makeFileDirs(String sPath)
	{
		try
		{
			File file = new File(sPath);
			if (!file.isDirectory()) {
				try {
					file.mkdirs();
				} catch (Exception e) {

				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
    /////清理应用所有文件
    public static void deleteAll(){
		delete(getSavePath());
	}

	public static void deleteInDir(String dir){
		try
		{
			File file= new File(dir);
			if(!file.exists()){
				return;
			}

			if(file.isDirectory()){
				File[] childFiles = file.listFiles();
				if (childFiles == null || childFiles.length == 0) {
					return;
				}

				for (int i = 0; i < childFiles.length; i++) {
					delete(childFiles[i].getAbsolutePath().toString());
				}


			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void delete(String sPath) {
		try
		{
			File file= new File(sPath);
            if(!file.exists()){
				return;
			}
			if (file.isFile()) {
				file.delete();
				return;
			}

			if(file.isDirectory()){
				File[] childFiles = file.listFiles();
				if (childFiles == null || childFiles.length == 0) {
					file.delete();
					return;
				}

				for (int i = 0; i < childFiles.length; i++) {
					delete(childFiles[i].getAbsolutePath().toString());
				}

				long len=file.length();

				byte[] buf=new byte[8192];
				long iPos=0;

				try {
					boolean bFdel=file.delete();
					Log.e("start","src dir del "+bFdel);
					File tmpFile=new File(sPath);
					//openFileOutput 只能用于应用缓存目录下的文件
//					FileOutputStream outStream2 =MainApplication.getInstance().openFileOutput(sPath,Context.MODE_PRIVATE);
					FileOutputStream outStream2=new FileOutputStream(tmpFile);
					while (iPos < len) {
						outStream2.write(buf);
						iPos += buf.length;
					}
					outStream2.close();
					if(tmpFile.exists()){
						boolean bDel=tmpFile.delete();
						Log.e("start","tmp dir del "+bDel);
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}

			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/////获取文件类型
	public static String getMIMEType(File file) {

		 if(file==null||!file.exists()){
			 return "*/*";
		 }
		String type="*/*";
		String fName = file.getName();
		//获取后缀名前的分隔符"."在fName中的位置。
		int dotIndex = fName.lastIndexOf(".");
		if(dotIndex < 0){
			return type;
		}
	    /* 获取文件的后缀名 包括.*/
		String end=fName.substring(dotIndex).toLowerCase();
		if(TextUtils.isEmpty(end))return type;

		return type;
	}


}
