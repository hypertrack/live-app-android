package io.hypertrack.sendeta.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.callbacks.HyperTrackEventCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParams;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.HyperTrackError;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.ServiceNotificationParams;
import com.hypertrack.lib.models.ServiceNotificationParamsBuilder;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.MetaApplication;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.PlaceAutocompleteAdapter;
import io.hypertrack.sendeta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.model.TaskETAResponse;
import io.hypertrack.sendeta.model.UserPlace;
import io.hypertrack.sendeta.service.FetchAddressIntentService;
import io.hypertrack.sendeta.service.FetchLocationIntentService;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.LocationStore;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.TaskManager;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.store.callback.ActionManagerCallback;
import io.hypertrack.sendeta.store.callback.ActionManagerListener;
import io.hypertrack.sendeta.store.callback.TaskETACallback;
import io.hypertrack.sendeta.util.AnimationUtils;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.GpsLocationReceiver;
import io.hypertrack.sendeta.util.ImageUtils;
import io.hypertrack.sendeta.util.KeyboardUtils;
import io.hypertrack.sendeta.util.LocationUtils;
import io.hypertrack.sendeta.util.NetworkChangeReceiver;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.PhoneUtils;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.util.SwipeButton;
import io.hypertrack.sendeta.util.SwipeButtonCustomItems;
import io.hypertrack.sendeta.util.images.RoundedImageView;

public class Home extends DrawerBaseActivity implements ResultCallback<Status>, LocationListener,
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = Home.class.getSimpleName();
    public CardView mAutocompleteResultsLayout;
    public RecyclerView mAutocompleteResults;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfoMessageView();
        }
    };
    private OnboardingUser user;
    private GoogleMap mMap;
    private Marker currentLocationMarker, destinationLocationMarker;
    private Location defaultLocation = new Location("default");
    BroadcastReceiver mLocationChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfoMessageView();

            Log.d(TAG, "Location Changed");

            // Initiate FusedLocation Updates on Location Changed
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && LocationUtils.isLocationEnabled(Home.this)) {
                // Remove location updates so that it resets
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, Home.this);

                //change the time of location updates
                createLocationRequest(LocationUtils.INITIAL_LOCATION_UPDATE_INTERVAL_TIME);

                //restart location updates with the new interval
                resumeLocationUpdates();

                locationFrequencyIncreased = true;
            }
        }
    };
    private AppBarLayout appBarLayout;
    private TabLayout vehicleTypeTabLayout;
    private TextView destinationText, destinationDescription, mAutocompletePlacesView, infoMessageViewText;
    private LinearLayout enterDestinationLayout, infoMessageView, endTripLoaderAnimationLayout;
    private FrameLayout mAutocompletePlacesLayout, bottomButtonLayout;
    private Button sendETAButton, retryButton;
    private SwipeButton endTripSwipeButton;
    private ImageButton shareButton, navigateButton, favoriteButton;
    private View customMarkerView;
    private RoundedImageView heroMarkerProfileImageView;
    private ProgressBar mAutocompleteLoader;
    private PlaceAutocompleteAdapter mAdapter;
    private UserPlace restoreTaskMetaPlace;
    private UserPlace destinationPlace;
    private ProgressDialog mProgressDialog;
    private boolean enterDestinationLayoutClicked = false, shouldRestoreTask = false, locationPermissionChecked = false,
            locationFrequencyIncreased = true, selectPushedTaskMetaPlace = false, handlePushedTaskDeepLink = false,
            destinationAddressGeocoded = false, isMapLoaded = false;
    private UserPlace pushedTaskMetaPlace;
    private String pushedTaskID;
    private float zoomLevel = 1.0f;
    private Integer etaInMinutes = 0;
    private Bitmap userBitmap;
    private HTUserVehicleType selectedVehicleType = SharedPreferenceManager.getLastSelectedVehicleType(this);

    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(UserPlace place) {
            // Set pushedTask Location Address received from ReverseGeocoding
            if (selectPushedTaskMetaPlace && destinationAddressGeocoded) {
                place = pushedTaskMetaPlace;
                destinationAddressGeocoded = false;
            }

            // Reset Handle pushedTask DeepLink flag
            selectPushedTaskMetaPlace = false;

            // On Click Disable handling/showing any more results
            mAdapter.setSearching(false);

            // Check if selected place is a User Favorite to log Analytics Event
            boolean isFavorite = false;
            user = OnboardingManager.sharedManager().getUser();
            if (user != null && place != null) {
                isFavorite = user.isSynced(place);
            }
            AnalyticsStore.getLogger().selectedAddress(mAutocompletePlacesView.getText().length(), isFavorite);

            //Restore Default State for Enter Destination Layout
            onEnterDestinationBackClick(null);

            if (place != null) {
                // Set the Enter Destination Layout to Selected Place
                destinationText.setGravity(Gravity.START);
                destinationText.setText(place.getName());

                if (!TextUtils.isEmpty(place.getAddress())) {
                    // Set the selected Place Description as Place Address
                    destinationDescription.setText(place.getAddress());
                    destinationDescription.setVisibility(View.VISIBLE);
                }
            }

            KeyboardUtils.hideKeyboard(Home.this, mAutocompletePlacesView);

            // Initialize VehicleTabLayout
            initializeVehicleTypeTab();

            onSelectPlace(place);
        }

        @Override
        public void OnError() {
            destinationDescription.setVisibility(View.GONE);
            destinationDescription.setText("");
        }
    };
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            final String constraint = s != null ? s.toString() : "";


            if (searchRunnable != null)
                searchHandler.removeCallbacks(searchRunnable);

            searchRunnable = new Runnable() {
                @Override
                public void run() {
                    mAdapter.setFilterString(constraint);
                }
            };

            searchHandler.postDelayed(searchRunnable, 400);


            // Show Autocomplete Data Fetch Loader when user typed something
            if (constraint.length() > 0)
                mAutocompleteLoader.setVisibility(View.VISIBLE);
        }
    };
    private AdapterView.OnClickListener enterDestinationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            enterDestinationLayoutClicked = true;

            // Check If LOCATION Permission is available
            if (!(ContextCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                // Show Rationale & Request for LOCATION permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PermissionUtils.showRationaleMessageAsDialog(Home.this, Manifest.permission.ACCESS_FINE_LOCATION,
                            getString(R.string.location_permission_rationale_msg));
                } else {
                    PermissionUtils.requestPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION);
                }

                return;
            }

            // Check if Location was enabled & if valid location was received
            if (!LocationUtils.isLocationEnabled(Home.this)) {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                    checkIfLocationIsEnabled();
            } else {
                // Reset Retry button
                retryButton.setVisibility(View.GONE);
                retryButton.setOnClickListener(null);

                // Reset Current State when user chooses to edit destination
                TaskManager.getSharedManager(Home.this).clearState();
                OnCompleteTask();

                // Hide the AppBar
                AnimationUtils.collapse(appBarLayout, 200);

                // Reset the Autocomplete TextView
                mAutocompletePlacesView.setText("");
                mAutocompletePlacesView.requestFocus();
                KeyboardUtils.showKeyboard(Home.this, mAutocompletePlacesView);

                // Show the Autocomplete Places Layout
                enterDestinationLayout.setVisibility(View.GONE);
                mAutocompletePlacesLayout.setVisibility(View.VISIBLE);

                showAutocompleteResults(true);
                updateAutoCompleteResults();
            }
        }
    };
    private ActionManagerListener onActionRefreshedListener = new ActionManagerListener() {
        @Override
        public void OnCallback() {
            if (Home.this.isFinishing())
                return;

            // Get TaskManager Instance
            TaskManager taskManager = TaskManager.getSharedManager(Home.this);
            if (taskManager == null)
                return;

            // Fetch updated HypertrackTask Instance
            Action action = taskManager.getHyperTrackAction();
            if (action == null) {
                return;
            }

            // Update ETA & Dihsplay Statuses using Task's Display field
            updateETAForOnGoingTask(action, taskManager.getPlace());
            updateDisplayStatusForOngoingTask(action);

            // Check if DestinationLocation has been updated
            // NOTE: Call this method after updateETAForOnGoingTask()
            updateDestinationLocationIfApplicable(action);
        }
    };
    private ActionManagerListener onActionCompletedListener = new ActionManagerListener() {
        @Override
        public void OnCallback() {

            // Call OnCompleteTask method on UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Home.this.completeTask();
                    HyperTrack.clearServiceNotificationParams();
                }
            });
        }
    };

    private void onSelectPlace(final UserPlace place) {
        if (place == null) {
            return;
        }


        getEtaForDestination(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()), new TaskETACallback() {
            @Override
            public void OnSuccess(TaskETAResponse etaResponse) {
                // Hide Retry Button
                showRetryButton(false, null);
                etaInMinutes = (int) etaResponse.getDuration();
                onETASuccess(etaResponse, place);

                if (handlePushedTaskDeepLink) {
                    sendETAButton.performClick();
                }
            }

            @Override
            public void OnError() {
                // Show Retry button to fetch eta again
                showRetryButton(true, place);

                if (destinationLocationMarker != null) {
                    destinationLocationMarker.remove();
                    destinationLocationMarker = null;
                }

                showETAError();
            }
        });
    }

    private void showRetryButton(boolean showRetryButton, final UserPlace place) {

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

    private void getEtaForDestination(LatLng destinationLocation, final TaskETACallback callback) {
        if (Home.this.isFinishing())
            return;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.calculating_eta_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        if (currentLocationMarker == null || currentLocationMarker.getPosition() == null || destinationLocation == null) {
            mProgressDialog.dismiss();
            callback.OnError();
            return;
        }
        TaskManager.getSharedManager(Home.this).getETA(currentLocationMarker.getPosition(), destinationLocation, selectedVehicleType.toString(), new TaskETACallback() {
            @Override
            public void OnSuccess(TaskETAResponse etaResponse) {
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


    private void showETAError() {
        Toast.makeText(this, getString(R.string.eta_fetching_error), Toast.LENGTH_SHORT).show();
    }

    private void onETASuccess(TaskETAResponse response, UserPlace place) {
        // Make the VehicleTabLayout visible onETASuccess
        AnimationUtils.expand(vehicleTypeTabLayout);
        LatLng latLng = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
        updateViewForETASuccess((int) response.getDuration() / 60, latLng);
        destinationPlace = place;
        TaskManager.getSharedManager(this).setPlace(destinationPlace);
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

    private void updateAutoCompleteResults() {
        List<UserPlace> places = null;
        user = OnboardingManager.sharedManager().getUser();
        if (user != null) {
            places = user.getPlaces();
        }

        if (places == null || places.isEmpty()) {
            return;
        }

        mAdapter.refreshFavorites(places);
        mAdapter.notifyDataSetChanged();
    }

    private void updateInfoMessageView() {
        if (!LocationUtils.isLocationEnabled(Home.this)) {
            infoMessageView.setVisibility(View.VISIBLE);

            if (!isInternetEnabled()) {
                infoMessageViewText.setText(R.string.location_off_info_message);
            } else {
                infoMessageViewText.setText(R.string.location_off_info_message);
            }
        } else {
            infoMessageView.setVisibility(View.VISIBLE);

            if (!isInternetEnabled()) {
                infoMessageViewText.setText(R.string.internet_off_info_message);
            } else {
                // Both Location & Network Enabled, Hide the Info Message View
                infoMessageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /*// Initialize UserStore
        OnboardingManager.sharedManager().initializeUser();*/

        // Initialize Toolbar without Home Button
        initToolbar(getResources().getString(R.string.toolbar_title), false);

        user = OnboardingManager.sharedManager().getUser();
        if (user.getImageBitmap() != null) {
            Bitmap bitmap = user.getImageBitmap();
            setToolbarIcon(bitmap);
        }

        // Initialize Maps
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        // Initialize UI Views
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        retryButton = (Button) findViewById(R.id.retryButton);

        // Get Default User Location from his CountryCode
        // SKIP: if Location Permission is Granted and Location is Enabled
        if (!HyperTrack.checkLocationServices(this) || !HyperTrack.checkLocationPermission(this)) {
            geocodeUserCountryName();
        }

        initGoogleClient();

        createLocationRequest(LocationUtils.INITIAL_LOCATION_UPDATE_INTERVAL_TIME);
        setupEnterDestinationView();
        setupAutoCompleteView();
        setupShareButton();
        setupSendETAButton();
        setupEndTripSwipeButton();
        setupNavigateButton();
        setupFavoriteButton();
        setupInfoMessageView();
        initCustomMarkerView();

        // Setup VehicleType TabLayout
        setupVehicleTypeTabLayout();

        // Check & Prompt User if Internet is Not Connected
        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
        }

        // Set HyperTrackCallback
        setHyperTrackCallback();

        //Ask for tracking permission
        checkForTrackingPermission();

        // Check if there is any currently running task to be restored
        restoreTaskStateIfNeeded();

        // Handle RECEIVE_ETA DeepLink
        Intent intent = getIntent();

        // Check if there is no Active Task currently
        if (!shouldRestoreTask && intent != null && intent.hasExtra(Constants.KEY_PUSH_TASK)) {
            handlePushedTaskDeepLink = true;
            selectPushedTaskMetaPlace = true;
            handlePushedTaskIntent(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        MenuItem menuItem = menu.findItem(R.id.tracking_toogle);
        if (SharedPreferenceManager.isTrackingON()) {
            menuItem.setTitle("Pause Tracking");
        } else {
            menuItem.setTitle("Resume Tracking");
        }
        return TaskManager.getSharedManager(this).getHyperTrackAction() == null;
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

    private void checkIfUserCreated() {
        final OnboardingUser onboardingUser = OnboardingUser.sharedOnboardingUser();
        if (TextUtils.isEmpty(onboardingUser.getId())) {

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.create_user));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            //Create a new user and set UserID
            HyperTrack.createUser(onboardingUser.getName(), onboardingUser.getPhone(), new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    if (response.getResponseObject() != null) {
                        User user = (User) response.getResponseObject();
                        String userID = user.getId();
                        onboardingUser.setId(userID);
                        OnboardingUser.setOnboardingUser();
                        checkForTrackingPermission();
                        if (mProgressDialog != null)
                            mProgressDialog.cancel();
                        UserStore.sharedStore.deleteUser();
                    }
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    if (mProgressDialog != null)
                        mProgressDialog.cancel();
                    Toast.makeText(Home.this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setHyperTrackCallback() {
        HyperTrack.setCallback(new HyperTrackEventCallback() {
            @Override
            public void onEvent(@NonNull final HyperTrackEvent event) {
                switch (event.getEventType()) {
                    case HyperTrackEvent.EventType.STOP_ENDED_EVENT:
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
                    default:
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
                startHyperTrackTracking(true);
           /* AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    .show();*/
                SharedPreferenceManager.setAskedForTrackingDialog();
            }
        }
    }

    private void initGoogleClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, 0  /*clientId */, this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void geocodeUserCountryName() {
        // Fetch Country Level Location only if no cached location is available
        Location lastKnownCachedLocation = LocationStore.sharedStore().getLastKnownUserLocation();
        if (lastKnownCachedLocation == null || lastKnownCachedLocation.getLatitude() == 0.0
                || lastKnownCachedLocation.getLongitude() == 0.0) {

            OnboardingManager onboardingManager = OnboardingManager.sharedManager();
            String countryName = PhoneUtils.getCountryName(onboardingManager.getUser().getCountryCode());

            if (!TextUtils.isEmpty(countryName)) {
                Intent intent = new Intent(this, FetchLocationIntentService.class);
                intent.putExtra(FetchLocationIntentService.RECEIVER, new GeocodingResultReceiver(new Handler()));
                intent.putExtra(FetchLocationIntentService.ADDRESS_DATA_EXTRA, countryName);
                startService(intent);
            }
        }
    }

    private void createLocationRequest(long locationUpdateIntervalTime) {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(100)
                .setInterval(locationUpdateIntervalTime)
                .setFastestInterval(locationUpdateIntervalTime);
    }

    private void setupEnterDestinationView() {
        // Initialize Enter Destination UI Views
        destinationText = (TextView) findViewById(R.id.destination_text);
        destinationDescription = (TextView) findViewById(R.id.destination_desc);
        enterDestinationLayout = (LinearLayout) findViewById(R.id.enter_destination_layout);

        // Set Click Listener for Enter Destination Layout
        if (enterDestinationLayout != null)
            enterDestinationLayout.setOnClickListener(enterDestinationClickListener);
    }

    private void setupAutoCompleteView() {
        // Initialize Autocomplete UI Views
        mAutocompletePlacesView = (AutoCompleteTextView) findViewById(R.id.autocomplete_places);
        mAutocompletePlacesLayout = (FrameLayout) findViewById(R.id.autocomplete_places_layout);
        mAutocompleteResults = (RecyclerView) findViewById(R.id.autocomplete_places_results);
        mAutocompleteResultsLayout = (CardView) findViewById(R.id.autocomplete_places_results_layout);
        mAutocompleteLoader = (ProgressBar) findViewById(R.id.autocomplete_progress);
        mAutocompletePlacesView.addTextChangedListener(mTextWatcher);

        // Initialize Autocomplete Results RecyclerView & Adapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(Home.this);
        layoutManager.setAutoMeasureEnabled(true);
        mAutocompleteResults.setLayoutManager(layoutManager);

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, mPlaceAutoCompleteListener);
        mAutocompleteResults.setAdapter(mAdapter);
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
        sendETAButton = (Button) findViewById(R.id.requestETAButton);
        bottomButtonLayout = (FrameLayout) findViewById(R.id.home_bottomButtonLayout);

        // Set Click Listener for SendETA Button
        sendETAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (SharedPreferenceManager.isTrackingON()) {
                    createSharingLink();
                } else {
                    startHyperTrackTracking(false);
                    createSharingLink();
                }
            }
        });
    }

    private void createSharingLink() {
        //Check if Location Permission has been granted & Location has been enabled
        if (PermissionUtils.checkForPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION)
                && LocationUtils.isLocationEnabled(Home.this)) {
            if (!TaskManager.getSharedManager(Home.this).isActionLive(null)) {
                // Start the Task
                startAction();
            } else {
                // Reset Current State when user chooses to edit destination
                TaskManager.getSharedManager(Home.this).clearState();
                OnCompleteTask();
            }
        } else {
            checkForLocationPermission();
        }
    }


    private void setupEndTripSwipeButton() {
        endTripSwipeButton = (SwipeButton) findViewById(R.id.endTripSwipeButton);
        endTripLoaderAnimationLayout = (LinearLayout) findViewById(R.id.endTripLoaderAnimationLayout);

        SwipeButtonCustomItems swipeButtonSettings = new SwipeButtonCustomItems() {
            @Override
            public void onSwipeConfirm() {
                // Check if Location Permission has been granted & Location has been enabled
                if (HyperTrack.checkLocationPermission(Home.this) && HyperTrack.checkLocationServices(Home.this)) {
                    showEndingTripAnimation(true);
                    completeTask();
                } else {
                    if (!HyperTrack.checkLocationServices(Home.this)) {
                        HyperTrack.requestLocationServices(Home.this, new HyperTrackCallback() {
                            @Override
                            public void onSuccess(@NonNull SuccessResponse response) {
                                requestLocationUpdates();
                            }

                            @Override
                            public void onError(@NonNull ErrorResponse errorResponse) {
                                Toast.makeText(Home.this, "Please enable location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        HyperTrack.requestPermissions(Home.this);
                    }
                }

            }
        };

        swipeButtonSettings.setButtonPressText(getString(R.string.action_slide_to_end_sharing))
                .setActionConfirmText(getString(R.string.action_slide_to_end_sharing));

        if (endTripSwipeButton != null) {
            endTripSwipeButton.setSwipeButtonCustomItems(swipeButtonSettings);
        }
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

    private void setupFavoriteButton() {
        // Initialize Favorite Button UI View
        favoriteButton = (ImageButton) findViewById(R.id.favorite_button);
        if (favoriteButton != null)
            favoriteButton.setVisibility(View.GONE);
    }

    private void setupInfoMessageView() {
        infoMessageView = (LinearLayout) findViewById(R.id.home_info_message_view);
        infoMessageViewText = (TextView) findViewById(R.id.home_info_message_text);
    }

    private void initCustomMarkerView() {
        // Initialize Custom Marker (Hero Marker) UI View
        customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_hero_marker, null);
        heroMarkerProfileImageView = (RoundedImageView) customMarkerView.findViewById(R.id.profile_image);
        updateProfileImage();
    }

    private void updateProfileImage() {

        try {
            Drawable d = heroMarkerProfileImageView.getDrawable();
            if (d != null && d instanceof BitmapDrawable) {
                ((BitmapDrawable) d).getBitmap().recycle();
            }
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }

        try {
            if (userBitmap == null) {
                user = OnboardingManager.sharedManager().getUser();
                if (user != null) {
                    userBitmap = user.getImageBitmap();
                }
            }

            if (userBitmap != null)
                heroMarkerProfileImageView.setImageBitmap(userBitmap);

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void restoreTaskStateIfNeeded() {
        final TaskManager taskManager = TaskManager.getSharedManager(this);

        //Check if there is any existing task to be restored
        if (taskManager.shouldRestoreState()) {

            Log.v(TAG, "Task is active");
            HTLog.i(TAG, "Task restored successfully.");

            restoreTaskMetaPlace = taskManager.getPlace();

            destinationText.setGravity(Gravity.START);
            destinationText.setText(restoreTaskMetaPlace.getName());

            if (!TextUtils.isEmpty(restoreTaskMetaPlace.getAddress())) {
                destinationDescription.setText(restoreTaskMetaPlace.getAddress());
                destinationDescription.setVisibility(View.VISIBLE);
            }

            // Start the Task
            LatLng latLng = new LatLng(restoreTaskMetaPlace.getLocation().getLatitude(), restoreTaskMetaPlace.getLocation().getLongitude());

            if (etaInMinutes == null || etaInMinutes == 0) {
                Action action = taskManager.getHyperTrackAction();
                if (action != null && action.getActionDisplay() != null && !TextUtils.isEmpty(action.getActionDisplay().getDurationRemaining())) {
                    Double displayETA = Double.valueOf(action.getActionDisplay().getDurationRemaining()) / 60;
                    etaInMinutes = displayETA.intValue();
                }
            }

            updateViewForETASuccess(etaInMinutes != 0 ? etaInMinutes : null, latLng);
            onStartTask();

            shouldRestoreTask = true;

        } else {
            HTLog.e(TAG, "No Task to restore.");
            shouldRestoreTask = false;

            // Initialize VehicleTabLayout
            initializeVehicleTypeTab();
        }
    }

    private void handlePushedTaskIntent(Intent intent) {
        // Fetch Task from Intent Params, if available
        pushedTaskID = intent.getStringExtra(Constants.KEY_TASK_ID);

        if (!TextUtils.isEmpty(pushedTaskID)) {

            double[] coords = new double[2];
            coords[0] = intent.getDoubleExtra(Constants.KEY_PUSH_DESTINATION_LAT, 0.0);
            coords[1] = intent.getDoubleExtra(Constants.KEY_PUSH_DESTINATION_LNG, 0.0);

            // Check if valid Destination Coordinates were provided or not
            if (coords[0] != 0.0 && coords[1] != 0.0) {
                String destinationName = coords[0] + ", " + coords[1];
                pushedTaskMetaPlace = new UserPlace(destinationName,
                        coords[0], coords[1]);

                // Reverse Geocode the Destination Location Coordinates to an Address
                reverseGeocode(new LatLng(coords[0], coords[1]));
            }
        } else {
            handlePushedTaskDeepLink = false;
            selectPushedTaskMetaPlace = false;
        }
    }

    private void reverseGeocode(LatLng latLng) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, new ReverseGeocodingResultReceiver(new Handler()));
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, latLng);
        startService(intent);
    }

    public void onEnterDestinationBackClick(View view) {
        // Hide VehicleType TabLayout onStartTask success
        AnimationUtils.collapse(vehicleTypeTabLayout);

        enterDestinationLayoutClicked = false;

        // Show the AppBar
        AnimationUtils.expand(appBarLayout, 200);

        // Hide the Autocomplete Results Layout
        showAutocompleteResults(false);

        // Reset the Enter Destination Layout
        enterDestinationLayout.setVisibility(View.VISIBLE);
        mAutocompletePlacesLayout.setVisibility(View.GONE);

        KeyboardUtils.hideKeyboard(Home.this, mAutocompletePlacesView);
    }

    /**
     * Method to publish update received from Adapter on the Autocomplete Results List
     *
     * @param publish Flag to indicate whether to update/remove the results
     */
    public void processPublishedResults(boolean publish) {
        showAutocompleteResults(publish);
        mAutocompleteLoader.setVisibility(View.GONE);
    }

    private void showAutocompleteResults(boolean show) {
        if (show) {
            mAutocompleteResults.smoothScrollToPosition(0);
            mAutocompleteResults.setVisibility(View.VISIBLE);
            mAutocompleteResultsLayout.setVisibility(View.VISIBLE);
        } else {
            mAutocompleteResults.setVisibility(View.GONE);
            mAutocompleteResultsLayout.setVisibility(View.GONE);
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
        UserPlace place = TaskManager.getSharedManager(Home.this).getPlace();
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Mumbai, India.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Check if Task has to be Restored & Update Map for that task
        if (shouldRestoreTask && restoreTaskMetaPlace != null) {
            if (TaskManager.getSharedManager(Home.this).getHyperTrackAction() == null) {
                return;
            }

            LatLng latLng = new LatLng(restoreTaskMetaPlace.getLocation().getLatitude(), restoreTaskMetaPlace.getLocation().getLongitude());

            if (etaInMinutes != null && etaInMinutes != 0) {

                updateViewForETASuccess(etaInMinutes, latLng);
            } else {
                updateViewForETASuccess(null, latLng);
            }
            onStartTask();
        }

        // Set Default View for map according to User's LastKnownLocation
        Location lastKnownCachedLocation = LocationStore.sharedStore().getLastKnownUserLocation();
        if (lastKnownCachedLocation != null && lastKnownCachedLocation.getLatitude() != 0.0
                && lastKnownCachedLocation.getLongitude() != 0.0) {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lastKnownCachedLocation.getLatitude(), lastKnownCachedLocation.getLongitude()), 12.0f));

            // Add currentLocationMarker based on User's LastKnownCachedLocation
            LatLng latLng = new LatLng(lastKnownCachedLocation.getLatitude(),
                    lastKnownCachedLocation.getLongitude());

            if (currentLocationMarker == null) {
                addMarkerToCurrentLocation(latLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
            } else {
                currentLocationMarker.setPosition(latLng);
            }

        } else {
            // Else Set Default View for map according to either User's Default Location
            // (If Country Info was available) or (0.0, 0.0)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(defaultLocation.getLatitude(), defaultLocation.getLongitude()), zoomLevel));
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !locationPermissionChecked) {
            locationPermissionChecked = true;
            checkForLocationPermission();
        }

        updateMapPadding(false);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                isMapLoaded = true;
                updateMapView();
            }
        });
    }

    private void checkForLocationPermission() {
        // Check If LOCATION Permission is available & then if Location is enabled
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkIfLocationIsEnabled();
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

    private void updateMapPadding(boolean activeTask) {
        if (mMap != null) {
            int top = getResources().getDimensionPixelSize(R.dimen.map_top_padding);
            int left = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            int right = activeTask ? getResources().getDimensionPixelSize(R.dimen.map_active_task_side_padding) : getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            int bottom = activeTask ? getResources().getDimensionPixelSize(R.dimen.map_active_task_bottom_padding) : getResources().getDimensionPixelSize(R.dimen.map_bottom_padding);

            mMap.setPadding(left, top, right, bottom);
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

        if (currentLocationMarker == null || currentLocationMarker.getPosition() == null) {
            Toast.makeText(Home.this, R.string.invalid_current_location, Toast.LENGTH_SHORT).show();
            return;
        }

        if (handlePushedTaskDeepLink) {
            if (destinationAddressGeocoded) {
                updatePushedDestinationAddress();
            }
        }

        ActionParams params = new ActionParamsBuilder()
                .setExpectedPlace(destinationPlace)
                .setType(Action.ACTION_TYPE_VISIT)
                .build();

        // Call assignAction to start the tracking action
        HyperTrack.createAndAssignAction(params, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                if (response.getResponseObject() != null) {
                    Action action = (Action) response.getResponseObject();
                    action.getActionDisplay().setDurationRemaining(String.valueOf(etaInMinutes));
                    TaskManager taskManager = TaskManager.getSharedManager(Home.this);
                    taskManager.setHyperTrackAction(action);
                    taskManager.onActionStart();
                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                    }

                    // Show ShareCard
                    share();

                    onStartTask();
                    addToRecentSearch();

                    // Reset handle pushedTask DeepLink Flag
                    handlePushedTaskDeepLink = false;
                    HyperTrack.clearServiceNotificationParams();
                    AnalyticsStore.getLogger().startedTrip(true, null);
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

                // Reset handle pushedTask DeepLink Flag
                handlePushedTaskDeepLink = false;

                AnalyticsStore.getLogger().startedTrip(false, ErrorMessages.START_TRIP_FAILED);
            }
        });

    }

    private void addToRecentSearch() {
        if (destinationPlace != null) {
            UserPlace recent = destinationPlace;
            if (!destinationPlace.isHome() && !destinationPlace.isWork()) {
                int size = OnboardingUser.onboardingUser.getPlaces().size();
                if (destinationPlace.getUserPlaceID() == -1) {
                    recent.setUserPlaceID(size);
                    OnboardingUser.onboardingUser.addPlace(recent);
                    OnboardingUser.setOnboardingUser();
                }
            }
        }
    }

    private void updatePushedDestinationAddress() {


        // Update the selected place with updated destinationLocationAddress

        TaskManager.getSharedManager(this).setPlace(pushedTaskMetaPlace);

        // Set the Enter Destination Layout to Selected Place
        destinationText.setText(destinationPlace.getName());

        if (!TextUtils.isEmpty(destinationPlace.getAddress())) {
            // Set the selected Place Description as Place Address
            destinationDescription.setText(destinationPlace.getAddress());
            destinationDescription.setVisibility(View.VISIBLE);
        }

        destinationAddressGeocoded = false;
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
     * Method to Initiate COMPLETE TASK
     */
    private void completeTask() {
        TaskManager.getSharedManager(this).completeAction(new ActionManagerCallback() {
            @Override
            public void OnSuccess() {
                OnCompleteTask();

                AnalyticsStore.getLogger().tappedEndTrip(true, null);
                HTLog.i(TAG, "Complete Action (CTA) happened successfully.");

                showEndingTripAnimation(false);
            }

            @Override
            public void OnError() {
                showCompleteTaskError();

                AnalyticsStore.getLogger().tappedEndTrip(false, ErrorMessages.END_TRIP_FAILED);
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
        // Hide VehicleType TabLayout onStartTask success
        AnimationUtils.collapse(vehicleTypeTabLayout);

        sendETAButton.setVisibility(View.GONE);
        endTripSwipeButton.setVisibility(View.VISIBLE);
        endTripSwipeButton.setText(R.string.action_slide_to_end_sharing);

        shareButton.setVisibility(View.VISIBLE);
        navigateButton.setVisibility(View.VISIBLE);
        enterDestinationLayout.setOnClickListener(null);
        favoriteButton.setVisibility(View.VISIBLE);
        updateFavoritesButton();

        // Update SelectedVehicleType in persistentStorage
        SharedPreferenceManager.setLastSelectedVehicleType(selectedVehicleType);

        TaskManager taskManager = TaskManager.getSharedManager(Home.this);
        taskManager.setActionRefreshedListener(onActionRefreshedListener);
        taskManager.setActionComletedListener(onActionCompletedListener);

        updateMapPadding(true);
        updateMapView();
        supportInvalidateOptionsMenu();
    }

    private void updateETAForOnGoingTask(Action action, Place place) {
        if (place == null || action.getActionDisplay() == null ||
                TextUtils.isEmpty(action.getActionDisplay().getDurationRemaining())) {
            return;
        }

        LatLng destinationLocation = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());

        // Get ETA Value to display from TaskDisplay field
        Double displayETA = Double.valueOf(action.getActionDisplay().getDurationRemaining()) / 60;
        etaInMinutes = displayETA.intValue();
        updateDestinationMarker(destinationLocation, etaInMinutes);
    }

    private void updateDisplayStatusForOngoingTask(Action action) {
        // Set Toolbar Title as DisplayStatus for currently active task
        if (action != null && action.getActionDisplay() != null) {
            this.setTitle(action.getActionDisplay().getStatusText());
        } else {

            // Set Toolbar Title as AppName
            this.setTitle(getResources().getString(R.string.toolbar_title));
            this.setSubTitle("");
        }

        // Set Toolbar SubTitle as DisplaySubStatus for currently active task
        if (action != null && action.getActionDisplay() != null) {
            this.setSubTitle(action.getActionDisplay().getSubStatusText());

        } else {
            this.setSubTitle("");
        }
    }

    private String getFormattedTimeString(Double timeInSeconds) {
        if (this.isFinishing() || timeInSeconds == null || timeInSeconds < 0)
            return null;

        int hours = (int) (timeInSeconds / 3600);
        int remainder = (int) (timeInSeconds - (hours * 3600));
        int mins = remainder / 60;

        if (hours <= 0 && mins <= 0)
            return null;

        StringBuilder builder = new StringBuilder();

        if (hours > 0) {
            builder.append(hours)
                    .append(" ")
                    .append(this.getResources().getQuantityString(R.plurals.hour_text, hours))
                    .append(" ");
        }

        if (mins > 0) {
            builder.append(mins)
                    .append(" ")
                    .append(this.getResources().getQuantityString(
                            R.plurals.minute_text, mins))
                    .append(" ");
        }

        return builder.toString();
    }

    private void updateDestinationLocationIfApplicable(Action action) {
        // Check if updatedDestination Location is not null
        Place destinationLocation = action.getExpectedPlace();

        if (destinationLocation == null || destinationLocation.getLocation() == null)
            return;

        // Check if updatedDestination Location Coordinates are valid
        LatLng updatedDestinationLatLng = new LatLng(destinationLocation.getLocation().getLatitude(),
                destinationLocation.getLocation().getLongitude());
        if (updatedDestinationLatLng.latitude == 0.0 || updatedDestinationLatLng.longitude == 0.0)
            return;


        // Check if destinationMarker's location has been changed
        if (destinationLocationMarker != null && destinationLocationMarker.getPosition() != null) {
            if (!LocationUtils.areLocationsSame(destinationLocationMarker.getPosition(), updatedDestinationLatLng)) {

                // Check if the DestinationLocationID has changed or only LatLng has changed
                UserPlace lastUpdatedDestination = TaskManager.getSharedManager(Home.this).getLastUpdatedDestination();
                if (lastUpdatedDestination != null && !TextUtils.isEmpty(lastUpdatedDestination.getId())
                        && lastUpdatedDestination.getId().equalsIgnoreCase(destinationLocation.getId())) {

                    // Update Place
                    TaskManager.getSharedManager(this).setPlace(lastUpdatedDestination);
                    HTLog.i(TAG, "Destination Location updated");
                } else {


                    // Set the DestinationText as updatedDestination's address
                    destinationText.setGravity(Gravity.START);
                    destinationText.setText(lastUpdatedDestination.getName());

                    // Hide destinationDescription layout
                    destinationDescription.setText("");
                    destinationDescription.setVisibility(View.GONE);

                    HTLog.i(TAG, "Destination Location changed");
                }

                TaskManager.getSharedManager(Home.this).setPlace(lastUpdatedDestination);
                Home.this.updateFavoritesButton();

                // Update Geofencing Request for updatedDestinationLocation
                TaskManager.getSharedManager(Home.this).setupGeofencing();

                updateDestinationMarker(updatedDestinationLatLng, etaInMinutes);
                updateMapView();
            }
        }

        // Update DestinationLocation
        TaskManager.getSharedManager(Home.this).setLastUpdatedDestination(new UserPlace(destinationLocation));
    }

    /**
     * Method to update State Variables & UI to reflect Task Ended
     */
    private void OnCompleteTask() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        if (destinationLocationMarker != null) {
            destinationLocationMarker.remove();
            destinationLocationMarker = null;
        }

        sendETAButton.setVisibility(View.GONE);
        endTripSwipeButton.setVisibility(View.GONE);
        bottomButtonLayout.setVisibility(View.GONE);

        shareButton.setVisibility(View.GONE);
        navigateButton.setVisibility(View.GONE);

        mAutocompletePlacesView.setVisibility(View.VISIBLE);
        favoriteButton.setVisibility(View.GONE);

        // Reset the Destination Text View
        destinationText.setGravity(Gravity.CENTER);
        destinationText.setText("");

        // Reset the Destionation Description View
        destinationDescription.setVisibility(View.GONE);
        destinationDescription.setText("");

        enterDestinationLayout.setOnClickListener(enterDestinationClickListener);
        updateMapPadding(false);

        // Reset Toolbar Title on EndTrip
        this.setTitle(getResources().getString(R.string.toolbar_title));
        this.setSubTitle("");

        // Resume LocationUpdates
        if (MetaApplication.isActivityVisible()) {
            resumeLocationUpdates();
        }
        if (SharedPreferenceManager.isTrackingON()) {
            startHyperTrackTracking(true);
        } else {
            stopHyperTrackTracking();
        }
        supportInvalidateOptionsMenu();

    }

    private void updateDestinationMarker(LatLng destinationLocation, Integer etaInMinutes) {
        if (mMap == null) {
            return;
        }

        if (destinationLocationMarker != null) {
            destinationLocationMarker.remove();
            destinationLocationMarker = null;
        }

        View markerView = getDestinationMarkerView(etaInMinutes);

        Bitmap bitmap = ImageUtils.getBitMapForView(this, markerView);
        if (bitmap != null) {
            destinationLocationMarker = mMap.addMarker(new MarkerOptions()
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "mGoogleApiClient is connected");

        // CheckForLocationPermission if not already asked
        if (!locationPermissionChecked) {
            locationPermissionChecked = true;
            checkForLocationPermission();
        }

        // Initiate location updates if permission granted
        if (PermissionUtils.checkForPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                LocationUtils.isLocationEnabled(Home.this)) {
            requestLocationUpdates();
        }
    }

    private boolean isInternetEnabled() {
        try {
            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void requestLocationUpdates() {
        if (SharedPreferenceManager.isTrackingON())
            startHyperTrackTracking(false);
        startLocationPolling();
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
                HyperTrack.requestLocationServices(Home.this, new HyperTrackCallback() {
                    @Override
                    public void onSuccess(@NonNull SuccessResponse successResponse) {

                    }

                    @Override
                    public void onError(@NonNull ErrorResponse errorResponse) {
                    }
                });
            }

        } else {
            HyperTrack.requestPermissions(this);
        }
    }

    private void startLocationPolling() {
        try {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        } catch (SecurityException exception) {
            Crashlytics.logException(exception);
        }
    }

    /**
     * Method to check if the Location Services are enabled and in case not, request user to
     * enable them.
     */
    private void checkIfLocationIsEnabled() {


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest).setAlwaysShow(true);
        PendingResult<LocationSettingsResult> pendingResult =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        //Start Location Service here if not already active
                        Log.d(TAG, "Fetching Location started!");
                        requestLocationUpdates();

                        // Perform EnterDestinationLayout Click on Location Enabled, if user clicked on it
                        if (enterDestinationLayoutClicked && enterDestinationLayout != null) {
                            enterDestinationLayout.performClick();

                            // Handle pushedTask DeepLink
                        } else if (selectPushedTaskMetaPlace) {
                            mPlaceAutoCompleteListener.OnSuccess(pushedTaskMetaPlace);
                        }

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(Home.this, Constants.REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        // This happens when phone is in Airplane/Flight Mode
                        // Uncomment ErrorMessage to prevent this from popping up on AirplaneMode
                        // Toast.makeText(Home.this, R.string.invalid_current_location, Toast.LENGTH_SHORT).show();

                        // Reset EnterDestinationLayoutClicked Flag if Location change was unavailable
                        if (enterDestinationLayoutClicked)
                            enterDestinationLayoutClicked = false;

                        // Reset handle pushedTask DeepLink Flag if Location change was unavailable
                        if (selectPushedTaskMetaPlace)
                            selectPushedTaskMetaPlace = false;

                        break;
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "Location: " + latLng.latitude + ", " + latLng.longitude);

        // Update Current Location on the map
        updateCurrentLocation(location);

        // Check if Location Frequency was decreased to (INITIAL_LOCATION_UPDATE_INTERVAL_TIME)
        // Remove the existing FusedLocationUpdates, and resume it with
        // default Polling Frequency (LOCATION_UPDATE_INTERVAL_TIME)
        if (locationFrequencyIncreased) {
            locationFrequencyIncreased = false;

            // Remove location updates so that it resets
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }

            // Change the time of location updates
            createLocationRequest(LocationUtils.LOCATION_UPDATE_INTERVAL_TIME);

            // Restart location updates with the new interval
            requestLocationUpdates();
        }

    }

    private void updateCurrentLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (mMap != null) {
            if (currentLocationMarker == null) {
                addMarkerToCurrentLocation(latLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
            } else {
                currentLocationMarker.setPosition(latLng);
            }

            updateMapView();
        }

        mAdapter.setBounds(LocationUtils.getBounds(latLng, 10000));

        LocationStore.sharedStore().setCurrentLocation(location);
    }

    private void addMarkerToCurrentLocation(LatLng latLng) {
        Bitmap bitmap = ImageUtils.getBitMapForView(this, customMarkerView);
        if (bitmap != null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            bitmap.recycle();
        }

        // Handle pushedTask DeepLink
        if (selectPushedTaskMetaPlace && LocationUtils.isLocationEnabled(Home.this)) {
            mPlaceAutoCompleteListener.OnSuccess(pushedTaskMetaPlace);
        }
    }

    private void updateMapView() {
        if (mMap == null || !isMapLoaded) {
            return;
        }

        if (currentLocationMarker == null && destinationLocationMarker == null) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (currentLocationMarker != null) {
            LatLng current = currentLocationMarker.getPosition();
            builder.include(current);
        }

        if (destinationLocationMarker != null) {
            LatLng destination = destinationLocationMarker.getPosition();
            builder.include(destination);
        }

        LatLngBounds bounds = builder.build();

        try {
            CameraUpdate cameraUpdate;
            if (destinationLocationMarker != null && currentLocationMarker != null) {
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
            } else {
                LatLng latLng = currentLocationMarker != null ? currentLocationMarker.getPosition() : destinationLocationMarker.getPosition();
                cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            }

            mMap.animateCamera(cameraUpdate);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
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

    /**
     * Method to Share current task
     */
    private void share() {
        String shareMessage = TaskManager.getSharedManager(Home.this).getShareMessage();
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

        TaskManager taskManager = TaskManager.getSharedManager(Home.this);

        Place place = taskManager.getPlace();
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

    /**
     * Method to add current selected destination as a Favorite
     * (NOTE: Only Applicable for Live Task)
     */
    public void OnFavoriteClick(View view) {
        TaskManager taskManager = TaskManager.getSharedManager(Home.this);
        showAddPlace(taskManager.getPlace());

        AnalyticsStore.getLogger().tappedFavorite();
    }

    private void showAddPlace(UserPlace place) {

        Intent addPlace = new Intent(this, AddFavoritePlace.class);
        addPlace.putExtra("meta_place", place);
        startActivityForResult(addPlace, Constants.FAVORITE_PLACE_REQUEST_CODE, null);
    }

    /**
     * Method to add Click Listener to Favorite Icon
     * (depends if current task is a Live Task & selected Place is not already a favorite)
     */
    private void updateFavoritesButton() {
        TaskManager taskManager = TaskManager.getSharedManager(Home.this);
        if (taskManager.isActionLive(null)) {
            UserPlace place = null;
            if (taskManager.getPlace() != null && taskManager.getPlace().getLocation() != null) {
                place = taskManager.getPlace();
            }

            user = OnboardingManager.sharedManager().getUser();
            if (user == null) {
                return;
            }

            if (user.isFavorite(place)) {
                markAsFavorite();
            } else {
                markAsNotFavorite();
            }
        }
    }

    private void markAsFavorite() {
        favoriteButton.setSelected(true);
        favoriteButton.setClickable(false);
        favoriteButton.setImageResource(R.drawable.ic_favorite);
    }

    private void markAsNotFavorite() {
        favoriteButton.setSelected(false);
        favoriteButton.setImageResource(R.drawable.ic_favorite_hollow);
        favoriteButton.setClickable(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                        checkIfLocationIsEnabled();
                    }

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                            getString(R.string.location_permission_never_allow));

                    // Reset EnterDestinationLayoutClicked Flag if Location permission was denied
                    if (enterDestinationLayoutClicked)
                        enterDestinationLayoutClicked = false;

                    // Reset handle pushedTask DeepLink Flag if Location Permission was denied
                    if (selectPushedTaskMetaPlace)
                        selectPushedTaskMetaPlace = false;
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.REQUEST_CHECK_SETTINGS) {

            switch (resultCode) {
                case Activity.RESULT_OK:

                    Log.i(TAG, "User agreed to make required location settings changes.");
                    Log.d(TAG, "Fetching Location started!");
                    requestLocationUpdates();

                    // Perform EnterDestinationLayout Click on Location Enabled, if user clicked on it
                    if (enterDestinationLayoutClicked && enterDestinationLayout != null) {
                        enterDestinationLayout.performClick();

                        // Handle pushedTask DeepLink
                    } else if (selectPushedTaskMetaPlace) {
                        mPlaceAutoCompleteListener.OnSuccess(pushedTaskMetaPlace);
                    }

                    break;

                case Activity.RESULT_CANCELED:

                    // Location Service Enable Request denied, boo! Fire LocationDenied event
                    Log.i(TAG, "User chose not to make required location settings changes.");

                    // Reset EnterDestinationLayoutClicked Flag if Location permission was denied
                    if (enterDestinationLayoutClicked)
                        enterDestinationLayoutClicked = false;

                    break;
            }

        } else if (requestCode == Constants.FAVORITE_PLACE_REQUEST_CODE) {

            // Update the Place Data
            if (data != null && data.hasExtra(AddFavoritePlace.KEY_UPDATED_PLACE)) {
                UserPlace updatedPlace = (UserPlace) data.getSerializableExtra(AddFavoritePlace.KEY_UPDATED_PLACE);
                if (updatedPlace != null) {
                    TaskManager.getSharedManager(Home.this).setPlace(updatedPlace);

                    if (!TextUtils.isEmpty(updatedPlace.getName())) {
                        // Set the DestinationText as updatedDestination's Name
                        destinationText.setGravity(Gravity.START);
                        destinationText.setText(updatedPlace.getName());
                    }

                    if (!TextUtils.isEmpty(updatedPlace.getAddress())) {
                        // Set the DestinationDescription as updatedDestination's Address
                        destinationDescription.setText(updatedPlace.getAddress());
                        destinationDescription.setVisibility(View.VISIBLE);
                    }
                }
            }

            updateFavoritesButton();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectivityChangeReceiver);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        TaskManager.getSharedManager(Home.this).stopRefreshingAction();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check if user is created or not
        checkIfUserCreated();

        TaskManager taskManager = TaskManager.getSharedManager(Home.this);
        if (taskManager.getHyperTrackAction() != null && !taskManager.getHyperTrackAction().isCompleted()) {
            taskManager.startRefreshingAction(0);
            // Set Task Manager Listeners
            taskManager.setActionRefreshedListener(onActionRefreshedListener);
            taskManager.setActionComletedListener(onActionCompletedListener);
        } else {
            // Reset Toolbar Title as AppName in case no existing trip
            this.setTitle(getResources().getString(R.string.toolbar_title));
            this.setSubTitle("");
        }

        // Check if Location & Network are Enabled
        updateInfoMessageView();

        updateFavoritesButton();
        updateCurrentLocationMarker();

        // Re-register BroadcastReceiver for Location_Change, Network_Change & GCM
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationChangeReceiver,
                new IntentFilter(GpsLocationReceiver.LOCATION_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectivityChangeReceiver,
                new IntentFilter(NetworkChangeReceiver.NETWORK_CHANGED));

        AppEventsLogger.activateApp(getApplication());
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, Constants.PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void updateCurrentLocationMarker() {
        if (currentLocationMarker == null) {
            return;
        }

        LatLng position = currentLocationMarker.getPosition();
        currentLocationMarker.remove();
        initCustomMarkerView();

        addMarkerToCurrentLocation(position);
    }

    private void resumeLocationUpdates() {
        defaultLocation = new Location("default");

        // Check if Location is Enabled & Resume LocationUpdates
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (LocationUtils.isLocationEnabled(Home.this)) {
                requestLocationUpdates();
            }
        } else {
            initGoogleClient();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onBackPressed() {
        if (enterDestinationLayoutClicked) {
            onEnterDestinationBackClick(null);
        } else {
            super.onBackPressed();

            if (userBitmap != null) {
                userBitmap.recycle();
                userBitmap = null;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                    Location lastKnownCachedLocation = LocationStore.sharedStore().getLastKnownUserLocation();
                    if (lastKnownCachedLocation != null && lastKnownCachedLocation.getLatitude() != 0.0
                            && lastKnownCachedLocation.getLongitude() != 0.0) {
                        return;
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                }
            }
        }
    }

    @SuppressLint("ParcelCreator")
    private class ReverseGeocodingResultReceiver extends ResultReceiver {
        ReverseGeocodingResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            //remove spinner from address text view

            if (resultCode == FetchAddressIntentService.SUCCESS_RESULT) {

                String geocodedAddress = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);

                if (!TextUtils.isEmpty(geocodedAddress)) {
                    pushedTaskMetaPlace.setName(geocodedAddress);

                    if (handlePushedTaskDeepLink) {
                        updatePushedDestinationAddress();
                    }

                    destinationAddressGeocoded = true;
                    Log.d(TAG, "Reverse Geocoding for Destination Successful");
                }
            }
        }
    }
}