package xechwic.android.util;

import android.text.TextUtils;

import xechwic.android.XWCodeTrans;
import xechwic.android.bean.ChatMsgEntity;

/**
 * Created by luman on 2016/11/1 09:42
 * 消息处理工具类
 */

public class MessageUtil {

    ///将文本保存为文件
    public static String msg2File(String msg){
        if(!TextUtils.isEmpty(msg)){
            String path=UriConfig.getSavePath()+"/aesgotfiles/"+System.currentTimeMillis()+".txt";
            FileUtil.saveFile(msg.trim(),path);
            return path;
        }
        return "";
    }

    ///获取昵称
    public static String getFriendName(ChatMsgEntity entity){
        String nickName=entity.getName();
        if(TextUtils.isEmpty(nickName)){
            nickName=entity.getFriendAccount();
        }
        return nickName;
    }

    ////转换成功状态栏提示文字
    public static String getNoticeMsg(String content){
        if(content.startsWith("(:voice)")){
            content= xechwic.android.XWCodeTrans.doTrans("[语音]");
        }else if(content.startsWith("(:image")){
            content= xechwic.android.XWCodeTrans.doTrans("[图片]");
        }else if(content.startsWith("(:file)")){
            content=xechwic.android.XWCodeTrans.doTrans("[文件]");
        }
        return content;
    }
}
