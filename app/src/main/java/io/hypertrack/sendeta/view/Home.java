package io.hypertrack.sendeta.view;

import android.Manifest;
import android.annotation.SuppressLint;
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

import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.HyperTrackError;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.ServiceNotificationParams;
import com.hypertrack.lib.models.ServiceNotificationParamsBuilder;
import com.hypertrack.lib.models.SuccessResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.callback.ActionManagerCallback;
import io.hypertrack.sendeta.callback.ActionManagerListener;
import io.hypertrack.sendeta.callback.ETACallback;
import io.hypertrack.sendeta.model.ETAResponse;
import io.hypertrack.sendeta.model.HyperTrackLiveUser;
import io.hypertrack.sendeta.receiver.GpsLocationReceiver;
import io.hypertrack.sendeta.receiver.NetworkChangeReceiver;
import io.hypertrack.sendeta.service.FetchLocationIntentService;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.util.AnimationUtils;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.ImageUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.Utils;

public class Home extends BaseActivity implements ResultCallback<Status> {

    private static final String TAG = Home.class.getSimpleName();
    private HyperTrackLiveUser user;
    private GoogleMap mMap;
    private Marker expectedPlaceMarker;
    private String lookupId = null;
    private Location defaultLocation = new Location("default");
    private TabLayout vehicleTypeTabLayout;
    private TextView infoMessageViewText;
    private LinearLayout infoMessageView, endTripLoaderAnimationLayout;
    private FrameLayout bottomButtonLayout;
    private Button sendETAButton, retryButton, endTripSwipeButton;
    private ImageButton shareButton, navigateButton;
    private Place destinationPlace;
    private ProgressDialog mProgressDialog;
    private boolean isMapLoaded = false, isvehicleTypeTabLayoutVisible = false;
    private float zoomLevel = 15.0f;
    private Integer etaInMinutes = 0;
    private HTUserVehicleType selectedVehicleType = SharedPreferenceManager.getLastSelectedVehicleType(this);

    private ActionManagerListener actionCompletedListener = new ActionManagerListener() {
        @Override
        public void OnCallback() {
            // Initiate Stop Sharing on UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    completeTask();
                }
            });
        }
    };

    public MapFragmentCallback callback = new MapFragmentCallback() {
        @Override
        public void onMapReadyCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
            super.onMapReadyCallback(hyperTrackMapFragment, map);
            onMapReady(map);
        }

        @Override
        public void onDestinationPlaceSelected(Place destinationPlace) {
            // Check if destination place was selected
            if (destinationPlace != null) {
                Home.this.destinationPlace = destinationPlace;
                onSelectPlace(destinationPlace);
            }
        }

        @Override
        public void onMapLoadedCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
            super.onMapLoadedCallback(hyperTrackMapFragment, map);
            isMapLoaded = true;
            updateMapView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Toolbar without Home Button
        initToolbar();
        user = OnboardingManager.sharedManager().getUser();

        // Initialize Map Fragment added in Activity Layout to getMapAsync
        HyperTrackMapFragment htMapFragment = (HyperTrackMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.htMapfragment);
        HomeMapAdapter adapter = new HomeMapAdapter(this, getToolbar());
        htMapFragment.setHTMapAdapter(adapter);
        htMapFragment.setMapFragmentCallback(callback);

        // Initialize UI Views
        retryButton = (Button) findViewById(R.id.retryButton);

        // Get Default User Location from his CountryCode
        // SKIP: if Location Permission is Granted and Location is Enabled
        if (!HyperTrack.checkLocationServices(this) || !HyperTrack.checkLocationPermission(this)) {
            geocodeUserCountryName();
        }

        setupShareButton();
        setupSendETAButton();
        setupEndTripSwipeButton();
        setupNavigateButton();
        setupInfoMessageView();

        // Setup VehicleType TabLayout
        setupVehicleTypeTabLayout();

        // Check & Prompt User if Internet is Not Connected
        if (!HyperTrackUtils.isInternetConnected(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
        }

        // Set HyperTrackCallback
        setHyperTrackCallback();

        //Ask for tracking permission
        checkForTrackingPermission();

        // Check if there is any currently running task to be restored
        restoreTaskStateIfNeeded();

        // Handles Tracking Url deeplink
        handleDeeplink();
    }

    private void onSelectPlace(final Place place) {
        if (place == null) {
            return;
        }

        getEtaForDestination(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()), new ETACallback() {
            @Override
            public void OnSuccess(ETAResponse etaResponse) {
                // Hide Retry Button
                showRetryButton(false, null);
                etaInMinutes = (int) etaResponse.getDuration();
                onETASuccess(etaResponse, place);
            }

            @Override
            public void OnError() {
                // Show Retry button to fetch eta again
                showRetryButton(true, place);

                if (expectedPlaceMarker != null) {
                    expectedPlaceMarker.remove();
                    expectedPlaceMarker = null;
                }

                showETAError();
            }
        });
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

    private void getEtaForDestination(final LatLng destinationLocation, final ETACallback callback) {
        if (Home.this.isFinishing())
            return;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.calculating_eta_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        HyperTrack.getCurrentLocation(new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                final Location currentLocation = (Location) response.getResponseObject();

                ActionManager.getSharedManager(Home.this).getETA(
                        new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                        destinationLocation, selectedVehicleType.toString(), new ETACallback() {
                            @Override
                            public void OnSuccess(ETAResponse etaResponse) {
                                if (mProgressDialog != null && !Home.this.isFinishing())
                                    mProgressDialog.dismiss();

                                if (callback != null)
                                    callback.OnSuccess(etaResponse);
                            }

                            @Override
                            public void OnError() {
                                if (mProgressDialog != null && !Home.this.isFinishing())
                                    mProgressDialog.dismiss();

                                if (callback != null)
                                    callback.OnError();
                            }
                        });
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                Toast.makeText(Home.this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();

                if (mProgressDialog != null && !Home.this.isFinishing())
                    mProgressDialog.dismiss();

                if (callback != null)
                    callback.OnError();
            }
        });
    }

    private void showETAError() {
        Toast.makeText(this, getString(R.string.eta_fetching_error), Toast.LENGTH_SHORT).show();
    }

    private void onETASuccess(ETAResponse response, Place place) {
        // Make the VehicleTabLayout visible onETASuccess
        AnimationUtils.expand(vehicleTypeTabLayout);
        isvehicleTypeTabLayoutVisible = true;
        LatLng latLng = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
        updateViewForETASuccess((int) response.getDuration() / 60, latLng);
        destinationPlace = place;
        ActionManager.getSharedManager(this).setPlace(destinationPlace);
    }

    private void updateViewForETASuccess(Integer etaInMinutes, LatLng latLng) {
        showSendETAButton();
        updateDestinationMarker(latLng, etaInMinutes);
        updateMapView();
    }

    private void showSendETAButton() {
        // Set SendETA Button Text
        sendETAButton.setText(getString(R.string.action_send_eta));
        sendETAButton.setVisibility(View.VISIBLE);
        bottomButtonLayout.setVisibility(View.VISIBLE);
    }

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

    private void setHyperTrackCallback() {
        HyperTrack.setCallback(new HyperTrackEventCallback() {
            @Override
            public void onEvent(@NonNull final HyperTrackEvent event) {
                switch (event.getEventType()) {
                    case HyperTrackEvent.EventType.STOP_ENDED_EVENT:
                        if (!ActionManager.getSharedManager(Home.this).isActionLive()) {
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
                                            .setContextText(getString(R.string.notification_set_destination))
                                            .setContentIntentActivityClass(SplashScreen.class)
                                            .setContentIntentExtras(action)
                                            .build();
                                    HyperTrack.setServiceNotificationParams(notificationParams);
                                }
                            });
                        }
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

    private void checkForTrackingPermission() {
        if (!TextUtils.isEmpty(OnboardingManager.sharedManager().getUser().getId())) {
            if (!SharedPreferenceManager.isAskForTrackingDialog()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("We will enable the background tracking by default. Do you want to disable it ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startHyperTrackTracking(true);
                            }
                        })
                        .show();
                SharedPreferenceManager.setAskedForTrackingDialog();
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

    private void setupShareButton() {
        // Initialize Share Button UI View
        shareButton = (ImageButton) findViewById(R.id.shareButton);

        // Set Click Listener for Share Button
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();

                //On tracking link share reset the notification
                HyperTrack.clearServiceNotificationParams();
                // TaskShared Flag is always false because couldn't find a way of knowing
                // whether the user successfully shared the task details or not
                AnalyticsStore.getLogger().tappedShareIcon(false);
            }
        });
    }

    private void setupSendETAButton() {
        // Initialize SendETA Button UI View
        sendETAButton = (Button) findViewById(R.id.sendETAButton);
        bottomButtonLayout = (FrameLayout) findViewById(R.id.home_bottomButtonLayout);

        // Set Click Listener for SendETA Button
        sendETAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SharedPreferenceManager.isTrackingON()) {
                    startHyperTrackTracking(false);
                }
                createSharingLink();
            }
        });
    }

    private void createSharingLink() {
        //Check if Location Permission has been granted & Location has been enabled
        if (HyperTrackUtils.isLocationPermissionAvailable(this) && HyperTrackUtils.isLocationEnabled(Home.this)) {
            if (!ActionManager.getSharedManager(Home.this).isActionLive()) {
                // Start the Task
                startAction();
            } else {
                // Reset Current State when user chooses to edit destination
                ActionManager.getSharedManager(Home.this).clearState();
                OnCompleteTask();
            }
        } else {
            checkForLocationPermission();
        }
    }

    private void setupEndTripSwipeButton() {
        endTripSwipeButton = (Button) findViewById(R.id.endTripSwipeButton);
        endTripSwipeButton.setText(getString(R.string.complete_action));
        endTripSwipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if Location Permission has been granted & Location has been enabled
                if (HyperTrack.checkLocationPermission(Home.this) && HyperTrack.checkLocationServices(Home.this)) {
                    showEndingTripAnimation(true);
                    completeTask();
                }
            }
        });

        endTripLoaderAnimationLayout = (LinearLayout) findViewById(R.id.endTripLoaderAnimationLayout);

/*        SwipeButtonCustomItems swipeButtonSettings = new SwipeButtonCustomItems() {
            @Override
            public void onSwipeConfirm() {
                // Check if Location Permission has been granted & Location has been enabled
                if (HyperTrack.checkLocationPermission(Home.this) && HyperTrack.checkLocationServices(Home.this)) {
                    showEndingTripAnimation(true);
                    completeTask();
                }
            }
        };

        swipeButtonSettings.setButtonPressText(getString(R.string.action_slide_to_end_sharing))
                .setActionConfirmText(getString(R.string.action_slide_to_end_sharing));

        if (endTripSwipeButton != null) {
            endTripSwipeButton.setSwipeButtonCustomItems(swipeButtonSettings);*/
    }

    private void showEndingTripAnimation(boolean show) {
        if (show) {
            endTripLoaderAnimationLayout.setVisibility(View.VISIBLE);
        } else {
            endTripLoaderAnimationLayout.setVisibility(View.GONE);
        }
    }

    private void setupNavigateButton() {
        // Initialize Navigate Button UI View
        navigateButton = (ImageButton) findViewById(R.id.navigateButton);

        // Set Click Listener for Navigate Button
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate();

                AnalyticsStore.getLogger().tappedNavigate();
            }
        });
    }

    private void setupInfoMessageView() {
        infoMessageView = (LinearLayout) findViewById(R.id.home_info_message_view);
        infoMessageViewText = (TextView) findViewById(R.id.home_info_message_text);
    }

    private void restoreTaskStateIfNeeded() {
        final ActionManager actionManager = ActionManager.getSharedManager(this);

        //Check if there is any existing task to be restored
        if (actionManager.shouldRestoreState()) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.fetching_data_msg));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            onStartTask();

        } else {
            // Initialize VehicleTabLayout
            initializeVehicleTypeTab();
        }
    }

    private void handleDeeplink() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(Track.KEY_TRACK_DEEPLINK, false)) {

            lookupId = intent.getStringExtra(Track.KEY_LOOKUP_ID);
            if (!TextUtils.isEmpty(lookupId)) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.fetching_data_msg));
                mProgressDialog.show();

                HyperTrack.trackActionByLookupId(lookupId, new HyperTrackCallback() {
                    @Override
                    public void onSuccess(@NonNull SuccessResponse response) {
                        if (mProgressDialog != null)
                            mProgressDialog.dismiss();

                        List<Action> actions = (List<Action>) response.getResponseObject();
                        if (actions != null && !actions.isEmpty()) {
                            Action action = actions.get(0);
                            destinationPlace = action.getExpectedPlace();
                            ActionManager.getSharedManager(Home.this).setPlace(destinationPlace);
                            setupSendETAButton();
                            showSendETAButton();
                        }
                    }

                    @Override
                    public void onError(@NonNull ErrorResponse errorResponse) {
                        if (mProgressDialog != null)
                            mProgressDialog.dismiss();
                        Toast.makeText(Home.this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                List<String> actionIdList = intent.getStringArrayListExtra(Track.KEY_ACTION_ID_LIST);
                // Check if a valid TASK_ID_LIST is available
                if (actionIdList != null && !actionIdList.isEmpty()) {
                    mProgressDialog = new ProgressDialog(this);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setMessage(getString(R.string.fetching_data_msg));
                    mProgressDialog.show();

                    HyperTrack.trackAction(actionIdList, new HyperTrackCallback() {
                        @Override
                        public void onSuccess(@NonNull SuccessResponse response) {
                            if (mProgressDialog != null)
                                mProgressDialog.dismiss();
                        }

                        @Override
                        public void onError(@NonNull ErrorResponse errorResponse) {
                            if (mProgressDialog != null)
                                mProgressDialog.dismiss();
                            Toast.makeText(Home.this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void setupVehicleTypeTabLayout() {
        //Initializing the vehicleTypeTabLayout
        vehicleTypeTabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //Adding the tabs using addTab() method
        vehicleTypeTabLayout.addTab(vehicleTypeTabLayout.newTab().setIcon(R.drawable.ic_vehicle_type_car));
        vehicleTypeTabLayout.addTab(vehicleTypeTabLayout.newTab().setIcon(R.drawable.ic_vehicle_type_bus));
        vehicleTypeTabLayout.addTab(vehicleTypeTabLayout.newTab().setIcon(R.drawable.ic_vehicle_type_motorbike));
        vehicleTypeTabLayout.addTab(vehicleTypeTabLayout.newTab().setIcon(R.drawable.ic_vehicle_type_walk));
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

        checkForLocationPermission();
    }

    private void checkForLocationPermission() {
        // Check If LOCATION Permission is available & then if Location is enabled
        if (HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestLocationServices(Home.this, null);
        } else {
            // Show Rationale & Request for LOCATION permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionUtils.showRationaleMessageAsDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                        getString(R.string.location_permission_rationale_msg));
            } else {
                PermissionUtils.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    private void updateMapPadding() {
        if (mMap != null) {
            int top = isvehicleTypeTabLayoutVisible ? getResources().getDimensionPixelSize(R.dimen.map_top_padding_with_vehicle_type_layout) :
                    getResources().getDimensionPixelSize(R.dimen.map_top_padding);
            int left = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            int bottom = getResources().getDimensionPixelSize(R.dimen.map_side_padding);

            if (endTripSwipeButton.isShown())
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
        mProgressDialog.setMessage(getString(R.string.starting_task_message));
        mProgressDialog.show();

        user = OnboardingManager.sharedManager().getUser();
        if (user == null) {
            mProgressDialog.dismiss();
            return;
        }

        ActionParamsBuilder builder = new ActionParamsBuilder()
                .setLookupId(lookupId != null ? lookupId : UUID.randomUUID().toString())
                .setType(Action.ACTION_TYPE_VISIT);

        if (!TextUtils.isEmpty(destinationPlace.getId())) {
            builder.setExpectedPlaceId(destinationPlace.getId());
        } else {
            builder.setExpectedPlace(destinationPlace);
        }

        // Call assignAction to start the tracking action
        HyperTrack.createAndAssignAction(builder.build(), new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                if (response.getResponseObject() != null) {

                    Action action = (Action) response.getResponseObject();
                    action.getActionDisplay().setDurationRemaining(String.valueOf(etaInMinutes));
                    ActionManager actionManager = ActionManager.getSharedManager(Home.this);
                    actionManager.setHyperTrackAction(action);
                    actionManager.onActionStart();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }

                    // Show ShareCard
                    share();

                    onStartTask();

                    HyperTrack.clearServiceNotificationParams();
                    AnalyticsStore.getLogger().sharedLiveLocation(true, null);
                    HTLog.i(TAG, "Task started successfully.");
                }
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                showStartTaskError(errorResponse);
                HTLog.e(TAG, "Task start failed.");

                AnalyticsStore.getLogger().sharedLiveLocation(false, ErrorMessages.START_TRIP_FAILED);
            }
        });
    }

    private void showStartTaskError(ErrorResponse errorResponse) {
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
                Toast.makeText(this, ErrorMessages.START_TRIP_FAILED, Toast.LENGTH_SHORT).show();
                return;
        }
    }

    /**
     * Method to Initiate COMPLETE Action
     */
    private void completeTask() {
        ActionManager.getSharedManager(this).completeAction(new ActionManagerCallback() {
            @Override
            public void OnSuccess() {
                OnCompleteTask();
                HyperTrack.clearServiceNotificationParams();
                AnalyticsStore.getLogger().tappedStopSharing(true, null);
                HTLog.i(TAG, "Complete Action (CTA) happened successfully.");

                showEndingTripAnimation(false);

                if (mMap != null) {
                    if (SharedPreferenceManager.getLastKnownLocation() != null) {
                        LatLng latLng = new LatLng(SharedPreferenceManager.getLastKnownLocation().getLatitude(),
                                SharedPreferenceManager.getLastKnownLocation().getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

                    } else if (mMap.getMyLocation() != null && mMap.isMyLocationEnabled()) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()), 16f));
                        SharedPreferenceManager.setLastKnownLocation(mMap.getMyLocation());
                    }
                }
            }

            @Override
            public void OnError() {
                showCompleteTaskError();

                AnalyticsStore.getLogger().tappedStopSharing(false, ErrorMessages.END_TRIP_FAILED);
                HTLog.e(TAG, "Complete Action (CTA) failed.");

                showEndingTripAnimation(false);
            }
        });
    }

    private void showCompleteTaskError() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        Toast.makeText(this, getString(R.string.complete_task_failed), Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to update State Variables & UI to reflect Task Started
     */
    private void onStartTask() {
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

        // Hide VehicleType TabLayout onStartTask success
        AnimationUtils.collapse(vehicleTypeTabLayout);
        isvehicleTypeTabLayoutVisible = false;

        sendETAButton.setVisibility(View.GONE);
        endTripSwipeButton.setVisibility(View.VISIBLE);
        endTripSwipeButton.setText(R.string.complete_action);

        shareButton.setVisibility(View.VISIBLE);
        navigateButton.setVisibility(View.VISIBLE);

        // Update SelectedVehicleType in persistentStorage
        SharedPreferenceManager.setLastSelectedVehicleType(selectedVehicleType);

        supportInvalidateOptionsMenu();
    }

    /**
     * Method to update State Variables & UI to reflect Task Ended
     */
    private void OnCompleteTask() {
        HTLog.i(TAG, "OnCompleteTask UI Changes Start");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        // Reset lookupId variable
        lookupId = null;

        // Hide VehicleType TabLayout onStartTask success
        AnimationUtils.collapse(vehicleTypeTabLayout);
        isvehicleTypeTabLayoutVisible = false;

        sendETAButton.setVisibility(View.GONE);
        endTripSwipeButton.setVisibility(View.GONE);
        bottomButtonLayout.setVisibility(View.GONE);

        shareButton.setVisibility(View.GONE);
        navigateButton.setVisibility(View.GONE);

        if (SharedPreferenceManager.isTrackingON()) {
            startHyperTrackTracking(true);
        } else {
            stopHyperTrackTracking();
        }

        supportInvalidateOptionsMenu();
        HTLog.i(TAG, "OnCompleteTask UI Changes Completed");

        updateMapView();
    }

    private void updateDestinationMarker(LatLng destinationLocation, Integer etaInMinutes) {
        if (mMap == null) {
            return;
        }

        if (expectedPlaceMarker != null) {
            expectedPlaceMarker.remove();
            expectedPlaceMarker = null;
        }

        View markerView = getDestinationMarkerView(etaInMinutes);

        Bitmap bitmap = ImageUtils.getBitMapForView(this, markerView);
        if (bitmap != null) {
            expectedPlaceMarker = mMap.addMarker(new MarkerOptions()
                    .position(destinationLocation)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            bitmap.recycle();
        }
    }

    private View getDestinationMarkerView(Integer etaInMinutes) {
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

    private void startHyperTrackTracking(final boolean byUser) {
        startHyperTrackTracking(byUser, null);
    }

    private void startHyperTrackTracking(final boolean byUser, final HyperTrackCallback callback) {
        // HACK: Check if user is tracking currently or not
        // Only for exisitng users because Permission and Location Settings have been checked here
        if (!HyperTrack.isTracking()) {
            HyperTrack.startTracking(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    if (callback != null)
                        callback.onSuccess(response);
                    if (byUser) {
                        SharedPreferenceManager.setTrackingON();
                        supportInvalidateOptionsMenu();
                    }
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    requestLocation();
                    if (callback != null)
                        callback.onError(errorResponse);

                }
            });
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

    public void requestLocation() {
        if (HyperTrack.checkLocationPermission(this)) {
            if (!HyperTrack.checkLocationServices(this)) {
                HyperTrack.requestLocationServices(Home.this, null);
            }
        } else {
            HyperTrack.requestPermissions(this);
        }
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
            Crashlytics.logException(e);
        }
    }

    /**
     * Method to Share current task
     */
    private void share() {
        if (ActionManager.getSharedManager(this).getHyperTrackAction() == null)
            return;

        String shareMessage = ActionManager.getSharedManager(Home.this).getHyperTrackAction().getShareMessage();
        if (shareMessage == null) {
            Toast.makeText(Home.this, R.string.share_message_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivityForResult(Intent.createChooser(sharingIntent, "Share via"), Constants.SHARE_REQUEST_CODE);
    }

    /**
     * Method to Navigate current task in Google Navigation
     */
    private void navigate() {
        ActionManager actionManager = ActionManager.getSharedManager(Home.this);

        Place place = actionManager.getPlace();
        if (place == null) {
            return;
        }

        Double latitude = place.getLocation().getLatitude();
        Double longitude = place.getLocation().getLongitude();

        String navigationString = latitude.toString() + "," + longitude.toString() + "&mode=d";
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
                if (!TextUtils.isEmpty(item.getTitle().toString())) {
                    if (item.getTitle().toString().equalsIgnoreCase("Resume Tracking")) {
                        startHyperTrackTracking(true, new HyperTrackCallback() {
                            @Override
                            public void onSuccess(@NonNull SuccessResponse response) {
                                item.setTitle("Pause Tracking");
                            }

                            @Override
                            public void onError(@NonNull ErrorResponse errorResponse) {

                            }
                        });

                    } else {
                        stopHyperTrackTracking();
                        item.setTitle("Resume Tracking");
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    HyperTrack.requestLocationServices(Home.this, null);

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                            getString(R.string.location_permission_never_allow));
                }
                break;
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.v(TAG, "Geofencing added successfully");
        } else {
            Log.v(TAG, "Geofencing not added. There was an error");
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectivityChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationChangeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

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

        AppEventsLogger.activateApp(getApplication());
    }

    @Override
    public void onBackPressed() {
        if (!isvehicleTypeTabLayoutVisible) {
            HyperTrack.removeActions(null);
            super.onBackPressed();
        } else {
            OnCompleteTask();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        HyperTrack.removeActions(null);
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
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
}