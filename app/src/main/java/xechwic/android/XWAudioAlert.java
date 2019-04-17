package xechwic.android;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import xechwic.android.act.MainApplication;

public class XWAudioAlert{ 
	/////private AudioTrack audioTrack=null;
	private static XWAudioAlert xwAA=new XWAudioAlert();
	
	
	/////Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    MediaPlayer mMediaPlayer=null;//// = new MediaPlayer();
	
	///public static int realFrequency=-1;
	///public static int realBits=-1;
	/////public byte[] data;
	Thread oldThread=null;
	
	
	/////static long lLastPlayMessage=0;
	
	
	public static void PlayMessageAlert()
	{
		/*if (System.currentTimeMillis()-lLastPlayMessage<=10000)
		{
			Log.v("XIM","PlayMessageAlert System.currentTimeMillis()-lLastPlayMessage<=10000");
			return;
		}*/
		/////lLastPlayMessage=System.currentTimeMillis();
		
		playAlert();
	}

	////限制时间内响一次
	public static void PlayMessageAlertLimit(int time)
	{
		/*if (System.currentTimeMillis()-lLastPlayMessage<=time)
		{
			Log.v("XIM","PlayMessageAlert System.currentTimeMillis()-lLastPlayMessage<=10000");
			return;
		}
		lLastPlayMessage=System.currentTimeMillis();
        */
		playAlert();
	}

	public static void playAlert(){
		try
		{
			AudioManager audioManager =
					(AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);

			if (audioManager != null)
			{
				int iRingerMode = audioManager.getRingerMode();

				if (iRingerMode!=AudioManager.RINGER_MODE_NORMAL)
				{
					Log.v("XIM","iRingerMode!=AudioManager.RINGER_MODE_NORMAL");
					return;
				}
				audioManager.setMode(AudioManager.MODE_NORMAL);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			final Ringtone r = RingtoneManager.getRingtone(MainApplication.getInstance(), notification);
			r.play();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private XWAudioAlert(){
	} 
	public static XWAudioAlert getAudioAlert(){
		return xwAA;
	}


	/**
	 * 播放来电铃声
	 */
	public  void startAudioAlert(){
		/*if(mMediaPlayer!=null){
			return;
		}*/
		 try
		    {
		      AudioManager audioManager = 
		      (AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
				{
					audioManager.setSpeakerphoneOn(true);
					audioManager.setMode(AudioManager.MODE_NORMAL);
		      }
		        int iRingerMode = audioManager.getRingerMode();
		        if (iRingerMode!=AudioManager.RINGER_MODE_NORMAL)
		        {
		        	Log.v("XIM","iRingerMode!=AudioManager.RINGER_MODE_NORMAL");
		        	return;
		        }

		    }
		    catch(Exception e)
		    {
		      e.printStackTrace();
		    }
		try{

		      Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			  mMediaPlayer = new MediaPlayer();
			  try {
			       mMediaPlayer.setDataSource(MainApplication.getInstance(), alert);  //后面的是try 和catch ，自动添加的
			  } catch (Exception e1) {
			   e1.printStackTrace();
			  }
			  mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			  mMediaPlayer.setLooping(true);    //循环播放开
			  try {
			      mMediaPlayer.prepare();     //后面的是try 和catch ，自动添加的
			  } catch (Exception e) {
			    e.printStackTrace();
			   }
			   mMediaPlayer.start();//开始播放
	   }
	   catch(Exception ex)
	   {
		   ex.printStackTrace();
	   }

	}

	/**
	 * 停止播放来电铃声
	 */
	public  void stoptAudioAlert(){
		try{
			if (mMediaPlayer!=null)
			{
				try {
					mMediaPlayer.stop();
					mMediaPlayer.release();
				}
				catch(Exception ex1)
				{

				}
				mMediaPlayer=null;
			}

			AudioManager audioManager =
					(AudioManager)MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
				audioManager.setSpeakerphoneOn(false);
			audioManager.setMode(AudioManager.MODE_NORMAL);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

}
