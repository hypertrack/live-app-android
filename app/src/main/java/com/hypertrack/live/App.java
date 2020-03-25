package com.hypertrack.live;

import android.app.Application;

import com.google.android.libraries.places.api.Places;
import com.hypertrack.sdk.HyperTrack;


public class App extends Application {
    public static final String TAG = "LApp:";


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

        HyperTrack.enableDebugLogging();
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBKZejrZNZpLlemrH28Nc46XzHsRSVRxKI");
        }
    }
}
