package xechwic.android;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import xechwic.android.util.TaskExecutor;

/**  
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告. 
 * 
 *  需要在Application中注册，为了要在程序启动器就监控整个程序。
 */    
public class XWCrashHandler implements UncaughtExceptionHandler {    
        
    public static final String TAG = "CrashHandler";    
        
    //系统默认的UncaughtException处理类     
    private Thread.UncaughtExceptionHandler mDefaultHandler;    
    //CrashHandler实例    
    private static XWCrashHandler instance=null;
   //程序的Context对象    
    private Context mContext;    
    //用来存储设备信息和异常信息    
    private Map<String, String> infos = new HashMap<String, String>();    
    
    //用于格式化日期,作为日志文件名的一部分    
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
    
    /** 保证只有一个CrashHandler实例 */    
    private XWCrashHandler() {}    
    
    /** 获取CrashHandler实例 ,单例模式 */    
    public static XWCrashHandler getInstance() {    
    	if(instance == null)
    		instance = new XWCrashHandler();   
        return instance;    
    }    
    
    /**  
     * 初始化  
     */    
    public void init(Context context) {    
        mContext = context;    
        //获取系统默认的UncaughtException处理器    
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();    
        //设置该CrashHandler为程序的默认处理器    
        Thread.setDefaultUncaughtExceptionHandler(this);    
    }    
    
    /**  
     * 当UncaughtException发生时会转入该函数来处理  
     */    
    @Override    
    public void uncaughtException(Thread thread, Throwable ex) {    
        if (!handleException(ex) && mDefaultHandler != null) {    
        	ex.printStackTrace();
            //如果用户没有处理则让系统默认的异常处理器来处理    
            mDefaultHandler.uncaughtException(thread, ex);    
        } else {    
            try {    
                Thread.sleep(10000);    
            } catch (InterruptedException e) {    
                Log.e(TAG, "error : ", e);    
            }    
            //退出程序    
            android.os.Process.killProcess(android.os.Process.myPid());    
            System.exit(1);    
        }    
    }    
    
    /**  
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.  
     *   
     * @param ex  
     * @return true:如果处理了该异常信息;否则返回false.  
     */    
    private boolean handleException(Throwable ex) {    
        if (ex == null) {    
            return false;    
        }    
        //收集设备参数信息     
        collectDeviceInfo(mContext);    
        
        Toast.makeText(mContext, XWCodeTrans.doTrans("很抱歉,程序出现异常,即将退出."), Toast.LENGTH_SHORT).show();

        
        //使用Toast来显示异常信息    
        new Thread() {    
            @Override    
            public void run() {    
                Looper.prepare();    
                Looper.loop();    
            }    
        }.start();    
        //保存日志文件     
        saveCatchInfo2File(ex);  
        return true;    
    }    
        
    /**  
     * 收集设备参数信息  
     * @param ctx  
     */    
    public void collectDeviceInfo(Context ctx) {    
        try {    
            PackageManager pm = ctx.getPackageManager();    
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);    
            if (pi != null) {    
                String versionName = pi.versionName == null ? "null" : pi.versionName;    
                String versionCode = pi.versionCode + "";    
                infos.put("versionName", versionName);    
                infos.put("versionCode", versionCode);    
            }    
        } catch (NameNotFoundException e) {    
            Log.e(TAG, "an error occured when collect package info", e);    
        }    
        Field[] fields = Build.class.getDeclaredFields();    
        for (Field field : fields) {    
            try {    
                field.setAccessible(true);    
                infos.put(field.getName(), field.get(null).toString());    
                Log.d(TAG, field.getName() + " : " + field.get(null));    
            } catch (Exception e) {    
                Log.e(TAG, "an error occured when collect crash info", e);    
            }    
        }    
    }    
    
    /**  
     * 保存错误信息到文件中  
     *   
     * @param ex  
     * @return  返回文件名称,便于将文件传送到服务器  
     */    
    private String saveCatchInfo2File(Throwable ex) {    
            
        StringBuffer sb = new StringBuffer();    
        for (Map.Entry<String, String> entry : infos.entrySet()) {    
            String key = entry.getKey();    
            String value = entry.getValue();    
            sb.append(key + "=" + value + "\r\n");    
        }    
            
        Writer writer = new StringWriter();    
        PrintWriter printWriter = new PrintWriter(writer);    
        ex.printStackTrace(printWriter);    
        Throwable cause = ex.getCause();    
        while (cause != null) {    
            cause.printStackTrace(printWriter);    
            cause = cause.getCause();    
        }    
        printWriter.close();    
        String result = writer.toString()+"\r\n";    
        sb.append(result);    
        
        
        Log.e("XIM","error exit:"+sb.toString());
        try {    
            ////long timestamp = System.currentTimeMillis();    
            String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); 
            		
            		////formatter.format(new Date());    
            String fileName = "crash-"+mContext.getPackageName()+"-"+ time + ".log";    
            /////if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) 
            {    
                String path = XWDataCenter.sProgPath+"/crashlog/";    
                File dir = new File(path);    
                if (!dir.exists()) {    
                    dir.mkdirs();    
                }    
                FileOutputStream fos = new FileOutputStream(path + fileName);    
                fos.write(sb.toString().getBytes()); 
                fos.close();    
                //发送给开发人员
                sendCrashLog2PM(path+fileName);
                
            }    
            return fileName;    
        } catch (Exception e) {    
            Log.e(TAG, "an error occured while writing file...", e);    
        }    
        return null;    
    }    
    
    
    public static void ReportInfoToServer(final String sMsg)
    {
        TaskExecutor.executeTask(new Runnable() {
            @Override
            public void run() {
                String httpUrl="http://"+XWDataCenter.getXIMIP()+"/a2buser/crachlog/log.php";
                try
                {
                    URL url = new URL(httpUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Open a HTTP  connection to  the URL
                    String boundary = "*****";
                    String twoHyphens = "--";
                    DataOutputStream dos = null;
                    String lineEnd = "\r\n";

                    conn.setConnectTimeout(10000);

                    byte[] data = ("msg=" + URLEncoder.encode(sMsg)).getBytes();

                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Charset", "UTF-8");
                    //					conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    conn.setRequestProperty("Content-Length", data.length + "");

                    conn.setDoOutput(true);// 准备写出
                    conn.getOutputStream().write(data);// 写出数据

                    Log.v("XIM","post crash error getResponseCode:"+ conn.getResponseCode());
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });

    }
    
    /**
     * 将捕获的导致崩溃的错误信息发送给开发人员
     * 
     * 目前只将log日志保存在sdcard 和输出到LogCat中，并未发送给后台。
     */
    private void sendCrashLog2PM(String fileName){
    	if(!new File(fileName).exists()){
    		Toast.makeText(mContext, XWCodeTrans.doTrans("日志文件不存在！"), Toast.LENGTH_SHORT).show();
    		return;
    	}
    	FileInputStream fis = null;
    	BufferedReader reader = null;
    	String s = null;
    	try {
			fis = new FileInputStream(fileName);
			reader = new BufferedReader(new InputStreamReader(fis, "GBK"));
			
			String sMsg=fileName+"\r\n";
			while(true){
				s = reader.readLine();
				if(s == null) break;
				//由于目前尚未确定以何种方式发送，所以先打出log日志。
				sMsg+=s.toString()+"\r\n";
			}
			
			ReportInfoToServer(sMsg);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{	// 关闭流
			try {
				reader.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
}    