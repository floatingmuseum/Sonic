package floatingmuseum.sonic.listener;

/**
 * Created by Floatingmuseum on 2017/3/15.
 */

public interface InitListener {
    void onGetContentLength(long contentLength);
    void onError();
}
