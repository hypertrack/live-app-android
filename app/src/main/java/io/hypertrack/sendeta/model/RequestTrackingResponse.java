package io.hypertrack.sendeta.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 09/11/16.
 */
public class RequestTrackingResponse {

    @SerializedName("tracking_url")
    @Expose
    private String trackingURL;

    @SerializedName("task_id")
    @Expose
    private String taskID;

    public String getTrackingURL() {
        return trackingURL;
    }

    public void setTrackingURL(String trackingURL) {
        this.trackingURL = trackingURL;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }
}
