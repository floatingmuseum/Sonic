package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.threads.BaseThread;
import floatingmuseum.sonic.threads.DownloadThread;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public interface ThreadListener {

    void onFetchContentLength(long contentLength,boolean isSupportRange);

    void onInitThreadError(DownloadException e);

    void onInitThreadPause();

    void onStart(ThreadInfo threadInfo);

    void onPause(ThreadInfo threadInfo);

    void onProgress(ThreadInfo threadInfo);

    void onError(ThreadInfo info, DownloadException e);

    void onFinished(ThreadInfo info);
}
