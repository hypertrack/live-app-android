package com.hypertrack.lib.internal.transmitter.devicehealth;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.HTConstants;
import com.hypertrack.lib.internal.common.logging.HTLog;

/**
 * Created by Arjun on 24/03/16.
 */
class BatteryState {

    private final String TAG = "BatteryState";

    private final int INVALID_BATTERY_PERCENTAGE = -1;

    private static final String HT_SHARED_PREFERENCE_BATTERY_PERCENTAGE = "com.hypertrack:Battery.BatteryPercentage";
    private static final String HT_SHARED_PREFERENCE_BATTERY_CHARGING_STATUS = "com.hypertrack:Battery.ChargingStatus";
    private static final String HT_SHARED_PREFERENCE_BATTERY_POWER_STATUS = "com.hypertrack:Battery.PowerStatus";
    private static final String HT_SHARED_PREFERENCE_BATTERY_POWER_SAVER_MODE = "com.hypertrack:Battery.PowerSaverMode";
    private static final String HT_SHARED_PREFERENCE_BATTERY_IDLE_MODE = "com.hypertrack:Battery.IdleMode";

    private final String BATTERY_STATUS_UNKNOWN = "UNKNOWN";
    private final String BATTERY_STATUS_CHARGING = "CHARGING";
    private final String BATTERY_STATUS_DISCHARGING = "DISCHARGING";
    private final String BATTERY_STATUS_NOT_CHARGING = "NOT_CHARGING";
    private final String BATTERY_STATUS_FULL = "FULL";
    private final String BATTERY_STATUS_INVALID = "INVALID";

    private final String BATTERY_POWER_SOURCE_BATTERY = "battery";
    private final String BATTERY_POWER_SOURCE_AC = "ac";
    private final String BATTERY_POWER_SOURCE_USB = "usb";
    private final String BATTERY_POWER_SOURCE_WIRELESS = "wireless";

    private final String BATTERY_POWER_SOURCE_INVALID = "invalid";
    private Context mContext;

    BatteryState(Context context) {
        mContext = context;
    }

    class BatteryHealth {
        @SerializedName("percentage")
        private Integer percentage;

        @SerializedName("charging")
        private String chargingStatus;

        @SerializedName("source")
        private String powerSource;

        @SerializedName("power_saver")
        private Boolean powerSaverMode;

        @SerializedName("idle_mode")
        private Boolean idleMode;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BatteryHealth that = (BatteryHealth) o;

            if (!percentage.equals(that.percentage)) return false;
            if (!chargingStatus.equals(that.chargingStatus)) return false;
            if (!powerSource.equals(that.powerSource)) return false;
            if (powerSaverMode != null ? !powerSaverMode.equals(that.powerSaverMode) : that.powerSaverMode != null)
                return false;
            return idleMode != null ? idleMode.equals(that.idleMode) : that.idleMode == null;
        }
    }

    BatteryHealth getBatteryHealth() {
        try {
            BatteryHealth batteryHealth = new BatteryHealth();
            Intent batteryIntent = mContext.getApplicationContext().registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            // Fetch Current BatteryHealth
            batteryHealth.percentage = (int) getBatteryPercentage(batteryIntent);
            batteryHealth.chargingStatus = getBatteryChargingStatus(batteryIntent);
            batteryHealth.powerSource = getBatteryPowerSource(batteryIntent);
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            // Fetch PowerSaverMode setting for API Version >= 21 (Lollipop)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                batteryHealth.powerSaverMode = pm.isPowerSaveMode();
            }

            // Fetch IdleMode setting for API Version >= 21 (Lollipop)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                batteryHealth.idleMode = pm.isDeviceIdleMode();
            }

            // Fetch Saved BatteryHealth
            BatteryHealth savedBatteryHealth = getSavedBatteryHealth();

            if (!batteryHealth.equals(savedBatteryHealth)) {
                saveBatteryHealth(batteryHealth);
                return batteryHealth;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getBatteryHealth: " + e.getMessage());
        }

        return null;
    }

    // Methods to clear cached BatteryState Data
    static void clearSavedBatteryStateData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(HT_SHARED_PREFERENCE_BATTERY_PERCENTAGE);
        editor.remove(HT_SHARED_PREFERENCE_BATTERY_CHARGING_STATUS);
        editor.remove(HT_SHARED_PREFERENCE_BATTERY_POWER_STATUS);
        editor.remove(HT_SHARED_PREFERENCE_BATTERY_POWER_SAVER_MODE);
        editor.remove(HT_SHARED_PREFERENCE_BATTERY_IDLE_MODE);
        editor.apply();
    }

    private double getBatteryPercentage(Intent batteryIntent) {
        int rawLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        double scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        double level = -1;
        if (rawLevel >= 0 && scale > 0) {
            level = rawLevel / scale;
        }

        return 100 * level;
    }

    private String getBatteryChargingStatus(Intent batteryIntent) {
        int batteryPowerStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);

        switch (batteryPowerStatus) {
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                return BATTERY_STATUS_UNKNOWN;

            case BatteryManager.BATTERY_STATUS_CHARGING:
                return BATTERY_STATUS_CHARGING;

            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return BATTERY_STATUS_DISCHARGING;

            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return BATTERY_STATUS_NOT_CHARGING;

            case BatteryManager.BATTERY_STATUS_FULL:
                return BATTERY_STATUS_FULL;

            default:
                return BATTERY_STATUS_INVALID;
        }
    }

    private String getBatteryPowerSource(Intent batteryIntent) {
        int batteryPowerSource = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, INVALID_BATTERY_PERCENTAGE);

        switch (batteryPowerSource) {
            case 0:
                return BATTERY_POWER_SOURCE_BATTERY;

            case BatteryManager.BATTERY_PLUGGED_AC:
                return BATTERY_POWER_SOURCE_AC;

            case BatteryManager.BATTERY_PLUGGED_USB:
                return BATTERY_POWER_SOURCE_USB;

            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return BATTERY_POWER_SOURCE_WIRELESS;

            default:
                return BATTERY_POWER_SOURCE_INVALID;
        }
    }

    private BatteryHealth getSavedBatteryHealth() {
        BatteryHealth batteryHealth = new BatteryHealth();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY,
                Context.MODE_PRIVATE);

        // Check if BatteryHealth keys were present, Return null otherwise
        if (sharedPreferences.contains(HT_SHARED_PREFERENCE_BATTERY_PERCENTAGE)
                || sharedPreferences.contains(HT_SHARED_PREFERENCE_BATTERY_CHARGING_STATUS)
                || sharedPreferences.contains(HT_SHARED_PREFERENCE_BATTERY_POWER_STATUS)
                || sharedPreferences.contains(HT_SHARED_PREFERENCE_BATTERY_POWER_SAVER_MODE)
                || sharedPreferences.contains(HT_SHARED_PREFERENCE_BATTERY_IDLE_MODE)) {

            batteryHealth.percentage = sharedPreferences.getInt(HT_SHARED_PREFERENCE_BATTERY_PERCENTAGE, INVALID_BATTERY_PERCENTAGE);
            batteryHealth.chargingStatus = sharedPreferences.getString(HT_SHARED_PREFERENCE_BATTERY_CHARGING_STATUS, null);
            batteryHealth.powerSource = sharedPreferences.getString(HT_SHARED_PREFERENCE_BATTERY_POWER_STATUS, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                batteryHealth.powerSaverMode = sharedPreferences.getBoolean(HT_SHARED_PREFERENCE_BATTERY_POWER_SAVER_MODE, false);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                batteryHealth.idleMode = sharedPreferences.getBoolean(HT_SHARED_PREFERENCE_BATTERY_IDLE_MODE, false);
            }
            return batteryHealth;
        }

        return null;
    }

    private void saveBatteryHealth(BatteryHealth batteryHealth) {
        SharedPreferences sharedpreferences = mContext.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(HT_SHARED_PREFERENCE_BATTERY_PERCENTAGE, batteryHealth.percentage);
        editor.putString(HT_SHARED_PREFERENCE_BATTERY_CHARGING_STATUS, batteryHealth.chargingStatus);
        editor.putString(HT_SHARED_PREFERENCE_BATTERY_POWER_STATUS, batteryHealth.powerSource);
        editor.putBoolean(HT_SHARED_PREFERENCE_BATTERY_POWER_SAVER_MODE, batteryHealth.powerSaverMode);
        editor.putBoolean(HT_SHARED_PREFERENCE_BATTERY_IDLE_MODE, batteryHealth.idleMode);
        editor.apply();
    }
}
