package xechwic.android;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import xechwic.android.act.MainApplication;

public class XWAudioPlay extends Thread{
	private XWDataCenter xwDC;
	private boolean needPlay;
	private byte audioData[]=new byte[8000*2*10];  ////10s;
	public AudioTrack audioTrack; 
	private int playBufSize;
	private int playminBuf;
	private boolean bPlaying=false;
	
	
	public XWAudioPlay(){}
	public XWAudioPlay(XWDataCenter xwDC){

		try{
			/*if (mMediaPlayer!=null)
			{
				mMediaPlayer.stop();
				mMediaPlayer=null;
			}*/

			AudioManager audioManager =
					(AudioManager) MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
			audioManager.setMode(AudioManager.MODE_IN_CALL);
		}catch(Exception e){
			e.printStackTrace();
		}


		this.xwDC=xwDC;
		playminBuf=AudioTrack.getMinBufferSize(XWDataCenter.frequency, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);  ;///320;//// 8000*2;////audioData.length;////AudioTrack.getMinBufferSize(XWDataCenter.frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
		////playminBuf=(playminBuf / (160 * 8)+1)*160*8;
		playBufSize=playminBuf;
		//if (playBufSize<8000)
        /////playBufSize=8000*2;
		///if (playBufSize< XWDataCenter.frequency *2 /100 * 40) //////////400ms,要大于40ms否则数据可能丢失!!!!
		/////	playBufSize= XWDataCenter.frequency *2 /100 * 40;
		if ((XWDataCenter.xwDC.xwAudioRecord==null)||(XWDataCenter.xwDC.xwAudioRecord.audioRecord==null))
		    audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, XWDataCenter.frequency, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM);
        else
			audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, XWDataCenter.frequency, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, playBufSize, AudioTrack.MODE_STREAM,XWDataCenter.xwDC.xwAudioRecord.audioRecord.getAudioSessionId());


		//////////////////////////采用系统的音量

		if(audioTrack.getState()==AudioTrack.STATE_UNINITIALIZED){
			//打开录音设备失败错误处理
			this.needPlay=false;
			this.audioTrack=null;
			if(XWDataCenter.xwContext instanceof FriendVideoDisplay){
				((FriendVideoDisplay)XWDataCenter.xwContext).videoAudioException(new StringBuffer(XWCodeTrans.doTrans("打开声音播放设备错误")));
			}
		}

	}
	
	byte[] btRecordTemp=new byte[8000*2*10];/////10s最大缓冲
	////byte[] btRecordTemp2=new byte[160*4];/////40ms每块....
	final int iHandleByteNum=8000*2/100;   ///10ms每块....
	int iRecordLen=0;

	//////////////用于回声
	byte[] tmpBufEcho=new byte[iHandleByteNum];
	////short [] tmpBufEchoshort=new short[iHandleByteNum/2];


	int CompressVoiceData(byte[] src,int istartsample, byte[] dst, int Samples, int iInterval)
	{
		int i;
		int j=0;
		for (i=istartsample;i<istartsample+Samples;i++)
		{
			if (i % iInterval == 0)
			{
				continue;
			}
			dst[j*2]=src[i*2];
			dst[j*2+1]=src[i*2+1];
			j++;
		}
		return j;
	}

	int iOverLengthCount=0;
	void RecordPlayed(byte[] bt, int iPos, int iLen)
	{
		/////if (iRecordLen>0)
		{
            if (iRecordLen+iLen<=btRecordTemp.length)
            {
    			System.arraycopy(bt, iPos, btRecordTemp, iRecordLen, iLen);
    			iRecordLen+=iLen;
            }
            /*else
            {
            	///iRecordLen=0;
            	/////return;
            }*/
		}

		if (iRecordLen<iHandleByteNum)  /////不够,等待下次处理!!!!!
		{
			return;
		}

        int iHandlePos=0;
        int i;

		//////缓冲中超过了半秒
		boolean bOverLength=iRecordLen>8000;
		int iDropPerPacks=0;
		if (bOverLength)
			iDropPerPacks=Math.max(16-(iRecordLen/8000),5);
		else
			iOverLengthCount=0;

        for (i=0;i<(iRecordLen/iHandleByteNum);i++)
        {
			if (bOverLength)
			{
				iOverLengthCount++;
				if (iOverLengthCount>iDropPerPacks)  /////每20个包丢一个
				{
					iHandlePos+=iHandleByteNum;
					iOverLengthCount=0;
					continue;
				}
			}

            ////////////对放音进行控制
			if (XWDataCenter.xwDC._apm!=null) {
				///System.arraycopy(btRecordTemp, iHandlePos, tmpBufEcho, 0, iHandleByteNum);
				//////ByteBuffer.wrap(tmpBufEcho).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(tmpBufEchoshort);
				XWDataCenter.xwDC._apm.ProcessRenderStreambytes(btRecordTemp, iHandlePos);
				//System.arraycopy(tmpBufEcho, 0,btRecordTemp, iHandlePos, iHandleByteNum);
				///XWDataCenter.xwDC.mAudioEchoCancelProcess.AnalyzeReverseStream10msData(tmpBufEcho, iHandleByteNum);
			}

			///////////声音缓冲超长，要采用压缩声音。
			/*if (bOverLength) {

				int iret=CompressVoiceData(btRecordTemp,iHandlePos/2,tmpBufEcho,iHandleByteNum/2,16);
				audioTrack.write(tmpBufEcho,0,iret*2); ///截10分之一
			}
			else*/
			{
				int iret=audioTrack.write(btRecordTemp,iHandlePos,iHandleByteNum);
			}

			////////////////////////
			//播音流 160*4字节.  40ms
			////////////////////////////////////////
        	//if (iret>=0)
        	{
			    /////xwDC.XWNetphoneEchoPlayed(btRecordTemp,iHandlePos,iret);

				///////////////////2015-09-14
				//////xwDC.XWNetphoneEchoPlayed(btRecordTemp,iHandlePos,iHandleByteNum);

				/*if (XWDataCenter.xwDC.mAudioEchoCancelProcess!=null) {
					System.arraycopy(btRecordTemp, iHandlePos, tmpBufEcho, 0, iHandleByteNum);
					XWDataCenter.xwDC.mAudioEchoCancelProcess.AnalyzeReverseStream10msData(tmpBufEcho, iHandleByteNum);
				}*/


				iHandlePos+=iHandleByteNum;
        	}

        }
        

        if (iHandlePos<iRecordLen)
        {
			////先移到
        	System.arraycopy(btRecordTemp, iHandlePos, tmpBufEcho, 0,iRecordLen- iHandlePos);
			/////再移回
			System.arraycopy(tmpBufEcho, 0, tmpBufEcho, 0,iRecordLen- iHandlePos);
        	iRecordLen=iRecordLen- iHandlePos;
        }
        else
        	iRecordLen=0;

	}
	
	public void run(){
		long lLastPushData=System.currentTimeMillis();
		

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
        

		
		if (audioTrack==null)
			return;
		
		if (audioTrack!=null)
		{
			try{
				audioTrack.play();
			}catch(Exception e){
				//打开录音设备失败错误处理
				this.needPlay=false;
				this.audioTrack=null;
				if(XWDataCenter.xwContext instanceof FriendVideoDisplay){
					((FriendVideoDisplay)XWDataCenter.xwContext).videoAudioException(new StringBuffer("打开声音播放设备错误"));
				}
			}		
		}		
		
		int realLen;	
		

		this.needPlay=true;
		bPlaying=true;
		while(needPlay){
			realLen=0;
			try{    	
				xwDC.NetPhoneClientDataLock();
				try{
					realLen=xwDC.getAudioPlayBufferLen();
					if ((realLen>0) && (realLen<=audioData.length) )
					{
						//System.arraycopy(xwDC.audioDataBuffer.array(),0,this.audioData , 0,realLen );
						xwDC.audioDataBuffer.position(0);
						xwDC.audioDataBuffer.get(this.audioData , 0, realLen);
				        //this.audioData=xwDC.audioDataBuffer;////.clone();
						////System.arraycopy(xwDC.audioDataBuffer,0,this.audioData. , 0,realLen );
				        ////Log.v("xim","realLen:"+realLen);
					}
				}
				finally
				{
				    xwDC.NetPhoneClientDataUnlock();		
				}				
				if (realLen>0)
				{
					try
					{
						
						////audioTrack.write(audioData,0,realLen);
						
						RecordPlayed(audioData,0,realLen);
						
						/*int iPos=0;
						while (iPos<realLen)
						{
					        int iRet=audioTrack.write(this.audioData,iPos,realLen-iPos);
					        if (iRet<=0)
					        	break;
					        
					        
					        RecordPlayed(audioData,iPos,iRet);
					        
					        iPos+=iRet;
						}*/
					////////////////////////////在这里回声播放!!!!!!!!2014-10-27
					/////xwDC.XWNetphoneEchoPlayed(audioData,realLen);
					
					Log.v("XWAudioPlay","audioTrack "+realLen);
					}
					catch (Exception e)
					{
						
					}
				}
				///sleep(4);
				else
				////if (realLen==0)
				{
					try
					{
					    Thread.sleep(1);
					}
					catch (Exception e1)
					{

					}
				}
				
				//if (System.currentTimeMillis()-lLastPushData>30000)
				//{
					//lLastPushData=System.currentTimeMillis();
					//audioTrack.flush();					
				//}
			}catch(Exception e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void stopAudioPlay(){
		this.needPlay=false;

		try{
			AudioManager audioManager =
					(AudioManager) MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
			audioManager.setMode(AudioManager.MODE_NORMAL);
		}catch(Exception e){
			e.printStackTrace();
		}

		try
		{
			if (audioTrack!=null)
				audioTrack.stop();
		}
		catch(Exception ex)
		{

		}
		try {
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(audioTrack!=null){
			
			if (bPlaying)
			{
				try
				{
				    audioTrack.stop();
				}
				catch (Exception e1)
				{
				}
				bPlaying=false;
			}
			audioTrack.release();
			//audioTrack=null;//此时本线程可能尚未结束
		}
		
		
		/*try
		{	
			AudioManager am = (AudioManager)xwDC.xwApp.getSystemService(Context.AUDIO_SERVICE);   
		    
			///am.setMode(AudioManager.MODE_NORMAL);
	         if(am.isSpeakerphoneOn()) {
                 am.setSpeakerphoneOn(false);
               }
			//am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);	
			//audioTrack.setStereoVolume((float)xwDC.sysInfo.getAudio_volume()/(float)100,(float)xwDC.sysInfo.getAudio_volume()/(float)100);////(0.7f, 0.7f);//设置当前音量大小
		}
		catch (Exception e)
		{
			
		}*/
		
	}
}
