package floatingmuseum.sonic.utils;

import android.util.Log;

/**
 * Created by Floatingmuseum on 2017/5/17.
 */

public class LogUtil {

    private static boolean isEnabled = false;

    public static void enabled(boolean enabled) {
        isEnabled = enabled;
    }

    public static void i(String tag, String msg) {
        if (isEnabled) {
            Log.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isEnabled) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isEnabled) {
            Log.e(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (isEnabled) {
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isEnabled) {
            Log.w(tag, msg);
        }
    }
}
