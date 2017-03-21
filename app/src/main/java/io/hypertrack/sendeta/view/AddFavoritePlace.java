package io.hypertrack.sendeta.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.AddPlaceAutocompleteAdapter;
import io.hypertrack.sendeta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.model.UserPlace;
import io.hypertrack.sendeta.service.FetchAddressIntentService;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.KeyboardUtils;
import io.hypertrack.sendeta.util.LocationUtils;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.SuccessErrorCallback;


/**
 * Created by piyush on 22/06/16.
 */
public class AddFavoritePlace extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        TouchableWrapper.TouchActionDown, TouchableWrapper.TouchActionUp {

    public static final String KEY_UPDATED_PLACE = "updated_place";
    public static final String KEY_ADDED_OR_EDITED = "added_or_edited";
    private final String TAG = "AddFavoritePlace";
    int top, bottom, left, right;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private EditText addPlaceNameView, addPlaceAddressView;
    private ImageView placeNameClearIcon, placeAddressClearIcon;
    private RecyclerView mAutocompleteResults;
    private CardView mAutocompleteResultsLayout;
    private AddPlaceAutocompleteAdapter mAdapter;
    private LinearLayout addFavPlaceParentLayout;
    private ProgressDialog mProgressDialog;
    private UserPlace metaPlace;
    private boolean addNewMetaPlace = false, myLocationButtonClicked = false;
    private LatLng latlng;
    private View.OnFocusChangeListener mPlaceNameFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                addPlaceNameView.setCursorVisible(true);
            } else {
                addPlaceNameView.setCursorVisible(false);
            }
        }
    };

    private TextWatcher mPlaceNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            // Show/Hide Clear Icon on Place Name View
            if (s != null && s.length() > 0) {
                placeNameClearIcon.setVisibility(View.VISIBLE);
                right = getResources().getDimensionPixelSize(R.dimen.padding_huge);
                addPlaceNameView.setPadding(left, top, right, bottom);
            } else {
                placeNameClearIcon.setVisibility(View.GONE);
                right = getResources().getDimensionPixelSize(R.dimen.padding_high);
                addPlaceNameView.setPadding(left, top, right, bottom);
            }
        }
    };

    private TextWatcher mPlaceAddressTextWatcher = new TextWatcher() {
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

            // Show/Hide Clear Icon on Place Address View
            if (constraint.length() > 0) {
                placeAddressClearIcon.setVisibility(View.VISIBLE);
                right = getResources().getDimensionPixelSize(R.dimen.padding_huge);
                addPlaceAddressView.setPadding(left, top, right, bottom);
            } else {
                placeAddressClearIcon.setVisibility(View.GONE);
                right = getResources().getDimensionPixelSize(R.dimen.padding_high);
                addPlaceAddressView.setPadding(left, top, right, bottom);
            }
        }
    };

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(UserPlace place) {

            // On Click Disable handling/showing any more results
            mAdapter.setSearching(false);

            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18.0f));
            }

            // Update LatLng variable with the currently selected Google Place
            latlng = place.getLatLng();

            metaPlace.setGooglePlacesID(place.getGooglePlacesID());

            // Remove Text Changed Listener to prevent Autocomplete updates on setText
            // Text Changed Listener will be added again in OnFocusChangeListener
            addPlaceAddressView.removeTextChangedListener(mPlaceAddressTextWatcher);

            addPlaceAddressView.setText(place.getAddress());
            mAdapter.setBounds(LocationUtils.getBounds(place.getLatLng(), LocationUtils.DISTANCE_IN_METERS));

            KeyboardUtils.hideKeyboard(AddFavoritePlace.this, addPlaceAddressView);
            addFavPlaceParentLayout.requestFocus();

            //Hide the Results List on Selection
            mAutocompleteResults.setVisibility(View.GONE);
            mAutocompleteResultsLayout.setVisibility(View.GONE);
        }

        @Override
        public void OnError() {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favorite_place);

        // Initialize Toolbar
        initToolbar(getString(R.string.title_activity_add_fav_place));

        // Fetch Meta Place object passed with intent
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("meta_place")) {
                metaPlace = (UserPlace) intent.getSerializableExtra("meta_place");
                latlng = metaPlace.getLatLng();
            }
        }

        // Initialize GoogleApiClient & UI Views
        initGoogleClient();
        createLocationRequest();
        getMap();
        initNameAddressView();
        initAutocompleteResultsView();

        top = getResources().getDimensionPixelSize(R.dimen.padding_high);
        bottom = getResources().getDimensionPixelSize(R.dimen.padding_high);
        left = getResources().getDimensionPixelSize(R.dimen.padding_high);
        right = getResources().getDimensionPixelSize(R.dimen.padding_huge);

        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, "We could not detect internet on your mobile or there seems to be connectivity issues.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        mGoogleApiClient.connect();
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LocationUtils.INITIAL_LOCATION_UPDATE_INTERVAL_TIME)
                .setFastestInterval(LocationUtils.INITIAL_LOCATION_UPDATE_INTERVAL_TIME);
    }

    private void getMap() {
        TouchableSupportMapFragment mapFragment = (TouchableSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        int top = getResources().getDimensionPixelSize(R.dimen.add_favorite_place_map_top_padding);
        int right = getResources().getDimensionPixelSize(R.dimen.add_favorite_place_map_right_padding);

        rlp.setMargins(0, 0, right, top);
    }

    private void initNameAddressView() {
        addFavPlaceParentLayout = (LinearLayout) findViewById(R.id.add_fav_place_parent);
        addPlaceNameView = (EditText) findViewById(R.id.add_fav_place_name);
        placeNameClearIcon = (ImageView) findViewById(R.id.add_fav_place_name_clear);
        addPlaceAddressView = (EditText) findViewById(R.id.add_fav_place_address);
        placeAddressClearIcon = (ImageView) findViewById(R.id.add_fav_place_address_clear);

        // Clear Focus & Cursor from AddPlaceName View
        addPlaceNameView.clearFocus();

        if (metaPlace.getAddress() != null && !metaPlace.getAddress().isEmpty()) {
            addPlaceAddressView.setText(metaPlace.getAddress());
            placeAddressClearIcon.setVisibility(View.VISIBLE);

            if (metaPlace.getName() != null && !metaPlace.getName().isEmpty()) {
                addPlaceNameView.setText(metaPlace.getName());
                placeNameClearIcon.setVisibility(View.VISIBLE);
            }
        } else {

            // If only Place Name is available, Update Name in Address field & Don't Reverse Geocode
            if (metaPlace.getName() != null && !metaPlace.getName().isEmpty()) {
                addPlaceAddressView.setText(metaPlace.getName());
                placeAddressClearIcon.setVisibility(View.VISIBLE);
            } else {
                reverseGeocode(metaPlace.getLatLng());
            }
        }

        addPlaceNameView.setOnFocusChangeListener(mPlaceNameFocusChangeListener);

        addPlaceNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addPlaceNameView.addTextChangedListener(mPlaceNameTextWatcher);
                } else {
                    addPlaceNameView.removeTextChangedListener(mPlaceNameTextWatcher);
                }
            }
        });

        addPlaceAddressView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addPlaceAddressView.addTextChangedListener(mPlaceAddressTextWatcher);
                } else {
                    addPlaceAddressView.removeTextChangedListener(mPlaceAddressTextWatcher);
                }
            }
        });
    }

    private void initAutocompleteResultsView() {
        mAutocompleteResults = (RecyclerView) findViewById(R.id.add_fav_places_results);
        mAutocompleteResultsLayout = (CardView) findViewById(R.id.add_fav_places_results_layout);

        mAdapter = new AddPlaceAutocompleteAdapter(this, mGoogleApiClient, mPlaceAutoCompleteListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mAutocompleteResults.setLayoutManager(layoutManager);
        mAutocompleteResults.setAdapter(mAdapter);

        mAdapter.setBounds(LocationUtils.getBounds(metaPlace.getLatLng(), LocationUtils.DISTANCE_IN_METERS));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(AddFavoritePlace.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        if (metaPlace.getLocation().getLatitude() != 0.0 && metaPlace.getLocation().getLongitude() != 0.0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(metaPlace.getLatLng(), 18.0f));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0.0, 0.0), 0.0f));
        }

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

    public void processPublishedResults(ArrayList<AutocompletePrediction> results) {
        if (results != null && results.size() > 0) {
            mAutocompleteResults.smoothScrollToPosition(0);
            mAutocompleteResults.setVisibility(View.VISIBLE);
            mAutocompleteResultsLayout.setVisibility(View.VISIBLE);
        } else {
            mAutocompleteResults.setVisibility(View.GONE);
            mAutocompleteResultsLayout.setVisibility(View.GONE);
        }
    }

    public void onSaveButtonClicked(MenuItem v) {
        OnboardingUser user = OnboardingManager.sharedManager().getUser();
        if (user == null) {
            return;
        }

        addNewMetaPlace = !user.isFavorite(metaPlace);
        metaPlace.setName(addPlaceNameView.getText().toString());

        if (metaPlace.getName() == null || metaPlace.getName().isEmpty()) {
            Toast.makeText(this, R.string.place_name_required_error, Toast.LENGTH_SHORT).show();

            processUpdatedMetaPlaceForAnalytics(false, ErrorMessages.PLACE_NAME_REQUIRED_ERROR);
            return;
        }

        if (metaPlace.isHome()) {
            if (user.hasHome() && !user.getHome().isEqualPlace(metaPlace)) {
                Toast.makeText(this, R.string.home_already_exists_error, Toast.LENGTH_SHORT).show();

                processUpdatedMetaPlaceForAnalytics(false, ErrorMessages.HOME_ALREADY_EXISTS_ERROR);
                return;
            }
        } else if (metaPlace.isWork()) {
            if (user.hasWork() && !user.getWork().isEqualPlace(metaPlace)) {
                Toast.makeText(this, R.string.work_already_exists_error, Toast.LENGTH_SHORT).show();

                processUpdatedMetaPlaceForAnalytics(false, ErrorMessages.WORK_ALREADY_EXISTS_ERROR);
                return;
            }
        }

        metaPlace.setAddress(addPlaceAddressView.getText().toString());
        metaPlace.setLatLng(latlng);

        if (addNewMetaPlace) {
            addPlace();
        } else {
            editPlace();
        }
    }

    public void onPlaceNameClearClick(View view) {
        placeNameClearIcon.setVisibility(View.GONE);
        addPlaceNameView.setText("");

        // Reset Right Padding for addPlaceNameView
        right = getResources().getDimensionPixelSize(R.dimen.padding_high);
        addPlaceNameView.setPadding(left, top, right, bottom);

        // Remove & Add the Text Watcher Again
        addPlaceNameView.removeTextChangedListener(mPlaceNameTextWatcher);
        addPlaceNameView.addTextChangedListener(mPlaceNameTextWatcher);
    }

    public void onPlaceAddressClearClick(View view) {
        placeAddressClearIcon.setVisibility(View.GONE);
        addPlaceAddressView.setText("");

        // Reset Right Padding for addPlaceAddressView
        right = getResources().getDimensionPixelSize(R.dimen.padding_high);
        addPlaceAddressView.setPadding(left, top, right, bottom);

        // Remove the Text Watcher Again
        // Text Changed Listener will be added again in OnFocusChangeListener
        addPlaceAddressView.removeTextChangedListener(mPlaceAddressTextWatcher);
        if (addPlaceAddressView.hasFocus()) {
            addPlaceAddressView.addTextChangedListener(mPlaceAddressTextWatcher);
        }

        mAutocompleteResults.setVisibility(View.GONE);
    }

    private void addPlace() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.saving_favorite_addresses_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        OnboardingManager.sharedManager().getUser().addPlace(metaPlace, new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {
                processUpdatedMetaPlaceForAnalytics(true, null);
                mProgressDialog.dismiss();
                broadcastResultIntent();
                finish();
            }

            @Override
            public void OnError() {
                processUpdatedMetaPlaceForAnalytics(false, ErrorMessages.ADDING_FAVORITE_PLACE_FAILED);

                mProgressDialog.dismiss();
                showAddPlaceError();
            }
        });
    }

    private void editPlace() {

       /* if (metaPlace.getId() == 0) {
            Toast.makeText(AddFavoritePlace.this, ErrorMessages.EDITING_ALREADY_SAVED_PLACE_ERROR,
                    Toast.LENGTH_SHORT).show();
            processUpdatedMetaPlaceForAnalytics(false, ErrorMessages.EDITING_ALREADY_SAVED_PLACE_ERROR);
            return;
        }*/

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.editing_favorite_addresses_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
//TODO add editPlace method
      /*  OnboardingManager.sharedManager().getUser().editPlace(metaPlace, new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {
                processUpdatedMetaPlaceForAnalytics(true, null);

                mProgressDialog.dismiss();
                broadcastResultIntent();
                finish();
            }

            @Override
            public void OnError() {
                processUpdatedMetaPlaceForAnalytics(false, ErrorMessages.EDITING_FAVORITE_PLACE_FAILED);

                mProgressDialog.dismiss();
                showEditPlaceError();
            }
        });
*/
    }

    private void broadcastResultIntent() {
        Intent intent = new Intent();
        intent.putExtra(KEY_ADDED_OR_EDITED, addNewMetaPlace);
        intent.putExtra(KEY_UPDATED_PLACE, metaPlace);
        setResult(Constants.FAVORITE_PLACE_REQUEST_CODE, intent);
    }

    /**
     * Method to process updated User FavoritePlace to log Analytics Event
     *
     * @param status       Flag to indicate status of FavoritePlace Deletion event
     * @param errorMessage ErrorMessage in case of Failure
     */
    private void processUpdatedMetaPlaceForAnalytics(boolean status, String errorMessage) {

        if (metaPlace.isHome()) {
            if (addNewMetaPlace) {
                AnalyticsStore.getLogger().addedHome(status, errorMessage);
            } else {
                AnalyticsStore.getLogger().editedHome(status, errorMessage);
            }
        } else if (metaPlace.isWork()) {
            if (addNewMetaPlace) {
                AnalyticsStore.getLogger().addedWork(status, errorMessage);
            } else {
                AnalyticsStore.getLogger().editedWork(status, errorMessage);
            }
        } else {

            if (addNewMetaPlace) {
                OnboardingUser user = OnboardingManager.sharedManager().getUser();

                // Initialize favoritesCount based on current UserPlace update status
                int favoritesCount = status ? 1 : 0;

                if (user != null) {
                    List<UserPlace> userPlaces = user.getFavoritePlaces();
                    if (userPlaces != null && userPlaces.size() > 0)
                        favoritesCount = userPlaces.size();
                }

                AnalyticsStore.getLogger().addedOtherFavorite(status, errorMessage, favoritesCount);
            } else {
                AnalyticsStore.getLogger().editedOtherFavorite(status, errorMessage);
            }
        }
    }

    private void showAddPlaceError() {
        Toast.makeText(this, R.string.adding_favorite_place_failed, Toast.LENGTH_SHORT).show();
    }

    private void showEditPlaceError() {
        Toast.makeText(this, R.string.editing_favorite_place_failed, Toast.LENGTH_SHORT).show();
    }

    private void reverseGeocode(LatLng latLng) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, new AddressResultReceiver(new Handler()));
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, latLng);
        startService(intent);
    }

    private void setAddress(String address) {
        addPlaceAddressView.setText(address);
        placeAddressClearIcon.setVisibility(View.VISIBLE);
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
                            status.startResolutionForResult(AddFavoritePlace.this, Constants.REQUEST_CHECK_SETTINGS);
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
    public void onTouchDown(MotionEvent event) {
    }

    @Override
    public void onTouchUp(MotionEvent event) {
        if (mMap != null) {
            metaPlace.setGooglePlacesID(null);
            mAdapter.setBounds(LocationUtils.getBounds(mMap.getCameraPosition().target, LocationUtils.DISTANCE_IN_METERS));
            reverseGeocode(mMap.getCameraPosition().target);

            latlng = mMap.getCameraPosition().target;
            // set spinner for address text view
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_fav_place, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "mGoogleApiClient is connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
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
                Toast.makeText(AddFavoritePlace.this, resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
