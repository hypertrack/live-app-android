package io.hypertrack.sendeta.util;

import android.app.Application;
import android.util.Log;

import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.lib.HyperTrack;


/**
 * Created by piyush on 03/07/16.
 */
public class DevDebugUtils {

    private static final String TAG = DevDebugUtils.class.getSimpleName();

    public static void installStetho(Application application) {
        //do nothing
    }

    public static void setHyperLogLevel(int logLevel) {
        HyperTrack.enableDebugLogging(Log.VERBOSE);
    }

    public static void sdkVersionMessage() {
        HyperLog.i(TAG, "HyperTrack Live: SDK Version " + HyperTrack.getSDKVersion());
    }
}