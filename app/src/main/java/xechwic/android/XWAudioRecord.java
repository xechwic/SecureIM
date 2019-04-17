package xechwic.android;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.FileOutputStream;

import xechwic.android.util.UriConfig;
import xechwic.android.util.XWDataCenterMessage;

import static xechwic.android.XWDataCenter.frequency;


/*缓冲带拆分的处理方式*/
public class XWAudioRecord extends Thread{
	private static String TAG="XWAudioRecord";
	public boolean isRecording = false;//是否录放的标记
	
	public AudioRecord audioRecord;
	public int recBufSize;
	public int recBufMinSize;
	public XWDataCenter xwDC; 
	
	
	////////////////////微信
	public boolean bIsWeiXin;
	public String sWeiXinToUser;
	public String sWeiXinFilePath;
	public int    iWeiXinIsOffSent; /////是否是离线发送
	public int    iWeiXinSentRet;
	
	byte[] btWeiXinAudioDataToCode;  //////存100ms的数据
	int    btWeiXinAudioDataToCodeLen;
	byte[] btWeiXinAudioCodedData;
	byte[] btWeiXinCodedTemp;
	int    iWeiXinAudioCodeLen;
	int    iG729Coder;
	
	public int imaxSecond=60;

//	public static final int recordBufferSegment=3;//不同手机一次性采集数据块大小不一样,三星i9003一次采集256ms块,即4096字节,4096*3即缓冲区大小,底层发送必须是40ms的倍数

	public XWAudioRecord(XWDataCenter xwDC){
		bIsWeiXin=false;
		iWeiXinSentRet=-1;
		
		
		Log.e(TAG,"XWAudioRecord");
		
		try
		{
			
			this.xwDC=xwDC;
			recBufMinSize=	AudioRecord.getMinBufferSize(frequency, AudioFormat.CHANNEL_IN_MONO,  AudioFormat.ENCODING_PCM_16BIT);
			recBufSize = recBufMinSize;
			if (xwDC._apm!=null)
			{
				recBufSize=8000*2/100;///xwDC.mAudioEchoCancelProcess.calculateBufferSize(16000, 2, 1);
			}

			///	recBufSize= XWDataCenter.frequency *2 /100 * 100;
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, AudioFormat.CHANNEL_IN_MONO,  AudioFormat.ENCODING_PCM_16BIT, recBufMinSize);
			if( (audioRecord==null) || (AudioRecord.STATE_UNINITIALIZED==audioRecord.getState())){
				Log.e(TAG,"XWAudioRecord 2"); 
				
				//打开录音设备失败错误处理
				if(XWDataCenter.xwContext instanceof FriendVideoDisplay){
					Log.e(TAG,"XWAudioRecord 3"); 
					
					((FriendVideoDisplay)XWDataCenter.xwContext).videoAudioException(new StringBuffer(XWCodeTrans.doTrans("打开录音设备错误")));
				}
				else   /////////////////
				{
					
					Log.e(TAG,"XWAudioRecord 4"); 
					/*StringBuffer sb=new StringBuffer("打开录音设备错误");
					try{
						final Builder builder = new AlertDialog.Builder(XWDataCenter.xwContext);
						 builder.setTitle(sb.toString());
						 builder.setPositiveButton("确认",new DialogInterface.OnClickListener(){
				             public void onClick(DialogInterface dialog, int whichButton){
				            	 ///stopVideo();
				             }
				         });
						 //builder.setNeutralButton("取消",new DialogInterface.OnClickListener(){
				         //    public void onClick(DialogInterface dialog, int whichButton){
				         //    }
				         //});
													try
								{
								builder.show();
								}
								catch(Exception ex)
								{
									
								}
					}catch(Exception e ){
					}*/
				}
				if (audioRecord!=null)
				{
					try
					{
					    audioRecord.release();
					}
					catch(Exception e1)
					{
						e1.printStackTrace();
					}
				    audioRecord=null;
				}
			}
			
			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}

	/****************************************/
	public short[] Bytes2Shorts(byte[] buf,short[] s) {
		byte bLength = 2;
		///short[] s = new short[buf.length / bLength];

		int iLen=Math.min(buf.length,s.length*2);
		for (int iLoop = 0; iLoop <= iLen-2; ) {
			s[iLoop/2]= (short)( (int )buf[iLoop] | ((int)buf[iLoop+1] << 8) );
			iLoop+=2;
		}

		return s;
	}

	synchronized private void shutdownaudo()
	{
		Log.v("xim", "XWAudioRecord shutdownaudo");
		
		if(audioRecord!=null){
			try{
			/////audioRecord.stop();
			try{
			audioRecord.release();
			}
			catch(Exception e2)
			{
				e2.printStackTrace();
			}
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			/////audioRecord=null;
		}
	}


	
	@Override
	public void run(){//不停止地往缓冲写
        try
        {
		    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
        try
        {
    		this.setPriority(Thread.MAX_PRIORITY);
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
	 
		Log.e(TAG,"XWAudioRecord run 0"); 	
	 try
	 {
		int iGotLen=0;
		long lLastPushData=System.currentTimeMillis();
		long TotalSamples=0;
		long LastCountSamples=lLastPushData;
		
		int  iVADState=0; 
		long lastActiveTime=System.currentTimeMillis();
		
		boolean bIsSelfStop=false;

		 long lastVAD=System.currentTimeMillis();

		 /////////用于主动掉弃，防止时间堆积,2017-02-24
		 long lLastDispose=System.currentTimeMillis();
		
		Log.v("xim", "XWAudioRecord run ");
		/////android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO); 
		
		if (audioRecord==null)
		{
			shutdownaudo();
			return;
		}
		Log.v("xim", "XWAudioRecord run 1");
		
 
		/////this.setPriority(Thread.MAX_PRIORITY);
		

		
		///ByteBuffer buffer=null;
		byte[] buffer=null;

		 int out_analog_level = 200;
		
		
		isRecording=true;
		
	
		try
		{
		    //buffer=ByteBuffer.allocateDirect(recBufSize);
			buffer=new byte[recBufSize];
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		 //short[] processBuffer = new short[recBufSize/2];///2];


		if (buffer==null)
		{
			isRecording=false;
			shutdownaudo();
			return;
		}


		 //////////////////关闭手机的agc
		 try {
			 if (AutomaticGainControl.isAvailable()) {
				 AutomaticGainControl agc = AutomaticGainControl.create(
						 audioRecord.getAudioSessionId()
				 );
				 agc.setEnabled(false);
			 }
		 }
		 catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }
		
		
		////xwDC.setAudioDataBuffer(buffer);	
		


		if (audioRecord!=null)
		{
			try{
				audioRecord.startRecording();//开始录制
				
				///////xechwic.android.XWCrashHandler.ReportInfoToServer("AudioRecord startRecording.\r\n");
				
			}catch(Exception e){
				//打开录音设备失败错误处理
				isRecording=false;
				//Log.v("tag", "3333333");
				shutdownaudo();			
				return;
			}
		}					
		
				
		
		if (bIsWeiXin)  ///////////如果是微信录音,申请微信
		{
			btWeiXinAudioDataToCode=new byte[160];  ///10ms
			if (btWeiXinAudioDataToCode==null)
			{
				isRecording=false;
				shutdownaudo();
				return;				
			}
			
			btWeiXinAudioCodedData=new byte[10*100*imaxSecond];  ///60s
			if (btWeiXinAudioCodedData==null)
			{
				isRecording=false;
				shutdownaudo();
				return;					
			}
			iG729Coder=XWDataCenter.xwDC.xwg729ainitencoder();
			if (iG729Coder==0)	
			{
				isRecording=false;
				shutdownaudo();
				return;					
			}

			btWeiXinCodedTemp=new byte[10];
			btWeiXinAudioDataToCodeLen=0;
			iWeiXinAudioCodeLen=0;			
			
			Log.v("XIM","WeiXin init.");
			
		}
		///byte[] buffer = new byte[recBufSize];


		 lastActiveTime=System.currentTimeMillis();
		 iVADState=XWDataCenter.xwDC.xwVADinit();
		
		
		LastCountSamples=System.currentTimeMillis();
		

		
		Log.v("xim", "XWAudioRecord run 4");		
		while(isRecording){
			try{
				//从MIC保存数据到缓冲区
				iGotLen=0;
				try
				{
				    ///iGotLen=audioRecord.read(buffer,0,recBufSize);
					/////buffer.position(0);
					iGotLen=audioRecord.read(buffer,0,buffer.length);//(buffer,recBufMinSize);
					
					///Log.v("tag", "audioRecord.read:"+iGotLen);
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
				
				if (bIsWeiXin) ///微信
				{
					//////////////////如果无声音,则主动退出。
					if (System.currentTimeMillis()-lastActiveTime>=3000)
					{
						isRecording=false;		
						Log.v("xim", "no voice, end recording.");
						
						bIsSelfStop=true;
						continue;
					}
					
					
					if (iGotLen>0)
					{
						//////Log.v("xim", "XWAudioRecord run 5");					
					    ////xwDC.sendAudioDataForSplit(buffer,iGotLen);
						
						int iPos=0;
						
						
						////Log.v("xim", "XWAudioRecord run 8 "+iPos+" "+btWeiXinAudioDataToCodeLen+" "+iGotLen);	
						
						/////iWeiXinAudioCodeLen=0;
						///buffer.position(0);
						while ((iPos+(160-btWeiXinAudioDataToCodeLen))<=iGotLen)
						{
							////Log.v("xim", "XWAudioRecord run 61");				
							//buffer.position(0);
							
							System.arraycopy(buffer, iPos, btWeiXinAudioDataToCode, btWeiXinAudioDataToCodeLen, (160-btWeiXinAudioDataToCodeLen));
							////buffer.position(i);
							
							///buffer.get(btWeiXinAudioDataToCode,  btWeiXinAudioDataToCodeLen, (160-btWeiXinAudioDataToCodeLen));
							////Log.v("xim", "XWAudioRecord run 62");
							
							iPos+=(160-btWeiXinAudioDataToCodeLen);
							btWeiXinAudioDataToCodeLen=0;
							
							
							////Log.v("xim", "XWAudioRecord run 6 "+iWeiXinAudioCodeLen + " " +btWeiXinAudioCodedData.length);	
							
							if (iWeiXinAudioCodeLen+10<=btWeiXinAudioCodedData.length)
							{
								////Log.v("xim", "XWAudioRecord run 71");	
								XWDataCenter.xwDC.xwg729aencoder(iG729Coder, btWeiXinAudioDataToCode, btWeiXinCodedTemp);
								
								////Log.v("xim", "XWAudioRecord run 72");	
								System.arraycopy( btWeiXinCodedTemp, 0,btWeiXinAudioCodedData, iWeiXinAudioCodeLen, 10);
								iWeiXinAudioCodeLen+=10;
								
								
								
								if (XWDataCenter.xwDC.xwVAD(iVADState,btWeiXinAudioDataToCode)==1)
								{
									lastActiveTime=System.currentTimeMillis();									
								}
								
								/////Log.v("xim", "XWAudioRecord run 7 "+iWeiXinAudioCodeLen);	
							}
							else
							{
								isRecording=false;		
								Log.v("xim", "over 60 seconds time.");
								continue;
							}
								
						}
						
						if (iPos<iGotLen)
						{	
							btWeiXinAudioDataToCodeLen=iGotLen-iPos;
							//buffer.get(btWeiXinAudioDataToCode,  iPos, btWeiXinAudioDataToCodeLen);
							System.arraycopy(buffer, iPos, btWeiXinAudioDataToCode, 0, btWeiXinAudioDataToCodeLen);
						}
						
						//////////////////处理微信
					    
					    TotalSamples+=iGotLen;
					    
					    //////Log.v("xim", "xwDC.sendAudioDataForSplit"+iGotLen);
					}
					else
					{
						try
						{
							sleep(1);
						}
						catch(Exception e2)
						{
							
						}
					}
					
				}
				else  /////////////聊天
				{
					/////每120秒丢一帧，防止堆积.2017-02-24
					/*if (System.currentTimeMillis()-lLastDispose>=1000)
					{
						lLastDispose=System.currentTimeMillis();
					}
					else */
					{

						TotalSamples += iGotLen;

						if ((iGotLen > 0) && XWDataCenter.audio_is_open && (XWDataCenter.xwDC.remote_audio_codec > 0)) {
								if (XWDataCenter.xwDC._apm != null) {
									XWDataCenter.xwDC._apm.SetStreamDelay(0);

									XWDataCenter.xwDC._apm.ProcessCaptureStreambytes(buffer, 0);
								/*if (XWDataCenter.xwDC.vm.getAgc()) {
									out_analog_level = XWDataCenter.xwDC._apm.AGCStreamAnalogLevel();
								}*/

									///////////2017-02-28,不能用vad,会引发爆破音
									/////如果没有声音则不发送以节约带宽。
								if (XWDataCenter.xwDC.vm.getVad()) {
										if (XWDataCenter.xwDC._apm.VADHasVoice()) {
											lastVAD=System.currentTimeMillis();
										}
									    else
										{
											//////////如果无声音超过2秒,则停。
											if (System.currentTimeMillis()-lastVAD>500) {
												Log.e("XWAudioRecord","VAD no voice.");
												continue;
											}
										}
									}

									/*if (XWDataCenter.xwDC.xwVAD(iVADState,buffer)==1)
									{
										lastActiveTime=System.currentTimeMillis();
									}
									else
									{
										//////////如果无声音超过2秒,则停。
										if (System.currentTimeMillis()-lastActiveTime>500) {
											Log.e("XWAudioRecord","VAD no voice.");
											continue;
										}
									}*/
							}

							//////////////////
							////录音流
							////////////////////////////////////
							xwDC.sendAudioDataForSplit(buffer, iGotLen);
						    ///Log.v("xim", "xwDC.sendAudioDataForSplit 2"+iGotLen);
						} else {
							try {
								sleep(1);
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						}
					}
				}

			}catch(Exception ex){
				ex.printStackTrace();
			}

			
		}
		
		xwDC.setAudioDataBuffer(null);
		
		
		////////////////////////////
		if (bIsWeiXin)
		{
			java.util.Date now=new java.util.Date();
	
			try
			{
				sWeiXinFilePath=UriConfig.getUserDataDir()+"/"+XWDataCenter.xwDC.loginName+"_"+DateFormat.format("yyyyMMddkkmmss", now).toString()+".xwx";
				Log.v("xim", "sWeiXinFilePath:"+sWeiXinFilePath);
				FileOutputStream fos = new FileOutputStream(sWeiXinFilePath);         
		        fos.write(btWeiXinAudioCodedData,0, iWeiXinAudioCodeLen);
		        fos.close();			
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			

		}
		
		
		if (bIsSelfStop)
		{
			if (XWDataCenter.xwDC!=null)
			    XWDataCenter.xwDC.XWMsghandle.sendEmptyMessage(XWDataCenterMessage.MSG_16);
		}
		
		
		if (iG729Coder>0)	
		{
			XWDataCenter.xwDC.xwg729adestroyencoder(iG729Coder);
		}
		if (iVADState>0)
		{
			XWDataCenter.xwDC.xwVADdestroy(iVADState);
		}
		
	 }
	 finally
	 {
		isRecording=false;
		
		shutdownaudo();
		Log.v("xim", "XWAudioRecord thread exited.");
		
		audioRecord=null;
		
		//////xechwic.android.XWCrashHandler.ReportInfoToServer("AudioRecord Thread stopped.\r\n");
	 }
	 
	
	}
	
	
	
	public void stopAudioRecord(){
		Log.v("xim", "stopAudioRecord");
		
		/////shutdownaudo();
		
		isRecording=false;

		try
		{
            if (audioRecord!=null)
				audioRecord.stop();
		}
		catch(Exception ex)
		{

		}
		try {
			this.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
