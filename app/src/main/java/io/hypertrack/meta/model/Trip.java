package io.hypertrack.meta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by suhas on 26/11/15.
 */
public class Trip {

    @SerializedName("hypertrack_trip_id")
    private String hypertrackTripID;

    @SerializedName("hypertrack_task_id")
    private String hypertrackTaskID;

    private String id;

    @SerializedName("share_url")
    private String shareUrl;

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getHypertrackTripID() {
        return hypertrackTripID;
    }

    public void setHypertrackTripID(String hypertrackTripID) {
        this.hypertrackTripID = hypertrackTripID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHypertrackTaskID() {
        return hypertrackTaskID;
    }

    public void setHypertrackTaskID(String hypertrackTaskID) {
        this.hypertrackTaskID = hypertrackTaskID;
    }

    @Override
    public String toString() {
        return "Trip{" +
                ", hypertrackTripID='" + hypertrackTripID + '\'' +
                ", hypertrackTaskID='" + hypertrackTaskID + '\'' +
                ", id='" + id + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
                '}';
    }
}
