package io.hypertrack.sendeta;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.hypertrack.lib.HyperTrack;

import io.fabric.sdk.android.Fabric;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.util.DevDebugUtils;

/**
 * Created by suhas on 11/11/15.
 */
public class MetaApplication extends Application {

    private static MetaApplication mInstance;
    private static boolean activityVisible;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;

        // Initialize AnalyticsStore to start logging Analytics Events
        AnalyticsStore.init(this);

        // Initialize HyperTrack SDK
        HyperTrack.initialize(this.getApplicationContext(), BuildConfig.HYPERTRACK_PK);
        HyperTrack.enableMockLocations(true);
        //  HyperTrack.disablePersistentNotification(true);

        // (NOTE: IFF current Build Variant is DEBUG)
        // Initialize Stetho to debug Databases
        DevDebugUtils.installStetho(this);
        // Enable HyperTrack Debug Logging
        DevDebugUtils.setHTLogLevel(Log.VERBOSE);
        // Log HyperTrack SDK Version
        DevDebugUtils.sdkVersionMessage();
    }

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
}

