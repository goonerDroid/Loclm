package com.sublime.loclm;

import android.app.Application;

import com.sublime.loclm.Utils.Timber;

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
