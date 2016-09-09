package io.hypertrack.sendeta.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.common.model.HTDriver;
import io.hypertrack.lib.common.model.HTPlace;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.common.model.HTTaskDisplay;
import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.ReceivedActivitiesAdapter;
import io.hypertrack.sendeta.adapter.callback.UserActivitiesOnClickListener;
import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.model.UserActivitiesListResponse;
import io.hypertrack.sendeta.model.UserActivityDetails;
import io.hypertrack.sendeta.model.UserActivityModel;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.TaskManager;
import io.hypertrack.sendeta.util.HyperTrackTaskUtils;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 29/08/16.
 */
public class ReceivedActivitiesFragment extends BaseFragment implements UserActivitiesOnClickListener {

    private NestedScrollView mScrollView;
    private RecyclerView inProcessRecyclerView, historyRecyclerView;
    private LinearLayout noDataLayout, inProcessActivitiesHeader, historyActivitiesHeader,
            inProcessMoreLayout, historyMoreLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noDataText;

    private ReceivedActivitiesAdapter inProcessActivitiesAdapter, historyActivitiesAdapter;
    private ArrayList<UserActivityModel> inProcessActivities, historyActivities;
    private Call<UserActivitiesListResponse> inProcessActivitiesCall, historyReceivedActivitiesCall;

    private boolean inProcessActivitiesCallCompleted = true, historyActivitiesCallCompleted = true;
    int inProcessActivitiesPage = 1, historyActivitiesPage = 1;

    private View.OnClickListener retryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Reset page for the data to be refreshed
            resetActivitiesData();

            getReceivedActivities();
        }
    };

    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            // Reset page for the data to be refreshed
            resetActivitiesData();

            getReceivedActivities();
        }
    };

    private void resetActivitiesData() {
        inProcessActivitiesPage = 1;
        historyActivitiesPage = 1;
        inProcessActivities.clear();
        historyActivities.clear();
    }

    private View.OnClickListener onMoreInProcessActivitiesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            displayLoader(true);

            SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class,
                    SharedPreferenceManager.getUserAuthToken());
            getInProcessActivities(sendETAService);
        }
    };

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
        noDataText.setText(R.string.received_activities_no_data_text);

        // Initialize SwipeToRefreshLayout
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_activities_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(swipeRefreshListener);

        // Initialize HeaderLayouts & FooterLayouts
        inProcessActivitiesHeader = (LinearLayout) rootView.findViewById(R.id.activities_in_process);
        historyActivitiesHeader = (LinearLayout) rootView.findViewById(R.id.activities_history);

        inProcessMoreLayout = (LinearLayout) rootView.findViewById(R.id.activities_in_process_more_layout);
        inProcessMoreLayout.setOnClickListener(onMoreInProcessActivitiesListener);

        historyMoreLayout = (LinearLayout) rootView.findViewById(R.id.activities_history_more_layout);
        historyMoreLayout.setOnClickListener(onMoreHistoryActivitiesListener);

        // Initialize RecyclerViews
        inProcessRecyclerView = (RecyclerView) rootView.findViewById(R.id.activities_in_process_list);
        inProcessRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        inProcessRecyclerView.setNestedScrollingEnabled(false);

        historyRecyclerView = (RecyclerView) rootView.findViewById(R.id.activities_history_list);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        historyRecyclerView.setNestedScrollingEnabled(false);

        // Initialize Scroll View
        mScrollView = (NestedScrollView) rootView.findViewById(R.id.fragment_activities_scroll_view);

        // Initialize Adapters
        inProcessActivitiesAdapter = new ReceivedActivitiesAdapter(getActivity(), inProcessActivities, this);
        inProcessRecyclerView.setAdapter(inProcessActivitiesAdapter);
        historyActivitiesAdapter = new ReceivedActivitiesAdapter(getActivity(), historyActivities, this);
        historyRecyclerView.setAdapter(historyActivitiesAdapter);

        inProcessActivities = new ArrayList<>();
        historyActivities = new ArrayList<>();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Reset page for the data to be refreshed
        resetActivitiesData();

        getReceivedActivities();

        // Scroll User's Received Activities to top by default
        mScrollView.smoothScrollTo(0, 0);
    }

    private void getReceivedActivities() {
        if (inProcessActivitiesCallCompleted || historyActivitiesCallCompleted) {
            displayLoader(true);
        }
        hideRetryLayout();
        noDataLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        // Initialize SendETAService, if not done already
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class,
                SharedPreferenceManager.getUserAuthToken());

        // Initiate inProcessReceivedActivities Call
        getInProcessActivities(sendETAService);

        // Initiate historyReceivedActivities Call
        getHistoryActivities(sendETAService);
    }

    private void getInProcessActivities(SendETAService sendETAService) {
        if (inProcessActivitiesCallCompleted) {
            inProcessActivitiesCallCompleted = false;

            inProcessActivitiesCall = sendETAService.getInProcessReceivedUserActivities(inProcessActivitiesPage);
            inProcessActivitiesCall.enqueue(new Callback<UserActivitiesListResponse>() {
                @Override
                public void onResponse(Call<UserActivitiesListResponse> call, Response<UserActivitiesListResponse> response) {
                    if (historyActivitiesCallCompleted) {
                        displayLoader(false);
                    }

                    if (response.isSuccessful()) {
                        UserActivitiesListResponse activitiesListResponse = response.body();

                        if (activitiesListResponse != null) {
                            if (!TextUtils.isEmpty(activitiesListResponse.getNext())) {
                                inProcessActivitiesPage++;
                                inProcessMoreLayout.setVisibility(View.VISIBLE);
                            } else {
                                inProcessMoreLayout.setVisibility(View.GONE);
                            }

                            if (activitiesListResponse.getUserActivities() != null
                                    && !activitiesListResponse.getUserActivities().isEmpty()) {

                                parseUserActivityDetails(activitiesListResponse.getUserActivities(), true);

                                inProcessActivitiesHeader.setVisibility(View.VISIBLE);
                                inProcessRecyclerView.setVisibility(View.VISIBLE);

                                swipeRefreshLayout.setVisibility(View.VISIBLE);
                                noDataLayout.setVisibility(View.GONE);

                                inProcessActivitiesAdapter.setUserActivities(inProcessActivities);

                                inProcessActivitiesCallCompleted = true;
                                return;
                            }
                        }

                        // Hide InProcess List Views
                        inProcessActivities.clear();
                        inProcessActivitiesHeader.setVisibility(View.GONE);
                        inProcessRecyclerView.setVisibility(View.GONE);

                        checkForNoData();

                        inProcessActivitiesCallCompleted = true;
                        return;
                    }

                    showRetryLayout(getString(R.string.generic_error_message), retryListener);
                    inProcessActivitiesCallCompleted = true;
                }

                @Override
                public void onFailure(Call<UserActivitiesListResponse> call, Throwable t) {
                    inProcessActivitiesCallCompleted = true;
                    if (historyActivitiesCallCompleted) {
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

    private void getHistoryActivities(SendETAService sendETAService) {
        if (historyActivitiesCallCompleted) {
            historyActivitiesCallCompleted = false;

            historyReceivedActivitiesCall = sendETAService.getHistoryReceivedUserActivities(historyActivitiesPage);
            historyReceivedActivitiesCall.enqueue(new Callback<UserActivitiesListResponse>() {
                @Override
                public void onResponse(Call<UserActivitiesListResponse> call, Response<UserActivitiesListResponse> response) {
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

                        checkForNoData();

                        historyActivitiesCallCompleted = true;
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

    private void parseUserActivityDetails(ArrayList<UserActivityDetails> userActivityDetailsDetails, boolean inProcess) {

        Activity context = getActivity();

        if (context != null && !context.isFinishing() && userActivityDetailsDetails != null) {

            // Parse all UserActivityDetails fetched from server
            for (UserActivityDetails userActivityDetails : userActivityDetailsDetails) {

                UserActivityModel userActivity = new UserActivityModel();
                userActivity.setInProcess(inProcess);

                // Get TaskDetails from UserActivityDetails
                HTTask task = userActivityDetails.getTaskDetails();
                if (task != null) {

                    // Get TaskID
                    userActivity.setTaskID(task.getId());

                    HTDriver driver = task.getDriver();
                    if (task.getDriver() != null) {
                        // Get Activity Title
                        if (!TextUtils.isEmpty(driver.getName())) {
                            userActivity.setTitle(task.getDriver().getName());
                        }

                        // Get Activity MainIcon
                        if (!TextUtils.isEmpty(driver.getPhotoURL())) {
                            userActivity.setDriverImageURL(driver.getPhotoURL());
                        }
                    }

                    // Get Activity EndAddress
                    HTPlace destination = task.getDestination();
                    if (destination != null && !TextUtils.isEmpty(destination.getAddress())) {
                        userActivity.setEndAddress(destination.getAddress());
                    }

                    if (inProcess) {
                        // Get Activity Subtitle
                        HTTaskDisplay taskDisplay = task.getTaskDisplay();
                        if (taskDisplay != null) {
                            String formattedTime = HyperTrackTaskUtils.getFormattedTimeString(context,
                                    HyperTrackTaskUtils.getTaskDisplayETA(taskDisplay));
                            if (!TextUtils.isEmpty(formattedTime)) {
                                userActivity.setSubtitle(formattedTime + " away");
                            }
                        }
                    } else {

                        // Get Activity Subtitle
                        String formattedSubtitle = HyperTrackTaskUtils.getFormattedTaskDurationAndDistance(context, task);
                        if (!TextUtils.isEmpty(formattedSubtitle)) {
                            userActivity.setSubtitle(formattedSubtitle);
                        }

                        // Get Activity Date
                        String formattedDate = HyperTrackTaskUtils.getTaskDateString(task);
                        if (!TextUtils.isEmpty(formattedDate)) {
                            userActivity.setDate(formattedDate);
                        }

                        // Get Activity StartAddress
                        String startLocationString =
                                task.getStartLocation() != null ? task.getStartLocation().getDisplayString() : null;
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
        if (inProcessActivity == null || TextUtils.isEmpty(inProcessActivity.getTaskID()) || isBusinessTripActive())
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
    public void onDestroy() {
        super.onDestroy();

        if (inProcessActivitiesCall != null) {
            inProcessActivitiesCall.cancel();
        }

        if (historyReceivedActivitiesCall != null) {
            historyReceivedActivitiesCall.cancel();
        }
    }
}