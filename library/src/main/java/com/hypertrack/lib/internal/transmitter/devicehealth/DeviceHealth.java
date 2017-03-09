package com.hypertrack.lib.internal.transmitter.devicehealth;

import android.content.Context;

import com.hypertrack.lib.internal.common.logging.HTLog;

/**
 * Created by piyush on 05/08/16.
 */

public class DeviceHealth {
    private static final String TAG = "DeviceHealth";

    private BatteryState.BatteryHealth batteryHealth;
    private RadioState.RadioHealth radioHealth;
    private LocationState.LocationHealth locationHealth;
    private DeviceModelState.DeviceModelHealth deviceModelHealth;

    public BatteryState.BatteryHealth getBatteryHealth() {
        return batteryHealth;
    }

    public RadioState.RadioHealth getRadioHealth() {
        return radioHealth;
    }

    public LocationState.LocationHealth getLocationHealth() {
        return locationHealth;
    }

    public DeviceModelState.DeviceModelHealth getDeviceModelHealth() {
        return deviceModelHealth;
    }

    private boolean isEmpty() {
        return !(batteryHealth != null || locationHealth != null || radioHealth != null
                || deviceModelHealth != null);
    }

    public static DeviceHealth getDeviceHealth(Context context) {
        DeviceHealth deviceHealth = new DeviceHealth();

        try {
            deviceHealth.batteryHealth = getBatteryHealth(context);
            deviceHealth.radioHealth = getRadioHealth(context);
            deviceHealth.locationHealth = getLocationHealth(context);
            deviceHealth.deviceModelHealth = getDeviceModelHealth(context);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getDeviceHealth: " + e.getMessage());
        }

        return deviceHealth.isEmpty() ? null : deviceHealth;
    }

    private static BatteryState.BatteryHealth getBatteryHealth(Context context) {
        BatteryState batteryState = new BatteryState(context);
        return batteryState.getBatteryHealth();
    }

    private static RadioState.RadioHealth getRadioHealth(Context context) {
        RadioState radioState = new RadioState(context);
        return radioState.getRadioHealth();
    }

    private static LocationState.LocationHealth getLocationHealth(Context context) {
        LocationState locationState = new LocationState(context);
        return locationState.getLocationHealth();
    }

    private static DeviceModelState.DeviceModelHealth getDeviceModelHealth(Context context) {
        DeviceModelState deviceModelState = new DeviceModelState(context);
        return deviceModelState.getDeviceModelHealth();
    }

    public static void clearSavedDeviceHealthParams(Context context) {
        BatteryState.clearSavedBatteryStateData(context);
        RadioState.clearSavedRadioStateData(context);
        LocationState.clearSavedLocationStateData(context);
        DeviceModelState.clearSavedDeviceModelStateData(context);
    }
}
