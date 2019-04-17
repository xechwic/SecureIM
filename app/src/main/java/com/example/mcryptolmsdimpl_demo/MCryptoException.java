package com.example.mcryptolmsdimpl_demo;



public class MCryptoException extends Exception {
    public static final int DEVICE_NOT_CONNECTED = 100;
    public static final int DEVICE_NOT_INIT = 101;
    public static final int INIT_ERROR = 102;
    public static final int LOGIN_ERROR = 103;
    public static final int LOGOUT_ERROR = 104;
    public static final int DISCONNECT_ERROR = 105;
    public static final int DEVICE_NOT_LOGIN = 106;
    public static final int DIGEST_TYPE_INVALID = 107;
    public static final int TOBESIGN_DECODE_ERROR = 108;
    public static final int SIGN_ERROR = 109;
    public static final int CERT_TYPE_INVALID = 110;
    public static final int GET_CERTIFICATE_ERROR = 111;
    public static final int GET_EMAIL_ERROR = 112;
    public static final int GET_PKCS7_SIGNATURE_VALUE_ERROR = 113;
    public static final int PKCS7_SIGNATURE_INVALID = 114;
    public static final int CERTIFICATE_DECODE_ERROR = 115;
    public static final int TOKEN_NOT_PRESENT_ERROR = 116;
    public static final int TOBEDIGEST_DECODE_ERROR = 117;
    public static final int NO_SUCH_ALGORITHM_ERROR = 118;
    public static final int NO_SPECIFIED = 119;
    public static final int GET_TOKEN_SERIALNUMBER_ERROR = 120;
    int a;

    public MCryptoException() {
    }

    public MCryptoException(int var1) {
        this.a = var1;
    }

    public MCryptoException(String var1) {
        super(var1);
    }

    public MCryptoException(Throwable var1) {
        super(var1);
    }

    public MCryptoException(String var1, Throwable var2) {
        super(var1, var2);
    }

    public MCryptoException(int var1, String var2) {
        super(var2);
        this.a = var1;
    }

    public MCryptoException(int var1, Throwable var2) {
        super(var2);
        this.a = var1;
    }

    public MCryptoException(int var1, String var2, Throwable var3) {
        super(var2, var3);
        this.a = var1;
    }

    public int getErrorCode() {
        return this.a;
    }
}
