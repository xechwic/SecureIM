package xechwic.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;


/**
 * 文件管理
 * @author Administrator
 *
 */
public class FileManager {

	/**
	 * 根据url获取文件名
	 * @param url
	 * @return
	 *     文件名
	 */
	public static String getFileName(String url){
		if(url==null || (url=url.trim()).length()<=0){
			return null;
		}
		int pos=url.indexOf("?");
		if(pos>0){
			url=url.substring(0,pos);
		}
		if(url.endsWith("/") && url.length()>2){
			url=url.substring(0,url.length()-1);
		}
		pos=url.lastIndexOf("/");
		if(pos>=0){
			 return url.substring(pos+1);
		}
		return null;
	}
	
	/**
	 * 根据url获取文件后缀
	 * @param url
	 * @return
	 *      文件后缀
	 */
	public static String getSuffix(String url){
		return getSuffixByFileName(getFileName(url));
	}
	
	/**
	 * 根据文件名获取文件后缀
	 * @param fileName
	 *      文件名
	 * @return
	 *      文件后缀
	 */
	public static String getSuffixByFileName(String fileName){
		if(fileName==null || (fileName=fileName.trim()).length()<=0){
			return null;
		}
		int pos=fileName.lastIndexOf(".");
		if(pos>=0 && fileName.length()-1>pos){
		   return fileName.substring(pos+1);	
		}  
		return null;
	} 
	
	/**
	 * 获取新的文件名  在之前的文件名后加(1),如300x300x75x1.jpg，变成300x300x75x1(1).jpg
	 * @param fileName
	 * @return
	 */
	public static String getNewFileName(String filePath,String fileName,int index){
		if(fileName==null || (fileName=fileName.trim()).length()<=0){
			return null;
		}
		String suffix=getSuffixByFileName(fileName);
		String newFileName;
		if(suffix==null){ 
			newFileName=fileName+"("+index+")";
		}else{
			newFileName=fileName.substring(0,fileName.length()-suffix.length()-1)+"("+index+")."+suffix;
		}
		if(isFileExist(filePath+newFileName)){
			return getNewFileName(filePath,fileName,index+1);
		}else{
			return newFileName;
		}
	}
	
	/**
	 * 获取新的文件名  在之前的文件名后加(i),如300x300x75x1.jpg，变成300x300x75x1(i).jpg
	 * @param fileName
	 * @param shieldStrs 屏蔽的名字
	 * @return
	 */
	public static String getNewFileName(String filePath,String fileName,int index,ArrayList<String> shieldStrs){
		String newFileName=getNewFileName(filePath, fileName, index);
		if(shieldStrs==null){
			return newFileName;
		}
		while (true) {
			for(String name:shieldStrs){
				if(newFileName.equals(name)){
					newFileName=increaseFileNameSuffix(newFileName);
					continue;
				}
			}
			return newFileName;
		}
	}
	
	/**
	 * 获取文件名
	 * @param fileName
	 * @param shieldStrs 需屏蔽的文件名
	 * @return
	 */
	public static String getNewFileName(String fileName,ArrayList<String> shieldStrs){
		String newFileName=fileName;
		while (true) {
			for(String name:shieldStrs){
				if(newFileName.equals(name)){
					newFileName=increaseFileNameSuffix(newFileName);
//					ELog.i(MusicApplication.tag,"......newFileName="+newFileName);
					continue;
				}
			}
			return newFileName;
		}
		
	}
	
	/**
	 * 修改文件名，如300x300x75x1(1).jpg，变成300x300x75x1(2).jpg
	 * @param fileName
	 * @return
	 */
	public static String increaseFileNameSuffix(String fileName){
		if(fileName==null || (fileName=fileName.trim()).length()<=0){
			return null;
		}
		String suffix=getSuffixByFileName(fileName);
		String newFileName;
		int index=0;
		if(suffix!=null){
			fileName=fileName.substring(0,fileName.length()-suffix.length()-1);
		}
		int pos1=fileName.lastIndexOf("(");
		int pos2=fileName.lastIndexOf(")");
		if(pos1>0 && pos2>0 && pos2>=pos1){
			String temp=fileName.substring(pos1+1,pos2);
			if(temp!=null){
				try {
					index=Integer.parseInt(temp);
					fileName=fileName.substring(0,pos1);
				} catch (Exception e) {
				}
			}
		}
		index++;
		if(suffix==null){ 
			newFileName=fileName+"("+index+")";
		}else{
			newFileName=fileName+"("+index+")."+suffix;
		}
		return newFileName;
	}
	
    /**
     * 判断文件是否存在
     * @param filePath
     * @return
     */
	public static boolean isFileExist(String filePath){
		if(filePath==null){
			return false;
		}
		File file=new File(filePath);
		if(file.exists()){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取指定路径文件大小
	 * @param filePath
	 * @return
	 */
	public static long getFileSize(String filePath){
		if(filePath==null){
			return 0;
		}
		File file=new File(filePath);
		if(file.exists()){
			return file.length();
		}
		return 0;
	}
	
	/**
	 * 删除指定路径的文件
	 * @param filePath
	 *        文件路径
	 */
	public static void deleteFile(String filePath){
		if(filePath==null){
			return;
		}
		try
		{
			File file=new File(filePath);
			if(file.exists()){
				file.delete();
			}
		}
		catch(Exception ex)
		{
			
		}
	}
	
	/**
	 * 获取文件夹下面的所有文件
	 * @param path
	 * @return
	 */
	public static String[] getDirectoryFiles(String path){
		if(path==null){
			return null;
		}
		File file=new File(path);
		if(!file.exists()){
			return null;
		}
		String[] files=file.list();
		if(files==null || files.length<=0){
			return null;
		}
		return files;
	}
	
	/**
	 * 删除文件夹中的内容
	 * @param path
	 */
	public static void deleteDirectory(String path){
		File file=new File(path);
		if(!file.exists()){
			return;
		}
		String fPath=file.getAbsolutePath();
		if(file.isDirectory()){
			String[] files=getDirectoryFiles(path);
			if(files==null){
				deleteFile(path);
				return;
			}
			for(String str:files){
				str=fPath+"/"+str;
			    file=new File(str);
				if(file.isDirectory()){
					deleteDirectory(str);
				}else if(file.isFile()){
					deleteFile(str);
				}
			}
			deleteFile(path);
		}else if(file.isFile()){
			deleteFile(path);
		}
	}
	
	/**
	 * 删除文件夹中的内容,不删除文件夹本身
	 * @param path
	 */
	public static void deleteDirectoryContent(String path){
		File file=new File(path);
		if(!file.exists()){
			return;
		}
		String fPath=file.getAbsolutePath();
		if(file.isDirectory()){
			String[] files=getDirectoryFiles(path);
			if(files==null){
				deleteFile(path);
				return;
			}
			for(String str:files){
				str=fPath+"/"+str;
			    file=new File(str);
				if(file.isDirectory()){
					deleteDirectory(str);
				}else if(file.isFile()){
					deleteFile(str);
				}
			}
//			deleteFile(path);
		}else if(file.isFile()){
			deleteFile(path);
		}
	}
	
	/**
	 * 移动单个文件到制定位置
	 * 
	 * @param src	源文件url
	 * @param dest	目标位置url
	 * @param fileName	文件名
	 * @throws Exception	操作出错，这个异常会带有产生异常的解释，用Exception.getMessage()提取
	 */
	public static void moveOneFile(String src, String dest) throws Exception {
		deleteFile(dest);
		copyOneFile(src, dest);
		deleteFile(src);
	}
	
	public static boolean renameFile(String src,String dest){
		if(src==null || dest==null){
//			throw new Exception("源文件目录或是目标文件目录为空");
			return false;
		}
		File srcFile=new File(src);//TODO 文件存在时 另外处理
		if(!srcFile.exists()){
//			throw new Exception("源文件不存在");
			return false;
		}
		File destFile=new File(dest);//TODO 文件存在时 另外处理
		if(destFile.exists()){
			return false;
		}
		return srcFile.renameTo(destFile);
	}
	
	/**
	 * 复制单个文件到制定位置
	 */
	public static void copyOneFile(String src, String dest) throws Exception {
		if(src==null || dest==null){
			throw new Exception(xechwic.android.XWCodeTrans.doTrans("源文件目录或是目标文件目录为空"));
		}
		File srcFile=new File(src);//TODO 文件存在时 另外处理
		if(!srcFile.exists()){
			throw new Exception(xechwic.android.XWCodeTrans.doTrans("源文件不存在"));
		}
		File destFile=new File(dest);//TODO 文件存在时 另外处理
		if(!destFile.exists()){
			destFile.createNewFile();
		}
		FileInputStream fis=new FileInputStream(srcFile);
		FileOutputStream fos=new FileOutputStream(destFile);
		byte[] data = new byte[1024];
		int len;
		// 流读取完或是文件被暂停后停止读取数据
		while ((len = fis.read(data)) != -1) {
			fos.write(data, 0, len);
		}
		fos.flush();
		fos.close();
		fis.close();
	}
	
	/**
	 * 文件大小格式化
	 * 
	 * @param size
	 * @return
	 */
	public static String fileSizeFormate(long size) {
		if (size <= 0) {
			return "0kB";
		}
		float kb = (float) size / 1024;
		if (kb < 1024) {
			return floatToString(kb) + "KB";
		} else {
			kb = kb / 1024;
			return floatToString(kb) + "MB";
		}
	}
	
	/**
	 * 将float转成字符串，只保留最多两位小数的
	 * 
	 * @param size
	 * @return
	 */
	public static String floatToString(float size) {
		String str = "" + size;
		int index = str.indexOf(".");
		if (index > 0 && index + 3 < str.length()) {
			str = str.substring(0, index + 3);
		}
		return str;
	}
}
