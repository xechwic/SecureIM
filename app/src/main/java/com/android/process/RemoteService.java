package com.android.process;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.process.aidl.IProcessService;

import java.io.File;

import xechwic.android.XWServices;
import xechwic.android.act.MainApplication;
import xechwic.android.util.FileUtil;
import xechwic.android.util.JRSConstants;
import xechwic.android.util.TaskExecutor;

/**
 * Author: river
 * Date: 2016/6/1 17:36
 * Description: 本地服务
 */
public class RemoteService extends Service {
    String TAG = "RemoteService";

    private ServiceBinder mServiceBinder;

    private LocalServiceConnection mLocalServiceConn;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this;
        mServiceBinder = new ServiceBinder();


        Log.i(TAG, TAG + " onCreate");
        startXWServiceTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG, "RemoteService onStartCommand");
        //////////绑定XWService
                startUpXWServices();

        if(intent!=null&&!TextUtils.isEmpty(intent.getAction())) {
            String action = intent.getAction();
            //////生成应用通知图标
            if (JRSConstants.CMD_ACTION_NOTIFICATION_OFF.equals(action)) {
                Log.e(TAG, "CMD_ACTION_START_REMOTE");
                //////绑定通知
                //////取消由XWService产生的通知
//                PowerManager pm = (PowerManager) MainApplication.getInstance().getSystemService(Context.POWER_SERVICE);
//                if(pm.isScreenOn()) {
//                    startForeground(MainApplication.APP_ICON_ID, MainApplication.getInstance().initNotification());
//                    stopForeground(true);
//                }
                return Service.START_STICKY;
            }
        }
        return START_STICKY;
    }


    private void startXWServiceTask(){
        TaskExecutor.executeTask(new Runnable() {
            @Override
            public void run() {
                try{
                    while (FileUtil.isGuardFileExist()){
                        Thread.sleep(JRSConstants.LONG_TIME*2);
                        if(FileUtil.isGuardFileExist()){
                            TaskExecutor.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG,"startXWServiceTask start XWServices");

                                    if(System.currentTimeMillis()-XWServices.lLastDoCheck>=JRSConstants.LONG_TIME)
                                    try {
                                        Intent intentservice=new Intent(MainApplication.getInstance(), XWServices.class);
                                        intentservice.setAction("DO_CHECK");
                                        MainApplication.getInstance().startService(intentservice);
                                    } catch (Exception e) {
                                        e.getStackTrace();
                                    }
                                }
                            });
                        }
                    }
                    if(!FileUtil.isGuardFileExist()){
                        stopSelf();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mLocalServiceConn !=null)
        unbindService(mLocalServiceConn);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceBinder;
    }

    /**
     * 通过AIDL实现进程间通信
     */
    class ServiceBinder extends IProcessService.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return "RemoteService";
        }
    }

    /**
     * 连接远程服务
     */
    class LocalServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                // 与远程服务通信
                IProcessService process = IProcessService.Stub.asInterface(service);
                Log.i(TAG, "连接" + process.getServiceName() + "服务成功");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // RemoteException连接过程出现的异常，才会回调,unbind不会回调
            // 监测，远程服务已经死掉，则重启远程服务
            Log.i(TAG, "XWService服务挂掉了,XWService服务被杀死");
            startUpXWServices();

        }
    }

    private void startUpXWServices(){
        boolean isGuard=true;
        try{
            File file=new File(MainApplication.getInstance().getCacheDir()+"/guard");
            isGuard=file.exists();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(isGuard) {
            // 启动远程服务
            startService(new Intent(this, XWServices.class));
            if (mLocalServiceConn == null) {
                mLocalServiceConn = new LocalServiceConnection();
            }
//            //////取消由XWService产生的通知
//            startForeground(MainApplication.APP_ICON_ID,MainApplication.getInstance().initNotification());
//            stopForeground(true);
            // 绑定远程服务
            bindService(new Intent(this, XWServices.class), mLocalServiceConn, Context.BIND_IMPORTANT);

        }
    }
}
