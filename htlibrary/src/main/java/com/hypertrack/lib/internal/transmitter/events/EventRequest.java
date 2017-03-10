package com.hypertrack.lib.internal.transmitter.events;

import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;

import java.util.List;

/**
 * Created by piyush on 20/02/17.
 */
public class EventRequest {
    private List<HyperTrackEvent> eventList;
    private Boolean completed;

    public List<HyperTrackEvent> getEventList() {
        return eventList;
    }

    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public EventRequest(List<HyperTrackEvent> eventList) {
        this.eventList = eventList;
    }
}
