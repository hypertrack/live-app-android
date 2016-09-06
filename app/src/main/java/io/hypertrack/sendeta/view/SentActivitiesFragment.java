package io.hypertrack.sendeta.view;

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

import java.util.ArrayList;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.SentActivitiesAdapter;
import io.hypertrack.sendeta.adapter.callback.UserActivitiesOnClickListener;
import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.model.UserActivitiesListResponse;
import io.hypertrack.sendeta.model.UserActivity;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 29/08/16.
 */
public class SentActivitiesFragment extends BaseFragment implements UserActivitiesOnClickListener{
    private RecyclerView inProcessRecyclerView, historyRecyclerView;
    private LinearLayout noDataLayout, inProcessActivitiesHeader, historyActivitiesHeader;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noDataText;

    private SentActivitiesAdapter inProcessActivitiesAdapter, historyActivitiesAdapter;

    private ArrayList<UserActivity> inProcessActivities;
    private ArrayList<UserActivity> historyActivities;

    private Call<UserActivitiesListResponse> inProcessSentActivitiesCall;
    private Call<UserActivitiesListResponse> historySentActivitiesCall;

    private boolean inProcessActivitiesCallCompleted = true, historyActivitiesCallCompleted = true;

    private View.OnClickListener retryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getSentActivities();
        }
    };

    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            getSentActivities();
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

        // Initialize HeaderLayouts
        inProcessActivitiesHeader = (LinearLayout) rootView.findViewById(R.id.activities_in_process);
        historyActivitiesHeader = (LinearLayout) rootView.findViewById(R.id.activities_history);

        // Initialize RecyclerViews
        inProcessRecyclerView = (RecyclerView) rootView.findViewById(R.id.activities_in_process_list);
        inProcessRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        historyRecyclerView = (RecyclerView) rootView.findViewById(R.id.activities_history_list);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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
    public void onResume() {
        super.onResume();

        getSentActivities();

        if (historyActivitiesAdapter != null) {
            for (MapView m : historyActivitiesAdapter.getMapViews()) {
                m.onResume();
            }
        }
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


        // Initiate inProcessReceivedActivities Call
        if (inProcessActivitiesCallCompleted) {
            inProcessActivitiesCallCompleted = false;

            inProcessSentActivitiesCall = sendETAService.getInProcessSentUserActivities();
            inProcessSentActivitiesCall.enqueue(new Callback<UserActivitiesListResponse>() {
                @Override
                public void onResponse(Call<UserActivitiesListResponse> call, Response<UserActivitiesListResponse> response) {
                    if (historyActivitiesCallCompleted) {
                        displayLoader(false);
                    }

                    if (response.isSuccessful()) {

                        UserActivitiesListResponse activitiesListResponse = response.body();
                        if (activitiesListResponse != null && activitiesListResponse.getUserActivities() != null
                                && !activitiesListResponse.getUserActivities().isEmpty()) {

                            inProcessActivities = activitiesListResponse.getUserActivities();
                            inProcessActivitiesHeader.setVisibility(View.VISIBLE);
                            inProcessRecyclerView.setVisibility(View.VISIBLE);

                            swipeRefreshLayout.setVisibility(View.VISIBLE);
                            noDataLayout.setVisibility(View.GONE);

                            inProcessActivitiesAdapter.setUserActivities(inProcessActivities);

                            inProcessActivitiesCallCompleted = true;
                            return;
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

        // Initiate historySentActivities Call
        if (historyActivitiesCallCompleted) {
            historyActivitiesCallCompleted = false;

            historySentActivitiesCall = sendETAService.getHistorySentUserActivities();
            historySentActivitiesCall.enqueue(new Callback<UserActivitiesListResponse>() {
                @Override
                public void onResponse(Call<UserActivitiesListResponse> call, Response<UserActivitiesListResponse> response) {
                    if (inProcessActivitiesCallCompleted) {
                        displayLoader(false);
                    }

                    if (response.isSuccessful()) {

                        UserActivitiesListResponse activitiesListResponse = response.body();
                        if (activitiesListResponse != null && activitiesListResponse.getUserActivities() != null
                                && !activitiesListResponse.getUserActivities().isEmpty()) {

                            historyActivities = activitiesListResponse.getUserActivities();
                            historyActivitiesHeader.setVisibility(View.VISIBLE);
                            historyRecyclerView.setVisibility(View.VISIBLE);

                            swipeRefreshLayout.setVisibility(View.VISIBLE);
                            noDataLayout.setVisibility(View.GONE);

                            historyActivitiesAdapter.setUserActivities(historyActivities);

                            historyActivitiesCallCompleted = true;
                            return;
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

    @Override
    public void OnInProcessActivityClicked(UserActivity inProcessActivity) {
        if (inProcessActivity == null || TextUtils.isEmpty(inProcessActivity.getTaskID()))
            return;

        ArrayList<String> taskIDList = new ArrayList<>();
        taskIDList.add(inProcessActivity.getTaskID());

        Intent trackTaskIntent = new Intent(getActivity(), Track.class);
        trackTaskIntent.putStringArrayListExtra(Track.KEY_TASK_ID_LIST, taskIDList);
        startActivity(trackTaskIntent);
    }

    @Override
    public void OnHistoryActivityClicked(UserActivity historyActivity) {
        if (historyActivity == null || TextUtils.isEmpty(historyActivity.getTaskID()))
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
    public void onPause() {
        super.onPause();

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

        if (inProcessSentActivitiesCall != null) {
            inProcessSentActivitiesCall.cancel();
        }

        if (historySentActivitiesCall != null) {
            historySentActivitiesCall.cancel();
        }

        super.onDestroy();
    }
}
