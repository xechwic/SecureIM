package xechwic.android.util;

/**
 * Created by luman on 2016/11/30 16:44
 *
 * APM声音处理配置
 */

public class ApmConfig {
    public static final int CHANNELS = 1;
    public static final int BITS_PER_SAMPLE = 16;
    public static final int SAMPLE_RATE = 8000;


    public static final int CALLBACK_BUFFER_SIZE_MS = 10;
    public static final int BUFFERS_PER_SECOND = 1000 / CALLBACK_BUFFER_SIZE_MS;

    public static final int AEC_BUFFER_SIZE_MS = 10;
    public static final int AEC_LOOP_COUNT = CALLBACK_BUFFER_SIZE_MS / AEC_BUFFER_SIZE_MS;

    public static final int JITTER_STEP_SIZE = CHANNELS * SAMPLE_RATE / BUFFERS_PER_SECOND;

    public static final int buffer_count = 15;

    public static final int PORT = 13000;



    public static int _receveCount = 0;
    public static int _sendCount = 0;

    public static int _aecPCLevel = 2;
    public static int _aecMobileLevel = 3;
    public static int _nsLevel = 3;

    ///////////agcmode 2,AdaptiveDigital,适合于手机
    public static int _agcLevel = 1;

}
