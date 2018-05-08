package floatingmuseum.sonic;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Floatingmuseum on 2017/4/20.
 */

public class TaskConfig implements Parcelable{

    private int maxThreads = 3;
    private int retryTime = 5;
    private int progressResponseInterval = 500;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;
    private String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    private int forceStart = Sonic.FORCE_START_NO;

    public int getMaxThreads() {
        return maxThreads;
    }

    public TaskConfig setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public TaskConfig setRetryTime(int retryTime) {
        this.retryTime = retryTime;
        return this;
    }

    public int getProgressResponseInterval() {
        return progressResponseInterval;
    }

    public TaskConfig setProgressResponseInterval(int progressResponseInterval) {
        this.progressResponseInterval = progressResponseInterval;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public TaskConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public TaskConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public String getDirPath() {
        return dirPath;
    }

    public TaskConfig setDirPath(String dirPath) {
        this.dirPath = dirPath;
        return this;
    }

    public int getForceStart() {
        return forceStart;
    }

    public TaskConfig setForceStart(int forceStart) {
        if (forceStart == Sonic.FORCE_START_NO || forceStart == Sonic.FORCE_START_YES) {
            this.forceStart = forceStart;
        }
        return this;
    }

    @Override
    public String toString() {
        return "TaskConfig{" +
                "maxThreads=" + maxThreads +
                ", retryTime=" + retryTime +
                ", progressResponseInterval=" + progressResponseInterval +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", dirPath='" + dirPath +
                ", forceStart='" + forceStart + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.maxThreads);
        dest.writeInt(this.retryTime);
        dest.writeInt(this.progressResponseInterval);
        dest.writeInt(this.connectTimeout);
        dest.writeInt(this.readTimeout);
        dest.writeString(this.dirPath);
        dest.writeInt(this.forceStart);
    }

    public TaskConfig() {
    }

    protected TaskConfig(Parcel in) {
        this.maxThreads = in.readInt();
        this.retryTime = in.readInt();
        this.progressResponseInterval = in.readInt();
        this.connectTimeout = in.readInt();
        this.readTimeout = in.readInt();
        this.dirPath = in.readString();
        this.forceStart = in.readInt();
    }

    public static final Creator<TaskConfig> CREATOR = new Creator<TaskConfig>() {
        @Override
        public TaskConfig createFromParcel(Parcel source) {
            return new TaskConfig(source);
        }

        @Override
        public TaskConfig[] newArray(int size) {
            return new TaskConfig[size];
        }
    };
}
