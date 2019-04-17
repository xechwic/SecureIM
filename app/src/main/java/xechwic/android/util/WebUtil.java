package xechwic.android.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by luman on 2016/10/25 10:13
 * 网页操作工具类
 */

public class WebUtil {

    //打开浏览器
    public static void openBrowser(Context context,String url){
        try{
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            context.startActivity(intent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 解析网页内容，获取eqxiu音乐地址
     */
    public static String captureHtml(String strURL) throws Exception {
        URL url = new URL(strURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        InputStreamReader input = new InputStreamReader(httpConn
                .getInputStream(), "utf-8");
        BufferedReader bufReader = new BufferedReader(input);
        String line = "";
        StringBuilder contentBuf = new StringBuilder();
        while ((line = bufReader.readLine()) != null) {
            contentBuf.append(line);
        }
        String buf = contentBuf.toString();
        int beginIx = buf.indexOf("bgAudio:");
        int endIx = buf.indexOf(".mp3");
        String result = buf.substring(beginIx, endIx);
        if(result!=null){
            //bgAudio:{"url":"group1/M00/AD/10/yq0KXFYohiKAMRS_AAZDvg9SVe4427
            result=result.replace("bgAudio:{\"url\":\"", "");
            result="http://res.eqxiu.com/"+result+".mp3";
        }
        return result;
    }
}
