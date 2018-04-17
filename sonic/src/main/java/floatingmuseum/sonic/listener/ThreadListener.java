package floatingmuseum.sonic.listener;

import floatingmuseum.sonic.DownloadException;
import floatingmuseum.sonic.entity.ThreadInfo;
import floatingmuseum.sonic.threads.BaseThread;
import floatingmuseum.sonic.threads.DownloadThread;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public interface ThreadListener {

    void onFetchContentLength(long contentLength,boolean isSupportRange,int hashCode);

    void onInitThreadError(DownloadException e,int hashCode);

    void onInitThreadPause(int hashCode);

    void onStart(ThreadInfo threadInfo,int hashCode);

    void onPause(ThreadInfo threadInfo,int hashCode);

    void onProgress(ThreadInfo threadInfo,int hashCode);

    void onError(ThreadInfo info, DownloadException e,int hashCode);

    void onFinished(ThreadInfo info,int hashCode);
}
