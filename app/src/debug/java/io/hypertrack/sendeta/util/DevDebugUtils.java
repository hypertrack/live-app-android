package io.hypertrack.sendeta.util;

import android.util.Log;

import com.hypertrack.lib.HyperTrack;


/**
 * Created by piyush on 03/07/16.
 */
public class DevDebugUtils {

    private static final String TAG = DevDebugUtils.class.getSimpleName();

    public static void setHTLogLevel(int logLevel) {

        HyperTrack.enableDebugLogging(logLevel);
    }

    public static void sdkVersionMessage() {
        Log.i(TAG, "HyperTrack Live: SDK Version " + HyperTrack.getSDKVersion());
    }
}
