package com.hypertrack.lib.internal.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import com.hypertrack.lib.internal.common.logging.HTLog;

import java.util.HashMap;

/**
 * Created by piyush on 10/10/16.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static boolean checkIfPowerSaverModeEnabled(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                return pm.isPowerSaveMode();
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while checkIfPowerSaverModeEnabled: " + e);
        }

        return false;
    }

    public static HashMap<String, String> getDeviceHeader() {
        String model = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Device-ID", manufacturer + " " + model);
        return headers;
    }

    public static String getDeviceId(Context context) {
        String device_uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return device_uuid != null ? device_uuid : "";
    }

    public static HashMap<String, String> getBatteryHeader(Context context) {
        Intent batteryIntent = context.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        double batteryPercentage = getBatteryPercentage(batteryIntent);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Battery", Double.toString(batteryPercentage));

        return headers;
    }

    private static double getBatteryPercentage(Intent batteryIntent) {
        int rawLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        double scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        double level = -1;
        if (rawLevel >= 0 && scale > 0) {
            level = rawLevel / scale;
        }

        return 100 * level;
    }
}
