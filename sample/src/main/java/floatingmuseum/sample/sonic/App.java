package floatingmuseum.sample.sonic;

import android.app.Application;
import android.content.Context;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

//import com.squareup.leakcanary.LeakCanary;

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
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            Log.i(TAG, "LeakCanary.isInAnalyzerProcess");
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);

        initSonic();
    }

    private void initSonic() {
        Sonic.getInstance()
                .setActiveTaskNumber(2)
                .setMaxThreads(5)
                .setProgressResponseInterval(100)
                .setRetryTime(2)
                .setReadTimeout(3000)
                .setConnectTimeout(3000)
                .setDirPath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/SonicDownloads")
                .setLogEnabled()
                .setBroadcastAction("FloatingMuseum")
                .init(this);
    }
}
