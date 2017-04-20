package floatingmuseum.sample.sonic;

import android.os.Environment;

/**
 * Created by Floatingmuseum on 2017/4/20.
 */

public class SingleTaskConfig {

    private int maxThreads = 3;
    private int retryTime = 5;
    private int progressResponseInterval = 500;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;
    private String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    public int getMaxThreads() {
        return maxThreads;
    }

    public SingleTaskConfig setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public SingleTaskConfig setRetryTime(int retryTime) {
        this.retryTime = retryTime;
        return this;
    }

    public int getProgressResponseInterval() {
        return progressResponseInterval;
    }

    public SingleTaskConfig setProgressResponseInterval(int progressResponseInterval) {
        this.progressResponseInterval = progressResponseInterval;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public SingleTaskConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public SingleTaskConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public String getDirPath() {
        return dirPath;
    }

    public SingleTaskConfig setDirPath(String dirPath) {
        this.dirPath = dirPath;
        return this;
    }
}
