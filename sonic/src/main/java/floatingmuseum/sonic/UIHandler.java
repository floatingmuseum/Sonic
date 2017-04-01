package floatingmuseum.sonic;

import android.os.Handler;
import android.os.Message;

import floatingmuseum.sonic.entity.TaskInfo;
import floatingmuseum.sonic.entity.UIListenerMessage;
import floatingmuseum.sonic.listener.DownloadListener;

/**
 * Created by Floatingmuseum on 2017/4/1.
 */

public class UIHandler extends Handler {

    private DownloadListener listener;

    @Override
    public void handleMessage(Message msg) {
        if (listener != null) {
            UIListenerMessage message = (UIListenerMessage) msg.obj;
            TaskInfo taskInfo = message.getTaskInfo();
            switch (message.getState()) {
                case Sonic.STATE_NONE:
                    break;
                case Sonic.STATE_DOWNLOADING:
                    listener.onProgress(taskInfo);
                    break;
                case Sonic.STATE_PAUSE:
                    listener.onPause(taskInfo);
                    break;
                case Sonic.STATE_WAITING:
                    listener.onWaiting(taskInfo);
                    break;
                case Sonic.STATE_ERROR:
                    listener.onError(taskInfo, message.getError());
                    break;
                case Sonic.STATE_FINISH:
                    listener.onFinish(taskInfo);
                    break;
            }
        }
    }

    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }
}
