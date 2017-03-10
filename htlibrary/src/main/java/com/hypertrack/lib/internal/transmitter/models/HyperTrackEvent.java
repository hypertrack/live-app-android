package com.hypertrack.lib.internal.transmitter.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.hypertrack.lib.internal.common.util.DateTimeUtility;

/**
 * Created by piyush on 17/02/17.
 */
public class HyperTrackEvent {

    @Expose(serialize = false, deserialize = false)
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private String userID;

    @SerializedName("recorded_at")
    private String recordedAt;

    @SerializedName("type")
    private String eventType;

    @SerializedName("location")
    private HyperTrackLocation location;

    @SerializedName("data")
    private Object data;

    public abstract class EventType {
        public static final String TRACKING_STARTED_EVENT = "tracking.started";
        public static final String TRACKING_STOPPED_EVENT = "tracking.ended";
        public static final String LOCATION_CHANGED_EVENT = "location.changed";
        public static final String ACTIVITY_CHANGED_EVENT = "activity.changed";
        public static final String ACTION_COMPLETED_EVENT = "action.completed";
        public static final String STOP_STARTED_EVENT = "stop.started";
        public static final String STOP_ENDED_EVENT = "stop.ended";

        public static final String LOCATION_HEALTH_CHANGED_EVENT = "device.location_config.changed";
        public static final String BATTERY_HEALTH_CHANGED_EVENT = "device.power.changed";
        public static final String RADIO_HEALTH_CHANGED_EVENT = "device.radio.changed";
        public static final String DEVICE_MODEL_HEALTH_CHANGED_EVENT = "device.info.changed";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public String getEventType() {
        return eventType;
    }

    public HyperTrackLocation getLocation() {
        return location;
    }

    public Object getData() {
        return data;
    }

    public HyperTrackEvent(String userID, String eventType) {
        this.userID = userID;
        this.eventType = eventType;
        this.recordedAt = DateTimeUtility.getCurrentTime();
    }

    public HyperTrackEvent(String userID, String eventType, HyperTrackLocation location) {
        this(userID, eventType);
        this.location = location;
    }

    public HyperTrackEvent(String userID, String eventType, HyperTrackLocation location, String recordedAt) {
        this(userID, eventType, location);
        this.recordedAt = recordedAt;
    }

    public HyperTrackEvent(String userID, String eventType, HyperTrackLocation location, Object data) {
        this(userID, eventType, location);
        this.data = data;
    }

    public HyperTrackEvent(String userID, String eventType, HyperTrackLocation location, String recordedAt, Object data) {
        this(userID, eventType, location, recordedAt);
        this.data = data;
    }

    public HyperTrackEvent(String userID, String eventType, Object data) {
        this(userID, eventType);
        this.data = data;
    }
}
