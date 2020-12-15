package com.amin.saazangplayer;

import android.app.Application;

import com.amin.saazangplayer.di.ApplicationModule;
import com.amin.saazangplayer.di.DaggerSPlayerComponnent;
import com.amin.saazangplayer.di.SPlayerComponnent;

public class App extends Application {
    private static App app;
    private SPlayerComponnent sPlayerComponnent;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        sPlayerComponnent = DaggerSPlayerComponnent.builder()
                .applicationModule(new ApplicationModule(this)).build();
    }

    public static App getApp() {
        return app;

    }

    public SPlayerComponnent getsPlayerComponnent() {
        return sPlayerComponnent;
    }
}
