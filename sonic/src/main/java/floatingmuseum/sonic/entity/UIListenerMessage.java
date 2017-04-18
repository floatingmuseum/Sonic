package floatingmuseum.sonic.entity;

import floatingmuseum.sonic.DownloadException;

/**
 * Created by Floatingmuseum on 2017/4/1.
 */

public class UIListenerMessage {

    private TaskInfo taskInfo;
    private int state;
    private DownloadException downloadException;

    public UIListenerMessage(TaskInfo taskInfo, int state, DownloadException downloadException) {
        this.taskInfo = taskInfo;
        this.state = state;
        this.downloadException = downloadException;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public DownloadException getDownloadException() {
        return downloadException;
    }

    public void setDownloadException(DownloadException downloadException) {
        this.downloadException = downloadException;
    }

}
