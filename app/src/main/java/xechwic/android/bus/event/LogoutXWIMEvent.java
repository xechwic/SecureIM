package xechwic.android.bus.event;

/**
 * Created by luman on 2016/10/21 10:01
 */

public class LogoutXWIMEvent {
    public int type=-1;//1关闭，2注销账户
    public LogoutXWIMEvent(int type){
        this.type=type;
    }
}
