package floatingmuseum.sonic.entity;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class ThreadInfo {

    private int id;
    private String url;
    //初始位置
    private long startPosition;
    //结束位置
    private long endPosition;
    //当前位置
    private long currentPosition;
    private long fileSize;
    private int isFinished;

    public ThreadInfo() {
    }

    public ThreadInfo(int id, String url, long startPosition, long endPosition, long currentPosition, long fileSize,int isFinished) {
        this.id = id;
        this.url = url;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.currentPosition = currentPosition;
        this.fileSize = fileSize;
        this.isFinished = isFinished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int isFinished() {
        return isFinished;
    }

    public void setFinished(int finished) {
        isFinished = finished;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", startPosition=" + startPosition +
                ", endPosition=" + endPosition +
                ", currentPosition=" + currentPosition +
                ", fileSize=" + fileSize +
                '}';
    }
}
