package xechwic.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

import xechwic.android.bean.MFile;

public class FileUtils {
	// sdcard路径
	private String SDPath;

	public FileUtils() {
		SDPath=Environment.getExternalStorageDirectory()+"";
	}

	public ArrayList<MFile> getFilesByPath(String path) {
		ArrayList<MFile> mFiles=null;
		try{
			File mainFile = new File(path);
			File[] files = mainFile.listFiles();
			if(files!=null && files.length>0){
				mFiles=new ArrayList<MFile>();
			}else{
				return null;
			}
			for(File file:files){
				MFile mFile=new MFile();
				mFile.setDirectory(file.isDirectory());
				mFile.setFilePath(file.getAbsolutePath());
				mFile.setFileName(file.getName());
				mFile.setParentFilePath(file.getParent());
				mFiles.add(mFile);
			}
		}catch (Exception e) {
		}
		return mFiles;
	}
	
	public ArrayList<String> getImageFilesByPath(String path) {
		if(path==null){
		 	return null;
		}
		ArrayList<String> mFiles=null;
		try{
			File mainFile = new File(path);
			File[] files = mainFile.listFiles();
			if(files!=null && files.length>0){
				mFiles=new ArrayList<String>();
			}else{
				return null;
			}
			for(File file:files){
				if(file.isFile()){
					Bitmap bitmap=BitmapFactory.decodeFile(file.getAbsolutePath());
					if(bitmap!=null){
						mFiles.add(file.getAbsolutePath());
					}
				}
			}
		}catch (Exception e) {
		}
		return mFiles;
	}
	
	public String getParentPath(String path){
		if(path == null || (path=path.trim()).length()<=0){
			return null;
		}
//		Log.d(tag, "path=" + path);
		if(path!=null && path.equalsIgnoreCase(SDPath)){
			return null;
		}
		try{
			File file = new File(path);
			if(file!=null){
				return file.getParent();
			}
		}catch (Exception e) {
		}
		return null;
	}
	
	public String getCurrPathDirtoryName(String path){
		if(path == null || (path=path.trim()).length()<=0){
			return null;
		}
		String[] strs=path.split("/");
		if(strs!=null && strs.length>0){
			return strs[strs.length-1];
		}
		return null;
	}
	
	public String getSdcardRootPath(){
		return SDPath;
	}
	
	/**
	 * 删除文件目录下本身以外的其他文件（包括目录）
	 */
	public static void deleteFileWithFilter(String path){
		File selfFile = new File(path);
		String dirName = selfFile.getParent();
		if(dirName.endsWith(File.separator)){
			dirName = dirName + File.separator;
		}
		File file = new File(dirName);
		if(!file.exists() || !file.isDirectory()){//不存在或者目录，返回;
			return;
		}
		File[] files = file.listFiles();
		for(int i = 0; i<files.length; i++){
			if(!files[i].getName().equals(selfFile.getName())){
				files[i].delete();
			}		
		}		
	}

	/**
	 * 删除文件目录(目录下所有文件)
	 */
	public static void deleteFileDir(String path){
		File file = new File(path);
		if(!file.exists() || !file.isDirectory()){//不存在或者目录，返回;
			return;
		}
		File[] files = file.listFiles();
		for(int i = 0; i<files.length; i++){
				files[i].delete();
		}
	}
}
