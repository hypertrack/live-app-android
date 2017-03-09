package com.hypertrack.lib.internal.transmitter.models;

import android.location.Location;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.models.ExpandedLocation;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;

public class HyperTrackLocation extends ExpandedLocation {

    @SerializedName("activity")
    private String activity;

    @SerializedName("activity_confidence")
    private int activityConfidence;

    @SerializedName("provider")
    private String provider;

    @SerializedName("recorded_at")
    private String recordedAt;

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Integer getActivityConfidence() {
        return activityConfidence;
    }

    public void setActivityConfidence(Integer activityConfidence) {
        this.activityConfidence = activityConfidence;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }

    private HyperTrackLocation() {
    }

    public HyperTrackLocation(Location location) {
        this(location, null);
    }

    public HyperTrackLocation(Location location, String provider) {
        super(location);

        this.recordedAt = DateTimeUtility.getCurrentTime();
        this.provider = provider;
    }

    public void setActivityDetails(String activity, int activityConfidence) {
        this.setActivity(activity);
        this.setActivityConfidence(activityConfidence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HyperTrackLocation that = (HyperTrackLocation) o;

        if (activityConfidence != that.activityConfidence) return false;
        if (activity != null ? !activity.equals(that.activity) : that.activity != null)
            return false;
        if (provider != null ? !provider.equals(that.provider) : that.provider != null)
            return false;
        return recordedAt != null ? recordedAt.equals(that.recordedAt) : that.recordedAt == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (activity != null ? activity.hashCode() : 0);
        result = 31 * result + activityConfidence;
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (recordedAt != null ? recordedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HyperTrackLocation{" +
                "location=" + getGeoJSONLocation() +
                ", locationAccuracy=" + getAccuracy() +
                ", speed=" + getSpeed() +
                ", bearing=" + getBearing() +
                ", altitude=" + getAltitude() +
                ", recordedAt='" + getRecordedAt() + '\'' +
                ", activity=" + activity +
                ", activityConfidence=" + activityConfidence +
                '}';
    }
}