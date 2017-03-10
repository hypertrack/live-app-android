package com.hypertrack.lib.internal.transmitter.events;

import android.content.Context;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.callbacks.HyperTrackEventCallback;
import com.hypertrack.lib.callbacks.SuccessErrorCallback;
import com.hypertrack.lib.internal.common.logging.DeviceLogsManager;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.network.HTGson;
import com.hypertrack.lib.internal.common.network.HTNetworkResponse;
import com.hypertrack.lib.internal.common.network.HyperTrackNetworkRequest.HTNetworkClient;
import com.hypertrack.lib.internal.common.network.HyperTrackPostRequest;
import com.hypertrack.lib.internal.common.network.NetworkErrorUtil;
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.util.ListUtility;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.transmitter.devicehealth.DeviceHealth;
import com.hypertrack.lib.internal.transmitter.models.EventData;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent.EventType;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;
import com.hypertrack.lib.internal.transmitter.utils.BroadcastManager;
import com.hypertrack.lib.models.ErrorResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piyush on 20/02/17.
 */
public class EventsManager {
    private static final String TAG = EventsManager.class.getSimpleName();
    private static final int EVENTS_BATCH_SIZE = 50;
    private static EventsManager eventsManager;
    private Context mContext;
    private UserPreferences userPreferences;
    private EventsDataSource dataSource;
    private NetworkManager networkManager;
    private DeviceLogsManager logsManager;
    private BroadcastManager broadcastManager;
    private HyperTrackEventCallback eventCallback;

    private EventsManager(Context context, EventsDataSource dataSource, UserPreferences userPreferences,
                          NetworkManager networkManager, DeviceLogsManager logsManager,
                          BroadcastManager broadcastManager, HyperTrackEventCallback eventCallback) {
        this.mContext = context;
        this.userPreferences = userPreferences;
        this.dataSource = dataSource;
        this.networkManager = networkManager;
        this.logsManager = logsManager;
        this.broadcastManager = broadcastManager;
        this.eventCallback = eventCallback;
    }

    public static EventsManager getInstance(Context context, EventsDataSource dataSource,
                                            UserPreferences userPreferences, NetworkManager networkManager,
                                            DeviceLogsManager logsManager, BroadcastManager broadcastManager,
                                            HyperTrackEventCallback eventCallback) {
        if (eventsManager == null) {
            synchronized (EventsManager.class) {
                if (eventsManager == null) {
                    eventsManager = new EventsManager(context, dataSource, userPreferences,
                            networkManager, logsManager, broadcastManager, eventCallback);
                }
            }
        }

        return eventsManager;
    }

    public void setEventCallback(HyperTrackEventCallback eventCallback) {
        this.eventCallback = eventCallback;
    }

    public void logTrackingStartedEvent() {
        try {
            HyperTrackEvent trackingStartedEvent = new HyperTrackEvent(userPreferences.getUserId(),
                    EventType.TRACKING_STARTED_EVENT);
            dataSource.addEvent(trackingStartedEvent);

            // Flush events to server
            this.flushEvents();
            logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);

            // Send Tracking.started event
            broadcastManager.userTrackingStartedBroadcast(userPreferences.getUserId());
            if (eventCallback != null)
                eventCallback.onEvent(trackingStartedEvent);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while logTrackingStartedEvent: " + e);
        }
    }

    public void logLocationChangedEvent(HyperTrackLocation location) {
        if (location == null)
            return;

        HyperTrackEvent locationChangedEvent = new HyperTrackEvent(userPreferences.getUserId(),
                EventType.LOCATION_CHANGED_EVENT, location);
        dataSource.addEvent(locationChangedEvent);

        // Send Location.changed event
        broadcastManager.userCurrentLocationBroadcast(location, userPreferences.getUserId());
        if (eventCallback != null)
            eventCallback.onEvent(locationChangedEvent);
    }

    public void logStopStartedEvent(String stopID, HyperTrackStop hyperTrackStop) {
        try {
            HTLog.i(TAG, "Stop Start detected for Stop ID: " + stopID);
            EventData.Stop stopStartedData = new EventData.Stop(stopID);
            HyperTrackEvent stopStartedEvent = new HyperTrackEvent(userPreferences.getUserId(), EventType.STOP_STARTED_EVENT,
                    hyperTrackStop.getLocation(), hyperTrackStop.getRecordedAt(), stopStartedData);
            dataSource.addEvent(stopStartedEvent);

            // Flush events to server
            this.flushEvents();
            logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);

            // Send Stop.started event
            broadcastManager.userStopStartedBroadcast(userPreferences.getUserId(), hyperTrackStop);
            if (eventCallback != null)
                eventCallback.onEvent(stopStartedEvent);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while logStopStartedEvent: " + e);
        }
    }

    public void logStopEndedEvent(String stopID, HyperTrackStop stop, HyperTrackLocation location) {
        try {
            HTLog.i(TAG, "Stop End detected for Stop ID: " + stopID);
            EventData.Stop stopEndedData = new EventData.Stop(stopID);
            HyperTrackEvent stopEndedEvent = new HyperTrackEvent(userPreferences.getUserId(), EventType.STOP_ENDED_EVENT,
                    location, stopEndedData);
            dataSource.addEvent(stopEndedEvent);

            // Flush events to server
            this.flushEvents();
            logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);

            // Send Stop.ended event
            broadcastManager.userStopEndedBroadcast(userPreferences.getUserId(), stop);
            if (eventCallback != null)
                eventCallback.onEvent(stopEndedEvent);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while logStopStartedEvent: " + e);
        }
    }

    public void logActionCompletedEvent(String actionId, HyperTrackLocation location) {
        try {
            HTLog.i(TAG, "Action Completed for Action ID: " + (TextUtils.isEmpty(actionId) ? "null" : actionId));
            EventData.Action actionCompletedData = null;

            // Check if actionId was provided for completeAction call
            if (TextUtils.isEmpty(actionId)) {
                actionCompletedData = new EventData.Action(actionId);
            }

            HyperTrackEvent actionCompletedEvent = new HyperTrackEvent(userPreferences.getUserId(), EventType.ACTION_COMPLETED_EVENT,
                    location, actionCompletedData);
            dataSource.addEvent(actionCompletedEvent);

            // Flush events to server
            this.flushEvents();
            logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);

            // Send action.completed event
            broadcastManager.userActionCompletedBroadcast(userPreferences.getUserId(), actionId);
            if (eventCallback != null)
                eventCallback.onEvent(actionCompletedEvent);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while logActionCompletedEvent: " + e);
        }
    }

    public void logActivityChangedEvent(HyperTrackLocation location) {
        HyperTrackEvent activityChangedEvent = new HyperTrackEvent(userPreferences.getUserId(), EventType.ACTIVITY_CHANGED_EVENT,
                location);
        dataSource.addEvent(activityChangedEvent);

        // Send activity.changed event
        if (eventCallback != null)
            eventCallback.onEvent(activityChangedEvent);
    }

    public void logHealthChangedEvent(DeviceHealth health) {
        if (health == null)
            return;

        if (health.getBatteryHealth() != null)
            dataSource.addEvent(new HyperTrackEvent(userPreferences.getUserId(), EventType.BATTERY_HEALTH_CHANGED_EVENT,
                    health.getBatteryHealth()));

        if (health.getRadioHealth() != null)
            dataSource.addEvent(new HyperTrackEvent(userPreferences.getUserId(), EventType.RADIO_HEALTH_CHANGED_EVENT,
                    health.getRadioHealth()));

        if (health.getLocationHealth() != null)
            dataSource.addEvent(new HyperTrackEvent(userPreferences.getUserId(), EventType.LOCATION_HEALTH_CHANGED_EVENT,
                    health.getLocationHealth()));

        if (health.getDeviceModelHealth() != null)
            dataSource.addEvent(new HyperTrackEvent(userPreferences.getUserId(), EventType.DEVICE_MODEL_HEALTH_CHANGED_EVENT,
                    health.getDeviceModelHealth()));
    }

    public void logTrackingEndedEvent() {
        try {
            HyperTrackLocation location = userPreferences.getLastRecordedLocation();
            HyperTrackEvent trackingEndedEvent = new HyperTrackEvent(userPreferences.getUserId(), EventType.TRACKING_STOPPED_EVENT,
                    location);
            dataSource.addEvent(trackingEndedEvent);

            // Flush events to server
            this.flushEvents();
            logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);

            // Send tracking.ended event
            broadcastManager.userTrackingEndedBroadcast(userPreferences.getUserId());
            if (eventCallback != null)
                eventCallback.onEvent(trackingEndedEvent);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while logStopStartedEvent: " + e);
        }
    }

    public boolean hasPendingEvents() {
        return dataSource.getCount(userPreferences.getUserId()) > 0L;
    }

    public void flushEvents() {
        String lastEventTimestamp = eventsManager.getLastEventRecordedAt();
        if (!TextUtils.isEmpty(lastEventTimestamp)) {
            eventsManager.flushEvents(lastEventTimestamp, new SuccessErrorCallback() {
                @Override
                public void onSuccess() {
                    HTLog.i(TAG, "Events data flushed successfully");
                }

                @Override
                public void onError(Exception exception) {
                    // do nothing
                }
            });
        }
    }

    public void postEvents(String timestamp, final SuccessErrorCallback callback) {
        final List<HyperTrackEvent> eventList = getEventsFromDataSource(userPreferences.getUserId(), timestamp);
        if (eventList == null || eventList.isEmpty())
            return;

        final List<EventRequest> eventRequestList = getEventsRequestList(eventList);
        if (eventRequestList.isEmpty()) {
            if (callback != null) {
                callback.onSuccess();
            }
        }

        for (final EventRequest request : eventRequestList) {
            final List<HyperTrackEvent> requestEvents = request.getEventList();

            JSONArray jsonArray = this.getJSONArray(requestEvents);
            if (jsonArray == null) {
                request.setCompleted(false);
                if (callback != null && haveAllEventsBeenPosted(eventRequestList)) {
                    callback.onError(new RuntimeException("JsonArray for one of the requestLocations was null"));
                }
                return;
            }

            this.postEventsToServer(jsonArray, new SuccessErrorCallback() {
                @Override
                public void onSuccess() {
                    HTLog.i(TAG, "Pushed " + requestEvents.size() + " events");

                    // Clear Pushed Locations from LocationsDB
                    dataSource.deleteEvents(requestEvents);
                    request.setCompleted(true);
                    requestEvents.clear();

                    if (callback != null && haveAllEventsBeenPosted(eventRequestList)) {
                        if (haveAllLogsBeenPostedSuccessfully(eventRequestList)) {
                            callback.onSuccess();
                        } else {
                            callback.onError(new RuntimeException("All locations weren't posted successfully!"));
                        }
                    }
                }

                @Override
                public void onError(Exception error) {
                    request.setCompleted(false);
                    HTLog.e(TAG, "OnPostingEvents Failure. Count: " + requestEvents.size()
                            + " Exception: " + (error != null ? error : "null"));

                    if (callback != null && haveAllEventsBeenPosted(eventRequestList)) {
                        if (error == null) {
                            error = new RuntimeException("All locations weren't posted successfully!");
                        }
                        callback.onError(error);
                    }
                }
            });
        }
    }

    private String getLastEventRecordedAt() {
        return dataSource.getCount(userPreferences.getUserId()) == 0 ?
                null : dataSource.getEventLastRecordedAt(userPreferences.getUserId());
    }

    private List<HyperTrackEvent> getEventsFromDataSource(String userID, String timestamp) {
        if (TextUtils.isEmpty(timestamp)) {
            return dataSource.getEventsForUserID(userID);
        }

        return dataSource.getEventsForUserIDBeforeTimestamp(userID, timestamp);
    }

    private void flushEvents(final String timestamp, final SuccessErrorCallback callback) {
        // Check if a valid timestamp param has been passed to flush events
        if (TextUtils.isEmpty(timestamp)) {
            if (callback != null)
                callback.onError(new RuntimeException("RecordedAt param is invalid"));
            return;
        }

        // Check if EventsDataSource has any cached data to be flushed
        if (!hasPendingEvents()) {
            if (callback != null)
                callback.onSuccess();
            return;
        }

        // Post cached events recursively
        this.postEvents(timestamp, new SuccessErrorCallback() {
            @Override
            public void onSuccess() {
                // Call flushEvents again to flush any more pending data
                flushEvents(timestamp, callback);
            }

            @Override
            public void onError(Exception exception) {
                // Error happened while flushEvents, return error callback
                if (callback != null)
                    callback.onError(exception);
            }
        });
    }

    private void postEventsToServer(JSONArray jsonArray, final SuccessErrorCallback callback) {
        // Get Topic for postEvents executeMqttPOST call
        String eventAPIURL = BuildConfig.CORE_API_BASE_URL + "sdk_events/bulk/";

        HyperTrackPostRequest<JSONArray> postNetworkRequest = new HyperTrackPostRequest<>(TAG, mContext,
                eventAPIURL, HTNetworkClient.HT_NETWORK_CLIENT_HTTP, jsonArray, JSONArray.class,
                new HTNetworkResponse.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        if (callback != null)
                            callback.onSuccess();
                    }
                },
                new HTNetworkResponse.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error, Exception exception) {
                        // Check for Invalid Token errors
                        if (eventCallback != null && NetworkErrorUtil.isInvalidTokenError(error)) {
                            eventCallback.onError(new ErrorResponse(error));
                        }

                        if (NetworkErrorUtil.isInvalidRequest(error)) {
                            HTLog.e(TAG, "Error occurred while postEventsToServer: " + error.networkResponse.statusCode);
                            if (callback != null)
                                callback.onSuccess();

                        } else {
                            if (callback != null)
                                callback.onError(exception);
                        }
                    }
                });

        networkManager.execute(mContext, postNetworkRequest);
    }

    private JSONArray getJSONArray(List<HyperTrackEvent> events) {
        if (events == null)
            return null;

        Gson gson = HTGson.gson();
        String jsonString = gson.toJson(events);

        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            HTLog.e(TAG, "Exception while creating JSONObject");
        }

        return jsonArray;
    }

    private List<EventRequest> getEventsRequestList(List<HyperTrackEvent> events) {
        List<List<HyperTrackEvent>> eventBatchList = ListUtility.partition(events, EVENTS_BATCH_SIZE);

        List<EventRequest> requests = new ArrayList<>();
        for (List<HyperTrackEvent> eventList : eventBatchList) {
            requests.add(new EventRequest(eventList));
        }

        return requests;
    }

    private boolean haveAllEventsBeenPosted(List<EventRequest> eventRequests) {
        boolean hasCompleted = true;

        for (EventRequest request : eventRequests) {
            if (request.isCompleted() == null) {
                hasCompleted = false;
                break;
            }
        }

        return hasCompleted;
    }

    private boolean haveAllLogsBeenPostedSuccessfully(List<EventRequest> eventRequests) {
        boolean hasCompleted = true;

        for (EventRequest request : eventRequests) {
            if (request.isCompleted() == null || !request.isCompleted()) {
                hasCompleted = false;
                break;
            }
        }

        return hasCompleted;
    }
}
