package xechwic.android.act;

import android.text.TextUtils;

/**
 * Created by luman on 2016/11/7 09:27
 *
 * 服务器配置
 */

public class ServerConfig {
    public static String XIM_TEST_DEF_IP="115.28.161.85";//"110.249.155.244";//"115.28.161.85";//测试默认IP
    public static String XIM_SERVER_IP="115.28.161.85";//"115.28.161.85";//服务器正式IP
    public static String XIM_SERVER_PORT="81";//服务器端口
    public static String XIM_SERVER_HOST="http://"+XIM_SERVER_IP+":"+XIM_SERVER_PORT;
    public static String APP_DOWNLOAD=XIM_SERVER_IP;
    public static String callback_url=XIM_SERVER_HOST+"/a2buser/xim_api_callback.php?user[]password[]caller_number[]called_number[]";
    public static String voucher_url=XIM_SERVER_HOST+"/a2buser/xim_api_voucher.php?user[]password[]";
    public static String server_app_name="ydx_securephone.apk";
    public static String server_versiontxt="ydx_securephone_ver.txt";
    public static String web_reg_url=XIM_SERVER_HOST+"/a2badmin/signup/tw/signup_mobile1.php";
    public static String JRS_VA_URL=XIM_SERVER_HOST+"/va_test.php";
    public static String ICONOPER_BASE_DIR=XIM_SERVER_HOST+"/a2buser/usericons";//网址
    public static String UPLOAD_HEAD_PIC="/Image_upload/Image_upload/upload/upload.php";//上传头像
    public static String GET_HEAD_PIC="/Image_upload/Image_upload/i/avatars/thumbs2/";//获取头像目录
    public static String GET_FPF_PIC="/getFriendProfilePic.php";//获取所有好友头像列表
    public static String GET_CONTACT_TYPE="/xim_api_isuserregister.php";//获取注册状态
    public static String GET_AHEAD="/getUserProfilePic.php";//获取一个好友头像

    public static void config(String ip){
        if(!TextUtils.isEmpty(ip)){
            XIM_SERVER_IP=ip;
            XIM_SERVER_HOST="http://"+XIM_SERVER_IP+":"+XIM_SERVER_PORT;
            APP_DOWNLOAD=XIM_SERVER_IP+":"+XIM_SERVER_PORT;
            callback_url=XIM_SERVER_HOST+"/a2buser/xim_api_callback.php?user[]password[]caller_number[]called_number[]";
            voucher_url=XIM_SERVER_HOST+"/a2buser/xim_api_voucher.php?user[]password[]";
            web_reg_url=XIM_SERVER_HOST+"/a2badmin/signup/tw/signup_mobile1.php";
            JRS_VA_URL=XIM_SERVER_HOST+"/va_test.php";
            ICONOPER_BASE_DIR=XIM_SERVER_HOST+"/a2buser/usericons";//网址
            UPLOAD_HEAD_PIC="/Image_upload/Image_upload/upload/upload.php";//上传头像
            GET_HEAD_PIC="/Image_upload/Image_upload/i/avatars/thumbs2/";//获取头像目录
            GET_FPF_PIC="/getFriendProfilePic.php";//获取所有好友头像列表
            GET_CONTACT_TYPE="/xim_api_isuserregister.php";//获取注册状态
            GET_AHEAD="/getUserProfilePic.php";//获取一个好友头像
        }
    }


}
