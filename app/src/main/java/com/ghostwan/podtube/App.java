package com.ghostwan.podtube;

import android.app.Application;
import android.os.StrictMode;
import teaspoon.TeaSpoon;

/**
 * Created by erwan on 25/03/2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TeaSpoon.initialize();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }


}
