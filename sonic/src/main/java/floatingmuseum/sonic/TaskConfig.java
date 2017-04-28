package floatingmuseum.sonic;

import android.os.Environment;

/**
 * Created by Floatingmuseum on 2017/4/20.
 */

public class TaskConfig {

    private int maxThreads = 3;
    private int retryTime = 5;
    private int progressResponseInterval = 500;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;
    private String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    public int getMaxThreads() {
        return maxThreads;
    }

    public TaskConfig setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public TaskConfig setRetryTime(int retryTime) {
        this.retryTime = retryTime;
        return this;
    }

    public int getProgressResponseInterval() {
        return progressResponseInterval;
    }

    public TaskConfig setProgressResponseInterval(int progressResponseInterval) {
        this.progressResponseInterval = progressResponseInterval;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public TaskConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public TaskConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public String getDirPath() {
        return dirPath;
    }

    public TaskConfig setDirPath(String dirPath) {
        this.dirPath = dirPath;
        return this;
    }

    @Override
    public String toString() {
        return "TaskConfig{" +
                "maxThreads=" + maxThreads +
                ", retryTime=" + retryTime +
                ", progressResponseInterval=" + progressResponseInterval +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", dirPath='" + dirPath + '\'' +
                '}';
    }
}
