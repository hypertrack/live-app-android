package com.hypertrack.lib.internal.consumer.models;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.internal.common.network.HTGson;
import com.hypertrack.lib.internal.common.network.HTNetworkResponse;
import com.hypertrack.lib.internal.common.network.HyperTrackGetRequest;
import com.hypertrack.lib.internal.common.network.HyperTrackNetworkRequest;
import com.hypertrack.lib.internal.common.network.HyperTrackPostRequest;
import com.hypertrack.lib.internal.common.network.NetworkErrorUtil;
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.utils.HTTaskUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.hypertrack.smart_scheduler.Job;
import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * Created by suhas on 29/08/15.
 */
public class TaskStore implements SmartScheduler.JobScheduledCallback {

    private static final String TAG = TaskStore.class.getSimpleName();
    private static final String UPDATE_DESTINATION_TAG = "update_destination";
    private static final String API_ENDPOINT = "tasks/";
    private static final String API_URL = BuildConfig.BASE_URL;
    private static final int FETCH_TASK_DETAILS_JOB = 12;
    private static final String FETCH_TASK_DETAILS_TAG = "com.hypertrack.consumer:FetchTaskDetails";
    private static final long FETCH_TASK_DETAILS_INTERVAL = BuildConfig.POLL_TIMER_DURATION;

    @Deprecated
    private static final String TASK_STATUS_TAG = "order_status";
    private static TaskFetchCallback delegate;
    private Context mContext;
    private TaskList taskList;
    private SmartScheduler smartScheduler;
    private NetworkManager networkManager;

    public TaskStore(Context context, TaskFetchCallback delegate, SmartScheduler smartScheduler,
                     NetworkManager networkManager) {
        mContext = context;
        TaskStore.delegate = delegate;
        this.smartScheduler = smartScheduler;
        this.networkManager = networkManager;
        taskList = new TaskList();
    }

    public HTTask getTask(String taskID) {
        return taskList.getTask(taskID);
    }

    public TaskList getTaskList() {
        return taskList;
    }

    public ArrayList<String> getTaskIDList() {
        return taskList.getTaskIDList();
    }

    /**
     * Method to get a list of Active TaskIDs
     *
     * @return
     */
    public List<String> getActiveTaskIDList() {
        return taskList.getActiveTaskIDList();
    }

    /**
     * Method to add a task to TaskList
     *
     * @param taskID
     * @param callBack
     */
    public void addTask(final String taskID, final HTTaskCallBack callBack) {
        // Check if taskID is being currently tracked or not
        if (taskList.getTask(taskID) != null) {
            if (callBack != null)
                callBack.onSuccess(taskList.getTask(taskID));
            return;
        }

        final ArrayList<String> taskIDList = new ArrayList<>();
        taskIDList.add(taskID);

        getTaskDetails(taskIDList, new TaskListCallBack() {
            @Override
            public void onSuccess(List<HTTask> taskToBeAdded) {

                // Add this tasks in taskListToBeAdded to taskList
                if (taskToBeAdded.size() > 0) {
                    taskList.addTask(taskToBeAdded.get(0));

                    if (callBack != null)
                        callBack.onSuccess(taskToBeAdded.get(0));
                }

                pollForTask();
            }

            @Override
            public void onError(Exception exception) {
                if (callBack != null)
                    callBack.onError(exception);
            }
        });
    }

    /**
     * Method to add a List of tasks to TaskList
     *
     * @param taskIDList
     * @param callBack
     */
    public void addTaskList(List<String> taskIDList, final TaskListCallBack callBack) {
        // Get all the tasks being tracked out of given taskIDList
        List<HTTask> trackedTaskList = new ArrayList<>();
        for (String taskID : taskIDList) {
            if (taskList.getTask(taskID) != null)
                trackedTaskList.add(taskList.getTask(taskID));
        }

        // Check if all the taskIDs in the list are being currently tracked or not
        if (taskIDList.size() == trackedTaskList.size()) {
            if (callBack != null)
                callBack.onSuccess(trackedTaskList);
            return;
        }

        getTaskDetails(taskIDList, new TaskListCallBack() {
            @Override
            public void onSuccess(List<HTTask> taskListToBeAdded) {

                // Add all the tasks in taskListToBeAdded to taskList
                if (taskListToBeAdded.size() > 0) {
                    taskList.addTaskList(taskListToBeAdded);

                    if (callBack != null)
                        callBack.onSuccess(taskListToBeAdded);
                }

                pollForTask();
            }

            @Override
            public void onError(Exception exception) {
                if (callBack != null)
                    callBack.onError(exception);
            }
        });
    }

    private void scheduleFetchTaskDetailsJob(long intervalInMillis) {
        // Remove existing job, if any
        if (smartScheduler.contains(FETCH_TASK_DETAILS_JOB)) {
            return;
        }

        // Schedule the next job
        Job fetchTaskDetailsJob = new Job.Builder(FETCH_TASK_DETAILS_JOB, this, FETCH_TASK_DETAILS_TAG)
                .setIntervalMillis(intervalInMillis)
                .setRequiredNetworkType(Job.NetworkType.NETWORK_TYPE_CONNECTED)
                .build();

        smartScheduler.addJob(fetchTaskDetailsJob);
    }

    private void removeFetchTaskDetailsJob() {
        if (smartScheduler == null)
            smartScheduler = SmartScheduler.getInstance(mContext);

        // Remove existing job, if any
        if (smartScheduler.contains(FETCH_TASK_DETAILS_JOB)) {
            smartScheduler.removeJob(FETCH_TASK_DETAILS_JOB);
        }
    }

    @Override
    public void onJobScheduled(Context context, Job job) {
        if (job != null && job.getJobId() == FETCH_TASK_DETAILS_JOB) {
            Log.d(TAG, "FetchTaskDetails Job Scheduled");
            pollForTask();
        }
    }

    public void pollForTask() {
        Log.d(TAG, "polling for task");

        removeFetchTaskDetailsJob();

        if (taskList.getActiveTaskIDList().size() > 0) {

            getTaskDetails(taskList.getActiveTaskIDList(), new TaskListCallBack() {
                @Override
                public void onSuccess(List<HTTask> taskListToBeAdded) {

                    // Add TaskList in response to be tracked
                    taskList.addTaskList(taskListToBeAdded);

                    // Check if there is any active task being tracked
                    if (!taskList.areAllTasksCompleted()) {
                        scheduleFetchTaskDetailsJob(FETCH_TASK_DETAILS_INTERVAL);
                    }

                    ArrayList<String> updatedTaskIDList = new ArrayList<String>();
                    if (taskListToBeAdded != null) {
                        for (HTTask task : taskListToBeAdded) {
                            if (!TextUtils.isEmpty(task.getId()))
                                updatedTaskIDList.add(task.getId());
                        }
                    }

                    delegate.onFetchTask(true, updatedTaskIDList);
                }

                @Override
                public void onError(Exception exception) {
                    // Check if there is any active task being tracked
                    if (!taskList.areAllTasksCompleted()) {
                        scheduleFetchTaskDetailsJob(FETCH_TASK_DETAILS_INTERVAL);
                    }
                }
            });
        }
    }

    public void getTaskDetails(final List<String> taskIDList, final TaskListCallBack callBack) {
        // Counter to maintain a count of TaskIDs for which data is being fetched
        int count = 0;

        // Also, filter tasks based on their current status
        StringBuilder url = new StringBuilder(API_URL + API_ENDPOINT + "expanded/?id=");

        // Construct url for a List of taskIDs
        for (String taskID : taskIDList) {

            // Check if taskID is not empty & if it is not already completed
            if (!TextUtils.isEmpty(taskID)) {
                if (taskList.getTask(taskID) == null || !taskList.getTask(taskID).isCompleted()) {
                    url.append(taskID);
                    count++;

                    // Check if current item is not the last in the list
                    if (taskIDList.indexOf(taskID) != taskIDList.size() - 1) {
                        url.append(",");
                    }
                }
            }
        }

        // Check if there is any valid taskID to be fetched or not
        if (count == 0 && callBack != null) {
            callBack.onSuccess(taskList != null ? taskList.getTaskList() : new ArrayList<HTTask>());
            return;
        }

        HyperTrackGetRequest<TaskListResponse> getNetworkRequest = new HyperTrackGetRequest<>(
                TAG, mContext, url.toString(), HyperTrackNetworkRequest.HTNetworkClient.HT_NETWORK_CLIENT_HTTP,
                TaskListResponse.class,
                new HTNetworkResponse.Listener<TaskListResponse>() {
                    @Override
                    public void onResponse(TaskListResponse response) {
                        HTLog.d(TAG, "Response : " + response.toString());

                        List<HTTask> updatedTaskList = response.getTaskList();

                        if (callBack != null) {
                            callBack.onSuccess(updatedTaskList);
                        }
                    }
                },
                new HTNetworkResponse.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error, Exception exception) {
                        if (error == null || error.networkResponse == null) {
                            if (callBack != null) {
                                callBack.onError(new RuntimeException("Failed to fetch task. Something went wrong."));
                            }

                            HTLog.e(TAG, "Failed to fetch task. Something went wrong.");
                        } else {
                            HTLog.e(TAG, "Error occurred while onErrorResponse in getTaskDetails: " + error);
                            try {
                                RuntimeException runtimeException = (RuntimeException) NetworkErrorUtil.getException(error);
                                if (callBack != null) {
                                    callBack.onError(runtimeException);
                                }

                            } catch (Exception e) {
                                HTLog.e(TAG, "Exception while onErrorResponse in getTaskDetails: " + e, e);
                            }
                        }
                    }
                });

        networkManager.execute(mContext, getNetworkRequest);
    }

    public void updateDestinationLocation(final String taskID, final GeoJSONLocation location,
                                          final UpdateDestinationCallback callback) {
        if (taskList == null || taskList.getTask(taskID) == null) {
            HTLog.e(TAG, "updateDestinationLocation Error: Please check if taskID is being passed correctly");
            if (callback != null)
                callback.onError(new IllegalArgumentException("Please check if taskID is being passed correctly"));
        }

        if (location == null) {
            HTLog.e(TAG, "updateDestinationLocation Error: Location is required to update Destination location");
            if (callback != null)
                callback.onError(new RuntimeException("Required Parameter: Location is required to update Destination location"));
        }

        String url = API_URL + API_ENDPOINT + taskID + "/update_destination/";

        Gson gson = HTGson.gson();
        String jsonString = gson.toJson(new UpdateDestinationRequest(location));

        HyperTrackPostRequest<HTTask> postNetworkRequest = new HyperTrackPostRequest<>(UPDATE_DESTINATION_TAG,
                mContext, url, HyperTrackNetworkRequest.HTNetworkClient.HT_NETWORK_CLIENT_HTTP,
                jsonString, HTTask.class,
                new HTNetworkResponse.Listener<HTTask>() {
                    @Override
                    public void onResponse(HTTask response) {
                        HTLog.d(TAG, "updateDestinationLocation Response : " + response.toString());

                        if (callback != null)
                            callback.onSuccess(response);
                    }
                },
                new HTNetworkResponse.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error, Exception exception) {
                        if (callback != null)
                            callback.onError(error);

                        HTLog.e(TAG, "updateDestinationLocation Error: " + error.networkResponse.statusCode);
                    }
                });

        networkManager.execute(mContext, postNetworkRequest);
    }

    public HTTask removeTask(String taskID) {
        if (taskList != null) {
            return taskList.removeTask(taskID);
        }

        return null;
    }

    public void invalidateTaskStoreJobs() {
        removeFetchTaskDetailsJob();

        // Cancel all pending calls with TAG
        networkManager.cancel(TAG);
    }

    public HTUser getUser(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return taskList.getTask(taskID).getUser();
        }

        return null;
    }

    public LatLng getDestinationLocationLatLng(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return HTTaskUtils.getDestinationLatLng(taskList.getTask(taskID));
        }

        return null;
    }

    public LatLng getCompletionLocationLatLng(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return HTTaskUtils.getCompletionLatLng(taskList.getTask(taskID));
        }

        return null;
    }

    public LatLng getStartLocationLatLng(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return HTTaskUtils.getStartLatLng(taskList.getTask(taskID));
        }

        return null;
    }

    public Integer getEstimatedTimeOfArrival(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {

            HTTask task = taskList.getTask(taskID);

            if (task.getETA() != null) {
                long duration = task.getETA().getTime() - (new Date()).getTime();
                double durationInMinutes = Math.ceil((duration / (float)1000) / (float)60);

                if (durationInMinutes < 1) {
                    durationInMinutes = 1;
                }

                return (int)durationInMinutes;
            }
        }

        return null;
    }

    public Integer getTaskDurationInMinutes(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return HTTaskUtils.getTaskDurationInMinutes(taskList.getTask(taskID));
        }

        return null;
    }

    public Double getTaskDistanceInKMs(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return HTTaskUtils.getTaskDistanceInKMs(taskList.getTask(taskID));
        }

        return null;
    }

    public String getStatus(String taskID) {

        if (taskList != null) {
            return HTTaskUtils.getTaskStatus(taskList.getTask(taskID));
        }

        return null;
    }

    public String getConnectionStatus(String taskID) {

        if (taskList != null) {
            return HTTaskUtils.getConnectionStatus(taskList.getTask(taskID));
        }

        return null;
    }

    public String getTaskDisplayStatus(String taskID) {

        if (taskList != null) {
            return HTTaskUtils.getTaskDisplayStatus(taskList.getTask(taskID));
        }

        return null;
    }

    public String getStartAddress(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return HTTaskUtils.getStartAddress(taskList.getTask(taskID));
        }

        return null;
    }

    public String getDestinationAddress(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return HTTaskUtils.getDestinationAddress(taskList.getTask(taskID));
        }

        return null;
    }

    public String getCompletionAddress(String taskID) {

        if (taskList != null && taskList.getTask(taskID) != null) {
            return HTTaskUtils.getCompletionAddress(taskList.getTask(taskID));
        }

        return null;
    }

    public HTUserVehicleType getVehicleType(String taskID) {
        if (taskList != null && taskList.getTask(taskID) != null) {

            HTTask task = taskList.getTask(taskID);

            if (task.getVehicleType() != null) {
                return task.getVehicleType();
            }

            if (task.getUser() != null) {
                if (task.getUser().getVehicleType() != null) {
                    return task.getUser().getVehicleType();
                }
            }
        }

        return HTUserVehicleType.MOTORCYCLE;
    }

    public boolean anyTaskInStartedState() {
        return taskList != null && taskList.anyTaskStarted();
    }

    public boolean isTaskCompleted(String taskID) {
        return taskList != null && taskList.isTaskCompleted(taskID);
    }

    public boolean isTaskFinished(String taskID) {
        return taskList != null && taskList.isTaskFinished(taskID);
    }
}
