package com.hypertrack.live;

import android.app.Application;

import com.hypertrack.live.debug.DebugHelper;


public class App extends Application {

    private boolean isForeground = false;

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean foreground) {
        isForeground = foreground;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DebugHelper.start(this);
    }
}
