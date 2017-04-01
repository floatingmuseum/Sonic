package floatingmuseum.sonic.entity;

/**
 * Created by Floatingmuseum on 2017/4/1.
 */

public class UIListenerMessage {

    private TaskInfo taskInfo;
    private int state;
    private Throwable error;

    public UIListenerMessage(TaskInfo taskInfo, int state, Throwable error) {
        this.taskInfo = taskInfo;
        this.state = state;
        this.error = error;
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

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

}
