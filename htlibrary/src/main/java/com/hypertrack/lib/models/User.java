package com.hypertrack.lib.models;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;

import java.util.List;

/**
 * Created by piyush on 28/02/17.
 */

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("lookup_id")
    private String lookupId;

    @SerializedName("availability_status")
    private String availabilityStatus;

    @SerializedName("pending_actions")
    private List<String> pendingActions;

    @SerializedName("last_location")
    private HyperTrackLocation lastLocation;

    @SerializedName("last_heartbeat_at")
    private String lastHeartbeatAt;

    @SerializedName("last_online_at")
    private String lastOnlineAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("modified_at")
    private String modifiedAt;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getLookupId() {
        return lookupId;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public List<String> getPendingActions() {
        return pendingActions;
    }

    public HyperTrackLocation getLastLocation() {
        return lastLocation;
    }

    public String getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public String getLastOnlineAt() {
        return lastOnlineAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }
}