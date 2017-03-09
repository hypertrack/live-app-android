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
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.R;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.HTConsumerClient;
import com.hypertrack.lib.internal.consumer.models.ActionNavigatorCallback;
import com.hypertrack.lib.internal.consumer.models.ActionNavigatorList;
import com.hypertrack.lib.internal.consumer.models.CustomMarker;
import com.hypertrack.lib.internal.consumer.models.HTAction;
import com.hypertrack.lib.internal.consumer.models.HTMapViewModel;
import com.hypertrack.lib.internal.consumer.models.HTPlace;
import com.hypertrack.lib.internal.consumer.models.HTUser;
import com.hypertrack.lib.internal.consumer.models.UpdateDestinationCallback;
import com.hypertrack.lib.internal.consumer.utils.AnimationUtils;
import com.hypertrack.lib.internal.consumer.utils.ConsumerConstants;
import com.hypertrack.lib.internal.consumer.utils.HTCircleImageView;
import com.hypertrack.lib.internal.consumer.utils.HTDownloadImageTask;
import com.hypertrack.lib.internal.consumer.utils.HTMapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class provides a MapInterface to track one or more actions in real-time.
 * This provides you a reference to the fragment included in your Activity.
 * <p>
 * The map fragment can be controlled by passing an instance of a HTMapAdapter
 * implementation. This instance needs to be set by calling {@link #setHTMapAdapter(HTMapAdapter)}.
 * <p>
 * Updates for HyperTrackMapFragment can be received by setting a
 * {@link HyperTrackMapFragment#setMapFragmentCallback(MapFragmentCallback)}
 * instance as a parameter to the {@link #setMapFragmentCallback(MapFragmentCallback)} method.
 */
public class HyperTrackMapFragment extends Fragment implements MapUpdateInterface, ActionNavigatorCallback,
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
    private RelativeLayout actionSummaryLayout;
    private RelativeLayout actionStartLayout;
    private RelativeLayout actionEndPlaceLayout;
    private TextView actionDurationTextView;
    private TextView actionDateTextView;
    private TextView actionStartAddressTextView;
    private TextView actionEndAddressTextView;
    private TextView actionStartTimeTextView;
    private TextView actionEndTimeTextView;
    private View actionVerticalSeparator;
    private View actionEndPlaceConfirmDialogVerticalLine;
    private ImageView actionEndEditPlaceDismiss;
    private ImageView actionEndEditPlaceConfirmDialogTriangle;
    private ImageView actionEndPlaceEditIcon;
    private RelativeLayout actionEndPlaceEditLayout;
    private TextView actionEndPlaceConfirmDialogTitle;
    private TextView actionEndPlaceConfirmDialogMessage;
    private FrameLayout actionEndPlaceConfirmLayout;
    private LinearLayout actionEndPlaceConfirmBtn;
    private LinearLayout actionEndPlaceConfirmDialog;
    private LinearLayout mapLayoutLoaderLayout;
    private ProgressBar mapLayoutLoader;
    private View view;
    private Toolbar orderStatusToolbar;
    private ImageButton setBoundsButton;
    private ImageView etaIcon;
    private TextView etaTimeTextView, etaTimeSuffixTextView;
    private HTConsumerClient consumerClient;
    private ActionNavigatorList actionNavigatorList;
    private HTMapViewModelList mapViewModelList;
    private GoogleMap mMap;
    private boolean isMapLoaded = false;
    private Marker multipleActionsDestinationMarker;
    private Marker markerSelectedOnClick;
    private String actionIDSelectedOnMarkerClick;
    private Map<String, CustomMarker> customMarkers;
    private boolean userInteractionDisabledMapBounds = false, editDestinationViewVisible = false;
    private String editDestinationActionID = "";
    /**
     * Callback for HyperTrackMapFragment to receive user action events
     *
     * @see
     */
    private MapFragmentCallback mapFragmentCallback;
    /**
     * Adapter for HyperTrackMapFragment to customize Map View and Markers
     *
     * @see HTMapAdapter
     */
    private HTMapAdapter mapAdapter;
    private BroadcastReceiver mOrderStatusChangedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(HTConsumerClient.INTENT_EXTRA_ACTION_ID_LIST)) {
                ArrayList<String> actionIDList = intent.getStringArrayListExtra(HTConsumerClient.INTENT_EXTRA_ACTION_ID_LIST);

                if (actionIDList != null && mapAdapter != null) {

                    onUpdateAllActions(actionIDList);

                    if (!editDestinationViewVisible && !TextUtils.isEmpty(actionIDSelectedOnMarkerClick)) {
                        HyperTrackMapFragment.this.showUserAndAddressInfo(actionIDSelectedOnMarkerClick);
                        HyperTrackMapFragment.this.updateOrderStatusToolbar(actionIDSelectedOnMarkerClick);
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
    private BroadcastReceiver mActionDetailRefreshedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(HTConsumerClient.INTENT_EXTRA_ACTION_ID_LIST)) {
                ArrayList<String> actionIDList = intent.getStringArrayListExtra(HTConsumerClient.INTENT_EXTRA_ACTION_ID_LIST);

                if (actionIDList != null) {
                    // Update the ActionInfo to Map View UI
                    onUpdateAllActions(actionIDList);

                    if (!TextUtils.isEmpty(actionIDSelectedOnMarkerClick)) {
                        updateOrderStatusToolbar(actionIDSelectedOnMarkerClick);
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
    private BroadcastReceiver mActionRemovedMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.hasExtra(HTConsumerClient.INTENT_EXTRA_ACTION_ID_LIST)) {
                ArrayList<String> actionIDList = intent.getStringArrayListExtra(HTConsumerClient.INTENT_EXTRA_ACTION_ID_LIST);

                if (actionIDList != null && actionIDList.size() > 0) {
                    // Remove the MapViewModels & ActionNavigators for these removed ActionIDs
                    for (String actionID : actionIDList) {
                        if (mapViewModelList != null)
                            mapViewModelList.removeHTMapViewModelForActionID(actionID);

                        actionNavigatorList.removeNavigator(actionID);
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
     * Call this method to set HTMapAdapter for HyperTrackMapFragment
     * IMPORTANT: An Adapter extending HTMapAdapter needs to be set to HyperTrackMapFragment using this method.
     *
     * @param mapAdapter mapAdapter can be used to customize settings for Map View,
     *                   Map Markers, Order Info Views and User Info Views.
     */
    public void setHTMapAdapter(HTMapAdapter mapAdapter) {
        if (mapAdapter == null)
            throw new RuntimeException("Required Parameter: HTMapAdapter cannot be null");

        if (getActivity() == null || getActivity().isFinishing())
            throw new RuntimeException("Activity is finishing. Could not set HTMapAdapter");

        this.mapAdapter = mapAdapter;
        this.mapAdapter.setMapUpdateInterface(this);
        this.mapAdapter.setConsumerClient(consumerClient);
        this.mapAdapter.setHTMapViewModelList(mapViewModelList);
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
     * For MULTIPLE ACTIVE ORDERS being tracked, OrderStatus of a given actionID will be displayed
     * when markers corresponding to that actionID are clicked.
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
     * @param markerID Marker ID for the ic_ht_hero_marker to be fetched
     * @return Returns the custom ic_ht_hero_marker for the given MarkerId if it exists, null otherwise
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
     * @return Returns true if the custom ic_ht_hero_marker is removed, false if Map is null.
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
     * @return Returns true if the custom ic_ht_hero_marker is removed, false if Map is null.
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
     * Call this method to get Hero Marker for a given Action
     *
     * @param actionID The ActionID for which the Hero Marker needs to be returned.
     * @return Hero Marker Instance for the given actionID
     */
    public Marker getHeroMarker(String actionID) {
        if (mapViewModelList == null || TextUtils.isEmpty(actionID)) {
            return null;
        }

        return mapViewModelList.getHeroMarkerForActionID(actionID);
    }

    /**
     * Call this method to get a List of Hero Markers for given Actions
     *
     * @param actionIDList The List of ActionIDs for which the Hero Marker needs to be returned.
     * @return List of Hero Marker Instances for the given List of actionIDs
     */
    public List<Marker> getHeroMarker(List<String> actionIDList) {
        List<Marker> heroMarkerList = new ArrayList<>();

        if (actionIDList != null && actionIDList.size() > 0) {

            for (String actionID : actionIDList) {

                if (getHeroMarker(actionID) != null) {
                    heroMarkerList.add(getHeroMarker(actionID));
                }
            }

            return heroMarkerList;
        }

        return null;
    }

    /**
     * Call this method to get Destination Marker for a given Action
     *
     * @param actionID The ActionID for which the Destination Marker needs to be returned.
     * @return Destination Marker Instance for the given actionID
     */
    public Marker getDestinationMarker(String actionID) {
        if (mapViewModelList == null || TextUtils.isEmpty(actionID)) {
            return null;
        }

        return mapViewModelList.getDestinationMarkerForActionID(actionID);
    }

    /**
     * Call this method to get a List of Destination Markers for given Actions
     *
     * @param actionIDList The List of ActionIDs for which the Destination Marker needs to be returned.
     * @return List of Destination Marker Instances for the given List of actionIDs
     */
    public List<Marker> getDestinationMarker(List<String> actionIDList) {
        List<Marker> destinationMarkerList = new ArrayList<>();

        if (actionIDList != null && actionIDList.size() > 0) {

            for (String actionID : actionIDList) {

                if (getDestinationMarker(actionID) != null) {
                    destinationMarkerList.add(getDestinationMarker(actionID));
                }
            }

            return destinationMarkerList;
        }

        return null;
    }

    /**
     * Call this method to get Source Marker for a given Action
     *
     * @param actionID The ActionID for which the Source Marker needs to be returned.
     * @return Source Marker Instance for the given actionID
     */
    public Marker getSourceMarker(String actionID) {
        if (mapViewModelList == null || TextUtils.isEmpty(actionID)) {
            return null;
        }

        return mapViewModelList.getSourceMarkerForActionID(actionID);
    }

    /**
     * Call this method to get a List of Source Markers for given Actions
     *
     * @param actionIDList The List of ActionIDs for which the Source Marker needs to be returned.
     * @return List of Source Marker Instances for the given List of actionIDs
     */
    public List<Marker> getSourceMarker(List<String> actionIDList) {
        List<Marker> sourceMarkerList = new ArrayList<>();

        if (actionIDList != null && actionIDList.size() > 0) {

            for (String actionID : actionIDList) {

                if (getSourceMarker(actionID) != null) {
                    sourceMarkerList.add(getSourceMarker(actionID));
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
        setHTMapAdapter(new HTMapAdapter(getContext()));
    }

    public void initializeMapFragment() {
        if (consumerClient == null)
            consumerClient = HyperTrack.getHTConsumerClient();

        if (mapViewModelList == null)
            mapViewModelList = new HTMapViewModelList();

        if (actionNavigatorList == null)
            actionNavigatorList = new ActionNavigatorList(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Check if Action StatusToolbar is enabled
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

        orderStatusToolbar = (Toolbar) view.findViewById(com.hypertrack.lib.R.id.action_status_bar);

        userName = (TextView) view.findViewById(com.hypertrack.lib.R.id.user_name);
        userProfileImage = (HTCircleImageView) view.findViewById(com.hypertrack.lib.R.id.user_profile_image);
        userContactNumber = (ImageButton) view.findViewById(R.id.user_contact_number);
        userInfoLayout = (RelativeLayout) view.findViewById(R.id.user_info_layout);

        ctaButton = (ImageButton) view.findViewById(com.hypertrack.lib.R.id.ctaButton);

        setBoundsButton = (ImageButton) view.findViewById(R.id.set_bounds_button);
        setBoundsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset UserInfoLayout & AddressInfoLayout Change their visibility
                HyperTrackMapFragment.this.resetUserAndAddressInfo();
                HyperTrackMapFragment.this.resetDestinationMarker();
                HyperTrackMapFragment.this.resetActionSummaryInfoViews();

                // Reset the selected ic_ht_hero_marker object
                markerSelectedOnClick = null;
                actionIDSelectedOnMarkerClick = null;

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
        addressInfoLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.action_info_layout);

        actionSummaryLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.action_summary_layout);
        actionStartLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.action_start_place_layout);
        actionVerticalSeparator = view.findViewById(com.hypertrack.lib.R.id.action_separator_vertical);

        actionDateTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.action_date);
        actionDurationTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.action_duration);

        actionStartAddressTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.start_place_address);
        actionStartTimeTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.action_start_time);

        actionEndPlaceLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.action_end_place_layout);
        actionEndAddressTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.end_place_address);
        actionEndTimeTextView = (TextView) view.findViewById(com.hypertrack.lib.R.id.action_end_time);

        actionEndPlaceEditIcon = (ImageView) view.findViewById(com.hypertrack.lib.R.id.action_end_place_edit_icon);
        actionEndEditPlaceDismiss = (ImageView) view.findViewById(com.hypertrack.lib.R.id.action_end_place_edit_dismiss);
        actionEndPlaceEditLayout = (RelativeLayout) view.findViewById(com.hypertrack.lib.R.id.action_end_place_edit_layout);
    }

    private void initEditDestinationViews() {
        actionEndPlaceConfirmLayout = (FrameLayout) view.findViewById(com.hypertrack.lib.R.id.action_end_place_confirm_layout);
        actionEndPlaceConfirmBtn = (LinearLayout) view.findViewById(com.hypertrack.lib.R.id.action_end_place_confirm_btn);
        actionEndPlaceConfirmDialog = (LinearLayout) view.findViewById(com.hypertrack.lib.R.id.action_end_place_confirm_dialog);
        actionEndPlaceConfirmDialogTitle = (TextView) view.findViewById(com.hypertrack.lib.R.id.action_end_place_confirm_dialog_title);
        actionEndPlaceConfirmDialogMessage = (TextView) view.findViewById(com.hypertrack.lib.R.id.action_end_place_confirm_dialog_message);

        actionEndEditPlaceConfirmDialogTriangle = (ImageView) view.findViewById(com.hypertrack.lib.R.id.action_end_place_confirm_dialog_triangle);
        actionEndPlaceConfirmDialogVerticalLine = view.findViewById(com.hypertrack.lib.R.id.action_end_place_confirm_dialog_vertical_line);
    }

    @Override
    public void notifyChanged() {
        List<String> actionIDList = mapAdapter.actionIDsToTrack(this);
        if (consumerClient == null || mapViewModelList == null || mMap == null) {
            return;
        }

        // Return if Activity is finishing
        if (getActivity() == null || getActivity().isFinishing())
            return;

        // Clear the HTMapViewModelList (Markers & Action Specific Settings)
        mapViewModelList.clearAllHTMapViewModels();

        // Clear the Map
        if (mMap != null) {
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onMapWillClear(this, mMap);
            }

            mMap.clear();
        }

        // Setup CustomMarkers on notifyChanged call
        setupCustomMarkers();

        actionNavigatorList.removeNavigatorsOtherThanActionIDs(actionIDList);

        // Reset Traffic Layer Visibility
        this.mMap.setTrafficEnabled(mapAdapter.showTrafficLayer(this));

        // Reset the Order StatusToolbar to its default state
        updateOrderStatusToolbar(null);

        for (String actionID : actionIDList) {
            this.onUpdateAction(actionID);
        }

        // Reset UserInfoLayout & AddressInfoLayout & Change their visibility
        // Important: This is done after onUpdateAction has been called to handle special case of
        // Single Action Tracking.
        this.resetUserAndAddressInfo();
        this.resetDestinationMarker();
        this.resetActionSummaryInfoViews();

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
    public void moveToLocationWithTimeInterval(String actionID, LatLng toPosition, long timeDuration) {
        if (mapViewModelList == null || mapAdapter == null || !this.shouldMoveHeroMarker(actionID, toPosition)) {
            return;
        }

        LatLng fromPosition = null;
        Marker heroMarker = mapViewModelList.getHeroMarkerForActionID(actionID);

        if (heroMarker == null) {
            heroMarker = this.setupHeroMarker(actionID, toPosition, null, true);
        } else {
            fromPosition = heroMarker.getPosition();
            this.moveMarker(actionID, heroMarker, toPosition, timeDuration);
        }

        if (heroMarker != null && mapAdapter.rotateHeroMarker(this, actionID)) {
            float bearing = bearingBetweenLatLngs(fromPosition, toPosition);
            heroMarker.setRotation(bearing);
        }

        if (heroMarker != null && this.mapFragmentCallback != null) {
            this.mapFragmentCallback.onHeroMarkerWillMove(HyperTrackMapFragment.this, actionID, heroMarker, toPosition);
        }

        if (mapAdapter.shouldBindView() && !userInteractionDisabledMapBounds) {
            if (TextUtils.isEmpty(actionIDSelectedOnMarkerClick)) {
                this.bindMapView();
            } else {
                this.bindMapView(actionIDSelectedOnMarkerClick);
            }
        }
    }

    private boolean shouldMoveHeroMarker(String actionID, LatLng toLocation) {
        if (mapViewModelList.getHeroMarkerForActionID(actionID) == null) {
            return true;
        }

        LatLng fromLocation = mapViewModelList.getHeroMarkerForActionID(actionID).getPosition();
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

    private Marker setupHeroMarker(String actionID, LatLng toPosition, Float bearing, boolean fireAddedCallback) {
        if (consumerClient == null || mapViewModelList == null || mapAdapter == null)
            return null;

        // Check if Action is null
        if (consumerClient.actionForActionID(actionID) == null)
            return null;

        // Check if ActionSummary is enabled and it has to be displayed
        if (mapAdapter.showActionSummaryForActionID(this, actionID) && consumerClient.showActionSummaryForActionStatus(actionID)) {
            return null;
        }

        if (toPosition != null) {
            // Remove Hero Marker if visible
            removeHeroMarker(actionID);

            // Get HeroMarker's Anchors from HTMapAdapter
            float[] anchors = null;
            if (mapAdapter != null)
                anchors = mapAdapter.getHeroMarkerAnchorValues(this, actionID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.5f};
            }

            mapViewModelList.setHeroMarkerForActionID(actionID,
                    mMap.addMarker(new MarkerOptions()
                            .anchor(anchors[0], anchors[1])
                            .position(toPosition)));

            if (bearing != null && mapAdapter.rotateHeroMarker(this, actionID)) {
                mapViewModelList.getHeroMarkerForActionID(actionID).setRotation(bearing);
            }

            this.setHeroMarkerView(actionID);

            // Callback for Hero Marker Added
            if (fireAddedCallback && mapFragmentCallback != null) {
                mapFragmentCallback.onHeroMarkerAdded(this, actionID, mapViewModelList.getHeroMarkerForActionID(actionID));
            }
        }

        return mapViewModelList.getHeroMarkerForActionID(actionID);
    }

    private void setHeroMarkerView(String actionID) {

        if (mapAdapter != null && mapViewModelList.getHeroMarkerForActionID(actionID) != null) {

            BitmapDescriptor icon;

            if (mapAdapter.getHeroMarkerViewForActionID(this, actionID) != null) {
                View heroMarkerView = mapAdapter.getHeroMarkerViewForActionID(this, actionID);
                Bitmap bitmap = createDrawableFromView(getContext(), heroMarkerView);
                if (bitmap == null)
                    return;

                icon = BitmapDescriptorFactory.fromBitmap(bitmap);
            } else {
                icon = BitmapDescriptorFactory.fromResource(mapAdapter.getHeroMarkerIconForActionID(this, actionID));
            }

            mapViewModelList.getHeroMarkerForActionID(actionID).setIcon(icon);
        }
    }

    private void bindMapView() {
        if (mMap == null || mapAdapter == null || consumerClient == null)
            return;

        if (userInteractionDisabledMapBounds || markerSelectedOnClick != null || !TextUtils.isEmpty(actionIDSelectedOnMarkerClick))
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int points = 0;

        List<String> actionIDsToTrack = mapAdapter.actionIDsToTrack(this);

        // Include ActionMarkers in LatLngBounds, if any
        if (actionIDsToTrack != null && mapViewModelList != null) {
            for (String actionID : mapAdapter.actionIDsToTrack(this)) {
                points += addLocationsToLatLngBuilder(builder, actionID);
            }
        }

        // Include CustomMarkers in LatLngBounds, if any
        bindCustomMarkers(builder);

        // Include MultipleDestinationMarker in LatLngBounds, if any
        if (multipleActionsDestinationMarker != null) {
            builder.include(multipleActionsDestinationMarker.getPosition());
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

    private void bindMapView(String actionID) {
        if (mMap == null || mapAdapter == null || consumerClient == null)
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int points = 0;

        List<String> actionIDsToTrack = mapAdapter.actionIDsToTrack(this);

        if (actionIDsToTrack != null && actionIDsToTrack.contains(actionID) && mapViewModelList != null) {
            points += addLocationsToLatLngBuilder(builder, actionID);
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

    private int addLocationsToLatLngBuilder(LatLngBounds.Builder builder, String actionID) {
        int points = 0;

        if (mapViewModelList.getHTMapViewModelForActionID(actionID) != null) {
            if (mapViewModelList.getHeroMarkerForActionID(actionID) != null) {
                LatLng location = mapViewModelList.getHeroMarkerForActionID(actionID).getPosition();
                builder.include(location);
                points++;
            }

            if (mapViewModelList.getDestinationMarkerForActionID(actionID) != null) {
                LatLng location = mapViewModelList.getDestinationMarkerForActionID(actionID).getPosition();
                builder.include(location);
                points++;
            }

            if (!mapAdapter.disableDynamicZoom(this) && mapViewModelList.getSourceMarkerForActionID(actionID) != null) {
                LatLng location = mapViewModelList.getSourceMarkerForActionID(actionID).getPosition();
                builder.include(location);
                points++;
            }

            if (mapAdapter.showActionSummaryForActionID(this, actionID) && consumerClient.showActionSummaryForActionStatus(actionID)) {
                if (mapViewModelList.getActionSummaryStartMarkerForActionID(actionID) != null) {
                    LatLng location = mapViewModelList.getActionSummaryStartMarkerForActionID(actionID).getPosition();
                    builder.include(location);
                    points++;
                }

                if (mapViewModelList.getActionSummaryEndMarkerForActionID(actionID) != null) {
                    LatLng location = mapViewModelList.getActionSummaryEndMarkerForActionID(actionID).getPosition();
                    builder.include(location);
                    points++;
                }

                List<LatLng> polyline = mapViewModelList.getActionSummaryPolylineForActionID(actionID);
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

    private void moveMarker(final String actionID, final Marker marker, final LatLng toPosition, final float speedDuration) {
        if (marker == null) {
            return;
        }

        if (speedDuration == 0) {
            marker.setPosition(toPosition);
            return;
        }

        // Remove all existing animations in progress
        actionNavigatorList.getActionAnimationHandler(actionID).removeCallbacksAndMessages(null);

        MarkerAnimation.animateMarker(actionNavigatorList.getActionAnimationHandler(actionID), marker, toPosition, speedDuration);
    }

    @Override
    public void onTouchDown(MotionEvent event) {

        if (editDestinationViewVisible) {
            actionEndPlaceConfirmDialog.setVisibility(View.GONE);
            actionEndPlaceConfirmLayout.setVisibility(View.GONE);
            actionEndEditPlaceConfirmDialogTriangle.setVisibility(View.GONE);
            actionEndPlaceConfirmDialogVerticalLine.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTouchUp(MotionEvent event) {

        // Show EditDestination Views, in case visible
        if (editDestinationViewVisible) {
            AnimationUtils.expand(actionEndPlaceConfirmDialog, 100);
            AnimationUtils.expand(actionEndPlaceConfirmLayout, 100);

            actionEndEditPlaceConfirmDialogTriangle.setVisibility(View.VISIBLE);
            actionEndPlaceConfirmDialogVerticalLine.setVisibility(View.VISIBLE);
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
                    if (consumerClient != null && mapAdapter.actionIDsToTrack(HyperTrackMapFragment.this) != null) {
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
                            (!TextUtils.isEmpty(actionIDSelectedOnMarkerClick) || markerSelectedOnClick != null)) {

                        // Reset UserInfoLayout & AddressInfoLayout Change their visibility
                        HyperTrackMapFragment.this.resetUserAndAddressInfo();
                        HyperTrackMapFragment.this.resetActionSummaryInfoViews();

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
                        if (mapAdapter != null && !TextUtils.isEmpty(editDestinationActionID)
                                && mapAdapter.shouldShowUserLocationMissingAlert(HyperTrackMapFragment.this, editDestinationActionID)) {
                            Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_action_destination_my_location_error_msg, Toast.LENGTH_SHORT).show();
                        }

                        // Callback for User Location Error Received
                        if (mapFragmentCallback != null)
                            mapFragmentCallback.onReceiveUserLocationMissingError(HyperTrackMapFragment.this,
                                    editDestinationActionID, getActivity().getString(com.hypertrack.lib.R.string.edit_action_destination_my_location_error_msg));
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
        if (consumerClient == null || mapViewModelList == null || mapAdapter == null || mapViewModelList.getHTMapViewModelList() == null) {
            return;
        }

        String lastActionIDSelectedOnMarkerClick = null;
        if (!TextUtils.isEmpty(actionIDSelectedOnMarkerClick)) {
            lastActionIDSelectedOnMarkerClick = actionIDSelectedOnMarkerClick;
        }

        boolean destinationMarkerClicked = false;

        // Check if the ic_ht_hero_marker clicked exists in HTMapViewModelList
        for (String actionID : mapViewModelList.getHTMapViewModelList().keySet()) {
            HTMapViewModel mapViewModel = mapViewModelList.getHTMapViewModelForActionID(actionID);

            // Handle Hero Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getHeroMarkerId())) {

                if (mapFragmentCallback != null)
                    mapFragmentCallback.onHeroMarkerClicked(this, actionID, marker);

                actionIDSelectedOnMarkerClick = actionID;
                break;
            }

            // Handle Source Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getSourceMarkerId())) {
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onSourceMarkerClicked(this, actionID, marker);

                actionIDSelectedOnMarkerClick = actionID;
                break;
            }

            // Handle Destination Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getDestinationMarkerId())) {
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onDestinationMarkerClicked(this, actionID, marker);

                actionIDSelectedOnMarkerClick = actionID;
                destinationMarkerClicked = true;
                break;
            }

            // Handle ActionSummaryStart Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getActionSummaryStartMarkerId())) {
                actionIDSelectedOnMarkerClick = actionID;
                break;
            }

            // Handle ActionSummaryEnd Marker Click
            if (marker.getId().equalsIgnoreCase(mapViewModel.getActionSummaryEndMarkerId())) {
                actionIDSelectedOnMarkerClick = actionID;
                break;
            }
        }

        if (multipleActionsDestinationMarker != null && marker.equals(multipleActionsDestinationMarker)) {
            if (mapFragmentCallback != null)
                mapFragmentCallback.onMultipleActionsDestinationMarkerClicked(this, multipleActionsDestinationMarker);
        }

        // Remove DestinationMarker for last selected action
        // Check for ActionID as we don't want to remove destination ic_ht_hero_marker if a ic_ht_hero_marker was clicked which
        // is not mentioned above
        if (!TextUtils.isEmpty(actionIDSelectedOnMarkerClick)) {
            if (!destinationMarkerClicked) {
                HyperTrackMapFragment.this.removeAllDestinationMarkers();
            }

            // Not a valid Action ic_ht_hero_marker, so fire callback for custom ic_ht_hero_marker click
        } else if (mapFragmentCallback != null) {
            mapFragmentCallback.onCustomMarkerClicked(HyperTrackMapFragment.this, marker);
        }

        // Show User & Address Info for ActionIDSelectedOnMarkerClick
        if (!TextUtils.isEmpty(actionIDSelectedOnMarkerClick)) {

            // Check if the current selected ic_ht_hero_marker is for the same actionID as the last selected ic_ht_hero_marker
            if (!actionIDSelectedOnMarkerClick.equalsIgnoreCase(lastActionIDSelectedOnMarkerClick)) {
                showUserAndAddressInfo(actionIDSelectedOnMarkerClick);
                updateOrderStatusToolbar(actionIDSelectedOnMarkerClick);
                removeMultipleActionsDestinationMarker();
            }

            // Check if destination ic_ht_hero_marker has been enabled for this actionID
            if (mapAdapter.showDestinationMarkerForActionID(this, actionIDSelectedOnMarkerClick)) {
                LatLng destinationLocation = consumerClient.getDestinationLocationLatLng(actionIDSelectedOnMarkerClick);
                setupDestinationMarker(actionIDSelectedOnMarkerClick, destinationLocation);
            }

            // Refocus map if the user has not disabled map bounds
            if (!userInteractionDisabledMapBounds) {
                bindMapView(actionIDSelectedOnMarkerClick);
                userInteractionDisabledMapBounds = true;
            }
        } else {

            // No Marker matched the selected one Check if a different ic_ht_hero_marker was selected than the current one
            // Reset UserInfoLayout & AddressInfoLayout Change their visibility
            HyperTrackMapFragment.this.resetUserAndAddressInfo();
            HyperTrackMapFragment.this.resetDestinationMarker();
            HyperTrackMapFragment.this.resetActionSummaryInfoViews();
        }
    }

    private void showUserAndAddressInfo(String actionID) {
        if (mapAdapter != null) {

            // Get the User Info Data if User View has to be shown
            if (mapAdapter.showUserInfoForActionID(HyperTrackMapFragment.this, actionID)) {
                this.updateUserInformation(actionID);

            } else {
                // Hide UserInfoLayout as per the setting for current actionID
                this.setUserInfoLayoutVisibility(actionID, false);
            }

            // Set AddressInfoLayout Visibility
            this.updateAddressInfo(actionID);
        }
    }

    private void updateUserInformation(String actionID) {

        if (consumerClient.getUser(actionID) != null) {

            // Remove UserImage Download Action
            if (userImageDownloadTask != null) {
                userImageDownloadTask.cancel(true);
                userImageDownloadTask = null;
            }

            HTUser user = consumerClient.getUser(actionID);

            if (user.getName() != null) {
                userName.setText(user.getName());

                if (TextUtils.isEmpty(user.getPhone()))
                    userContactNumber.setVisibility(View.GONE);
                else {
                    userContactNumber.setVisibility(View.VISIBLE);
                }

                userProfileImage.setImageResource(com.hypertrack.lib.R.drawable.ht_profile_image);

                if (user.getPhotoURL() != null) {
                    userImageDownloadTask = new HTDownloadImageTask(userProfileImage)
                            .execute(user.getPhotoURL());
                }

                // Hide UserInfoLayout as per the setting for current actionID
                this.setUserInfoLayoutVisibility(actionID, true);
                return;
            }
        }

        // Hide UserInfoLayout as per the setting for current actionID
        this.setUserInfoLayoutVisibility(actionID, false);
    }

    private void setUserInfoLayoutVisibility(final String actionID, boolean showLayout) {

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
            if (actionID == null)
                return;

            userInfoLayout.setVisibility(View.VISIBLE);

            if (userProfileImage != null) {
                userProfileImage.setVisibility(View.VISIBLE);
            }

            if (userContactNumber != null) {
                if (mapAdapter.showCallButtonForActionID(this, actionID)) {
                    final HTUser user = consumerClient.getUser(actionID);
                    if (TextUtils.isEmpty(user.getPhone()))
                        userContactNumber.setVisibility(View.GONE);
                    else
                        userContactNumber.setVisibility(View.VISIBLE);
                    userContactNumber.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (getActivity() == null || getActivity().isFinishing() ||
                                    consumerClient == null || consumerClient.getUser(actionID) == null) {
                                return;
                            }


                            if (!TextUtils.isEmpty(user.getPhone())) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + user.getPhone()));
                                startActivity(intent);
                            }

                            if (mapFragmentCallback != null)
                                mapFragmentCallback.onCallButtonClicked(HyperTrackMapFragment.this, actionID);
                        }
                    });
                } else {
                    userContactNumber.setVisibility(View.GONE);
                    userContactNumber.setOnClickListener(null);
                }
            }

            if (ctaButton != null) {
                if (mapAdapter.showOrderDetailsButtonForActionID(this, actionID)) {
                    ctaButton.setVisibility(View.VISIBLE);
                    ctaButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mapFragmentCallback != null)
                                mapFragmentCallback.onOrderDetailsButtonClicked(HyperTrackMapFragment.this, actionID);
                        }
                    });
                } else {
                    ctaButton.setVisibility(View.GONE);
                    ctaButton.setOnClickListener(null);
                }
            }
        }

        if (actionID != null) {
            if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
                mapViewModelList.getHTMapViewModelForActionID(actionID).setUserInfoVisible(showLayout);

            this.updateMapPadding(actionID);
        } else {
            resetMapPadding();
        }
    }

    private void showMultipleActionsUserInfo() {
        if (getActivity() == null || getActivity().isFinishing() || mapAdapter == null)
            return;

        // Check if UserInfo Layout has been hidden for Multiple Actions view
        if (mapAdapter.showUserInfoForMultipleActionsView(this)) {

            // Show UserInfo Layout with default text
            userName.setText(getString(com.hypertrack.lib.R.string.multiple_action_tracking_user_info_text));
            userInfoLayout.setVisibility(View.VISIBLE);

            // Hide actionID specific views from UserInfo Layout
            userProfileImage.setVisibility(View.GONE);
            userContactNumber.setVisibility(View.GONE);
            ctaButton.setVisibility(View.GONE);
        } else {
            userInfoLayout.setVisibility(View.GONE);
        }
    }

    private void showMultipleActionsAddressInfo() {
        if (getActivity() == null || getActivity().isFinishing() || mapAdapter == null || consumerClient == null
                || consumerClient.getActionIDList() == null || consumerClient.getActionIDList().size() < 1)
            return;

        // Check if AddressInfo Layout has been hidden for Multiple Actions view
        if (mapAdapter.showAddressInfoViewForMultipleActionsView(this)) {

            // Fetch actionID for first action being tracked
            String actionID = consumerClient.getActionIDList().get(0);
            HTAction action = consumerClient.actionForActionID(actionID);

            if (action != null && action.getExpectedPlace() != null
                    && action.getExpectedPlace().getDisplayString() != null) {
                this.actionEndAddressTextView.setText(action.getExpectedPlace().getDisplayString());

                this.setAddressInfoVisibility(actionID, true);
                return;
            }
            this.setAddressInfoVisibility(actionID, false);
        } else {
            addressInfoLayout.setVisibility(View.GONE);
        }
    }

    private void resetDestinationMarker() {
        if (!editDestinationViewVisible) {

            if (mapAdapter == null || consumerClient == null || consumerClient.getActionIDList() == null
                    || consumerClient.getActionIDList().isEmpty())
                return;

            if (mapAdapter.getActionIDsToTrackCount(this) > 1) {
                HyperTrackMapFragment.this.removeAllDestinationMarkers();

                if (mapAdapter.showMultipleActionsDestinationMarker(this)) {
                    String actionID = mapAdapter.actionIDsToTrack(this).get(0);
                    LatLng destinationLocation = consumerClient.getDestinationLocationLatLng(actionID);
                    setupMultipleActionsDestinationMarker(actionID, destinationLocation);
                    return;
                }
            } else if (mapAdapter.getActionIDsToTrackCount(this) == 1) {
                HyperTrackMapFragment.this.removeAllDestinationMarkers();

                String actionID = mapAdapter.actionIDsToTrack(this).get(0);
                if (!TextUtils.isEmpty(actionID) && mapAdapter.showDestinationMarkerForActionID(this, actionID)) {
                    LatLng destinationLocation = consumerClient.getDestinationLocationLatLng(actionID);
                    setupDestinationMarker(actionID, destinationLocation);
                }
            }

            this.removeMultipleActionsDestinationMarker();
        }
    }

    private boolean checkIfAnyActionIsActive(List<String> actionIDList) {
        if (consumerClient == null || actionIDList == null || actionIDList.isEmpty())
            return false;

        for (String actionID : actionIDList) {
            HTAction action = consumerClient.actionForActionID(actionID);
            if (!(action.isCompleted() || action.isCanceled())) {
                return true;
            }
        }

        return false;
    }

    private void setupMultipleActionsDestinationMarker(String actionID, LatLng location) {
        if (mapAdapter == null)
            return;

        // Setup Destination Marker Only if EditDestination Overlay is not visible
        if (!editDestinationViewVisible) {

            if (location == null) {
                return;
            }

            // Remove MultipleActions DestinationMarker
            this.removeMultipleActionsDestinationMarker();

            // Get MultipleActions DestinationMarker's Anchors from HTMapAdapter
            float[] anchors = null;
            anchors = mapAdapter.getMultipleActionsDestinationMarkerAnchorValues(this, actionID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.915f};
            }

            View markerView = mapAdapter.getMultipleActionsDestinationMarkerView(this);

            if (markerView != null) {
                Bitmap markerBitmap = createDrawableFromView(getContext(), markerView);
                if (markerBitmap != null) {
                    multipleActionsDestinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .anchor(anchors[0], anchors[1])
                            .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));

                    return;
                }

                // Callback for Destination Marker Added
                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onMultipleActionsDestinationMarkerAdded(this, multipleActionsDestinationMarker);
                }
            }

            multipleActionsDestinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .anchor(anchors[0], anchors[1])
                    .icon(BitmapDescriptorFactory.fromResource(
                            mapAdapter.getMultipleActionsDestinationMarkerIcon(this))));

            // Callback for Destination Marker Added
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onMultipleActionsDestinationMarkerAdded(this, multipleActionsDestinationMarker);
            }
        }
    }

    private void updateOrderStatusToolbar(String actionID) {
        if (mapAdapter == null || orderStatusToolbar == null || consumerClient == null || !this.isAdded()
                || getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (mapAdapter.showOrderStatusToolbar(this)) {
            AnimationUtils.expand(orderStatusToolbar, 200);

            // Handle Order StatusToolbar for Multiple Action Tracking with single destination
            if (TextUtils.isEmpty(actionID) && mapAdapter.getActionIDsToTrackCount(this) > 1) {
                int count = mapAdapter.getActionIDsToTrackCount(this);
                if (count > 1) {

                    String multipleActionsTitle = mapAdapter.getMultipleActionsOrderStatusToolbarTitle(this);
                    if (TextUtils.isEmpty(multipleActionsTitle)) {
                        multipleActionsTitle = count + " " + getString(com.hypertrack.lib.R.string.multiple_action_tracking_suffix);
                    }

                    orderStatusToolbar.setTitle(multipleActionsTitle);
                    orderStatusToolbar.setSubtitle("");
                }

            } else {
                // Handle Order StatusToolbar for Special Case of Single Action Tracking
                if (mapAdapter.getActionIDsToTrackCount(this) == 1) {
                    actionID = mapAdapter.actionIDsToTrack(this) != null ? mapAdapter.actionIDsToTrack(this).get(0) : null;
                }

                orderStatusToolbar.setTitle(getOrderStatusToolbarTitle(actionID));
                orderStatusToolbar.setSubtitle(consumerClient.getActionDisplaySubStatusText(actionID));
            }

        } else {
            AnimationUtils.collapse(orderStatusToolbar, 200);
        }
    }

    private String getOrderStatusToolbarTitle(String actionID) {
        if (!(getActivity() == null || getActivity().isFinishing() || consumerClient == null || TextUtils.isEmpty(actionID) || TextUtils.isEmpty(consumerClient.getActionDisplayStatusText(actionID))))
            return consumerClient.getActionDisplayStatusText(actionID);

        return mapAdapter.getOrderStatusToolbarDefaultTitle(this);


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

        for (String actionID : mapAdapter.actionIDsToTrack(this)) {
            if (mapViewModelList.getHeroMarkerForActionID(actionID) != null) {
                mapViewModelList.getHeroMarkerForActionID(actionID).hideInfoWindow();
            }

            if (mapViewModelList.getDestinationMarkerForActionID(actionID) != null) {
                mapViewModelList.getDestinationMarkerForActionID(actionID).hideInfoWindow();
            }

            if (mapViewModelList.getSourceMarkerForActionID(actionID) != null) {
                mapViewModelList.getSourceMarkerForActionID(actionID).hideInfoWindow();
            }
        }
    }

    private void onUpdateAllActions(List<String> actionIDList) {
        if (mapAdapter == null || consumerClient == null)
            return;

        List<String> actionIDsToTrack;

        if (actionIDList == null || actionIDList.isEmpty()) {
            actionIDList = mapAdapter.actionIDsToTrack(HyperTrackMapFragment.this);
            actionIDsToTrack = actionIDList;

            if (actionIDList == null || actionIDList.isEmpty())
                return;
        } else {
            actionIDsToTrack = mapAdapter.actionIDsToTrack(HyperTrackMapFragment.this);
        }

        for (String actionID : actionIDList) {
            // Update action Data if current actionID exists in actionIDsToTrack List
            if (actionIDsToTrack.contains(actionID)) {
                onUpdateAction(actionID);
            }
        }

        if (actionIDsToTrack.size() == 1 && actionIDList.size() == 1 && mapAdapter.showDestinationMarkerForActionID(this, actionIDList.get(0))) {
            LatLng destinationLocation = consumerClient.getDestinationLocationLatLng(actionIDList.get(0));
            setupDestinationMarker(actionIDList.get(0), destinationLocation);
        }

        // Reset Map Bounds
        if (mapAdapter.shouldBindView() && !userInteractionDisabledMapBounds) {
            bindMapView();
        }
    }

    private void onUpdateAction(String actionID) {
        if (getActivity() == null || getActivity().isFinishing())
            return;

        if (mMap == null || mapAdapter == null || consumerClient == null || consumerClient.getStatus(actionID) == null)
            return;

        if (mapViewModelList.getHTMapViewModelForActionID(actionID) == null) {
            mapViewModelList.addHTMapViewModelForActionID(actionID);
        }

        this.updateMapMarkers(actionID);

        if (consumerClient.actionForActionID(actionID) != null) {

            // Check if ActionSummary has to be displayed
            if (mapAdapter.showActionSummaryForActionID(this, actionID) && consumerClient.showActionSummaryForActionStatus(actionID)) {
                // Stop the timer to update ETA displayed on DestinationMarker
                stopETATimer();

                // Check if ActionSummary view has been enabled
                if (mapAdapter.showActionSummaryForActionID(this, actionID)) {
                    // Remove HeroMarker on actionCompleted
                    removeHeroMarker(actionID);
                    updateMapWithActionSummary(actionID);
                }
            } else {
                // Start the timer to update ETA displayed on DestinationMarker
                startETATimer(actionID);

                // Process updated Action's TimeAwarePolyline for navigation
                actionNavigatorList.processTimeAwarePolyline(consumerClient.actionForActionID(actionID));
            }
        }
    }

    private void updateMapMarkers(String actionID) {

        if (consumerClient == null || mMap == null || mapViewModelList == null)
            return;

        if (mapAdapter.showSourceMarkerForActionID(this, actionID)) {
            setupSourceMarker(actionID);
        }
    }

    private void setupSourceMarker(String actionID) {
        if (consumerClient == null || mMap == null || mapAdapter == null)
            return;

        LatLng location = consumerClient.getStartedLocationLatLng(actionID);
        if (location == null) {
            return;
        }

        if (mapViewModelList.getSourceMarkerForActionID(actionID) == null) {
            BitmapDescriptor sourceMarkerIcon = null;

            if (mapAdapter.getSourceMarkerViewForActionID(this, actionID) != null) {
                View sourceMarkerView = mapAdapter.getSourceMarkerViewForActionID(this, actionID);
                if (createDrawableFromView(getContext(), sourceMarkerView) != null) {
                    sourceMarkerIcon = BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), sourceMarkerView));
                }
            } else {
                sourceMarkerIcon = BitmapDescriptorFactory.fromResource(mapAdapter.getSourceMarkerIconForActionID(this, actionID));
            }

            // Get SourceMarker's Anchors from HTMapAdapter
            float[] anchors = null;
            if (mapAdapter != null)
                anchors = mapAdapter.getSourceMarkerAnchorValues(this, actionID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 1.0f};
            }

            if (sourceMarkerIcon != null) {
                mapViewModelList.setSourceMarkerForActionID(actionID,
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .anchor(anchors[0], anchors[1])
                                .icon(sourceMarkerIcon)));

                // Callback for Source Marker Added
                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onSourceMarkerAdded(this, actionID, mapViewModelList.getSourceMarkerForActionID(actionID));
                }
            }

            return;
        }

        if (mapViewModelList.getSourceMarkerForActionID(actionID).getPosition().equals(location)) {
            return;
        }

        mapViewModelList.getSourceMarkerForActionID(actionID).setPosition(location);
    }

    private void setupDestinationMarker(String actionID, LatLng location) {
        if (mMap == null || mapAdapter == null || consumerClient == null || TextUtils.isEmpty(actionID)
                || location == null)
            return;

        if (consumerClient.actionForActionID(actionID) == null)
            return;

        // Check if ActionSummary is enabled
        if (mapAdapter.showActionSummaryForActionID(this, actionID) && consumerClient.showActionSummaryForActionStatus(actionID))
            return;

        // Setup Destination Marker Only if EditDestination Overlay is not visible
        if (!editDestinationViewVisible) {

            // Remove Destination Marker in all scenarios
            this.removeMultipleActionsDestinationMarker();
            this.removeAllDestinationMarkers();

            // Get DestinationMarker's Anchors from HTMapAdapter
            float[] anchors = mapAdapter.getDestinationMarkerAnchorValues(this, actionID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.915f};
            }

            View markerView = this.getCustomMarkerView(actionID);

            if (markerView != null && createDrawableFromView(getContext(), markerView) != null) {
                mapViewModelList.setDestinationMarkerForActionID(actionID,
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .anchor(anchors[0], anchors[1])
                                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), markerView)))));

                // Callback for Destination Marker Added
                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onDestinationMarkerAdded(this, actionID, mapViewModelList.getDestinationMarkerForActionID(actionID));
                }
                return;
            }

            // Get MultipleActionsDestinationMarker's Anchors from HTMapAdapter
            anchors = mapAdapter.getMultipleActionsDestinationMarkerAnchorValues(this, actionID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.915f};
            }

            // Add MultipleActionsViewMarker in case a valid DestinationMarkerView was not set or if ETA was not valid
            mapViewModelList.setDestinationMarkerForActionID(actionID,
                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .anchor(anchors[0], anchors[1])
                            .icon(BitmapDescriptorFactory.fromResource(mapAdapter.getMultipleActionsDestinationMarkerIcon(this)))));

            // Callback for Destination Marker Added
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onDestinationMarkerAdded(this, actionID, mapViewModelList.getDestinationMarkerForActionID(actionID));
            }
        }
    }

    private View getCustomMarkerView(String actionID) {
        if (consumerClient == null || getActivity() == null || getActivity().isFinishing())
            return null;

        View marker = mapAdapter.getDestinationMarkerViewForActionID(this, actionID);
        this.etaTimeTextView = (TextView) marker.findViewById(com.hypertrack.lib.R.id.eta_txt);
        this.etaTimeSuffixTextView = (TextView) marker.findViewById(com.hypertrack.lib.R.id.eta_txt_suffix);
        this.etaIcon = (ImageView) marker.findViewById(com.hypertrack.lib.R.id.eta_icon);

        // TODO: 13/07/16 HACK to check whether View provided to us by Adapter was ht_custom_marker_layout or not
        if (this.etaTimeTextView != null && this.etaTimeSuffixTextView != null && this.etaIcon != null) {

            // Return a default destination ic_ht_hero_marker without a valid eta
            Integer etaInMinutes = consumerClient.getActionDisplayETA(actionID);
            if (etaInMinutes == null || etaInMinutes <= 0) {
                return ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(com.hypertrack.lib.R.layout.ht_invalid_eta_marker_layout, null);
            }

            this.etaIcon.setImageResource(mapAdapter.getDestinationMarkerIconForActionID(this, actionID));
            updateETAInfo(actionID);
        }

        return marker;
    }

    private void updateETAInfo(String actionID) {
        if (this.etaTimeTextView == null || this.etaTimeSuffixTextView == null) {
            return;
        }

        Integer etaInMinutes = consumerClient.getActionDisplayETA(actionID);
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

    private void startETATimer(final String actionID) {
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
                        updateETAInfo(actionID);
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

    private void setAddressInfoVisibility(String actionID, boolean flag) {
        if (flag) {
            this.addressInfoLayout.setVisibility(View.VISIBLE);
        } else {
            this.addressInfoLayout.setVisibility(View.GONE);
        }

        this.updateMapPadding(actionID);
    }

    private void updateAddressInfo(String actionID) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        if (consumerClient == null || consumerClient.actionForActionID(actionID) == null) {
            return;
        }

        this.setAddressInfoVisibility(actionID, mapAdapter.showAddressInfoViewForActionID(this, actionID));

        HTAction action = consumerClient.actionForActionID(actionID);

        if (action == null)
            return;

        // Check if Action CompletedAddress is available
        this.actionEndAddressTextView.setText(consumerClient.getCompletedAddress(actionID));

        // Check if Address Info View has to be displayed for a completed Action denoted by given actionID
        if (mapAdapter.showActionSummaryForActionID(this, actionID) && consumerClient.showActionSummaryForActionStatus(actionID)) {
            this.enableEndPlaceEditView(false, null);
            this.actionDurationTextView.setText(consumerClient.getActionMeteringString(getActivity(), actionID));
            this.actionStartAddressTextView.setText(consumerClient.getStartPlaceAddress(actionID));
            this.actionStartTimeTextView.setText(action.getActionStartTimeDisplayString());
            this.actionEndTimeTextView.setText(action.getActionEndTimeDisplayString());
            this.actionDateTextView.setText(action.getActionDateDisplayString());
            this.setActionSummaryVisibility(View.VISIBLE);

        } else {
            this.actionEndTimeTextView.setText("");
            this.enableEndPlaceEditView(true, actionID);
            this.resetActionSummaryInfoViews();
        }
    }

    private void enableEndPlaceEditView(boolean enable, final String actionID) {
        if (getActivity() == null || getActivity().isFinishing() || true) {
            return;
        }

        if (enable && !mapAdapter.disableEditDestinationForActionID(this, actionID)) {

            if (TextUtils.isEmpty(actionID) || consumerClient == null || consumerClient.actionForActionID(actionID) == null)
                return;

            actionEndPlaceEditIcon.setVisibility(View.VISIBLE);
            actionEndPlaceLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getActivity() != null && !getActivity().isFinishing()) {

                        final int actionResId = consumerClient.taskForActionID(actionID);
                        final String actionText = getString(actionResId);

                        AlertDialog.Builder builder = new AlertDialog.Builder((new ContextThemeWrapper(getActivity(), R.style.HTEditDestinationAlertDialogTheme)));
                        builder.setTitle(getString(com.hypertrack.lib.R.string.edit_action_destination_cnf_dialog, actionText));
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Callback for Begin Editing Destination
                                if (mapFragmentCallback != null)
                                    mapFragmentCallback.onBeginEditingDestination(HyperTrackMapFragment.this, actionID);

                                if (TextUtils.isEmpty(actionID) || consumerClient.actionForActionID(actionID).getExpectedPlace() == null
                                        || consumerClient.actionForActionID(actionID).getExpectedPlace().getLocation() == null) {
                                    Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_action_destination_location_unavailable_error_msg,
                                            Toast.LENGTH_SHORT).show();
                                    HTLog.e(TAG, "Error occurred while setupEditDestinationLayout: DestinationLocation is null");
                                    dialog.dismiss();
                                }

                                setupEditDestinationLayout(actionID, actionText);
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Callback for Cancelled Editing Destination
                                if (mapFragmentCallback != null)
                                    mapFragmentCallback.onCanceledEditingDestination(HyperTrackMapFragment.this, actionID);

                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                }
            });
        } else {
            actionEndPlaceEditIcon.setVisibility(View.GONE);
            actionEndPlaceConfirmBtn.setVisibility(View.GONE);
            actionEndPlaceEditLayout.setVisibility(View.GONE);
            actionEndPlaceLayout.setOnClickListener(null);
        }
    }

    private void setupEditDestinationLayout(String actionID, String actionText) {
        if (getActivity() == null || getActivity().isFinishing())
            return;

        editDestinationActionID = actionID;

        // Get Current LatLng for Action DestinationLocation
        HTPlace destination = consumerClient.actionForActionID(actionID).getExpectedPlace();
        if (destination == null || destination.getLocation() == null) {
            Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_action_destination_location_unavailable_error_msg,
                    Toast.LENGTH_SHORT).show();
            HTLog.e(TAG, "Error occurred while setupEditDestinationLayout: DestinationLocation is null");
            return;
        }

        double[] coordinates = destination.getLocation().getCoordinates();
        LatLng editDestinationLatLng = new LatLng(coordinates[1], coordinates[0]);

        if (editDestinationLatLng.latitude == 0.0 || editDestinationLatLng.longitude == 0.0) {
            Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_action_destination_location_unavailable_error_msg,
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

        // Remove Destination Marker For All Actions
        HyperTrackMapFragment.this.removeAllDestinationMarkers();

        // Enable MyLocation Button on Map
        if (checkForLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }

        // Update Map Padding
        updateMapPadding(actionID);

        // Show Edit Destination Layout
        showEditDestination(actionID, editDestinationLatLng, actionText);
    }

    private void showEditDestination(final String actionID, final LatLng editDestinationLatLng, final String actionText) {
        if (mapViewModelList == null || editDestinationLatLng == null)
            return;

        // Set End Place Layout Dismiss Icon ClickListener
        actionEndEditPlaceDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the Edit Destination Layouts & Restore the previous states
                hideEditDestinationLayout(actionID, editDestinationLatLng);

                // Callback for Cancelled Editing Destination
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onCanceledEditingDestination(HyperTrackMapFragment.this, actionID);
            }
        });

        // Move the Destination Marker to the center of the map
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(editDestinationLatLng, 16));

        // Set Confirm Button Click Listener & Make the Views Visible
        actionEndPlaceConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;

                if (mMap != null) {

                    if (mMap.getCameraPosition().zoom < 13) {
                        // Check if Edit Destination Failure Alert has been enabled
                        if (mapAdapter.shouldShowEditDestinationFailureAlert(HyperTrackMapFragment.this, actionID)) {
                            Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_action_destination_zoom_level_error_msg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        final LatLng updatedDestinationLatLng = mMap.getCameraPosition() != null ? mMap.getCameraPosition().target : null;
                        // Initiate Destination location update
                        updateDestinationLocation(actionID, updatedDestinationLatLng, actionText);
                    }
                }
            }
        });

        // Set Confirm Destination Texts depending on Action Action type
        actionEndPlaceConfirmDialogTitle.setText(getString(com.hypertrack.lib.R.string.edit_action_destination_header_title, actionText));
        actionEndPlaceConfirmDialogMessage.setText(getString(com.hypertrack.lib.R.string.edit_action_destination_header_message, actionText));

        actionEndPlaceConfirmBtn.setVisibility(View.VISIBLE);
        actionEndPlaceEditLayout.setVisibility(View.VISIBLE);
    }

    private void updateDestinationLocation(final String actionID, final LatLng updatedDestinationLatLng, final String actionText) {
        if (consumerClient == null || TextUtils.isEmpty(actionID) || updatedDestinationLatLng == null)
            return;

        GeoJSONLocation location = new GeoJSONLocation(updatedDestinationLatLng.latitude, updatedDestinationLatLng.longitude);

        mapLayoutLoaderLayout.setVisibility(View.VISIBLE);

        // Perform Update Destination Location Network Call
        consumerClient.updateDestinationLocation(actionID, location, new UpdateDestinationCallback() {
            @Override
            public void onSuccess(HTAction action) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;

                mapLayoutLoaderLayout.setVisibility(View.GONE);

                // Hide the Edit Destination Layouts & Restore the previous states
                GeoJSONLocation destinationLocation = action.getExpectedPlace() != null ? action.getExpectedPlace().getLocation() : null;

                if (destinationLocation == null) {
                    destinationLocation = new GeoJSONLocation(updatedDestinationLatLng.latitude,
                            updatedDestinationLatLng.longitude);
                }

                // Hide the Edit Destination Layouts & Restore the previous states
                hideEditDestinationLayout(actionID, new LatLng(destinationLocation.getLatitude(),
                        destinationLocation.getLongitude()));

                // Show Edit Destination Success Message
                Toast.makeText(getActivity(), getString(com.hypertrack.lib.R.string.edit_action_destination_success_msg, actionText),
                        Toast.LENGTH_SHORT).show();

                // Callback for Edit Destination completed
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onEndEditingDestination(HyperTrackMapFragment.this, actionID);
            }

            @Override
            public void onError(Exception exception) {
                if (getActivity() == null || getActivity().isFinishing())
                    return;

                mapLayoutLoaderLayout.setVisibility(View.GONE);

                if (mapAdapter.shouldShowEditDestinationFailureAlert(HyperTrackMapFragment.this, actionID)) {
                    Toast.makeText(getActivity(), com.hypertrack.lib.R.string.edit_action_destination_error_msg, Toast.LENGTH_SHORT).show();
                }

                // Callback for Error occurred in Edit Destination
                if (mapFragmentCallback != null)
                    mapFragmentCallback.onReceiveEditDestinationError(HyperTrackMapFragment.this, actionID, exception.getMessage());
            }
        });
    }

    private void hideEditDestinationLayout(String actionID, LatLng destinationLatLng) {
        if (getActivity() == null || getActivity().isFinishing() || mapAdapter == null)
            return;

        // Reset the Flag to indicate EditDestinationView Visible
        editDestinationViewVisible = false;
        editDestinationActionID = "";

        // Hide the Confirm Destination Layouts
        actionEndPlaceConfirmBtn.setVisibility(View.GONE);
        actionEndPlaceEditLayout.setVisibility(View.GONE);

//        HyperTrackMapFragment.this.resetUserAndAddressInfo();
//        HyperTrackMapFragment.this.resetDestinationMarker();

        updateMapPadding(actionID);

        // Reset Address Info View to its default state
        this.showUserAndAddressInfo(actionID);
        this.setupDestinationMarker(actionID, destinationLatLng);

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

    private void setActionSummaryVisibility(int visibility) {
        this.actionSummaryLayout.setVisibility(visibility);
        this.actionStartLayout.setVisibility(visibility);
        this.actionVerticalSeparator.setVisibility(visibility);
    }

    private void updateMapWithActionSummary(String actionID) {
        if (consumerClient == null
                || consumerClient.actionForActionID(actionID) == null) {
            return;
        }

        this.removeHeroMarker(actionID);
        this.removeAllDestinationMarkers();
        this.removeSourceMarker(actionID);

        modifyMapForTripSummary();
        addTripSummaryPolyline(actionID);

        LatLng sourceLocationLatLng = consumerClient.getStartedLocationLatLng(actionID);

        //if sourceLocation LatLng is null take first LatLng of polyline.
        if (sourceLocationLatLng == null)
            sourceLocationLatLng = mapViewModelList.getActionSummaryPolylineForActionID(actionID).get(0);


        if (sourceLocationLatLng != null) {

            this.removeActionSummaryStartMarker(actionID);

            // Get ActionSummaryStartMarker's Anchors from HTMapAdapter
            float[] anchors = null;
            if (mapAdapter != null)
                anchors = mapAdapter.getActionSummaryStartMarkerAnchorValues(this, actionID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.5f};
            }

            mapViewModelList.setActionSummaryStartMarkerForActionID(actionID,
                    mMap.addMarker(new MarkerOptions()
                            .position(sourceLocationLatLng)
                            .anchor(anchors[0], anchors[1])
                            .icon(BitmapDescriptorFactory.fromResource(
                                    mapAdapter.getActionSummaryStartMarkerIconForActionID(this, actionID)))));
        }

        LatLng CompletedLocationLatLng = consumerClient.getCompletedLocationLatLng(actionID);

        if (CompletedLocationLatLng == null)
            CompletedLocationLatLng = consumerClient.getDestinationLocationLatLng(actionID);

        if (CompletedLocationLatLng != null) {

            this.removeActionSummaryEndMarker(actionID);

            // Get ActionSummaryEndMarker's Anchors from HTMapAdapter
            float[] anchors = null;
            if (mapAdapter != null)
                anchors = mapAdapter.getActionSummaryStartMarkerAnchorValues(this, actionID);

            if (anchors == null || anchors.length < 2 || anchors[0] < 0.0f || anchors[0] > 1.0f
                    || anchors[1] < 0.0f || anchors[1] > 1.0f) {
                anchors = new float[]{0.5f, 0.5f};
            }

            mapViewModelList.setActionSummaryEndMarkerForActionID(actionID,
                    mMap.addMarker(new MarkerOptions()
                            .position(CompletedLocationLatLng)
                            .anchor(anchors[0], anchors[1])
                            .icon(BitmapDescriptorFactory.fromResource(
                                    mapAdapter.getActionSummaryEndMarkerIconForActionID(this, actionID)))));
        }


    }

    private void modifyMapForTripSummary() {
        this.setBoundsButton.setVisibility(View.GONE);
        this.mMap.setTrafficEnabled(false);
    }

    private void addTripSummaryPolyline(String actionID) {
        if (mapViewModelList == null
                || consumerClient == null
                || consumerClient.actionForActionID(actionID) == null
                || consumerClient.actionForActionID(actionID).getEncodedPolyline() == null) {
            return;
        }

        String encodedPolyline = consumerClient.actionForActionID(actionID).getEncodedPolyline();

        mapViewModelList.setActionSummaryPolylineForActionID(actionID, HTMapUtils.decode(encodedPolyline));
        PolylineOptions options = new PolylineOptions().width(8).color(Color.parseColor("#0A61C2"));

        options.addAll(mapViewModelList.getActionSummaryPolylineForActionID(actionID));

        mMap.addPolyline(options);
        updateMapBounds(actionID, mapViewModelList.getActionSummaryPolylineForActionID(actionID));
    }

    private void updateMapBounds(String actionID, List<LatLng> polyline) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int count = 0;
        if (polyline != null) {
            for (LatLng latLng : polyline) {
                count++;
                builder.include(latLng);
            }
        }

        if (mapViewModelList.getActionSummaryStartMarkerForActionID(actionID) != null
                && mapViewModelList.getActionSummaryStartMarkerForActionID(actionID).getPosition() != null) {
            builder.include(mapViewModelList.getActionSummaryStartMarkerForActionID(actionID).getPosition());
        }

        if (mapViewModelList.getActionSummaryEndMarkerForActionID(actionID) != null
                && mapViewModelList.getActionSummaryEndMarkerForActionID(actionID).getPosition() != null) {
            builder.include(mapViewModelList.getActionSummaryEndMarkerForActionID(actionID).getPosition());
        }
        if (count == 0 || !isMapLoaded)
            return;
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

        if (consumerClient != null && mapAdapter != null && mapAdapter.actionIDsToTrack(this) != null) {
            for (String actionID : mapAdapter.actionIDsToTrack(this)) {
                this.resetMarkers(actionID);
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
        this.actionNavigatorList.stopPollingForAllActions();
        this.actionNavigatorList.clearAllNavigators();

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

    private void resetMarkers(String actionID) {
        if (this.mMap == null) return;

        this.removeAllDestinationMarkers();
        this.removeHeroMarker(actionID);
        this.removeSourceMarker(actionID);
        this.removeActionSummaryEndMarker(actionID);
        this.removeActionSummaryStartMarker(actionID);
    }

    private void resetUserAndAddressInfo() {

        // Setup User & Address InfoView Only if EditDestination Overlay is not visible
        if (!editDestinationViewVisible) {

            if (mapAdapter.getActionIDsToTrackCount(this) > 1) {
                // Show UserInfo & AddressInfo Views for Multiple Actions tracking
                this.showMultipleActionsUserInfo();
                this.showMultipleActionsAddressInfo();
                this.enableEndPlaceEditView(false, null);

                // Handle Info Views for Special Case of Single Action Tracking
            } else if (mapAdapter.getActionIDsToTrackCount(this) == 1) {
                String actionID = mapAdapter.actionIDsToTrack(this) != null ? mapAdapter.actionIDsToTrack(this).get(0) : null;

                if (actionID == null)
                    return;

                this.showUserAndAddressInfo(actionID);

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

        if (this.actionEndTimeTextView != null) {
            this.actionEndTimeTextView.setText("");
        }

        if (this.actionEndAddressTextView != null) {
            this.actionEndAddressTextView.setText("");
        }

        this.resetActionSummaryInfoViews();
    }

    private void resetActionSummaryInfoViews() {
        // Handle ActionSummary Info View for Special Case of Single Action Tracking
        if (consumerClient != null && mapAdapter != null && mapAdapter.getActionIDsToTrackCount(this) == 1) {
            String actionID = mapAdapter.actionIDsToTrack(this) != null ? mapAdapter.actionIDsToTrack(this).get(0) : null;

            // Check if ActionSummary Info View has to be displayed for a completed Action denoted by given actionID
            if (mapAdapter.showActionSummaryForActionID(this, actionID) && consumerClient.showActionSummaryForActionStatus(actionID)) {
                this.setActionSummaryVisibility(View.VISIBLE);
                this.enableEndPlaceEditView(false, null);
                return;
            }
        }

        this.setActionSummaryVisibility(View.GONE);

        if (this.actionDateTextView != null) {
            this.actionDateTextView.setText("");
        }

        if (this.actionDurationTextView != null) {
            this.actionDurationTextView.setText("");
        }

        if (this.actionStartAddressTextView != null) {
            this.actionStartAddressTextView.setText("");
        }

        if (this.actionStartTimeTextView != null) {
            this.actionStartTimeTextView.setText("");
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

    private void updateMapPadding(String actionID) {
        if (getActivity() == null || getActivity().isFinishing())
            return;

        if (mMap == null || actionID == null)
            return;


        int top = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_top_partial_expanded_padding);
        int bottom = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_bottom_expanded_padding);
        int defaultPadding = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_default_padding);

        if (editDestinationViewVisible) {
            bottom = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_bottom_medium_padding);
            defaultPadding = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_no_side_padding);
        } else {
            if (!mapAdapter.showUserInfoForActionID(this, actionID)) {
                bottom = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_default_top_bottom_padding);
            }

            if (actionSummaryLayout != null && actionSummaryLayout.getVisibility() == View.VISIBLE) {
                top = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_top_expanded_padding);
            } else if (!mapAdapter.showAddressInfoViewForActionID(this, actionID)) {
                top = getResources().getDimensionPixelSize(com.hypertrack.lib.R.dimen.io_ht_lib_consumer_map_fragment_default_top_bottom_padding);
            }
        }

        mMap.setPadding(defaultPadding, top, defaultPadding, bottom);
    }

    // Remove Markers from Map for a ActionID
    private void removeSourceMarker(String actionID) {
        // Remove Marker From Map & HTMapViewModelList
        if (mMap != null && mapViewModelList.getSourceMarkerForActionID(actionID) != null) {

            // Callback for Source Marker Removed
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onSourceMarkerRemoved(this, actionID, mapViewModelList.getSourceMarkerForActionID(actionID));
            }

            mapViewModelList.getSourceMarkerForActionID(actionID).remove();
            mapViewModelList.setSourceMarkerForActionID(actionID, null);
        }
    }

    private void removeMultipleActionsDestinationMarker() {
        if (multipleActionsDestinationMarker != null) {

            // Callback for MultipleActions Destination Marker Removed
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onMultipleActionsDestinationMarkerRemoved(this, multipleActionsDestinationMarker);
            }

            multipleActionsDestinationMarker.remove();
            multipleActionsDestinationMarker = null;
        }
    }

    private void removeDestinationMarker(String actionID) {
        // Remove Marker From Map & HTMapViewModelList
        if (mMap != null && mapViewModelList.getDestinationMarkerForActionID(actionID) != null) {

            // Callback for Destination Marker Removed
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onDestinationMarkerRemoved(this, actionID, mapViewModelList.getDestinationMarkerForActionID(actionID));
            }

            mapViewModelList.getDestinationMarkerForActionID(actionID).remove();
            mapViewModelList.setDestinationMarkerForActionID(actionID, null);
        }
    }

    private void removeAllDestinationMarkers() {
        // Remove All Destination Markers From Map & HTMapViewModelList
        if (mMap != null) {

            ArrayList<String> destinationMarkers = mapViewModelList.getDestinationMarkersForAllActions();

            if (destinationMarkers != null && !destinationMarkers.isEmpty()) {

                for (String actionID : destinationMarkers) {

                    if (!TextUtils.isEmpty(actionID)) {
                        // Callback for Destination Marker Removed
                        if (mapFragmentCallback != null) {
                            mapFragmentCallback.onDestinationMarkerRemoved(this, actionID, mapViewModelList.getDestinationMarkerForActionID(actionID));
                        }

                        mapViewModelList.getDestinationMarkerForActionID(actionID).remove();
                        mapViewModelList.setDestinationMarkerForActionID(actionID, null);
                    }
                }
            }
        }
    }

    private void removeHeroMarker(String actionID) {
        // Remove Marker From Map & HTMapViewModelList
        if (mMap != null && mapViewModelList.getHeroMarkerForActionID(actionID) != null) {

            // Callback for Hero Marker Removed
            if (mapFragmentCallback != null) {
                mapFragmentCallback.onHeroMarkerRemoved(this, actionID, mapViewModelList.getHeroMarkerForActionID(actionID));
            }

            mapViewModelList.getHeroMarkerForActionID(actionID).remove();
            mapViewModelList.setHeroMarkerForActionID(actionID, null);
        }
    }

    private void removeActionSummaryStartMarker(String actionID) {
        // Remove Marker From Map & HTMapViewModelList
        if (mMap != null && mapViewModelList.getActionSummaryStartMarkerForActionID(actionID) != null) {
            mapViewModelList.getActionSummaryStartMarkerForActionID(actionID).remove();
            mapViewModelList.setActionSummaryStartMarkerForActionID(actionID, null);
        }
    }

    private void removeActionSummaryEndMarker(String actionID) {
        // Remove Marker From Map & HTMapViewModelList
        if (mMap != null && mapViewModelList.getActionSummaryEndMarkerForActionID(actionID) != null) {
            mapViewModelList.getActionSummaryEndMarkerForActionID(actionID).remove();
            mapViewModelList.setActionSummaryEndMarkerForActionID(actionID, null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() == null || getActivity().isFinishing())
            return false;

        //Back button inside toolbar (If Action StatusToolbar is enabled)
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

        IntentFilter orderStatusChangedFilter = new IntentFilter(HTConsumerClient.ACTION_STATUS_CHANGED_NOTIFICATION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mOrderStatusChangedMessageReceiver, orderStatusChangedFilter);

        IntentFilter actionRefreshedFilter = new IntentFilter(HTConsumerClient.ACTION_DETAIL_REFRESHED_NOTIFICATION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mActionDetailRefreshedMessageReceiver, actionRefreshedFilter);

        IntentFilter actionRemovedFilter = new IntentFilter(HTConsumerClient.ACTION_REMOVED_FROM_TRACKING_NOTIFICATION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mActionRemovedMessageReceiver, actionRemovedFilter);

        HTLog.v("lifecycle", "Inside OnResume");

        // Initialize Map Fragment
        initializeMapFragment();

        onUpdateAllActions(null);
    }

    @Override
    public void onPause() {
        super.onPause();

        HTLog.v("lifecycle", "Inside onPause");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mOrderStatusChangedMessageReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mActionDetailRefreshedMessageReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mActionRemovedMessageReceiver);

        // Stop Polling for All Actions in onPause()
        actionNavigatorList.clearAllNavigators();

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