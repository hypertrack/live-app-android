package io.hypertrack.meta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by suhas on 26/11/15.
 */
public class UserTrip {

    private String user;

    @SerializedName("hypertrack_trip_id")
    private String hypertrackTripId;

    private String id;

    @SerializedName("share_url")
    private String shareUrl;

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public UserTrip(String user, String hypertrackTripId) {
        this.user = user;
        this.hypertrackTripId = hypertrackTripId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHypertrackTripId() {
        return hypertrackTripId;
    }

    public void setHypertrackTripId(String hypertrackTripId) {
        this.hypertrackTripId = hypertrackTripId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UserTrip{" +
                "user='" + user + '\'' +
                ", hypertrackTripId='" + hypertrackTripId + '\'' +
                ", id='" + id + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
                '}';
    }
}
