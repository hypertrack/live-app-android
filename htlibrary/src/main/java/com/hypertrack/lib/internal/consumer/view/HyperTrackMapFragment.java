package com.hypertrack.lib.internal.consumer.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.ConsumerClient;
import com.hypertrack.lib.internal.consumer.models.CustomMarker;
import com.hypertrack.lib.internal.consumer.models.HTTask;
import com.hypertrack.lib.internal.consumer.models.HTUser;
import com.hypertrack.lib.internal.consumer.models.MapViewModel;
import com.hypertrack.lib.internal.consumer.models.TaskNavigatorCallback;
import com.hypertrack.lib.internal.consumer.models.TaskNavigatorList;
import com.hypertrack.lib.internal.consumer.models.UpdateDestinationCallback;
import com.hypertrack.lib.internal.consumer.utils.AnimationUtils;
import com.hypertrack.lib.internal.consumer.utils.ConsumerConstants;
import com.hypertrack.lib.internal.consumer.utils.HTCircleImageView;
import com.hypertrack.lib.internal.consumer.utils.HTDownloadImageTask;
import com.hypertrack.lib.internal.consumer.utils.HTMapUtils;
import com.hypertrack.lib.models.Place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class provides a MapInterface to track one or more tasks in real-time.
 * This provides you a reference to the fragment included in your Activity.
 * <p>
 * The map fragment can be controlled by passing an instance of a MapAdapter
 * implementation. This instance needs to be set by calling {@link #setHTMapAdapter(MapAdapter)}.
 * <p>
 * Updates for HyperTrackMapFragment can be received by setting a
 * {@link HyperTrackMapFragment#setMapFragmentCallback(MapFragmentCallback)}
 * instance as a parameter to the {@link #setMapFragmentCallback(MapFragmentCallback)} method.
 */
public class HyperTrackMapFragment extends Fragment implements MapUpdateInterface, TaskNavigatorCallback,
        OnMapReadyCallback, TouchableWrapper.TouchActionDown, TouchableWrapper.TouchActionUp {

    private static final String TAG = HyperTrackMapFragment.class.getSimpleName();
    // Data
    private Timer etaTimer;
    // Views
    private TextView userName;
    private HTCircleImageView userProfileImage;
    private ImageButton userContactNumber;
    private RelativeLayout userInfoLayout;
    private ImageButton ctaButton;
    private AsyncTask<String, Void, Bitmap> userImageDownloadTask;
    private RelativeLayout addressInfoLayout;
    private RelativeLayout taskSummaryLayout;
    private RelativeLayout taskStartLayout;
    private RelativeLayout taskEndPlaceLayout;
    private TextView taskDurationTextView;
    private TextView taskDateTextView;
    private TextView taskStartAddressTextView;
    private TextView taskEndAddressTextView;
    private TextView taskStartTimeTextView;
    private TextView taskEndTimeTextView;
    private View taskVerticalSeparator;
    private View taskEndPlaceConfirmDialogVerticalLine;
    private ImageView taskEndEditPlaceDismiss;
    private ImageView taskEndEditPlaceConfirmDialogTriangle;
    private ImageView taskEndPlaceEditIcon;
    private RelativeLayout taskEndPlaceEditLayout;
    private TextView taskEndPlaceConfirmDialogTitle;
    private TextView taskEndPlaceConfirmDialogMessage;
    private FrameLayout taskEndPlaceConfirmLayout;
    private LinearLayout taskEndPlaceConfirmBtn;
    private LinearLayout taskEndPlaceConfirmDialog;
    private LinearLayout mapLayoutLoaderLayout;
    private ProgressBar mapLayoutLoader;
    private View view;
    private Toolbar orderStatusToolbar;
    private ImageButton setBoundsButton;
    private ImageView etaIcon;
    private TextView etaTimeTextView, etaTimeSuffixTextView;
    private ConsumerClient consumerClient;
    private TaskNavigatorList taskNavigatorList;
    private MapViewModelList mapViewModelList;
    private GoogleMap mMap;
    private boolean isMapLoaded = false;
    private Marker multipleTasksDestinationMarker;
    private Marker markerSelectedOnClick;
    private String taskIDSelectedOnMarkerClick;
    private Map<String, CustomMarker> customMarkers;
    private boolean userInteractionDisabledMapBounds = false, editDestinationViewVisible = false;
    private String editDestinationTaskID = "";
    /**
     * Callback for HyperTrackMapFragment to receive user action events
     *
     * @see
     */
    private MapFragmentCallback mapFragmentCallback;
    /**
     * Adapter for HyperTrackMapFragment to customize Map View and Markers
     *
     * @see MapAdapter
     */
    private MapAdapter mapAdapter;
    private BroadcastReceiver mOrderStatusChangedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(ConsumerClient.INTENT_EXTRA_TASK_ID_LIST)) {
                ArrayList<String> taskIDList = intent.getStringArrayListExtra(ConsumerClient.INTENT_EXTRA_TASK_ID_LIST);

                if (taskIDList != null && mapAdapter != null) {

                    onUpdateAllTasks(taskIDList);

                    if (!editDestinationViewVisible && !TextUtils.isEmpty(taskIDSelectedOnMarkerClick)) {
                        HyperTrackMapFragment.this.showUserAndAddressInfo(taskIDSelectedOnMarkerClick);
                        HyperTrackMapFragment.this.updateOrderStatusToolbar(taskIDSelectedOnMarkerClick);
                    } else {
                        // Reset Address Info View to its default state
                        HyperTrackMapFragment.this.resetUserAndAddressInfo();
                        HyperTrackMapFragment.this.resetDestinationMarker();
                        HyperTrackMapFragment.this.updateOrderStatusToolbar(null);
                    }
                }
            }
        }
    };
    private BroadcastReceiver mTaskDetailRefreshedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(ConsumerClient.INTENT_EXTRA_TASK_ID_LIST)) {
                ArrayList<String> taskIDList = intent.getStringArrayListExtra(ConsumerClient.INTENT_EXTRA_TASK_ID_LIST);

                if (taskIDList != null) {
                    // Update the TaskInfo to Map View UI
                    onUpdateAllTasks(taskIDList);

                    if (!TextUtils.isEmpty(taskIDSelectedOnMarkerClick)) {
                        updateOrderStatusToolbar(taskIDSelectedOnMarkerClick);
                    } else {
                        // Reset OrderStatus Toolbar to its default state
                        updateOrderStatusToolbar(null);
                    }
                }
            }
        }
    };


    ///--------------------------------------
    /// Methods
    ///--------------------------------------
    private BroadcastReceiver mTaskRemovedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(ConsumerClient.INTENT_EXTRA_TASK_ID_LIST)) {
                ArrayList<String> taskIDList = intent.getStringArrayListExtra(ConsumerClient.INTENT_EXTRA_TASK_ID_LIST);

                if (taskIDList != null && taskIDList.size() > 0) {
                    // Remove the MapViewModels & TaskNavigators for these removed TaskIDs
                    for (String taskID : taskIDList) {
                        if (mapViewModelList != null)
                            mapViewModelList.removeMapViewModelForTaskID(taskID);

                        taskNavigatorList.removeNavigator(taskID);
                    }

                    // Notify the updates to Map View UI
                    notifyChanged();
                }
            }
        }
    };

    public HyperTrackMapFragment() {
    }

    /**
     * Call this method to get Map Object
     */
    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * Call this method to enable setting of bounds for the Map View
     */
    public void resetMapBounds() {
        this.resetFlags();
        this.bindMapView();
    }

    /**
     * Call this method to disable setting of bounds for the Map View
     */
    public void disableMapBounds() {
        mapViewModelList.setShouldBindView(false);
    }

    /**
     * Call this method to set MapAdapter for HyperTrackMapFragment
     * IMPORTANT: An Adapter extending MapAdapter needs to be set to HyperTrackMapFragment using this method.
     *
     * @param mapAdapter mapAdapter can be used to customize settings for Map View,
     *                     Map Markers, Order Info Views and User Info Views.
     */
    public void setHTMapAdapter(MapAdapter mapAdapter) {
        if (mapAdapter == null)
            throw new RuntimeException("Required Parameter: MapAdapter cannot be null");

        if (getActivity() == null || getActivity().isFinishing())
            throw new RuntimeException("Activity is finishing. Could not set MapAdapter");

        this.mapAdapter = mapAdapter;
        this.mapAdapter.setMapUpdateInterface(this);
        this.mapAdapter.setConsumerClient(consumerClient);
        this.mapAdapter.setMapViewModelList(mapViewModelList);
        this.mapAdapter.setMapFragmentCallback(this.mapFragmentCallback);
    }

    /**
     * Call this method to set MapFragmentCallback for HyperTrackMapFragment.
     * IMPORTANT: This Callback will provide listener methods for Map Markers and Views.
     *
     * @param mapFragmentCallback mapFragmentCallback instance with required callback methods implemented.
     */
    public void setMapFragmentCallback(MapFragmentCallback mapFragmentCallback) {
        this.mapFragmentCallback = mapFragmentCallback;

        if (mapAdapter != null) {
            mapAdapter.setMapFragmentCallback(this.mapFragmentCallback);
        }
    }

    /**
     * Call this method to get the instance of HyperTrackMapFragment's OrderStatus Toolbar.
     * NOTE: OrderStatus toolbar will be null if call to super.onCreateView() is not done before
     * this method is called.
     * <p>
     * OrderStatus Toolbar is to reflect OrderStatus of the current order being tracked.
     * <p>
     * For SINGLE ACTIVE ORDER being tracked, OrderStatus will be displayed on the Toolbar.
     * For MULTIPLE ACTIVE ORDERS being tracked, OrderStatus of a given taskID will be displayed
     * when markers corresponding to that taskID are clicked.
     * <p>
     * IMPORTANT: Inorder to change the OrderStatus Toolbar theme to support your App Theme,
     * add a style resource containing desired Toolbar Style Settings in your styles.xml
     * with the name "HTOrderStatusToolbarTheme". This theme in your styles.xml file will override
     * OrderStatus Toolbar's default style settings.
     *
     * @return Order Status Toolbar instance for HyperTrackMapFragment. Will be null if toolbar has not
     * been instantiated yet.
     */
    public Toolbar getOrderStatusToolbar() {
        return orderStatusToolbar;
    }

    /**
     * Call this method to add a Custom Marker to HyperTrackMapFragment
     *
     * @param markerOptions Pass instance of MarkerOptions defining the Marker properties. Refer to
     *                      <a href="https://developers.google.com/android/reference/com/google/android/gms/maps/model/MarkerOptions">
     *                      Google MarkerOptions documentation</a> for more info.
     * @return Custom Marker instance added to HyperTrackMapFragment
     */
    public Marker addCustomMarker(@NonNull MarkerOptions markerOptions) {
        if (mMap == null)
            return null;

        Marker marker = mMap.addMarker(markerOptions);
        if (marker != null) {
            customMarkers.put(marker.getId(), new CustomMarker(markerOptions, marker));

            if (mapFragmentCallback != null)
                mapFragmentCallback.onCustomMarkerAdded(HyperTrackMapFragment.this, marker);

            // Reset Map Bounds
            if (mapAdapter.shouldBindView() && !userInteractionDisabledMapBounds) {
                bindMapView();
            }
        }

        return marker;
    }

    /**
     * Method to get Custom Marker for a given MarkerID
     *
     * @param markerID Marker ID for the marker to be fetched
     * @return Returns the custom marker for the given MarkerId if it exists, null otherwise
     */
    public Marker getCustomMarker(String markerID) {
        if (TextUtils.isEmpty(markerID))
            return null;

        CustomMarker customMarker = customMarkers.get(markerID);
        return customMarker != null ? customMarker.getMarker() : null;
    }

    /**
     * Method to get List of all Custom Markers
     *
     * @return Returns a list of custom markers if they exist, null otherwise
     */
    public List<Marker> getAllCustomMarkers() {
        List<Marker> customMarkersList = new ArrayList<>();
        if (customMarkers.size() == 0)
            return null;

        for (CustomMarker customMarker : customMarkers.values()) {
            if (customMarker != null && customMarker.getMarker() != null)
                customMarkersList.add(customMarker.getMarker());
        }

        return customMarkersList;
    }

    /**
     * Call this method to remove a Custom Marker, for a given MarkerId
     *
     * @return Returns true if the custom marker is removed, false if Map is null.
     */
    public boolean removeCustomMarker(String markerID) {
        if (!TextUtils.isEmpty(markerID) && customMarkers.get(markerID) != null) {

            CustomMarker customMarker = customMarkers.get(markerID);
            return !(customMarker != null && customMarker.getMarker() != null) || removeCustomMarker(customMarker.getMarker());
        }

        return false;
    }

    /**
     * Call this method to remove a Custom Marker added to HyperTrackMapFragment
     *
     * @return Returns true if the custom marker is removed, false if Map is null.
     */
    public boolean removeCustomMarker(@NonNull Marker marker) {
        if (customMarkers.get(marker.getId()) != null) {

            CustomMarker customMarker = customMarkers.get(marker.getId());
            if (customMarker != null && customMarker.getMarker() != null) {

                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onCustomMarkerRemoved(HyperTrackMapFragment.this, customMarker.getMarker());
                }

                customMarker.getMarker().remove();
            }

            return true;
        }

        return false;
    }

    /**
     * Call this method to clear all custom markers from HyperTrackMapFragment
     *
     * @return Returns true if all custom markers are removed, false if Map is null.
     */
    public boolean clearCustomMarkers() {
        if (mMap != null) {
            for (String markerID : customMarkers.keySet()) {
                CustomMarker customMarker = customMarkers.get(markerID);

                if (customMarker != null && customMarker.getMarker() != null) {
                    removeCustomMarker(customMarker.getMarker());
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Call this method to get Hero Marker for a given Task
     *
     * @param taskID The TaskID for which the Hero Marker needs to be returned.
     * @return Hero Marker Instance for the given taskID
     */
    public Marker getHeroMarker(String taskID) {
        if (mapViewModelList == null || TextUtils.isEmpty(taskID)) {
            return null;
        }

        return mapViewModelList.getHeroMarkerForTaskID(taskID);
    }

    /**
     * Call this method to get a List of Hero Markers for given Tasks
     *
     * @param taskIDList The List of TaskIDs for which the Hero Marker needs to be returned.
     * @return List of Hero Marker Instances for the given List of taskIDs
     */
    public List<Marker> getHeroMarker(List<String> taskIDList) {
        List<Marker> heroMarkerList = new ArrayList<>();

        if (taskIDList != null && taskIDList.size() > 0) {

            for (String taskID : taskIDList) {

                if (getHeroMarker(taskID) != null) {
                    heroMarkerList.add(getHeroMarker(taskID));
                }
            }

            return heroMarkerList;
        }

        return null;
    }

    /**
     * Call this method to get Destination Marker for a given Task
     *
     * @param taskID The TaskID for which the Destination Marker needs to be returned.
     * @return Destination Marker Instance for the given taskID
     */
    public Marker getDestinationMarker(String taskID) {
        if (mapViewModelList == null || TextUtils.isEmpty(taskID)) {
            return null;
        }

        return mapViewModelList.getDestinationMarkerForTaskID(taskID);
    }

    /**
     * Call this method to get a List of Destination Markers for given Tasks
     *
     * @param taskIDList The List of TaskIDs for which the Destination Marker needs to be returned.
     * @return List of Destination Marker Instances for the given List of taskIDs
     */
    public List<Marker> getDestinationMarker(List<String> taskIDList) {
        List<Marker> destinationMarkerList = new ArrayList<>();

        if (taskIDList != null && taskIDList.size() > 0) {

            for (String taskID : taskIDList) {

                if (getDestinationMarker(taskID) != null) {
                    destinationMarkerList.add(getDestinationMarker(taskID));
                }
            }

            return destinationMarkerList;
        }

        return null;
    }

    /**
     * Call this method to get Source Marker for a given Task
     *
     * @param taskID The TaskID for which the Source Marker needs to be returned.
     * @return Source Marker Instance for the given taskID
     */
    public Marker getSourceMarker(String taskID) {
        if (mapViewModelList == null || TextUtils.isEmpty(taskID)) {
            return null;
        }

        return mapViewModelList.getSourceMarkerForTaskID(taskID);
    }

    /**
     * Call this method to get a List of Source Markers for given Tasks
     *
     * @param taskIDList The List of TaskIDs for which the Source Marker needs to be returned.
     * @return List of Source Marker Instances for the given List of taskIDs
     */
    public List<Marker> getSourceMarker(List<String> taskIDList) {
        List<Marker> sourceMarkerList = new ArrayList<>();

        if (taskIDList != null && taskIDList.size() > 0) {

            for (String taskID : taskIDList) {

                if (getSourceMarker(taskID) != null) {
                    sourceMarkerList.add(getSourceMarker(taskID));
                }
            }

            return sourceMarkerList;
        }

        return null;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Reset EditDestination Visible Flag
        editDestinationViewVisible = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        initializeMapFragment();
        setHTMapAdapter(new MapAdapter(getContext()));
    }

    public void initializeMapFragment() {
        if (consumerClient == null)
            consumerClient = ConsumerClient.getInstance();

        if (mapViewModelList == null)
            mapViewModelList = new MapViewModelList();

        if (taskNavigatorList == null)
            taskNavigatorList = new TaskNavigatorList(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Check if Task StatusToolbar is enabled
        if (mapAdapter.showOrderStatusToolbar(this) && getActivity() != null && !getActivity().isFinishing()
                && getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(orderStatusToolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(com.hypertrack.lib.R.layout.fragment_map, container, false);

        TouchableSupportMapFragment mapFragment = (TouchableSupportMapFragment) getChildFragmentManager().findFragmentById(com.hypertrack.lib.R.id.map);
        mapFragment.setCallback(this);
        mapFragment.getMapAsync(this);

        // Initialize customMarkers map
        customMarkers = new HashMap<>();

        initViews();
        return view;
    }

    private void initViews() {
        mapLayoutLoaderLayout = (LinearLayout) view.findViewById(com.hypertrack.lib.R.id.mapLayout_loader_parent);
        mapLayoutLoader = (ProgressBar) view.findViewById(com.hypertrack.lib.R.id.mapLayout_loader);
        mapLayoutLoader.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        orderStatusToolbar = (Toolbar) view.findViewById(com.hypertrack.lib.R.id.task_status_bar);

        userName = (TextView) view.findViewById(com.hypertrack.lib.R.id.userName);
        userProfileImage = (HTCircleImageView) view.findViewById(com.hypertrack.lib.R.id.userProfileImage);
        userContactNumber = (ImageButton) view.findViewById(com.hypertrack.lib.R.id.userContactNumber);
        userInfoLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.userInfoLayout);

        ctaButton = (ImageButton) view.findViewById(com.hypertrack.lib.R.id.ctaButton);

        setBoundsButton = (ImageButton) view.findViewById(com.hypertrack.lib.R.id.setBoundsButton);
        setBoundsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset UserInfoLayout & AddressInfoLayout Change their visibility
                HyperTrackMapFragment.this.resetUserAndAddressInfo();
                HyperTrackMapFragment.this.resetDestinationMarker();
                HyperTrackMapFragment.this.resetTaskSummaryInfoViews();

                // Reset the selected marker object
                markerSelectedOnClick = null;
                taskIDSelectedOnMarkerClick = null;

                // Reset OrderStatus Toolbar to its default state
                updateOrderStatusToolbar(null);

                setBoundsButton.setVisibility(View.GONE);
                userInteractionDisabledMapBounds = false;
                bindMapView();
            }
        });

        setBoundsButton.setVisibility(View.GONE);

        initAddressInfoViews();

        initEditDestinationViews();
    }

    private void initAddressInfoViews() {
        addressInfoLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.task_info_layout);

        taskSummaryLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.task_summary_layout);
        taskStartLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.task_start_place_layout);
        taskVerticalSeparator = view.findViewById(com.hypertrack.lib.R.id.task_separator_vertical);

        taskDateTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.task_date);
        taskDurationTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.task_duration);

        taskStartAddressTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.start_place_address);
        taskStartTimeTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.task_start_time);

        taskEndPlaceLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.task_end_place_layout);
        taskEndAddressTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.end_place_address);
        taskEndTimeTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.task_end_time);

        taskEndPlaceEditIcon = (ImageView) view.findViewById(com.hypertrack.lib.R.id.task_end_place_edit_icon);
        taskEndEditPlaceDismiss = (ImageView) view.findViewById(com.hypertrack.lib.R.id.task_end_place_edit_dismiss);
        taskEndPlaceEditLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.task_end_place_edit_layout);
    }

    private void initEditDestinationViews() {
        taskEndPlaceConfirmLayout = (FrameLayout) view.findViewById(com.hypertrack.lib.R.id.task_end_place_confirm_layout);
        taskEndPlaceConfirmBtn = (LinearLayout) view.findViewById(com.hypertrack.lib.R.id.task_end_place_confirm_btn);
        taskEndPlaceConfirmDialog = (LinearLayout) view.findViewById(com.hypertrack.lib.R.id.task_end_place_confirm_dialog);
        taskEndPlaceConfirmDialogTitle = (TextView) view.findViewById(com.hypertrack.lib.R.id.task_end_place_confirm_dialog_title);
        taskEndPlaceConfirmDialogMessage = (TextView) view.findViewById(com.hypertrack.lib.R.id.task_end_place_confirm_dialog_message);

        taskEndEditPlaceConfirmDialogTriangle = (ImageView) view.findViewById(com.hypertrack.lib.R.id.task_end_place_confirm_dialog_triangle);
        taskEndPlaceConfirmDialogVerticalLine = view.findViewById(com.hypertrack.lib.R.id.task_end_place_confirm_dialog_vertical_line);
    }

    @Override
    public void notifyChanged() {
        List<String> taskIDList = mapAdapter.taskIDsToTrack(this);
        if (consumerClient == null || mapViewModelList == null || mMap == null) {
            return;
        }

        // Return if Activity is finishing
        if (getActivity() == null || getActivity().isFinishing())
            return;

        // Clear the MapViewModelList (Markers & Task Specific Settings)
        mapViewModelList.clearAllMapViewModels();

        // Clear the Map
        if (mMap != null) {
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onMapWillClear(this, mMap);
            }

            mMap.clear();
        }

        // Setup CustomMarkers on notifyChanged call
        setupCustomMarkers();

        taskNavigatorList.removeNavigatorsOtherThanTaskIDs(taskIDList);

        // Reset Traffic Layer Visibility
        this.mMap.setTrafficEnabled(mapAdapter.showTrafficLayer(this));

        // Reset the Order StatusToolbar to its default state
        updateOrderStatusToolbar(null);

        for (String taskID : taskIDList) {
            this.onUpdateTask(taskID);
        }

        // Reset UserInfoLayout & AddressInfoLayout & Change their visibility
        // Important: This is done after onUpdateTask has been called to handle special case of
        // Single Task Tracking.
        this.resetUserAndAddressInfo();
        this.resetDestinationMarker();
        this.resetTaskSummaryInfoViews();

        // Reset Map Bounds
        if (mapAdapter.shouldBindView() && !userInteractionDisabledMapBounds) {
            bindMapView();
        }

        // Send MapCleared callback
        if (mMap != null && mapFragmentCallback != null) {
            mapFragmentCallback.onMapCleared(this, mMap);
        }
    }

    private void setupCustomMarkers() {

        for (String customMarkerID : customMarkers.keySet()) {

            CustomMarker customMarker = customMarkers.get(customMarkerID);

            if (customMarker != null && customMarker.getMarkerOptions() != null) {
                // Update customMarkers with the updated Marker instance
                customMarkers.put(customMarkerID, new CustomMarker(customMarker.getMarkerOptions(),
                        mMap.addMarker(customMarker.getMarkerOptions())));

                // Callback for the customMarkers added
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onCustomMarkerAdded(HyperTrackMapFragment.this,
                            customMarkers.get(customMarkerID).getMarker());
            }
        }
    }

    @Override
    public void moveToLocationWithTimeInterval(String taskID, LatLng toPosition, long timeDuration) {
        if (mapViewModelList == null || mapAdapter == null || !this.shouldMoveHeroMarker(taskID, toPosition)) {
            return;
        }

        LatLng fromPosition = null;
        Marker heroMarker = mapViewModelList.getHeroMarkerForTaskID(taskID);

        if (heroMarker == null) {
            heroMarker = this.setupHeroMarker(taskID, toPosition, null, true);
        } else {
            fromPosition = heroMarker.getPosition();
            this.moveMarker(taskID, heroMarker, toPosition, timeDuration);
        }

        if (heroMarker != null && mapAdapter.rotateHeroMarker(this, taskID)) {
            float bearing = bearingBetweenLatLngs(fromPosition, toPosition);
            heroMarker.setRotation(bearing);
        }

        if (heroMarker != null && this.mapFragmentCallback != null) {
            this.mapFragmentCallback.onHeroMarkerWillMove(HyperTrackMapFragment.this, taskID, heroMarker, toPosition);
        }

        if (mapAdapter.shouldBindView() && !userInteractionDisabledMapBounds) {
            if (TextUtils.isEmpty(taskIDSelectedOnMarkerClick)) {
                this.bindMapView();
            } else {
                this.bindMapView(taskIDSelectedOnMarkerClick);
            }
        }
    }

    private boolean shouldMoveHeroMarker(String taskID, LatLng toLocation) {
        if (mapViewModelList.getHeroMarkerForTaskID(taskID) == null) {
            return true;
        }

        LatLng fromLocation = mapViewModelList.getHeroMarkerForTaskID(taskID).getPosition();
        double distance = calculateDistance(fromLocation, toLocation);

        return distance > MapFragmentConstants.THRESHOLD_DISTANCE;
    }

    private double calculateDistance(LatLng fromLoc, LatLng toLoc) {

        float results[] = new float[1];

        try {
            Location.distanceBetween(fromLoc.latitude, fromLoc.longitude, toLoc.latitude, toLoc.longitude, results);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (results[0] < 0)
            results[0] = 0;

        return results[0];
    }

    private float bearingBetweenLatLngs(LatLng begin, LatLng end) {
        if (begin == null) {
            return -1000;
        }

        Location beginL = convertLatLngToLocation(begin);
        Location endL = convertLatLngToLocation(end);

        return beginL.bearingTo(endL);
    }

    private Marker setupHeroMarker(String taskID, LatLng toPosition, Float bearing, boolean fireAddedCallback) {
        if (consumerClient == null || mapViewModelList == null || mapAdapter == null)
            return null;

        // Check if Task is null
        if (consumerClient.taskForTaskID(taskID) == null)
            return null;

        // Check if TaskSummary is enabled and it has to be displayed
        if (mapAdapter.showTaskSummaryForTaskID(this, taskID)
                && consumerClient.showTaskSummaryForTaskStatus(taskID)) {
            return null;
        }

        if (toPosition != null) {
            // Remove Hero Marker if visible
            removeHeroMarker(taskID);

            // Get HeroMarker's Anchors from MapAdapter
            float[] anchors = null;
            if (mapAdapter != null)
                anchors = mapAdapter.getHeroMarkerAnchorValues(this, taskID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.5f};
            }

            mapViewModelList.setHeroMarkerForTaskID(taskID,
                    mMap.addMarker(new MarkerOptions()
                            .anchor(anchors[0], anchors[1])
                            .position(toPosition)));

            if (bearing != null && mapAdapter.rotateHeroMarker(this, taskID)) {
                mapViewModelList.getHeroMarkerForTaskID(taskID).setRotation(bearing);
            }

            this.setHeroMarkerView(taskID);

            // Callback for Hero Marker Added
            if (fireAddedCallback && mapFragmentCallback != null) {
                mapFragmentCallback.onHeroMarkerAdded(this, taskID, mapViewModelList.getHeroMarkerForTaskID(taskID));
            }
        }

        return mapViewModelList.getHeroMarkerForTaskID(taskID);
    }

    private void setHeroMarkerView(String taskID) {

        if (mapAdapter != null && mapViewModelList.getHeroMarkerForTaskID(taskID) != null) {

            BitmapDescriptor icon;

            if (mapAdapter.getHeroMarkerViewForTaskID(this, taskID) != null) {
                View heroMarkerView = mapAdapter.getHeroMarkerViewForTaskID(this, taskID);
                Bitmap bitmap = createDrawableFromView(getContext(), heroMarkerView);
                if (bitmap == null)
                    return;

                icon = BitmapDescriptorFactory.fromBitmap(bitmap);
            } else {
                icon = BitmapDescriptorFactory.fromResource(mapAdapter.getHeroMarkerIconForTaskID(this, taskID));
            }

            mapViewModelList.getHeroMarkerForTaskID(taskID).setIcon(icon);
        }
    }

    private void bindMapView() {
        if (mMap == null || mapAdapter == null || consumerClient == null)
            return;

        if (userInteractionDisabledMapBounds || markerSelectedOnClick != null || !TextUtils.isEmpty(taskIDSelectedOnMarkerClick))
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int points = 0;

        List<String> taskIDsToTrack = mapAdapter.taskIDsToTrack(this);

        // Include TaskMarkers in LatLngBounds, if any
        if (taskIDsToTrack != null && mapViewModelList != null) {
            for (String taskID : mapAdapter.taskIDsToTrack(this)) {
                points += addLocationsToLatLngBuilder(builder, taskID);
            }
        }

        // Include CustomMarkers in LatLngBounds, if any
        bindCustomMarkers(builder);

        // Include MultipleDestinationMarker in LatLngBounds, if any
        if (multipleTasksDestinationMarker != null) {
            builder.include(multipleTasksDestinationMarker.getPosition());
            points++;
        }

        // Don't animate map if there were no points to be included or mapNotLoaded
        if (points == 0 || !isMapLoaded)
            return;

        LatLngBounds bounds = builder.build();
        CameraUpdate cu;
        if (points == 1) {
            // Set Zoom Level to 15 in case of a single point in LatLngBounds
            cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 15);
        } else {
            cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        }

        mMap.animateCamera(cu);
    }

    private void bindCustomMarkers(LatLngBounds.Builder builder) {
        for (CustomMarker customMarker : customMarkers.values()) {

            if (customMarker != null && customMarker.getMarker() != null &&
                    customMarker.getMarker().getPosition() != null) {
                builder.include(customMarker.getMarker().getPosition());
            }
        }
    }

    private void bindMapView(String taskID) {
        if (mMap == null || mapAdapter == null || consumerClient == null)
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int points = 0;

        List<String> taskIDsToTrack = mapAdapter.taskIDsToTrack(this);

        if (taskIDsToTrack != null && taskIDsToTrack.contains(taskID) && mapViewModelList != null) {
            points += addLocationsToLatLngBuilder(builder, taskID);
        }

        if (points == 0 || !isMapLoaded) {
            return;
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cu;
        if (points == 1) {
            // Set Zoom Level to 15 in case of a single point in LatLngBounds
            cu = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 15);
        } else {
            cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        }
        mMap.animateCamera(cu);
    }

    private int addLocationsToLatLngBuilder(LatLngBounds.Builder builder, String taskID) {
        int points = 0;

        if (mapViewModelList.getMapViewModelForTaskID(taskID) != null) {
            if (mapViewModelList.getHeroMarkerForTaskID(taskID) != null) {
                LatLng location = mapViewModelList.getHeroMarkerForTaskID(taskID).getPosition();
                builder.include(location);
                points++;
            }

            if (mapViewModelList.getDestinationMarkerForTaskID(taskID) != null) {
                LatLng location = mapViewModelList.getDestinationMarkerForTaskID(taskID).getPosition();
                builder.include(location);
                points++;
            }

            if (!mapAdapter.disableDynamicZoom(this) && mapViewModelList.getSourceMarkerForTaskID(taskID) != null) {
                LatLng location = mapViewModelList.getSourceMarkerForTaskID(taskID).getPosition();
                builder.include(location);
                points++;
            }

            if (mapAdapter.showTaskSummaryForTaskID(this, taskID) && consumerClient.showTaskSummaryForTaskStatus(taskID)) {
                if (mapViewModelList.getTaskSummaryStartMarkerForTaskID(taskID) != null) {
                    LatLng location = mapViewModelList.getTaskSummaryStartMarkerForTaskID(taskID).getPosition();
                    builder.include(location);
                    points++;
                }

                if (mapViewModelList.getTaskSummaryEndMarkerForTaskID(taskID) != null) {
                    LatLng location = mapViewModelList.getTaskSummaryEndMarkerForTaskID(taskID).getPosition();
                    builder.include(location);
                    points++;
                }

                List<LatLng> polyline = mapViewModelList.getTaskSummaryPolylineForTaskID(taskID);
                if (polyline != null && polyline.size() > 0) {
                    for (LatLng latLng : polyline) {
                        builder.include(latLng);
                        points++;
                    }
                }
            }
        }

        return points;
    }

    private Location convertLatLngToLocation(LatLng latLng) {
        Location loc = new Location("someLoc");
        loc.setLatitude(latLng.latitude);
        loc.setLongitude(latLng.longitude);
        return loc;
    }

    private void moveMarker(final String taskID, final Marker marker, final LatLng toPosition, final float speedDuration) {
        if (marker == null) {
            return;
        }

        if (speedDuration == 0) {
            marker.setPosition(toPosition);
            return;
        }

        // Remove all existing animations in progress
        taskNavigatorList.getTaskAnimationHandler(taskID).removeCallbacksAndMessages(null);

        MarkerAnimation.animateMarker(taskNavigatorList.getTaskAnimationHandler(taskID), marker, toPosition, speedDuration);
    }

    @Override
    public void onTouchDown(MotionEvent event) {

        if (editDestinationViewVisible) {
            taskEndPlaceConfirmDialog.setVisibility(View.GONE);
            taskEndPlaceConfirmLayout.setVisibility(View.GONE);
            taskEndEditPlaceConfirmDialogTriangle.setVisibility(View.GONE);
            taskEndPlaceConfirmDialogVerticalLine.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTouchUp(MotionEvent event) {

        // Show EditDestination Views, in case visible
        if (editDestinationViewVisible) {
            AnimationUtils.expand(taskEndPlaceConfirmDialog, 100);
            AnimationUtils.expand(taskEndPlaceConfirmLayout, 100);

            taskEndEditPlaceConfirmDialogTriangle.setVisibility(View.VISIBLE);
            taskEndPlaceConfirmDialogVerticalLine.setVisibility(View.VISIBLE);
        } else {
            // Handle Set Bounds Button
            setBoundsButton.setVisibility(View.VISIBLE);
            userInteractionDisabledMapBounds = true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (mMap != null) {
            if (mapAdapter != null && mapAdapter.getMapFragmentInitialState(this) != null) {
                mMap.moveCamera(mapAdapter.getMapFragmentInitialState(this));
            }

            this.setMapGestures();
            this.resetMapPadding();

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    // Handle Marker Clicks (not in case EditDestination Overlay is visible)
                    if (!editDestinationViewVisible) {
                        userInteractionDisabledMapBounds = false;
                        handleMarkerClick(marker);
                        markerSelectedOnClick = marker;
                    }

                    return true;
                }
            });

            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    isMapLoaded = true;
                    if (consumerClient != null && mapAdapter.taskIDsToTrack(HyperTrackMapFragment.this) != null) {
                        // Notify the updates to Map View UI
                        notifyChanged();
                    }

                    if (mapFragmentCallback != null)
                        mapFragmentCallback.onMapLoadedCallback(HyperTrackMapFragment.this, mMap);
                }
            });

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (!editDestinationViewVisible &&
                            (!TextUtils.isEmpty(taskIDSelectedOnMarkerClick) || markerSelectedOnClick != null)) {

                        // Reset UserInfoLayout & AddressInfoLayout Change their visibility
                        HyperTrackMapFragment.this.resetUserAndAddressInfo();
                        HyperTrackMapFragment.this.resetTaskSummaryInfoViews();

                        // Reset OrderStatus Toolbar to its default state
                        updateOrderStatusToolbar(null);

                        if (setBoundsButton != null)
                            setBoundsButton.performClick();

                        if (mapFragmentCallback != null)
                            mapFragmentCallback.onMapClick(HyperTrackMapFragment.this, mMap);
                    }
                }
            });

            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (mMap.getMyLocation() == null && getActivity() != null && !getActivity().isFinishing()) {
                        if (mapAdapter != null && !TextUtils.isEmpty(editDestinationTaskID)
                                && mapAdapter.shouldShowUserLocationMissingAlert(HyperTrackMapFragment.this, editDestinationTaskID)) {
                            Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_task_destination_my_location_error_msg, Toast.LENGTH_SHORT).show();
                        }

                        // Callback for User Location Error Received
                        if (mapFragmentCallback != null)
                            mapFragmentCallback.onReceiveUserLocationMissingError(HyperTrackMapFragment.this,
                                    editDestinationTaskID, getActivity().getString(com.hypertrack.lib.R.string.edit_task_destination_my_location_error_msg));
                    }

                    if (mapFragmentCallback != null)
                        mapFragmentCallback.onMapMyLocationButtonClick(HyperTrackMapFragment.this, mMap);
                    return false;
                }
            });
        }

        if (mapFragmentCallback != null)
            mapFragmentCallback.onMapReadyCallback(HyperTrackMapFragment.this, mMap);
    }

    private void handleMarkerClick(Marker marker) {
        if (consumerClient == null || mapViewModelList == null || mapAdapter == null || mapViewModelList.getMapViewModelList() == null) {
            return;
        }

        String lastTaskIDSelectedOnMarkerClick = null;
        if (!TextUtils.isEmpty(taskIDSelectedOnMarkerClick)) {
            lastTaskIDSelectedOnMarkerClick = taskIDSelectedOnMarkerClick;
        }

        boolean destinationMarkerClicked = false;

        // Check if the marker clicked exists in MapViewModelList
        for (String taskID : mapViewModelList.getMapViewModelList().keySet()) {
            MapViewModel mapViewModel = mapViewModelList.getMapViewModelForTaskID(taskID);

            // Handle Hero Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getHeroMarkerId())) {

                if (mapFragmentCallback != null)
                    mapFragmentCallback.onHeroMarkerClicked(this, taskID, marker);

                taskIDSelectedOnMarkerClick = taskID;
                break;
            }

            // Handle Source Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getSourceMarkerId())) {
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onSourceMarkerClicked(this, taskID, marker);

                taskIDSelectedOnMarkerClick = taskID;
                break;
            }

            // Handle Destination Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getDestinationMarkerId())) {
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onDestinationMarkerClicked(this, taskID, marker);

                taskIDSelectedOnMarkerClick = taskID;
                destinationMarkerClicked = true;
                break;
            }

            // Handle TaskSummaryStart Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getTaskSummaryStartMarkerId())) {
                taskIDSelectedOnMarkerClick = taskID;
                break;
            }

            // Handle TaskSummaryEnd Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getTaskSummaryEndMarkerId())) {
                taskIDSelectedOnMarkerClick = taskID;
                break;
            }
        }

        if (multipleTasksDestinationMarker != null && marker.equals(multipleTasksDestinationMarker)) {
            if (mapFragmentCallback != null)
                mapFragmentCallback.onMultipleTasksDestinationMarkerClicked(this, multipleTasksDestinationMarker);
        }

        // Remove DestinationMarker for last selected task
        // Check for TaskID as we don't want to remove destination marker if a marker was clicked which
        // is not mentioned above
        if (!TextUtils.isEmpty(taskIDSelectedOnMarkerClick)) {
            if (!destinationMarkerClicked) {
                HyperTrackMapFragment.this.removeAllDestinationMarkers();
            }

            // Not a valid Task marker, so fire callback for custom marker click
        } else if (mapFragmentCallback != null) {
            mapFragmentCallback.onCustomMarkerClicked(HyperTrackMapFragment.this, marker);
        }

        // Show User & Address Info for TaskIDSelectedOnMarkerClick
        if (!TextUtils.isEmpty(taskIDSelectedOnMarkerClick)) {

            // Check if the current selected marker is for the same taskID as the last selected marker
            if (!taskIDSelectedOnMarkerClick.equalsIgnoreCase(lastTaskIDSelectedOnMarkerClick)) {
                showUserAndAddressInfo(taskIDSelectedOnMarkerClick);
                updateOrderStatusToolbar(taskIDSelectedOnMarkerClick);
                removeMultipleTasksDestinationMarker();
            }

            // Check if destination marker has been enabled for this taskID
            if (mapAdapter.showDestinationMarkerForTaskID(this, taskIDSelectedOnMarkerClick)) {
                LatLng destinationLocation = consumerClient.getDestinationLocationLatLng(taskIDSelectedOnMarkerClick);
                setupDestinationMarker(taskIDSelectedOnMarkerClick, destinationLocation);
            }

            // Refocus map if the user has not disabled map bounds
            if (!userInteractionDisabledMapBounds) {
                bindMapView(taskIDSelectedOnMarkerClick);
                userInteractionDisabledMapBounds = true;
            }
        } else {

            // No Marker matched the selected one Check if a different marker was selected than the current one
            // Reset UserInfoLayout & AddressInfoLayout Change their visibility
            HyperTrackMapFragment.this.resetUserAndAddressInfo();
            HyperTrackMapFragment.this.resetDestinationMarker();
            HyperTrackMapFragment.this.resetTaskSummaryInfoViews();
        }
    }

    private void showUserAndAddressInfo(String taskID) {
        if (mapAdapter != null) {

            // Get the User Info Data if User View has to be shown
            if (mapAdapter.showUserInfoForTaskID(HyperTrackMapFragment.this, taskID)) {
                this.updateUserInformation(taskID);

            } else {
                // Hide UserInfoLayout as per the setting for current taskID
                this.setUserInfoLayoutVisibility(taskID, false);
            }

            // Set AddressInfoLayout Visibility
            this.updateAddressInfo(taskID);
        }
    }

    private void updateUserInformation(String taskID) {

        if (consumerClient.getUser(taskID) != null) {

            // Remove UserImage Download Task
            if (userImageDownloadTask != null) {
                userImageDownloadTask.cancel(true);
                userImageDownloadTask = null;
            }

            HTUser user = consumerClient.getUser(taskID);

            if (user.getName() != null) {
                userName.setText(user.getName());

                if (user.getPhone() == null)
                    userContactNumber.setImageResource(com.hypertrack.lib.R.drawable.ic_ht_call_button_disabled);

                userProfileImage.setImageResource(com.hypertrack.lib.R.drawable.ht_profile_image);

                if (user.getPhotoURL() != null) {
                    userImageDownloadTask = new HTDownloadImageTask(userProfileImage)
                            .execute(user.getPhotoURL());
                }

                // Hide UserInfoLayout as per the setting for current taskID
                this.setUserInfoLayoutVisibility(taskID, true);
                return;
            }
        }

        // Hide UserInfoLayout as per the setting for current taskID
        this.setUserInfoLayoutVisibility(taskID, false);
    }

    private void setUserInfoLayoutVisibility(final String taskID, boolean showLayout) {

        if (userInfoLayout == null)
            return;

        if (!showLayout) {
            userInfoLayout.setVisibility(View.GONE);

            if (userProfileImage != null) {
                userProfileImage.setVisibility(View.GONE);
            }

            if (userContactNumber != null) {
                userContactNumber.setVisibility(View.GONE);
                userContactNumber.setOnClickListener(null);
            }

            if (ctaButton != null) {
                ctaButton.setVisibility(View.GONE);
                ctaButton.setOnClickListener(null);
            }
        } else {
            if (taskID == null)
                return;

            userInfoLayout.setVisibility(View.VISIBLE);

            if (userProfileImage != null) {
                userProfileImage.setVisibility(View.VISIBLE);
            }

            if (userContactNumber != null) {
                if (mapAdapter.showCallButtonForTaskID(this, taskID)) {
                    userContactNumber.setVisibility(View.VISIBLE);
                    userContactNumber.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getActivity() == null || getActivity().isFinishing() ||
                                    consumerClient == null || consumerClient.getUser(taskID) == null) {
                                return;
                            }

                            HTUser user = consumerClient.getUser(taskID);
                            if (!TextUtils.isEmpty(user.getPhone())) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + user.getPhone()));
                                startActivity(intent);
                            }

                            if (mapFragmentCallback != null)
                                mapFragmentCallback.onCallButtonClicked(HyperTrackMapFragment.this, taskID);
                        }
                    });
                } else {
                    userContactNumber.setVisibility(View.GONE);
                    userContactNumber.setOnClickListener(null);
                }
            }

            if (ctaButton != null) {
                if (mapAdapter.showOrderDetailsButtonForTaskID(this, taskID)) {
                    ctaButton.setVisibility(View.VISIBLE);
                    ctaButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mapFragmentCallback != null)
                                mapFragmentCallback.onOrderDetailsButtonClicked(HyperTrackMapFragment.this, taskID);
                        }
                    });
                } else {
                    ctaButton.setVisibility(View.GONE);
                    ctaButton.setOnClickListener(null);
                }
            }
        }

        if (taskID != null) {
            if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
                mapViewModelList.getMapViewModelForTaskID(taskID).setUserInfoVisible(showLayout);

            this.updateMapPadding(taskID);
        } else {
            resetMapPadding();
        }
    }

    private void showMultipleTasksUserInfo() {
        if (getActivity() == null || getActivity().isFinishing() || mapAdapter == null)
            return;

        // Check if UserInfo Layout has been hidden for Multiple Tasks view
        if (mapAdapter.showUserInfoForMultipleTasksView(this)) {

            // Show UserInfo Layout with default text
            userName.setText(getString(com.hypertrack.lib.R.string.multiple_task_tracking_user_info_text));
            userInfoLayout.setVisibility(View.VISIBLE);

            // Hide taskID specific views from UserInfo Layout
            userProfileImage.setVisibility(View.GONE);
            userContactNumber.setVisibility(View.GONE);
            ctaButton.setVisibility(View.GONE);
        } else {
            userInfoLayout.setVisibility(View.GONE);
        }
    }

    private void showMultipleTasksAddressInfo() {
        if (getActivity() == null || getActivity().isFinishing() || mapAdapter == null || consumerClient == null
                || consumerClient.getTaskIDList() == null || consumerClient.getTaskIDList().size() < 1)
            return;

        // Check if AddressInfo Layout has been hidden for Multiple Tasks view
        if (mapAdapter.showAddressInfoViewForMultipleTasksView(this)) {

            // Fetch taskID for first task being tracked
            String taskID = consumerClient.getTaskIDList().get(0);
            HTTask task = consumerClient.taskForTaskID(taskID);

            if (task != null && task.getDestination() != null
                    && task.getDestination().getDisplayString() != null) {
                this.taskEndAddressTextView.setText(task.getDestination().getDisplayString());

                this.setAddressInfoVisibility(taskID, true);
                return;
            }
            this.setAddressInfoVisibility(taskID, false);
        } else {
            addressInfoLayout.setVisibility(View.GONE);
        }
    }

    private void resetDestinationMarker() {
        if (!editDestinationViewVisible) {

            if (mapAdapter == null || consumerClient == null || consumerClient.getTaskIDList() == null
                    || consumerClient.getTaskIDList().isEmpty())
                return;

            if (mapAdapter.getTaskIDsToTrackCount(this) > 1) {
                HyperTrackMapFragment.this.removeAllDestinationMarkers();

                if (mapAdapter.showMultipleTasksDestinationMarker(this)) {
                    String taskID = mapAdapter.taskIDsToTrack(this).get(0);
                    LatLng destinationLocation = consumerClient.getDestinationLocationLatLng(taskID);
                    setupMultipleTasksDestinationMarker(taskID, destinationLocation);
                    return;
                }
            } else if (mapAdapter.getTaskIDsToTrackCount(this) == 1) {
                HyperTrackMapFragment.this.removeAllDestinationMarkers();

                String taskID = mapAdapter.taskIDsToTrack(this).get(0);
                if (!TextUtils.isEmpty(taskID) && mapAdapter.showDestinationMarkerForTaskID(this, taskID)) {
                    LatLng destinationLocation = consumerClient.getDestinationLocationLatLng(taskID);
                    setupDestinationMarker(taskID, destinationLocation);
                }
            }

            this.removeMultipleTasksDestinationMarker();
        }
    }

    private boolean checkIfAnyTaskIsActive(List<String> taskIDList) {
        if (consumerClient == null || taskIDList == null || taskIDList.isEmpty())
            return false;

        for (String taskID : taskIDList) {
            HTTask task = consumerClient.taskForTaskID(taskID);
            if (!(task.isCompleted() || task.isCanceled())) {
                return true;
            }
        }

        return false;
    }

    private void setupMultipleTasksDestinationMarker(String taskID, LatLng location) {
        if (mapAdapter == null)
            return;

        // Setup Destination Marker Only if EditDestination Overlay is not visible
        if (!editDestinationViewVisible) {

            if (location == null) {
                return;
            }

            // Remove MultipleTasks DestinationMarker
            this.removeMultipleTasksDestinationMarker();

            // Get MultipleTasks DestinationMarker's Anchors from MapAdapter
            float[] anchors = null;
            anchors = mapAdapter.getMultipleTasksDestinationMarkerAnchorValues(this, taskID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.915f};
            }

            View markerView = mapAdapter.getMultipleTasksDestinationMarkerView(this);

            if (markerView != null) {
                Bitmap markerBitmap = createDrawableFromView(getContext(), markerView);
                if (markerBitmap != null) {
                    multipleTasksDestinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .anchor(anchors[0], anchors[1])
                            .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));

                    return;
                }

                // Callback for Destination Marker Added
                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onMultipleTasksDestinationMarkerAdded(this, multipleTasksDestinationMarker);
                }
            }

            multipleTasksDestinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .anchor(anchors[0], anchors[1])
                    .icon(BitmapDescriptorFactory.fromResource(
                            mapAdapter.getMultipleTasksDestinationMarkerIcon(this))));

            // Callback for Destination Marker Added
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onMultipleTasksDestinationMarkerAdded(this, multipleTasksDestinationMarker);
            }
        }
    }

    private void updateOrderStatusToolbar(String taskID) {
        if (mapAdapter == null || orderStatusToolbar == null || consumerClient == null || !this.isAdded()
                || getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (mapAdapter.showOrderStatusToolbar(this)) {
            AnimationUtils.expand(orderStatusToolbar, 200);

            // Handle Order StatusToolbar for Multiple Task Tracking with single destination
            if (TextUtils.isEmpty(taskID) && mapAdapter.getTaskIDsToTrackCount(this) > 1) {
                int count = mapAdapter.getTaskIDsToTrackCount(this);
                if (count > 1) {

                    String multipleTasksTitle = mapAdapter.getMultipleTasksOrderStatusToolbarTitle(this);
                    if (TextUtils.isEmpty(multipleTasksTitle)) {
                        multipleTasksTitle = count + " " + getString(com.hypertrack.lib.R.string.multiple_task_tracking_suffix);
                    }

                    orderStatusToolbar.setTitle(multipleTasksTitle);
                    orderStatusToolbar.setSubtitle("");
                }

            } else {
                // Handle Order StatusToolbar for Special Case of Single Task Tracking
                if (mapAdapter.getTaskIDsToTrackCount(this) == 1) {
                    taskID = mapAdapter.taskIDsToTrack(this) != null ? mapAdapter.taskIDsToTrack(this).get(0) : null;
                }

                orderStatusToolbar.setTitle(getOrderStatusToolbarTitle(taskID));
                orderStatusToolbar.setSubtitle(consumerClient.getTaskDisplaySubStatus(getActivity(), taskID));
            }

        } else {
            AnimationUtils.collapse(orderStatusToolbar, 200);
        }
    }

    private String getOrderStatusToolbarTitle(String taskID) {
        if (getActivity() == null || getActivity().isFinishing() || consumerClient == null ||
                TextUtils.isEmpty(taskID) || TextUtils.isEmpty(consumerClient.getTaskDisplayStatus(taskID)))
            return mapAdapter.getOrderStatusToolbarDefaultTitle(this);

        Integer resourceId = consumerClient.getStatusTextResourceIdForToolbar(taskID);
        if (resourceId == null) {
            if (!TextUtils.isEmpty(consumerClient.getTaskDisplayStatusText(taskID))) {
                return consumerClient.getTaskDisplayStatusText(taskID);
            }
            return mapAdapter.getOrderStatusToolbarDefaultTitle(this);
        }

        return getString(resourceId);
    }

    private void setMapGestures() {

        // Enable MyLocation Button on Map
        if (mapAdapter != null && checkForLocationPermission()) {
            mMap.setMyLocationEnabled(mapAdapter.setMyLocationEnabled(HyperTrackMapFragment.this));
            mMap.getUiSettings().setMyLocationButtonEnabled(mapAdapter.setMyLocationButtonEnabled(HyperTrackMapFragment.this));
        }

        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
    }

    private void hideInfoWindows() {

        if (consumerClient == null || mapViewModelList == null || mapAdapter == null) {
            return;
        }

        for (String taskID : mapAdapter.taskIDsToTrack(this)) {
            if (mapViewModelList.getHeroMarkerForTaskID(taskID) != null) {
                mapViewModelList.getHeroMarkerForTaskID(taskID).hideInfoWindow();
            }

            if (mapViewModelList.getDestinationMarkerForTaskID(taskID) != null) {
                mapViewModelList.getDestinationMarkerForTaskID(taskID).hideInfoWindow();
            }

            if (mapViewModelList.getSourceMarkerForTaskID(taskID) != null) {
                mapViewModelList.getSourceMarkerForTaskID(taskID).hideInfoWindow();
            }
        }
    }

    private void onUpdateAllTasks(List<String> taskIDList) {
        if (mapAdapter == null || consumerClient == null)
            return;

        List<String> taskIDsToTrack;

        if (taskIDList == null || taskIDList.isEmpty()) {
            taskIDList = mapAdapter.taskIDsToTrack(HyperTrackMapFragment.this);
            taskIDsToTrack = taskIDList;

            if (taskIDList == null || taskIDList.isEmpty())
                return;
        } else {
            taskIDsToTrack = mapAdapter.taskIDsToTrack(HyperTrackMapFragment.this);
        }

        for (String taskID : taskIDList) {
            // Update task Data if current taskID exists in taskIDsToTrack List
            if (taskIDsToTrack.contains(taskID)) {
                onUpdateTask(taskID);
            }
        }

        if (taskIDsToTrack.size() == 1 && taskIDList.size() == 1 && mapAdapter.showDestinationMarkerForTaskID(this, taskIDList.get(0))) {
            LatLng destinationLocation = consumerClient.getDestinationLocationLatLng(taskIDList.get(0));
            setupDestinationMarker(taskIDList.get(0), destinationLocation);
        }

        // Reset Map Bounds
        if (mapAdapter.shouldBindView() && !userInteractionDisabledMapBounds) {
            bindMapView();
        }
    }

    private void onUpdateTask(String taskID) {
        if (getActivity() == null || getActivity().isFinishing())
            return;

        if (mMap == null || mapAdapter == null || consumerClient == null || consumerClient.getStatus(taskID) == null)
            return;

        if (mapViewModelList.getMapViewModelForTaskID(taskID) == null) {
            mapViewModelList.addMapViewModelForTaskID(taskID);
        }

        this.updateMapMarkers(taskID);

        if (consumerClient.taskForTaskID(taskID) != null) {

            // Check if TaskSummary has to be displayed
            if (mapAdapter.showTaskSummaryForTaskID(this, taskID) && consumerClient.showTaskSummaryForTaskStatus(taskID)) {
                // Stop the timer to update ETA displayed on DestinationMarker
                stopETATimer();

                // Check if TaskSummary view has been enabled
                if (mapAdapter.showTaskSummaryForTaskID(this, taskID)) {
                    // Remove HeroMarker on taskCompletion
                    removeHeroMarker(taskID);
                    updateMapWithTaskSummary(taskID);
                }
            } else {
                // Start the timer to update ETA displayed on DestinationMarker
                startETATimer(taskID);

                // Process updated Task's TimeAwarePolyline for navigation
                taskNavigatorList.processTimeAwarePolyline(consumerClient.taskForTaskID(taskID));
            }
        }
    }

    private void updateMapMarkers(String taskID) {

        if (consumerClient == null || mMap == null || mapViewModelList == null)
            return;

        if (mapAdapter.showSourceMarkerForTaskID(this, taskID)) {
            setupSourceMarker(taskID);
        }
    }

    private void setupSourceMarker(String taskID) {
        if (consumerClient == null || mMap == null || mapAdapter == null)
            return;

        LatLng location = consumerClient.getSourceLocationLatLng(taskID);
        if (location == null) {
            return;
        }

        if (mapViewModelList.getSourceMarkerForTaskID(taskID) == null) {
            BitmapDescriptor sourceMarkerIcon = null;

            if (mapAdapter.getSourceMarkerViewForTaskID(this, taskID) != null) {
                View sourceMarkerView = mapAdapter.getSourceMarkerViewForTaskID(this, taskID);
                if (createDrawableFromView(getContext(), sourceMarkerView) != null) {
                    sourceMarkerIcon = BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), sourceMarkerView));
                }
            } else {
                sourceMarkerIcon = BitmapDescriptorFactory.fromResource(mapAdapter.getSourceMarkerIconForTaskID(this, taskID));
            }

            // Get SourceMarker's Anchors from MapAdapter
            float[] anchors = null;
            if (mapAdapter != null)
                anchors = mapAdapter.getSourceMarkerAnchorValues(this, taskID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 1.0f};
            }

            if (sourceMarkerIcon != null) {
                mapViewModelList.setSourceMarkerForTaskID(taskID,
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .anchor(anchors[0], anchors[1])
                                .icon(sourceMarkerIcon)));

                // Callback for Source Marker Added
                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onSourceMarkerAdded(this, taskID, mapViewModelList.getSourceMarkerForTaskID(taskID));
                }
            }

            return;
        }

        if (mapViewModelList.getSourceMarkerForTaskID(taskID).getPosition().equals(location)) {
            return;
        }

        mapViewModelList.getSourceMarkerForTaskID(taskID).setPosition(location);
    }

    private void setupDestinationMarker(String taskID, LatLng location) {
        if (mMap == null || mapAdapter == null || consumerClient == null || TextUtils.isEmpty(taskID)
                || location == null)
            return;

        if (consumerClient.taskForTaskID(taskID) == null)
            return;

        // Check if TaskSummary is enabled
        if (mapAdapter.showTaskSummaryForTaskID(this, taskID) && consumerClient.showTaskSummaryForTaskStatus(taskID))
            return;

        // Setup Destination Marker Only if EditDestination Overlay is not visible
        if (!editDestinationViewVisible) {

            // Remove Destination Marker in all scenarios
            this.removeMultipleTasksDestinationMarker();
            this.removeAllDestinationMarkers();

            // Get DestinationMarker's Anchors from MapAdapter
            float[] anchors = mapAdapter.getDestinationMarkerAnchorValues(this, taskID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.915f};
            }

            View markerView = this.getCustomMarkerView(taskID);

            if (markerView != null && createDrawableFromView(getContext(), markerView) != null) {
                mapViewModelList.setDestinationMarkerForTaskID(taskID,
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .anchor(anchors[0], anchors[1])
                                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), markerView)))));

                // Callback for Destination Marker Added
                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onDestinationMarkerAdded(this, taskID, mapViewModelList.getDestinationMarkerForTaskID(taskID));
                }
                return;
            }

            // Get MultipleTasksDestinationMarker's Anchors from MapAdapter
            anchors = mapAdapter.getMultipleTasksDestinationMarkerAnchorValues(this, taskID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.915f};
            }

            // Add MultipleTasksViewMarker in case a valid DestinationMarkerView was not set or if ETA was not valid
            mapViewModelList.setDestinationMarkerForTaskID(taskID,
                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .anchor(anchors[0], anchors[1])
                            .icon(BitmapDescriptorFactory.fromResource(mapAdapter.getMultipleTasksDestinationMarkerIcon(this)))));

            // Callback for Destination Marker Added
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onDestinationMarkerAdded(this, taskID, mapViewModelList.getDestinationMarkerForTaskID(taskID));
            }
        }
    }

    private View getCustomMarkerView(String taskID) {
        if (consumerClient == null || getActivity() == null || getActivity().isFinishing())
            return null;

        View marker = mapAdapter.getDestinationMarkerViewForTaskID(this, taskID);
        this.etaTimeTextView = (TextView) marker.findViewById(com.hypertrack.lib.R.id.eta_txt);
        this.etaTimeSuffixTextView = (TextView) marker.findViewById(com.hypertrack.lib.R.id.eta_txt_suffix);
        this.etaIcon = (ImageView) marker.findViewById(com.hypertrack.lib.R.id.eta_icon);

        // TODO: 13/07/16 HACK to check whether View provided to us by Adapter was ht_custom_marker_layout or not
        if (this.etaTimeTextView != null && this.etaTimeSuffixTextView != null && this.etaIcon != null) {

            // Return a default destination marker without a valid eta
            Integer etaInMinutes = consumerClient.getTaskDisplayETA(taskID);
            if (etaInMinutes == null || etaInMinutes <= 0) {
                return ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(com.hypertrack.lib.R.layout.ht_invalid_eta_marker_layout, null);
            }

            this.etaIcon.setImageResource(mapAdapter.getDestinationMarkerIconForTaskID(this, taskID));
            updateETAInfo(taskID);
        }

        return marker;
    }

    private void updateETAInfo(String taskID) {
        if (this.etaTimeTextView == null || this.etaTimeSuffixTextView == null) {
            return;
        }

        Integer etaInMinutes = consumerClient.getTaskDisplayETA(taskID);
        if (etaInMinutes == null || etaInMinutes <= 0)
            return;

        updateDestinationMarkerWithETA(etaInMinutes);
    }

    private void updateDestinationMarkerWithETA(Integer etaInMinutes) {
        // Set empty view if etaInMinutes is null
        if (etaInMinutes == null) {
            this.etaTimeTextView.setText("");
            this.etaTimeSuffixTextView.setText("");

            // Set ETA to 0 if etaInMinutes is 0 or below
        } else if (etaInMinutes <= 0) {
            etaInMinutes = 0;
            this.etaTimeTextView.setText(String.valueOf(etaInMinutes));
            this.etaTimeSuffixTextView.setText(this.getResources().getQuantityString(com.hypertrack.lib.R.plurals.minute_text, etaInMinutes));

        } else {

            // Set ETA in minutes if etaInMinutes is equal or below MINUTES_ON_ETA_MARKER_LIMIT
            if (etaInMinutes <= ConsumerConstants.MINUTES_ON_ETA_MARKER_LIMIT) {
                this.etaTimeTextView.setText(String.valueOf(etaInMinutes));
                this.etaTimeSuffixTextView.setText(this.getResources().getQuantityString(com.hypertrack.lib.R.plurals.minute_text, etaInMinutes));

                // Set ETA in hours if etaInMinutes is above MINUTES_ON_ETA_MARKER_LIMIT
            } else {
                int hours = etaInMinutes / ConsumerConstants.MINUTES_IN_AN_HOUR;

                // Round off ETA to nearest hour
                if (etaInMinutes % ConsumerConstants.MINUTES_IN_AN_HOUR < ConsumerConstants.MINUTES_TO_ROUND_OFF_TO_HOUR) {
                    this.etaTimeTextView.setText(String.valueOf(hours));
                } else {
                    hours = hours + 1;
                    this.etaTimeTextView.setText(String.valueOf(hours));
                }
                this.etaTimeSuffixTextView.setText(this.getResources().getQuantityString(com.hypertrack.lib.R.plurals.hour_text, hours));
            }
        }
    }

    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();

        if (getActivity() != null && !getActivity().isFinishing()) {
            Bitmap bitmap = null;
            try {
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
                view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
                view.buildDrawingCache();
                bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);
            } catch (Exception e) {
                e.printStackTrace();
                HTLog.e(TAG, "Exception occurred while createDrawableFromView: " + e);
            }

            return bitmap;
        }

        return null;
    }

    private void startETATimer(final String taskID) {
        if (etaTimer != null) {
            return;
        }

        etaTimer = new Timer();
        final Handler handler = new Handler();

        etaTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateETAInfo(taskID);
                    }
                });
            }
        }, 30000);
    }

    private void stopETATimer() {
        if (etaTimer != null) {
            etaTimer.cancel();
            etaTimer = null;
        }
    }

    private void setTrafficVisibility(boolean flag) {

        mapViewModelList.setTrafficEnabled(flag);

        if (mMap != null) {
            mMap.setTrafficEnabled(mapAdapter.showTrafficLayer(this));
        }
    }

    // Address Info Methods

    private void setAddressInfoVisibility(String taskID, boolean flag) {
        if (flag) {
            this.addressInfoLayout.setVisibility(View.VISIBLE);
        } else {
            this.addressInfoLayout.setVisibility(View.GONE);
        }

        this.updateMapPadding(taskID);
    }

    private void updateAddressInfo(String taskID) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (consumerClient == null || consumerClient.taskForTaskID(taskID) == null) {
            return;
        }

        this.setAddressInfoVisibility(taskID, mapAdapter.showAddressInfoViewForTaskID(this, taskID));

        HTTask task = consumerClient.taskForTaskID(taskID);

        if (task == null)
            return;

        // Check if Task CompletionAddress is available
        this.taskEndAddressTextView.setText(consumerClient.getCompletionAddress(taskID));

        // Check if Address Info View has to be displayed for a completed Task denoted by given taskID
        if (mapAdapter.showTaskSummaryForTaskID(this, taskID) && consumerClient.showTaskSummaryForTaskStatus(taskID)) {
            this.enableEndPlaceEditView(false, null);
            this.taskDurationTextView.setText(consumerClient.getTaskMeteringString(getActivity(), taskID));
            this.taskStartAddressTextView.setText(consumerClient.getStartAddress(taskID));
            this.taskStartTimeTextView.setText(task.getTaskStartTimeDisplayString());
            this.taskEndTimeTextView.setText(task.getTaskEndTimeDisplayString());
            this.taskDateTextView.setText(task.getTaskDateDisplayString());
            this.setTaskSummaryVisibility(View.VISIBLE);

        } else {
            this.taskEndTimeTextView.setText("");
            this.enableEndPlaceEditView(true, taskID);
            this.resetTaskSummaryInfoViews();
        }
    }

    private void enableEndPlaceEditView(boolean enable, final String taskID) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (enable && !mapAdapter.disableEditDestinationForTaskID(this, taskID)) {

            if (TextUtils.isEmpty(taskID) || consumerClient == null || consumerClient.taskForTaskID(taskID) == null)
                return;

            taskEndPlaceEditIcon.setVisibility(View.VISIBLE);
            taskEndPlaceLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getActivity() != null && !getActivity().isFinishing()) {

                        final int actionResId = consumerClient.taskActionForTaskID(taskID);
                        final String actionText = getString(actionResId);

                        AlertDialog.Builder builder = new AlertDialog.Builder((new ContextThemeWrapper(getActivity(),
                                com.hypertrack.lib.R.style.HTEditDestinationAlertDialogTheme)));
                        builder.setTitle(getString(com.hypertrack.lib.R.string.edit_task_destination_cnf_dialog, actionText));
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Callback for Begin Editing Destination
                                if (mapFragmentCallback != null)
                                    mapFragmentCallback.onBeginEditingDestination(HyperTrackMapFragment.this, taskID);

                                if (TextUtils.isEmpty(taskID) || consumerClient.taskForTaskID(taskID).getDestination() == null
                                        || consumerClient.taskForTaskID(taskID).getDestination().getLocation() == null) {
                                    Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_task_destination_location_unavailable_error_msg,
                                            Toast.LENGTH_SHORT).show();
                                    HTLog.e(TAG, "Error occurred while setupEditDestinationLayout: DestinationLocation is null");
                                    dialog.dismiss();
                                }

                                setupEditDestinationLayout(taskID, actionText);
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Callback for Cancelled Editing Destination
                                if (mapFragmentCallback != null)
                                    mapFragmentCallback.onCanceledEditingDestination(HyperTrackMapFragment.this, taskID);

                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {
            taskEndPlaceEditIcon.setVisibility(View.GONE);
            taskEndPlaceConfirmBtn.setVisibility(View.GONE);
            taskEndPlaceEditLayout.setVisibility(View.GONE);
            taskEndPlaceLayout.setOnClickListener(null);
        }
    }

    private void setupEditDestinationLayout(String taskID, String actionText) {
        if (getActivity() == null || getActivity().isFinishing())
            return;

        editDestinationTaskID = taskID;

        // Get Current LatLng for Task DestinationLocation
        Place destination = consumerClient.taskForTaskID(taskID).getDestination();
        if (destination == null || destination.getLocation() == null) {
            Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_task_destination_location_unavailable_error_msg,
                    Toast.LENGTH_SHORT).show();
            HTLog.e(TAG, "Error occurred while setupEditDestinationLayout: DestinationLocation is null");
            return;
        }

        double[] coordinates = destination.getLocation().getCoordinates();
        LatLng editDestinationLatLng = new LatLng(coordinates[1], coordinates[0]);

        if (editDestinationLatLng.latitude == 0.0 || editDestinationLatLng.longitude == 0.0) {
            Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_task_destination_location_unavailable_error_msg,
                    Toast.LENGTH_SHORT).show();
            HTLog.e(TAG, "Error occurred while setupEditDestinationLayout: Destination LatLng invalid");
            return;
        }

        // Disable BindMapView
        userInteractionDisabledMapBounds = true;
        editDestinationViewVisible = true;

        // Hide the SetBoundsButton, in case visible
        setBoundsButton.setVisibility(View.GONE);

        // Hide User & Address Info Views
        hideUserInfoView();
        hideAddressInfoView();

        // Remove Destination Marker For All Tasks
        HyperTrackMapFragment.this.removeAllDestinationMarkers();

        // Enable MyLocation Button on Map
        if (checkForLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        // Update Map Padding
        updateMapPadding(taskID);

        // Show Edit Destination Layout
        showEditDestination(taskID, editDestinationLatLng, actionText);
    }

    private void showEditDestination(final String taskID, final LatLng editDestinationLatLng, final String actionText) {
        if (mapViewModelList == null || editDestinationLatLng == null)
            return;

        // Set End Place Layout Dismiss Icon ClickListener
        taskEndEditPlaceDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the Edit Destination Layouts & Restore the previous states
                hideEditDestinationLayout(taskID, editDestinationLatLng);

                // Callback for Cancelled Editing Destination
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onCanceledEditingDestination(HyperTrackMapFragment.this, taskID);
            }
        });

        // Move the Destination Marker to the center of the map
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(editDestinationLatLng, 16));

        // Set Confirm Button Click Listener & Make the Views Visible
        taskEndPlaceConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;

                if (mMap != null) {

                    if (mMap.getCameraPosition().zoom < 13) {
                        // Check if Edit Destination Failure Alert has been enabled
                        if (mapAdapter.shouldShowEditDestinationFailureAlert(HyperTrackMapFragment.this, taskID)) {
                            Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_task_destination_zoom_level_error_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        final LatLng updatedDestinationLatLng = mMap.getCameraPosition() != null ? mMap.getCameraPosition().target : null;
                        // Initiate Destination location update
                        updateDestinationLocation(taskID, updatedDestinationLatLng, actionText);
                    }
                }
            }
        });

        // Set Confirm Destination Texts depending on Task Action type
        taskEndPlaceConfirmDialogTitle.setText(getString(com.hypertrack.lib.R.string.edit_task_destination_header_title, actionText));
        taskEndPlaceConfirmDialogMessage.setText(getString(com.hypertrack.lib.R.string.edit_task_destination_header_message, actionText));

        taskEndPlaceConfirmBtn.setVisibility(View.VISIBLE);
        taskEndPlaceEditLayout.setVisibility(View.VISIBLE);
    }

    private void updateDestinationLocation(final String taskID, final LatLng updatedDestinationLatLng, final String actionText) {
        if (consumerClient == null || TextUtils.isEmpty(taskID) || updatedDestinationLatLng == null)
            return;

        GeoJSONLocation location = new GeoJSONLocation(updatedDestinationLatLng.latitude, updatedDestinationLatLng.longitude);

        mapLayoutLoaderLayout.setVisibility(View.VISIBLE);

        // Perform Update Destination Location Network Call
        consumerClient.updateDestinationLocation(taskID, location, new UpdateDestinationCallback() {
            @Override
            public void onSuccess(HTTask task) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;

                mapLayoutLoaderLayout.setVisibility(View.GONE);

                // Hide the Edit Destination Layouts & Restore the previous states
                GeoJSONLocation destinationLocation = task.getDestination() != null ? task.getDestination().getLocation() : null;

                if (destinationLocation == null) {
                    destinationLocation = new GeoJSONLocation(updatedDestinationLatLng.latitude,
                            updatedDestinationLatLng.longitude);
                }

                // Hide the Edit Destination Layouts & Restore the previous states
                hideEditDestinationLayout(taskID, new LatLng(destinationLocation.getLatitude(),
                        destinationLocation.getLongitude()));

                // Show Edit Destination Success Message
                Toast.makeText(getActivity(), getString(com.hypertrack.lib.R.string.edit_task_destination_success_msg, actionText),
                        Toast.LENGTH_SHORT).show();

                // Callback for Edit Destination completed
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onEndEditingDestination(HyperTrackMapFragment.this, taskID);
            }

            @Override
            public void onError(Exception exception) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;

                mapLayoutLoaderLayout.setVisibility(View.GONE);

                if (mapAdapter.shouldShowEditDestinationFailureAlert(HyperTrackMapFragment.this, taskID)) {
                    Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_task_destination_error_msg, Toast.LENGTH_SHORT).show();
                }

                // Callback for Error occurred in Edit Destination
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onReceiveEditDestinationError(HyperTrackMapFragment.this, taskID, exception.getMessage());
            }
        });
    }

    private void hideEditDestinationLayout(String taskID, LatLng destinationLatLng) {
        if (getActivity() == null || getActivity().isFinishing() || mapAdapter == null)
            return;

        // Reset the Flag to indicate EditDestinationView Visible
        editDestinationViewVisible = false;
        editDestinationTaskID = "";

        // Hide the Confirm Destination Layouts
        taskEndPlaceConfirmBtn.setVisibility(View.GONE);
        taskEndPlaceEditLayout.setVisibility(View.GONE);

//        HyperTrackMapFragment.this.resetUserAndAddressInfo();
//        HyperTrackMapFragment.this.resetDestinationMarker();

        updateMapPadding(taskID);

        // Reset Address Info View to its default state
        this.showUserAndAddressInfo(taskID);
        this.setupDestinationMarker(taskID, destinationLatLng);

        // Disable MyLocation Button on Map
        if (checkForLocationPermission()) {
            mMap.setMyLocationEnabled(mapAdapter.setMyLocationEnabled(HyperTrackMapFragment.this));
            mMap.getUiSettings().setMyLocationButtonEnabled(mapAdapter.setMyLocationButtonEnabled(HyperTrackMapFragment.this));
        }

        // Enable & Call BindMapView
        if (setBoundsButton != null) {
            setBoundsButton.performClick();
        }
    }

    private boolean checkForLocationPermission() {
        return (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED);
    }

    private void setTaskSummaryVisibility(int visibility) {
        this.taskSummaryLayout.setVisibility(visibility);
        this.taskStartLayout.setVisibility(visibility);
        this.taskVerticalSeparator.setVisibility(visibility);
    }

    private void updateMapWithTaskSummary(String taskID) {
        if (consumerClient == null
                || consumerClient.taskForTaskID(taskID) == null) {
            return;
        }

        this.removeHeroMarker(taskID);
        this.removeAllDestinationMarkers();
        this.removeSourceMarker(taskID);

        LatLng sourceLocationLatLng = consumerClient.getSourceLocationLatLng(taskID);
        if (sourceLocationLatLng != null) {

            this.removeTaskSummaryStartMarker(taskID);

            // Get TaskSummaryStartMarker's Anchors from MapAdapter
            float[] anchors = null;
            if (mapAdapter != null)
                anchors = mapAdapter.getTaskSummaryStartMarkerAnchorValues(this, taskID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.5f};
            }

            mapViewModelList.setTaskSummaryStartMarkerForTaskID(taskID,
                    mMap.addMarker(new MarkerOptions()
                            .position(sourceLocationLatLng)
                            .anchor(anchors[0], anchors[1])
                            .icon(BitmapDescriptorFactory.fromResource(
                                    mapAdapter.getTaskSummaryStartMarkerIconForTaskID(this, taskID)))));
        }

        LatLng completionLocationLatLng = consumerClient.getCompletionLocationLatLng(taskID);
        if (completionLocationLatLng != null) {

            this.removeTaskSummaryEndMarker(taskID);

            // Get TaskSummaryEndMarker's Anchors from MapAdapter
            float[] anchors = null;
            if (mapAdapter != null)
                anchors = mapAdapter.getTaskSummaryStartMarkerAnchorValues(this, taskID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.5f};
            }

            mapViewModelList.setTaskSummaryEndMarkerForTaskID(taskID,
                    mMap.addMarker(new MarkerOptions()
                            .position(completionLocationLatLng)
                            .anchor(anchors[0], anchors[1])
                            .icon(BitmapDescriptorFactory.fromResource(
                                    mapAdapter.getTaskSummaryEndMarkerIconForTaskID(this, taskID)))));
        }

        modifyMapForTripSummary();
        addTripSummaryPolyline(taskID);
    }

    private void modifyMapForTripSummary() {
        this.setBoundsButton.setVisibility(View.GONE);
        this.mMap.setTrafficEnabled(false);
    }

    private void addTripSummaryPolyline(String taskID) {
        if (mapViewModelList == null
                || consumerClient == null
                || consumerClient.taskForTaskID(taskID) == null
                || consumerClient.taskForTaskID(taskID).getEncodedPolyline() == null) {
            return;
        }

        String encodedPolyline = consumerClient.taskForTaskID(taskID).getEncodedPolyline();

        mapViewModelList.setTaskSummaryPolylineForTaskID(taskID, HTMapUtils.decode(encodedPolyline));
        PolylineOptions options = new PolylineOptions().width(8).color(Color.parseColor("#0A61C2"));
        options.addAll(mapViewModelList.getTaskSummaryPolylineForTaskID(taskID));

        mMap.addPolyline(options);
        updateMapBounds(taskID, mapViewModelList.getTaskSummaryPolylineForTaskID(taskID));
    }

    private void updateMapBounds(String taskID, List<LatLng> polyline) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (polyline != null) {
            for (LatLng latLng : polyline) {
                builder.include(latLng);
            }
        }

        if (mapViewModelList.getTaskSummaryStartMarkerForTaskID(taskID) != null
                && mapViewModelList.getTaskSummaryStartMarkerForTaskID(taskID).getPosition() != null) {
            builder.include(mapViewModelList.getTaskSummaryStartMarkerForTaskID(taskID).getPosition());
        }

        if (mapViewModelList.getTaskSummaryEndMarkerForTaskID(taskID) != null
                && mapViewModelList.getTaskSummaryEndMarkerForTaskID(taskID).getPosition() != null) {
            builder.include(mapViewModelList.getTaskSummaryEndMarkerForTaskID(taskID).getPosition());
        }

        LatLngBounds bounds = builder.build();

        if (isMapLoaded) {
            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 0);
            mMap.moveCamera(update);
        }
    }

    /**
     * Call this method to stop all the timers.
     * IMPORTANT: This needs to be called before killing the map or the map won't get deallocated.
     */
    private void deinitMap() {
        consumerClient.invalidateAllTimers();
    }

    // Reset Methods
    private void resetViews() {
        this.resetFlags();
        this.resetMap();

        if (consumerClient != null && mapAdapter != null && mapAdapter.taskIDsToTrack(this) != null) {
            for (String taskID : mapAdapter.taskIDsToTrack(this)) {
                this.resetMarkers(taskID);
            }
        }

        this.hideUserInfoView();
        this.hideAddressInfoView();
    }

    private void resetFlags() {
        setBoundsButton.setVisibility(View.GONE);
        mapViewModelList.setShouldBindView(true);
    }

    private void resetMap() {
        this.taskNavigatorList.stopPollingForAllTasks();
        this.taskNavigatorList.clearAllNavigators();

        if (this.mMap != null) {
            this.setTrafficVisibility(mapAdapter.showTrafficLayer(this));

            this.setMapGestures();

            if (mapAdapter != null && mapAdapter.getMapFragmentInitialState(this) != null) {
                this.mMap.moveCamera(mapAdapter.getMapFragmentInitialState(this));
            } else {
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 2);
                this.mMap.moveCamera(update);
            }
        }

        if (this.setBoundsButton != null) {
            this.setBoundsButton.setVisibility(View.GONE);
        }

        this.resetMapPadding();
    }

    private void resetMarkers(String taskID) {
        if (this.mMap == null) return;

        this.removeAllDestinationMarkers();
        this.removeHeroMarker(taskID);
        this.removeSourceMarker(taskID);
        this.removeTaskSummaryEndMarker(taskID);
        this.removeTaskSummaryStartMarker(taskID);
    }

    private void resetUserAndAddressInfo() {

        // Setup User & Address InfoView Only if EditDestination Overlay is not visible
        if (!editDestinationViewVisible) {

            if (mapAdapter.getTaskIDsToTrackCount(this) > 1) {
                // Show UserInfo & AddressInfo Views for Multiple Tasks tracking
                this.showMultipleTasksUserInfo();
                this.showMultipleTasksAddressInfo();
                this.enableEndPlaceEditView(false, null);

                // Handle Info Views for Special Case of Single Task Tracking
            } else if (mapAdapter.getTaskIDsToTrackCount(this) == 1) {
                String taskID = mapAdapter.taskIDsToTrack(this) != null ? mapAdapter.taskIDsToTrack(this).get(0) : null;

                if (taskID == null)
                    return;

                this.showUserAndAddressInfo(taskID);

            } else {
                // Hide User & Address Info Views
                hideUserInfoView();
                hideAddressInfoView();
            }
        }
    }

    private void hideUserInfoView() {
        if (mMap == null || mapAdapter == null) {
            return;
        }

        this.setUserInfoLayoutVisibility(null, false);

        if (this.userName != null) {
            this.userName.setText("");
        }

        if (this.userProfileImage != null) {
            this.userProfileImage.setImageResource(com.hypertrack.lib.R.drawable.ht_profile_image);
        }
    }

    private void hideAddressInfoView() {

        if (this.addressInfoLayout != null) {
            this.addressInfoLayout.setVisibility(View.GONE);
        }

        if (this.taskEndTimeTextView != null) {
            this.taskEndTimeTextView.setText("");
        }

        if (this.taskEndAddressTextView != null) {
            this.taskEndAddressTextView.setText("");
        }

        this.resetTaskSummaryInfoViews();
    }

    private void resetTaskSummaryInfoViews() {
        // Handle TaskSummary Info View for Special Case of Single Task Tracking
        if (consumerClient != null && mapAdapter != null && mapAdapter.getTaskIDsToTrackCount(this) == 1) {
            String taskID = mapAdapter.taskIDsToTrack(this) != null ? mapAdapter.taskIDsToTrack(this).get(0) : null;

            // Check if TaskSummary Info View has to be displayed for a completed Task denoted by given taskID
            if (mapAdapter.showTaskSummaryForTaskID(this, taskID) && consumerClient.showTaskSummaryForTaskStatus(taskID)) {
                this.setTaskSummaryVisibility(View.VISIBLE);
                this.enableEndPlaceEditView(false, null);
                return;
            }
        }

        this.setTaskSummaryVisibility(View.GONE);

        if (this.taskDateTextView != null) {
            this.taskDateTextView.setText("");
        }

        if (this.taskDurationTextView != null) {
            this.taskDurationTextView.setText("");
        }

        if (this.taskStartAddressTextView != null) {
            this.taskStartAddressTextView.setText("");
        }

        if (this.taskStartTimeTextView != null) {
            this.taskStartTimeTextView.setText("");
        }
    }

    private void resetMapPadding() {
        if (getActivity() == null || getActivity().isFinishing())
            return;

        if (mMap == null)
            return;

        int top = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_top_partial_expanded_padding);
        int bottom = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_bottom_expanded_padding);
        int defaultPadding = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_default_padding);

        mMap.setPadding(defaultPadding, top, defaultPadding, bottom);
    }

    private void updateMapPadding(String taskID) {
        if (getActivity() == null || getActivity().isFinishing())
            return;

        if (mMap == null || taskID == null)
            return;


        int top = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_top_partial_expanded_padding);
        int bottom = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_bottom_expanded_padding);
        int defaultPadding = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_default_padding);

        if (editDestinationViewVisible) {
            bottom = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_bottom_medium_padding);
            defaultPadding = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_no_side_padding);
        } else {
            if (!mapAdapter.showUserInfoForTaskID(this, taskID)) {
                bottom = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_default_top_bottom_padding);
            }

            if (taskSummaryLayout != null && taskSummaryLayout.getVisibility() == View.VISIBLE) {
                top = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_top_expanded_padding);
            } else if (!mapAdapter.showAddressInfoViewForTaskID(this, taskID)) {
                top = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_default_top_bottom_padding);
            }
        }

        mMap.setPadding(defaultPadding, top, defaultPadding, bottom);
    }

    // Remove Markers from Map for a TaskID
    private void removeSourceMarker(String taskID) {
        // Remove Marker From Map & MapViewModelList
        if (mMap != null && mapViewModelList.getSourceMarkerForTaskID(taskID) != null) {

            // Callback for Source Marker Removed
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onSourceMarkerRemoved(this, taskID, mapViewModelList.getSourceMarkerForTaskID(taskID));
            }

            mapViewModelList.getSourceMarkerForTaskID(taskID).remove();
            mapViewModelList.setSourceMarkerForTaskID(taskID, null);
        }
    }

    private void removeMultipleTasksDestinationMarker() {
        if (multipleTasksDestinationMarker != null) {

            // Callback for MultipleTasks Destination Marker Removed
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onMultipleTasksDestinationMarkerRemoved(this, multipleTasksDestinationMarker);
            }

            multipleTasksDestinationMarker.remove();
            multipleTasksDestinationMarker = null;
        }
    }

    private void removeDestinationMarker(String taskID) {
        // Remove Marker From Map & MapViewModelList
        if (mMap != null && mapViewModelList.getDestinationMarkerForTaskID(taskID) != null) {

            // Callback for Destination Marker Removed
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onDestinationMarkerRemoved(this, taskID, mapViewModelList.getDestinationMarkerForTaskID(taskID));
            }

            mapViewModelList.getDestinationMarkerForTaskID(taskID).remove();
            mapViewModelList.setDestinationMarkerForTaskID(taskID, null);
        }
    }

    private void removeAllDestinationMarkers() {
        // Remove All Destination Markers From Map & MapViewModelList
        if (mMap != null) {

            ArrayList<String> destinationMarkers = mapViewModelList.getDestinationMarkersForAllTasks();

            if (destinationMarkers != null && !destinationMarkers.isEmpty()) {

                for (String taskID : destinationMarkers) {

                    if (!TextUtils.isEmpty(taskID)) {
                        // Callback for Destination Marker Removed
                        if (mapFragmentCallback != null) {
                            mapFragmentCallback.onDestinationMarkerRemoved(this, taskID, mapViewModelList.getDestinationMarkerForTaskID(taskID));
                        }

                        mapViewModelList.getDestinationMarkerForTaskID(taskID).remove();
                        mapViewModelList.setDestinationMarkerForTaskID(taskID, null);
                    }
                }
            }
        }
    }

    private void removeHeroMarker(String taskID) {
        // Remove Marker From Map & MapViewModelList
        if (mMap != null && mapViewModelList.getHeroMarkerForTaskID(taskID) != null) {

            // Callback for Hero Marker Removed
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onHeroMarkerRemoved(this, taskID, mapViewModelList.getHeroMarkerForTaskID(taskID));
            }

            mapViewModelList.getHeroMarkerForTaskID(taskID).remove();
            mapViewModelList.setHeroMarkerForTaskID(taskID, null);
        }
    }

    private void removeTaskSummaryStartMarker(String taskID) {
        // Remove Marker From Map & MapViewModelList
        if (mMap != null && mapViewModelList.getTaskSummaryStartMarkerForTaskID(taskID) != null) {
            mapViewModelList.getTaskSummaryStartMarkerForTaskID(taskID).remove();
            mapViewModelList.setTaskSummaryStartMarkerForTaskID(taskID, null);
        }
    }

    private void removeTaskSummaryEndMarker(String taskID) {
        // Remove Marker From Map & MapViewModelList
        if (mMap != null && mapViewModelList.getTaskSummaryEndMarkerForTaskID(taskID) != null) {
            mapViewModelList.getTaskSummaryEndMarkerForTaskID(taskID).remove();
            mapViewModelList.setTaskSummaryEndMarkerForTaskID(taskID, null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() == null || getActivity().isFinishing())
            return false;

        //Back button inside toolbar (If Task StatusToolbar is enabled)
        if (item.getItemId() == android.R.id.home && mapAdapter.showOrderStatusToolbar(this)
                && getActivity() != null) {
            getActivity().onBackPressed();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter orderStatusChangedFilter = new IntentFilter(ConsumerClient.TASK_STATUS_CHANGED_NOTIFICATION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mOrderStatusChangedMessageReceiver, orderStatusChangedFilter);

        IntentFilter taskRefreshedFilter = new IntentFilter(ConsumerClient.TASK_DETAIL_REFRESHED_NOTIFICATION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mTaskDetailRefreshedMessageReceiver, taskRefreshedFilter);

        IntentFilter taskRemovedFilter = new IntentFilter(ConsumerClient.TASK_REMOVED_FROM_TRACKING_NOTIFICATION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mTaskRemovedMessageReceiver, taskRemovedFilter);

        HTLog.v("lifecycle", "Inside OnResume");

        // Initialize Map Fragment
        initializeMapFragment();

        onUpdateAllTasks(null);
    }

    @Override
    public void onPause() {
        super.onPause();

        HTLog.v("lifecycle", "Inside onPause");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mOrderStatusChangedMessageReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mTaskDetailRefreshedMessageReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mTaskRemovedMessageReceiver);

        // Stop Polling for All Tasks in onPause()
        taskNavigatorList.clearAllNavigators();

        stopETATimer();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class MapFragmentConstants {
        public static final int THRESHOLD_DISTANCE = 1;
    }
}