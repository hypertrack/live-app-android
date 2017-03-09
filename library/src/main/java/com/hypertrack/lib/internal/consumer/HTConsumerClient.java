package com.hypertrack.lib.internal.consumer;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.R;
import com.hypertrack.lib.internal.common.logging.DeviceLogsManager;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.ExpandedLocation;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.models.ActionFetchCallback;
import com.hypertrack.lib.internal.consumer.models.ActionListCallBack;
import com.hypertrack.lib.internal.consumer.models.ActionStore;
import com.hypertrack.lib.internal.consumer.models.HTAction;
import com.hypertrack.lib.internal.consumer.models.HTActionCallBack;
import com.hypertrack.lib.internal.consumer.models.HTDisplay;
import com.hypertrack.lib.internal.consumer.models.HTPlace;
import com.hypertrack.lib.internal.consumer.models.HTUser;
import com.hypertrack.lib.internal.consumer.models.UpdateDestinationCallback;
import com.hypertrack.lib.internal.consumer.utils.HTActionUtils;
import com.hypertrack.lib.internal.consumer.utils.LocationUtils;
import com.hypertrack.lib.internal.consumer.view.HyperTrackMapFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * This class can be used to add one or more action to be tracked on {@link HyperTrackMapFragment}
 * by calling the method {@link #trackAction(String, Activity, HTActionCallBack)} with a actionID or a
 * list of actionIDs.
 * <p>
 * Other APIs here provide access to data for the actions which are being currently tracked.
 */

public class HTConsumerClient implements ActionFetchCallback, Application.ActivityLifecycleCallbacks {

    public static final String TAG = HTConsumerClient.class.getSimpleName();

    public static final String ACTION_ID_REQUIRED_ERROR_MESSAGE = "Required Parameter: actionID is required to start tracking.";
    public static final String ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE = "Required Parameter: activity instance is required to start tracking.";
    public static final String CALLBACK_REQUIRED_ERROR_MESSAGE = "Required Parameter: callback instance is required to start tracking.";

    public static final String INTENT_EXTRA_ORDER_STATUS = "ACTION_STATUS";
    public static final String INTENT_EXTRA_ACTION_ID_LIST = "ACTION_ID_LIST";

    public static final String ACTION_STATUS_CHANGED_NOTIFICATION = "com.hypertrack.consumer:HTStatusChangedNotification";
    public static final String ACTION_DETAIL_REFRESHED_NOTIFICATION = "com.hypertrack.consumer:HTActionDetailsRefreshedNotification";
    public static final String ACTION_REMOVED_FROM_TRACKING_NOTIFICATION = "com.hypertrack.consumer:HTActionRemovedFromTrackingNotification";

    public static final String ACTION_SUB_STATUS_TYPE_DELAYED = "delayed";
    public static final int POST_DEVICE_LOGS_JOB = 11;
    private static final int POST_DEVICE_LOG_INTERVAL = 30000;
    private static final String POST_DEVICE_LOG_TAG = "com.hypertrack.consumer:PostDeviceLog";
    protected DeviceLogsManager logsManager;
    protected NetworkManager networkManager;
    private Context mContext;
    private ActionStore actionStore;
    private SmartScheduler scheduler;
    BroadcastReceiver mPowerSaverModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update PostDeviceLogs Job on PowerSaverMode change
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (scheduler != null) {
                    scheduler.onPowerSaverModeChanged(pm.isPowerSaveMode());
                }
            }
        }
    };
    private HashMap<String, String> currentActionStatusList;
    private boolean powerSaverModeRegistered = false;

    public HTConsumerClient(Context context, SmartScheduler scheduler, DeviceLogsManager logsManager,
                            NetworkManager networkManager) {
        this.mContext = context;
        this.scheduler = scheduler;
        this.logsManager = logsManager;
        this.networkManager = networkManager;

        // Instantiate ActionStore on HTConsumerClient instantiation
        actionStore = new ActionStore(mContext, this, this.scheduler, this.networkManager);
        currentActionStatusList = new HashMap<>();

        // Register for PowerSaverModeChange receiver
        this.registerPowerSaverModeReceiver(context.getApplicationContext(), true);

        // Set Application Callbacks
        setApplicationInstance(context);
    }

    /**
     * Method to check whether a List of Actions have same destination location or not
     *
     * @param HTActionList of HTAction objects for which the location has to be validated
     * @return Returns true if either there is only one action or a set of actions for which the Destination
     * Locations are 110m apart, false otherwise.
     */
    public static boolean checkIfActionsHaveSameDestination(List<HTAction> actionList) {
        if (actionList == null || actionList.isEmpty())
            return false;

        if (actionList.size() == 1)
            return true;

        HTPlace destination1 = actionList.get(0).getExpectedPlace();
        HTPlace destination2;

        if (destination1 == null)
            return false;

        for (int i = 1; i < actionList.size(); i++) {

            if (actionList.get(i) != null) {
                destination2 = actionList.get(i).getExpectedPlace();

                if (destination2 == null) {
                    return false;
                }

                if (!LocationUtils.areLocationsNearby(destination1.getLocation(), destination2.getLocation())) {
                    return false;
                }
            }
        }

        return true;
    }

    private void setApplicationInstance(Context context) {
        Application application = (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(this);
    }

    /**
     * Track an order by passing a actionID. Also, takes HTActionCallBack as second parameter.
     *
     * @param actionID
     * @param callback
     */
    public void trackAction(String actionID, Activity activity, final HTActionCallBack callback) {

        if (callback == null) {
            HTLog.e(TAG, "Error occurred while trackAction: " + CALLBACK_REQUIRED_ERROR_MESSAGE);
            throw new IllegalArgumentException(CALLBACK_REQUIRED_ERROR_MESSAGE);
        }

        if (activity == null || activity.getApplication() == null) {
            HTLog.e(TAG, "Error occurred while trackAction: " + ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE);
            callback.onError(new IllegalArgumentException(ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE));
            return;
        }

        if (TextUtils.isEmpty(actionID)) {
            HTLog.e(TAG, "Error occurred while trackAction: " + ACTION_ID_REQUIRED_ERROR_MESSAGE);
            callback.onError(new IllegalArgumentException(ACTION_ID_REQUIRED_ERROR_MESSAGE));
            return;
        }

        actionStore.addAction(actionID, new HTActionCallBack() {
            @Override
            public void onSuccess(HTAction action) {
                currentActionStatusList.put(action.getId(), action.getStatus());
                callback.onSuccess(action);
            }

            @Override
            public void onError(Exception exception) {
                callback.onError(exception);
            }
        });

        activity.getApplication().registerActivityLifecycleCallbacks(this);
    }

    /**
     * Track an order by passing a List of actionIDs. Also, takes HTActionCallBack as second parameter.
     *
     * @param actionIDList
     * @param callback
     */
    public void trackAction(final List<String> actionIDList, final Activity activity, final ActionListCallBack callback) {
        if (callback == null) {
            HTLog.e(TAG, "Error occurred while trackAction: " + CALLBACK_REQUIRED_ERROR_MESSAGE);
            throw new IllegalArgumentException(CALLBACK_REQUIRED_ERROR_MESSAGE);
        }

        if (activity == null || activity.getApplication() == null) {
            HTLog.e(TAG, "Error occurred while trackAction: " + ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE);
            callback.onError(new IllegalArgumentException(ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE));
            return;
        }

        if (actionIDList == null || actionIDList.isEmpty() || actionIDList.contains("")) {
            HTLog.e(TAG, "Error occurred while trackAction: " + ACTION_ID_REQUIRED_ERROR_MESSAGE);
            callback.onError(new IllegalArgumentException(ACTION_ID_REQUIRED_ERROR_MESSAGE));
            return;
        }

        actionStore.addActionList(actionIDList, new ActionListCallBack() {
            @Override
            public void onSuccess(List<HTAction> actionList) {
                if (actionList != null) {
                    for (HTAction action : actionList) {
                        currentActionStatusList.put(action.getId(), action.getStatus());
                    }
                }

                callback.onSuccess(actionList);
            }

            @Override
            public void onError(Exception exception) {
                callback.onError(exception);
            }
        });

        activity.getApplication().registerActivityLifecycleCallbacks(this);
    }

    /**
     * Method to get a Action object for a given actionID
     *
     * @param actionID
     * @return
     */
    public HTAction actionForActionID(String actionID) {
        if (actionStore != null)
            return actionStore.getAction(actionID);

        return null;
    }

    /**
     * Method to get the task assigned to this action
     *
     * @param actionID
     * @return
     **/

    public int taskForActionID(String actionID) {
        if (actionStore != null && actionStore.getAction(actionID) != null
                && !TextUtils.isEmpty(actionStore.getAction(actionID).getAction())) {

            switch (actionStore.getAction(actionID).getAction().toLowerCase()) {
                case HTAction.ACTION_DELIVERY:
                    return com.hypertrack.lib.R.string.action_delivery;
                case HTAction.ACTION_PICKUP:
                    return com.hypertrack.lib.R.string.action_pickup;
                case HTAction.ACTION_VISIT:
                    return R.string.action_visit;
                case HTAction.ACTION_TASK:
                    return R.string.action_task;
                case HTAction.ACTION_DROPOFF:
                    return R.string.action_dropoff;
                case HTAction.ACTION_STOPOVER:
                    return R.string.action_stopover;
            }
        }

        return com.hypertrack.lib.R.string.action_delivery;
    }

    public ArrayList<String> getActionIDList() {
        if (actionStore != null) {
            return actionStore.getActionIDList();
        }

        return null;
    }

    /**
     * Method to get a List of currently Active ActionIDs
     *
     * @return
     */
    public List<String> getActiveActionIDList() {
        if (actionStore != null)
            return actionStore.getActiveActionIDList();

        return null;
    }

    /**
     * Helper to remove action ids list from ActionStore
     *
     * @param actionIDList
     * @return
     **/

    private List<HTAction> removeActionsFromActionStore(List<String> actionIDList) {
        if (actionIDList == null || actionIDList.size() == 0 || actionStore == null)
            return null;

        List<HTAction> removedActionList = new ArrayList<>();

        for (String actionID : actionIDList) {
            if (!TextUtils.isEmpty(actionID) && actionStore.getAction(actionID) != null) {
                currentActionStatusList.remove(actionID);
                removedActionList.add(actionStore.removeAction(actionID));
            }
        }

        if (actionStore.getActionIDList() == null || actionStore.getActionIDList().size() == 0) {
            actionStore.invalidateActionStoreJobs();
        }

        return removedActionList;
    }

    /**
     * Method to remove a actionID from ActionList
     *
     * @param actionIDToBeRemoved
     * @return
     */
    public HTAction removeActionID(String actionIDToBeRemoved) {
        List<String> actionIDListToBeRemoved = new ArrayList<>();
        actionIDListToBeRemoved.add(actionIDToBeRemoved);

        List<HTAction> removedActionList = removeActionsFromActionStore(actionIDListToBeRemoved);

        if (removedActionList != null && removedActionList.size() > 0 && removedActionList.get(0) != null) {
            ArrayList<String> removedActionIDList = new ArrayList<>();
            removedActionIDList.add(removedActionList.get(0).getId());

            // Send ActionRemoved Success broadcast in case of any Action being removed from tracking
            broadcastActionRemoved(removedActionIDList);

            return removedActionList.get(0);
        }

        // Send ActionRemoved broadcast with null Intent extra in case no action was removed from tracking
        broadcastActionRemoved(null);

        return null;
    }

    /**
     * Method to remove a List of ActionIDs from ActionList
     *
     * @param actionIDListToBeRemoved
     * @return
     */
    public List<HTAction> removeActionID(List<String> actionIDListToBeRemoved) {
        if (actionIDListToBeRemoved != null && actionIDListToBeRemoved.size() > 0) {

            List<HTAction> removedActionList = removeActionsFromActionStore(actionIDListToBeRemoved);

            ArrayList<String> removedActionIDList = null;

            if (removedActionList != null && removedActionList.size() > 0) {
                removedActionIDList = new ArrayList<>();

                for (int i = 0; i < removedActionList.size(); i++) {
                    if (removedActionList.get(i) != null) {
                        removedActionIDList.add(removedActionList.get(i).getId());
                    }
                }

                if (removedActionIDList.size() > 0) {
                    // Send ActionRemoved Success broadcast in case of any Action being removed from tracking
                    broadcastActionRemoved(removedActionIDList);
                }
            } else {
                // Send ActionRemoved broadcast with null Intent extra in case no action was removed from tracking
                broadcastActionRemoved(null);
            }

            return removedActionList;
        }

        // Send ActionRemoved broadcast with null Intent extra in case no action was removed from tracking
        broadcastActionRemoved(null);

        return null;
    }

    /**
     * Method to clear all actions from ActionList
     */
    public void clearActions() {
        if (actionStore != null && actionStore.getActionIDList() != null && actionStore.getActionIDList().size() > 0) {

            List<String> actionIDListToBeRemoved = actionStore.getActionIDList();
            removeActionID(actionIDListToBeRemoved);
        } else {
            // Send ActionRemoved broadcast with null Intent extra in case no action was removed from tracking
            broadcastActionRemoved(null);
        }
    }


    public void updateDestinationLocation(String actionID, GeoJSONLocation location, UpdateDestinationCallback callback) {
        // TODO check for update destination API 6/03/2017 
        if (actionStore != null && actionStore.getAction(actionID) != null) {
            actionStore.updateDestinationLocation(actionID, location, callback);
        } else {
            if (callback != null)
                callback.onError(new IllegalStateException("No action has been started with given actionID"));
        }
    }

    /**
     * Call this method to get DestinationLocation's LatLng
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's DestinationLocation LatLng if it is being tracked currently, null otherwise.
     */
    public LatLng getDestinationLocationLatLng(String actionID) {
        if (actionStore != null)
            return actionStore.getDestinationLocationLatLng(actionID);

        return null;
    }

    /**
     * Call this method to get CompletedLocation's LatLng
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's CompletedLocation LatLng if it is being tracked currently, null otherwise.
     */
    public LatLng getCompletedLocationLatLng(String actionID) {
        if (actionStore != null)
            return actionStore.getCompletedLocationLatLng(actionID);

        return null;
    }

    /*
      * Call this method to get SourceLocation's LatLng
      *
      * @param actionID Pass actionID of the action as parameter
      * @return Action's SourceLocation LatLng if it is being tracked currently, null otherwise.
      */
    public LatLng getStartedLocationLatLng(String actionID) {
        if (actionStore != null)
            return actionStore.getStartLocationLatLng(actionID);

        return null;
    }

    /**
     * Call this method to get SourceLocation's Address
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's SourceLocation Address if it is being tracked currently, null otherwise.
     */
    public String getStartPlaceAddress(String actionID) {
        if (actionStore != null)
            return actionStore.getStartPlaceAddress(actionID);

        return null;
    }

    /**
     * Call this method to get DestinationLocation's Address
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's DestinationLocation Address if it is being tracked currently, null otherwise.
     */
    public String getDestinationAddress(String actionID) {
        if (actionStore != null)
            return actionStore.getDestinationPlaceAddress(actionID);

        return null;
    }

    /**
     * Call this method to get Action CompletedLocation's Address
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's CompletedLocation Address if it is being tracked currently, null otherwise.
     */
    public String getCompletedAddress(String actionID) {
        if (actionStore != null)
            return actionStore.getCompletedPlaceAddress(actionID);

        return null;
    }

    /**
     * Call this method to get actions's current status.
     * <p>
     * Action's Status can be one of [HTAction#ACTION_STATUS_NOT_STARTED,
     * HTAction#ACTION_STATUS_DISPATCHING, HTAction#ACTION_STATUS_USER_ON_THE_WAY,
     * HTAction#ACTION_STATUS_USER_ARRIVING, HTAction#ACTION_STATUS_USER_ARRIVED,
     * HTAction#ACTION_STATUS_COMPLETED, HTAction#ACTION_STATUS_CANCELED,
     * HTAction#ACTION_STATUS_ABORTED, HTAction#ACTION_STATUS_SUSPENDED]
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's Status if it is being tracked currently, null otherwise.
     */
    public String getStatus(String actionID) {
        if (actionStore != null)
            return actionStore.getStatus(actionID);

        return null;
    }

   /* *//**
     * Call this method to get actions's current connection status
     * <p>
     * Action's Connection Status can be one of [HTAction#ACTION_STATUS_CONNECTION_HEALTHY,
     * HTAction#ACTION_STATUS_CONNECTION_LOST]
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's Connection status if it is being tracked currently, null otherwise.
     *//*
    public String getConnectionStatus(String actionID) {
        if (actionStore != null)
            return actionStore.getConnectionStatus(actionID);

        return null;
    }
*/

    /**
     * Call this method to get actions's display eta value (in minutes)
     * <p>
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Double value of ETA (in minutes) if a action is being tracked currently, null otherwise
     */
    public Integer getActionDisplayETA(String actionID) {
        if (actionStore != null && actionStore.getAction(actionID) != null) {
            return HTActionUtils.getActionDisplayETA(actionStore.getAction(actionID).getActionDisplay());
        }

        return null;
    }

   /* *//**
     * Call this method to get actions's current display status
     * <p>
     * Action's Display Status can be one of HTAction#ACTION_STATUS_NOT_STARTED,
     * HTAction#ACTION_STATUS_DISPATCHING, HTAction#ACTION_STATUS_USER_ON_THE_WAY,
     * HTAction#ACTION_STATUS_USER_ARRIVING, HTAction#ACTION_STATUS_USER_ARRIVED,
     * HTAction#ACTION_STATUS_COMPLETED, HTAction#ACTION_STATUS_CANCELED,
     * HTAction#ACTION_STATUS_ABORTED, HTAction#ACTION_STATUS_SUSPENDED,
     * HTAction#ACTION_STATUS_CONNECTION_LOST, HTAction#ACTION_STATUS_NO_LOCATION]
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's Display Status if it is being tracked currently, null otherwise.
     *//*
    public String getActionDisplayStatus(String actionID) {
        if (actionStore != null)
            return actionStore.getActionDisplayStatus(actionID);

        return null;
    }
*/

    /**
     * Call this method to get actions's current status text to be displayed
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's Display Status Text if it is being tracked currently, null otherwise.
     */
    public String getActionDisplayStatusText(String actionID) {
        if (actionStore != null && this.getActionDisplay(actionID) != null) {
            return this.getActionDisplay(actionID).getStatusText();
        }

        return null;
    }

    /**
     * Call this method to get action's current sub-status text to be displayed
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Action's Display Sub-Status Text if it is being tracked currently, null otherwise.
     */
    public String getActionDisplaySubStatusText(String actionID) {
        if (actionStore != null && this.getActionDisplay(actionID) != null) {
            return this.getActionDisplay(actionID).getSubStatusText();
        }

        return null;
    }

    /**
     * Call this method to get actions's current display object
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Display for given actionID if it is being tracked currently, null otherwise.
     */
    public HTDisplay getActionDisplay(String actionID) {
        if (actionStore != null && actionStore.getAction(actionID) != null)
            return actionStore.getAction(actionID).getActionDisplay();

        return null;
    }

   /* *//**
     * Call this method to get Actions's Status Text ResourceId.
     *
     * @param actionID Pass actionID of the action as parameter
     * @return ResourceId for Action's Status text.
     *//*
    public Integer getStatusTextResourceIdForToolbar(String actionID) {
        if (actionStore != null) {
            String actionDisplayStatus = mContext.getString(actionStore.getActionDisplayStatus(actionID));

            if (TextUtils.isEmpty(actionDisplayStatus))
                return null;

            switch (actionDisplayStatus) {
                case HTAction.ACTION_STATUS_NOT_STARTED:
                    return com.hypertrack.lib.R.string.action_status_not_started;
                case HTAction.ACTION_STATUS_DISPATCHING:
                    return com.hypertrack.lib.R.string.action_status_dispatching;
                case HTAction.ACTION_STATUS_USER_ON_THE_WAY:
                    return com.hypertrack.lib.R.string.action_status_user_on_the_way;
                case HTAction.ACTION_STATUS_USER_ARRIVING:
                    return com.hypertrack.lib.R.string.action_status_user_arriving;
                case HTAction.ACTION_STATUS_USER_ARRIVED:
                    return com.hypertrack.lib.R.string.action_status_user_arrived;
                case HTAction.ACTION_STATUS_COMPLETED:
                    return com.hypertrack.lib.R.string.action_status_completed;
                case HTAction.ACTION_STATUS_CANCELED:
                    return com.hypertrack.lib.R.string.action_status_canceled;
                case HTAction.ACTION_STATUS_ABORTED:
                    return com.hypertrack.lib.R.string.action_status_aborted;
                case HTAction.ACTION_STATUS_SUSPENDED:
                    return com.hypertrack.lib.R.string.action_status_suspended;
                case HTAction.ACTION_STATUS_NO_LOCATION:
                    return com.hypertrack.lib.R.string.action_status_no_location;
                case HTAction.ACTION_STATUS_LOCATION_LOST:
                    return com.hypertrack.lib.R.string.action_status_location_lost;
                case HTAction.ACTION_STATUS_CONNECTION_LOST:
                    return com.hypertrack.lib.R.string.action_status_connection_lost;
                default:
                    return null;
            }
        }

        return null;
    }*/


    /**
     * Call this method to get LastUpdated ETA for a action.
     * <p>
     * In case of Loss of Connectivity with the user, the ETA returned will be the
     * last updated value.
     *
     * @param actionID ActionID of the action for which lastUpdatedETA has to be fetched
     * @return Last updated ETA (in minutes)
     */
    public Integer getLastUpdatedETAInMinutes(String actionID) {
        return getActionDisplayETA(actionID);
    }

    /**
     * Call this method to get flag to show/hide ActionSummary for current ActionStatus
     *
     * @param actionID Pass actionID of the action as parameter
     * @return Boolean flag to show ActionSummary for specific ActionStatus of given actionID
     */

    public boolean showActionSummaryForActionStatus(String actionID) {

        if (actionStore != null) {
            HTDisplay actionDisplay = this.getActionDisplay(actionID);
            if (actionDisplay != null && actionDisplay.isShowSummary())
                return true;
        }

        return false;
    }


    /**
     * Call this method to get Current ETA for a action.
     *
     * @param actionID ActionID of the action for which ETA has to be fetched
     * @return Current ETA (in minutes)
     */
    public Integer getEstimatedTimeOfArrivalInMinutes(String actionID) {
        if (actionStore != null)
            return actionStore.getEstimatedTimeOfArrival(actionID);

        return null;
    }

    /**
     * Call this method to get Action's Duration (in minutes)
     *
     * @param actionID ActionID of the action for which the duration has to be computed
     * @return Action's Duration (in minutes)
     */
    public Integer getActionDuration(String actionID) {
        if (actionStore != null)
            return actionStore.getActionDurationInMinutes(actionID);

        return null;
    }

    /**
     * Call this method to get Action's Duration String
     *
     * @param actionID ActionID of the action for which the duration has to be computed
     * @return Action's Duration String
     */
    public String getActionDurationString(Context context, String actionID) {
        if (actionStore != null) {
            return HTActionUtils.getActionDurationString(context, actionStore.getAction(actionID));
        }

        return null;
    }

    /**
     * Call this method to get Action's Distance (in KMs)
     *
     * @param actionID ActionID of the action for which the distance has to be computed
     * @return Action's Distance (in KMs)
     */
    public Double getActionDistanceInKMs(String actionID) {
        if (actionStore != null) {
            return HTActionUtils.getActionDistanceInKMs(actionStore.getAction(actionID));
        }

        return null;
    }

    /**
     * Call this method to get Action's Distance String
     *
     * @param actionID ActionID of the action for which the distance has to be computed
     * @return Action's Distance String
     */
    public String getActionDistanceString(Context context, String actionID) {
        if (actionStore != null) {
            HTActionUtils.getActionDistanceString(context, actionStore.getAction(actionID));
        }

        return null;
    }

    /**
     * Call this method to get Action's Metering Data String
     *
     * @param actionID ActionID of the action for which the metering data has to be computed
     * @return Action's Metering Data String
     */
    public String getActionMeteringString(Context context, String actionID) {
        if (actionStore != null)
            return HTActionUtils.getActionMeteringString(context, actionStore.getAction(actionID));

        return null;
    }

    @Override
    public void onFetchAction(boolean result, ArrayList<String> actionIDList) {

        if (actionStore != null) {
            ArrayList<String> changedStatusActionList = new ArrayList<>();
            ArrayList<String> refreshedActionList = new ArrayList<>();

            // Check for all actionIDs existing in ActionStore
            for (String actionID : actionIDList) {

                if (actionStore.getAction(actionID) != null) {
                    String actionStatus = actionStore.getStatus(actionID);

                    // Check if the state was changed for current action
                    if (this.currentActionStatusList.get(actionID) != null
                            && !this.currentActionStatusList.get(actionID).equalsIgnoreCase(actionStatus)) {

                        // Update currentActionStatus & changedStatusActionList (indicating State Change for current actionID)
                        this.currentActionStatusList.put(actionID, actionStatus);
                        changedStatusActionList.add(actionID);
                    } else {
                        refreshedActionList.add(actionID);
                    }
                }
            }

            // Send ActionStatusChanged broadcast in case of State Change in any Action
            if (changedStatusActionList.size() > 0) {
                this.broadcastActionStatusChanged(changedStatusActionList);
            }
            // Send ActionStatusRefreshed broadcast in case of Action Data Refresh
            if (refreshedActionList.size() > 0) {
                this.broadcastActionRefreshed(refreshedActionList);
            }
        }
    }

    private void broadcastActionStatusChanged(ArrayList<String> changedStatusActionIDList) {
        Intent intent = new Intent(HTConsumerClient.ACTION_STATUS_CHANGED_NOTIFICATION);
        intent.putExtra(INTENT_EXTRA_ORDER_STATUS, this.currentActionStatusList);
        intent.putStringArrayListExtra(INTENT_EXTRA_ACTION_ID_LIST, changedStatusActionIDList);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void broadcastActionRefreshed(ArrayList<String> actionIDList) {
        Intent intent = new Intent(HTConsumerClient.ACTION_DETAIL_REFRESHED_NOTIFICATION);
        intent.putStringArrayListExtra(INTENT_EXTRA_ACTION_ID_LIST, actionIDList);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void broadcastActionRemoved(ArrayList<String> actionIDList) {
        Intent intent = new Intent(HTConsumerClient.ACTION_REMOVED_FROM_TRACKING_NOTIFICATION);
        intent.putStringArrayListExtra(INTENT_EXTRA_ACTION_ID_LIST, actionIDList);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * @return
     */
    public HTUser getUser(String actionID) {
        if (actionStore != null)
            return actionStore.getUser(actionID);

        return null;
    }

    public ExpandedLocation getUserLastKnownLocation(String actionID) {
        if (actionStore != null && actionStore.getUser(actionID) != null) {
            return actionStore.getUser(actionID).getLastLocation();
        }

        return null;
    }

    public LatLng getUserLastKnownLocationCoordinates(String actionID) {
        if (this.getUserLastKnownLocation(actionID) != null) {
            GeoJSONLocation location = this.getUserLastKnownLocation(actionID).getGeoJSONLocation();
            if (location != null)
                return new LatLng(location.getCoordinates()[1], location.getCoordinates()[0]);
        }

        return null;
    }

    public Float getUserLastKnownLocationBearing(String actionID) {
        if (this.getUserLastKnownLocation(actionID) != null) {
            Float bearing = this.getUserLastKnownLocation(actionID).getBearing();
            return bearing;
        }

        return null;
    }


    public boolean anyActionInStartedState() {
        if (actionStore != null) {
            return actionStore.anyActionInStartedState();
        }

        return false;
    }

    public boolean isActionCompleted(String actionID) {
        if (actionStore != null) {
            return actionStore.isActionCompleted(actionID);
        }

        return false;
    }

    public boolean isActionFinished(String actionID) {
        if (actionStore != null) {
            return actionStore.isActionFinished(actionID);
        }

        return false;
    }

    public void invalidateAllTimers() {
        if (actionStore != null) {
            actionStore.invalidateActionStoreJobs();
        }
    }

    protected DeviceLogsManager getLogsManager() {
        return logsManager;
    }

    private void registerPowerSaverModeReceiver(Context context, boolean register) {
        try {
            if (register && !powerSaverModeRegistered) {
                powerSaverModeRegistered = true;
                context.registerReceiver(mPowerSaverModeChangedReceiver,
                        new IntentFilter("android.os.action.POWER_SAVE_MODE_CHANGED"));
            } else if (powerSaverModeRegistered) {
                powerSaverModeRegistered = false;
                context.unregisterReceiver(mPowerSaverModeChangedReceiver);
            }
        } catch (IllegalArgumentException e) {
            HTLog.e(TAG, "Exception occurred while registerPowerSaverModeReceiver(" + register + "): " + e);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        HTLog.v(TAG, "Inside onActivityResumed");

        if (actionStore != null && actionStore.getActiveActionIDList() != null
                && actionStore.getActiveActionIDList().size() > 0) {
            actionStore.pollForAction();
        }

        this.registerPowerSaverModeReceiver(activity.getApplicationContext(), true);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        HTLog.v(TAG, "Inside onActivityPaused");

        this.invalidateAllTimers();
        this.registerPowerSaverModeReceiver(activity.getApplicationContext(), false);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        this.registerPowerSaverModeReceiver(activity.getApplicationContext(), false);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }


}