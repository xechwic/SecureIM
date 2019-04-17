package xechwic.android;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import xechwic.android.act.MainApplication;

/**
 * Created by luman on 2017/1/5 21:38
 */

////////////////用于播放一段数字wave
 public class XWPlayThread extends Thread{
    private byte []btPlay=null;
    private int Len;

    public XWPlayThread(byte[] bt,int iLen) {
        btPlay=bt;
        Len=iLen;
    }

    @Override
    public void run(){
        boolean bIsSpeakerOn=false;
        // ///打开扩音器
        try {
            AudioManager am = (AudioManager) XWDataCenter.xwDC.xwApp
                    .getSystemService(Context.AUDIO_SERVICE);
            {
                am.setMode(AudioManager.MODE_NORMAL);
                bIsSpeakerOn = am.isSpeakerphoneOn();
                if (!bIsSpeakerOn)
                    am.setSpeakerphoneOn(true);
            }
        } catch (Exception e) {
               e.printStackTrace();
        }

        try {
            int playBufSize = Len;// /8000*2;////audioData.length;////AudioTrack.getMinBufferSize(XWDataCenter.frequency,
            // AudioFormat.CHANNEL_CONFIGURATION_MONO,
            // AudioFormat.ENCODING_PCM_16BIT);
            AudioTrack audioTrackPlay = new AudioTrack(
                    AudioManager.STREAM_MUSIC, XWDataCenter.frequency,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, playBufSize,
                    AudioTrack.MODE_STATIC);

            Log.e("XIM", "XWPlayThread " + Len);
            audioTrackPlay.write(btPlay, 0, Len);
            audioTrackPlay.play();
            // //audioTrackPlay.flush();
            while ((audioTrackPlay.getPlaybackHeadPosition() < Len / 2)
                    || (audioTrackPlay.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)) {
                Thread.sleep(100);
            }
            audioTrackPlay.stop();
            audioTrackPlay.release();

            Log.e("XIM", "XWPlayThread end.");
        } catch (Exception e) {
            Log.e("XIM", "XWPlayThread error " + e.getMessage());
        }

        // //////////恢复
        try {
            AudioManager am = (AudioManager) MainApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
            {
                if (am.isSpeakerphoneOn()) {
                    if (!bIsSpeakerOn)
                        am.setSpeakerphoneOn(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
