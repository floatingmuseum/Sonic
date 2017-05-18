package floatingmuseum.sonic.entity;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import floatingmuseum.sonic.Sonic;
import floatingmuseum.sonic.TaskConfig;
import floatingmuseum.sonic.utils.FileUtil;

/**
 * Created by Floatingmuseum on 2017/5/10.
 */

public class DownloadRequest {

    private String url;
    private String tag;
    private String fileName;
    private String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    private int maxThreads = 3;
    private int retryTime = 5;
    private int progressResponseInterval = 500;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;
    private int forceStart = Sonic.FORCE_START_NO;
    private boolean isCustomTaskConfig = false;

    public String getUrl() {
        return url;
    }

    public DownloadRequest setUrl(@NonNull String url) {
        this.url = url;
        return this;
    }

    public String getTag() {
        if (TextUtils.isEmpty(tag)) {
            return url;
        }
        return tag;
    }

    public DownloadRequest setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public String getFileName() {
        if (TextUtils.isEmpty(fileName)) {
            return FileUtil.getUrlFileName(url);
        }
        return fileName;
    }

    public DownloadRequest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getDirPath() {
        return dirPath;
    }

    public DownloadRequest setDirPath(String dirPath) {
        this.dirPath = dirPath;
        isCustomTaskConfig = true;
        return this;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * <p>How many threads working for this task.
     * <p>default is 3.
     */
    public DownloadRequest setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            this.maxThreads = 1;
        } else {
            this.maxThreads = maxThreads;
        }
        isCustomTaskConfig = true;
        return this;
    }

    public int getRetryTime() {
        return retryTime;
    }

    /**
     * default is 5;
     */
    public DownloadRequest setRetryTime(int retryTime) {
        if (retryTime < 0) {
            retryTime = 0;
        } else {
            this.retryTime = retryTime;
        }
        isCustomTaskConfig = true;
        return this;
    }

    public int getProgressResponseInterval() {
        return progressResponseInterval;
    }

    /**
     * <p>DownloadListener onProgress() method will be call at defined interval.
     * <p>the milliseconds must between 0 to 1000.
     * <p>the default for the interval is 500 milliseconds.
     */
    public DownloadRequest setProgressResponseInterval(int progressResponseInterval) {
        if (progressResponseInterval < 0) {
            this.progressResponseInterval = 0;
        } else if (progressResponseInterval > 1000) {
            this.progressResponseInterval = 1000;
        } else {
            this.progressResponseInterval = progressResponseInterval;
        }
        isCustomTaskConfig = true;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public DownloadRequest setConnectTimeout(int connectTimeout) {
        if (connectTimeout > 0) {
            this.connectTimeout = connectTimeout;
            isCustomTaskConfig = true;
        }
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * default is 5000 milliseconds.
     */
    public DownloadRequest setReadTimeout(int readTimeout) {
        if (readTimeout > 0) {
            this.readTimeout = readTimeout;
            isCustomTaskConfig = true;
        }
        return this;
    }

    public int getForceStart() {
        return forceStart;
    }

    /**
     * A task will start immediately no matter how active
     */
    public DownloadRequest setForceStart(int forceStart) {
        if (forceStart == Sonic.FORCE_START_NO || forceStart == Sonic.FORCE_START_YES) {
            this.forceStart = forceStart;
            isCustomTaskConfig = true;
        }
        return this;
    }

    public boolean isCustomTaskConfig() {
        return isCustomTaskConfig;
    }

    public TaskConfig getTaskConfig() {
        return new TaskConfig().setDirPath(dirPath)
                .setRetryTime(retryTime)
                .setMaxThreads(maxThreads)
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .setProgressResponseInterval(progressResponseInterval)
                .setForceStart(forceStart);
    }
}
