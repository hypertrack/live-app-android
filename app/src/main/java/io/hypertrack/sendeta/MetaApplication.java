package io.hypertrack.sendeta;

import android.app.Application;
import android.util.Log;

import com.hypertrack.lib.HyperTrack;

import io.hypertrack.sendeta.util.DevDebugUtils;

/**
 * Created by suhas on 11/11/15.
 */
public class MetaApplication extends Application {

    private static MetaApplication mInstance;
    private static boolean activityVisible;

    public static synchronized MetaApplication getInstance() {
        return mInstance;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // Initialize HyperTrack SDK
        HyperTrack.initialize(this.getApplicationContext(), BuildConfig.HYPERTRACK_PK);
        HyperTrack.enableMockLocations(false);

        // Enable HyperTrack Debug Logging
        DevDebugUtils.setHTLogLevel(Log.ERROR);
        // Log HyperTrack SDK Version
        DevDebugUtils.sdkVersionMessage();
    }
}

