package io.hypertrack.sendeta.view;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Date;
import java.util.List;

import io.hypertrack.lib.common.model.HTDriverVehicleType;
import io.hypertrack.lib.consumer.utils.HTCircleImageView;
import io.hypertrack.lib.transmitter.model.HTTrip;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.PlaceAutocompleteAdapter;
import io.hypertrack.sendeta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.TripETAResponse;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.LocationStore;
import io.hypertrack.sendeta.store.TripManager;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.store.callback.TripETACallback;
import io.hypertrack.sendeta.store.callback.TripManagerCallback;
import io.hypertrack.sendeta.store.callback.TripManagerListener;
import io.hypertrack.sendeta.util.AnimationUtils;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.KeyboardUtils;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.PermissionUtils;

public class Home extends BaseActivity implements ResultCallback<Status>, LocationListener,
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "Home";
    private static final long INTERVAL_TIME = 5000;


    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    private SupportMapFragment mMapFragment;

    private AppBarLayout appBarLayout;
    private TextView destinationText, destinationDescription, mAutocompletePlacesView;
    private LinearLayout enterDestinationLayout, mAutocompletePlacesLayout;
    public CardView mAutocompleteResultsLayout;
    public RecyclerView mAutocompleteResults;
    private Button sendETAButton;
    private ImageButton shareButton, navigateButton, favoriteButton;
    private View customMarkerView;
    private HTCircleImageView profileViewProfileImage;

    private ProgressDialog mProgressDialog;
    private ProgressBar mAutocompleteLoader;

    private Bitmap profilePicBitmap;
    private Marker currentLocationMarker, destinationLocationMarker;

    private PlaceAutocompleteAdapter mAdapter;

    private boolean enterDestinationLayoutClicked = false, tripShared = false;

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(MetaPlace place) {

            // Check if selected place is a User Favorite to log Analytics Event
            boolean isFavorite = false;
            User user = UserStore.sharedStore.getUser();
            if (user != null) {
                isFavorite = user.isSynced(place);
            }
            AnalyticsStore.getLogger().selectedAddress(mAutocompletePlacesView.getText().length(), isFavorite);

            //Restore Default State for Enter Destination Layout
            onEnterDestinationBackClick(null);

            // Set the Enter Destination Layout to Selected Place
            destinationText.setGravity(Gravity.LEFT);
            destinationText.setText(place.getName());

            // Set the selected Place Description as Place Address
            destinationDescription.setText(place.getAddress());
            destinationDescription.setVisibility(View.VISIBLE);

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

        getEtaForDestination(place.getLatLng(), new TripETACallback() {
            @Override
            public void OnSuccess(TripETAResponse etaResponse) {
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

    private void getEtaForDestination(LatLng destinationLocation, final TripETACallback callback) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting your destination");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        if (currentLocationMarker == null || currentLocationMarker.getPosition() == null || destinationLocation == null) {
            mProgressDialog.dismiss();
            callback.OnError();
            return;
        }

        TripManager.getSharedManager().getETA(currentLocationMarker.getPosition(), destinationLocation, new TripETACallback() {
            @Override
            public void OnSuccess(TripETAResponse etaResponse) {
                mProgressDialog.dismiss();
                callback.OnSuccess(etaResponse);
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
                callback.OnError();
            }
        });
    }

    private void showETAError() {
        Toast.makeText(this, getString(R.string.eta_fetching_error), Toast.LENGTH_SHORT).show();
    }

    private void onETASuccess(TripETAResponse response, MetaPlace place) {
        updateViewForETASuccess((int) response.getDuration() / 60, place.getLatLng());
        TripManager.getSharedManager().setPlace(place);
    }

    private void updateViewForETASuccess(int etaInMinutes, LatLng latLng) {
        showSendETAButton();
        updateDestinationMarker(latLng, etaInMinutes);
        updateMapView();
    }

    private void showSendETAButton() {
        sendETAButton.setText(getString(R.string.action_send_eta));
        sendETAButton.setVisibility(View.VISIBLE);
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

            // Check If LOCATION Permission is available
            if (!(ContextCompat.checkSelfPermission(Home.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                // Show Rationale & Request for LOCATION permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PermissionUtils.showRationaleMessageAsDialog(Home.this, Manifest.permission.ACCESS_FINE_LOCATION,
                            getString(R.string.read_phone_state_permission_title), getString(R.string.read_phone_state_msg));
                } else {
                    PermissionUtils.requestPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION);
                }

                return;
            }

            // Check if Location was enabled & if valid location was received
            if (currentLocationMarker == null ||
                    new LatLng(0.0, 0.0).equals(currentLocationMarker.getPosition())) {

                Toast.makeText(Home.this, "Please enable Location first", Toast.LENGTH_SHORT).show();
                checkIfLocationIsEnabled();

            } else {
                enterDestinationLayoutClicked = true;

                // Reset Current State when user chooses to edit destination
                TripManager.getSharedManager().clearState();
                OnTripEnd();

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
        User user = UserStore.sharedStore.getUser();
        if (user != null) {
            places = user.getPlaces();
        }

        if (places == null || places.isEmpty()) {
            return;
        }

        mAdapter.refreshFavorites(places);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Toolbar without Home Button
        initToolbar(getResources().getString(R.string.app_name), false);

        // Initialize Maps
        MapsInitializer.initialize(getApplicationContext());

        // Get Map Object
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        // Initialize UI Views
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        checkIfUserIsOnBoard();

        initGoogleClient();
        createLocationRequest();
        setupEnterDestinationView();
        setupAutoCompleteView();
        setupShareButton();
        setupSendETAButton();
        setupNavigateButton();
        setupFavoriteButton();
        initCustomMarkerView();

        // Check & Prompt User if Internet is Not Connected
        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, "We could not detect internet on your mobile or there seems to be connectivity issues",
                    Toast.LENGTH_SHORT).show();
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Please wait while we check if there is any existing trip.");
        mProgressDialog.show();
    }

    private void checkIfUserIsOnBoard() {
        boolean isUserOnboard = UserStore.isUserLoggedIn();
        if (!isUserOnboard) {
            startActivity(new Intent(this, Register.class));
            finish();
        } else {
            UserStore.sharedStore.initializeUser();
        }
    }

    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL_TIME);
        locationRequest.setFastestInterval(INTERVAL_TIME);
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
        mAutocompletePlacesLayout = (LinearLayout) findViewById(R.id.autocomplete_places_layout);
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

                AnalyticsStore.getLogger().tappedShareIcon(tripShared);
            }
        });
    }

    private void setupSendETAButton() {
        // Initialize SendETA Button UI View
        sendETAButton = (Button) findViewById(R.id.sendETAButton);

        // Set Click Listener for SendETA Button
        sendETAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TripManager.getSharedManager().isTripActive()) {
                    startTrip();
                } else {
                    endTrip();
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

    private void initCustomMarkerView() {
        // Initialize Custom Marker (Hero Marker) UI View
        customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_hero_marker, null);
        profileViewProfileImage = (HTCircleImageView) customMarkerView.findViewById(R.id.profile_image);
        updateProfileImage();
    }

    private void updateProfileImage() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile_pic);
        User user = UserStore.sharedStore.getUser();
        if (user != null) {
            Bitmap userImageBitmap = user.getImageBitmap();
            if (userImageBitmap != null) {
                bitmap = userImageBitmap;
            }
        }

        profileViewProfileImage.setImageBitmap(bitmap);
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

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                restoreTripStateIfNeeded();
            }
        });
    }

    private void updateMapPadding(boolean activeTrip) {
        int top = getResources().getDimensionPixelSize(R.dimen.map_top_padding);
        int left = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
        int right = activeTrip ? getResources().getDimensionPixelSize(R.dimen.map_active_trip_side_padding) : getResources().getDimensionPixelSize(R.dimen.map_side_padding);
        int bottom = getResources().getDimensionPixelSize(R.dimen.map_bottom_padding);

        mMap.setPadding(left, top, right, bottom);
    }

    private void restoreTripStateIfNeeded() {
        final TripManager tripManager = TripManager.getSharedManager();
        tripManager.restoreState(new TripManagerCallback() {
            @Override
            public void OnSuccess() {
                Log.v(TAG, "Trip is active");

                if (mProgressDialog != null)
                    mProgressDialog.dismiss();

                MetaPlace place = tripManager.getPlace();
                updateViewForETASuccess(0, place.getLatLng());
                destinationText.setGravity(Gravity.LEFT);
                destinationText.setText(place.getName());

                destinationDescription.setText(place.getAddress());
                destinationDescription.setVisibility(View.VISIBLE);

                onTripStart();
            }

            @Override
            public void OnError() {
                Log.v(TAG, "Trip is not active");

                if (mProgressDialog != null)
                    mProgressDialog.dismiss();
            }
        });
    }

    /**
     * Method to Initiate START TRIP
     */
    private void startTrip() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Preparing your trip");
        mProgressDialog.show();

        TripManager.getSharedManager().startTrip(new TripManagerCallback() {
            @Override
            public void OnSuccess() {
                mProgressDialog.dismiss();
                share();
                onTripStart();

                AnalyticsStore.getLogger().startedTrip(true, null);
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
                showStartTripError();

                AnalyticsStore.getLogger().startedTrip(false, ErrorMessages.START_TRIP_FAILED);
            }
        });
    }

    private void showStartTripError() {
        Toast.makeText(this, ErrorMessages.START_TRIP_FAILED, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to Initiate END TRIP
     */
    private void endTrip() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Stopping trip ... ");
        mProgressDialog.show();

        TripManager.getSharedManager().endTrip(new TripManagerCallback() {
            @Override
            public void OnSuccess() {
                mProgressDialog.dismiss();
                OnTripEnd();

                AnalyticsStore.getLogger().tappedEndTrip(true, null);
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
                showEndTripError();

                AnalyticsStore.getLogger().tappedEndTrip(false, ErrorMessages.END_TRIP_FAILED);
            }
        });
    }

    private void showEndTripError() {
        Toast.makeText(this, getString(R.string.end_trip_failed), Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to update State Variables & UI to reflect Trip Started
     */
    private void onTripStart() {
        // TODO: 29/06/16 Add sendETAButton visibility to VISIBLE?
        sendETAButton.setText(getString(R.string.action_end_trip));
        shareButton.setVisibility(View.VISIBLE);
        navigateButton.setVisibility(View.VISIBLE);
        enterDestinationLayout.setOnClickListener(null);
        favoriteButton.setVisibility(View.VISIBLE);
        updateFavoritesButton();

        TripManager tripManager = TripManager.getSharedManager();
        tripManager.setTripRefreshedListener(new TripManagerListener() {
            @Override
            public void OnCallback() {
                updateETAForOnGoingTrip();
            }
        });
        tripManager.setTripEndedListener(new TripManagerListener() {
            @Override
            public void OnCallback() {
                OnTripEnd();
            }
        });

        updateMapPadding(true);
    }

    private void updateETAForOnGoingTrip() {
        TripManager tripManager = TripManager.getSharedManager();

        HTTrip trip = tripManager.getHyperTrackTrip();
        if (trip == null) {
            return;
        }

        MetaPlace place = tripManager.getPlace();
        if (place == null) {
            return;
        }

        LatLng destinationLocation = new LatLng(place.getLatitude(), place.getLongitude());

        Date ETA = trip.getETA();
        if (ETA == null) {
            return;
        }

        Date now = new Date();
        long etaInSecond = ETA.getTime() - now.getTime();
        int etaInMinutes = (int) etaInSecond / (60 * 1000);

        updateDestinationMarker(destinationLocation, etaInMinutes);
    }

    /**
     * Method to update State Variables & UI to reflect Trip Ended
     */
    private void OnTripEnd() {
        sendETAButton.setVisibility(View.GONE);
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
    }

    private void updateDestinationMarker(LatLng destinationLocation, int etaInMinutes) {
        if (destinationLocationMarker != null) {
            destinationLocationMarker.remove();
            destinationLocationMarker = null;
        }

        View markerView = getDestinationMarkerView(etaInMinutes);

        destinationLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(destinationLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(getBitMapForView(this, markerView))));
    }

    private View getDestinationMarkerView(int etaInMinutes) {
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_destination_marker_layout, null);
        TextView etaTextView = (TextView) marker.findViewById(R.id.eta_txt);
        updateTextViewForMinutes(etaTextView, etaInMinutes);
        return marker;
    }

    private void updateTextViewForMinutes(TextView textView, int etaInMinutes) {
        if (etaInMinutes == 0) {
            textView.setText("--");
        } else {
            textView.setText(etaInMinutes + " m");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "mGoogleApiClient is connected");

        // Check If LOCATION Permission is available & then if Location is enabled
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkIfLocationIsEnabled();

        } else {
            // Show Rationale & Request for LOCATION permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionUtils.showRationaleMessageAsDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                        getString(R.string.read_phone_state_permission_title), getString(R.string.read_phone_state_msg));
            } else {
                PermissionUtils.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    private void requestForLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this);
        } catch (SecurityException exception) {
            Toast.makeText(Home.this, "Unable to request for location. Please check permissions in app settings.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to check if the Location Services are enabled and in case not, request user to
     * enable them.
     */
    private void checkIfLocationIsEnabled() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).setAlwaysShow(true);
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
                        requestForLocationUpdates();

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

        LocationStore.sharedStore().setCurrentLocation(location);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (currentLocationMarker == null) {
            addMarkerToCurrentLocation(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
        } else {
            currentLocationMarker.setPosition(latLng);
        }

        updateMapView();

        mAdapter.setBounds(getBounds(latLng, 10000));
    }

    private void addMarkerToCurrentLocation(LatLng latLng) {
        currentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(getBitMapForView(this, customMarkerView))));
    }

    private Bitmap getBitMapForView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();

        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

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

        mMap.animateCamera(cameraUpdate);
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
     * Method to Share current trip
     */
    private void share() {
        String shareMessage = TripManager.getSharedManager().getShareMessage();
        if (shareMessage == null) {
            return;
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivityForResult(Intent.createChooser(sharingIntent, "Share via"), Constants.SHARE_REQUEST_CODE);
    }

    /**
     * Method to Navigate current trip in Google Navigation
     */
    private void navigate() {
        TripManager tripManager = TripManager.getSharedManager();

        MetaPlace place = tripManager.getPlace();
        if (place == null) {
            return;
        }

        Double latitude = place.getLatitude();
        Double longitude = place.getLongitude();
        if (latitude == null || longitude == null) {
            return;
        }

        String mode = "d";
        HTTrip trip = tripManager.getHyperTrackTrip();
        if (trip != null && trip.getVehicleType() != null) {
            HTDriverVehicleType type = trip.getVehicleType();
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
     * Method to Open User Profile Screen
     *
     * @param menuItem
     */
    public void onProfileButtonClicked(MenuItem menuItem) {
        Intent profileIntent = new Intent(this, UserProfile.class);
        startActivity(profileIntent);

        AnalyticsStore.getLogger().tappedProfile();
    }

    /**
     * Method to add current selected destination as a Favorite
     * (NOTE: Only Applicable for Live Trip)
     *
     * @param view
     */
    public void OnFavoriteClick(View view) {
        TripManager tripManager = TripManager.getSharedManager();
        MetaPlace place = tripManager.getPlace();

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
     * (depends if current trip is a Live Trip & selected Place is not already a favorite)
     */
    private void updateFavoritesButton() {
        TripManager tripManager = TripManager.getSharedManager();
        if (tripManager.isTripActive()) {
            MetaPlace place = tripManager.getPlace();
            if (place == null) {
                return;
            }

            User user = UserStore.sharedStore.getUser();
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

    private void updateCurrentLocationMarker() {
        if (currentLocationMarker == null) {
            return;
        }

        LatLng position = currentLocationMarker.getPosition();
        currentLocationMarker.remove();
        initCustomMarkerView();

        addMarkerToCurrentLocation(position);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkIfLocationIsEnabled();
                } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PermissionUtils.showPermissionDeclineMessage(this, Manifest.permission.ACCESS_FINE_LOCATION);
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
                    requestForLocationUpdates();
                    break;

                case Activity.RESULT_CANCELED:

                    Log.i(TAG, "User chose not to make required location settings changes.");

                    // Location Service Enable Request denied, boo! Fire LocationDenied event

                    break;
            }

        } else if (requestCode == Constants.FAVORITE_PLACE_REQUEST_CODE) {

            updateFavoritesButton();

        } else if (requestCode == Constants.SHARE_REQUEST_CODE) {

            Log.d(TAG, resultCode + "");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFavoritesButton();
        updateCurrentLocationMarker();

        AppEventsLogger.activateApp(getApplication());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (enterDestinationLayoutClicked) {
            onEnterDestinationBackClick(null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}