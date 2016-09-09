package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 29/08/16.
 */
public class AddTaskToTrackDTO {

    @SerializedName("short_code")
    private String shortCode;

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public AddTaskToTrackDTO(String shortCode) {
        this.shortCode = shortCode;
    }
}
