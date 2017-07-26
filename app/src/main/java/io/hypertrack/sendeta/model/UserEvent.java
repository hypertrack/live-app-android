package io.hypertrack.sendeta.model;


import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;

/**
 * Created by Aman Jain on 25/05/17.
 */

public class UserEvent extends HyperTrackEvent {

    @SerializedName("id")
    private String id;

    @SerializedName("has_been_delivered")
    private boolean hasBeenDelivered;

    @SerializedName("delivered_at")
    private String deliveredAt;

    public UserEvent(String userID, String eventType) {
        super(userID, eventType);
    }

    public boolean isHasBeenDelivered() {
        return hasBeenDelivered;
    }

    public void setHasBeenDelivered(boolean hasBeenDelivered) {
        this.hasBeenDelivered = hasBeenDelivered;
    }

    public String getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(String deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "hasBeenDelivered=" + hasBeenDelivered +
                ", deliveredAt='" + deliveredAt + '\'' +
                "} " + super.toString();
    }
}
