package org.xbase.android.sample;

import android.app.Application;

/**
 * Created by Administrator on 2016/8/22.
 */
public class App extends Application{
    public static String HOST = "http://shuiguorili.com:8080";

    @Override
    public void onCreate() {
        super.onCreate();
        org.xbase.android.utils.App.init(this);
    }
}
