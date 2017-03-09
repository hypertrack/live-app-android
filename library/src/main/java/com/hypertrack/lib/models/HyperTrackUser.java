package com.hypertrack.lib.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 28/02/17.
 */

public class HyperTrackUser {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
