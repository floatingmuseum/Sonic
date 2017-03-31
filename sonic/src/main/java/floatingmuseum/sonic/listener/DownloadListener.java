package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.entity.TaskInfo;

/**
 * Created by Floatingmuseum on 2017/3/30.
 */

public interface DownloadListener {

    void onProgress(TaskInfo taskInfo);

    void onFinish(TaskInfo taskInfo);

    void onError(TaskInfo taskInfo, Throwable e);
}
