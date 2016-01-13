package io.hypertrack.meta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by suhas on 13/01/16.
 *
 name (optional): A name for the device.
 active (default True): A boolean that determines whether the device will be sent notifications.
 device_id (optional): A UUID for the device obtained from Android/iOS APIs, if you wish to uniquely identify it.
 registration_id (required): The GCM registration id or the APNS token for the device.
 device_type: android/iphone
 */
public class DeviceInfo {

    private String name;

    private Boolean active = true;

    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("registration_id")
    private String registrationId;

    @SerializedName("device_type")
    private String deviceType = "android";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "name='" + name + '\'' +
                ", active=" + active +
                ", deviceId='" + deviceId + '\'' +
                ", registrationId='" + registrationId + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }
}
