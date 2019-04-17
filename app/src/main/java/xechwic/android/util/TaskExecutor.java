package xechwic.android.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池
 */

public class TaskExecutor {

    private static Handler sHandler = new Handler(Looper.getMainLooper());
    private static ExecutorService FULL_TASK_EXECUTOR;////newCachedThreadPool

    public static void runOnUIThread(Runnable runnable) {
        sHandler.post(runnable);
    }

    public static void executeTask(Runnable task) {
        executeTask(task, true);
    }



    public static void executeTask(Runnable task, boolean parallel) {
        if (FULL_TASK_EXECUTOR == null) {
            FULL_TASK_EXECUTOR = Executors.newCachedThreadPool();
        }
        if (parallel) {////并列无序执行
            FULL_TASK_EXECUTOR.execute(task);
        }else{/////按顺序执行
            ///
//            FULL_TASK_EXECUTOR.execute(task);
        }

    }
}
