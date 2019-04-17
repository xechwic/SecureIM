package com.example.mcryptolmsdimpl_demo;

import android.content.Context;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MCryptoUtil {
	MCrypto mcrypto = null;
	
	public void init(Context mcontext, String pincode) throws MCryptoException {
		mcrypto = new MCryptoLMSDImpl(mcontext);
		mcrypto.init("longmai-roads365.properties");
		mcrypto.login(pincode);
	}
	
	public void logout() throws MCryptoException {
		try {
			mcrypto.logout();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		try {
			mcrypto.disconnect();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public String[] listKeys() throws MCryptoException {
		return mcrypto.listKey();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public void genRSAKey(String label) throws MCryptoException {
		((MCryptoLMSDImpl)mcrypto).genEncryptRSAKeyPair(label);
	}
	
	public byte[] rsaEncrypt(String Label, byte[] data) throws MCryptoException {
		return mcrypto.encrypt(Label, data);
	}
	
	public byte[] rsaDecrypt(String Label, byte[] cipher) throws MCryptoException {
		return mcrypto.decrypt(Label, cipher);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public void genAESKey(String label) throws MCryptoException {
		((MCryptoLMSDImpl)mcrypto).genAESKey(label, 256);
	}
	
	public byte[] aesEncrypt(String label, byte[] data) throws MCryptoException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		return ((MCryptoLMSDImpl)mcrypto).aesEncrypt(label, data);
	}
	
	public byte[] aesDecrypt(String label, byte[] cipher) throws MCryptoException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		return ((MCryptoLMSDImpl)mcrypto).aesDecrypt(label, cipher);
	}
	
	public void storeAES(String label, byte[] key) throws MCryptoException {
		((MCryptoLMSDImpl)mcrypto).storeAESKey(label, key);
	}
	
	public byte[] getAES(String label) throws MCryptoException {
		return ((MCryptoLMSDImpl)mcrypto).getAESKey(label);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	
	public PublicKey exportRSAPublicKey(String label) throws MCryptoException, InvalidKeySpecException, NoSuchAlgorithmException {
		byte[] publicKeyExponent = ((MCryptoLMSDImpl)mcrypto).exportEncryptPublicKeyE(label);
		byte[] publicKeyModulus = ((MCryptoLMSDImpl)mcrypto).exportEncryptPublicKeyM(label);
			
		BigInteger pubKeyM = new BigInteger(1, publicKeyModulus);
		BigInteger pubKeyE = new BigInteger(publicKeyExponent);
		return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(pubKeyM, pubKeyE));
	}
	
	public String[] listRSAKey(Context mcontext)  throws MCryptoException {
		MCrypto mcrypto = new MCryptoLMSDImpl(mcontext);
		mcrypto.init("longmai-roads365.properties");
		
		return mcrypto.listKey();
	}
	
	public void deleteRSAKey(String label) throws MCryptoException {
		((MCryptoLMSDImpl)mcrypto).deleteContainer(label);
	}
	
	public String[] listAESKey(Context mcontext)  throws MCryptoException {
		MCrypto mcrypto = new MCryptoLMSDImpl(mcontext);
		mcrypto.init("longmai-roads365.properties");
		
		return ((MCryptoLMSDImpl)mcrypto).listAESKey();
	}
	
	public void deleteAESKey(String label) throws MCryptoException {
		((MCryptoLMSDImpl)mcrypto).deleteAESKey(label);
	}

	/////////////清理所有aes key
	public void deleteAllAESKey() throws MCryptoException {
		((MCryptoLMSDImpl)mcrypto).deleteAllAESKey();
	}
}
