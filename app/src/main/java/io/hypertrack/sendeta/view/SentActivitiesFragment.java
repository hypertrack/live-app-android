package io.hypertrack.sendeta.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.SentActivitiesAdapter;
import io.hypertrack.sendeta.adapter.callback.UserActivitiesOnClickListener;
import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.model.UserActivitiesListResponse;
import io.hypertrack.sendeta.model.UserActivityDetails;
import io.hypertrack.sendeta.model.UserActivityModel;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.TaskManager;
import io.hypertrack.sendeta.store.callback.TaskManagerListener;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 29/08/16.
 */
public class SentActivitiesFragment extends BaseFragment implements UserActivitiesOnClickListener {

    private RecyclerView inProcessRecyclerView, historyRecyclerView;
    private LinearLayout noDataLayout, inProcessActivitiesHeader, historyActivitiesHeader, historyMoreLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noDataText;

    private SentActivitiesAdapter inProcessActivitiesAdapter, historyActivitiesAdapter;
    private ArrayList<UserActivityModel> inProcessActivities, historyActivities;
    private Call<UserActivitiesListResponse> historySentActivitiesCall;

    private boolean inProcessActivitiesCallCompleted = true, historyActivitiesCallCompleted = true;
    int historyActivitiesPage = 1;

    private View.OnClickListener retryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Reset page for the data to be refreshed
            resetActivitiesData();

            getSentActivities();
        }
    };

    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // Reset page for the data to be refreshed
            resetActivitiesData();

            getSentActivities();
        }
    };

    private void resetActivitiesData() {
        historyActivitiesPage = 1;
        inProcessActivities.clear();
        historyActivities.clear();
    }

    private View.OnClickListener onMoreHistoryActivitiesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            displayLoader(true);

            SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class,
                    SharedPreferenceManager.getUserAuthToken());
            getHistoryActivities(sendETAService);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_activities, container, false);

        initRetryAndLoader(rootView);

        // Initialize NoDataLayout
        noDataLayout = (LinearLayout) rootView.findViewById(R.id.fragment_activities_no_data);
        noDataText = (TextView) rootView.findViewById(R.id.fragment_activities_no_data_text);
        noDataText.setText(R.string.sent_activities_no_data_text);

        // Initialize SwipeToRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_activities_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(swipeRefreshListener);

        // Initialize HeaderLayouts & FooterLayouts
        inProcessActivitiesHeader = (LinearLayout) rootView.findViewById(R.id.activities_in_process);
        historyActivitiesHeader = (LinearLayout) rootView.findViewById(R.id.activities_history);

        historyMoreLayout = (LinearLayout) rootView.findViewById(R.id.activities_history_more_layout);
        historyMoreLayout.setOnClickListener(onMoreHistoryActivitiesListener);

        // Initialize RecyclerViews
        inProcessRecyclerView = (RecyclerView) rootView.findViewById(R.id.activities_in_process_list);
        inProcessRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        inProcessRecyclerView.setNestedScrollingEnabled(false);

        historyRecyclerView = (RecyclerView) rootView.findViewById(R.id.activities_history_list);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        historyRecyclerView.setNestedScrollingEnabled(false);

        // Initialize Adapters
        inProcessActivitiesAdapter = new SentActivitiesAdapter(getActivity(), inProcessActivities, this);
        inProcessRecyclerView.setAdapter(inProcessActivitiesAdapter);
        historyActivitiesAdapter = new SentActivitiesAdapter(getActivity(), historyActivities, this, true);
        historyRecyclerView.setAdapter(historyActivitiesAdapter);

        inProcessActivities = new ArrayList<>();
        historyActivities = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Reset page for the data to be refreshed
        resetActivitiesData();

        getSentActivities();
    }

    private void getSentActivities() {
        if (inProcessActivitiesCallCompleted || historyActivitiesCallCompleted) {
            displayLoader(true);
        }
        hideRetryLayout();
        noDataLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        // Initialize SendETAService, if not done already
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class,
                SharedPreferenceManager.getUserAuthToken());

        // Initiate historySentActivities Call
        getHistoryActivities(sendETAService);

        // Initiate inProcessReceivedActivities Call
        getInProcessActivity();
    }

    private void getInProcessActivity() {
        inProcessActivities.clear();

        TaskManager taskManager = TaskManager.getSharedManager(getActivity());

        // Check if current trip is a Personal Trip
        if ((BuildConfig.API_KEY).equalsIgnoreCase(HyperTrack.getPublishableKey(getActivity()))) {

            HTTask activeTask = taskManager.getHyperTrackTask();
            if (activeTask != null && !HTTask.TASK_STATUS_COMPLETED.equalsIgnoreCase(activeTask.getStatus())) {

                ArrayList<UserActivityDetails> inProcessSentActivities = new ArrayList<>();
                inProcessSentActivities.add(new UserActivityDetails(activeTask.getId(), true, activeTask));

                parseUserActivityDetails(inProcessSentActivities, true);

                inProcessActivitiesHeader.setVisibility(View.VISIBLE);
                inProcessRecyclerView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                noDataLayout.setVisibility(View.GONE);

                inProcessActivitiesAdapter.setUserActivities(inProcessActivities);

                taskManager.setTaskRefreshedListener(new TaskManagerListener() {
                    @Override
                    public void OnCallback() {
                        if (getActivity() == null || getActivity().isFinishing())
                            return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update InProcessActivity
                                getInProcessActivity();
                            }
                        });
                    }
                });

                taskManager.setTaskCompletedListener(new TaskManagerListener() {
                    @Override
                    public void OnCallback() {
                        if (getActivity() == null || getActivity().isFinishing())
                            return;

                        // Call OnCompleteTask method on UI thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Update InProcessActivity
                                getInProcessActivity();
                            }
                        });
                    }
                });

                inProcessActivitiesCallCompleted = true;
                return;
            }
        }

        // Hide InProcess List Views
        inProcessActivities.clear();
        inProcessActivitiesHeader.setVisibility(View.GONE);
        inProcessRecyclerView.setVisibility(View.GONE);

        inProcessActivitiesCallCompleted = true;
        checkForNoData();
    }

    private void getHistoryActivities(SendETAService sendETAService) {
        if (historyActivitiesCallCompleted) {
            historyActivitiesCallCompleted = false;

            historySentActivitiesCall = sendETAService.getHistorySentUserActivities(historyActivitiesPage);
            historySentActivitiesCall.enqueue(new Callback<UserActivitiesListResponse>() {
                @Override
                public void onResponse(Call<UserActivitiesListResponse> call, Response<UserActivitiesListResponse> response) {
                    if (getActivity() == null || getActivity().isFinishing())
                        return;

                    if (inProcessActivitiesCallCompleted) {
                        displayLoader(false);
                    }

                    if (response.isSuccessful()) {
                        UserActivitiesListResponse activitiesListResponse = response.body();

                        if (activitiesListResponse != null) {
                            if (!TextUtils.isEmpty(activitiesListResponse.getNext())) {
                                historyActivitiesPage++;
                                historyMoreLayout.setVisibility(View.VISIBLE);
                            } else {
                                historyMoreLayout.setVisibility(View.GONE);
                            }

                            if (activitiesListResponse.getUserActivities() != null
                                    && !activitiesListResponse.getUserActivities().isEmpty()) {

                                parseUserActivityDetails(activitiesListResponse.getUserActivities(), false);

                                historyActivitiesHeader.setVisibility(View.VISIBLE);
                                historyRecyclerView.setVisibility(View.VISIBLE);

                                swipeRefreshLayout.setVisibility(View.VISIBLE);
                                noDataLayout.setVisibility(View.GONE);

                                historyActivitiesAdapter.setUserActivities(historyActivities);

                                historyActivitiesCallCompleted = true;
                                return;
                            }
                        }

                        // Hide InProcess List Views
                        historyActivities.clear();
                        historyActivitiesHeader.setVisibility(View.GONE);
                        historyRecyclerView.setVisibility(View.GONE);
                        historyMoreLayout.setVisibility(View.GONE);

                        historyActivitiesCallCompleted = true;
                        checkForNoData();
                        return;
                    }

                    showRetryLayout(getString(R.string.generic_error_message), retryListener);
                    historyActivitiesCallCompleted = true;
                }

                @Override
                public void onFailure(Call<UserActivitiesListResponse> call, Throwable t) {
                    historyActivitiesCallCompleted = true;
                    if (inProcessActivitiesCallCompleted) {
                        displayLoader(false);
                    }

                    ErrorData errorData = new ErrorData();
                    try {
                        errorData = NetworkUtils.processFailure(t);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showErrorMessage(errorData);
                }

                private void showErrorMessage(ErrorData errorData) {
                    if (getActivity() == null || getActivity().isFinishing())
                        return;

                    if (ErrorCodes.NO_INTERNET.equalsIgnoreCase(errorData.getCode()) ||
                            ErrorCodes.REQUEST_TIMED_OUT.equalsIgnoreCase(errorData.getCode())) {
                        showRetryLayout(getString(R.string.network_issue), retryListener);

                    } else {
                        showRetryLayout(getString(R.string.generic_error_message), retryListener);
                    }
                }
            });
        }
    }

    private void checkForNoData() {
        if ((inProcessActivitiesCallCompleted && inProcessActivities.isEmpty())
                && (historyActivitiesCallCompleted && historyActivities.isEmpty())) {
            noDataLayout.setVisibility(View.VISIBLE);
        }
    }

    private void parseUserActivityDetails(ArrayList<UserActivityDetails> userActivityDetailsList, boolean inProcess) {

        Activity context = getActivity();

        if (context != null && !context.isFinishing() && userActivityDetailsList != null) {
            // Parse all UserActivityDetails fetched from server
            for (UserActivityDetails userActivityDetails : userActivityDetailsList) {

                UserActivityModel userActivity = new UserActivityModel();
                userActivity.setInProcess(inProcess);

                // Get TaskDetails from UserActivityDetails
                HTTask task = userActivityDetails.getTaskDetails();
                if (task != null) {

                    // Get TaskID
                    userActivity.setTaskID(task.getId());

                    // Get Activity Title
                    if (task.getTaskDisplay() != null) {
                        Integer resId = HTTaskUtils.getTaskDisplayStatus(task.getTaskDisplay());
                        if (resId != null) {
                            userActivity.setTitle(context.getString(resId));
                        } else if (!TextUtils.isEmpty(task.getTaskDisplay().getStatusText())) {
                            userActivity.setTitle(task.getTaskDisplay().getStatusText());
                        }
                    }

                    // Get Activity CompletionAddress
                    String completionLocationAddress = HTTaskUtils.getCompletionAddress(task);
                    if (!TextUtils.isEmpty(completionLocationAddress)) {
                        userActivity.setEndAddress(completionLocationAddress);
                    }

                    if (inProcess) {

                        // Get Activity Subtitle
                        HTTaskDisplay taskDisplay = task.getTaskDisplay();
                        if (taskDisplay != null && HTTaskUtils.getTaskDisplayETA(taskDisplay) != null) {

                            String formattedTime = HTTaskUtils.getFormattedTimeString(context,
                                    HTTaskUtils.getTaskDisplayETA(taskDisplay).doubleValue());
                            if (!TextUtils.isEmpty(formattedTime)) {
                                userActivity.setSubtitle(formattedTime + " away");
                            }
                        }
                    } else {

                        // Get Activity Subtitle
                        String formattedSubtitle = HTTaskUtils.getTaskMeteringString(context, task);
                        if (!TextUtils.isEmpty(formattedSubtitle)) {
                            userActivity.setSubtitle(formattedSubtitle);
                        }

                        // Get Activity Date
                        String formattedDate = task.getTaskDateDisplayString();
                        if (!TextUtils.isEmpty(formattedDate)) {
                            userActivity.setDate(formattedDate);
                        }

                        // Get Activity StartAddress
                        String startLocationString = HTTaskUtils.getStartAddress(task);
                        if (!TextUtils.isEmpty(startLocationString)) {
                            userActivity.setStartAddress(startLocationString);
                        }

                        // Get Activity StartTime
                        if (!TextUtils.isEmpty(task.getTaskStartTimeDisplayString())) {
                            userActivity.setStartTime(task.getTaskStartTimeDisplayString());
                        }

                        // Get Activity EndTime
                        if (!TextUtils.isEmpty(task.getTaskEndTimeDisplayString())) {
                            userActivity.setEndTime(task.getTaskEndTimeDisplayString());
                        }

                        // Get Completion Location
                        LatLng completionLatLng = HTTaskUtils.getCompletionLatLng(task);
                        if (completionLatLng != null) {
                            if (completionLatLng.latitude != 0.0 && completionLatLng.longitude != 0.0) {
                                userActivity.setEndLocation(completionLatLng);
                            }
                        }

                        // Get Start Location
                        LatLng startLatLng = HTTaskUtils.getStartLatLng(task);
                        if (startLatLng != null) {
                            if (startLatLng.latitude != 0.0 && startLatLng.longitude != 0.0) {
                                userActivity.setStartLocation(startLatLng);
                            }
                        }

                        // Get Polyline
                        String encodedPolyline = task.getEncodedPolyline();
                        if (!TextUtils.isEmpty(encodedPolyline)) {
                            List<LatLng> polyline = HTMapUtils.decode(encodedPolyline);
                            if (polyline != null && !polyline.isEmpty()) {
                                userActivity.setPolyline(polyline);
                            }
                        }
                    }

                    if (inProcess) {
                        inProcessActivities.add(userActivity);
                    } else {
                        historyActivities.add(userActivity);
                    }
                }
            }
        }
    }

    private boolean isBusinessTripActive() {
        String currentPublishableKey = HyperTrack.getPublishableKey(getActivity());
        // Check if Current Selected key is for a Business Account
        if (!currentPublishableKey.equalsIgnoreCase(BuildConfig.API_KEY)) {

            // Check if a business trip is active and show an error
            if (TaskManager.getSharedManager(getActivity()).isTaskActive()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.error_tracking_while_on_business_trip);
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            }
        }

        return false;
    }

    @Override
    public void OnInProcessActivityClicked(int position, UserActivityModel inProcessActivity) {

        if (getActivity() == null || getActivity().isFinishing() || isBusinessTripActive())
            return;

        if (inProcessActivity == null || TextUtils.isEmpty(inProcessActivity.getTaskID()))
            return;

        ArrayList<String> taskIDList = new ArrayList<>();
        taskIDList.add(inProcessActivity.getTaskID());

        Intent trackTaskIntent = new Intent(getActivity(), Track.class);
        trackTaskIntent.putStringArrayListExtra(Track.KEY_TASK_ID_LIST, taskIDList);
        startActivity(trackTaskIntent);
    }

    @Override
    public void OnHistoryActivityClicked(int position, UserActivityModel historyActivity) {
        if (historyActivity == null || TextUtils.isEmpty(historyActivity.getTaskID()) || isBusinessTripActive())
            return;

        ArrayList<String> taskIDList = new ArrayList<>();
        taskIDList.add(historyActivity.getTaskID());

        Intent trackTaskIntent = new Intent(getActivity(), Track.class);
        trackTaskIntent.putStringArrayListExtra(Track.KEY_TASK_ID_LIST, taskIDList);
        startActivity(trackTaskIntent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (historyActivitiesAdapter != null) {
            for (MapView m : historyActivitiesAdapter.getMapViews()) {
                m.onLowMemory();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start Refreshing Task, if one exists
        if (TaskManager.getSharedManager(getActivity()).getHyperTrackTask() != null) {
            TaskManager.getSharedManager(getActivity()).startRefreshingTask(0);
        }

        if (historyActivitiesAdapter != null) {
            for (MapView m : historyActivitiesAdapter.getMapViews()) {
                m.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        TaskManager.getSharedManager(getActivity()).stopRefreshingTask();

        if (historyActivitiesAdapter != null) {
            for (MapView m : historyActivitiesAdapter.getMapViews()) {
                m.onPause();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (historyActivitiesAdapter != null) {
            for (MapView m : historyActivitiesAdapter.getMapViews()) {
                m.onDestroy();
            }
        }

        if (historySentActivitiesCall != null) {
            historySentActivitiesCall.cancel();
        }

        super.onDestroy();
    }
}