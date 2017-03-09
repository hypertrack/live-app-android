package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 15/08/16.
 */
public class HTDisplay {

    @SerializedName("status_text")
    private String statusText;

    @SerializedName("sub_status_text")
    private String subStatusText;

    @SerializedName("show_summary")
    private boolean showSummary;

    @SerializedName("duration_remaining")
    private String durationRemaining;

    public boolean isShowSummary() {
        return showSummary;
    }

    public void setShowSummary(boolean showSummary) {
        this.showSummary = showSummary;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
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

    @Override
    public String toString() {
        return "HTDisplay{" +
                "statusText='" + statusText + '\'' +
                ", subStatusText='" + subStatusText + '\'' +
                ", showSummary=" + showSummary +
                ", durationRemaining='" + durationRemaining + '\'' +
                '}';
    }
}
