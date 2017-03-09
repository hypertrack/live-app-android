package com.hypertrack.lib.internal.transmitter.devicehealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.internal.common.HTConstants;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.Utils;

import java.util.TimeZone;

/**
 * Created by piyush on 06/08/16.
 */

/** package */ class DeviceModelState {
    private static final String TAG = "DeviceModelState";

    private static final String HT_SHARED_PREFERENCE_DEVICE_ID = "com.hypertrack:DeviceModel.DeviceID";
    private static final String HT_SHARED_PREFERENCE_OS = "com.hypertrack:DeviceModel.OS";
    private static final String HT_SHARED_PREFERENCE_OS_VERSION = "com.hypertrack:DeviceModel.OSVersion";
    private static final String HT_SHARED_PREFERENCE_CUSTOM_OS_VERSION = "com.hypertrack:DeviceModel.CustomOSVersion";
    private static final String HT_SHARED_PREFERENCE_SDK_VERSION = "com.hypertrack:DeviceModel.SDKVersion";
    private static final String HT_SHARED_PREFERENCE_DEVICE = "com.hypertrack:DeviceModel.Device";
    private static final String HT_SHARED_PREFERENCE_MODEL = "com.hypertrack:DeviceModel.Model";
    private static final String HT_SHARED_PREFERENCE_PRODUCT = "com.hypertrack:DeviceModel.Product";
    private static final String HT_SHARED_PREFERENCE_BRAND = "com.hypertrack:DeviceModel.Brand";
    private static final String HT_SHARED_PREFERENCE_MANUFACTURER = "com.hypertrack:DeviceModel.Manufacturer";
    private static final String HT_SHARED_PREFERENCE_TIME_ZONE = "com.hypertrack:DeviceModel.TimeZone";

    private Context mContext;

    DeviceModelState(Context mContext) {
        this.mContext = mContext;
    }

    // Methods to clear cached LocationState Data
    static void clearSavedDeviceModelStateData(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(HT_SHARED_PREFERENCE_DEVICE_ID);
        editor.remove(HT_SHARED_PREFERENCE_OS);
        editor.remove(HT_SHARED_PREFERENCE_OS_VERSION);
        editor.remove(HT_SHARED_PREFERENCE_CUSTOM_OS_VERSION);
        editor.remove(HT_SHARED_PREFERENCE_SDK_VERSION);
        editor.remove(HT_SHARED_PREFERENCE_DEVICE);
        editor.remove(HT_SHARED_PREFERENCE_MODEL);
        editor.remove(HT_SHARED_PREFERENCE_PRODUCT);
        editor.remove(HT_SHARED_PREFERENCE_BRAND);
        editor.remove(HT_SHARED_PREFERENCE_MANUFACTURER);
        editor.remove(HT_SHARED_PREFERENCE_TIME_ZONE);
        editor.apply();
    }

    DeviceModelHealth getDeviceModelHealth() {

        try {
            // Fetch Current DeviceModelState Params
            DeviceModelHealth deviceModelHealth = new DeviceModelHealth();
            deviceModelHealth.deviceID = Utils.getDeviceId(mContext);
            deviceModelHealth.os = "Android";
            deviceModelHealth.osVersion = Build.VERSION.RELEASE;
            deviceModelHealth.customOSVersion = System.getProperty("os.version");
            deviceModelHealth.sdkVersion = BuildConfig.VERSION_NAME;
            deviceModelHealth.device = Build.DEVICE;
            deviceModelHealth.model = Build.MODEL;
            deviceModelHealth.product = Build.PRODUCT;
            deviceModelHealth.brand = Build.BRAND;
            deviceModelHealth.manufacturer = Build.MANUFACTURER;
            deviceModelHealth.timeZone = TimeZone.getDefault().getID();

            // Fetch Cached DeviceModelState Params
            DeviceModelHealth savedDeviceModelHealth = getSavedDeviceModelHealth();

            if (!deviceModelHealth.equals(savedDeviceModelHealth)) {
                saveDeviceModelHealth(deviceModelHealth);
                return deviceModelHealth;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getDeviceModelHealth: " + e.getMessage());
        }

        return null;
    }

    private DeviceModelHealth getSavedDeviceModelHealth() {
        try {
            DeviceModelHealth deviceModelHealth = new DeviceModelHealth();
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY,
                    Context.MODE_PRIVATE);

            // Check if DeviceModelHealth keys were present, Return null otherwise
            if (sharedPreferences.contains(HT_SHARED_PREFERENCE_DEVICE_ID)) {

                deviceModelHealth.deviceID = sharedPreferences.getString(HT_SHARED_PREFERENCE_DEVICE_ID, null);
                deviceModelHealth.os = sharedPreferences.getString(HT_SHARED_PREFERENCE_OS, null);
                deviceModelHealth.osVersion = sharedPreferences.getString(HT_SHARED_PREFERENCE_OS_VERSION, null);
                deviceModelHealth.customOSVersion = sharedPreferences.getString(HT_SHARED_PREFERENCE_CUSTOM_OS_VERSION, null);
                deviceModelHealth.sdkVersion = sharedPreferences.getString(HT_SHARED_PREFERENCE_SDK_VERSION, null);
                deviceModelHealth.device = sharedPreferences.getString(HT_SHARED_PREFERENCE_DEVICE, null);
                deviceModelHealth.model = sharedPreferences.getString(HT_SHARED_PREFERENCE_MODEL, null);
                deviceModelHealth.product = sharedPreferences.getString(HT_SHARED_PREFERENCE_PRODUCT, null);
                deviceModelHealth.brand = sharedPreferences.getString(HT_SHARED_PREFERENCE_BRAND, null);
                deviceModelHealth.manufacturer = sharedPreferences.getString(HT_SHARED_PREFERENCE_MANUFACTURER, null);
                deviceModelHealth.timeZone = sharedPreferences.getString(HT_SHARED_PREFERENCE_TIME_ZONE, null);
                return deviceModelHealth;
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while getSavedDeviceModelHealth: " + e);
        }
        return null;
    }

    private void saveDeviceModelHealth(DeviceModelHealth deviceModelHealth) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(HTConstants.HT_DEVICE_HEALTH_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HT_SHARED_PREFERENCE_DEVICE_ID, deviceModelHealth.deviceID);
        editor.putString(HT_SHARED_PREFERENCE_OS, deviceModelHealth.os);
        editor.putString(HT_SHARED_PREFERENCE_OS_VERSION, deviceModelHealth.osVersion);
        editor.putString(HT_SHARED_PREFERENCE_CUSTOM_OS_VERSION, deviceModelHealth.customOSVersion);
        editor.putString(HT_SHARED_PREFERENCE_SDK_VERSION, deviceModelHealth.sdkVersion);
        editor.putString(HT_SHARED_PREFERENCE_DEVICE, deviceModelHealth.device);
        editor.putString(HT_SHARED_PREFERENCE_MODEL, deviceModelHealth.model);
        editor.putString(HT_SHARED_PREFERENCE_PRODUCT, deviceModelHealth.product);
        editor.putString(HT_SHARED_PREFERENCE_BRAND, deviceModelHealth.brand);
        editor.putString(HT_SHARED_PREFERENCE_MANUFACTURER, deviceModelHealth.manufacturer);
        editor.putString(HT_SHARED_PREFERENCE_TIME_ZONE, deviceModelHealth.timeZone);
        editor.apply();
    }

    class DeviceModelHealth {
        @SerializedName("device_id")
        private String deviceID;

        @SerializedName("os")
        private String os;

        @SerializedName("os_version")
        private String osVersion;

        @SerializedName("custom_os_version")
        private String customOSVersion;

        @SerializedName("sdk_version")
        private String sdkVersion;

        @SerializedName("device")
        private String device;

        @SerializedName("model")
        private String model;

        @SerializedName("product")
        private String product;

        @SerializedName("brand")
        private String brand;

        @SerializedName("manufacturer")
        private String manufacturer;

        @SerializedName("time_zone")
        private String timeZone;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DeviceModelHealth that = (DeviceModelHealth) o;

            if (!deviceID.equals(that.deviceID)) return false;
            if (!os.equals(that.os)) return false;
            if (!osVersion.equals(that.osVersion)) return false;
            if (!customOSVersion.equals(that.customOSVersion)) return false;
            if (!sdkVersion.equals(that.sdkVersion)) return false;
            if (!device.equals(that.device)) return false;
            if (!model.equals(that.model)) return false;
            if (!product.equals(that.product)) return false;
            if (!brand.equals(that.brand)) return false;
            if (!manufacturer.equals(that.manufacturer)) return false;
            return timeZone.equals(that.timeZone);
        }
    }
}
