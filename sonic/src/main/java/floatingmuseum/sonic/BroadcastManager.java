package floatingmuseum.sonic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import floatingmuseum.sonic.entity.TaskInfo;

/**
 * Created by Floatingmuseum on 2017/5/24.
 */

public class BroadcastManager {

    private LocalBroadcastManager manager;
    private String action;

    public BroadcastManager(Context context, String action) {
        manager = LocalBroadcastManager.getInstance(context);
        this.action = action;
    }

    public void sendBroadcast(int type, TaskInfo taskInfo, DownloadException exception) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(Sonic.EXTRA_DOWNLOAD_TASK_INFO, taskInfo);
        if (Sonic.STATE_ERROR == type) {
            intent.putExtra(Sonic.EXTRA_DOWNLOAD_EXCEPTION, exception);
        }
        manager.sendBroadcast(intent);
    }
}
