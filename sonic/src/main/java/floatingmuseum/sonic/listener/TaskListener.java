package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.entity.TaskInfo;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public interface TaskListener {

    void onStart(TaskInfo taskInfo);

    void onPause(TaskInfo taskInfo,int hashcode);

    void onProgress(TaskInfo taskInfo);

    void onError(TaskInfo taskInfo, DownloadException downloadException,int hashcode);

    void onFinish(TaskInfo taskInfo,int hashcode);

    void onCancel(TaskInfo taskInfo,int hashcode);
}
