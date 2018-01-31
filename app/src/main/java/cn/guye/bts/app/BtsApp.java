package cn.guye.bts.app;

import android.app.Application;


/**
 * Created by nieyu2 on 18/1/30.
 */

public class BtsApp extends Application {

    public static  BtsApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
