package floatingmuseum.sonic.entity;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import floatingmuseum.sonic.Sonic;
import floatingmuseum.sonic.TaskConfig;
import floatingmuseum.sonic.utils.FileUtil;

/**
 * Created by Floatingmuseum on 2017/5/10.
 */

public class DownloadRequest {

    private TaskConfig selfTaskConfig;
    private String url;
    private String tag;
    private String fileName;
    private boolean isCustomTaskConfig = false;

    public DownloadRequest() {
        TaskConfig defaultTaskConfig = Sonic.getInstance().getTaskConfig();
        selfTaskConfig = new TaskConfig();
        selfTaskConfig.setDirPath(defaultTaskConfig.getDirPath())
                .setMaxThreads(defaultTaskConfig.getMaxThreads())
                .setRetryTime(defaultTaskConfig.getRetryTime())
                .setConnectTimeout(defaultTaskConfig.getConnectTimeout())
                .setReadTimeout(defaultTaskConfig.getReadTimeout())
                .setProgressResponseInterval(defaultTaskConfig.getProgressResponseInterval())
                .setForceStart(defaultTaskConfig.getForceStart());
    }

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
        return selfTaskConfig.getDirPath();
    }

    public DownloadRequest setDirPath(String dirPath) {
        selfTaskConfig.setDirPath(dirPath);
        isCustomTaskConfig = true;
        return this;
    }

    public int getMaxThreads() {
        return selfTaskConfig.getMaxThreads();
    }

    /**
     * <p>How many threads working for this task.
     * <p>default is 3.
     */
    public DownloadRequest setMaxThreads(int maxThreads) {
        if (maxThreads < 1) {
            selfTaskConfig.setMaxThreads(1);
        } else {
            selfTaskConfig.setMaxThreads(maxThreads);
        }
        isCustomTaskConfig = true;
        return this;
    }

    public int getRetryTime() {
        return selfTaskConfig.getRetryTime();
    }

    /**
     * default is 5;
     */
    public DownloadRequest setRetryTime(int retryTime) {
        if (retryTime < 0) {
            selfTaskConfig.setRetryTime(0);
        } else {
            selfTaskConfig.setRetryTime(retryTime);
        }
        isCustomTaskConfig = true;
        return this;
    }

    public int getProgressResponseInterval() {
        return selfTaskConfig.getProgressResponseInterval();
    }

    /**
     * <p>DownloadListener onProgress() method will be call at defined interval.
     * <p>the milliseconds must between 0 to 1000.
     * <p>the default for the interval is 500 milliseconds.
     */
    public DownloadRequest setProgressResponseInterval(int progressResponseInterval) {
        if (progressResponseInterval < 0) {
            selfTaskConfig.setProgressResponseInterval(0);
        } else if (progressResponseInterval > 1000) {
            selfTaskConfig.setProgressResponseInterval(1000);
        } else {
            selfTaskConfig.setProgressResponseInterval(progressResponseInterval);
        }
        isCustomTaskConfig = true;
        return this;
    }

    public int getConnectTimeout() {
        return selfTaskConfig.getConnectTimeout();
    }

    public DownloadRequest setConnectTimeout(int connectTimeout) {
        if (connectTimeout > 0) {
            selfTaskConfig.setConnectTimeout(connectTimeout);
            isCustomTaskConfig = true;
        }
        return this;
    }

    public int getReadTimeout() {
        return selfTaskConfig.getReadTimeout();
    }

    /**
     * default is 5000 milliseconds.
     */
    public DownloadRequest setReadTimeout(int readTimeout) {
        if (readTimeout > 0) {
            selfTaskConfig.setReadTimeout(readTimeout);
            isCustomTaskConfig = true;
        }
        return this;
    }

    public int getForceStart() {
        return selfTaskConfig.getForceStart();
    }

    /**
     * A task will start immediately no matter how active
     */
    public DownloadRequest setForceStart(int forceStart) {
        if (forceStart == Sonic.FORCE_START_NO || forceStart == Sonic.FORCE_START_YES) {
            selfTaskConfig.setForceStart(forceStart);
            isCustomTaskConfig = true;
        }
        return this;
    }

    public boolean isCustomTaskConfig() {
        return isCustomTaskConfig;
    }

    public TaskConfig getTaskConfig() {
        return selfTaskConfig;
    }
}
