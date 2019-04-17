package xechwic.android.util;
public class Base64Util {
 public static byte[] encode(byte[] data) {
	 return org.bouncycastle.util.encoders.Base64.encode(data);
 }
 public static byte[] decode(byte[] data) {
        return org.bouncycastle.util.encoders.Base64.decode(data);
 }
}