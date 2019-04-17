package xechwic.android.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luman on 2017/1/6 21:55
 * 文件类型操作
 */

public class MIMEUtil {

    private static MIMEUtil mimeUtil=null;
    public static MIMEUtil getInstance(){
        if(mimeUtil==null){
            mimeUtil=new MIMEUtil();
        }
        return mimeUtil;
    }

    private MIMEUtil(){}

    public void clearMIMEMap(){
        if(map!=null){
            map.clear();
            map=null;
        }
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     */
    public static String getMIMEType(File file) {

        String type="*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
	    /* 获取文件的后缀名*/
        String end=fName.substring(dotIndex,fName.length()).toLowerCase();
        if(TextUtils.isEmpty(end))return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        type=MIMEUtil.getInstance().getMIMEMap().get(end);
        Log.e("mime","type:"+type);
        return type;
    }

    public static boolean isOfficeFile(File file){
        String type="*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            return false;
        }
	    /* 获取文件的后缀名*/
        String end=fName.substring(dotIndex,fName.length()).toLowerCase();
        if(TextUtils.isEmpty(end))return false;
        if(end.equals(".doc")||end.equals(".docx")||end.equals(".xls")
            ||end.equals(".xlsx")||end.equals(".pps")||end.equals(".ppt")
            ||end.equals(".pptx")||end.equals(".rtf")||end.equals(".pptx")
                ||end.equals(".rtf")){
             return true;
        }

        return false;
    }
    //////去除一些特殊文件类型
    private Map<String,String> map;
    public  Map<String,String> getMIMEMap(){
        if(map==null){
            map=new HashMap<>();
            map.put(".3gp",    "video/3gpp");
            map.put(".apk",    "application/vnd.android.package-archive");
            map.put(".asf",    "video/x-ms-asf");
            map.put(".avi",    "video/x-msvideo");
//            map.put(".bin",    "application/octet-stream");
            map.put(".bmp",    "image/bmp");
//            map.put(".c",  "text/plain");
//            map.put(".class",  "application/octet-stream");
//            map.put(".conf",   "text/plain");
//            map.put(".cpp",    "text/plain");
            map.put(".doc",    "application/msword");
            map.put(".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            map.put(".xls",    "application/vnd.ms-excel");
            map.put(".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            map.put(".exe",    "application/octet-stream");
            map.put(".gif",    "image/gif");
            map.put(".gtar",   "application/x-gtar");
            map.put(".gz", "application/x-gzip");
//            map.put(".h",  "text/plain");
            map.put(".htm",    "text/html");
            map.put(".html",   "text/html");
//            map.put(".jar",    "application/java-archive");
//            map.put(".java",   "text/plain");
            map.put(".jpeg",   "image/jpeg");
            map.put(".jpg",    "image/jpeg");
//            map.put(".js", "application/x-javascript");
//            map.put(".log",    "text/plain");
            map.put(".m3u",    "audio/x-mpegurl");
            map.put(".m4a",    "audio/mp4a-latm");
            map.put(".m4b",    "audio/mp4a-latm");
            map.put(".m4p",    "audio/mp4a-latm");
            map.put(".m4u",    "video/vnd.mpegurl");
            map.put(".m4v",    "video/x-m4v");
            map.put(".mov",    "video/quicktime");
            map.put(".mp2",    "audio/x-mpeg");
            map.put(".mp3",    "audio/x-mpeg");
            map.put(".mp4",    "video/mp4");
            map.put(".mpc",    "application/vnd.mpohun.certificate");
            map.put(".mpe",    "video/mpeg");
            map.put(".mpeg",   "video/mpeg");
            map.put(".mpg",    "video/mpeg");
            map.put(".mpg4",   "video/mp4");
            map.put(".mpga",   "audio/mpeg");
            map.put(".msg",    "application/vnd.ms-outlook");
            map.put(".ogg",    "audio/ogg");
            map.put(".pdf",    "application/pdf");
            map.put(".png",    "image/png");
            map.put(".pps",    "application/vnd.ms-powerpoint");
            map.put(".ppt",    "application/vnd.ms-powerpoint");
            map.put(".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation");
//            map.put(".prop",   "text/plain");
//            map.put(".rc", "text/plain");
            map.put(".rmvb",   "audio/x-pn-realaudio");
            map.put(".rtf",    "application/rtf");
//            map.put(".sh", "text/plain");
            map.put(".tar",    "application/x-tar");
            map.put(".tgz",    "application/x-compressed");
            map.put(".txt",    "text/plain");
            map.put(".wav",    "audio/x-wav");
            map.put(".wma",    "audio/x-ms-wma");
            map.put(".wmv",    "audio/x-ms-wmv");
            map.put(".wps",    "application/vnd.ms-works");
            map.put(".xml",    "text/plain");
            map.put(".z",  "application/x-compress");
            map.put(".zip",    "application/x-zip-compressed");
            map.put("",        "*/*");
        }
        return map;
    }



}
