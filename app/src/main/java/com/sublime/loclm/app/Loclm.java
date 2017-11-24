package com.sublime.loclm.app;

import android.app.Application;

import com.sublime.loclm.BuildConfig;
import com.sublime.loclm.utils.Timber;

/**
 * Created by goonerdroid
 * on 21/11/17.
 */

public class Loclm extends Application {

    @Override public void onCreate() {
        super.onCreate();
        //Initializes Timber logging only on debug build :-)
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
