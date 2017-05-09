package io.hypertrack.sendeta.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackMapFragment;
import com.hypertrack.lib.HyperTrackUtils;
import com.hypertrack.lib.MapFragmentCallback;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.callbacks.HyperTrackEventCallback;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.HyperTrackError;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.ServiceNotificationParams;
import com.hypertrack.lib.models.ServiceNotificationParamsBuilder;
import com.hypertrack.lib.models.SuccessResponse;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.callback.ActionManagerListener;
import io.hypertrack.sendeta.model.ETAResponse;
import io.hypertrack.sendeta.presenter.HomePresenter;
import io.hypertrack.sendeta.presenter.IHomePresenter;
import io.hypertrack.sendeta.receiver.GpsLocationReceiver;
import io.hypertrack.sendeta.receiver.NetworkChangeReceiver;
import io.hypertrack.sendeta.service.FetchLocationIntentService;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.util.AnimationUtils;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.ImageUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.Utils;

public class Home extends BaseActivity implements HomeView {

    private static final String TAG = Home.class.getSimpleName();
    private GoogleMap mMap;
    private Marker expectedPlaceMarker;
    private String lookupId = null;
    private Location defaultLocation = new Location("default");
    private TabLayout vehicleTypeTabLayout;
    private TextView infoMessageViewText;
    private LinearLayout infoMessageView;
    private FrameLayout bottomButtonLayout;
    private Button shareLiveLocationButton, retryButton, stopSharingButton;
    private ImageButton shareButton, navigateButton;
    private Place expectedPlace;
    private ProgressDialog mProgressDialog;
    private boolean isMapLoaded = false, isvehicleTypeTabLayoutVisible = false;
    private float zoomLevel = 15.0f;
    private HTUserVehicleType selectedVehicleType = SharedPreferenceManager.getLastSelectedVehicleType(this);

    private IHomePresenter<HomeView> presenter = new HomePresenter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI Views
        initializeUIViews();

        // Initialize Map Fragment added in Activity Layout to getMapAsync
        HyperTrackMapFragment htMapFragment = (HyperTrackMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.htMapfragment);
        HomeMapAdapter adapter = new HomeMapAdapter(this, getToolbar());
        htMapFragment.setHTMapAdapter(adapter);
        htMapFragment.setMapFragmentCallback(callback);

        // Get Default User Location from his CountryCode
        // SKIP: if Location Permission is Granted and Location is Enabled
        if (!HyperTrack.checkLocationServices(this) || !HyperTrack.checkLocationPermission(this)) {
            geocodeUserCountryName();
        }

        // Check & Prompt User if Internet is Not Connected
        if (!HyperTrackUtils.isInternetConnected(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
        }

        // Set callback for HyperTrackEvent updates
        setCallbackForHyperTrackEvents();

        // Ask for tracking permission
        checkForBackgroundTrackingPermission();

        // Check if location is being shared currently
        restoreLocationSharingIfNeeded();

        // Handles Tracking Url deeplink
        handleTrackingUrlDeeplink();
    }

    private void initializeUIViews() {
        // Initialize Toolbar without Home Button
        initToolbar();

        // Initialize Retry button
        retryButton = (Button) findViewById(R.id.retryButton);

        // Initialize Share button and its click listener
        shareButton = (ImageButton) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.shareTrackingUrl(ActionManager.getSharedManager(Home.this));
            }
        });

        // Initialize ShareLiveLocation button and its click listener
        shareLiveLocationButton = (Button) findViewById(R.id.shareLiveLocation);
        bottomButtonLayout = (FrameLayout) findViewById(R.id.home_bottomButtonLayout);
        shareLiveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLiveLocation();
            }
        });

        // Initialize StopSharing button and its click listener
        stopSharingButton = (Button) findViewById(R.id.stopSharingButton);
        stopSharingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if Location Permission has been granted & Location has been enabled
                if (HyperTrack.checkLocationPermission(Home.this) && HyperTrack.checkLocationServices(Home.this)) {
                    presenter.stopSharing(ActionManager.getSharedManager(Home.this));
                }
            }
        });

        // Initialize Navigate button and its click listener
        navigateButton = (ImageButton) findViewById(R.id.navigateButton);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.openNavigationForExpectedPlace(ActionManager.getSharedManager(Home.this));
            }
        });

        // Setup Info message view layouts
        infoMessageView = (LinearLayout) findViewById(R.id.home_info_message_view);
        infoMessageViewText = (TextView) findViewById(R.id.home_info_message_text);

        // Initializing the vehicleTypeTabLayout and its tabs using addTab() method
        vehicleTypeTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        vehicleTypeTabLayout.addTab(vehicleTypeTabLayout.newTab().setIcon(R.drawable.ic_vehicle_type_car));
        vehicleTypeTabLayout.addTab(vehicleTypeTabLayout.newTab().setIcon(R.drawable.ic_vehicle_type_bus));
        vehicleTypeTabLayout.addTab(vehicleTypeTabLayout.newTab().setIcon(R.drawable.ic_vehicle_type_motorbike));
        vehicleTypeTabLayout.addTab(vehicleTypeTabLayout.newTab().setIcon(R.drawable.ic_vehicle_type_walk));
    }

    public MapFragmentCallback callback = new MapFragmentCallback() {
        @Override
        public void onMapReadyCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
            onMapReady(map);
        }

        @Override
        public void onExpectedPlaceSelected(Place expectedPlace) {
            // Check if expected place was selected
            if (expectedPlace != null) {
                Home.this.expectedPlace = expectedPlace;
                onSelectPlace(Home.this.expectedPlace);
            }
        }

        @Override
        public void onMapLoadedCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
            isMapLoaded = true;
            updateMapView();
        }

        @Override
        public void onActionRefreshed(List<String> refreshedActionIds, List<Action> refreshedActions) {
            if (shareLiveLocationButton != null && shareLiveLocationButton.isShown()
                    && HyperTrack.getConsumerClient().getActiveActionIDList() == null) {
                shareLiveLocationButton.setVisibility(View.GONE);
            }
        }
    };

    private void shareLiveLocation() {
        //Check if Location Permission has been granted & Location has been enabled
        if (HyperTrack.checkLocationPermission(this) && HyperTrack.checkLocationServices(this)) {
            if (!ActionManager.getSharedManager(Home.this).isActionLive()) {
                // Start the Task
                startAction();
            } else {
                // Reset Current State when user chooses to edit destination
                ActionManager.getSharedManager(Home.this).clearState();
                OnStopSharing();
            }
        } else {
            checkForLocationSettings();
        }
    }

    private void checkForLocationSettings() {
        // Check If LOCATION Permission is available & then if Location is enabled
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestLocationServices(this, null);
            return;
        }

        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this, null);
        }
    }

    /**
     * Method to set callback for HyperTrackEvents to update notification with relevant information.
     * Note: Show share tracking url message on Stop_Ended/Trip_Started event and reset it in other cases.
     */
    private void setCallbackForHyperTrackEvents() {
        HyperTrack.setCallback(new HyperTrackEventCallback() {
            @Override
            public void onEvent(@NonNull final HyperTrackEvent event) {
                switch (event.getEventType()) {
                    case HyperTrackEvent.EventType.STOP_ENDED_EVENT:

                        //Check if user has shared his tracking link
                        if (ActionManager.getSharedManager(Home.this).isActionLive()) {
                            return;
                        }

                        Home.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ServiceNotificationParamsBuilder builder = new ServiceNotificationParamsBuilder();
                                ArrayList<String> action = new ArrayList<>();
                                action.add("Set Destination Address");
                                ServiceNotificationParams notificationParams = builder
                                        .setSmallIcon(R.drawable.ic_ht_service_notification_small)
                                        .setSmallIconBGColor(ContextCompat.getColor(Home.this, R.color.colorAccent))
                                        .setContentTitle(getString(R.string.notification_share_tracking_link))
                                        .setContextText(getString(R.string.notification_set_expected_place))
                                        .setContentIntentActivityClass(SplashScreen.class)
                                        .setContentIntentExtras(action)
                                        .build();
                                HyperTrack.setServiceNotificationParams(notificationParams);
                            }
                        });
                        break;
                    case HyperTrackEvent.EventType.TRACKING_STOPPED_EVENT:
                    case HyperTrackEvent.EventType.ACTION_ASSIGNED_EVENT:
                    case HyperTrackEvent.EventType.ACTION_COMPLETED_EVENT:
                    case HyperTrackEvent.EventType.STOP_STARTED_EVENT:
                        HyperTrack.clearServiceNotificationParams();
                        break;
                }
            }

            @Override
            public void onError(@NonNull final ErrorResponse errorResponse) {
                // do nothing
            }
        });
    }

    /**
     * Method to request user to be tracked in background. This enables the app to give user better
     * suggestions for tracking links when he/she is on the move.
     */
    private void checkForBackgroundTrackingPermission() {
        // Check if a valid user exists
        if (HyperTrack.getUserId() == null)
            return;

        // Check if the user has denied for Background tracking
        if (!SharedPreferenceManager.hasRequestedForBackgroundTracking()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.background_tracking_permission_msg)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startHyperTrackTracking(true);
                        }
                    })
                    .setNegativeButton("Disable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
            SharedPreferenceManager.setRequestedForBackgroundTracking();
        }
    }

    /*
     * Method to restore app's state in case of ongoing location sharing for current user.
     */
    private void restoreLocationSharingIfNeeded() {
        final ActionManager actionManager = ActionManager.getSharedManager(this);

        //Check if there is any existing task to be restored
        if (actionManager.shouldRestoreState()) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.fetching_details_msg));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            onShareLiveLocation();

        } else {
            // Initialize VehicleTabLayout
            initializeVehicleTypeTab();
        }
    }

    /**
     * Method to handle Tracking url deeplinks to enable live location sharing amongst friends
     */
    private void handleTrackingUrlDeeplink() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(Track.KEY_TRACK_DEEPLINK, false)) {
            // Add ProgressDialog
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.fetching_details_msg));
            mProgressDialog.show();

            // Get required parameters for tracking Actions on map
            lookupId = intent.getStringExtra(Track.KEY_LOOKUP_ID);
            List<String> actionIDs = intent.getStringArrayListExtra(Track.KEY_ACTION_ID_LIST);

            // Call trackActionsOnMap method
            presenter.trackActionsOnMap(lookupId, actionIDs, ActionManager.getSharedManager(this));
        }
    }

    @Override
    public void showTrackActionsOnMapSuccess(List<Action> actions) {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        expectedPlace = ActionManager.getSharedManager(Home.this).getPlace();
        showShareLiveLocationButton();
    }

    @Override
    public void showTrackActionsOnMapError(ErrorResponse errorResponse) {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        Toast.makeText(this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to be called when user selects an expected place to be used for sharing his live location
     * via the tracking url.
     * @param place Expected place for the user
     */
    private void onSelectPlace(final Place place) {
        if (place == null || this.isFinishing()) {
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.calculating_eta_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        presenter.getETAForExpectedPlace(place, selectedVehicleType.toString(), ActionManager.getSharedManager(this));
    }

    @Override
    public void showGetETAForExpectedPlaceSuccess(ETAResponse etaResponse, Place expectedPlace) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        // Hide Retry Button
        showRetryButton(false, null);
        onETASuccess(etaResponse, expectedPlace);
    }

    @Override
    public void showGetETAForExpectedPlaceError(ErrorResponse errorResponse, Place expectedPlace) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        // Show Retry button to fetch eta again
        showRetryButton(true, expectedPlace);


        if (expectedPlaceMarker != null) {
            expectedPlaceMarker.remove();
            expectedPlaceMarker = null;
        }

        Toast.makeText(this, getString(R.string.eta_fetching_error), Toast.LENGTH_SHORT).show();
    }

    private void showRetryButton(boolean showRetryButton, final Place place) {
        if (showRetryButton) {
            // Initialize RetryButton on getETAForDestination failure
            bottomButtonLayout.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Fetch ETA for selected place
                    onSelectPlace(place);
                }
            });
        } else {
            // Reset Retry button
            bottomButtonLayout.setVisibility(View.GONE);
            retryButton.setVisibility(View.GONE);
            retryButton.setOnClickListener(null);
        }
    }

    private void onETASuccess(ETAResponse response, Place place) {
        // Make the VehicleTabLayout visible onETASuccess
        AnimationUtils.expand(vehicleTypeTabLayout);
        isvehicleTypeTabLayoutVisible = true;
        LatLng latLng = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
        updateViewForETASuccess((int) response.getDuration() / 60, latLng);
        expectedPlace = place;
        ActionManager.getSharedManager(this).setPlace(expectedPlace);
    }

    private void updateViewForETASuccess(Integer etaInMinutes, LatLng latLng) {
        showShareLiveLocationButton();
        updateExpectedPlaceMarker(latLng, etaInMinutes);
        updateMapView();
    }

    private void updateExpectedPlaceMarker(LatLng expectedPlaceLocation, Integer etaInMinutes) {
        if (mMap == null) {
            return;
        }

        if (expectedPlaceMarker != null) {
            expectedPlaceMarker.remove();
            expectedPlaceMarker = null;
        }

        View markerView = getExpectedPlaceMarkerView(etaInMinutes);

        Bitmap bitmap = ImageUtils.getBitMapForView(this, markerView);
        if (bitmap != null) {
            expectedPlaceMarker = mMap.addMarker(new MarkerOptions()
                    .position(expectedPlaceLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            bitmap.recycle();
        }
    }

    private View getExpectedPlaceMarkerView(Integer etaInMinutes) {
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_destination_marker_layout, null);
        TextView etaTimeTextView = (TextView) marker.findViewById(R.id.eta_time);
        TextView etaTimeTypeTextView = (TextView) marker.findViewById(R.id.eta_time_type_text);
        updateTextViewForMinutes(etaTimeTextView, etaTimeTypeTextView, etaInMinutes);
        return marker;
    }

    private void updateTextViewForMinutes(TextView etaTimeTextView, TextView etaTimeTypeTextView, Integer etaInMinutes) {
        // Set empty view if etaInMinutes is null
        if (etaInMinutes == null) {
            etaTimeTextView.setText("");
            etaTimeTypeTextView.setText("");

            // Set ETA to 0 if etaInMinutes is 0 or below
        } else if (etaInMinutes <= 0) {
            etaInMinutes = 0;
            etaTimeTextView.setText(String.valueOf(etaInMinutes));
            etaTimeTypeTextView.setText(this.getResources().getQuantityString(R.plurals.eta_in_minute_text, etaInMinutes));

        } else {

            // Set ETA in minutes if etaInMinutes is equal or below MINUTES_ON_ETA_MARKER_LIMIT
            if (etaInMinutes <= Constants.MINUTES_ON_ETA_MARKER_LIMIT) {
                etaTimeTextView.setText(String.valueOf(etaInMinutes));
                etaTimeTypeTextView.setText(this.getResources().getQuantityString(R.plurals.eta_in_minute_text, etaInMinutes));

                // Set ETA in hours if etaInMinutes is above MINUTES_ON_ETA_MARKER_LIMIT
            } else {
                int hours = etaInMinutes / Constants.MINUTES_IN_AN_HOUR;

                // Round off ETA to nearest hour
                if (etaInMinutes % Constants.MINUTES_IN_AN_HOUR < Constants.MINUTES_TO_ROUND_OFF_TO_HOUR) {
                    etaTimeTextView.setText(String.valueOf(hours));
                } else {
                    hours = hours + 1;
                    etaTimeTextView.setText(String.valueOf(hours));
                }
                etaTimeTypeTextView.setText(this.getResources().getQuantityString(R.plurals.eta_in_hour_text, hours));
            }
        }
    }

    private void showShareLiveLocationButton() {
        // Set SendETA Button Text
        shareLiveLocationButton.setText(getString(R.string.action_share_live_location));
        shareLiveLocationButton.setVisibility(View.VISIBLE);
        bottomButtonLayout.setVisibility(View.VISIBLE);
    }

    private void initializeVehicleTypeTab() {
        // Remove onTabSelectedListener
        vehicleTypeTabLayout.setOnTabSelectedListener(null);

        for (int i = 0; i < vehicleTypeTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = vehicleTypeTabLayout.getTabAt(i);
            if (tab != null) {
                if (selectedVehicleType.equals(getVehicleTypeForTabPosition(tab.getPosition()))) {
                    int tabIconColor = ContextCompat.getColor(Home.this, R.color.tab_layout_selected_item);
                    if (tab.getIcon() != null)
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    tab.select();
                    continue;
                }

                tab.setCustomView(R.layout.vehicle_type_tab_layout);

                if (tab.getIcon() == null)
                    return;

                int tabIconColor = ContextCompat.getColor(Home.this, R.color.tab_layout_unselected_item);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }
        }

        // Set onTabSelectedListener
        vehicleTypeTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // VehicleType tab has been changed
                onVehicleTypeTabChanged(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab == null || tab.getIcon() == null)
                    return;

                int tabIconColor = ContextCompat.getColor(Home.this, R.color.tab_layout_unselected_item);
                tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void onVehicleTypeTabChanged(TabLayout.Tab tab) {
        if (tab == null || tab.getIcon() == null)
            return;

        int tabIconColor = ContextCompat.getColor(Home.this, R.color.tab_layout_selected_item);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

        selectedVehicleType = getVehicleTypeForTabPosition(tab.getPosition());

        // Check if a place has been selected or
        Place place = ActionManager.getSharedManager(Home.this).getPlace();
        if (place == null)
            return;

        // Call getETAForDestination with selected vehicleType
        Home.this.onSelectPlace(place);
    }

    private HTUserVehicleType getVehicleTypeForTabPosition(int tabPosition) {
        switch (tabPosition) {
            case 1:
                return HTUserVehicleType.VAN;
            case 2:
                return HTUserVehicleType.MOTORCYCLE;
            case 3:
                return HTUserVehicleType.WALK;
            case 0:
            default:
                return HTUserVehicleType.CAR;
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng;

        if (SharedPreferenceManager.getActionID(Home.this) == null) {
            if (googleMap != null && googleMap.isMyLocationEnabled() && googleMap.getMyLocation() != null) {
                SharedPreferenceManager.setLastKnownLocation(googleMap.getMyLocation());
                latLng = new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

            } else {
                // Set Default View for map according to User's LastKnownLocation
                if (SharedPreferenceManager.getLastKnownLocation() != null) {
                    defaultLocation = SharedPreferenceManager.getLastKnownLocation();
                }

                // Else Set Default View for map according to either User's Default Location
                // (If Country Info was available) or (0.0, 0.0)
                if (defaultLocation != null && defaultLocation.getLatitude() != 0.0
                        && defaultLocation.getLongitude() != 0.0) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(defaultLocation.getLatitude(), defaultLocation.getLongitude()), zoomLevel));
                }
            }
        }

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        checkForLocationSettings();
    }

    private void updateMapPadding() {
        if (mMap != null) {
            int top = isvehicleTypeTabLayoutVisible ? getResources().getDimensionPixelSize(R.dimen.map_top_padding_with_vehicle_type_layout) :
                    getResources().getDimensionPixelSize(R.dimen.map_top_padding);
            int left = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            int bottom = getResources().getDimensionPixelSize(R.dimen.map_side_padding);

            if (stopSharingButton.isShown())
                bottom = getResources().getDimensionPixelSize(R.dimen.map_bottom_padding);
            if (lookupId == null)
                bottom = getResources().getDimensionPixelSize(R.dimen.home_map_bottom_padding);
            mMap.setPadding(left, top, 0, bottom);
        }
    }

    /**
     * Method to Initiate START TASK
     */
    private void startAction() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.sharing_live_location_message));
        mProgressDialog.show();

        presenter.shareLiveLocation(ActionManager.getSharedManager(this), lookupId, expectedPlace);
    }

    @Override
    public void showShareLiveLocationError(ErrorResponse errorResponse) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        switch (errorResponse.getErrorCode()) {
            case HyperTrackError.Code.PUBLISHABLE_KEY_NOT_CONFIGURED:
            case HyperTrackError.Code.SDK_NOT_INITIALIZED:
            case HyperTrackError.Code.USER_ID_NOT_CONFIGURED:
            case HyperTrackError.Code.PLAY_SERVICES_UNAVAILABLE:
            case HyperTrackError.Code.PERMISSIONS_NOT_REQUESTED:
            case HyperTrackError.Code.LOCATION_SETTINGS_DISABLED:
            case HyperTrackError.Code.LOCATION_SETTINGS_LOW_ACCURACY:
            case HyperTrackError.Code.NETWORK_CONNECTIVITY_ERROR:
            case HyperTrackError.Code.LOCATION_SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            default:
                Toast.makeText(this, ErrorMessages.SHARE_LIVE_LOCATION_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showShareLiveLocationSuccess(Action action) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        // Show ShareCard
        presenter.shareTrackingUrl(ActionManager.getSharedManager(this));

        onShareLiveLocation();
    }

    /**
     * Method to update State Variables & UI to reflect Task Started
     */
    private void onShareLiveLocation() {
        if (ActionManager.getSharedManager(Home.this).getHyperTrackAction() == null)
            return;

        ActionManager.getSharedManager(this).setActionComletedListener(actionCompletedListener);
        lookupId = ActionManager.getSharedManager(this).getHyperTrackAction().getLookupID();

        HyperTrack.trackActionByLookupId(lookupId, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                // do nothing
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                Toast.makeText(Home.this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        expectedPlaceMarker = null;

        // Hide VehicleType TabLayout onShareLiveLocation success
        AnimationUtils.collapse(vehicleTypeTabLayout);
        isvehicleTypeTabLayoutVisible = false;

        shareLiveLocationButton.setVisibility(View.GONE);
        stopSharingButton.setVisibility(View.VISIBLE);
        stopSharingButton.setText(R.string.action_stop_sharing);

        shareButton.setVisibility(View.VISIBLE);
        navigateButton.setVisibility(View.VISIBLE);

        // Update SelectedVehicleType in persistentStorage
        SharedPreferenceManager.setLastSelectedVehicleType(selectedVehicleType);

        supportInvalidateOptionsMenu();
    }

    private ActionManagerListener actionCompletedListener = new ActionManagerListener() {
        @Override
        public void OnCallback() {
            // Initiate Stop Sharing on UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    presenter.stopSharing(ActionManager.getSharedManager(Home.this));
                }
            });
        }
    };


    @Override
    public void showStopSharingError() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        Toast.makeText(this, getString(R.string.stop_sharing_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showStopSharingSuccess() {
        OnStopSharing();
    }

    /**
     * Method to update State Variables & UI to reflect Task Ended
     */
    private void OnStopSharing() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        // Reset lookupId variable
        lookupId = null;

        // Hide VehicleType TabLayout onShareLiveLocation success
        AnimationUtils.collapse(vehicleTypeTabLayout);
        isvehicleTypeTabLayoutVisible = false;

        shareLiveLocationButton.setVisibility(View.GONE);
        stopSharingButton.setVisibility(View.GONE);
        bottomButtonLayout.setVisibility(View.GONE);

        shareButton.setVisibility(View.GONE);
        navigateButton.setVisibility(View.GONE);

        if (SharedPreferenceManager.isTrackingON()) {
            startHyperTrackTracking(true);
        } else {
            stopHyperTrackTracking();
        }

        supportInvalidateOptionsMenu();

        updateMapView();
    }

    private void updateMapView() {
        if (mMap == null || !isMapLoaded || lookupId != null) {
            return;
        }

        LatLng currentLocation = null;
        if (HyperTrack.checkLocationPermission(this) && mMap.isMyLocationEnabled() && mMap.getMyLocation() != null) {
            currentLocation = new LatLng(mMap.getMyLocation().getLatitude(),
                    mMap.getMyLocation().getLongitude());
            SharedPreferenceManager.setLastKnownLocation(mMap.getMyLocation());
        }

        try {
            int count = 0;
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            if (currentLocation != null) {
                builder.include(currentLocation);
                count++;
            }

            if (expectedPlaceMarker != null) {
                builder.include(expectedPlaceMarker.getPosition());
                count++;
            }

            if (count == 1 && currentLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));

            } else if (count >= 1) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            }

            updateMapPadding();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showShareTrackingUrlError() {
        Toast.makeText(Home.this, R.string.share_message_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showShareTrackingUrlSuccess(String shareMessage) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivityForResult(Intent.createChooser(sharingIntent, "Share via"), Constants.SHARE_REQUEST_CODE);
    }

    @Override
    public void showOpenNavigationError() {
        Toast.makeText(Home.this, R.string.navigate_to_expected_place_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOpenNavigationSuccess(double latitude, double longitude) {
        String navigationString = Double.toString(latitude) + "," + Double.toString(longitude) + "&mode=d";
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + navigationString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (ActionManager.getSharedManager(this).getHyperTrackAction() != null)
            return false;

        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem menuItem = menu.findItem(R.id.tracking_toogle);
        if (SharedPreferenceManager.isTrackingON()) {
            menuItem.setTitle("Pause Tracking");
        } else {
            menuItem.setTitle("Resume Tracking");
        }

        // Hide menu items if user is on an Action
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tracking_toogle:
                // Check if clicked item is Resume tracking
                if (getString(R.string.action_resume_tracking).equalsIgnoreCase(item.getTitle().toString())) {
                    // Start Tracking the user
                    startHyperTrackTracking(true);
                    item.setTitle(R.string.action_pause_tracking);

                } else {
                    // Stop Tracking the user
                    stopHyperTrackTracking();
                    item.setTitle(R.string.action_resume_tracking);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startHyperTrackTracking(final boolean byUser) {
        // HACK: Check if user is tracking currently or not
        // Only for exisitng users because Permission and Location Settings have been checked here
        if (!HyperTrack.isTracking()) {
            HyperTrack.startTracking();
            if (byUser) {
                SharedPreferenceManager.setTrackingON();
                supportInvalidateOptionsMenu();
            }

        } else if (byUser) {
            SharedPreferenceManager.setTrackingON();
            supportInvalidateOptionsMenu();
        }
    }

    private void stopHyperTrackTracking() {
        HyperTrack.stopTracking();
        SharedPreferenceManager.setTrackingOFF();
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkForLocationSettings();

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                        getString(R.string.location_permission_never_allow));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                checkForLocationSettings();
            } else {
                // Handle Location services request denied error
                Snackbar.make(findViewById(R.id.parent_layout), R.string.location_services_snackbar_msg,
                        Snackbar.LENGTH_INDEFINITE).setAction("Enable Location", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkForLocationSettings();
                    }
                }).show();
            }
        }
    }

    private void geocodeUserCountryName() {
        // Fetch Country Level Location only if no cached location is available
        Location lastKnownCachedLocation = SharedPreferenceManager.getLastKnownLocation();
        if (lastKnownCachedLocation == null || lastKnownCachedLocation.getLatitude() == 0.0
                || lastKnownCachedLocation.getLongitude() == 0.0) {

            OnboardingManager onboardingManager = OnboardingManager.sharedManager();
            String countryName = Utils.getCountryName(onboardingManager.getUser().getCountryCode());

            if (!TextUtils.isEmpty(countryName)) {
                Intent intent = new Intent(this, FetchLocationIntentService.class);
                intent.putExtra(FetchLocationIntentService.RECEIVER, new GeocodingResultReceiver(new Handler()));
                intent.putExtra(FetchLocationIntentService.ADDRESS_DATA_EXTRA, countryName);
                startService(intent);
            }
        }
    }

    @SuppressLint("ParcelCreator")
    private class GeocodingResultReceiver extends ResultReceiver {
        GeocodingResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchLocationIntentService.SUCCESS_RESULT) {
                LatLng latLng = resultData.getParcelable(FetchLocationIntentService.RESULT_DATA_KEY);
                if (latLng == null)
                    return;
                defaultLocation.setLatitude(latLng.latitude);
                defaultLocation.setLongitude(latLng.longitude);
                Log.d(TAG, "Geocoding for Country Name Successful: " + latLng.toString());

                if (mMap != null) {
                    if (defaultLocation.getLatitude() != 0.0 || defaultLocation.getLongitude() != 0.0)
                        zoomLevel = 4.0f;

                    // Check if any Location Data is available, meaning Country zoom level need not be used
                    Location lastKnownCachedLocation = SharedPreferenceManager.getLastKnownLocation();
                    if (lastKnownCachedLocation != null && lastKnownCachedLocation.getLatitude() != 0.0
                            && lastKnownCachedLocation.getLongitude() != 0.0) {
                        return;
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Attach View Presenter to View
        presenter.attachView(this);

        ActionManager actionManager = ActionManager.getSharedManager(Home.this);
        if (actionManager.getHyperTrackAction() != null && !actionManager.getHyperTrackAction().isCompleted()) {
            actionManager.setActionComletedListener(actionCompletedListener);

            lookupId = actionManager.getHyperTrackAction().getLookupID();
            HyperTrack.trackActionByLookupId(lookupId, null);
        }

        // Check if Location & Network are Enabled
        updateInfoMessageView();

        // Re-register BroadcastReceiver for Location_Change, Network_Change & GCM
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationChangeReceiver,
                new IntentFilter(GpsLocationReceiver.LOCATION_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectivityChangeReceiver,
                new IntentFilter(NetworkChangeReceiver.NETWORK_CHANGED));

    }

    BroadcastReceiver mLocationChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfoMessageView();
        }
    };

    BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfoMessageView();
        }
    };

    private void updateInfoMessageView() {
        if (!HyperTrackUtils.isLocationEnabled(Home.this)) {
            infoMessageView.setVisibility(View.VISIBLE);

            if (!HyperTrackUtils.isInternetConnected(this)) {
                infoMessageViewText.setText(R.string.location_off_info_message);
            } else {
                infoMessageViewText.setText(R.string.location_off_info_message);
            }
        } else {
            infoMessageView.setVisibility(View.VISIBLE);

            if (!HyperTrackUtils.isInternetConnected(this)) {
                infoMessageViewText.setText(R.string.internet_off_info_message);
            } else {
                // Both Location & Network Enabled, Hide the Info Message View
                infoMessageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Attach View Presenter to View
        presenter.detachView();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectivityChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationChangeReceiver);
    }

    @Override
    public void onBackPressed() {
        if (!isvehicleTypeTabLayoutVisible) {
            HyperTrack.removeActions(null);
            super.onBackPressed();
        } else {
            OnStopSharing();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        HyperTrack.removeActions(null);
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}