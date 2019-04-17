package xechwic.android.bus.event;

/**
 * Created by luman on 2016/11/1 10:05
 */

public class LoginEvent {
    public int type;//1成功,2失败,3正在登陆
    public LoginEvent(int type){
        this.type=type;
    }
}
