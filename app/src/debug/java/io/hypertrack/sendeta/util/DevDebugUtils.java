package io.hypertrack.sendeta.util;


import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.logging.HTLog;


/**
 * Created by piyush on 03/07/16.
 */
public class DevDebugUtils {

    private static final String TAG = DevDebugUtils.class.getSimpleName();

    public static void installStetho(Application application) {
        Stetho.initializeWithDefaults(application);
    }

    public static void setHTLogLevel(int logLevel) {
        HyperTrack.enableDebugLogging(logLevel);
    }

    public static void sdkVersionMessage() {
        HTLog.i(TAG, "HyperTrack Live: SDK Version " + HyperTrack.getSDKVersion());
    }
}
