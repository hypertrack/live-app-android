package com.hypertrack.lib.internal.common.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.internal.common.HTConstants;
import com.hypertrack.lib.internal.common.logging.HTLog;

/**
 * Created by piyush on 30/01/17.
 */
public class SDKUpdateUtil {

    private static final String TAG = SDKUpdateUtil.class.getSimpleName();
    private static final String SDK_VERSION_KEY = "com.hypertrack.sdk_version";

    /**
     * Called when App gets updated. Code which needs to be implemented on App Version Update
     * from oldAppVersion to currentAppVersion.
     *
     * @param context
     */
    public static void checkAndUpdateAppVersion(Context context) {

        int currentAppVersionCode = BuildConfig.VERSION_CODE;
        int oldAppVersionCode = getOldSDKVersion(context);

        // Check if this is a Fresh Install, or App Upgrade
        if (oldAppVersionCode > -1) {

            // Check if upgraded AppVersion is greater than the old one
            if (currentAppVersionCode > oldAppVersionCode) {
                // Handle SDKUpdate scenarios
            }
        } else {
            update(context, currentAppVersionCode);
        }
    }

    /**
     * Method to get oldSDKVersion already saved in SharedPreferences
     *
     * @param context
     * @return
     */
    private static int getOldSDKVersion(Context context) {
        try {
            SharedPreferences sharedpreferences = context.getSharedPreferences(HTConstants.HT_SHARED_PREFS_KEY,
                    Context.MODE_PRIVATE);
            return sharedpreferences.getInt(SDK_VERSION_KEY, -1);
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while getOldSDKVersion: " + e);
            return -1;
        }
    }

    /**
     * Method to update SharedPreferences on SDK Update
     *
     * @param context
     * @param currentSDKVersionCode
     */
    private static void update(Context context, int currentSDKVersionCode) {
        try {
            SharedPreferences sharedpreferences = context.getSharedPreferences(HTConstants.HT_SHARED_PREFS_KEY,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SDK_VERSION_KEY, currentSDKVersionCode);
            editor.apply();
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while SDKUpdateUtil.update: " + e);
        }
    }
}
