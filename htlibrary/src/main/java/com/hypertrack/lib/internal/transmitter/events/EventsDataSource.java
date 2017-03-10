package com.hypertrack.lib.internal.transmitter.events;

import android.support.annotation.NonNull;

import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;

import java.util.List;

/**
 * Created by piyush on 18/02/17.
 */
public interface EventsDataSource {
    long getCount(String userID);
    void addEvent(HyperTrackEvent event);
    void addEvents(List<HyperTrackEvent> eventList);
    String getEventLastRecordedAt(String userID);
    List<HyperTrackEvent> getEventsForUserID(String userID);
    List<HyperTrackEvent> getEventsForUserIDBeforeTimestamp(String userID, @NonNull String timestamp);
    void deleteEvents(List<HyperTrackEvent> eventList);
    void deleteAllEvents();
}
