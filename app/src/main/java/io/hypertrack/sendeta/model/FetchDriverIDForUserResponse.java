package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 12/09/16.
 */
public class FetchDriverIDForUserResponse {

    @SerializedName("id")
    private String userID;

    @SerializedName("hypertrack_driver_id")
    private String hypertrackDriverID;

    @SerializedName("publishable_key")
    private String publishableKey;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getHypertrackDriverID() {
        return hypertrackDriverID;
    }

    public void setHypertrackDriverID(String hypertrackDriverID) {
        this.hypertrackDriverID = hypertrackDriverID;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public void setPublishableKey(String publishableKey) {
        this.publishableKey = publishableKey;
    }
}
