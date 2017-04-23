package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.threads.DownloadThread;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public interface ThreadListener {

    void onPause(ThreadInfo threadInfo);

    void onProgress(ThreadInfo threadInfo);

    void onError(DownloadThread errorThread, Throwable e);

    void onFinished(int threadId);
}
