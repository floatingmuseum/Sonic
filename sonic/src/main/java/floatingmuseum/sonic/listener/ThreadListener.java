package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.entity.ThreadInfo;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public interface ThreadListener {

    void onPause(ThreadInfo threadInfo);

    void onProgress(ThreadInfo threadInfo);

    void onError(ThreadInfo threadInfo,Throwable e);

    void onFinished(int threadId);
}
