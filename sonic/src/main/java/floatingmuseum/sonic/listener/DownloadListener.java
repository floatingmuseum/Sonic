package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.entity.TaskInfo;

/**
 * Created by Floatingmuseum on 2017/3/30.
 */

public interface DownloadListener {
    void onStart(TaskInfo taskInfo);

    void onWaiting(TaskInfo taskInfo);

    void onPause(TaskInfo taskInfo);

    void onProgress(TaskInfo taskInfo);

    void onFinish(TaskInfo taskInfo);

    void onError(TaskInfo taskInfo, DownloadException downloadException);

    void onCancel(TaskInfo taskInfo);
}
