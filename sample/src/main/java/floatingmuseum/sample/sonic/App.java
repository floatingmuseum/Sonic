package floatingmuseum.sample.sonic;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

import floatingmuseum.sonic.Sonic;

/**
 * Created by Floatingmuseum on 2017/3/31.
 */

public class App extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        initSonic();
    }

    private void initSonic() {

        GlobeConfig globeConfig = new GlobeConfig();

        Sonic.getInstance().init(this);
    }
}
