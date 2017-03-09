package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;

/**
 * Created by Aman Jain on 05/03/17.
 */
public class HTUser {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("group_id")
    @Expose
    private String groupId;

    @SerializedName("lookup_id")
    @Expose
    private String lookupId;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("phone")
    @Expose
    private String phone;

    @SerializedName("photo")
    @Expose
    private String photoURL;

    @SerializedName("availability_status")
    @Expose
    private String availabilityStatus;

    @SerializedName("last_location")
    @Expose
    private HyperTrackLocation lastLocation;

    @SerializedName("last_online_at")
    @Expose
    private String lastOnlineAt;

    @SerializedName("display")
    @Expose
    private HTDisplay display;

    @SerializedName("created_at")
    @Expose
    private String createdAt;

    @SerializedName("modified_at")
    @Expose
    private String modifiedAt;

    public HTUser(String id, String groupId, String lookupId, String name, String phone, String photoURL,
                  String availabilityStatus, HyperTrackLocation lastLocation, String lastOnlineAt,
                  HTDisplay display, String createdAt, String modifiedAt) {
        this.id = id;
        this.groupId = groupId;
        this.lookupId = lookupId;
        this.name = name;
        this.phone = phone;
        this.photoURL = photoURL;
        this.availabilityStatus = availabilityStatus;
        this.lastLocation = lastLocation;
        this.lastOnlineAt = lastOnlineAt;
        this.display = display;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getLookupId() {
        return lookupId;
    }

    public void setLookupId(String lookupId) {
        this.lookupId = lookupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photo) {
        this.photoURL = photo;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public HyperTrackLocation getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(HyperTrackLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getLastOnlineAt() {
        return lastOnlineAt;
    }

    public void setLastOnlineAt(String lastOnlineAt) {
        this.lastOnlineAt = lastOnlineAt;
    }

    public HTDisplay getDisplay() {
        return display;
    }

    public void setDisplay(HTDisplay display) {
        this.display = display;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public String toString() {
        return "HTUser{" +
                "id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", lookupId='" + lookupId + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", photoURL='" + photoURL + '\'' +
                ", availabilityStatus='" + availabilityStatus + '\'' +
                ", lastLocation=" + lastLocation +
                ", lastOnlineAt='" + lastOnlineAt + '\'' +
                ", display=" + display +
                ", createdAt='" + createdAt + '\'' +
                ", modifiedAt='" + modifiedAt + '\'' +
                '}';
    }
}
