package floatingmuseum.sample.sonic;


/**
 * Created by Floatingmuseum on 2017/4/1.
 */

public class AppInfo {
    private String url;
    private String name;
    private long currentSize;
    private long TotalSize;
    private int progress;
    private int state;
    private boolean isForceTask;

    public AppInfo(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public long getTotalSize() {
        return TotalSize;
    }

    public void setTotalSize(long totalSize) {
        TotalSize = totalSize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isForceTask() {
        return isForceTask;
    }

    public void setForceTask(boolean forceTask) {
        isForceTask = forceTask;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", currentSize=" + currentSize +
                ", TotalSize=" + TotalSize +
                ", progress=" + progress +
                ", state=" + state +
                ", isForceTask=" + isForceTask +
                '}';
    }
}
