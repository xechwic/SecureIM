package com.example.mcryptolmsdimpl_demo;


import android.content.Context;
import android.util.Log;

import com.example.mcryptolmsdimpl_demo.asn1.pkcs.PrivateKeyInfo;
import com.example.mcryptolmsdimpl_demo.asn1.pkcs.RSAPrivateKeyStructure;
import com.longmai.security.plugin.SOF_AppLib;
import com.longmai.security.plugin.SOF_DeviceLib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class MCryptoLMSDImpl implements com.example.mcryptolmsdimpl_demo.MCrypto {
    static String TAG = "MCryptoLMSDImpl";
    private static String authCode = "12345678";
    private static String appName = "TFKRSA";
    private static String soPin = "admin";
    private static String provider = "BC";
    private Context mContext = null;
    private SOF_AppLib mSOFLib = null;
    private Map fileMap = new TreeMap();
    private int ret = 0;

    public MCryptoLMSDImpl(Context var1) {
        this.mContext = var1;
    }

    public synchronized void init(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        MCryptoSDKInit var2 = new MCryptoSDKInit();
        var2.init(this.mContext, this.mContext.getAssets(), var1);
        this.ret = SOF_DeviceLib.SOF_LoadLibrary(this.mContext, "0", "mToken TF/SD Card", "com.longmai.security.plugin.driver.tf.TFDriver", "TFKRSA");
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("LoadLibrary Error : " + SOF_DeviceLib.SOF_GetLastError());
        } else {
            ArrayList tfList = new ArrayList();
            this.ret = SOF_DeviceLib.SOF_EnumDevices(this.mContext, tfList);
            if(this.ret != 0) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("can not find sd card : " + SOF_DeviceLib.SOF_GetLastError());
            } else if(tfList.size() <= 0) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("scan devices error : " + SOF_DeviceLib.SOF_GetLastError());
            } else {
                for(int i = 0; i < tfList.size(); ++i) {
                    this.ret = SOF_DeviceLib.SOF_Connect((String)tfList.get(i), authCode);
                    if(this.ret == 0) {
                        this.mSOFLib = SOF_DeviceLib.SOF_GetInstance(appName);
                        if(this.mSOFLib != null) {
                            break;
                        }
                    }
                }

            }
        }
    }

    public synchronized void disconnect() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.mSOFLib = null;
        this.fileMap = null;
        this.ret = SOF_DeviceLib.SOF_Disconnect();
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("disconnect fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    public synchronized void login(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        int[] var2 = new int[1];
        this.ret = this.mSOFLib.SOF_Login(var1, var2);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("login fail : " + SOF_DeviceLib.SOF_GetLastError() + ", left times : " + var2[0]);
        }
    }

    public synchronized void logout() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.fileMap = null;
        this.ret = this.mSOFLib.SOF_Logout();
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("logout fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    public synchronized void changePin(String var1, String var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        int[] var3 = new int[1];
        this.ret = this.mSOFLib.SOF_ChanegPassWd(var1, var2, var3);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("change pincode fail : " + SOF_DeviceLib.SOF_GetLastError() + ", left times : " + var3[0]);
        }
    }

    public synchronized int unblock(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        int[] var2 = new int[1];
        this.ret = this.mSOFLib.SOF_UnblockPIN(soPin, var1, var2);
        if(this.ret == 0) {
            return 0;
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("unblock fail : " + SOF_DeviceLib.SOF_GetLastError() + ", left times : " + var2[0]);
        }
    }

    public synchronized String[] listKey() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        ArrayList var1 = new ArrayList();
        if(this.mSOFLib.SOF_EnumContainers(var1) == 0) {
            String[] var2 = new String[var1.size()];
            var2 = (String[])var1.toArray(var2);
            return var2;
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get key labels fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    public synchronized String findKeyLabelByCert(byte[] var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var3 = this.listKey();
        X509Certificate var4 = null;

        for(int var5 = 0; var5 < var3.length; ++var5) {
            if(this.c(var3[var5])) {
                var4 = this.getSignCertificate(var3[var5]);
                if(Arrays.equals(var4.getIssuerX500Principal().getEncoded(), var1) && Arrays.equals(var4.getSerialNumber().toString().getBytes(), var2)) {
                    return var3[var5];
                }
            }

            if(this.d(var3[var5])) {
                var4 = this.getEncryptCertificate(var3[var5]);
                if(Arrays.equals(var4.getIssuerX500Principal().getEncoded(), var1) && Arrays.equals(var4.getSerialNumber().toString().getBytes(), var2)) {
                    return var3[var5];
                }
            }
        }

        return null;
    }

    public synchronized X509Certificate getEncryptCertificate(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.d(var1)) {
            return this.a(0, var1);
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist encrypt certificate");
        }
    }

    public synchronized X509Certificate getEncryptCertificate() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var1 = this.listKey();

        for(int var2 = 0; var2 < var1.length; ++var2) {
            if(this.d(var1[var2])) {
                Log.i(TAG, "key label of certificate : " + var1[var2]);
                return this.a(0, var1[var2]);
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist any encrypt certificate");
    }

    public synchronized X509Certificate getSignCertificate(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.c(var1)) {
            return this.a(1, var1);
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist sign certificate");
        }
    }

    public synchronized X509Certificate getSignCertificate() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var1 = this.listKey();

        for(int var2 = 0; var2 < var1.length; ++var2) {
            if(this.c(var1[var2])) {
                Log.i(TAG, "key label of certificate : " + var1[var2]);
                return this.a(1, var1[var2]);
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist any sign certificate");
    }

    public synchronized X509Certificate getX509Certificate(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.getSignCertificate(var1);
    }

    public synchronized X509Certificate getX509Certificate() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.getSignCertificate();
    }

    public synchronized void importCert(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.importSignCert(var1, var2);
    }

    public synchronized void importCert(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.importSignCert(var1);
    }

    public synchronized byte[] exportCert(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        try {
            return this.getSignCertificate(var1).getEncoded();
        } catch (CertificateEncodingException var3) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException(var3.getMessage());
        }
    }

    public synchronized byte[] exportCert() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        try {
            return this.getSignCertificate().getEncoded();
        } catch (CertificateEncodingException var2) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException(var2.getMessage());
        }
    }

    public synchronized void deleteCert(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.deleteContainer(var1);
        this.createContainer(var1);
    }

    public synchronized void deleteCert() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String var1 = this.listKey()[0];
        Log.i(TAG, "key label to delete certificate : " + var1);
        this.deleteContainer(var1);
        this.createContainer(var1);
    }

    public synchronized byte[] exportPublicKeyE(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.exportSignPublicKeyE(var1);
    }

    public synchronized byte[] exportPublicKeyE() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.exportSignPublicKeyE();
    }

    public synchronized byte[] exportPublicKeyM(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.exportSignPublicKeyM(var1);
    }

    public synchronized byte[] exportPublicKeyM() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.exportSignPublicKeyM();
    }

    public synchronized byte[] sign(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.sign(var1, var2, "SHA1");
    }

    public synchronized byte[] sign(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var2 = this.listKey();

        for(int var3 = 0; var3 < var2.length; ++var3) {
            if(this.a(var2[var3])) {
                Log.i(TAG, "key label to sign : " + var2[var3]);
                return this.sign(var2[var3], var1, "SHA1");
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
    }

    public synchronized byte[] sign(String var1, byte[] var2, String var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.a(var1)) {
            byte[] var4 = new byte[2048];
            int[] var5 = new int[1];
            boolean var6 = false;
            String var7 = var3.toUpperCase(Locale.ENGLISH);
            byte var8 = -1;
            switch(var7.hashCode()) {
                case -1850268089:
                    if(var7.equals("SHA256")) {
                        var8 = 2;
                    }
                    break;
                case -1850267037:
                    if(var7.equals("SHA384")) {
                        var8 = 3;
                    }
                    break;
                case -1850265334:
                    if(var7.equals("SHA512")) {
                        var8 = 4;
                    }
                    break;
                case 76158:
                    if(var7.equals("MD5")) {
                        var8 = 0;
                    }
                    break;
                case 2543909:
                    if(var7.equals("SHA1")) {
                        var8 = 1;
                    }
            }

            short var9;
            switch(var8) {
                case 0:
                    var9 = 129;
                    break;
                case 1:
                    var9 = 2;
                    break;
                case 2:
                    var9 = 4;
                    break;
                case 3:
                    var9 = 130;
                    break;
                case 4:
                    var9 = 131;
                    break;
                default:
                    throw new com.example.mcryptolmsdimpl_demo.MCryptoException("not support algorithm : " + var3);
            }

            this.ret = this.mSOFLib.SOF_SignData(var1, 1, var9, var2, var2.length, var4, var5);
            if(this.ret == 0) {
                byte[] var10 = new byte[var5[0]];
                System.arraycopy(var4, 0, var10, 0, var5[0]);
                return var10;
            } else {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("sign data fail : " + SOF_DeviceLib.SOF_GetLastError());
            }
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
        }
    }

    public synchronized byte[] sign(byte[] var1, String var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var3 = this.listKey();

        for(int var4 = 0; var4 < var3.length; ++var4) {
            if(this.a(var3[var4])) {
                return this.sign(var3[var4], var1, var2);
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
    }

    public synchronized byte[] encrypt(String keylabel, byte[] data) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        /**
         * 先判断容器类型后导出公钥进行加密
         */
        //判断容器类型
        if ((keylabel==null)||(data==null))
        {
            return data;
        }
        SOF_AppLib app=this.mSOFLib;

        int[] containerType = new int[1];
        int[] signKeyLen = new int[1];
        int[] exchKeyLen = new int[1];
        int[] signCertFlag = new int[1];
        int[] exchCertFlag = new int[1];

        int rtn = app.SOF_GetContainerInfo(keylabel, containerType, signKeyLen, exchKeyLen, signCertFlag, exchCertFlag);
        if(rtn != 0){
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("检测容器类型失败，错误码:" + SOF_DeviceLib.SOF_GetLastError());
        }
        if(containerType[0] == 0){
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("容器中没有任何证书，无法继续!");
        }
        //导出加密证书公钥
        byte[] pubKeyBlob = new byte[4096];
        int[] blobLen = new int[1];
        rtn = app.SOF_ExportPublicKeyBlob(keylabel, 0, pubKeyBlob, blobLen);
        if(rtn != 0){
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("导出交换证书公钥失败，错误码:" + SOF_DeviceLib.SOF_GetLastError());
        }

        byte[]input = data;

        byte[] output = new byte[data.length*2+2048];
        int[] outputLen = new int[1];

        int algoId = 0;
        if(containerType[0] == 1){
            algoId = SOF_DeviceLib.SGD_RSA;
        }else if(containerType[0] == 2) {
            algoId = SOF_DeviceLib.SGD_SM2_1;
        }

        long l = System.currentTimeMillis();

        rtn = app.SOF_ExtPublicEncrypt(pubKeyBlob, blobLen[0], algoId, input, input.length, output, outputLen);
        if(rtn == 0){
        }else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("加密失败，错误码:" + SOF_DeviceLib.SOF_GetLastError());
        }

        byte[] retdata=new byte[outputLen[0]];
        System.arraycopy(output,0,retdata,0,outputLen[0]);
        return retdata;

        /*if(this.authCode(var1)) {
            byte[] var3 = new byte[var2.length + 2048];
            int[] var4 = new int[1];
            this.ret = this.mSOFLib.SOF_PublicEncrypt(var1, 0, var2, var2.length, var3, var4);
            if(this.ret == 0) {
                byte[] var5 = new byte[var4[0]];
                System.arraycopy(var3, 0, var5, 0, var4[0]);
                return var5;
            } else {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("encrypt fail : " + SOF_DeviceLib.SOF_GetLastError());
            }
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
        }*/
    }

    public synchronized byte[] encrypt(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var2 = this.listKey();

        for(int var3 = 0; var3 < var2.length; ++var3) {
            if(this.b(var2[var3])) {
                Log.i(TAG, "key label to encryt : " + var2[var3]);
                return this.encrypt(var2[var3], var1);
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
    }

    public synchronized byte[] decrypt(String keylabel, byte[] data) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        //判断容器类型
        if ((keylabel==null)||(data==null))
        {
            return data;
        }
        SOF_AppLib app=this.mSOFLib;

        byte[] input = data;
        byte[] output = new byte[data.length+2048];
        int[] outputLen = new int[1];
        int rtn = app.SOF_PrivateDecrypt(keylabel, 0, input, input.length, output, outputLen);
        if(rtn == 0){
        }else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("解密失败，错误码:" + SOF_DeviceLib.SOF_GetLastError());
        }
        byte[] retdata=new byte[outputLen[0]];
        System.arraycopy(output,0,retdata,0,outputLen[0]);
        return retdata;

        /*if(this.authCode(var1)) {
            byte[] var3 = new byte[var2.length];
            int[] var4 = new int[1];
            this.ret = this.mSOFLib.SOF_PrivateDecrypt(var1, 0, var2, var2.length, var3, var4);
            if(this.ret == 0) {
                byte[] var5 = new byte[var4[0]];
                System.arraycopy(var3, 0, var5, 0, var4[0]);
                return var5;
            } else {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("decrypt fail : " + SOF_DeviceLib.SOF_GetLastError());
            }
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
        }*/
    }

    public synchronized byte[] decrypt(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var2 = this.listKey();

        for(int var3 = 0; var3 < var2.length; ++var3) {
            if(this.b(var2[var3])) {
                Log.i(TAG, "key label to decrypt : " + var2[var3]);
                byte[] var4 = new byte[var1.length];
                int[] var5 = new int[1];
                this.ret = this.mSOFLib.SOF_PrivateDecrypt(var2[var3], 0, var1, var1.length, var4, var5);
                if(this.ret == 0) {
                    byte[] var6 = new byte[var5[0]];
                    System.arraycopy(var4, 0, var6, 0, var5[0]);
                    return var6;
                }
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist currect key to decrypt");
    }

    public synchronized void genRSAKeyPair(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.genSignRSAKeyPair(var1);
    }

    public synchronized void genRSAKeyPair() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.genSignRSAKeyPair();
    }

    public synchronized void createContainer(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.ret = this.mSOFLib.SOF_CreateContainer(var1);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("create container " + var1 + " fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    public synchronized void deleteContainer(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.ret = this.mSOFLib.SOF_DeleteContainer(var1);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("delete container " + var1 + " fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    public synchronized String getContainerInfo(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        int[] var2 = new int[1];
        int[] var3 = new int[1];
        int[] var4 = new int[1];
        int[] var5 = new int[1];
        int[] var6 = new int[1];
        this.ret = this.mSOFLib.SOF_GetContainerInfo(var1, var2, var3, var4, var5, var6);
        if(this.ret == 0) {
            return "container name : " + var1 + "\ncontainer type : " + var2[0] + "\nexchange key length : " + var4[0] + "\nsign key length : " + var3[0] + "\nexchange certificate existed : " + var6[0] + "\nsign certificate existed : " + var5[0];
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get container\'s info fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    public synchronized boolean verify(String var1, byte[] var2, byte[] var3, String var4) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.a(var1)) {
            byte[] var5 = new byte[2048];
            int[] var6 = new int[1];
            boolean var7 = false;
            String var8 = var4.toUpperCase(Locale.ENGLISH);
            byte var9 = -1;
            switch(var8.hashCode()) {
                case -1850268089:
                    if(var8.equals("SHA256")) {
                        var9 = 2;
                    }
                    break;
                case -1850267037:
                    if(var8.equals("SHA384")) {
                        var9 = 3;
                    }
                    break;
                case -1850265334:
                    if(var8.equals("SHA512")) {
                        var9 = 4;
                    }
                    break;
                case 76158:
                    if(var8.equals("MD5")) {
                        var9 = 0;
                    }
                    break;
                case 2543909:
                    if(var8.equals("SHA1")) {
                        var9 = 1;
                    }
            }

            short var12;
            switch(var9) {
                case 0:
                    var12 = 129;
                    break;
                case 1:
                    var12 = 2;
                    break;
                case 2:
                    var12 = 4;
                    break;
                case 3:
                    var12 = 130;
                    break;
                case 4:
                    var12 = 131;
                    break;
                default:
                    throw new com.example.mcryptolmsdimpl_demo.MCryptoException("without algorithm : " + var4);
            }

            this.ret = this.mSOFLib.SOF_ExportPublicKeyBlob(var1, 1, var5, var6);
            if(this.ret == 0) {
                byte[] var13 = new byte[2048];
                int[] var14 = new int[1];
                this.ret = this.mSOFLib.SOF_DigestData(var12, var2, var2.length, var13, var14);
                if(this.ret == 0) {
                    byte[] var10 = new byte[var6[0]];
                    System.arraycopy(var5, 0, var10, 0, var6[0]);
                    byte[] var11 = new byte[var14[0]];
                    System.arraycopy(var13, 0, var11, 0, var14[0]);
                    this.ret = this.mSOFLib.SOF_VerifySignedData(var10, var6[0], 65536, var12, var11, var14[0], var3, var3.length);
                    return this.ret == 0;
                } else {
                    throw new com.example.mcryptolmsdimpl_demo.MCryptoException("create digest fail : " + SOF_DeviceLib.SOF_GetLastError());
                }
            } else {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("export public key blob fail : " + SOF_DeviceLib.SOF_GetLastError());
            }
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
        }
    }

    public synchronized boolean verify(byte[] var1, byte[] var2, String var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var4 = this.listKey();

        for(int var5 = 0; var5 < var4.length; ++var5) {
            if(this.a(var4[var5]) && this.verify(var4[var5], var1, var2, var3)) {
                Log.i(TAG, "key label to verify : " + var4[var5]);
                return true;
            }
        }

        return false;
    }

    public synchronized void importSignCert(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.findContainer(var1)) {
            if(this.c(var1)) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("This key label already exist sign certificate");
            }
        } else {
            this.createContainer(var1);
        }

        this.a(var1, (byte[])var2, 1);
    }

    public synchronized void importSignCert(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String var2 = UUID.randomUUID().toString();
        if(this.findContainer(var2)) {
            this.importSignCert(var1);
        } else {
            Log.i(TAG, "key label to import sign certificate : " + var2);
            this.createContainer(var2);
            this.a(var2, (byte[])var1, 1);
        }

    }

    public synchronized void importEncryptCert(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.findContainer(var1)) {
            if(this.d(var1)) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("This key label already exist encrypt certificate");
            }
        } else {
            this.createContainer(var1);
        }

        this.a(var1, (byte[])var2, 0);
    }

    public synchronized void importEncryptCert(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String var2 = UUID.randomUUID().toString();
        if(this.findContainer(var2)) {
            this.importEncryptCert(var1);
        } else {
            Log.i(TAG, "key label to import encrypt certificate : " + var2);
            this.createContainer(var2);
            this.a(var2, (byte[])var1, 0);
        }

    }

    public synchronized byte[] exportSignPublicKeyE(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.a(var1)) {
            return this.a(var1, 1);
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
        }
    }

    public synchronized byte[] exportSignPublicKeyE() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var1 = this.listKey();

        for(int var2 = 0; var2 < var1.length; ++var2) {
            if(this.a(var1[var2])) {
                Log.i(TAG, "key label of certificate : " + var1[var2]);
                return this.a(var1[var2], 1);
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
    }

    public synchronized byte[] exportSignPublicKeyM(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.a(var1)) {
            return this.b(var1, 1);
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
        }
    }

    public synchronized byte[] exportSignPublicKeyM() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var1 = this.listKey();

        for(int var2 = 0; var2 < var1.length; ++var2) {
            if(this.a(var1[var2])) {
                Log.i(TAG, "key label of certificate : " + var1[var2]);
                return this.b(var1[var2], 1);
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
    }

    public synchronized byte[] exportEncryptPublicKeyE(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.b(var1)) {
            return this.a(var1, 0);
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
        }
    }

    public synchronized byte[] exportEncryptPublicKeyE() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var1 = this.listKey();

        for(int var2 = 0; var2 < var1.length; ++var2) {
            if(this.b(var1[var2])) {
                Log.i(TAG, "key label of certificate : " + var1[var2]);
                return this.a(var1[var2], 0);
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
    }

    public synchronized byte[] exportEncryptPublicKeyM(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.b(var1)) {
            return this.b(var1, 0);
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
        }
    }

    public synchronized byte[] exportEncryptPublicKeyM() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] var1 = this.listKey();

        for(int var2 = 0; var2 < var1.length; ++var2) {
            if(this.b(var1[var2])) {
                Log.i(TAG, "key label of certificate : " + var1[var2]);
                return this.b(var1[var2], 0);
            }
        }

        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist key");
    }

    public synchronized void importSignRSAKeyPair(String var1, KeyPair var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.findContainer(var1)) {
            if(this.a(var1)) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("This key label already exist sign key pair");
            }
        } else {
            this.createContainer(var1);
        }

        this.a(var1, (KeyPair)var2, 1);
    }

    public synchronized void importSignRSAKeyPair(KeyPair var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String var2 = UUID.randomUUID().toString();
        if(this.findContainer(var2)) {
            this.importSignRSAKeyPair(var1);
        } else {
            Log.i(TAG, "key label to import sign key pair : " + var2);
            this.createContainer(var2);
            this.a(var2, (KeyPair)var1, 1);
        }

    }

    public synchronized void importEncryptRSAKeyPair(String var1, KeyPair var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.findContainer(var1)) {
            if(this.b(var1)) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("This key label already exist encrypt key pair");
            }
        } else {
            this.createContainer(var1);
        }

        this.a(var1, (KeyPair)var2, 0);
    }

    public synchronized void importEncryptRSAKeyPair(KeyPair var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String var2 = UUID.randomUUID().toString();
        if(this.findContainer(var2)) {
            this.importEncryptRSAKeyPair(var1);
        } else {
            Log.i(TAG, "key label to import encrypt key pair : " + var2);
            this.createContainer(var2);
            this.a(var2, (KeyPair)var1, 0);
        }

    }

    public synchronized void genSignRSAKeyPair(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.findContainer(var1)) {
            if(this.a(var1)) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("This key label already exist sign key pair");
            }
        } else {
            this.createContainer(var1);
        }

        this.genRsaKeyPair(var1, 1);
    }

    public synchronized void genSignRSAKeyPair() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String var1 = UUID.randomUUID().toString();
        if(this.findContainer(var1)) {
            this.genSignRSAKeyPair();
        } else {
            Log.i(TAG, "key label to generate sign RSA key pair : " + var1);
            this.createContainer(var1);
            this.genRsaKeyPair(var1, 1);
        }

    }

    public synchronized void genEncryptRSAKeyPair(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(this.findContainer(var1)) {
            if(this.b(var1)) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("This key label already exist encrypt key pair");
            }
        } else {
            this.createContainer(var1);
        }

        this.genRsaKeyPair(var1, 0);
    }

    public synchronized void genEncryptRSAKeyPair() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String var1 = UUID.randomUUID().toString();
        if(this.findContainer(var1)) {
            this.genEncryptRSAKeyPair();
        } else {
            Log.i(TAG, "key label to generate encrypt RSA key pair : " + var1);
            this.createContainer(var1);
            this.genRsaKeyPair(var1, 0);
        }

    }

    private synchronized boolean a(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.checkContainerInfo(var1)[0];
    }

    private synchronized boolean b(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.checkContainerInfo(var1)[1];
    }

    private synchronized boolean c(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.checkContainerInfo(var1)[2];
    }

    private synchronized boolean d(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        return this.checkContainerInfo(var1)[3];
    }

    private synchronized boolean[] checkContainerInfo(String container) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        int[] containerType = new int[1];
        int[] signKeyLen = new int[1];
        int[] exchKeyLen = new int[1];
        int[] signCertFlag = new int[1];
        int[] exchCertFlag = new int[1];
        this.ret = this.mSOFLib.SOF_GetContainerInfo(container, containerType, signKeyLen, exchKeyLen, signCertFlag, exchCertFlag);
        if(this.ret == 0) {
            boolean[] var7 = new boolean[]{signKeyLen[0] > 0, exchKeyLen[0] > 0, signCertFlag[0] == 1, exchCertFlag[0] == 1};
            return var7;
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get container info fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    private synchronized boolean findContainer(String containerName) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        String[] containers = this.listKey();
        if(containers.length == 0) {
            return false;
        } else {
            String[] containersArray = containers;
            int containerLength = containers.length;

            for(int i = 0; i < containerLength; ++i) {
                String container = containersArray[i];
                if(container.contentEquals(containerName)) {
                    return true;
                }
            }

            return false;
        }
    }

    private synchronized X509Certificate a(int var1, String var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        byte[] var3 = new byte[4096];
        int[] var4 = new int[1];
        this.ret = this.mSOFLib.SOF_ExportUserCert(var2, var1, var3, var4);
        if(this.ret == 0) {
            try {
                CertificateFactory var5 = CertificateFactory.getInstance("X.509", provider);
                ByteArrayInputStream var6 = new ByteArrayInputStream(var3, 0, var4[0]);
                X509Certificate var7 = (X509Certificate)var5.generateCertificate(var6);
                var6.close();
                return var7;
            } catch (CertificateException var8) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get certificate from CertificateFactory error : " + var8.getMessage());
            } catch (IOException var9) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("InputStream error : " + var9.getMessage());
            } catch (NoSuchProviderException var10) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("NoSuchProviderException error : " + var10.getMessage());
            }
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get certificate fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    private synchronized void a(String var1, byte[] var2, int var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        this.ret = this.mSOFLib.SOF_ImportCertificate(var1, var3, var2, var2.length);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("import certificate fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    private synchronized byte[] a(String var1, int var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        byte[] var3 = new byte[2048];
        int[] var4 = new int[1];
        this.ret = this.mSOFLib.SOF_ExportPublicKeyBlob(var1, var2, var3, var4);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("export public key Exponent fail : " + SOF_DeviceLib.SOF_GetLastError());
        } else {
            byte[] var5 = new byte[4];

            for(int var6 = 0; var6 < 4; ++var6) {
                var5[var6] = var3[var6 + var4[0] - 4];
            }

            return var5;
        }
    }

    private synchronized byte[] b(String var1, int var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        byte[] var3 = new byte[2048];
        int[] var4 = new int[1];
        this.ret = this.mSOFLib.SOF_ExportPublicKeyBlob(var1, var2, var3, var4);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("export public key Module fail : " + SOF_DeviceLib.SOF_GetLastError());
        } else {
            byte[] var5 = new byte[var4[0] - 8 - 4];

            for(int var6 = 0; var6 < var4[0] - 8 - 4; ++var6) {
                var5[var6] = var3[var6 + 8];
            }

            return var5;
        }
    }

    private synchronized void a(String var1, KeyPair var2, int var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        PrivateKeyInfo var4 = PrivateKeyInfo.getInstance(var2.getPrivate().getEncoded());
        RSAPrivateKeyStructure var5 = RSAPrivateKeyStructure.getInstance(var4.getPrivateKey());
        String var6 = "";
        String var7 = "00010000";
        String var8 = "00000800";
        String var9 = this.c(var5.getModulus().toString(16), 512);
        String var10 = this.c(var5.getPublicExponent().toString(16), 8);
        String var11 = this.c(var5.getPrivateExponent().toString(16), 512);
        String var12 = this.c(var5.getPrime1().toString(16), 256);
        String var13 = this.c(var5.getPrime2().toString(16), 256);
        String var14 = this.c(var5.getExponent1().toString(16), 256);
        String var15 = this.c(var5.getExponent2().toString(16), 256);
        String var16 = this.c(var5.getCoefficient().toString(16), 256);
        var6 = var7 + var8 + var9 + var10 + var11 + var12 + var13 + var14 + var15 + var16;
        char[] var17 = var6.toCharArray();
        int var18 = var17.length / 2;
        byte[] var19 = new byte[var18];

        for(int var20 = 0; var20 < var18; ++var20) {
            int var21 = var20 * 2;
            int var22 = Integer.parseInt(var6.substring(var21, var21 + 2), 16);
            var19[var20] = (byte)var22;
        }

        this.ret = this.mSOFLib.SOF_ImportExtRSAKeyPair(var1, var3, var19, var18);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("importPrivateKey fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    private synchronized String c(String var1, int var2) {
        return var1.length() > var2?var1.substring(var1.length() - var2, var1.length()):(var1.length() < var2?this.c("0" + var1, var2):var1);
    }

    private synchronized void genRsaKeyPair(String var1, int var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        byte[] pubKey = new byte[512];
        int[] keyLen = new int[1];
        this.ret = this.mSOFLib.SOF_GenKeyPair(var1, var2, SOF_DeviceLib.SGD_RSA, 1024, pubKey, keyLen);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("generate RSA key pair fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    public synchronized void genAESKey(String var1, int var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(var2 != 128 && var2 != 192 && var2 != 256) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("generate AES key with wrong key size : " + var2);
        } else {
            byte[] var3 = new byte[var2 / 8];
            this.ret = SOF_DeviceLib.SOF_GenRandom(var3.length, var3);
            if(this.ret != 0) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("generate AES key fail : " + SOF_DeviceLib.SOF_GetLastError());
            } else {
                this.storeAESKey(var1, var3);
            }
        }
    }

    public synchronized void deleteAESKey(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        ArrayList var2 = new ArrayList();
        this.ret = this.mSOFLib.SOF_EnumFiles(var2);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get files fail : " + SOF_DeviceLib.SOF_GetLastError());
        } else {
            /////byte[] var3 = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15};
            String var4 = "AES_" + /*new String(var3) +*/ var1;
            if(var2.contains(var4)) {
                this.ret = this.mSOFLib.SOF_DeleteFile(var4);
                if(this.ret != 0) {
                    throw new com.example.mcryptolmsdimpl_demo.MCryptoException("delete files fail : " + SOF_DeviceLib.SOF_GetLastError());
                } else {
                    if(this.fileMap.containsKey(var4)) {
                        this.fileMap.remove(var4);
                    }

                }
            } else {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist this AES key");
            }
        }
    }

    public synchronized byte[] aesEncrypt(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        byte[] var3 = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15};
        String var4 = "AES_" + new String(var3) + var1;
        Object var5 = null;
        byte[] var11;
        if(this.fileMap.containsKey(var4)) {
            var11 = (byte[])this.fileMap.get(var4);
        } else {
            var11 = this.getAESKey(var1);
            this.fileMap.put(var4, var11);
        }

        boolean var6 = false;
        int var12;
        switch(var11.length * 8) {
            case 128:
                var12 = -2147483374;
                break;
            case 192:
                var12 = -2147483358;
                break;
            case 256:
                var12 = -2147483326;
                break;
            default:
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("wrong key size");
        }

        byte[] var7 = new byte[var2.length * 2 + 2048];
        int[] var8 = new int[1];
        byte[] var9 = new byte[8];
        this.ret = this.mSOFLib.SOF_EncryptData(var12, var11, var11.length, 1, var9, var9.length, var2, var2.length, var7, var8);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("AES encrypt fail : " + SOF_DeviceLib.SOF_GetLastError());
        } else {
            byte[] var10 = new byte[var8[0]];
            System.arraycopy(var7, 0, var10, 0, var8[0]);
            return var10;
        }
    }

    public synchronized byte[] aesDecrypt(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] var3 = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15};
        String var4 = "AES_" + new String(var3) + var1;
        Object var5 = null;
        byte[] var11;
        if(this.fileMap.containsKey(var4)) {
            var11 = (byte[])this.fileMap.get(var4);
        } else {
            var11 = this.getAESKey(var1);
            this.fileMap.put(var4, var11);
        }

        boolean var6 = false;
        int var12;
        switch(var11.length * 8) {
            case 128:
                var12 = -2147483374;
                break;
            case 192:
                var12 = -2147483358;
                break;
            case 256:
                var12 = -2147483326;
                break;
            default:
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("wrong key size");
        }

        byte[] var7 = new byte[var2.length * 2 + 2048];
        int[] var8 = new int[1];
        byte[] var9 = new byte[8];
        this.ret = this.mSOFLib.SOF_DecryptData(var12, var11, var11.length, 1, var9, var9.length, var2, var2.length, var7, var8);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("AES decrypt fail : " + SOF_DeviceLib.SOF_GetLastError());
        } else {
            byte[] var10 = new byte[var8[0]];
            System.arraycopy(var7, 0, var10, 0, var8[0]);
            return var10;
        }
    }

    public synchronized void storeAESKey(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        if(var2.length != 16 && var2.length != 24 && var2.length != 32) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("wrong AES key size");
        } else {
            ////////byte[] var3 = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15};
            String var4 = "AES_" + /*new String(var3) +*/ var1;
            ArrayList var5 = new ArrayList();
            this.ret = this.mSOFLib.SOF_EnumFiles(var5);
            if(this.ret != 0) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get files fail : " + SOF_DeviceLib.SOF_GetLastError());
            } else if(var5.contains(var4)) {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("This AES key is already exist");
            } else {
                ////////////////
                //////this.b();
                byte[] var6 = this.encrypt( var1 /*this.getAESContainerName()*/, var2);
                this.ret = this.mSOFLib.SOF_CreaterFile(var4, var6.length, 16, 16);
                if(this.ret != 0) {
                    throw new com.example.mcryptolmsdimpl_demo.MCryptoException("sdcard create file fail : " + SOF_DeviceLib.SOF_GetLastError());
                } else {
                    this.ret = this.mSOFLib.SOF_WriteFile(var4, 0, var6, var6.length);
                    if(this.ret != 0) {
                        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("sdcard write file fail : " + SOF_DeviceLib.SOF_GetLastError());
                    }
                }
            }
        }
    }

    public synchronized byte[] getAESKey(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        ////byte[] var2 = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15};
        String var3 = "AES_" + /*new String(var2) +*/ var1;
        ArrayList var4 = new ArrayList();
        this.ret = this.mSOFLib.SOF_EnumFiles(var4);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get files fail : " + SOF_DeviceLib.SOF_GetLastError());
        } else if(!var4.contains(var3)) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t exist this AES key");
        } else {
            String var5 =var1;/// this.getAESContainerName();
            /*if(this.findContainer(var1))*/ {
                /*if(!this.b(var1)) {
                    throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t have RSA key pair for decrypting AES key");
                } else */
                {
                    byte[] var6 = new byte[256];
                    this.ret = this.mSOFLib.SOF_ReadFile(var3, 0, 256, var6);
                    if(this.ret != 0) {
                        throw new com.example.mcryptolmsdimpl_demo.MCryptoException("sdcard read file fail : " + SOF_DeviceLib.SOF_GetLastError());
                    } else {
                        return this.decrypt(var5, var6);
                    }
                }
            } /*else {
                throw new com.example.mcryptolmsdimpl_demo.MCryptoException("doesn\'t have RSA key pair for decrypting AES key");
            }*/
        }
    }


    public synchronized void deleteAllAESKey() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        ArrayList var4 = new ArrayList();
        this.ret = this.mSOFLib.SOF_EnumFiles(var4);
        if(this.ret != 0) {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get files fail : " + SOF_DeviceLib.SOF_GetLastError());
        }  else {
            for(int var5 = 0; var5 < var4.size(); ++var5) {
                try {
                    this.mSOFLib.SOF_DeleteFile((String) var4.get(var5));
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

      public synchronized String[] listAESKey() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        byte[] var1 = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15};
        String var2 = "AES_" + new String(var1);
        ArrayList var3 = new ArrayList();
        ArrayList var4 = new ArrayList();
        if(this.mSOFLib.SOF_EnumFiles(var3) == 0) {
            for(int var5 = 0; var5 < var3.size(); ++var5) {
                if(((String)var3.get(var5)).contains(var2)) {
                    var4.add(((String)var3.get(var5)).replace(var2, ""));
                }
            }

            String[] var6 = new String[var4.size()];
            var6 = (String[])var4.toArray(var6);
            return var6;
        } else {
            throw new com.example.mcryptolmsdimpl_demo.MCryptoException("get AES key labels fail : " + SOF_DeviceLib.SOF_GetLastError());
        }
    }

    //////AES与RSA使用同一个容器
    private synchronized String getAESContainerName() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
//        byte[] var1 = new byte[]{(byte)1, (byte)2, (byte)3, (byte)4, (byte)5, (byte)6, (byte)7, (byte)8, (byte)9, (byte)10, (byte)11, (byte)12, (byte)13, (byte)14, (byte)15, (byte)17, (byte)18, (byte)19, (byte)20, (byte)21, (byte)22, (byte)23, (byte)24, (byte)25, (byte)26, (byte)27, (byte)28, (byte)29, (byte)30, (byte)31};
//        return "RSAForAES_" + new String(var1);
        return MainActivity.sTag;
    }

    private synchronized void b() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        /*String var1 = this.getAESContainerName();
        if(this.findContainer(var1)) {
            if(this.b(var1)) {
                return;
            }
        } else {
            this.createContainer(var1);
        }

        this.genRsaKeyPair(var1, 0);
        */
    }

    public synchronized byte[] listPin() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized byte[] des3Decrypt(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized byte[] des3Encrypt(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized String getHotp() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized String getSIPInfo() throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized void importPrivateKey(String var1, byte[] var2, byte[] var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized void importPrivateKey(byte[] var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized void importPublicKey(String var1, byte[] var2, byte[] var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized void importPublicKey(byte[] var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized byte[] signRecovery(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized byte[] verifyRecovery(byte[] var1) throws MCryptoException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
