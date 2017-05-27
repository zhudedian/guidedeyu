package com.ider.guidedeyu.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by Eric on 2017/5/20.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
