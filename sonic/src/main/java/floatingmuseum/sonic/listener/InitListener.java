package floatingmuseum.sonic.listener;


import floatingmuseum.sonic.DownloadException;

/**
 * Created by Floatingmuseum on 2017/3/15.
 */

public interface InitListener {
    void onGetContentLength(long contentLength,boolean isSupportRange);
    void onInitError(DownloadException e);
}
