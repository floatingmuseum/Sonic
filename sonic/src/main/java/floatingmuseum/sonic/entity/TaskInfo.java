package floatingmuseum.sonic.entity;

/**
 * Created by Floatingmuseum on 2017/3/30.
 */

public class TaskInfo {

    private String downloadUrl;
    private String tag;
    private String name;
    private String dirPath;
    private String filePath;
    private long currentSize;
    private long totalSize;
    private int progress;
    private long speed;
    private int state;

    public TaskInfo(){}

    public TaskInfo(String downloadUrl, String tag, String name, String dirPath, String filePath, long currentSize, long totalSize, int progress, long speed, int state) {
        this.downloadUrl = downloadUrl;
        this.tag = tag;
        this.name = name;
        this.dirPath = dirPath;
        this.filePath = filePath;
        this.currentSize = currentSize;
        this.totalSize = totalSize;
        this.progress = progress;
        this.speed = speed;
        this.state = state;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

//    public long getSpeed() {
//        return speed;
//    }

//    public void setSpeed(long speed) {
//        this.speed = speed;
//    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
