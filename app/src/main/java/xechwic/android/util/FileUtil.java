package xechwic.android.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import xechwic.android.act.MainApplication;


public class FileUtil {


	public static boolean isBlackLiveExist(){
		return isFileExist(MainApplication.getInstance().getCacheDir()+"/blacklive");
	}

	public static boolean isGuardFileExist(){
		return isFileExist(MainApplication.getInstance().getCacheDir()+"/guard");
	}

	//////生成亮屏临时文件
	public static void createBlackLiveFile(){
		try{
			File file=new File(MainApplication.getInstance().getCacheDir()+"/blacklive");
			if(!file.exists()){
				boolean create=file.createNewFile();
				Log.e("FileUtil","create file"+create);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	//////删除亮屏临时文件
	public static void deleteBlackLiveFile(){
		try{
			File file=new File(MainApplication.getInstance().getCacheDir()+"/blacklive");
			if(file.exists()){
				boolean create=file.delete();
				Log.e("FileUtil","delete file"+create);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/////生成守护文件
	public static void createGuardFile(){
		try{
			File file=new File(MainApplication.getInstance().getCacheDir()+"/guard");
			if(!file.exists()){
				boolean create=file.createNewFile();
				Log.e("FileUtil","create file"+create);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	/////删除守护文件
	public static void deleteGuardFile(){
		try{
			File file=new File(MainApplication.getInstance().getCacheDir()+"/guard");
			if(file.exists()){
				boolean create=file.delete();
				Log.e("FileUtil","delete file"+create);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	/////守护文件是否存在
	public static boolean isGuardFileExists(){
		try{
			File file=new File(MainApplication.getInstance().getCacheDir()+"/guard");
			if(file.exists()){
				return true;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}


	/////文件名hex转换，避免乱码
	public static String toHexName(String filename){
		try {
			if (!TextUtils.isEmpty(filename)) {
				if (filename.lastIndexOf(".") > 0) {
					String ext = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
					filename = HexUtil.bytesToHexString(getFileNameNoExt(filename).getBytes()) + "." + ext;
				} else {
					filename = HexUtil.bytesToHexString(filename.getBytes());
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return  filename;
	}

	//////hex转成原来文件名
	public static String toSrcName(String filename){
		try {
			if (!TextUtils.isEmpty(filename)) {
				Log.e("fileutil","filename:"+filename);
				if (filename.lastIndexOf(".") > 0) {
					String ext = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
					filename = new String(HexUtil.hexStringToBytes(getFileNameNoExt(filename))) + "." + ext;
				} else {
					filename = new String(HexUtil.hexStringToBytes(getFileNameNoExt(filename)));
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		Log.e("fileutil","toSrcName:"+filename);
		return  filename;
	}

   /**文件重命名
	 * @param dirpath 文件目录
	 * @param oldname  原来的文件名
	 * @param newname 新文件名
	 */
	public static void renameFile(String dirpath,String oldname,String newname){
		try {
			if (!oldname.equals(newname)) {//新的文件名和以前文件名不同时,才有必要进行重命名
				File oldfile = new File(dirpath + "/" + oldname);
				File newfile = new File(dirpath + "/" + newname);
				if (!oldfile.exists()) {
					return;//重命名文件不存在
				}
				if (newfile.exists())//若在该目录下已经有一个文件和新文件名相同，则不允许重命名
					System.out.println(newname + "已经存在！");
				else {
					oldfile.renameTo(newfile);
				}
			} else {
				System.out.println("新文件名和旧文件名相同...");
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/////根据路径获取文件名，不管是否文件存在
	public static String getFileName(String sFilePath){
		if(!TextUtils.isEmpty(sFilePath)){
			if(sFilePath.lastIndexOf("/")>-1){
				return sFilePath.substring(sFilePath.lastIndexOf("/")+1);

			}
		}
		return null;
	}

	/////获取文件扩展名
	public static String getFileExt(String sFilePath){
		if(TextUtils.isEmpty(sFilePath)){
			return "";
		}
		String  sExt="";
		String fileName =sFilePath.substring(sFilePath.lastIndexOf("/")+1, sFilePath.length());
		if(fileName.lastIndexOf(".") > 0){
			sExt = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
		}
		return sExt;
	}
	/////获取文件名没有扩展名
	public static String getFileNameNoExt(String fileName){
         String name=fileName;
		if(fileName.lastIndexOf(".") > 0){
			name = fileName.substring(0, fileName.lastIndexOf("."));
		}
		return name;
	}

	/**
	 * 某路径是否存在，不存在则创建 返回 true: 文件夹存在，或创建成功 false: 不存在
	 */
	public static boolean openOrCreatDir(String path) {
		File file = new File(path);

		try
		{
			if (!file.exists()) {
				return file.mkdirs();
			}
			return true;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 文件是否已存在
	 */
	public static boolean isFileExist(String path){
		File file = new File(path);
		return file.exists();
	}
	

	/**
	 * String --> InputStream
	 */
	public static InputStream ToStream(String str) {
		ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
		return stream;
	}

	/**
	 * InputStream --> String
	 */
	public static String ToString(InputStream is) {
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuffer buffer = new StringBuffer();
		String line = "";

		try {
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return buffer.toString();
	}

	
	/**
	 * 通过路径获取图片(传递缩放到的宽度)
	 */
	public static Bitmap getBitmapByPath(String path,int width){
		Bitmap photo = null;
		try {
			Options op = new Options();
			op.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, op);
			int wRatio = (int) op.outWidth / width;
			int hRatio = (int) op.outHeight / width;
			if (wRatio > 1 || hRatio > 1) {
				if (wRatio > hRatio) {
					op.inSampleSize = wRatio;
				} else {
					op.inSampleSize = hRatio;
				}
			}
			op.inJustDecodeBounds = false;
			photo = BitmapFactory.decodeFile(path,
					op);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return photo;
	}
	/*** 获取文件大小 ***/
	public static  long getFileSizes(File f){
		long s = 0;
		try {
			if (f!=null&&f.exists()) {
				FileInputStream fis = null;
				fis = new FileInputStream(f);
				s = fis.available();
				fis.close();
			} else {
				f.createNewFile();
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		return s;
	}

	
	/**
	 * 删除文件
	 */
	public static void deleteFile(String path){
		if(path!=null){
			UriConfig.delete(path);
		}
	}

	////将文本保存到指定文件
	public static void saveFile(String str,String filePath) {
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				File dir = new File(file.getParent());
				dir.mkdirs();
				file.createNewFile();
			}
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(str.getBytes());
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	}
