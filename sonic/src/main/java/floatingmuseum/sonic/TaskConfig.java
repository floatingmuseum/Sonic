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

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    public int getProgressResponseInterval() {
        return progressResponseInterval;
    }

    public void setProgressResponseInterval(int progressResponseInterval) {
        this.progressResponseInterval = progressResponseInterval;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
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
