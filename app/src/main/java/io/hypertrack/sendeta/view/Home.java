package io.hypertrack.sendeta.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.google.android.gms.location.LocationSettingsStates;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import io.hypertrack.lib.common.model.HTDriverVehicleType;
import io.hypertrack.lib.common.model.HTPlace;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.common.model.HTTaskDisplay;
import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.MembershipSpinnerAdapter;
import io.hypertrack.sendeta.adapter.PlaceAutocompleteAdapter;
import io.hypertrack.sendeta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.sendeta.model.Destination;
import io.hypertrack.sendeta.model.GCMAddDeviceDTO;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.Task;
import io.hypertrack.sendeta.model.TaskETAResponse;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.service.FetchAddressIntentService;
import io.hypertrack.sendeta.service.FetchLocationIntentService;
import io.hypertrack.sendeta.service.RegistrationIntentService;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.LocationStore;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.TaskManager;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.store.callback.TaskETACallback;
import io.hypertrack.sendeta.store.callback.TaskManagerCallback;
import io.hypertrack.sendeta.store.callback.TaskManagerListener;
import io.hypertrack.sendeta.util.AnimationUtils;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.GpsLocationReceiver;
import io.hypertrack.sendeta.util.KeyboardUtils;
import io.hypertrack.sendeta.util.NetworkChangeReceiver;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.PhoneUtils;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.util.images.RoundedImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends DrawerBaseActivity implements ResultCallback<Status>, LocationListener,
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "Home";
    private static final long LOCATION_UPDATE_INTERVAL_TIME = 5000;
    private static final long INITIAL_LOCATION_UPDATE_INTERVAL_TIME = 500;
    private static final long TIMEOUT_LOCATION_LOADER = 15000;

    private User user;
    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    private Marker currentLocationMarker, destinationLocationMarker;

    private Location defaultLocation = new Location("default");

    private SupportMapFragment mMapFragment;
    private AppBarLayout appBarLayout;
    private TextView destinationText, destinationDescription, mAutocompletePlacesView, infoMessageViewText;
    private LinearLayout enterDestinationLayout, infoMessageView;
    private FrameLayout mAutocompletePlacesLayout;
    public CardView mAutocompleteResultsLayout;
    public RecyclerView mAutocompleteResults;
    private Button sendETAButton;
    private LinearLayout bottomButtonLayout;
    private ImageButton shareButton, navigateButton, favoriteButton;
    private View customMarkerView;
    private RoundedImageView heroMarkerProfileImageView;
    private ProgressBar mAutocompleteLoader;
    private ImageView infoMessageViewIcon;

    private Spinner membershipsSpinner;
    private CardView membershipsSpinnerLayout;
    private List<Membership> membershipsList;

    private PlaceAutocompleteAdapter mAdapter;

    private MetaPlace restoreTaskMetaPlace;

    private ProgressDialog mProgressDialog;
    private boolean enterDestinationLayoutClicked = false, shouldRestoreTask = false, locationPermissionChecked = false,
            taskRestoreFinished = false, animateDelayForRestoredTask = false, locationFrequencyIncreased = true;

    private boolean selectPushDestinationPlace = false, handlePushDestinationDeepLink = false,
            destinationAddressGeocoded = false, mRegistrationBroadcastReceived = false;
    private MetaPlace pushDestinationPlace;
    private int pushDestinationAccountId;
    private Task pushDestinationTask;

    private boolean isReceiverRegistered;

    private float zoomLevel = 1.0f;

    private Integer etaInMinutes = 0;
    private Bitmap userBitmap;
    private Call<ResponseBody> sendGCMToServerCall;

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(MetaPlace place) {

            // Set pushDestination Location Address received from ReverseGeocoding
            if (selectPushDestinationPlace && destinationAddressGeocoded) {
                place = pushDestinationPlace;
                destinationAddressGeocoded = false;
            }

            // Reset Handle pushDestination DeepLink flag
            selectPushDestinationPlace = false;

            // On Click Disable handling/showing any more results
            mAdapter.setSearching(false);

            // Check if selected place is a User Favorite to log Analytics Event
            boolean isFavorite = false;
            user = UserStore.sharedStore.getUser();
            if (user != null) {
                isFavorite = user.isSynced(place);
            }
            AnalyticsStore.getLogger().selectedAddress(mAutocompletePlacesView.getText().length(), isFavorite);

            //Restore Default State for Enter Destination Layout
            onEnterDestinationBackClick(null);

            // Set the Enter Destination Layout to Selected Place
            destinationText.setGravity(Gravity.LEFT);
            destinationText.setText(place.getName());

            if (!TextUtils.isEmpty(place.getAddress())) {
                // Set the selected Place Description as Place Address
                destinationDescription.setText(place.getAddress());
                destinationDescription.setVisibility(View.VISIBLE);
            }

            KeyboardUtils.hideKeyboard(Home.this, mAutocompletePlacesView);
            onSelectPlace(place);
        }

        @Override
        public void OnError() {
            destinationDescription.setVisibility(View.GONE);
            destinationDescription.setText("");
        }
    };

    private void onSelectPlace(final MetaPlace place) {
        if (place == null) {
            return;
        }

        getEtaForDestination(place.getLatLng(), new TaskETACallback() {
            @Override
            public void OnSuccess(TaskETAResponse etaResponse) {
                onETASuccess(etaResponse, place);
            }

            @Override
            public void OnError() {
                if (destinationLocationMarker != null) {
                    destinationLocationMarker.remove();
                    destinationLocationMarker = null;
                }

                showETAError();
            }
        });
    }

    private void getEtaForDestination(LatLng destinationLocation, final TaskETACallback callback) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.getting_eta_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        if (currentLocationMarker == null || currentLocationMarker.getPosition() == null || destinationLocation == null) {
            mProgressDialog.dismiss();
            callback.OnError();
            return;
        }

        TaskManager.getSharedManager(Home.this).getETA(currentLocationMarker.getPosition(), destinationLocation, new TaskETACallback() {
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

    private void onETASuccess(TaskETAResponse response, MetaPlace place) {
        updateViewForETASuccess(new Integer((int) response.getDuration() / 60), place.getLatLng());
        TaskManager.getSharedManager(this).setPlace(place);
    }

    private void updateViewForETASuccess(Integer etaInMinutes, LatLng latLng) {
        showSendETAButton();
        updateDestinationMarker(latLng, etaInMinutes);
        updateMapView();
    }

    private void showSendETAButton() {
        // Set SendETA Button Text
        String sendETAButtonText = getString(R.string.action_send_eta);
        if (handlePushDestinationDeepLink) {

            if (this.user != null) {
                Membership selectedAccount = user.getMembershipForAccountId(pushDestinationAccountId);

                String accountNameText = selectedAccount != null ? selectedAccount.getAccountName() : sendETAButtonText;
                sendETAButton.setText(sendETAButtonText + " to " + accountNameText);
            } else {
                sendETAButton.setText(sendETAButtonText);
            }

        } else {
            sendETAButton.setText(sendETAButtonText);
        }

        sendETAButton.setVisibility(View.VISIBLE);
        bottomButtonLayout.setVisibility(View.VISIBLE);
        membershipsSpinnerLayout.setVisibility(View.VISIBLE);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String constraint = s != null ? s.toString() : "";
            mAdapter.setFilterString(constraint);

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
            if (!isLocationEnabled()) {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                    checkIfLocationIsEnabled();

            } else {
                // Reset Current State when user chooses to edit destination
                TaskManager.getSharedManager(Home.this).clearState();
                OnCompleteTask();

                // Reset the Destination Text View
                destinationText.setGravity(Gravity.CENTER);
                destinationText.setText("");

                // Reset the Destionation Description View
                destinationDescription.setVisibility(View.GONE);
                destinationDescription.setText("");

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

    private void updateAutoCompleteResults() {
        List<MetaPlace> places = null;
        user = UserStore.sharedStore.getUser();
        if (user != null) {
            places = user.getPlaces();
        }

        if (places == null || places.isEmpty()) {
            return;
        }

        mAdapter.refreshFavorites(places);
        mAdapter.notifyDataSetChanged();
    }

    BroadcastReceiver mLocationChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfoMessageView();

            Log.d(TAG, "Location Changed");

            // Initiate FusedLocation Updates on Location Changed
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && isLocationEnabled()) {
                // Remove location updates so that it resets
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, Home.this);

                //change the time of location updates
                createLocationRequest(INITIAL_LOCATION_UPDATE_INTERVAL_TIME);

                //restart location updates with the new interval
                resumeLocationUpdates();

                locationFrequencyIncreased = true;
            }
        }
    };

    BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfoMessageView();
        }
    };

    BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "broadcast received");
            mRegistrationBroadcastReceived = true;
            sendGCMRegistrationToServer();
            registerGCMReceiver(false);
        }
    };

    private void updateInfoMessageView() {
        if (!isLocationEnabled()) {
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

    private AdapterView.OnItemSelectedListener mOnMembershipSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            if (membershipsList != null) {
                // Update the selected Membership in UserStore
                UserStore.sharedStore.updateSelectedMembership(membershipsList.get(position).getAccountId());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UserStore
        UserStore.sharedStore.initializeUser();

        // Start GCM Registration
        startGcmRegistration();

        // Initialize Toolbar without Home Button
        initToolbarWithDrawer(getResources().getString(R.string.app_name));

        // Setup Membership Spinner
        setupMembershipsSpinner();

        // Initialize Maps
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        // Initialize UI Views
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        // Get Default User Location from his CountryCode
        // SKIP: if Location Permission is Granted and Location is Enabled
        if (!isLocationEnabled() || !PermissionUtils.checkForPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            geocodeUserCountryName();
        }

        initGoogleClient();

        createLocationRequest(INITIAL_LOCATION_UPDATE_INTERVAL_TIME);
        setupEnterDestinationView();
        setupAutoCompleteView();
        setupShareButton();
        setupSendETAButton();
        setupNavigateButton();
        setupFavoriteButton();
        setupInfoMessageView();
        initCustomMarkerView();

        // Check & Prompt User if Internet is Not Connected
        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
        }

        // Check if there is any currently running task to be restored
        restoreTaskStateIfNeeded();

        // Handle RECEIVE_ETA_FOR_DESTINATION DeepLink
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.KEY_PUSH_DESTINATION)
                && intent.getBooleanExtra(Constants.KEY_PUSH_DESTINATION, false)) {

            // Check if there is no Active Task currently
            if (!shouldRestoreTask) {
                handlePushDestinationDeepLink = true;
                selectPushDestinationPlace = true;
                handlePushDestinationIntent(intent);
            } else {
                Toast.makeText(Home.this, R.string.notification_deeplink_active_task_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startGcmRegistration() {
        user = UserStore.sharedStore.getUser();
        if (user != null && user.getId() != null && checkPlayServices()) {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void setupMembershipsSpinner() {
        membershipsSpinner = (Spinner) findViewById(R.id.home_membership_spinner);
        membershipsSpinnerLayout = (CardView) findViewById(R.id.home_membership_spinner_layout);

        user = UserStore.sharedStore.getUser();

        if (user != null) {
            membershipsList = user.getAcceptedMemberships();
        }

        if (membershipsList != null && membershipsList.size() > 1) {
            MembershipSpinnerAdapter adapter = new MembershipSpinnerAdapter(this, R.layout.layout_home_spinner,
                    R.layout.layout_home_spinner_dropdown_item, user.getFullName(), membershipsList);
            membershipsSpinner.setAdapter(adapter);
            membershipsSpinner.setOnItemSelectedListener(mOnMembershipSelectedListener);
            membershipsSpinner.setPrompt("Select Profile");

            // Set Previously Selected Membership in Spinner
            setPreviouslySelectedMembership();

            membershipsSpinner.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (membershipsList.size() > 1) {
                        membershipsSpinner.performClick();
                    }
                    return false;
                }
            });
        } else {
            membershipsSpinnerLayout.setVisibility(View.GONE);
        }
    }

    private void setPreviouslySelectedMembership() {
        if (membershipsList == null) {
            user = UserStore.sharedStore.getUser();
            membershipsList = user.getAcceptedMemberships();
        }

        Integer selectedMembershipAccId = UserStore.sharedStore.getSelectedMembershipAccountId();
        if (selectedMembershipAccId != null && user.isAcceptedMembership(selectedMembershipAccId)) {

            // Set Default selection to the last selected Membership
            Membership selectedMembership = user.getMembershipForAccountId(selectedMembershipAccId);
            if (selectedMembership != null && membershipsList.contains(selectedMembership)) {
                membershipsSpinner.setSelection(membershipsList.indexOf(selectedMembership));
            }
        }
    }

    private void initGoogleClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, 0 /* clientId */, this)
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
                .setInterval(locationUpdateIntervalTime)
                .setFastestInterval(locationUpdateIntervalTime);
    }

    private void setupEnterDestinationView() {
        // Initialize Enter Destination UI Views
        destinationText = (TextView) findViewById(R.id.destination_text);
        destinationDescription = (TextView) findViewById(R.id.destination_desc);
        enterDestinationLayout = (LinearLayout) findViewById(R.id.enter_destination_layout);

        // Set Click Listener for Enter Destination Layout
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

                // TaskShared Flag is always false because couldn't find a way of knowing
                // whether the user successfully shared the task details or not
                AnalyticsStore.getLogger().tappedShareIcon(false);
            }
        });
    }

    private void setupSendETAButton() {
        // Initialize SendETA Button UI View
        sendETAButton = (Button) findViewById(R.id.sendETAButton);
        bottomButtonLayout = (LinearLayout) findViewById(R.id.home_bottomButtonLayout);

        // Set Click Listener for SendETA Button
        sendETAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if Location Permission has been granted & Location has been enabled
                if (PermissionUtils.checkForPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        && isLocationEnabled()) {
                    if (!TaskManager.getSharedManager(Home.this).isTaskActive()) {

                        // Check if the selectedAccountId is different from pushDestinationAccountId
                        if (handlePushDestinationDeepLink && pushDestinationAccountId != 0) {
                            User user = UserStore.sharedStore.getUser();
                            if (user != null && pushDestinationAccountId != user.getSelectedMembershipAccountId()) {

                                Toast.makeText(Home.this, R.string.push_destination_incorrect_account_id,
                                        Toast.LENGTH_SHORT).show();

                                AnalyticsStore.getLogger().startedTrip(false, ErrorMessages.START_TRIP_ERROR_INVALID_ACCOUNT_ID);
                                return;
                            }
                        }

                        // Start the Task
                        startTask();
                    } else {

                        // Complete the Task
                        completeTask();
                    }
                } else {
                    checkForLocationPermission();
                }
            }
        });
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
        favoriteButton.setVisibility(View.GONE);
    }

    private void setupInfoMessageView() {
        infoMessageView = (LinearLayout) findViewById(R.id.home_info_message_view);
        infoMessageViewIcon = (ImageView) findViewById(R.id.home_info_message_icon);
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
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (userBitmap == null) {
                user = UserStore.sharedStore.getUser();
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

            destinationText.setGravity(Gravity.LEFT);
            destinationText.setText(restoreTaskMetaPlace.getName());

            if (!TextUtils.isEmpty(restoreTaskMetaPlace.getAddress())) {
                destinationDescription.setText(restoreTaskMetaPlace.getAddress());
                destinationDescription.setVisibility(View.VISIBLE);
            }

            // Start the Task
            updateViewForETASuccess(etaInMinutes != 0 ? etaInMinutes : null, restoreTaskMetaPlace.getLatLng());
            onTaskStart();

            shouldRestoreTask = true;

        } else {
            HTLog.e(TAG, "No Task to restore.");
            shouldRestoreTask = false;
        }
    }

    private void handlePushDestinationIntent(Intent intent) {
        String accountIdData = intent.getStringExtra(Constants.KEY_ACCOUNT_ID);
        pushDestinationAccountId = accountIdData != null ? Integer.valueOf(accountIdData) : 0;

        UserStore.sharedStore.updateSelectedMembership(pushDestinationAccountId);
        setPreviouslySelectedMembership();

        // Fetch Task from Intent Params, if available
        String taskData = intent.getStringExtra(Constants.KEY_TASK);
        if (!TextUtils.isEmpty(taskData)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Task>() {
            }.getType();
            pushDestinationTask = gson.fromJson(taskData, type);
        }

        if (pushDestinationTask != null) {
            Destination destination = pushDestinationTask.getDestination();

            // Check if a valid destination & a valid Location has been provided
            if (destination != null && destination.getLocation() != null) {

                // Check if valid coordinates have been provided in the Location
                double[] destinationCoordinates = destination.getLocation().getCoordinates();
                if (destinationCoordinates[0] != 0.0 && destinationCoordinates[1] != 0.0) {

                    // Check if valid Destination Address was provided or not
                    if (TextUtils.isEmpty(destination.getAddress())) {
                        StringBuilder destinationName = new StringBuilder(destinationCoordinates[1] + ", " +
                                destinationCoordinates[0]);

                        pushDestinationPlace = new MetaPlace(destinationName.toString(),
                                destinationCoordinates[1], destinationCoordinates[0]);

                        // Reverse Geocode the Destination Location Coordinates to an Address
                        reverseGeocode(new LatLng(destinationCoordinates[1], destinationCoordinates[0]));
                    } else {

                        pushDestinationPlace = new MetaPlace(destination.getAddress(), destinationCoordinates[1],
                                destinationCoordinates[0]);
                    }
                    return;
                }
            }
        }

        // Check if the DeepLink had Lat & Lng, handle it appropriately
        Double pushDestinationLat = intent.getDoubleExtra(Constants.KEY_PUSH_DESTINATION_LAT, 0.0);
        Double pushDestinationLng = intent.getDoubleExtra(Constants.KEY_PUSH_DESTINATION_LNG, 0.0);

        if (pushDestinationLat != 0.0 && pushDestinationLng != 0.0) {

            LatLng destinationLatLng = new LatLng(pushDestinationLat, pushDestinationLng);
            StringBuilder destinationName = new StringBuilder(destinationLatLng.latitude + ", " +
                    destinationLatLng.longitude);

            pushDestinationPlace = new MetaPlace(destinationName.toString(), destinationLatLng);

            // Reverse Geocode LatLng to an Address
            reverseGeocode(destinationLatLng);

            // ETA for given Destination Location will be fetched when Location Permission is granted,
            // Location Settings are Enabled and currentLocation is available
            return;
        }

        handlePushDestinationDeepLink = false;
        selectPushDestinationPlace = false;
    }

    private void reverseGeocode(LatLng latLng) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, new ReverseGeocodingResultReceiver(new Handler()));
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, latLng);
        startService(intent);
    }

    public void onEnterDestinationBackClick(View view) {
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

        updateMapPadding(false);

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

            updateMapView();

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

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Check if Task has to be Restored & Update Map for that task
                if (shouldRestoreTask && restoreTaskMetaPlace != null) {
                    taskRestoreFinished = true;

                    if (TaskManager.getSharedManager(Home.this).getHyperTrackTask() == null) {
                        return;
                    }

                    updateViewForETASuccess(etaInMinutes != 0 ? etaInMinutes : null, restoreTaskMetaPlace.getLatLng());
                    onTaskStart();
                }
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
    private void startTask() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.starting_task_message));
        mProgressDialog.show();

        user = UserStore.sharedStore.getUser();
        if (user == null) {
            mProgressDialog.dismiss();
            return;
        }

        if (currentLocationMarker == null || currentLocationMarker.getPosition() == null) {
            Toast.makeText(Home.this, R.string.invalid_current_location, Toast.LENGTH_SHORT).show();
            return;
        }

        if (handlePushDestinationDeepLink) {
            if (destinationAddressGeocoded) {
                updateDestinationLocationAddress();
            }
        }

        LatLng currentLocation = currentLocationMarker.getPosition();

        // Call startTask to start the task
        TaskManager.getSharedManager(this).startTask(pushDestinationTask, currentLocation,
                this.user.getSelectedMembershipAccountId(), HTDriverVehicleType.CAR, new TaskManagerCallback() {
                    @Override
                    public void OnSuccess() {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }

                        // Don't show ShareCard by default for Business Account Tasks
                        if (!handlePushDestinationDeepLink)
                            share();

                        onTaskStart();

                        // Reset handle pushDestination DeepLink Flag
                        handlePushDestinationDeepLink = false;

                        AnalyticsStore.getLogger().startedTrip(true, null);
                        HTLog.i(TAG, "Task started successfully.");
                    }

                    @Override
                    public void OnError() {
                        if (mProgressDialog != null) {
                            mProgressDialog.dismiss();
                        }

                        showStartTaskError();
                        HTLog.e(TAG, "Task start failed.");

                        // Reset handle pushDestination DeepLink Flag
                        handlePushDestinationDeepLink = false;

                        AnalyticsStore.getLogger().startedTrip(false, ErrorMessages.START_TRIP_FAILED);
                    }
                });
    }

    private void updateDestinationLocationAddress() {
        final MetaPlace destinationPlace = pushDestinationPlace;

        // Update the selected place with updated destinationLocationAddress
        TaskManager.getSharedManager(this).setPlace(destinationPlace);

        // Set the Enter Destination Layout to Selected Place
        destinationText.setText(destinationPlace.getName());

        if (!TextUtils.isEmpty(destinationPlace.getAddress())) {
            // Set the selected Place Description as Place Address
            destinationDescription.setText(destinationPlace.getAddress());
            destinationDescription.setVisibility(View.VISIBLE);
        }

        destinationAddressGeocoded = false;
    }

    private void showStartTaskError() {
        Toast.makeText(this, ErrorMessages.START_TRIP_FAILED, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to Initiate COMPLETE TASK
     */
    private void completeTask() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.completing_task_message));
        mProgressDialog.show();

        TaskManager.getSharedManager(this).completeTask(new TaskManagerCallback() {
            @Override
            public void OnSuccess() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                OnCompleteTask();

                AnalyticsStore.getLogger().tappedEndTrip(true, null);
                HTLog.i(TAG, "Complete Task (CTA) happened successfully.");
            }

            @Override
            public void OnError() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                showCompleteTaskError();

                AnalyticsStore.getLogger().tappedEndTrip(false, ErrorMessages.END_TRIP_FAILED);
                HTLog.e(TAG, "Complete Task (CTA) failed.");
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
    private void onTaskStart() {
        sendETAButton.setText(getString(R.string.action_end_trip));
        membershipsSpinnerLayout.setVisibility(View.GONE);

        shareButton.setVisibility(View.VISIBLE);
        navigateButton.setVisibility(View.VISIBLE);
        enterDestinationLayout.setOnClickListener(null);
        favoriteButton.setVisibility(View.VISIBLE);
        updateFavoritesButton();

        TaskManager taskManager = TaskManager.getSharedManager(Home.this);
        taskManager.setTaskRefreshedListener(new TaskManagerListener() {
            @Override
            public void OnCallback() {
                // Get TaskManager Instance
                TaskManager taskManager = TaskManager.getSharedManager(Home.this);
                if (taskManager == null)
                    return;

                // Fetch updated HypertrackTask Instance
                HTTask task = taskManager.getHyperTrackTask();
                if (task == null) {
                    return;
                }

                // Update ETA & Display Statuses using Task's Display field
                updateETAForOnGoingTask(task, taskManager.getPlace());
                updateDisplayStatusForOngoingTask(task);

                // Check if DestinationLocation has been updated
                // NOTE: Call this method after updateETAForOnGoingTask()
                checkForUpdatedDestinationLocation(task);
            }
        });
        taskManager.setTaskCompletedListener(new TaskManagerListener() {
            @Override
            public void OnCallback() {
                OnCompleteTask();
            }
        });

        updateMapPadding(true);
        updateMapView();
    }

    private void checkForUpdatedDestinationLocation(HTTask task) {
        // Check if updatedDestination Location is not null
        HTPlace destinationLocation = task.getDestination();
        if (destinationLocation == null || destinationLocation.getLocation() == null)
            return;

        // Check if updatedDestination Location Coordinates are valid
        LatLng updatedDestinationLatLng = new LatLng(destinationLocation.getLocation().getLatitude(),
                destinationLocation.getLocation().getLongitude());
        if (updatedDestinationLatLng.latitude == 0.0 || updatedDestinationLatLng.longitude == 0.0)
            return;

        // Check if destinationMarker's location has been changed
        if (destinationLocationMarker != null && destinationLocationMarker.getPosition() != null) {
            if (!destinationLocationMarker.getPosition().equals(updatedDestinationLatLng)) {

                // TODO: 18/08/16 Check how to update changed MetaPlace Id
                MetaPlace place = new MetaPlace(destinationLocation.getAddress(), updatedDestinationLatLng);
                TaskManager.getSharedManager(Home.this).setPlace(place);

                // Set the Enter Destination Layout to Selected Place
                destinationText.setGravity(Gravity.LEFT);
                destinationText.setText(place.getName());

                // Hide the favorites button (because this place doesnt have a valid PlaceId)
                favoriteButton.setVisibility(View.GONE);

                updateDestinationMarker(updatedDestinationLatLng, etaInMinutes);
                updateMapView();
            }
            return;
        }

        // Add Destination Marker with fetched DestinationLatLng & ETA value
        updateDestinationMarker(updatedDestinationLatLng, etaInMinutes);
    }

    private void updateETAForOnGoingTask(HTTask task, MetaPlace place) {
        if (place == null) {
            return;
        }

        LatLng destinationLocation = new LatLng(place.getLatitude(), place.getLongitude());

        // Get ETA Value to display from TaskDisplay field
        etaInMinutes = getTaskDisplayETA(task.getTaskDisplay());
        updateDestinationMarker(destinationLocation, etaInMinutes);
    }

    private Integer getTaskDisplayETA(HTTaskDisplay taskDisplay) {
        if (taskDisplay != null && !TextUtils.isEmpty(taskDisplay.getDurationRemaining())) {
            Double etaInSeconds = Double.valueOf(taskDisplay.getDurationRemaining());

            if (etaInSeconds != null) {
                double etaInMinutes = Math.ceil((etaInSeconds / (float) 60));
                if (etaInMinutes < 1) {
                    etaInMinutes = 1;
                }

                return (int) etaInMinutes;
            }
        }

        return null;
    }

    private void updateDisplayStatusForOngoingTask(HTTask task) {
        HTTaskDisplay taskDisplay = task.getTaskDisplay();

        // Set Toolbar Title as DisplayStatus for currently active task
        Integer taskDisplayStatusResId = getTaskDisplayStatus(taskDisplay);
        if (taskDisplayStatusResId != null) {
            this.setTitle(getString(taskDisplayStatusResId));
        } else {
            // Set Toolbar Title as AppName
            this.setTitle(getResources().getString(R.string.app_name));
        }

        // Set Toolbar SubTitle as DisplaySubStatus for currently active task
        String taskDisplaySubStatus = getTaskDisplaySubStatus(taskDisplay);
        if (!TextUtils.isEmpty(taskDisplaySubStatus)) {
            this.setSubTitle(taskDisplaySubStatus);
        } else {
            this.setSubTitle("");
        }
    }

    public Integer getTaskDisplayStatus(HTTaskDisplay taskDisplay) {
        if (taskDisplay == null || TextUtils.isEmpty(taskDisplay.getStatus()))
            return null;

        String taskDisplayStatus = taskDisplay.getStatus();

        switch (taskDisplayStatus) {
            case HTTask.TASK_STATUS_NOT_STARTED:
                return io.hypertrack.lib.consumer.R.string.task_status_not_started;
            case HTTask.TASK_STATUS_DISPATCHING:
                return io.hypertrack.lib.consumer.R.string.task_status_dispatching;
            case HTTask.TASK_STATUS_DRIVER_ON_THE_WAY:
                return io.hypertrack.lib.consumer.R.string.task_status_driver_on_the_way;
            case HTTask.TASK_STATUS_DRIVER_ARRIVING:
                return io.hypertrack.lib.consumer.R.string.task_status_driver_arriving;
            case HTTask.TASK_STATUS_DRIVER_ARRIVED:
                return io.hypertrack.lib.consumer.R.string.task_status_driver_arrived;
            case HTTask.TASK_STATUS_COMPLETED:
                return io.hypertrack.lib.consumer.R.string.task_status_completed;
            case HTTask.TASK_STATUS_CANCELED:
                return io.hypertrack.lib.consumer.R.string.task_status_canceled;
            case HTTask.TASK_STATUS_ABORTED:
                return io.hypertrack.lib.consumer.R.string.task_status_aborted;
            case HTTask.TASK_STATUS_SUSPENDED:
                return io.hypertrack.lib.consumer.R.string.task_status_suspended;
            case HTTask.TASK_STATUS_NO_LOCATION:
                return io.hypertrack.lib.consumer.R.string.task_status_no_location;
            case HTTask.TASK_STATUS_CONNECTION_LOST:
                return io.hypertrack.lib.consumer.R.string.task_status_connection_lost;
            default:
                return null;
        }
    }

    private String getTaskDisplaySubStatus(HTTaskDisplay taskDisplay) {

        if (taskDisplay != null && taskDisplay.getSubStatusDuration() != null) {

            Double timeInSeconds = Double.valueOf(taskDisplay.getSubStatusDuration());
            String formattedTime = this.getFormattedTimeString(timeInSeconds);
            if (TextUtils.isEmpty(formattedTime))
                return null;

            StringBuilder builder = (new StringBuilder(formattedTime));
            if ("delayed".equalsIgnoreCase(taskDisplay.getSubStatus())) {
                builder.append(this.getString(io.hypertrack.lib.consumer.R.string.task_sub_status_delayed_suffix_text));
            } else {
                builder.append(this.getString(io.hypertrack.lib.consumer.R.string.task_sub_status_default_suffix_text));
            }

            return builder.toString();
        }

        return null;
    }

    private String getFormattedTimeString(Double timeInSeconds) {
        if (this == null || this.isFinishing() || timeInSeconds == null || timeInSeconds < 0)
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
                    .append(this.getResources().getQuantityString(io.hypertrack.lib.consumer.R.plurals.hour_text, hours))
                    .append(" ");
        }

        if (mins > 0) {
            builder.append(mins)
                    .append(" ")
                    .append(this.getResources().getQuantityString(
                            io.hypertrack.lib.consumer.R.plurals.minute_text, mins))
                    .append(" ");
        }

        return builder.toString();
    }

    /**
     * Method to update State Variables & UI to reflect Task Ended
     */
    private void OnCompleteTask() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        sendETAButton.setVisibility(View.GONE);
        membershipsSpinnerLayout.setVisibility(View.GONE);
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

        if (destinationLocationMarker != null) {
            destinationLocationMarker.remove();
            destinationLocationMarker = null;
        }

        enterDestinationLayout.setOnClickListener(enterDestinationClickListener);
        updateMapPadding(false);

        // Reset Toolbar Title on EndTrip
        this.setTitle(getResources().getString(R.string.app_name));
        this.setSubTitle("");
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

        Bitmap bitmap = getBitMapForView(this, markerView);
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
                isLocationEnabled()) {
            requestLocationUpdates();
        }
    }

    private boolean isLocationEnabled() {
        try {
            ContentResolver contentResolver = this.getContentResolver();
            // Find out what the settings say about which providers are enabled
            int mode = Settings.Secure.getInt(
                    contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            if (mode == Settings.Secure.LOCATION_MODE_OFF) {
                // Location is turned OFF!
                return false;
            } else {
                // Location is turned ON!
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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
        startLocationPolling();
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
            public void onResult(LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
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

                            // Handle pushDestination DeepLink
                        } else if (selectPushDestinationPlace) {
                            mPlaceAutoCompleteListener.OnSuccess(pushDestinationPlace);
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

                        // Reset handle pushDestination DeepLink Flag if Location change was unavailable
                        if (selectPushDestinationPlace)
                            selectPushDestinationPlace = false;

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

        if (mMap != null) {
            if (currentLocationMarker == null) {
                addMarkerToCurrentLocation(latLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
            } else {
                currentLocationMarker.setPosition(latLng);
            }

            updateMapView();
        }

        mAdapter.setBounds(getBounds(latLng, 10000));

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
            createLocationRequest(LOCATION_UPDATE_INTERVAL_TIME);

            // Restart location updates with the new interval
            startLocationPolling();
        }

        LocationStore.sharedStore().setCurrentLocation(location);
    }

    private void addMarkerToCurrentLocation(LatLng latLng) {
        Bitmap bitmap = getBitMapForView(this, customMarkerView);
        if (bitmap != null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            bitmap.recycle();
        }

        // Handle pushDestination DeepLink
        if (selectPushDestinationPlace && isLocationEnabled()) {
            mPlaceAutoCompleteListener.OnSuccess(pushDestinationPlace);
        }
    }

    private Bitmap getBitMapForView(Context context, View view) {
        Bitmap bitmap = null;
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();

            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
            view.buildDrawingCache();
            bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return bitmap;
    }

    private LatLngBounds getBounds(LatLng latLng, int mDistanceInMeters) {
        double latRadian = Math.toRadians(latLng.latitude);

        double degLatKm = 110.574235;
        double degLongKm = 110.572833 * Math.cos(latRadian);
        double deltaLat = mDistanceInMeters / 1000.0 / degLatKm;
        double deltaLong = mDistanceInMeters / 1000.0 / degLongKm;

        double minLat = latLng.latitude - deltaLat;
        double minLong = latLng.longitude - deltaLong;
        double maxLat = latLng.latitude + deltaLat;
        double maxLong = latLng.longitude + deltaLong;

        LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(new LatLng(minLat, minLong));
        b.include(new LatLng(maxLat, maxLong));
        LatLngBounds bounds = b.build();

        return bounds;
    }

    private void updateMapView() {
        if (mMap == null) {
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

        CameraUpdate cameraUpdate;
        if (destinationLocationMarker != null && currentLocationMarker != null) {
            cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        } else {
            LatLng latLng = currentLocationMarker != null ? currentLocationMarker.getPosition() : destinationLocationMarker.getPosition();
            cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        }

        if (taskRestoreFinished && !animateDelayForRestoredTask) {
            mMap.animateCamera(cameraUpdate, 2000, null);
            animateDelayForRestoredTask = true;
        } else {
            mMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onResult(Status status) {
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

        MetaPlace place = taskManager.getPlace();
        if (place == null) {
            return;
        }

        Double latitude = place.getLatitude();
        Double longitude = place.getLongitude();
        if (latitude == null || longitude == null) {
            return;
        }

        String mode = "d";
        HTTask task = taskManager.getHyperTrackTask();
        if (task != null && task.getVehicleType() != null) {
            HTDriverVehicleType type = task.getVehicleType();
            switch (type) {
                case BICYCLE:
                    mode = "b";
                    break;
                case WALK:
                    mode = "w";
                    break;
                default:
                    mode = "d";
                    break;
            }
        }

        String navigationString = latitude.toString() + "," + longitude.toString() + "&mode=" + mode;
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + navigationString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        startActivity(mapIntent);
    }

    /**
     * Method to add current selected destination as a Favorite
     * (NOTE: Only Applicable for Live Task)
     *
     * @param view
     */
    public void OnFavoriteClick(View view) {
        TaskManager taskManager = TaskManager.getSharedManager(Home.this);
        MetaPlace place = taskManager.getPlace();

        if (place == null) {
            return;
        }

        showAddPlace(place);

        AnalyticsStore.getLogger().tappedFavorite();
    }

    private void showAddPlace(MetaPlace place) {
        MetaPlace newPlace = new MetaPlace(place);
        newPlace.setName(null);

        Intent addPlace = new Intent(this, AddFavoritePlace.class);
        addPlace.putExtra("meta_place", newPlace);
        startActivityForResult(addPlace, Constants.FAVORITE_PLACE_REQUEST_CODE, null);
    }

    /**
     * Method to add Click Listener to Favorite Icon
     * (depends if current task is a Live Task & selected Place is not already a favorite)
     */
    private void updateFavoritesButton() {
        TaskManager taskManager = TaskManager.getSharedManager(Home.this);
        if (taskManager.isTaskActive()) {
            MetaPlace place = taskManager.getPlace();
            if (place == null) {
                return;
            }

            user = UserStore.sharedStore.getUser();
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

    @SuppressLint("ParcelCreator")
    private class GeocodingResultReceiver extends ResultReceiver {
        public GeocodingResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchLocationIntentService.SUCCESS_RESULT) {
                LatLng latLng = resultData.getParcelable(FetchLocationIntentService.RESULT_DATA_KEY);
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
        public ReverseGeocodingResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            //remove spinner from address text view

            if (resultCode == FetchAddressIntentService.SUCCESS_RESULT) {

                String geocodedAddress = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);

                if (!TextUtils.isEmpty(geocodedAddress)) {
                    pushDestinationPlace.setName(geocodedAddress);

                    if (handlePushDestinationDeepLink) {
                        updateDestinationLocationAddress();
                    }

                    destinationAddressGeocoded = true;
                    Log.d(TAG, "Reverse Geocoding for Destination Successful");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                        checkIfLocationIsEnabled();

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                            getString(R.string.location_permission_never_allow));

                    // Reset EnterDestinationLayoutClicked Flag if Location permission was denied
                    if (enterDestinationLayoutClicked)
                        enterDestinationLayoutClicked = false;

                    // Reset handle pushDestination DeepLink Flag if Location Permission was denied
                    if (selectPushDestinationPlace)
                        selectPushDestinationPlace = false;
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

                        // Handle pushDestination DeepLink
                    } else if (selectPushDestinationPlace) {
                        mPlaceAutoCompleteListener.OnSuccess(pushDestinationPlace);
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

            updateFavoritesButton();

        } else if (requestCode == Constants.SHARE_REQUEST_CODE) {

            Log.d(TAG, resultCode + "");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister BroadcastReceiver for Location_Change & Network_Change
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectivityChangeReceiver);

        registerGCMReceiver(false);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        TaskManager.getSharedManager(Home.this).stopRefreshingTask();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start Refreshing Task, if one exists
        if (TaskManager.getSharedManager(Home.this).getHyperTrackTask() != null) {
            TaskManager.getSharedManager(Home.this).startRefreshingTask(0);
        }

        // Check if Location & Network are Enabled
        updateInfoMessageView();

        // Resume FusedLocation Updates
        resumeLocationUpdates();

        updateFavoritesButton();
        updateCurrentLocationMarker();

        // Re-register BroadcastReceiver for Location_Change, Network_Change & GCM
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationChangeReceiver,
                new IntentFilter(GpsLocationReceiver.LOCATION_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectivityChangeReceiver,
                new IntentFilter(NetworkChangeReceiver.NETWORK_CHANGED));

        if (!mRegistrationBroadcastReceived)
            registerGCMReceiver(true);

        // Setup Membership Spinner
        setupMembershipsSpinner();

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

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     */
    private void sendGCMRegistrationToServer() {
        final String token = SharedPreferenceManager.getGCMToken();

        try {
            User user = UserStore.sharedStore.getUser();

            if (user != null && token.length() > 0) {
                GCMAddDeviceDTO gcmAddDeviceDTO = new GCMAddDeviceDTO(token);

                SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class,
                        SharedPreferenceManager.getUserAuthToken());

                sendGCMToServerCall = sendETAService.addGCMToken(user.getId(), gcmAddDeviceDTO);
                sendGCMToServerCall.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            HTLog.d(TAG, "Registration Key pushed to server successfully");
                        } else {
                            HTLog.e(TAG, "Registration Key push to server failed: " + response.raw().networkResponse().code()
                                    + ", " + response.raw().networkResponse().message() + ", "
                                    + response.raw().networkResponse().request().url());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        HTLog.e(TAG, "Registration Key push to server failed: " + t.getMessage());
                    }
                });
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Registration Key push to server failed: " + e.getMessage());
        }
    }

    private void registerGCMReceiver(boolean register) {
        if (register) {
            if (!isReceiverRegistered) {
                LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                        new IntentFilter(RegistrationIntentService.REGISTRATION_COMPLETE));
                isReceiverRegistered = true;
            }
        } else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);

        if (membershipsList != null)
            setPreviouslySelectedMembership();
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
            if (isLocationEnabled()) {
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
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

            if (sendGCMToServerCall != null)
                sendGCMToServerCall.cancel();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}