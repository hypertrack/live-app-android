package io.hypertrack.sendeta.view;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
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
import com.hypertrack.lib.internal.common.util.HTTextUtils;
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
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
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
    private TextView infoMessageViewText;
    private LinearLayout infoMessageView;
    private Button retryButton;
    private ImageButton shareButton, navigateButton;
    private Place expectedPlace;
    private ProgressDialog mProgressDialog;
    private boolean isMapLoaded = false, isvehicleTypeTabLayoutVisible = false;
    private float zoomLevel = 15.0f;
    private HTUserVehicleType selectedVehicleType = SharedPreferenceManager.getLastSelectedVehicleType(this);
    private HomeMapAdapter adapter;
    private IHomePresenter<HomeView> presenter = new HomePresenter();
    private CoordinatorLayout rootLayout;
    private boolean fromPlaceline = false;

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

    public MapFragmentCallback callback = new MapFragmentCallback() {
        @Override
        public void onMapReadyCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
            onMapReady(map);
        }

        @Override
        public void onExpectedPlaceSelected(Place expectedPlace) {
            // Check if destination place was selected
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
            ActionManager actionManager = ActionManager.getSharedManager(Home.this);

            if (actionManager.getHyperTrackAction() != null) {
                //Get the index of active action from refreshed action Ids
                int index = refreshedActionIds.indexOf(actionManager.getHyperTrackActionId());

                if (index >= 0) {

                    //Get refreshed action Data
                    Action action = refreshedActions.get(refreshedActionIds.indexOf(
                            actionManager.getHyperTrackActionId()));
                    //Update action data to Shared Preference
                    actionManager.setHyperTrackAction(action);

                    //If action has completed hide stop sharing button
                    if (action.hasActionFinished()) {
                        navigateButton.setVisibility(View.GONE);
                        shareButton.setVisibility(View.GONE);
                        if (mProgressDialog != null)
                            mProgressDialog.dismiss();
                    }
                }
            }
        }

        @Override
        public void onLiveLocationShareButtonClicked(Action action) {
            if (action != null) {
                expectedPlace = action.getExpectedPlace();
                lookupId = action.getLookupId();
            }
            shareLiveLocation();
        }

        @Override
        public void onLiveLocationStopButtonClicked(Action action) {
            if (HyperTrack.checkLocationPermission(Home.this) && HyperTrack.checkLocationServices(Home.this)) {
                mProgressDialog = new ProgressDialog(Home.this);
                mProgressDialog.setMessage(getString(R.string.stop_sharing_message));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                presenter.stopSharing(ActionManager.getSharedManager(Home.this));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getIntent() != null && getIntent().hasExtra("class_from")) {
            if (getIntent().getStringExtra("class_from").
                    equalsIgnoreCase(Placeline.class.getSimpleName()))
                fromPlaceline = true;
        }
        //  startAnimation();
        // Initialize UI Views
        initializeUIViews();

        // Initialize Map Fragment added in Activity Layout to getMapAsync
        HyperTrackMapFragment htMapFragment;
        htMapFragment = (HyperTrackMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.htMapfragment);

        adapter = new HomeMapAdapter(this, getToolbar());
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

        // Check if location is being shared currently
        restoreLocationSharingIfNeeded();

        // Handles Tracking Url deeplink
        handleTrackingUrlDeeplink();

        // Attach View Presenter to View
        presenter.attachView(this);

    }

    private void startAnimation() {
        rootLayout = (CoordinatorLayout) findViewById(R.id.parent_layout);
        rootLayout.setVisibility(View.INVISIBLE);
        ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    circularRevealActivity();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void circularRevealActivity() {

        int cx = (rootLayout.getLeft() + rootLayout.getRight());
        int cy = (rootLayout.getTop() + rootLayout.getBottom());

        int finalRadius = (int) Math.hypot(rootLayout.getRight(), rootLayout.getBottom());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0, finalRadius);
        circularReveal.setDuration(600);

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    private void initializeUIViews() {
        // Initialize Retry button
        retryButton = (Button) findViewById(R.id.retryButton);

        // Initialize Share button and its click listener
        shareButton = (ImageButton) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.shareTrackingUrl(ActionManager.getSharedManager(Home.this));
                AnalyticsStore.getLogger().tappedShareIcon(false);
            }
        });

        // Initialize Navigate button and its click listener
        navigateButton = (ImageButton) findViewById(R.id.navigateButton);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.openNavigationForExpectedPlace(ActionManager.getSharedManager(Home.this));
                AnalyticsStore.getLogger().tappedNavigate();
            }
        });

        // Setup Info message view layouts
        infoMessageView = (LinearLayout) findViewById(R.id.home_info_message_view);
        infoMessageViewText = (TextView) findViewById(R.id.home_info_message_text);
    }

    private void shareLiveLocation() {
        //Check if Location Permission has been granted & Location has been enabled
        if (HyperTrack.checkLocationPermission(this) && HyperTrack.checkLocationServices(this)) {
            //Check if user has already shared his tracking link
            if (ActionManager.getSharedManager(Home.this).getHyperTrackAction() == null ||
                    ActionManager.getSharedManager(Home.this).getHyperTrackAction().hasActionFinished()) {
                // Start the Action
                startAction();
            } else {
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
                                        .setContextText(getString(R.string.notification_set_destination))
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
     *
     * @param place Expected place for the user
     */
    private void onSelectPlace(final Place place) {
        if (place == null || place.getLocation() == null || this.isFinishing()) {
            return;
        }
        expectedPlace = place;
        ActionManager.getSharedManager(this).setPlace(expectedPlace);
        //presenter.getETAForExpectedPlace(place, selectedVehicleType.toString(), ActionManager.getSharedManager(this));
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
            retryButton.setVisibility(View.GONE);
            retryButton.setOnClickListener(null);
        }
    }

    private void onETASuccess(ETAResponse response, Place place) {
        // Make the VehicleTabLayout visible onETASuccess
        // AnimationUtils.expand(vehicleTypeTabLayout);
        //isvehicleTypeTabLayoutVisible = true;
        LatLng latLng = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
        updateViewForETASuccess((int) response.getDuration() / 60, latLng);
        expectedPlace = place;
        ActionManager.getSharedManager(this).setPlace(expectedPlace);
    }

    private void updateViewForETASuccess(Integer etaInMinutes, LatLng latLng) {
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
//            int top = isvehicleTypeTabLayoutVisible ? getResources().getDimensionPixelSize(R.dimen.map_top_padding_with_vehicle_type_layout) :
//                    getResources().getDimensionPixelSize(R.dimen.map_top_padding);
            int top = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            int left = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            int bottom = getResources().getDimensionPixelSize(R.dimen.map_side_padding);

          /*  if (stopSharingButton.isShown())
                bottom = getResources().getDimensionPixelSize(R.dimen.map_bottom_padding);*/
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
        lookupId = ActionManager.getSharedManager(this).getHyperTrackAction().getLookupId();

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

        shareButton.setVisibility(View.VISIBLE);
        navigateButton.setVisibility(View.VISIBLE);

        // Update SelectedVehicleType in persistentStorage
        SharedPreferenceManager.setLastSelectedVehicleType(selectedVehicleType);


    }

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

        shareButton.setVisibility(View.GONE);
        navigateButton.setVisibility(View.GONE);

        if (SharedPreferenceManager.isTrackingON()) {
            startHyperTrackTracking(true);
        } else {
            stopHyperTrackTracking();
        }


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
            Crashlytics.logException(e);
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
        //Check if map application is installed or not.
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                Toast.makeText(this, "Please install a map application", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startHyperTrackTracking(final boolean byUser) {
        // HACK: Check if user is tracking currently or not
        // Only for exisitng users because Permission and Location Settings have been checked here
        if (!HyperTrack.isTracking()) {
            HyperTrack.startTracking();
            if (byUser) {
                SharedPreferenceManager.setTrackingON();
            }

        } else if (byUser) {
            SharedPreferenceManager.setTrackingON();
        }
    }

    private void stopHyperTrackTracking() {
        HyperTrack.stopTracking();
        SharedPreferenceManager.setTrackingOFF();

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

            if (!HTTextUtils.isEmpty(countryName)) {
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
    protected void onResume() {
        super.onResume();

        ActionManager actionManager = ActionManager.getSharedManager(Home.this);
        if (actionManager.getHyperTrackAction() != null && !actionManager.getHyperTrackAction().isCompleted()) {
            actionManager.setActionComletedListener(actionCompletedListener);

            lookupId = actionManager.getHyperTrackAction().getLookupId();
            HyperTrack.trackActionByLookupId(lookupId, null);
        }
        /*else {
            presenter.stopSharing();
        }*/

        // Check if Location & Network are Enabled
        updateInfoMessageView();

        // Re-register BroadcastReceiver for Location_Change, Network_Change & GCM
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationChangeReceiver,
                new IntentFilter(GpsLocationReceiver.LOCATION_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectivityChangeReceiver,
                new IntentFilter(NetworkChangeReceiver.NETWORK_CHANGED));

        AppEventsLogger.activateApp(getApplication());
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

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectivityChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationChangeReceiver);
    }

    @Override
    public void onBackPressed() {

        HyperTrack.removeActions(null);

        ActionManager actionManager = ActionManager.getSharedManager(this);

        //If tracking action has completed and summary view is visible then on back press clear the view
        // so that user can share new tracking url without reopening the app.
        if (actionManager.getHyperTrackAction() != null &&
                actionManager.getHyperTrackAction().hasActionFinished()) {

            // Reset lookupId variable
            lookupId = null;
            OnStopSharing();
            ActionManager.getSharedManager(this).clearState();
            return;

        } else if (isvehicleTypeTabLayoutVisible) {
            OnStopSharing();
            return;
        }
        super.onBackPressed();
        if (!fromPlaceline) {
            startActivity(new Intent(Home.this, Placeline.class));
        }
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    protected void onStop() {

        ActionManager actionManager = ActionManager.getSharedManager(this);

        //If tracking action has completed and summary view is visible then on back press clear the view
        // so that user can share new tracking url without reopening the app.
        if (actionManager.getHyperTrackAction() != null &&
                actionManager.getHyperTrackAction().hasActionFinished()) {

            // Reset lookupId variable
            lookupId = null;

            OnStopSharing();
            ActionManager.getSharedManager(this).clearState();
        }

        if (mProgressDialog != null)
            mProgressDialog.dismiss();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Detach View from Presenter
        presenter.detachView();
        super.onDestroy();
    }
}