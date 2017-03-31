package floatingmuseum.sample.sonic;

import android.app.Application;
import android.content.Context;

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
        Sonic.init(this);
    }
}
