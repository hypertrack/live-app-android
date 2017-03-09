package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.util.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ulhas on 12/03/16.
 */
public class HTAction {

    public static final String ACTION_STATUS_NOT_STARTED = "not_started";
    public static final String ACTION_STATUS_ASSIGNED = "assigned";
    public static final String ACTION_STATUS_DISPATCHING = "dispatching";
    public static final String ACTION_STATUS_USER_ON_THE_WAY = "on_the_way";
    public static final String ACTION_STATUS_USER_ARRIVING = "arriving";
    public static final String ACTION_STATUS_USER_ARRIVED = "arrived";
    public static final String ACTION_STATUS_COMPLETED = "completed";
    public static final String ACTION_STATUS_CANCELED = "canceled";
    public static final String ACTION_STATUS_ABORTED = "aborted";
    public static final String ACTION_STATUS_SUSPENDED = "suspended";

    public static final String ACTION_STATUS_NO_LOCATION = "no_location";
    public static final String ACTION_STATUS_LOCATION_LOST = "location_lost";

    public static final String ACTION_STATUS_CONNECTION_LOST = "connection_lost";
    public static final String ACTION_STATUS_CONNECTION_HEALTHY = "connection_healthy";

    public static final String ACTION_PICKUP = "pickup";
    public static final String ACTION_DELIVERY = "delivery";
    public static final String ACTION_DROPOFF = "dropoff";
    public static final String ACTION_VISIT = "visit";
    public static final String ACTION_STOPOVER = "stopover";
    public static final String ACTION_TASK = "task";

    @SerializedName("id")
    private String id;

    @SerializedName("user")
    private HTUser user;

    @SerializedName("type")
    private String action;

    @SerializedName("expected_place")
    private HTPlace expectedPlace;

    @SerializedName("expected_at")
    private Date expectedAT;

    @SerializedName("completed_place")
    private HTPlace completedPlace;

    @SerializedName("completed_at")
    private Date completedAT;

    @SerializedName("assigned_at")
    private Date assignedAT;

    @SerializedName("status")
    private String status;

    @SerializedName("eta")
    private Date ETA;

    @SerializedName("initial_eta")
    private Date initialETA;

    @SerializedName("tracking_url")
    private String trackingURL;

    @SerializedName("time_aware_polyline")
    private String timeAwarePolyline;

    @SerializedName("encoded_polyline")
    private String encodedPolyline;

    @SerializedName("distance")
    private String distance;

    @SerializedName("display")
    private HTDisplay display;

    @SerializedName("started_place")
    private HTPlace startPlace;

    @SerializedName("started_at")
    private Date startedAT;


    public HTAction() {
    }

    public HTAction(String actionID) {
        this.id = actionID;
    }


    public HTAction(String id, HTUser user, String action, HTPlace expectedPlace, Date expectedAT, HTPlace completedPlace, Date completedAT, Date assignedAT, String status, Date ETA, Date initialETA, String trackingURL, String timeAwarePolyline, String encodedPolyline, String distance, HTDisplay display, HTPlace startPlace, Date startedAT) {

        this.id = id;
        this.user = user;
        this.action = action;
        this.expectedPlace = expectedPlace;
        this.expectedAT = expectedAT;
        this.completedPlace = completedPlace;
        this.completedAT = completedAT;
        this.assignedAT = assignedAT;
        this.status = status;
        this.ETA = ETA;
        this.initialETA = initialETA;
        this.trackingURL = trackingURL;
        this.timeAwarePolyline = timeAwarePolyline;
        this.encodedPolyline = encodedPolyline;
        this.distance = distance;
        this.display = display;
        this.startPlace = startPlace;
        this.startedAT = startedAT;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HTUser getUser() {
        return user;
    }

    public void setUser(HTUser user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public HTPlace getExpectedPlace() {
        return expectedPlace;
    }

    public void setExpectedPlace(HTPlace expectedPlace) {
        this.expectedPlace = expectedPlace;
    }

    public Date getExpectedAT() {
        return expectedAT;
    }

    public void setExpectedAT(Date expectedAT) {
        this.expectedAT = expectedAT;
    }

    public HTPlace getCompletedPlace() {
        return completedPlace;
    }

    public void setCompletedPlace(HTPlace completedPlace) {
        this.completedPlace = completedPlace;
    }

    public Date getCompletedAT() {
        return completedAT;
    }

    public void setCompletedAT(Date completedAT) {
        this.completedAT = completedAT;
    }

    public Date getAssignedAT() {
        return assignedAT;
    }

    public void setAssignedAT(Date assignedAT) {
        this.assignedAT = assignedAT;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getETA() {
        return ETA;
    }

    public void setETA(Date ETA) {
        this.ETA = ETA;
    }

    public Date getInitialETA() {
        return initialETA;
    }

    public void setInitialETA(Date initialETA) {
        this.initialETA = initialETA;
    }

    public String getTrackingURL() {
        return trackingURL;
    }

    public void setTrackingURL(String trackingURL) {
        this.trackingURL = trackingURL;
    }

    public String getTimeAwarePolyline() {
        return timeAwarePolyline;
    }

    public void setTimeAwarePolyline(String timeAwarePolyline) {
        this.timeAwarePolyline = timeAwarePolyline;
    }

    public String getEncodedPolyline() {
        return encodedPolyline;
    }

    public void setEncodedPolyline(String encodedPolyline) {
        this.encodedPolyline = encodedPolyline;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public HTDisplay getActionDisplay() {
        return display;
    }

    public void setDisplay(HTDisplay display) {
        this.display = display;
    }

    public HTPlace getStartPlace() {
        return startPlace;
    }

    public void setStartPlace(HTPlace startPlace) {
        this.startPlace = startPlace;
    }

    public Date getStartedAT() {
        return startedAT;
    }

    public void setStartedAT(Date startedAT) {
        this.startedAT = startedAT;
    }

    @Override
    public String toString() {
        return "HTAction{" +
                "id='" + id + '\'' +
                ", user=" + user +
                ", action='" + action + '\'' +
                ", expectedPlace=" + expectedPlace +
                ", expectedAT=" + expectedAT +
                ", completedPlace=" + completedPlace +
                ", completedAT=" + completedAT +
                ", assignedAT=" + assignedAT +
                ", status='" + status + '\'' +
                ", ETA=" + ETA +
                ", initialETA=" + initialETA +
                ", trackingURL='" + trackingURL + '\'' +
                ", timeAwarePolyline='" + timeAwarePolyline + '\'' +
                ", encodedPolyline='" + encodedPolyline + '\'' +
                ", distance='" + distance + '\'' +
                ", display=" + display +
                ", startPlace=" + startPlace +
                ", startedAT=" + startedAT +
                '}';
    }

    public boolean notStarted() {
        return (ACTION_STATUS_NOT_STARTED.equalsIgnoreCase(this.status));
    }

    public boolean isInTransit() {
        // Return True if the action has either NOT_STARTED yet, has been COMPLETED or CANCELED
        return !(ACTION_STATUS_NOT_STARTED.equalsIgnoreCase(this.status)
                || ACTION_STATUS_COMPLETED.equalsIgnoreCase(this.status)
                || ACTION_STATUS_CANCELED.equalsIgnoreCase(this.status));
    }

    public boolean isCompleted() {
        return (ACTION_STATUS_COMPLETED.equalsIgnoreCase(this.status));
    }

    public boolean isSuspended() {
        return ACTION_STATUS_SUSPENDED.equalsIgnoreCase(this.status);
    }

    public boolean isCanceled() {
        return ACTION_STATUS_CANCELED.equalsIgnoreCase(this.status);
    }

    public boolean isAborted() {
        return ACTION_STATUS_ABORTED.equalsIgnoreCase(this.status);
    }

    public boolean hasActionFinished() {
        return (ACTION_STATUS_COMPLETED.equalsIgnoreCase(this.status)
                || ACTION_STATUS_CANCELED.equalsIgnoreCase(this.status)
                || ACTION_STATUS_ABORTED.equalsIgnoreCase(this.status)
                || ACTION_STATUS_SUSPENDED.equalsIgnoreCase(this.status));
    }

    public String getActionDateDisplayString() {
        if (this.assignedAT == null && this.completedAT == null) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        if (this.completedAT != null)
            return dateFormat.format(this.completedAT);
        else if (this.assignedAT != null)
            return dateFormat.format(this.assignedAT);

        return null;
    }

    public String getActionStartTimeDisplayString() {
        if (this.assignedAT == null) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        return dateFormat.format(this.assignedAT);
    }

    public String getActionEndTimeDisplayString() {
        if (this.completedAT == null) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        return dateFormat.format(this.completedAT);
    }

    public Integer getDurationInMinutes() {
        if (this.startedAT == null || this.completedAT == null) {
            return null;
        }

        long duration = this.completedAT.getTime() - this.startedAT.getTime();
        double durationInMinutes = Math.ceil((duration / (float) 1000) / (float) 60);
        return (int) durationInMinutes;
    }

    public Double getDistanceInKMS() {
        if (TextUtils.isEmpty(this.distance)) {
            return null;
        }

        return Double.parseDouble(this.distance) / 1000.0;
    }


}
