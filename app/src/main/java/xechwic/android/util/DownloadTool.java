package xechwic.android.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import xechwic.android.XWCodeTrans;

/**
 * 文件下载工具
 *
 */
public class DownloadTool {

	private static final String TAG = DownloadTool.class.getSimpleName();

	private Context context = null;
	private String fileUrl = ""; // file的URL地址
	private String savePath = null; // 下载的file存放的路径
	private String saveDir = null; // 下载的file文件存放目录
	private int progress = 0; // 下载进度
	private final int DOWNLOAD_ING = 1; // 标记正在下载
	private final int DOWNLOAD_OVER = 2; // 标记下载完成
	private final int DOWNLOAD_CANCEL= 3;//取消下载
	ProgressDialog mProgressDialog = null;
	private Boolean interceptFlag = false; // 标记用户是否在下载过程中取消下载
    private DownLoadListener listerner=null;//下载监听
    
	public DownloadTool(Context context, String filePath) {
		if (context == null) {
			throw new NullPointerException("context == null");
		}
		this.context = context;
        initFilePath(filePath);
	}

	public void setListener(DownLoadListener listerner){
		this.listerner=listerner;
	}
	
	/**
	 * 初始化文件保存路径
	 * @param filePath
	 */
	private void initFilePath(String filePath){
		if(filePath==null||filePath.length()<1){
			return;
		}
		savePath = filePath;
		saveDir = savePath.substring(0,savePath.lastIndexOf("/"));
	}
	
	/**
	 * 下载文件
	 * @param url
	 */
	public void download(String url) {
		if (context == null||saveDir==null||savePath==null||url==null) {
			return;
		}
	
			try {
				fileUrl =utf_8URL(url);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
		showDownloadDialog();
	}

	private Handler mhandler = new Handler() { // 更新UI的handler

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DOWNLOAD_ING:
				Log.e(TAG,"mhandler:Dowload_ing"+progress);
				// 更新进度条
                if(mProgressDialog!=null){
                	mProgressDialog.setProgress(progress);
                }
				
				break;
			case DOWNLOAD_OVER:
				Log.e(TAG,"handler DOWNLOAD_OVER");
				if(mProgressDialog!=null&&mProgressDialog.isShowing()){
					mProgressDialog.dismiss();
				}
				if(listerner!=null){
					listerner.DownLoadOver();
				}
				break;
			case DOWNLOAD_CANCEL:
				Log.e(TAG,"handler DOWNLOAD_CANCEL");
				if(mProgressDialog!=null&&mProgressDialog.isShowing()){
					mProgressDialog.dismiss();
				}
				break;
			default:
				break;
			}
		}

	};

	/*
	 * 弹出下载进度对话框
	 */
	private void showDownloadDialog() {
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setTitle(xechwic.android.XWCodeTrans.doTrans("下载..."));
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, xechwic.android.XWCodeTrans.doTrans("取消下载"),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Log.e(TAG,"onClick:取消下载");
						interceptFlag = true;
						DownloadTool.this.mhandler.sendEmptyMessage(DOWNLOAD_CANCEL);
					}
				});
		mProgressDialog.show();
		mProgressDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				Log.e(TAG,"onDismiss");
				interceptFlag = true;
			}
		});
		downloadFile();

	}

	/*
	 * 下载文件
	 */
	private void downloadFile() {
		TaskExecutor.executeTask(downloadFileRunnable);
	}

	// 匿名内部类，文件下载线程
	private Runnable downloadFileRunnable = new Runnable() {

		public void run() {
			try {
				URL url = new URL(fileUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				Log.e(TAG, "总字节数:" + length);
				InputStream is = conn.getInputStream();
				File fileDir = new File(saveDir);
				if (!fileDir.exists()) {
					fileDir.mkdirs();
				}
				File saveFile = new File(savePath);
				if (saveFile.exists()) {
					saveFile.delete();
				}
				FileOutputStream out = new FileOutputStream(saveFile,false);
				int count = 0;
				int readnum = 0;
				byte[] buffer = new byte[1024];
				do {
					readnum = is.read(buffer);
					count += readnum;
					int p=(int) (((float) count / length) * 100);
					if(!interceptFlag){
						if(progress!=p){
							progress=p;
							Log.e(TAG, "下载进度" + progress);
							Message msg = DownloadTool.this.mhandler.obtainMessage();
							msg.what=DOWNLOAD_ING;
							DownloadTool.this.mhandler.sendMessage(msg);
						}
					
					}
					if (readnum <= 0) {
						//TODO 已经点击取消下载时不进行安装
						if(!interceptFlag){
							// 下载结束
							Message msg = DownloadTool.this.mhandler.obtainMessage();
							msg.what=DOWNLOAD_OVER;
							DownloadTool.this.mhandler.sendMessage(msg);
						}else{
							Message msg = DownloadTool.this.mhandler.obtainMessage();
							msg.what=DOWNLOAD_CANCEL;
							DownloadTool.this.mhandler.sendMessage(msg);
						}
						break;
					}
					out.write(buffer, 0, readnum);
				} while (!interceptFlag);
				is.close();
				out.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	};

	/**
	 * 　　* 将URL中的中文转为UTF-8编码
	 */
	public String utf_8URL(String str) throws UnsupportedEncodingException {

		StringBuffer sb = new StringBuffer();

		int len = str.length();

		for (int i = 0; i < len; i++) {

			char c = str.charAt(i);

			if (c > 0x7F) {

				sb.append(URLEncoder.encode(String.valueOf(c), "utf-8"));
			} else {

				sb.append(c);

			}

		}
		return sb.toString();
	}

	/*
	 * 安装下载的apk文件
	 */
	public void installApk() {
		File file = new File(savePath);
		if (!file.exists()) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + file.toString()),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}
	
	/**
	 * 下载监听接口
	 *
	 */
	public interface DownLoadListener{
		/**
		 * 下载完成
		 */
		void DownLoadOver();
	}
}
