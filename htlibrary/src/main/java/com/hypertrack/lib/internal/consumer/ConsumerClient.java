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
import com.hypertrack.lib.internal.common.logging.DeviceLogsManager;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.ExpandedLocation;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.models.HTTask;
import com.hypertrack.lib.internal.consumer.models.HTTaskCallBack;
import com.hypertrack.lib.internal.consumer.models.HTTaskDisplay;
import com.hypertrack.lib.internal.consumer.models.HTUser;
import com.hypertrack.lib.internal.consumer.models.TaskFetchCallback;
import com.hypertrack.lib.internal.consumer.models.TaskListCallBack;
import com.hypertrack.lib.internal.consumer.models.TaskStore;
import com.hypertrack.lib.internal.consumer.models.UpdateDestinationCallback;
import com.hypertrack.lib.internal.consumer.utils.HTTaskUtils;
import com.hypertrack.lib.internal.consumer.utils.LocationUtils;
import com.hypertrack.lib.internal.consumer.view.HyperTrackMapFragment;
import com.hypertrack.lib.models.Place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * This class can be used to add one or more task to be tracked on {@link HyperTrackMapFragment}
 * by calling the method {@link #trackTask(String, Activity, HTTaskCallBack)} with a taskID or a
 * list of taskIDs.
 * <p>
 * Other APIs here provide access to data for the tasks which are being currently tracked.
 */

public class ConsumerClient implements TaskFetchCallback, Application.ActivityLifecycleCallbacks {

    public static final String TAG = ConsumerClient.class.getSimpleName();

    public static final String TASK_ID_REQUIRED_ERROR_MESSAGE = "Required Parameter: taskID is required to start tracking.";
    public static final String ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE = "Required Parameter: activity instance is required to start tracking.";
    public static final String CALLBACK_REQUIRED_ERROR_MESSAGE = "Required Parameter: callback instance is required to start tracking.";

    public static final String INTENT_EXTRA_ORDER_STATUS = "TASK_STATUS";
    public static final String INTENT_EXTRA_TASK_ID_LIST = "TASK_ID_LIST";

    public static final String TASK_STATUS_CHANGED_NOTIFICATION = "com.hypertrack.consumer:HTStatusChangedNotification";
    public static final String TASK_DETAIL_REFRESHED_NOTIFICATION = "com.hypertrack.consumer:HTTaskDetailsRefreshedNotification";
    public static final String TASK_REMOVED_FROM_TRACKING_NOTIFICATION = "com.hypertrack.consumer:HTTaskRemovedFromTrackingNotification";

    public static final String TASK_SUB_STATUS_TYPE_DELAYED = "delayed";
    public static final int POST_DEVICE_LOGS_JOB = 11;
    private static final int POST_DEVICE_LOG_INTERVAL = 30000;
    private static final String POST_DEVICE_LOG_TAG = "com.hypertrack.consumer:PostDeviceLog";
    private static ConsumerClient mInstance;
    protected DeviceLogsManager logsManager;
    protected NetworkManager networkManager;
    private Context mContext;
    private TaskStore taskStore;
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
    private HashMap<String, String> currentTaskStatusList;
    private boolean powerSaverModeRegistered = false;

    public ConsumerClient(Context context, SmartScheduler scheduler, DeviceLogsManager logsManager,
                          NetworkManager networkManager) {
        this.mContext = context;
        this.scheduler = scheduler;
        this.logsManager = logsManager;
        this.networkManager = networkManager;

        // Instantiate TaskStore on ConsumerClient instantiation
        taskStore = new TaskStore(mContext, this, this.scheduler, this.networkManager);
        currentTaskStatusList = new HashMap<>();

        // Register for PowerSaverModeChange receiver
        this.registerPowerSaverModeReceiver(context.getApplicationContext(), true);

        // Set Application Callbacks
        setApplicationInstance(context);
    }

    public static ConsumerClient getInstance() {
        return mInstance;
    }

    /**
     * Method to check whether a List of Tasks have same destination location or not
     *
     * @param taskList List of HTTask objects for which the location has to be validated
     * @return Returns true if either there is only one task or a set of tasks for which the Destination
     * Locations are 110m apart, false otherwise.
     */
    public static boolean checkIfTasksHaveSameDestination(List<HTTask> taskList) {
        if (taskList == null || taskList.isEmpty())
            return false;

        if (taskList.size() == 1)
            return true;

        Place destination1 = taskList.get(0).getDestination();
        Place destination2;

        if (destination1 == null)
            return false;

        for (int i = 1; i < taskList.size(); i++) {

            if (taskList.get(i) != null) {
                destination2 = taskList.get(i).getDestination();

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

    /**
     * Method to initialize ConsumerClient (HyperTrack's Consumer SDK).
     * <p>
     * <u><b>IMPORTANT:</b></u>
     * Call this method from your Application.java file with ApplicationContext as parameter.
     *
     * @param context Pass your Application's context variable as the parameter
     */
    public void initConsumerClient(Context context) {
        // TODO: 19/02/17 Add init implementation here
    }

    private void setApplicationInstance(Context context) {
        Application application = (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(this);
    }

    /**
     * Track an order by passing a taskID. Also, takes HTTaskCallBack as second parameter.
     *
     * @param taskID
     * @param callback
     */
    public void trackTask(String taskID, Activity activity, final HTTaskCallBack callback) {

        if (callback == null) {
            HTLog.e(TAG, "Error occurred while trackTask: " + CALLBACK_REQUIRED_ERROR_MESSAGE);
            throw new IllegalArgumentException(CALLBACK_REQUIRED_ERROR_MESSAGE);
        }

        if (activity == null || activity.getApplication() == null) {
            HTLog.e(TAG, "Error occurred while trackTask: " + ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE);
            callback.onError(new IllegalArgumentException(ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE));
            return;
        }

        if (TextUtils.isEmpty(taskID)) {
            HTLog.e(TAG, "Error occurred while trackTask: " + TASK_ID_REQUIRED_ERROR_MESSAGE);
            callback.onError(new IllegalArgumentException(TASK_ID_REQUIRED_ERROR_MESSAGE));
            return;
        }

        taskStore.addTask(taskID, new HTTaskCallBack() {
            @Override
            public void onSuccess(HTTask task) {
                currentTaskStatusList.put(task.getId(), task.getStatus());
                callback.onSuccess(task);
            }

            @Override
            public void onError(Exception exception) {
                callback.onError(exception);
            }
        });

        activity.getApplication().registerActivityLifecycleCallbacks(this);
    }

    /**
     * Track an order by passing a List of taskIDs. Also, takes HTTaskCallBack as second parameter.
     *
     * @param taskIDList
     * @param callback
     */
    public void trackTask(final List<String> taskIDList, final Activity activity, final TaskListCallBack callback) {
        if (callback == null) {
            HTLog.e(TAG, "Error occurred while trackTask: " + CALLBACK_REQUIRED_ERROR_MESSAGE);
            throw new IllegalArgumentException(CALLBACK_REQUIRED_ERROR_MESSAGE);
        }

        if (activity == null || activity.getApplication() == null) {
            HTLog.e(TAG, "Error occurred while trackTask: " + ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE);
            callback.onError(new IllegalArgumentException(ACTIVITY_INSTANCE_REQUIRED_ERROR_MESSAGE));
            return;
        }

        if (taskIDList == null || taskIDList.isEmpty() || taskIDList.contains("")) {
            HTLog.e(TAG, "Error occurred while trackTask: " + TASK_ID_REQUIRED_ERROR_MESSAGE);
            callback.onError(new IllegalArgumentException(TASK_ID_REQUIRED_ERROR_MESSAGE));
            return;
        }

        taskStore.addTaskList(taskIDList, new TaskListCallBack() {
            @Override
            public void onSuccess(List<HTTask> taskList) {
                if (taskList != null) {
                    for (HTTask task : taskList) {
                        currentTaskStatusList.put(task.getId(), task.getStatus());
                    }
                }

                callback.onSuccess(taskList);
            }

            @Override
            public void onError(Exception exception) {
                callback.onError(exception);
            }
        });

        activity.getApplication().registerActivityLifecycleCallbacks(this);
    }

    /**
     * Method to get a Task object for a given taskID
     *
     * @param taskID
     * @return
     */
    public HTTask taskForTaskID(String taskID) {
        if (taskStore != null)
            return taskStore.getTask(taskID);

        return null;
    }

    public int taskActionForTaskID(String taskID) {
        if (taskStore != null && taskStore.getTask(taskID) != null
                && !TextUtils.isEmpty(taskStore.getTask(taskID).getAction())) {

            switch (taskStore.getTask(taskID).getAction().toLowerCase()) {
                case HTTask.TASK_ACTION_DELIVERY:
                    return com.hypertrack.lib.R.string.task_action_delivery;
                case HTTask.TASK_ACTION_PICKUP:
                    return com.hypertrack.lib.R.string.task_action_pickup;
                case HTTask.TASK_ACTION_VISIT:
                    return com.hypertrack.lib.R.string.task_action_visit;
                case HTTask.TASK_ACTION_TASK:
                    return com.hypertrack.lib.R.string.task_action_task;
            }
        }

        return com.hypertrack.lib.R.string.task_action_delivery;
    }

    public ArrayList<String> getTaskIDList() {
        if (taskStore != null) {
            return taskStore.getTaskIDList();
        }

        return null;
    }

    /**
     * Method to get a List of currently Active TaskIDs
     *
     * @return
     */
    public List<String> getActiveTaskIDList() {
        if (taskStore != null)
            return taskStore.getActiveTaskIDList();

        return null;
    }

    private List<HTTask> removeTasksFromTaskStore(List<String> taskIDList) {
        if (taskIDList == null || taskIDList.size() == 0 || taskStore == null)
            return null;

        List<HTTask> removedTaskList = new ArrayList<>();

        for (String taskID : taskIDList) {
            if (!TextUtils.isEmpty(taskID) && taskStore.getTask(taskID) != null) {
                currentTaskStatusList.remove(taskID);
                removedTaskList.add(taskStore.removeTask(taskID));
            }
        }

        if (taskStore.getTaskIDList() == null || taskStore.getTaskIDList().size() == 0) {
            taskStore.invalidateTaskStoreJobs();
        }

        return removedTaskList;
    }

    /**
     * Method to remove a taskID from TaskList
     *
     * @param taskIDToBeRemoved
     * @return
     */
    public HTTask removeTaskID(String taskIDToBeRemoved) {
        List<String> taskIDListToBeRemoved = new ArrayList<>();
        taskIDListToBeRemoved.add(taskIDToBeRemoved);

        List<HTTask> removedTaskList = removeTasksFromTaskStore(taskIDListToBeRemoved);

        if (removedTaskList != null && removedTaskList.size() > 0 && removedTaskList.get(0) != null) {
            ArrayList<String> removedTaskIDList = new ArrayList<>();
            removedTaskIDList.add(removedTaskList.get(0).getId());

            // Send TaskRemoved Success broadcast in case of any Task being removed from tracking
            broadcastTaskRemoved(removedTaskIDList);

            return removedTaskList.get(0);
        }

        // Send TaskRemoved broadcast with null Intent extra in case no task was removed from tracking
        broadcastTaskRemoved(null);

        return null;
    }

    /**
     * Method to remove a List of TaskIDs from TaskList
     *
     * @param taskIDListToBeRemoved
     * @return
     */
    public List<HTTask> removeTaskID(List<String> taskIDListToBeRemoved) {
        if (taskIDListToBeRemoved != null && taskIDListToBeRemoved.size() > 0) {

            List<HTTask> removedTaskList = removeTasksFromTaskStore(taskIDListToBeRemoved);

            ArrayList<String> removedTaskIDList = null;

            if (removedTaskList != null && removedTaskList.size() > 0) {
                removedTaskIDList = new ArrayList<>();

                for (int i = 0; i < removedTaskList.size(); i++) {
                    if (removedTaskList.get(i) != null) {
                        removedTaskIDList.add(removedTaskList.get(i).getId());
                    }
                }

                if (removedTaskIDList.size() > 0) {
                    // Send TaskRemoved Success broadcast in case of any Task being removed from tracking
                    broadcastTaskRemoved(removedTaskIDList);
                }
            } else {
                // Send TaskRemoved broadcast with null Intent extra in case no task was removed from tracking
                broadcastTaskRemoved(null);
            }

            return removedTaskList;
        }

        // Send TaskRemoved broadcast with null Intent extra in case no task was removed from tracking
        broadcastTaskRemoved(null);

        return null;
    }

    /**
     * Method to clear all tasks from TaskList
     */
    public void clearTasks() {
        if (taskStore != null && taskStore.getTaskIDList() != null && taskStore.getTaskIDList().size() > 0) {

            List<String> taskIDListToBeRemoved = taskStore.getTaskIDList();
            removeTaskID(taskIDListToBeRemoved);
        } else {
            // Send TaskRemoved broadcast with null Intent extra in case no task was removed from tracking
            broadcastTaskRemoved(null);
        }
    }

    public void updateDestinationLocation(String taskID, GeoJSONLocation location, UpdateDestinationCallback callback) {
        if (taskStore != null && taskStore.getTask(taskID) != null) {
            taskStore.updateDestinationLocation(taskID, location, callback);
        } else {
            if (callback != null)
                callback.onError(new IllegalStateException("No task has been started with given taskID"));
        }
    }

    /**
     * Call this method to get DestinationLocation's LatLng
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's DestinationLocation LatLng if it is being tracked currently, null otherwise.
     */
    public LatLng getDestinationLocationLatLng(String taskID) {
        if (taskStore != null)
            return taskStore.getDestinationLocationLatLng(taskID);

        return null;
    }

    /**
     * Call this method to get CompletionLocation's LatLng
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's CompletionLocation LatLng if it is being tracked currently, null otherwise.
     */
    public LatLng getCompletionLocationLatLng(String taskID) {
        if (taskStore != null)
            return taskStore.getCompletionLocationLatLng(taskID);

        return null;
    }

    /**
     * Call this method to get SourceLocation's LatLng
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's SourceLocation LatLng if it is being tracked currently, null otherwise.
     */
    public LatLng getSourceLocationLatLng(String taskID) {
        if (taskStore != null)
            return taskStore.getStartLocationLatLng(taskID);

        return null;
    }

    /**
     * Call this method to get SourceLocation's Address
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's SourceLocation Address if it is being tracked currently, null otherwise.
     */
    public String getStartAddress(String taskID) {
        if (taskStore != null)
            return taskStore.getStartAddress(taskID);

        return null;
    }

    /**
     * Call this method to get DestinationLocation's Address
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's DestinationLocation Address if it is being tracked currently, null otherwise.
     */
    public String getDestinationAddress(String taskID) {
        if (taskStore != null)
            return taskStore.getDestinationAddress(taskID);

        return null;
    }

    /**
     * Call this method to get Task CompletionLocation's Address
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's CompletionLocation Address if it is being tracked currently, null otherwise.
     */
    public String getCompletionAddress(String taskID) {
        if (taskStore != null)
            return taskStore.getCompletionAddress(taskID);

        return null;
    }

    /**
     * Call this method to get tasks's current status.
     * <p>
     * Task's Status can be one of [HTTask#TASK_STATUS_NOT_STARTED,
     * HTTask#TASK_STATUS_DISPATCHING, HTTask#TASK_STATUS_USER_ON_THE_WAY,
     * HTTask#TASK_STATUS_USER_ARRIVING, HTTask#TASK_STATUS_USER_ARRIVED,
     * HTTask#TASK_STATUS_COMPLETED, HTTask#TASK_STATUS_CANCELED,
     * HTTask#TASK_STATUS_ABORTED, HTTask#TASK_STATUS_SUSPENDED]
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's Status if it is being tracked currently, null otherwise.
     */
    public String getStatus(String taskID) {
        if (taskStore != null)
            return taskStore.getStatus(taskID);

        return null;
    }

    /**
     * Call this method to get tasks's current connection status
     * <p>
     * Task's Connection Status can be one of [HTTask#TASK_STATUS_CONNECTION_HEALTHY,
     * HTTask#TASK_STATUS_CONNECTION_LOST]
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's Connection status if it is being tracked currently, null otherwise.
     */
    public String getConnectionStatus(String taskID) {
        if (taskStore != null)
            return taskStore.getConnectionStatus(taskID);

        return null;
    }

    /**
     * Call this method to get tasks's display eta value (in minutes)
     * <p>
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Double value of ETA (in minutes) if a task is being tracked currently, null otherwise
     */
    public Integer getTaskDisplayETA(String taskID) {
        if (taskStore != null && taskStore.getTask(taskID) != null) {
            return HTTaskUtils.getTaskDisplayETA(taskStore.getTask(taskID).getTaskDisplay());
        }

        return null;
    }

    /**
     * Call this method to get tasks's current display status
     * <p>
     * Task's Display Status can be one of HTTask#TASK_STATUS_NOT_STARTED,
     * HTTask#TASK_STATUS_DISPATCHING, HTTask#TASK_STATUS_USER_ON_THE_WAY,
     * HTTask#TASK_STATUS_USER_ARRIVING, HTTask#TASK_STATUS_USER_ARRIVED,
     * HTTask#TASK_STATUS_COMPLETED, HTTask#TASK_STATUS_CANCELED,
     * HTTask#TASK_STATUS_ABORTED, HTTask#TASK_STATUS_SUSPENDED,
     * HTTask#TASK_STATUS_CONNECTION_LOST, HTTask#TASK_STATUS_NO_LOCATION]
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's Display Status if it is being tracked currently, null otherwise.
     */
    public String getTaskDisplayStatus(String taskID) {
        if (taskStore != null)
            return taskStore.getTaskDisplayStatus(taskID);

        return null;
    }

    /**
     * Call this method to get tasks's current status text to be displayed
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's Display Status Text if it is being tracked currently, null otherwise.
     */
    public String getTaskDisplayStatusText(String taskID) {
        if (taskStore != null && this.getTaskDisplay(taskID) != null) {
            return this.getTaskDisplay(taskID).getStatusText();
        }

        return null;
    }

    /**
     * Call this method to get task's current display sub-status
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's Display Sub-Status if it is being tracked currently, null otherwise.
     */
    public String getTaskDisplaySubStatus(Context context, String taskID) {
        if (taskStore != null) {
            HTTaskDisplay taskDisplay = this.getTaskDisplay(taskID);

            if (taskDisplay != null) {
                if (taskDisplay.getSubStatusDuration() != null) {

                    Double timeInSeconds = Double.valueOf(taskDisplay.getSubStatusDuration());
                    StringBuilder builder = new StringBuilder(HTTaskUtils.getFormattedTimeString(context,
                            timeInSeconds)).append(" ");

                    if (TASK_SUB_STATUS_TYPE_DELAYED.equalsIgnoreCase(taskDisplay.getSubStatus())) {
                        builder.append(context.getString(com.hypertrack.lib.R.string.task_sub_status_delayed_suffix_text));
                    } else {
                        builder.append(context.getString(com.hypertrack.lib.R.string.task_sub_status_default_suffix_text));
                    }

                    return builder.toString();
                }

                if (!TextUtils.isEmpty(taskDisplay.getSubStatusText())) {
                    return taskDisplay.getSubStatusText();
                }
            }
        }

        return null;
    }

    /**
     * Call this method to get task's current sub-status text to be displayed
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Task's Display Sub-Status Text if it is being tracked currently, null otherwise.
     */
    public String getTaskDisplaySubStatusText(String taskID) {
        if (taskStore != null && this.getTaskDisplay(taskID) != null) {
            return this.getTaskDisplay(taskID).getSubStatusText();
        }

        return null;
    }

    /**
     * Call this method to get flag to show/hide TaskSummary for current TaskStatus
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Boolean flag to show TaskSummary for specific TaskStatus of given taskID
     */
    public boolean showTaskSummaryForTaskStatus(String taskID) {
        if (taskStore != null) {
            HTTaskDisplay taskDisplay = this.getTaskDisplay(taskID);

            if (taskDisplay != null && taskDisplay.isShowTaskSummary())
                return true;
        }

        return false;
    }

    /**
     * Call this method to get tasks's current display object
     *
     * @param taskID Pass taskID of the task as parameter
     * @return Display for given taskID if it is being tracked currently, null otherwise.
     */
    public HTTaskDisplay getTaskDisplay(String taskID) {
        if (taskStore != null && taskStore.getTask(taskID) != null)
            return taskStore.getTask(taskID).getTaskDisplay();

        return null;
    }

    /**
     * Call this method to get Tasks's Status Text ResourceId.
     *
     * @param taskID Pass taskID of the task as parameter
     * @return ResourceId for Task's Status text.
     */
    public Integer getStatusTextResourceIdForToolbar(String taskID) {
        if (taskStore != null) {

            String taskDisplayStatus = taskStore.getTaskDisplayStatus(taskID);

            if (TextUtils.isEmpty(taskDisplayStatus))
                return null;

            switch (taskDisplayStatus) {
                case HTTask.TASK_STATUS_NOT_STARTED:
                    return com.hypertrack.lib.R.string.task_status_not_started;
                case HTTask.TASK_STATUS_DISPATCHING:
                    return com.hypertrack.lib.R.string.task_status_dispatching;
                case HTTask.TASK_STATUS_USER_ON_THE_WAY:
                    return com.hypertrack.lib.R.string.task_status_user_on_the_way;
                case HTTask.TASK_STATUS_USER_ARRIVING:
                    return com.hypertrack.lib.R.string.task_status_user_arriving;
                case HTTask.TASK_STATUS_USER_ARRIVED:
                    return com.hypertrack.lib.R.string.task_status_user_arrived;
                case HTTask.TASK_STATUS_COMPLETED:
                    return com.hypertrack.lib.R.string.task_status_completed;
                case HTTask.TASK_STATUS_CANCELED:
                    return com.hypertrack.lib.R.string.task_status_canceled;
                case HTTask.TASK_STATUS_ABORTED:
                    return com.hypertrack.lib.R.string.task_status_aborted;
                case HTTask.TASK_STATUS_SUSPENDED:
                    return com.hypertrack.lib.R.string.task_status_suspended;
                case HTTask.TASK_STATUS_NO_LOCATION:
                    return com.hypertrack.lib.R.string.task_status_no_location;
                case HTTask.TASK_STATUS_LOCATION_LOST:
                    return com.hypertrack.lib.R.string.task_status_location_lost;
                case HTTask.TASK_STATUS_CONNECTION_LOST:
                    return com.hypertrack.lib.R.string.task_status_connection_lost;
                default:
                    return null;
            }
        }

        return null;
    }

    /**
     * Call this method to get LastUpdated ETA for a task.
     * <p>
     * In case of Loss of Connectivity with the user, the ETA returned will be the
     * last updated value.
     *
     * @param taskID TaskID of the task for which lastUpdatedETA has to be fetched
     * @return Last updated ETA (in minutes)
     */
    public Integer getLastUpdatedETAInMinutes(String taskID) {
        return getTaskDisplayETA(taskID);
    }

    /**
     * Call this method to get Current ETA for a task.
     *
     * @param taskID TaskID of the task for which ETA has to be fetched
     * @return Current ETA (in minutes)
     */
    public Integer getEstimatedTimeOfArrivalInMinutes(String taskID) {
        if (taskStore != null)
            return taskStore.getEstimatedTimeOfArrival(taskID);

        return null;
    }

    /**
     * Call this method to get Task's Duration (in minutes)
     *
     * @param taskID TaskID of the task for which the duration has to be computed
     * @return Task's Duration (in minutes)
     */
    public Integer getTaskDuration(String taskID) {
        if (taskStore != null)
            return taskStore.getTaskDurationInMinutes(taskID);

        return null;
    }

    /**
     * Call this method to get Task's Duration String
     *
     * @param taskID TaskID of the task for which the duration has to be computed
     * @return Task's Duration String
     */
    public String getTaskDurationString(Context context, String taskID) {
        if (taskStore != null) {
            return HTTaskUtils.getTaskDurationString(context, taskStore.getTask(taskID));
        }

        return null;
    }

    /**
     * Call this method to get Task's Distance (in KMs)
     *
     * @param taskID TaskID of the task for which the distance has to be computed
     * @return Task's Distance (in KMs)
     */
    public Double getTaskDistanceInKMs(String taskID) {
        if (taskStore != null) {
            return HTTaskUtils.getTaskDistanceInKMs(taskStore.getTask(taskID));
        }

        return null;
    }

    /**
     * Call this method to get Task's Distance String
     *
     * @param taskID TaskID of the task for which the distance has to be computed
     * @return Task's Distance String
     */
    public String getTaskDistanceString(Context context, String taskID) {
        if (taskStore != null) {
            HTTaskUtils.getTaskDistanceString(context, taskStore.getTask(taskID));
        }

        return null;
    }

    /**
     * Call this method to get Task's Metering Data String
     *
     * @param taskID TaskID of the task for which the metering data has to be computed
     * @return Task's Metering Data String
     */
    public String getTaskMeteringString(Context context, String taskID) {
        if (taskStore != null)
            return HTTaskUtils.getTaskMeteringString(context, taskStore.getTask(taskID));

        return null;
    }

    @Override
    public void onFetchTask(boolean result, ArrayList<String> taskIDList) {

        if (taskStore != null) {
            ArrayList<String> changedStatusTaskList = new ArrayList<>();
            ArrayList<String> refreshedTaskList = new ArrayList<>();

            // Check for all taskIDs existing in TaskStore
            for (String taskID : taskIDList) {

                if (taskStore.getTask(taskID) != null) {
                    String taskStatus = taskStore.getStatus(taskID);

                    // Check if the state was changed for current task
                    if (this.currentTaskStatusList.get(taskID) != null
                            && !this.currentTaskStatusList.get(taskID).equalsIgnoreCase(taskStatus)) {

                        // Update currentTaskStatus & changedStatusTaskList (indicating State Change for current taskID)
                        this.currentTaskStatusList.put(taskID, taskStatus);
                        changedStatusTaskList.add(taskID);
                    } else {
                        refreshedTaskList.add(taskID);
                    }
                }
            }

            // Send TaskStatusChanged broadcast in case of State Change in any Task
            if (changedStatusTaskList.size() > 0) {
                this.broadcastTaskStatusChanged(changedStatusTaskList);
            }
            // Send TaskStatusRefreshed broadcast in case of Task Data Refresh
            if (refreshedTaskList.size() > 0) {
                this.broadcastTaskRefreshed(refreshedTaskList);
            }
        }
    }

    private void broadcastTaskStatusChanged(ArrayList<String> changedStatusTaskIDList) {
        Intent intent = new Intent(ConsumerClient.TASK_STATUS_CHANGED_NOTIFICATION);
        intent.putExtra(INTENT_EXTRA_ORDER_STATUS, this.currentTaskStatusList);
        intent.putStringArrayListExtra(INTENT_EXTRA_TASK_ID_LIST, changedStatusTaskIDList);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void broadcastTaskRefreshed(ArrayList<String> taskIDList) {
        Intent intent = new Intent(ConsumerClient.TASK_DETAIL_REFRESHED_NOTIFICATION);
        intent.putStringArrayListExtra(INTENT_EXTRA_TASK_ID_LIST, taskIDList);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void broadcastTaskRemoved(ArrayList<String> taskIDList) {
        Intent intent = new Intent(ConsumerClient.TASK_REMOVED_FROM_TRACKING_NOTIFICATION);
        intent.putStringArrayListExtra(INTENT_EXTRA_TASK_ID_LIST, taskIDList);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    /**
     * @return
     */
    public HTUser getUser(String taskID) {
        if (taskStore != null)
            return taskStore.getUser(taskID);

        return null;
    }

    public ExpandedLocation getUserLastKnownLocation(String taskID) {
        if (taskStore != null && taskStore.getUser(taskID) != null) {
            return taskStore.getUser(taskID).getLastKnownLocation();
        }

        return null;
    }

    public LatLng getUserLastKnownLocationCoordinates(String taskID) {
        if (this.getUserLastKnownLocation(taskID) != null) {
            GeoJSONLocation location = this.getUserLastKnownLocation(taskID).getGeoJSONLocation();
            if (location != null)
                return new LatLng(location.getCoordinates()[1], location.getCoordinates()[0]);
        }

        return null;
    }

    public Float getUserLastKnownLocationBearing(String taskID) {
        if (this.getUserLastKnownLocation(taskID) != null) {
            Float bearing = this.getUserLastKnownLocation(taskID).getBearing();
            return bearing;
        }

        return null;
    }

    /**
     * @return
     */
    public HTUserVehicleType getVehicleTypeString(String taskID) {

        if (taskStore != null)
            return taskStore.getVehicleType(taskID);

        return HTUserVehicleType.MOTORCYCLE;
    }

    public boolean anyTaskInStartedState() {
        if (taskStore != null) {
            return taskStore.anyTaskInStartedState();
        }

        return false;
    }

    public boolean isTaskCompleted(String taskID) {
        if (taskStore != null) {
            return taskStore.isTaskCompleted(taskID);
        }

        return false;
    }

    public boolean isTaskFinished(String taskID) {
        if (taskStore != null) {
            return taskStore.isTaskFinished(taskID);
        }

        return false;
    }

    public void invalidateAllTimers() {
        if (taskStore != null) {
            taskStore.invalidateTaskStoreJobs();
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

        if (taskStore != null && taskStore.getActiveTaskIDList() != null
                && taskStore.getActiveTaskIDList().size() > 0) {
            taskStore.pollForTask();
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