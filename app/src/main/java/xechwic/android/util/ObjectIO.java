package xechwic.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectIO 
{	
	public static boolean saveObject(Object object, String absFileName) 
	{ 
		if(null == object || null == absFileName || absFileName.length() <= 0)
		{
			return false;
		}
		
		File file = new File(absFileName);
		String parentPath = file.getParent();
				
		if(null != parentPath && !openOrCreatDir(parentPath))
			return false;
		
		FileOutputStream os = null;
		ObjectOutputStream oos = null;
		
		try 
		{
			os = new FileOutputStream(absFileName);
			
			oos = new ObjectOutputStream(os);
			oos.writeObject(object);
			
			oos.close();
			os.close();			
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			return false;
		}
		catch (IOException e) 
		{
			return false;
		}
		
		return true;
				
	}
	
	/*
	 * 从指定目录  读取一个对象
	 * 
	 */
	public static Object readObject(String absFileName) 
	{		
		File f = new File(absFileName); 
		if(!f.isFile())
			return null;
		
		Object obj = null; 
		try
		{ 
			FileInputStream is = new FileInputStream(absFileName); 
			ObjectInputStream ois = new ObjectInputStream(is);
			
			obj = ois.readObject();
			
			ois.close();
			is.close();
		}
		catch (Exception e) 
		{ 
			e.printStackTrace();
			System.out.println("readObject失败了"+absFileName);
		} 
	
		return obj; 
	}

	/**
	 * 某路径是否存在，不存在则创建 返回 true: 文件夹存在，或创建成功 false: 不存在
	 */
	public static boolean openOrCreatDir(String path) {
		File file = new File(path);

		if (!file.exists()) {
			return file.mkdirs();
		}

		return true;
	}
	
}
