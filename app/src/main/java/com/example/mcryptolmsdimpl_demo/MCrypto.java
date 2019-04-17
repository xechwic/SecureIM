package com.example.mcryptolmsdimpl_demo;


import java.security.cert.X509Certificate;

public interface MCrypto {
    void init(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void changePin(String var1, String var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] decrypt(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] decrypt(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void disconnect() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] encrypt(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] encrypt(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] exportCert() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] exportCert(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    String getHotp() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void login(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void logout() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] sign(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] sign(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] sign(byte[] var1, String var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] sign(String var1, byte[] var2, String var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] listPin() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    String[] listKey() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    String getSIPInfo() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void genRSAKeyPair() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void genRSAKeyPair(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void importCert(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void importCert(String var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void deleteCert() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void deleteCert(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void importPrivateKey(byte[] var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void importPrivateKey(String var1, byte[] var2, byte[] var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void importPublicKey(byte[] var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    void importPublicKey(String var1, byte[] var2, byte[] var3) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    int unblock(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] exportPublicKeyM() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] exportPublicKeyM(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] exportPublicKeyE() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] exportPublicKeyE(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] des3Encrypt(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] des3Decrypt(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    X509Certificate getX509Certificate() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    X509Certificate getX509Certificate(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    String findKeyLabelByCert(byte[] var1, byte[] var2) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    X509Certificate getEncryptCertificate() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    X509Certificate getEncryptCertificate(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    X509Certificate getSignCertificate() throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    X509Certificate getSignCertificate(String var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] verifyRecovery(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;

    byte[] signRecovery(byte[] var1) throws com.example.mcryptolmsdimpl_demo.MCryptoException;
}

