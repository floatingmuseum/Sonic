package floatingmuseum.sonic.entity;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.threads.BaseThread;

/**
 * Created by Floatingmuseum on 2017/4/1.
 */

public class UIMessage {

    public static final int THREAD_FETCH_CONTENT_LENGTH = 0;
    public static final int THREAD_INIT_THREAD_ERROR = 1;
    public static final int THREAD_INIT = 2;
    public static final int THREAD_INIT_PAUSE = 3;
    public static final int THREAD_START = 4;
    public static final int THREAD_PROGRESS = 5;
    public static final int THREAD_PAUSE = 6;
    public static final int THREAD_FINISH = 7;
    public static final int THREAD_ERROR = 8;

    private int state;
    private long contentLength;
    private boolean isSupportRange;
    private ThreadInfo threadInfo;
    private DownloadException downloadException;

    public UIMessage(int state) {
        this.state = state;
    }

    public long getContentLength() {
        return contentLength;
    }

    public UIMessage setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public boolean isSupportRange() {
        return isSupportRange;
    }

    public UIMessage setSupportRange(boolean supportRange) {
        isSupportRange = supportRange;
        return this;
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public UIMessage setThreadInfo(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
        return this;
    }

    public int getState() {
        return state;
    }

    public UIMessage setState(int state) {
        this.state = state;
        return this;
    }

    public DownloadException getDownloadException() {
        return downloadException;
    }

    public UIMessage setDownloadException(DownloadException downloadException) {
        this.downloadException = downloadException;
        return this;
    }

}
