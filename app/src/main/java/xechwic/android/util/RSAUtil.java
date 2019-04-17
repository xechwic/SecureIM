package xechwic.android.util;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.crypto.Cipher;

import xechwic.android.XWDataCenter;


public class RSAUtil {
	//算法 
	public static String Key_ALGORITHM="RSA"; 
	//私钥 
	public static String Private_Key="RSAPrivateKey"; 
	//公钥 
	public static String Public_Key="RSAPublicKey"; 
	//密钥长度 
	public static int Key_Size=2048; 
	
	
	
	public static PublicKey getPublicKey(String key)  {   
        byte[] keyBytes;   
        try
        {
        	android.util.Log.v("RSAUtil","getPublicKey 1");
	        keyBytes = (Base64Util.decode(key.getBytes("ISO-8859-1")));   
	        
        	android.util.Log.v("RSAUtil","getPublicKey 2 keyBytes "+keyBytes.length);
	
	        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);   
	        
        	android.util.Log.v("RSAUtil","getPublicKey 3");
	        KeyFactory keyFactory = KeyFactory.getInstance(Key_ALGORITHM);   
	        PublicKey publicKey = keyFactory.generatePublic(keySpec);   
	        return publicKey;   
	        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
        return null;
  }   
    
  public static PrivateKey getPrivateKey(String key) {   
        byte[] keyBytes;   
        try
        {
	        keyBytes = (Base64Util.decode(key.getBytes("ISO-8859-1")));   
	
	        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);   
	        KeyFactory keyFactory = KeyFactory.getInstance(Key_ALGORITHM);   
	        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);   
	        return privateKey;   
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
        return null;
  }   

    
  public static String getKeyString(Key key) {   
	  
	    try
	    {
	        byte[] keyBytes = key.getEncoded();   
	             
	        String s = new String (Base64Util.encode(keyBytes),"ISO-8859-1");   
	        return s;   
	    }
	    catch(Exception ex)
	    {
	    	ex.printStackTrace();
	    }
	    return null;
  }   
  
    public static String getTimeStamp()
    {
    	return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }
    
    public static Date ParseTimeStamp(String sDate)
    {
    	if (sDate==null)
    		return null;
    	try
    	{
    		return new SimpleDateFormat("yyyyMMddHHmmssSSS").parse(sDate);
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	return null;
    }
		
	public static boolean generateKey(String sPubKeyPath,String sPrivateKeyPath,Integer lock){ 
		
		try
		{
		    android.util.Log.v("RSAUtil","generateKey ...");
			
			//实例化密钥生成器 
			KeyPairGenerator keypairgenerator=KeyPairGenerator.getInstance(Key_ALGORITHM); 

			//初始化 
			keypairgenerator.initialize(Key_Size); 
			
			//获得密钥对 
			KeyPair keypair=keypairgenerator.generateKeyPair(); 
			//密钥 
			RSAPrivateKey pritekey=(RSAPrivateKey)keypair.getPrivate(); 
			//公钥 
			RSAPublicKey pubkey=(RSAPublicKey)keypair.getPublic(); 
			
			
			String sTimeStamp=getTimeStamp();
			
			
			synchronized (lock)
			{
				FileOutputStream fos=new FileOutputStream(sPubKeyPath);
				fos.write( (sTimeStamp+"\r").getBytes("ISO-8859-1"));
				fos.write(getKeyString(pubkey).getBytes("ISO-8859-1"));
				fos.close();
				
				fos=new FileOutputStream(sPrivateKeyPath);
				
				///////////////////////////private key
				
				///////////2016-06-30,使用sd card对保存的私钥加密
				
				try
				{
					String sprivate=getKeyString(pritekey);
					///Log.e("RSAUtil","sprivate:"+sprivate);
					byte[] btSDCardEncode=com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_data(sprivate.getBytes("ISO-8859-1"));
					///Log.e("RSAUtil","btSDCardEncode:"+new String (btSDCardEncode,"iso-8859-1"));
					/////////////////
					fos.write( (sTimeStamp+"\r").getBytes("ISO-8859-1"));
					fos.write(btSDCardEncode);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				finally
				{
										
					fos.close();
				}
			}

		    android.util.Log.v("RSAUtil","generateKey ok!");

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
		return true;

	} 
	

	public static PrivateKey privatekey=null;

	//获得密钥 
	public static PrivateKey readPrivateKey(String sFile){
		////////////2016-09-29,缓存privatekey
		if (privatekey!=null)
			return privatekey;

		PrivateKey pk=null;
		try
		{
			File f=new File(sFile);
			if(!f.exists()){
				return null;
			}
			byte[] bt=new byte[(int)f.length()];
			FileInputStream fos=new FileInputStream(f);
			int iRet=fos.read(bt);
			
			fos.close();
			if (iRet>0)
			{
                String sText=new String(bt,"ISO-8859-1");
                int iPos=sText.indexOf("\r");
                if (iPos>=0)
                {
                	sText=sText.substring(iPos+1).trim();
                }
				byte[] btnew=sText.getBytes("ISO-8859-1");
				
				///////////2016-06-30,使用sd card对保存的私钥解密
				byte[] btSDCardDecode=com.example.mcryptolmsdimpl_demo.MainActivity.decrypt_data(btnew);
				///fos.write(btSDCardEncode);
				
				pk=getPrivateKey(new String(btSDCardDecode,"ISO-8859-1"));
				privatekey=pk;
			}
			
		}
		catch(Exception ex)
		{
			Log.e("RSAUtil","readPrivateKey error:"+ex.getMessage());
			ex.printStackTrace();
		}
		return pk;
	} 
	
	
	//获得公钥 
	public static PublicKey readPublicKey(String sFile){ 
		PublicKey pk=null;
		try
		{
			File f=new File(sFile);	
			byte[] bt=new byte[(int)f.length()];
			
			FileInputStream fos=new FileInputStream(f);
			int iRet=fos.read(bt);
			
			fos.close();
			if (iRet>0)
			{
                String sText=new String(bt,"ISO-8859-1");
                int iPos=sText.indexOf("\r");
                if (iPos>=0)
                {
                	sText=sText.substring(iPos+1).trim();
                }
				byte[] btnew= (sText.getBytes("ISO-8859-1"));

				pk=getPublicKey(new String(btnew,"ISO-8859-1"));
			}
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return pk;
	} 
	
	//公钥加密 
	public static byte[] encryptByPubKey(byte[] data,String sFile){ 

		Log.e("RSAUtil","encryptByPubKey"+sFile);
		//对数据进行解密 
		try
		{
			Cipher cipher=Cipher.getInstance(Key_ALGORITHM); 
			cipher.init(Cipher.ENCRYPT_MODE,readPublicKey(sFile)); 
			return Base64Util.encode(cipher.doFinal(data));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}	
		return null;
	} 
	
	//私钥解密 
	public static byte[] decryptByPrivateKey(byte[] data,String sFile){ 
		//对数据进行解密 
		try
		{   if(data==null||data.length==0){
			return null;
		    }
			Cipher cipher=Cipher.getInstance(Key_ALGORITHM); 
			cipher.init(Cipher.DECRYPT_MODE,readPrivateKey(sFile)); 
			return cipher.doFinal(Base64Util.decode(data));
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}	
		return null;
	} 
	
	

	//////////////////读文件的第iLen行字串
	public static String readFileLine(String sFile,int iLine)
	{
		try
		{
		
			File file = new File(sFile);
			if(!file.exists()){
				return null;
			}
	        BufferedReader br = new BufferedReader(new FileReader(file));  
	        
	        String line=null;
	        
	        int iNum=0;
	        try
	        {
	        	while (iNum<iLine)
	        	{
		            line= br.readLine();
		            if (line!=null)
		        	    line=line.trim();
		            else
		        	    return null;
		            
		            iNum++;
		            
		            if (iNum==iLine)
		            	return line;
	        	}
		        
	        }
	        finally
	        {
	        	if (br!=null)
	                br.close();
	        }
	        return null;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String readRSATimestamp(String sFile)
	{
		return readFileLine(sFile,1);
	}
	
	public static void SaveAESKeysToFile(String sFile,String sTime1,String sPass1,String sTime2,String sPass2 ,String sOK////,
			////String snewTime1,String snewPass1,String snewTime2,String snewPass2
			)
	{
		try
		{
			/////Log.e("RSAUtil","SaveAESKeysToFile:"+sTime1+" "+sPass1+" "+sTime2+" "+sPass2);
			
			FileOutputStream fos=new FileOutputStream(sFile);
			fos.write( (sTime1+"\r").getBytes("ISO-8859-1"));
			
			/////////////////对AES密钥进行保护
			if (sPass1==null)
				sPass1="";
			if (sPass2==null)
				sPass2="";
			byte[] btSDCardEncode=RSAUtil.encryptByPubKey(sPass1.getBytes("ISO-8859-1"), XWDataCenter.xwDC.sPubkeyFile);//com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_data(sPass1.getBytes("ISO-8859-1"));
			sPass1=new String (btSDCardEncode,"ISO-8859-1");
			
			fos.write( (sPass1+"\r").getBytes("ISO-8859-1"));
			fos.write( (sTime2+"\r").getBytes("ISO-8859-1"));
			
			/////////////////对AES密钥进行保护
			btSDCardEncode=RSAUtil.encryptByPubKey(sPass2.getBytes("ISO-8859-1"), XWDataCenter.xwDC.sPubkeyFile);//com.example.mcryptolmsdimpl_demo.MainActivity.encrypt_data(sPass2.getBytes("ISO-8859-1"));
			sPass2=new String (btSDCardEncode,"ISO-8859-1");
			
			fos.write( (sPass2+"\r").getBytes("ISO-8859-1"));
			fos.write( (sOK+"\r").getBytes("ISO-8859-1"));
			/*fos.write( (snewTime1+"\r").getBytes("ISO-8859-1"));
			fos.write( (snewPass1+"\r").getBytes("ISO-8859-1"));
			fos.write( (snewTime2+"\r").getBytes("ISO-8859-1"));
			fos.write( (snewPass2+"\r").getBytes("ISO-8859-1"));	*/		
			fos.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return;
	}
	
	
    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
'A', 'B', 'C', 'D', 'E', 'F' };
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }
	
    static private String sKeepLastKey=null;
	public static synchronized String generateAESKey(String sLastKey)
	{
		if (sLastKey==null)
			sLastKey=sKeepLastKey;
		
		SecureRandom rd=new SecureRandom();
		rd.setSeed(System.nanoTime());
		
		byte[] buf=new byte[16];
		rd.nextBytes(buf);
		
        MessageDigest md5;
        try{
            //////fis = new FileInputStream(filename);
            md5 = MessageDigest.getInstance("MD5");
            {
            	if (sLastKey!=null)
                    md5.update(sLastKey.getBytes("ISO-8859-1"),0,sLastKey.length());
                md5.update(buf,0,buf.length);

				String sUUID=UUID.randomUUID().toString();
				md5.update(sUUID.getBytes(),0,sUUID.getBytes().length);
            }
            ///////fis.close();
            sKeepLastKey=  toHexString(md5.digest());  
            return sKeepLastKey;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
		////rd.nextBytes(buf);
	}


	/////////////产生32字节的aes key
	public static synchronized byte[] generateSDCard32bytesAESKey()
	{
		//if (sLastKey==null)
		//	sLastKey=sKeepLastKey;

		SecureRandom rd=new SecureRandom();
		rd.setSeed(System.nanoTime());

		MessageDigest md5;
		byte[] buf=new byte[16];

		byte[] ret=new byte[32];
		try{


			rd.nextBytes(buf);
			//////fis = new FileInputStream(filename);
			md5 = MessageDigest.getInstance("MD5");
			{
				if (sKeepLastKey!=null)
					md5.update(sKeepLastKey.getBytes("ISO-8859-1"),0,sKeepLastKey.length());
				md5.update(buf,0,buf.length);

				String sUUID=UUID.randomUUID().toString();
				md5.update(sUUID.getBytes(),0,sUUID.getBytes().length);
			}
			///////fis.close();
			byte [] bt1=md5.digest();
			sKeepLastKey=  toHexString(bt1);

			///////后16字节
			rd.nextBytes(buf);
			//////fis = new FileInputStream(filename);
			md5 = MessageDigest.getInstance("MD5");
			{
				if (sKeepLastKey!=null)
					md5.update(sKeepLastKey.getBytes("ISO-8859-1"),0,sKeepLastKey.length());
				md5.update(buf,0,buf.length);

				String sUUID=UUID.randomUUID().toString();
				md5.update(sUUID.getBytes(),0,sUUID.getBytes().length);
			}
			///////fis.close();
			byte [] bt2=md5.digest();
			sKeepLastKey=  toHexString(bt1);

			System.arraycopy(bt1,0,ret,0,16);
			System.arraycopy(bt2,0,ret,16,16);

			////return sKeepLastKey;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return ret;
		////rd.nextBytes(buf);
	}
	
}
