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

    @SerializedName("track_uri")
    private String trackUri;

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

    public String getTrackUri() {
        return trackUri;
    }

    public void setTrackUri(String trackUri) {
        this.trackUri = trackUri;
    }

    @Override
    public String toString() {
        return "UserTrip{" +
                "user='" + user + '\'' +
                ", hypertrackTripId='" + hypertrackTripId + '\'' +
                ", id='" + id + '\'' +
                ", trackUri='" + trackUri + '\'' +
                '}';
    }
}
