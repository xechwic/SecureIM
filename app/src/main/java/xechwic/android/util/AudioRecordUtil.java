package xechwic.android.util;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.Settings;

/**
 * Created by luman on 2017/1/6 15:40
 * 录音权限检测
 */

public class AudioRecordUtil {
    // 音频获取源
    public static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    public static int bufferSizeInBytes = 0;
    /**
     * 判断是是否有录音权限
     */
    public static boolean isHasPermission(final Context context){
        //开始录制音频
        try{
            bufferSizeInBytes = 0;
            bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                    channelConfig, audioFormat);
            AudioRecord audioRecord =  new AudioRecord(audioSource, sampleRateInHz,
                    channelConfig, audioFormat, bufferSizeInBytes);
            // 防止某些手机崩溃，例如联想
            audioRecord.startRecording();
            /**
             * 根据开始录音判断是否有录音权限
             */
            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                context.startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                return false;
            }else{
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }


        return true;
    }
}
