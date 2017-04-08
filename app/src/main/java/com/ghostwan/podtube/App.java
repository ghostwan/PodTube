package com.ghostwan.podtube;

import android.app.Application;
import com.liulishuo.filedownloader.FileDownloader;
import teaspoon.TeaSpoon;

/**
 * Created by erwan on 25/03/2017.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TeaSpoon.initialize();
        FileDownloader.init(getApplicationContext());
    }
}
