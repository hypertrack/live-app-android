package com.hypertrack.lib.internal.transmitter.models;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by piyush on 21/02/17.
 */
public class HyperTrackStop implements Serializable {

    private static final int LOITERING_DELAY_MS = 30000;
    private static final int NOTIFICATION_RESPONSIVENESS_MS = 5000;
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;

    private String id;
    private HyperTrackLocation location;
    private String recordedAt;
    private float radius = GEOFENCE_RADIUS_IN_METERS;
    private int transitionType = Geofence.GEOFENCE_TRANSITION_EXIT;
    private int loiteringDelay = LOITERING_DELAY_MS;
    private int notificationResponsiveness = NOTIFICATION_RESPONSIVENESS_MS;
    private long expirationDuration = Geofence.NEVER_EXPIRE;
    private int initialTrigger = GeofencingRequest.INITIAL_TRIGGER_EXIT;
    private boolean isAdded = false;
    private boolean isStopStarted = false;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HyperTrackLocation getLocation() {
        return location;
    }

    public void setLocation(HyperTrackLocation location) {
        this.location = location;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(int transitionType) {
        this.transitionType = transitionType;
    }

    public int getLoiteringDelay() {
        return loiteringDelay;
    }

    public void setLoiteringDelay(int loiteringDelay) {
        this.loiteringDelay = loiteringDelay;
    }

    public int getNotificationResponsiveness() {
        return notificationResponsiveness;
    }

    public void setNotificationResponsiveness(int notificationResponsiveness) {
        this.notificationResponsiveness = notificationResponsiveness;
    }

    public long getExpirationDuration() {
        return expirationDuration;
    }

    public void setExpirationDuration(long expirationDuration) {
        this.expirationDuration = expirationDuration;
    }

    public int getInitialTrigger() {
        return initialTrigger;
    }

    public void setInitialTrigger(int initialTrigger) {
        this.initialTrigger = initialTrigger;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public boolean isStopStarted() {
        return isStopStarted;
    }

    public void setStopStarted(boolean stopStarted) {
        this.isStopStarted = stopStarted;
    }

    public HyperTrackStop updateLocation(HyperTrackLocation location) {
        this.location = location;
        this.recordedAt = DateTimeUtility.getCurrentTime();
        return this;
    }

    public HyperTrackStop updateStartTimeoutExpired() {
        this.isStopStarted = true;
        return this;
    }

    public HyperTrackStop() {
        this.id = UUID.randomUUID().toString();
    }

    public HyperTrackStop(HyperTrackLocation location) {
        this();
        this.location = location;
        this.recordedAt = DateTimeUtility.getCurrentTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HyperTrackStop hyperTrackStop = (HyperTrackStop) o;

        if (Float.compare(hyperTrackStop.radius, radius) != 0) return false;
        if (transitionType != hyperTrackStop.transitionType) return false;
        if (loiteringDelay != hyperTrackStop.loiteringDelay) return false;
        if (notificationResponsiveness != hyperTrackStop.notificationResponsiveness) return false;
        if (expirationDuration != hyperTrackStop.expirationDuration) return false;
        if (initialTrigger != hyperTrackStop.initialTrigger) return false;
        if (!id.equals(hyperTrackStop.id)) return false;
        if (!location.equals(hyperTrackStop.location)) return false;
        return recordedAt.equals(hyperTrackStop.recordedAt);
    }

    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + (radius != +0.0f ? Float.floatToIntBits(radius) : 0);
        result = 31 * result + transitionType;
        result = 31 * result + loiteringDelay;
        result = 31 * result + notificationResponsiveness;
        result = 31 * result + (int) (expirationDuration ^ (expirationDuration >>> 32));
        result = 31 * result + initialTrigger;
        return result;
    }

    @Override
    public String toString() {
        return "HyperTrackStop{" +
                "id='" + id + '\'' +
                ", location=" + location +
                ", recordedAt='" + recordedAt + '\'' +
                ", radius=" + radius +
                ", transitionType=" + transitionType +
                ", loiteringDelay=" + loiteringDelay +
                ", notificationResponsiveness=" + notificationResponsiveness +
                ", expirationDuration=" + expirationDuration +
                ", initialTrigger=" + initialTrigger +
                ", isAdded=" + isAdded +
                ", isStopStarted=" + isStopStarted +
                '}';
    }
}
