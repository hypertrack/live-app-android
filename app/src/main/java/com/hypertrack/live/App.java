package com.hypertrack.live;

import android.app.Application;

import com.google.android.libraries.places.api.Places;


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

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDlJnfZX9OsayqA3EtVtUI-yGa-OBRoseU");
        }
    }
}
