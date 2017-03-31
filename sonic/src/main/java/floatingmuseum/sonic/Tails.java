package floatingmuseum.sonic;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Floatingmuseum on 2017/3/31.
 * <p>
 * Tails is Sonic's best friend.
 */

public class Tails extends Service {

    private static Sonic sonic;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Sonic getSonic() {
        Context context = Sonic.getContext();
        if (isServiceRunning(context)) {
            context.startService(new Intent(context, Tails.class));
        }
        if (sonic == null) {
            sonic = Sonic.getInstance();
        }
        return sonic;
    }

    public static boolean isServiceRunning(Context context) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (serviceList == null || serviceList.size() == 0) return false;
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(Tails.class.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}
