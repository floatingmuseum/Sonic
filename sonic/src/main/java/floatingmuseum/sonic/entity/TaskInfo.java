package floatingmuseum.sonic.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Floatingmuseum on 2017/3/30.
 */

public class TaskInfo implements Parcelable {

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
    private int taskHashcode;

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

    public int getTaskHashcode() {
        return taskHashcode;
    }

    public void setTaskHashcode(int taskHashcode) {
        this.taskHashcode = taskHashcode;
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

    @Override
    public String toString() {
        return "TaskInfo{" +
                "downloadUrl='" + downloadUrl + '\'' +
                ", tag='" + tag + '\'' +
                ", name='" + name + '\'' +
                ", dirPath='" + dirPath + '\'' +
                ", filePath='" + filePath + '\'' +
                ", currentSize=" + currentSize +
                ", totalSize=" + totalSize +
                ", progress=" + progress +
                ", speed=" + speed +
                ", state=" + state +
                ", taskHashcode=" + taskHashcode +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.downloadUrl);
        dest.writeString(this.tag);
        dest.writeString(this.name);
        dest.writeString(this.dirPath);
        dest.writeString(this.filePath);
        dest.writeLong(this.currentSize);
        dest.writeLong(this.totalSize);
        dest.writeInt(this.progress);
        dest.writeLong(this.speed);
        dest.writeInt(this.state);
        dest.writeInt(this.taskHashcode);
    }

    protected TaskInfo(Parcel in) {
        this.downloadUrl = in.readString();
        this.tag = in.readString();
        this.name = in.readString();
        this.dirPath = in.readString();
        this.filePath = in.readString();
        this.currentSize = in.readLong();
        this.totalSize = in.readLong();
        this.progress = in.readInt();
        this.speed = in.readLong();
        this.state = in.readInt();
        this.taskHashcode = in.readInt();
    }

    public static final Creator<TaskInfo> CREATOR = new Creator<TaskInfo>() {
        @Override
        public TaskInfo createFromParcel(Parcel source) {
            return new TaskInfo(source);
        }

        @Override
        public TaskInfo[] newArray(int size) {
            return new TaskInfo[size];
        }
    };
}
