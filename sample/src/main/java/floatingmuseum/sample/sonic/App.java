package floatingmuseum.sample.sonic;

import android.app.Application;
import android.content.Context;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import floatingmuseum.sonic.Sonic;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class App extends Application {

    private static final String TAG = App.class.getName();
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        Log.i(TAG, "Sonic...App...onCreate()");
        if (LeakCanary.isInAnalyzerProcess(this)) {
            Log.i(TAG, "LeakCanary.isInAnalyzerProcess");
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        initSonic();
    }

    private void initSonic() {
        Sonic.getInstance()
                .setActiveTaskNumber(2)
                .setMaxThreads(5)
                .setProgressResponseInterval(300)
                .setRetryTime(4)
                .setReadTimeout(3000)
                .setConnectTimeout(3000)
                .setDirPath(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())
                .init(this);
    }
}
