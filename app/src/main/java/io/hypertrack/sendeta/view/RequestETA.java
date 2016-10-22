package io.hypertrack.sendeta.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.messenger.ShareToMessengerParams;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.text.CharacterIterator;
import java.util.List;

import io.hypertrack.lib.common.model.HTLocation;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.PlaceAutocompleteAdapter;
import io.hypertrack.sendeta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.sendeta.model.CreateDestinationDTO;
import io.hypertrack.sendeta.model.CreateTaskDTO;
import io.hypertrack.sendeta.model.Destination;
import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import io.hypertrack.sendeta.service.FetchAddressIntentService;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.LocationStore;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.KeyboardUtils;
import io.hypertrack.sendeta.util.LocationUtils;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.messenger.MessengerUtils.finishShareToMessenger;
import static com.facebook.messenger.MessengerUtils.shareToMessenger;

/**
 * Created by piyush on 21/10/16.
 */
public class RequestETA extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, TouchableWrapper.TouchActionDown, TouchableWrapper.TouchActionUp {

    private final String TAG = RequestETA.class.getSimpleName();
    private float zoomLevel = 1.0f;
    private static final long INITIAL_LOCATION_UPDATE_INTERVAL_TIME = 500;
    private Location defaultLocation = new Location("default");

    private User user;
    private boolean mPicking = false;
    private CallbackManager callbackManager;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private GoogleMap mMap;
    private LatLng currentLatLng;

    private Button retryButton, requestETAButton;
    private TextView destinationText, destinationDescription;
    private LinearLayout enterDestinationLayout, bottomButtonLayout;

    private AutoCompleteTextView mAutocompletePlacesView;
    private FrameLayout mAutocompletePlacesLayout;
    private RecyclerView mAutocompleteResults;
    private CardView mAutocompleteResultsLayout;
    private ProgressBar mAutocompleteLoader;
    private PlaceAutocompleteAdapter mAdapter;

    private Call<HTTask> createTaskCall;

    private boolean myLocationButtonClicked = false;

    private View.OnClickListener enterDestinationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // Reset Retry button
            retryButton.setVisibility(View.GONE);
            retryButton.setOnClickListener(null);

            // Reset the Destination Text View
            destinationText.setGravity(Gravity.CENTER);
            destinationText.setText("");

            // Reset the Destionation Description View
            destinationDescription.setVisibility(View.GONE);
            destinationDescription.setText("");

            // Reset the Autocomplete TextView
            mAutocompletePlacesView.setText("");
            mAutocompletePlacesView.requestFocus();
            KeyboardUtils.showKeyboard(RequestETA.this, mAutocompletePlacesView);

            // Show the Autocomplete Places Layout
            enterDestinationLayout.setVisibility(View.GONE);
            mAutocompletePlacesLayout.setVisibility(View.VISIBLE);

            showAutocompleteResults(true);
            updateAutoCompleteResults();
        }
    };

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(MetaPlace place) {
            // On Click Disable handling/showing any more results
            mAdapter.setSearching(false);

            // Check if selected place is a User Favorite to log Analytics Event
            boolean isFavorite = false;
            user = UserStore.sharedStore.getUser();
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

            KeyboardUtils.hideKeyboard(RequestETA.this, mAutocompletePlacesView);

            if (mMap != null && place != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18.0f));
            }
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
            String constraint = s != null ? s.toString() : "";
            mAdapter.setFilterString(constraint);

            // Show Autocomplete Data Fetch Loader when user typed something
            if (constraint.length() > 0)
                mAutocompleteLoader.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize UserStore
        UserStore.sharedStore.initializeUser();

        // Initialize FB SDK
        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_request_eta);

        initToolbar(getString(R.string.title_activity_request_eta));

        // Initialize Maps
        getMap();

        // Initialize UI Views
        retryButton = (Button) findViewById(R.id.retryButton);

        initGoogleClient();
        createLocationRequest(INITIAL_LOCATION_UPDATE_INTERVAL_TIME);
        setupEnterDestinationView();
        setupAutoCompleteView();
        setuprequestETAButton();

        // Check & Prompt User if Internet is Not Connected
        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
        }

        // TODO: 22/10/16 Handle REQUEST_ETA Deeplink
    }

    private void getMap() {
        // Initialize Maps
        TouchableSupportMapFragment mMapFragment = (TouchableSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        View locationButton = ((View) mMapFragment.getView().findViewById(1).getParent()).findViewById(2);
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        int bottom = getResources().getDimensionPixelSize(R.dimen.map_active_task_bottom_padding);
        rlp.setMargins(0, 0, 0, bottom);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        currentLatLng = LocationStore.sharedStore().getCurrentLatLng();
        if (currentLatLng == null) {
            // Set Default View for map according to User's LastKnownLocation
            Location lastKnownCachedLocation = LocationStore.sharedStore().getLastKnownUserLocation();
            currentLatLng = new LatLng(lastKnownCachedLocation.getLatitude(),
                    lastKnownCachedLocation.getLongitude());
        }

        if (currentLatLng.latitude != 0.0 && currentLatLng.longitude != 0.0) {
            reverseGeocode(currentLatLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18.0f));

        } else {
            // Else Set Default View for map according to either User's Default Location
            // (If Country Info was available) or (0.0, 0.0)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(defaultLocation.getLatitude(), defaultLocation.getLongitude()), zoomLevel));
        }

        int bottom = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
        mMap.setPadding(0, 0, 0, bottom);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                checkForLocationPermission();
                myLocationButtonClicked = true;
                return false;
            }
        });

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (myLocationButtonClicked) {
                    onTouchUp(null);
                    myLocationButtonClicked = false;
                }
            }
        });
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(RequestETA.this);
        layoutManager.setAutoMeasureEnabled(true);
        mAutocompleteResults.setLayoutManager(layoutManager);

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, mPlaceAutoCompleteListener);
        mAutocompleteResults.setAdapter(mAdapter);
    }

    private void setuprequestETAButton() {
        // Initialize RequestETA Button UI View
        requestETAButton = (Button) findViewById(R.id.requestETAButton);
        bottomButtonLayout = (LinearLayout) findViewById(R.id.home_bottomButtonLayout);
    }

    public void onRequestETAClick(View view) {
        displayLoader(true);

        String address = destinationText.getText().toString() + destinationDescription.getText().toString();

        CreateDestinationDTO destination = null;
        if (currentLatLng != null && currentLatLng.longitude != 0.0 && currentLatLng.latitude != 0.0)
            destination = new CreateDestinationDTO(address,
                    new HTLocation(currentLatLng.latitude, currentLatLng.longitude));

        HyperTrackService hyperTrackService = HyperTrackServiceGenerator.createService(
                HyperTrackService.class, BuildConfig.HYPERTRACK_API_KEY);

        createTaskCall = hyperTrackService.createTask(new CreateTaskDTO(destination));
        createTaskCall.enqueue(new Callback<HTTask>() {
            @Override
            public void onResponse(Call<HTTask> call, Response<HTTask> response) {
                if (response != null && response.isSuccessful()) {

                    HTTask task = response.body();
                    if (task != null && !TextUtils.isEmpty(task.getId())) {

                        String requestETAUrl = "www.sendeta.com/request/?uuid=" + task.getId() + "*lat=" +
                                task.getDestination().getLocation().getLatitude() + "*lng=" + task.getDestination().getLocation().getLongitude();

                        String shareMessage = "Hey there! Click on the link to send me your ETA. " + requestETAUrl;

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
                        startActivityForResult(Intent.createChooser(sharingIntent, "Share via"), Constants.SHARE_REQUEST_CODE);
                    }
                }

                displayLoader(false);
            }

            @Override
            public void onFailure(Call<HTTask> call, Throwable t) {
                displayLoader(false);

                ErrorData errorData = new ErrorData();
                try {
                    errorData = NetworkUtils.processFailure(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showErrorMessage(errorData);
            }

            private void showErrorMessage(ErrorData errorData) {
                if (RequestETA.this.isFinishing())
                    return;

                if (ErrorCodes.NO_INTERNET.equalsIgnoreCase(errorData.getCode()) ||
                        ErrorCodes.REQUEST_TIMED_OUT.equalsIgnoreCase(errorData.getCode())) {
                    Toast.makeText(RequestETA.this, R.string.network_issue, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(RequestETA.this, R.string.generic_error_message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onEnterDestinationBackClick(View view) {
        // Hide the Autocomplete Results Layout
        showAutocompleteResults(false);

        // Reset the Enter Destination Layout
        enterDestinationLayout.setVisibility(View.VISIBLE);
        mAutocompletePlacesLayout.setVisibility(View.GONE);

        KeyboardUtils.hideKeyboard(RequestETA.this, mAutocompletePlacesView);
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

    private void sendToMessenger() {
        ShareToMessengerParams shareToMessengerParams =
                ShareToMessengerParams.newBuilder(null, "image/jpeg")
                        .setMetaData("{ \"image\" : \"trees\" }")
                        .build();

        if (mPicking) {
            finishShareToMessenger(this, shareToMessengerParams);
        } else {
            shareToMessenger(
                    this,
                    Constants.SHARE_TO_MESSENGER_REQUEST_CODE,
                    shareToMessengerParams);
        }
    }

    @Override
    public void onTouchDown(MotionEvent event) {
    }

    @Override
    public void onTouchUp(MotionEvent event) {
        if (mMap != null) {
            mAdapter.setBounds(LocationUtils.getBounds(mMap.getCameraPosition().target, LocationUtils.DISTANCE_IN_METERS));
            reverseGeocode(mMap.getCameraPosition().target);

            currentLatLng = mMap.getCameraPosition().target;
            // set spinner for address text view
        }
    }

    private void reverseGeocode(LatLng latLng) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, new AddressResultReceiver(new Handler()));
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, latLng);
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            //remove spinner from address text view

            if (resultCode == FetchAddressIntentService.SUCCESS_RESULT) {
                setAddress(resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY));
            } else {
                Toast.makeText(RequestETA.this, resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setAddress(String address) {
        // Set the DestinationText as updatedDestination's address
        destinationText.setGravity(Gravity.START);
        destinationText.setText(address);

        // Hide destinationDescription layout
        destinationDescription.setText("");
        destinationDescription.setVisibility(View.GONE);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
                        checkIfLocationIsEnabled();

                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                            getString(R.string.location_permission_never_allow));
                }
                break;
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
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(RequestETA.this, Constants.REQUEST_CHECK_SETTINGS);
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
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "mGoogleApiClient is connected");
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
}
