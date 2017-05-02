package floatingmuseum.sonic.threads;

import android.util.Log;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.listener.ThreadListener;

/**
 * Created by Floatingmuseum on 2017/5/2.
 */

public class SingleThread implements Runnable {

    private static final String TAG = SingleThread.class.getName();

    private boolean stopThread = false;

    //下载文件夹路径
    private String dirPath;
    private String fileName;
    private int readTimeout;
    private int connectTimeout;
    private ThreadInfo threadInfo;
    private ThreadListener listener;

    public SingleThread(ThreadInfo threadInfo, String dirPath, String fileName, int readTimeout, int connectTimeout, ThreadListener listener) {
        this.threadInfo = threadInfo;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.listener = listener;
    }

    @Override
    public void run() {
        Log.i(TAG, threadInfo.getId() + "号线程开始工作" + "..." + fileName);
        isDownloading = true;
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream inputStream = null;
    }

    private boolean isDownloading = false;
    private boolean isPaused = false;
    private boolean isFailed = false;
    private boolean isFinished = false;
    private DownloadException downloadException;

    public boolean isDownloading() {
        return isDownloading;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public DownloadException getException() {
        return downloadException;
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public void stopThread() {
        stopThread = true;
    }
}
