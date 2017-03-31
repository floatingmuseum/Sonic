package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.entity.ThreadInfo;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public interface ThreadListener {

    void onProgress(ThreadInfo threadInfo);

    void onFinished(int threadId);
}
