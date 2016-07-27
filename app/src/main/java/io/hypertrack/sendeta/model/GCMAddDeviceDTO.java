package io.hypertrack.sendeta.model;

import android.content.Context;
import android.provider.Settings;

import com.google.gson.annotations.SerializedName;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by piyush on 27/07/16.
 */
public class GCMAddDeviceDTO {

    @SerializedName("device_type")
    private final String deviceType = "Android";

    @SerializedName("registration_id")
    private String registrationId;

    @SerializedName("device_id")
    private String deviceId;

    public String getDeviceType() {
        return deviceType;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public GCMAddDeviceDTO(Context context, String registrationId) {
        this.registrationId = registrationId;
        this.deviceId = getDeviceUUID(context);
    }

    private String getDeviceUUID(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        UUID uuid = null;

        try {
            if (!"9774d56d682e549c".equals(androidId)) {
                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return uuid != null ? uuid.toString() : androidId;
    }
}
