package xechwic.android;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by luman on 2017/1/5 21:48
 */

public class DNSLookupThread extends Thread {
    private String hostname="www.baidu.com";
    private String sGetIP=null;

    public DNSLookupThread(String hostname) {
        this.hostname = hostname;
    }

    public void run() {
        try {
            InetAddress inetAddress = InetAddress.getByName(hostname);
            sGetIP=inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    public synchronized String getIP() {
        return sGetIP;
    }
}
