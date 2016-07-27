package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 27/07/16.
 */
public class GCMAddDeviceDTO {

    @SerializedName("device_type")
    private final String deviceType = "android";

    @SerializedName("registration_id")
    private String registrationId;

    public String getDeviceType() {
        return deviceType;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public GCMAddDeviceDTO(String registrationId) {
        this.registrationId = registrationId;
    }
}
