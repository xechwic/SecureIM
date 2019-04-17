package xechwic.android.bus.event;

/**
 * Created by luman on 2016/11/21 10:20
 * 下载头像列表
 */

public class HeadListEvent {
    public int type; // 1成功，-1失败
    public HeadListEvent(int type){
        this.type=type;
    }
}
