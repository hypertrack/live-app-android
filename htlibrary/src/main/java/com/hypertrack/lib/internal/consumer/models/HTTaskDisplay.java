package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 15/08/16.
 */
public class HTTaskDisplay {

    @SerializedName("status")
    private String status;

    @SerializedName("status_text")
    private String statusText;

    @SerializedName("sub_status")
    private String subStatus;

    @SerializedName("sub_status_text")
    private String subStatusText;

    @SerializedName("duration_remaining")
    private String durationRemaining;

    @SerializedName("show_summary")
    private boolean showTaskSummary;

    @SerializedName("sub_status_duration")
    private String subStatusDuration;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public String getSubStatusText() {
        return subStatusText;
    }

    public void setSubStatusText(String subStatusText) {
        this.subStatusText = subStatusText;
    }

    public String getDurationRemaining() {
        return durationRemaining;
    }

    public void setDurationRemaining(String durationRemaining) {
        this.durationRemaining = durationRemaining;
    }

    public boolean isShowTaskSummary() {
        return showTaskSummary;
    }

    public void setShowTaskSummary(boolean showTaskSummary) {
        this.showTaskSummary = showTaskSummary;
    }

    public String getSubStatusDuration() {
        return subStatusDuration;
    }

    public void setSubStatusDuration(String subStatusDuration) {
        this.subStatusDuration = subStatusDuration;
    }

    @Override
    public String toString() {
        return "HTTaskDisplay{" +
                "status='" + (status != null ? status : "") + '\'' +
                ", statusText='" + (statusText != null ? statusText : "") + '\'' +
                ", subStatus='" + (subStatus != null ? subStatus : "") + '\'' +
                ", subStatusText='" + (subStatusText != null ? subStatusText : "") + '\'' +
                ", durationRemaining='" + (durationRemaining != null ? durationRemaining : "") + '\'' +
                ", showTaskSummary=" + showTaskSummary +
                ", subStatusDuration='" + (subStatusDuration != null ? subStatusDuration : "") + '\'' +
                '}';
    }
}
