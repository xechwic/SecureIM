package com.example.mcryptolmsdimpl_demo;

import android.content.Context;
import android.util.Log;

import org.bouncycastle.util.encoders.Base64;

import java.security.Security;

import xechwic.android.XWDataCenter;
import xechwic.android.act.MainApplication;
import xechwic.android.util.AppConfig;
import xechwic.android.util.RSAUtil;



public class MainActivity {


    static{
        if(AppConfig.SD_ENCODE) {
            try {
                //Log.e("MainActivity","Security.addProvider 1");
                Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                ///Log.e("MainActivity","Security.addProvider 2");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    static public String sTag=null;

    static final String cont_AES="38477WWFGTT$^&gxghdhqhdhhdheuwFK";
    ////////////////64字节字符串
    static private String sAESPassWord=cont_AES;


    public static synchronized void getAESKey()
    {
        /////////////////如果非sd卡，则以该固定aes key存取本地消息文件。
        if(!AppConfig.SD_ENCODE){
            sAESPassWord=cont_AES;
            return ;
        }

        if(sAESPassWord!=null){
            return ;
        }
        Log.e("TFCard","getAESKey "+sTag);
        try
        {
            byte[] btAES=null;
            try
            {
                btAES=mcryptoUtil.getAES(sTag);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                btAES=null;
            }

            if (btAES==null || btAES.length==0)
            {
                mcryptoUtil.deleteAllAESKey();
                btAES= RSAUtil.generateSDCard32bytesAESKey();
                sAESPassWord=new String(btAES,"iso-8859-1");
                mcryptoUtil.storeAES(sTag,btAES);
                Log.e("TFCard","mcryptoUtil.storeAES "+sTag);
                /////////Log.e("mainActivity","generateSDCard32bytesAESKey:"+RSAUtil.toHexString(btAES));
            }
            else {
                /////////Log.e("MainActivity","getAES:"+RSAUtil.toHexString(btAES));
                //byte[] ret=decrypt_data(sAESkey.getBytes("iso-8859-1"));
                sAESPassWord=new String(btAES,"iso-8859-1");
            }
            /////Log.e("XIM","getAESKey"+RSAUtil.toHexString(sAESPassWord.getBytes("iso-8859-1")));
        }
        catch(Exception ex1)
        {
            ex1.printStackTrace();
        }

    }

    private static String sCurUserID="";
    /////////////////设置当前key的用户ID
    public static void setKeyID(String sNewTag)
    {
        if ((sTag==null)&&(AppConfig.SD_ENCODE))
        {
            boolean bTFOK=true;
            if (mcryptoUtil != null) {
                try {
                    mcryptoUtil.logout();
                    mcryptoUtil = null;
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
            try {
                mcryptoUtil = new MCryptoUtil();
                try {
                    Log.e("CheckSDCard", "mcryptoUtil.init ...");
                    mcryptoUtil.init(MainApplication.getInstance(), "12345678");
                    Log.e("CheckSDCard", "mcryptoUtil.init ok");
                } catch (Exception e2) {
                    bTFOK = false;
                    ///return;
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            if (bTFOK) {
                String[] keys=null;
                try {
                    keys = mcryptoUtil.listRSAKey(MainApplication.getInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if ((keys!=null) &&(keys.length>0)) {
                    sTag = keys[0];
                    Log.e("TFCard","setKeyID got sTag:"+sTag);
                }
            }

            if (mcryptoUtil != null) {
                try {
                    mcryptoUtil.logout();
                    mcryptoUtil = null;
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
        }
        if (sTag==null)
            sTag="RSAKEY";/////+sNewTag;
}


    static private MCryptoUtil mcryptoUtil =null;
    static public boolean bIsSDCardOK=false;
    static private long lLastCheckOK=0;


    static public  boolean CheckSDCard(Context ctx)
    {
        if(!AppConfig.SD_ENCODE){
            sAESPassWord=cont_AES;
            return  true;//取消SD卡检测
        }
        return bIsSDCardOK;
    }

    /////自动修正tf卡的问题。

    static public synchronized void repairTFCard(Context ctx)
    {
        if(!AppConfig.SD_ENCODE){
            return ;
        }
        boolean bTFOK=true;
            /////bIsSDCardOK=false;
            Log.e("repairTFCard","repairTFCard.");
            try {
                if (mcryptoUtil != null) {
                    try {
                        mcryptoUtil.logout();
                        mcryptoUtil = null;
                    } catch (Exception ex1) {
                        ex1.printStackTrace();
                    }
                }
                try {
                    mcryptoUtil = new MCryptoUtil();
                    try {
                        Log.e("repairTFCard", "mcryptoUtil.init ...");
                        mcryptoUtil.init(ctx, "12345678");
                        Log.e("repairTFCard", "mcryptoUtil.init ok");
                    } catch (Exception e2) {
                        bTFOK = false;
                        ///return;
                        e2.printStackTrace();
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    bTFOK = false;
                }

                try {
                    if (bTFOK) {
                        ////Log.i("MCryptoLMSDImpl test", new String(Base64.encode(pub.getEncoded())));
                        try {
                            byte[] data = "Hellow World".getBytes();
                            byte[] cipher = mcryptoUtil.rsaEncrypt(sTag, data);
                            Log.e("repairTFCard", "mcryptoUtil.rsaEncrypt:" + (new String(cipher)));
                            byte[] plain = mcryptoUtil.rsaDecrypt(sTag, cipher);
                            Log.e("repairTFCard", "mcryptoUtil.rsaDecrypt:" + new String(plain));
                            if (!"Hellow World".equals(new String(plain))) {
                                bTFOK = false;
                                Log.e("XIM", "mcryptoUtil.rsaDecrypt failed!");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            bTFOK = false;
                        }

                        /////当加解密不正常时，尝试生成rsa key
                        /*if (!bTFOK) {
                            try {
                                try {
                                    ///mcryptoUtil.deleteRSAKey(sTag);
                                    mcryptoUtil.deleteAllAESKey();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } catch (Exception ex1) {
                                ex1.printStackTrace();
                            }
                        }*/

                        /////当加解密不正常时，尝试生成rsa key
                        if (bTFOK) {
                            try {
                                /*Log.e("repairTFCard", "mcryptoUtil.genRSAKey...");
                                mcryptoUtil.genRSAKey(sTag);
                                Log.e("repairTFCard", "mcryptoUtil.genRSAKey ok");
                                */
                                //bTFOK = true;
                                //////////////成功生成新的RSAkey,则sAESPassWord要置空.
                                sAESPassWord = null;

                                if ((sAESPassWord==null)) {
                                    try {
                                        Log.e("repairTFCard","getAESKey...");
                                        getAESKey();
                                        Log.e("repairTFCard","getAESKey ok");
                                        //sAESPassWord=new String (getAESKey(),"iso-8859-1");
                                        ///mcryptoUtil.genAESKey(sTag);
                                    } catch (Exception ex1) {
                                        ex1.printStackTrace();
                                    }
                                }
                            } catch (Exception ex1) {
                                ex1.printStackTrace();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            catch(Exception ex1)
            {
                ex1.printStackTrace();
            }

            if (mcryptoUtil != null) {
            try {
                mcryptoUtil.logout();
                mcryptoUtil = null;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

    }


    static public synchronized void doCheckSDCard(Context ctx)
    {
        if(!AppConfig.SD_ENCODE){
            sAESPassWord=cont_AES;
            bIsSDCardOK=false;
			return ;
		}

        ////////////////////如果私钥为空
        ////if ((System.currentTimeMillis()-lLastCheckOK>30000) ||(sAESPassWord==null)/*||(xechwic.android.util.RSAUtil.privatekey==null)*/)
        {
            ///Log.e("TFCARD","doCheckSDCard 1");
            ////lLastCheckOK=System.currentTimeMillis();

            boolean bTFOK=true;
            /////bIsSDCardOK=false;
            Log.e("CheckSDCard","CheckSDCard.");
            try
            {
                if (mcryptoUtil!=null)
                {
                    try
                    {
                        mcryptoUtil.logout();
                        mcryptoUtil=null;
                    }
                    catch(Exception ex1)
                    {
                        ex1.printStackTrace();
                    }
                }
                try {
                    mcryptoUtil = new MCryptoUtil();
                    try {
                        Log.e("CheckSDCard", "mcryptoUtil.init ...");
                        mcryptoUtil.init(ctx, "12345678");
                        Log.e("CheckSDCard", "mcryptoUtil.init ok");
                    } catch (Exception e2) {
                        bTFOK = false;
                        e2.printStackTrace();
                        ///return;
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }

                try
                {
                    ///////////////切换用户要清理AES KEY
                    /////////////2016-09-29,置私钥为空。
                    ////xechwic.android.util.RSAUtil.privatekey=null;
                    //////if (bUserChanged)
                    ///if ((sAESPassWord==null)||(xechwic.android.util.RSAUtil.privatekey==null))
                    if (bTFOK)
                    {
                        ////Log.i("MCryptoLMSDImpl test", new String(Base64.encode(pub.getEncoded())));
                        try {
                            byte[] data = "Hellow World".getBytes();
                            byte[] cipher = mcryptoUtil.rsaEncrypt(sTag, data);
                            Log.e("XIM", "mcryptoUtil.rsaEncrypt:" + (new String(cipher)));
                            byte[] plain = mcryptoUtil.rsaDecrypt(sTag, cipher);
                            Log.e("XIM","mcryptoUtil.rsaDecrypt:"+ new String (plain));
                            if (!"Hellow World".equals(new String(plain))) {
                                bTFOK = false;
                                Log.e("XIM","mcryptoUtil.rsaDecrypt failed!");
                            }
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                            bTFOK = false;
                        }

                        if (bTFOK) {
                            if (sAESPassWord != null) {
                                byte[] btAES = null;
                                try {
                                    btAES = mcryptoUtil.getAES(sTag);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    bTFOK = false;
                                }
                                if ( (btAES==null) || !sAESPassWord.equals(new String(btAES, "iso-8859-1"))) {
                                    bTFOK = false;
                                }
                            }
                        }
                   }
                    else  //////////2016-10-26,无卡时本地消息也要用aes加密
                    {
                        sAESPassWord=cont_AES;
                    }

                    /////Log.e("TFCARD","doCheckSDCard 3");


                    bIsSDCardOK=bTFOK;
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    bIsSDCardOK=false;
                }
            }
            catch(Exception ex)
            {
                bIsSDCardOK=false;
                ex.printStackTrace();
            }

            ////////////2016-11-22
            if (!bIsSDCardOK)
                sAESPassWord=null;


            ///////////退出tf卡
            if (mcryptoUtil != null) {
                try {
                    mcryptoUtil.logout();
                    mcryptoUtil = null;
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }

            ////bUserChanged=false; ///////////////处理完
        }
    }


    static public  synchronized byte[] encrypt_data(byte [] data)
    {
        byte [] ret=data;

        if (!bIsSDCardOK)
            return data;

        if (data==null)
            return null;

        try
        {
            boolean bTFOK=true;
            if (mcryptoUtil != null) {
                try {
                    mcryptoUtil.logout();
                    mcryptoUtil = null;
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
            try {
                mcryptoUtil = new MCryptoUtil();
                try {
                    Log.e("CheckSDCard", "mcryptoUtil.init ...");
                    mcryptoUtil.init(MainApplication.getInstance(), "12345678");
                    Log.e("CheckSDCard", "mcryptoUtil.init ok");
                } catch (Exception e2) {
                    bTFOK = false;
                    e2.printStackTrace();
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }

            ret=new byte[0];
            int iPos=0;

            while (iPos<data.length) {
                int iHandle=(data.length-iPos)>32 ? 32: (data.length-iPos);

                byte[] btNew=new byte[iHandle];
                System.arraycopy(data,iPos,btNew,0,iHandle);
                byte[] encoded = Base64.encode(mcryptoUtil.rsaEncrypt(sTag, btNew));

                byte[] newData=new byte[ret.length+encoded.length+1];
                System.arraycopy(ret,0,newData,0,ret.length);
                System.arraycopy(encoded,0,newData,ret.length,encoded.length);
                newData[ret.length+encoded.length]='\t';

                ret=newData;
                iPos+=iHandle;
            }

            if (ret==null)
                return data;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return data;
        }

        if (mcryptoUtil != null) {
            try {
                mcryptoUtil.logout();
                mcryptoUtil = null;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
        return ret;
    }


    static public  synchronized byte[] decrypt_data(byte [] data)
    {
        byte [] ret=data;
        if (!bIsSDCardOK)
            return data;

        ret=new byte[0];
        try
        {
            boolean bTFOK=true;
            if (mcryptoUtil != null) {
                try {
                    mcryptoUtil.logout();
                    mcryptoUtil = null;
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
            try {
                mcryptoUtil = new MCryptoUtil();
                try {
                    Log.e("CheckSDCard", "mcryptoUtil.init ...");
                    mcryptoUtil.init(MainApplication.getInstance(), "12345678");
                    Log.e("CheckSDCard", "mcryptoUtil.init ok");
                } catch (Exception e2) {
                    bTFOK = false;
                    ///return;
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }

            String sText=new String(data,"ISO-8859-1");

            int iPos=0;
            while (sText.length()>0) {
                iPos=sText.indexOf("\t");
                String todecode="";


                if (iPos<0)
                {
                    todecode=sText;
                    sText="";
                }
                else
                {
                    todecode = sText.substring(0, iPos);
                    if (iPos<(sText.length()-1))
                        sText = sText.substring(iPos + 1).trim();
                    else
                        sText="";
                }

                byte[] deocded=mcryptoUtil.rsaDecrypt(sTag, Base64.decode(todecode.getBytes("iso-8859-1")));
                byte[] newData=new byte[ret.length+deocded.length];
                System.arraycopy(ret,0,newData,0,ret.length);
                System.arraycopy(deocded,0,newData,ret.length,deocded.length);

                ret=newData;
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return data;
        }
        if (mcryptoUtil != null) {
            try {
                mcryptoUtil.logout();
                mcryptoUtil = null;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }

        return ret;
    }

    static public  synchronized void unMountSDCard()
    {
        if(!AppConfig.SD_ENCODE){
            return ;
        }
        try
        {
            if (mcryptoUtil!=null)
            {
                mcryptoUtil.logout();
            }
        }
        catch(Exception ex1)
        {
        }
        mcryptoUtil=null;
    }

    ///////////////////////////使用sd卡方法对字节数组加密
    static public synchronized byte[] encrypt_aes(byte[] data)
    {
        if ((data==null) || (sAESPassWord==null))
            return data;
        try
        {
            /////Log.e("sdcard","encrypt_aes "+ RSAUtil.toHexString(sAESPassWord.getBytes("iso-8859-1")));

            byte []byteDst=new byte[data.length+16];
            int iLen=XWDataCenter.xwDC.XWNetphoneAESEncodeBytes(data,byteDst,(sAESPassWord+"\0").getBytes("iso-8859-1"));

            ///Log.e("sdcard","encrypt_aes 2 iLen:"+iLen);
            if (iLen>0)
            {
                byte [] ret=new byte[iLen];
                System.arraycopy(byteDst, 0, ret, 0, iLen);

                ret= Base64.encode(ret);

                /////Log.e("sdcard","encrypt_aes 3 ret:"+RSAUtil.toHexString(ret));

                return ret;
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return data;
    }


    ///////////////////////////使用sd卡方法对字节数组解密
    static public synchronized byte[] decrypt_aes(byte[] data)
    {

        if ((data==null) || (sAESPassWord==null))
            return data;
        try
        {
            /////Log.e("sdcard","decrypt_aes "+ RSAUtil.toHexString(sAESPassWord.getBytes("iso-8859-1")));

            data=Base64.decode(data);

            byte []byteDst=new byte[data.length];
            int iLen=XWDataCenter.xwDC.XWNetphoneAESDecodeBytes(data,byteDst,(sAESPassWord+"\0").getBytes("iso-8859-1"));

            ///Log.e("sdcard","decrypt_aes 2 iLen:"+iLen);

            if (iLen>0)
            {
                byte [] ret=new byte[iLen];
                System.arraycopy(byteDst, 0, ret, 0, iLen);
                return ret;
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        return data;
    }


    /////////////////////////////////////使用sd卡方法对文件加密
    static public synchronized boolean encrypt_aes_file(String sFile, String sToFile)
    {
        boolean bRet=false;
        if ((sFile==null)||(sToFile==null))
            return false;

        try
        {
            bRet= XWDataCenter.xwDC.AESEncodeFile(sFile, sToFile, sAESPassWord);
        }
        catch(Exception ex1)
        {
            ex1.printStackTrace();
        }

        return bRet;
    }

    /////////////////////////////////////使用sd卡方法对文件解密
    static public synchronized boolean decrypt_aes_file(String sFile, String sToFile)
    {
        boolean bRet=false;
        if ((sFile==null)||(sToFile==null))
            return false;

        try
        {
            bRet= XWDataCenter.xwDC.AESDecodeFile(sFile, sToFile, sAESPassWord);
        }
        catch(Exception ex1)
        {
            ex1.printStackTrace();
        }

        return bRet;
    }

    ////////////////用于处理用户
    static public  synchronized byte[] encrypt_userpassword(String sUserID,byte [] data)
    {
        byte [] ret=data;

        if ( (sUserID==null) || (sUserID.length()==0) || (sUserID.equalsIgnoreCase("null")))
            return data;

        try
        {

            ///Log.e("TFCARD","encrypt_userpassword 1");
            setKeyID(sUserID);
            ////lLastCheckOK=0;
            doCheckSDCard(MainApplication.getInstance());
           /// Log.e("TFCARD","encrypt_userpassword 3");

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return data;
        }
        return encrypt_data(data);
    }


    static public  synchronized byte[] decrypt_userpassword(String sUserID,byte [] data)
    {
        byte [] ret=data;

        if ( (sUserID==null) || (sUserID.length()==0) || (sUserID.equalsIgnoreCase("null")))
            return data;

        try
        {
            ///Log.e("TFCARD","decrypt_userpassword 1");
            setKeyID(sUserID);
            ////lLastCheckOK=0;
            doCheckSDCard(MainApplication.getInstance());
            /////Log.e("TFCARD","decrypt_userpassword 3");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return data;
        }
        return decrypt_data(data);
    }

    static public  synchronized void removekeys(String sUser)
    {
        if(!AppConfig.SD_ENCODE){
            return ;
        }
        try
        {
            //Log.e("TFCARD","removekeys 1");
            setKeyID(sUser);
            ////lLastCheckOK=0;
            doCheckSDCard(MainApplication.getInstance());
            ///Log.e("TFCARD","removekeys 3");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        boolean bTFOK=true;
        if (mcryptoUtil != null) {
            try {
                mcryptoUtil.logout();
                mcryptoUtil = null;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
        try {
            mcryptoUtil = new MCryptoUtil();
            try {
                Log.e("CheckSDCard", "mcryptoUtil.init ...");
                mcryptoUtil.init(MainApplication.getInstance(), "12345678");
                Log.e("CheckSDCard", "mcryptoUtil.init ok");
            } catch (Exception e2) {
                bTFOK = false;
                ///return;
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        /////先删除AES文件，再删除容器
        if (!bTFOK)
            return;

        try
        {
            mcryptoUtil.deleteAllAESKey();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }


        /*try
        {
            String[] keys=mcryptoUtil.listRSAKey(MainApplication.getInstance());
            int i;
            for (i=0;i<keys.length;i++) {
                mcryptoUtil.deleteRSAKey(keys[i]);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }*/


        sAESPassWord=null;

        if (mcryptoUtil != null) {
            try {
                mcryptoUtil.logout();
                mcryptoUtil = null;
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
    }
}
