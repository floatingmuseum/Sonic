package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.entity.TaskInfo;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public interface TaskListener {
    void onProgress(TaskInfo taskInfo);
    void onError(TaskInfo taskInfo,Throwable e);
    void onFinish(TaskInfo taskInfo);
}
