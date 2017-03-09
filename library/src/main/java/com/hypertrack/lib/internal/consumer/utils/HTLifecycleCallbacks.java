package com.hypertrack.lib.internal.consumer.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.hypertrack.lib.internal.common.logging.HTLog;

/**
 * Created by suhas on 09/11/15.
 */
public class HTLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = HTLifecycleCallbacks.class.getSimpleName();
    private static HTLifecycleCallbacks mInstance;

    private HTLifecycleCallbacks(){
    }

    public synchronized static HTLifecycleCallbacks getInstance() {
        if (mInstance == null) {
            mInstance = new HTLifecycleCallbacks();
        }
        return mInstance;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        HTLog.v(TAG,"activity created");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        HTLog.v(TAG,"activity started");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        HTLog.v(TAG,"activity resumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        HTLog.v(TAG,"activity paused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        HTLog.v(TAG,"activity stopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        HTLog.v(TAG,"save instance");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        HTLog.v(TAG,"activity destroyed");
    }
}
