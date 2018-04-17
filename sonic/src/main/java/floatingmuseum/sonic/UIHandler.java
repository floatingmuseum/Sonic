package floatingmuseum.sonic;

import android.os.Handler;
import android.os.Message;

import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.entity.UIMessage;

/**
 * Created by Floatingmuseum on 2017/4/1.
 */

public class UIHandler extends Handler {

    private DownloadTask task;

    public UIHandler(DownloadTask task) {
        this.task = task;
    }

    @Override
    public void handleMessage(Message msg) {
        if (task != null) {
            UIMessage message = (UIMessage) msg.obj;
            switch (message.getState()) {
                case UIMessage.THREAD_FETCH_CONTENT_LENGTH:
                    task.onFetchContentLength(message.getContentLength(),message.isSupportRange(),message.getHashCode());
                    break;
                case UIMessage.THREAD_INIT_THREAD_ERROR:
                    task.onInitThreadError(message.getDownloadException(),message.getHashCode());
                    break;
                case UIMessage.THREAD_INIT:
                    break;
                case UIMessage.THREAD_INIT_PAUSE:
                    task.onInitThreadPause(message.getHashCode());
                    break;
                case UIMessage.THREAD_START:
                    task.onProgress(message.getThreadInfo(),message.getHashCode());
                    break;
                case UIMessage.THREAD_PROGRESS:
                    task.onProgress(message.getThreadInfo(),message.getHashCode());
                    break;
                case UIMessage.THREAD_PAUSE:
                    task.onPause(message.getThreadInfo(),message.getHashCode());
                    break;
                case UIMessage.THREAD_FINISH:
                    task.onFinished(message.getThreadInfo(),message.getHashCode());
                    break;
                case UIMessage.THREAD_ERROR:
                    task.onError(message.getThreadInfo(),message.getDownloadException(),message.getHashCode());
//                    listener.onError(taskInfo, message.getDownloadException());
                    break;
            }
        }
    }
}
