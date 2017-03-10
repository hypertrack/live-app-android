package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.models.Place;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ulhas on 12/03/16.
 */
public class HTTask {

    public static final String TASK_STATUS_NOT_STARTED = "not_started";
    public static final String TASK_STATUS_DISPATCHING = "dispatching";
    public static final String TASK_STATUS_USER_ON_THE_WAY = "on_the_way";
    public static final String TASK_STATUS_USER_ARRIVING = "arriving";
    public static final String TASK_STATUS_USER_ARRIVED = "arrived";
    public static final String TASK_STATUS_COMPLETED = "completed";
    public static final String TASK_STATUS_CANCELED = "canceled";
    public static final String TASK_STATUS_ABORTED = "aborted";
    public static final String TASK_STATUS_SUSPENDED = "suspended";

    public static final String TASK_STATUS_NO_LOCATION = "no_location";
    public static final String TASK_STATUS_LOCATION_LOST = "location_lost";

    public static final String TASK_STATUS_CONNECTION_LOST = "connection_lost";
    public static final String TASK_STATUS_CONNECTION_HEALTHY = "connection_healthy";

    public static final String TASK_ACTION_DELIVERY = "delivery";
    public static final String TASK_ACTION_VISIT = "visit";
    public static final String TASK_ACTION_PICKUP = "pickup";
    public static final String TASK_ACTION_TASK = "task";

    @SerializedName("id")
    private String id;

    @SerializedName("order_id")
    private String orderID;

    @SerializedName("trip_id")
    private String tripID;

    @SerializedName("is_user_live")
    private Boolean isUserLive;

    @SerializedName("status")
    private String status;

    @SerializedName("connection_status")
    private String connectionStatus;

    @SerializedName("display")
    private HTTaskDisplay taskDisplay;

    @SerializedName("action")
    private String action;

    @SerializedName("eta")
    private Date ETA;

    @SerializedName("initial_eta")
    private Date initialETA;

    @SerializedName("committed_eta")
    private Date committedETA;

    @SerializedName("start_time")
    private Date startTime;

    @SerializedName("completion_time")
    private Date completionTime;

    @SerializedName("cancelation_time")
    private Date cancellationTime;

    @SerializedName("user_id")
    private String userID;

    @SerializedName("user")
    private HTUser user;

    @SerializedName("start_address")
    private String startAddress;

    @SerializedName("start_location")
    private GeoJSONLocation startLocation;

    @SerializedName("destination")
    private Place destination;

    @SerializedName("hub")
    private Place hub;

    @SerializedName("completion_address")
    private String completionAddress;

    @SerializedName("completion_location")
    private GeoJSONLocation completionLocation;

    @SerializedName("encoded_polyline")
    private String encodedPolyline;

    @SerializedName("time_aware_polyline")
    private String timeAwarePolyline;

    @SerializedName("distance")
    private Integer distance;

    @SerializedName("vehicle_type")
    private HTUserVehicleType vehicleType;

    @SerializedName("tracking_url")
    private String trackingURL;

    @SerializedName("last_heartbeat")
    private Date lastHeartBeat;

    public HTTask() {
    }

    public HTTask(String taskID) {
        this.id = taskID;
    }

    public HTTask(String taskID, String status, String action, Date ETA, String trackingURL,
                  HTUserVehicleType vehicleType, String encodedPolyline, HTTaskDisplay taskDisplay) {
        this.id = taskID;
        this.status = status;
        this.action = action;
        this.ETA = ETA;
        this.trackingURL = trackingURL;
        this.vehicleType = vehicleType;
        this.encodedPolyline = encodedPolyline;
        this.taskDisplay = taskDisplay;
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public String getOrderID() {
        return orderID;
    }

    private void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public Boolean isUserLive() {
        return isUserLive;
    }

    public void setUserActive(boolean userActive) {
        isUserLive = userActive;
    }

    public String getStatus() {
        return status;
    }

    private void setStatus(String status) {
        this.status = status;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public HTTaskDisplay getTaskDisplay() {
        return taskDisplay;
    }

    public void setTaskDisplay(HTTaskDisplay taskDisplay) {
        this.taskDisplay = taskDisplay;
    }

    public String getAction() {
        return action;
    }

    private void setAction(String action) {
        this.action = action;
    }

    public Date getETA() {
        return ETA;
    }

    private void setETA(Date ETA) {
        this.ETA = ETA;
    }

    public Date getInitialETA() {
        return initialETA;
    }

    private void setInitialETA(Date initialETA) {
        this.initialETA = initialETA;
    }

    public Date getCommittedETA() {
        return committedETA;
    }

    private void setCommittedETA(Date committedETA) {
        this.committedETA = committedETA;
    }

    public Date getStartTime() {
        return startTime;
    }

    private void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getCompletionTime() {
        return completionTime;
    }

    private void setCompletionTime(Date completionTime) {
        this.completionTime = completionTime;
    }

    public Date getCancellationTime() {
        return cancellationTime;
    }

    public void setCancellationTime(Date cancellationTime) {
        this.cancellationTime = cancellationTime;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public HTUser getUser() {
        return user;
    }

    private void setUser(HTUser user) {
        this.user = user;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public GeoJSONLocation getStartLocation() {
        return startLocation;
    }

    private void setStartLocation(GeoJSONLocation startLocation) {
        this.startLocation = startLocation;
    }

    public Place getDestination() {
        return destination;
    }

    private void setDestination(Place destination) {
        this.destination = destination;
    }

    public String getCompletionAddress() {
        return completionAddress;
    }

    public void setCompletionAddress(String completionAddress) {
        this.completionAddress = completionAddress;
    }

    public GeoJSONLocation getCompletionLocation() {
        return completionLocation;
    }

    private void setCompletionLocation(GeoJSONLocation completionLocation) {
        this.completionLocation = completionLocation;
    }

    public String getEncodedPolyline() {
        return encodedPolyline;
    }

    private void setEncodedPolyline(String encodedPolyline) {
        this.encodedPolyline = encodedPolyline;
    }

    public String getTimeAwarePolyline() {
        return timeAwarePolyline;
    }

    public void setTimeAwarePolyline(String timeAwarePolyline) {
        this.timeAwarePolyline = timeAwarePolyline;
    }

    public Integer getDistance() {
        return distance;
    }

    private void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Place getHub() {
        return hub;
    }

    private void setHub(Place hub) {
        this.hub = hub;
    }

    public HTUserVehicleType getVehicleType() {
        return vehicleType;
    }

    private void setVehicleType(HTUserVehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getTrackingURL() {
        return trackingURL;
    }

    private void setTrackingURL(String trackingURL) {
        this.trackingURL = trackingURL;
    }

    public Date getLastHeartBeat() {
        return lastHeartBeat;
    }

    public void setLastHeartBeat(Date lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
    }

    @Override
    public String toString() {
        return "HTTask{" +
                "id='" + (id != null ? id : "") + '\'' +
                ", orderID='" + (orderID != null ? orderID : "") + '\'' +
                ", tripID='" + (tripID != null ? tripID : "") + '\'' +
                ", status='" + (status != null ? status : "") + '\'' +
                ", connectionStatus='" + (connectionStatus != null ? connectionStatus : "") + '\'' +
                ", display='" + (taskDisplay != null ? taskDisplay : "") + '\'' +
                ", action='" + (action != null ? action : "") + '\'' +
                ", ETA=" + (ETA != null ? ETA : "") +
                ", initialETA=" + (initialETA != null ? initialETA : "") +
                ", committedETA=" + (committedETA != null ? committedETA : "") +
                ", startTime=" + (startTime != null ? startTime : "") +
                ", completionTime=" + (completionTime != null ? completionTime : "") +
                ", userID='" + (userID != null ? userID : "") + '\'' +
                ", user=" + (user != null ? user : "") +
                ", startAddress=" + (startAddress != null ? startAddress : "") +
                ", startLocation=" + (startLocation != null ? startLocation : "") +
                ", destination=" + (destination != null ? destination : "") +
                ", hub=" + (hub != null ? hub : "") +
                ", completionAddress=" + (completionAddress != null ? completionAddress : "") +
                ", completionLocation=" + (completionLocation != null ? completionLocation : "") +
                ", encodedPolyline='" + (encodedPolyline != null ? encodedPolyline : "") + '\'' +
                ", distance=" + (distance != null ? distance : "") +
                ", vehicleType=" + (vehicleType != null ? vehicleType : "") +
                ", trackingURL='" + (trackingURL != null ? trackingURL : "") + '\'' +
                ", lastHeartBeat=" + (lastHeartBeat != null ? lastHeartBeat : "") +
                '}';
    }

    public boolean notStarted() {
        return (TASK_STATUS_NOT_STARTED.equalsIgnoreCase(this.status));
    }

    public boolean isInTransit() {
        // Return True if the task has either NOT_STARTED yet, has been COMPLETED or CANCELED
        return !(TASK_STATUS_NOT_STARTED.equalsIgnoreCase(this.status)
                || TASK_STATUS_COMPLETED.equalsIgnoreCase(this.status)
                || TASK_STATUS_CANCELED.equalsIgnoreCase(this.status));
    }

    public boolean isCompleted() {
        return (TASK_STATUS_COMPLETED.equalsIgnoreCase(this.status));
    }

    public boolean isSuspended() {
        return TASK_STATUS_SUSPENDED.equalsIgnoreCase(this.status);
    }

    public boolean isCanceled() {
        return TASK_STATUS_CANCELED.equalsIgnoreCase(this.status);
    }

    public boolean isAborted() {
        return TASK_STATUS_ABORTED.equalsIgnoreCase(this.status);
    }

    public boolean hasTaskFinished() {
        return (TASK_STATUS_COMPLETED.equalsIgnoreCase(this.status)
                || TASK_STATUS_CANCELED.equalsIgnoreCase(this.status)
                || TASK_STATUS_ABORTED.equalsIgnoreCase(this.status)
                || TASK_STATUS_SUSPENDED.equalsIgnoreCase(this.status));
    }

    public String getTaskDateDisplayString() {
        if (this.completionTime == null && this.startTime == null) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        if (this.completionTime != null)
            return dateFormat.format(this.completionTime);
        else if (this.startTime != null)
            return dateFormat.format(this.startTime);

        return null;
    }

    public String getTaskStartTimeDisplayString() {
        if (this.startTime == null) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        return dateFormat.format(this.startTime);
    }

    public String getTaskEndTimeDisplayString() {
        if (this.completionTime == null) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        return dateFormat.format(this.completionTime);
    }

    public Integer getDurationInMinutes() {
        if (this.startTime == null || this.completionTime == null) {
            return null;
        }

        long duration = this.completionTime.getTime() - this.startTime.getTime();
        double durationInMinutes = Math.ceil((duration / (float) 1000) / (float) 60);
        return (int) durationInMinutes;
    }

    public Double getDistanceInKMS() {
        if (this.distance == null) {
            return null;
        }

        return this.distance / 1000.0;
    }
}
