package com.hypertrack.lib.internal.consumer.models;

import android.content.Context;
import android.util.Log;

import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.network.HTGson;
import com.hypertrack.lib.internal.common.network.HTNetworkResponse;
import com.hypertrack.lib.internal.common.network.HyperTrackGetRequest;
import com.hypertrack.lib.internal.common.network.HyperTrackNetworkRequest;
import com.hypertrack.lib.internal.common.network.HyperTrackPostRequest;
import com.hypertrack.lib.internal.common.network.NetworkErrorUtil;
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.utils.HTActionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.hypertrack.smart_scheduler.Job;
import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * Created by suhas on 29/08/15.
 */
public class ActionStore implements SmartScheduler.JobScheduledCallback {

    private static final String TAG = ActionStore.class.getSimpleName();
    private static final String UPDATE_DESTINATION_TAG = "update_destination";
    private static final String API_ENDPOINT = "actions/";
    private static final String API_URL = BuildConfig.CORE_API_BASE_URL;
    private static final int FETCH_ACTION_DETAILS_JOB = 12;
    private static final String FETCH_ACTION_DETAILS_TAG = "com.hypertrack.consumer:FetchActionDetails";
    private static final long FETCH_ACTION_DETAILS_INTERVAL = BuildConfig.POLL_TIMER_DURATION;

    @Deprecated
    private static final String ACTION_STATUS_TAG = "order_status";
    private static ActionFetchCallback delegate;
    private Context mContext;
    private ActionList actionList;
    private SmartScheduler smartScheduler;
    private NetworkManager networkManager;

    public ActionStore(Context context, ActionFetchCallback delegate, SmartScheduler smartScheduler,
                       NetworkManager networkManager) {
        mContext = context;
        ActionStore.delegate = delegate;
        this.smartScheduler = smartScheduler;
        this.networkManager = networkManager;
        actionList = new ActionList();
    }

    public HTAction getAction(String actionID) {
        return actionList.getAction(actionID);
    }

    public ActionList getActionList() {
        return actionList;
    }

    public ArrayList<String> getActionIDList() {
        return actionList.getActionIDList();
    }

    /**
     * Method to get a list of Active ActionIDs
     *
     * @return
     */
    public List<String> getActiveActionIDList() {
        return actionList.getActiveActionIDList();
    }

    /**
     * Method to add a action to ActionList
     *
     * @param actionID
     * @param callBack
     */
    public void addAction(final String actionID, final HTActionCallBack callBack) {
        // Check if actionID is being currently tracked or not
        if (actionList.getAction(actionID) != null) {
            if (callBack != null)
                callBack.onSuccess(actionList.getAction(actionID));
            return;
        }

        final ArrayList<String> actionIDList = new ArrayList<>();
        actionIDList.add(actionID);

        getActionDetails(actionIDList, new ActionListCallBack() {
            @Override
            public void onSuccess(List<HTAction> actionToBeAdded) {

                // Add this actions in actionListToBeAdded to actionList
                if (actionToBeAdded.size() > 0) {
                    actionList.addAction(actionToBeAdded.get(0));

                    if (callBack != null)
                        callBack.onSuccess(actionToBeAdded.get(0));
                }

                pollForAction();
            }

            @Override
            public void onError(Exception exception) {
                if (callBack != null)
                    callBack.onError(exception);
            }
        });
    }

    /**
     * Method to add a List of actions to ActionList
     *
     * @param actionIDList
     * @param callBack
     */
    public void addActionList(List<String> actionIDList, final ActionListCallBack callBack) {
        // Get all the actions being tracked out of given actionIDList
        List<HTAction> trackedActionList = new ArrayList<>();
        for (String actionID : actionIDList) {
            if (actionList.getAction(actionID) != null)
                trackedActionList.add(actionList.getAction(actionID));
        }

        // Check if all the actionIDs in the list are being currently tracked or not
        if (actionIDList.size() == trackedActionList.size()) {
            if (callBack != null)
                callBack.onSuccess(trackedActionList);
            return;
        }

        getActionDetails(actionIDList, new ActionListCallBack() {
            @Override
            public void onSuccess(List<HTAction> actionListToBeAdded) {

                // Add all the actions in actionListToBeAdded to actionList
                if (actionListToBeAdded.size() > 0) {
                    actionList.addActionList(actionListToBeAdded);

                    if (callBack != null)
                        callBack.onSuccess(actionListToBeAdded);
                }

                pollForAction();
            }

            @Override
            public void onError(Exception exception) {
                if (callBack != null)
                    callBack.onError(exception);
            }
        });
    }

    private void scheduleFetchActionDetailsJob(long intervalInMillis) {
        // Remove existing job, if any
        if (smartScheduler.contains(FETCH_ACTION_DETAILS_JOB)) {
            return;
        }

        // Schedule the next job
        Job fetchActionDetailsJob = new Job.Builder(FETCH_ACTION_DETAILS_JOB, this, FETCH_ACTION_DETAILS_TAG)
                .setIntervalMillis(intervalInMillis)
                .setRequiredNetworkType(Job.NetworkType.NETWORK_TYPE_CONNECTED)
                .build();

        smartScheduler.addJob(fetchActionDetailsJob);
    }

    private void removeFetchActionDetailsJob() {
        if (smartScheduler == null)
            smartScheduler = SmartScheduler.getInstance(mContext);

        // Remove existing job, if any
        if (smartScheduler.contains(FETCH_ACTION_DETAILS_JOB)) {
            smartScheduler.removeJob(FETCH_ACTION_DETAILS_JOB);
        }
    }

    @Override
    public void onJobScheduled(Context context, Job job) {
        if (job != null && job.getJobId() == FETCH_ACTION_DETAILS_JOB) {
            Log.d(TAG, "FetchActionDetails Job Scheduled");
            pollForAction();
        }
    }

    public void pollForAction() {
        Log.d(TAG, "polling for action");

        removeFetchActionDetailsJob();

        if (actionList.getActiveActionIDList().size() > 0) {

            getActionDetails(actionList.getActiveActionIDList(), new ActionListCallBack() {
                @Override
                public void onSuccess(List<HTAction> actionListToBeAdded) {

                    // Add ActionList in response to be tracked
                    actionList.addActionList(actionListToBeAdded);

                    // Check if there is any active action being tracked
                    if (!actionList.areAllActionsCompleted()) {
                        scheduleFetchActionDetailsJob(FETCH_ACTION_DETAILS_INTERVAL);
                    }

                    ArrayList<String> updatedActionIDList = new ArrayList<String>();
                    if (actionListToBeAdded != null) {
                        for (HTAction action : actionListToBeAdded) {
                            if (!TextUtils.isEmpty(action.getId()))
                                updatedActionIDList.add(action.getId());
                        }
                    }

                    delegate.onFetchAction(true, updatedActionIDList);
                }

                @Override
                public void onError(Exception exception) {
                    // Check if there is any active action being tracked
                    if (!actionList.areAllActionsCompleted()) {
                        scheduleFetchActionDetailsJob(FETCH_ACTION_DETAILS_INTERVAL);
                    }
                }
            });
        }
    }

    public void getActionDetails(final List<String> actionIDList, final ActionListCallBack callBack) {
        // Counter to maintain a count of ActionIDs for which data is being fetched
        int count = 0;

        // Also, filter actions based on their current status
        StringBuilder url = new StringBuilder(API_URL + API_ENDPOINT);

        // Construct url for a List of actionIDs
        for (String actionID : actionIDList) {

            // Check if actionID is not empty & if it is not already completed
            if (!TextUtils.isEmpty(actionID)) {
                if (actionList.getAction(actionID) == null || !actionList.getAction(actionID).isCompleted()) {
                    url.append(actionID);
                    count++;

                    // Check if current item is not the last in the list
                    if (actionIDList.indexOf(actionID) != actionIDList.size() - 1) {
                        url.append(",");
                    }
                }
            }
        }

        // Check if there is any valid actionID to be fetched or not
        if (count == 0 && callBack != null) {
            callBack.onSuccess(actionList != null ? actionList.getActionList() : new ArrayList<HTAction>());
            return;
        } else {
            url.append("/detailed/");
        }

        HyperTrackGetRequest<HTAction> getNetworkRequest = new HyperTrackGetRequest<>(
                TAG, mContext, url.toString(), HyperTrackNetworkRequest.HTNetworkClient.HT_NETWORK_CLIENT_HTTP,
                HTAction.class,
                new HTNetworkResponse.Listener<HTAction>() {
                    @Override
                    public void onResponse(HTAction response) {
                        HTLog.d(TAG, "Response : " + response.toString());

                        List<HTAction> updatedActionList = new ArrayList<HTAction>();
                        updatedActionList.add(response);

                        if (callBack != null) {
                            callBack.onSuccess(updatedActionList);
                        }
                    }
                },
                new HTNetworkResponse.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error, Exception exception) {
                        if (error == null || error.networkResponse == null) {
                            if (callBack != null) {
                                callBack.onError(new RuntimeException("Failed to fetch action. Something went wrong."));
                            }

                            HTLog.e(TAG, "Failed to fetch action. Something went wrong.");
                        } else {
                            HTLog.e(TAG, "Error occurred while onErrorResponse in getActionDetails: " + error);
                            try {
                                RuntimeException runtimeException = (RuntimeException) NetworkErrorUtil.getException(error);
                                if (callBack != null) {
                                    callBack.onError(runtimeException);
                                }

                            } catch (Exception e) {
                                HTLog.e(TAG, "Exception while onErrorResponse in getActionDetails: " + e, e);
                            }
                        }
                    }
                });

        networkManager.execute(mContext, getNetworkRequest);
    }

    public void updateDestinationLocation(final String actionID, final GeoJSONLocation location, final UpdateDestinationCallback callback) {
        if (actionList == null || actionList.getAction(actionID) == null) {
            HTLog.e(TAG, "updateDestinationLocation Error: Please check if actionID is being passed correctly");
            if (callback != null)
                callback.onError(new IllegalArgumentException("Please check if actionID is being passed correctly"));
        }

        if (location == null) {
            HTLog.e(TAG, "updateDestinationLocation Error: Location is required to update Destination location");
            if (callback != null)
                callback.onError(new RuntimeException("Required Parameter: Location is required to update Destination location"));
        }

        String url = API_URL + API_ENDPOINT + actionID + "/update_destination/";

        Gson gson = HTGson.gson();
        String jsonString = gson.toJson(new UpdateDestinationRequest(location));

        HyperTrackPostRequest<HTAction> postNetworkRequest = new HyperTrackPostRequest<>(UPDATE_DESTINATION_TAG,
                mContext, url, HyperTrackNetworkRequest.HTNetworkClient.HT_NETWORK_CLIENT_HTTP,
                jsonString, HTAction.class,
                new HTNetworkResponse.Listener<HTAction>() {
                    @Override
                    public void onResponse(HTAction response) {
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

    public HTAction removeAction(String actionID) {
        if (actionList != null) {
            return actionList.removeAction(actionID);
        }

        return null;
    }

    public void invalidateActionStoreJobs() {
        removeFetchActionDetailsJob();

        // Cancel all pending calls with TAG
        networkManager.cancel(TAG);
    }

    public HTUser getUser(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return actionList.getAction(actionID).getUser();
        }

        return null;
    }

    public LatLng getDestinationLocationLatLng(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return HTActionUtils.getExpectedPlaceLatLng(actionList.getAction(actionID));
        }

        return null;
    }

    public LatLng getCompletedLocationLatLng(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return HTActionUtils.getCompletedPlaceLatLng(actionList.getAction(actionID));
        }

        return null;
    }

    public LatLng getStartLocationLatLng(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return HTActionUtils.getStartPlaceLatLng(actionList.getAction(actionID));
        }

        return null;
    }

    public Integer getEstimatedTimeOfArrival(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {

            HTAction action = actionList.getAction(actionID);

            if (action.getETA() != null) {
                long duration = action.getETA().getTime() - (new Date()).getTime();
                double durationInMinutes = Math.ceil((duration / (float) 1000) / (float) 60);

                if (durationInMinutes < 1) {
                    durationInMinutes = 1;
                }

                return (int) durationInMinutes;
            }
        }

        return null;
    }


    public Integer getActionDurationInMinutes(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return HTActionUtils.getActionDurationInMinutes(actionList.getAction(actionID));
        }

        return null;
    }

    public Double getActionDistanceInKMs(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return HTActionUtils.getActionDistanceInKMs(actionList.getAction(actionID));
        }

        return null;
    }

    public String getStatus(String actionID) {

        if (actionList != null) {
            return HTActionUtils.getActionStatus(actionList.getAction(actionID));
        }

        return null;
    }



    public String getActionDisplayStatus(String actionID) {

        if (actionList != null) {
            return HTActionUtils.getActionDisplayStatus(actionList.getAction(actionID).getActionDisplay());
        }

        return null;
    }


    public String getStartPlaceAddress(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return HTActionUtils.getStartedPlaceAddress(actionList.getAction(actionID));
        }

        return null;
    }

    public String getDestinationPlaceAddress(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return HTActionUtils.getExpectedPlaceAddress(actionList.getAction(actionID));
        }

        return null;
    }

    public String getCompletedPlaceAddress(String actionID) {

        if (actionList != null && actionList.getAction(actionID) != null) {
            return HTActionUtils.getCompletedPlaceAddress(actionList.getAction(actionID));
        }

        return null;
    }


    public boolean anyActionInStartedState() {
        return actionList != null && actionList.anyActionStarted();
    }

    public boolean isActionCompleted(String actionID) {
        return actionList != null && actionList.isActionCompleted(actionID);
    }

    public boolean isActionFinished(String actionID) {
        return actionList != null && actionList.isActionFinished(actionID);
    }
}
