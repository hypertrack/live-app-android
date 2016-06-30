package io.hypertrack.sendeta.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.AddPlaceAutocompleteAdapter;
import io.hypertrack.sendeta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.service.FetchAddressIntentService;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.KeyboardUtils;
import io.hypertrack.sendeta.util.NetworkUtils;
import io.hypertrack.sendeta.util.SuccessErrorCallback;
import io.realm.RealmList;

/**
 * Created by piyush on 22/06/16.
 */
public class AddFavoritePlace extends BaseActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, TouchableWrapper.TouchActionDown, TouchableWrapper.TouchActionUp {

    private final String TAG = "AddFavoritePlace";

    public static final int FAVORITE_PLACE_REQUEST_CODE = 100;
    public static final String KEY_UPDATED_PLACE = "updated_place";
    public static final String KEY_ADDED_OR_EDITED = "added_or_edited";

    private static final int DISTANCE_IN_METERS = 10000;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;

    private EditText addPlaceNameView, addPlaceAddressView;

    private ImageView placeNameClearIcon, placeAddressClearIcon;

    private RecyclerView mAutocompleteResults;
    private CardView mAutocompleteResultsLayout;
    private AddPlaceAutocompleteAdapter mAdapter;

    private ProgressDialog mProgressDialog;

    private MetaPlace metaPlace;
    private boolean metaPlaceAdded = false;

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
            } else {
                placeNameClearIcon.setVisibility(View.GONE);
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
            mAdapter.getFilter().filter(constraint);

            // Show/Hide Clear Icon on Place Address View
            if (constraint.length() > 0) {
                placeAddressClearIcon.setVisibility(View.VISIBLE);
            } else {
                placeAddressClearIcon.setVisibility(View.GONE);
            }
        }
    };

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(MetaPlace place) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18.0f));

            metaPlace.setGooglePlacesID(place.getGooglePlacesID());

            // Remove Text Changed Listener to prevent Autocomplete updates on setText
            addPlaceAddressView.removeTextChangedListener(mPlaceAddressTextWatcher);

            addPlaceAddressView.setText(place.getAddress());
            mAdapter.setBounds(getBounds(place.getLatLng(), DISTANCE_IN_METERS));

            KeyboardUtils.hideKeyboard(AddFavoritePlace.this, addPlaceAddressView);

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

        initToolbar("Favorite");

        // Fetch Meta Place object passed with intent
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("meta_place")) {
                metaPlace = (MetaPlace) intent.getSerializableExtra("meta_place");
            }
        }

        // Initialize GoogleApiClient & UI Views
        initGoogleClient();
        getMap();
        initNameAddressView();
        initAutocompleteResultsView();

        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, "We could not detect internet on your mobile or there seems to be connectivity issues.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .build();

        mGoogleApiClient.connect();
    }

    private void getMap() {
        TouchableSupportMapFragment mapFragment = (TouchableSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initNameAddressView() {
        addPlaceNameView = (EditText) findViewById(R.id.add_fav_place_name);
        placeNameClearIcon = (ImageView) findViewById(R.id.add_fav_place_name_clear);
        addPlaceAddressView = (EditText) findViewById(R.id.add_fav_place_address);
        placeAddressClearIcon = (ImageView) findViewById(R.id.add_fav_place_address_clear);

        if (metaPlace.getName() != null && !metaPlace.getName().isEmpty()) {
            addPlaceNameView.setText(metaPlace.getName());
            placeNameClearIcon.setVisibility(View.VISIBLE);
        }

        if (metaPlace.getAddress() != null && !metaPlace.getAddress().isEmpty()) {
            addPlaceAddressView.setText(metaPlace.getAddress());
            placeAddressClearIcon.setVisibility(View.VISIBLE);
        } else {
            reverseGeocode(metaPlace.getLatLng());
        }

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

        mAdapter.setBounds(getBounds(metaPlace.getLatLng(), DISTANCE_IN_METERS));
    }

    private LatLngBounds getBounds(LatLng latLng, int mDistanceInMeters) {
        double latRadian = Math.toRadians(latLng.latitude);

        double degLatKm = 110.574235;
        double degLngKm = 110.572833 * Math.cos(latRadian);
        double deltaLat = mDistanceInMeters / 1000.0 / degLatKm;
        double deltaLong = mDistanceInMeters / 1000.0 / degLngKm;

        double minLat = latLng.latitude - deltaLat;
        double minLong = latLng.longitude - deltaLong;
        double maxLat = latLng.latitude + deltaLat;
        double maxLong = latLng.longitude + deltaLong;

        com.google.android.gms.maps.model.LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(new LatLng(minLat, minLong));
        b.include(new LatLng(maxLat, maxLong));
        LatLngBounds bounds = b.build();

        return bounds;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(metaPlace.getLatLng(), 18.0f));
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
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        metaPlaceAdded = user.isSynced(metaPlace);
        metaPlace.setName(addPlaceNameView.getText().toString());

        if (metaPlace.getName() == null || metaPlace.getName().isEmpty()) {
            Toast.makeText(this, R.string.place_name_error, Toast.LENGTH_LONG).show();
            return;
        }

        if (metaPlace.isHome()) {
            if (user.hasHome() && !user.getHome().isEqualPlace(metaPlace)) {
                Toast.makeText(this, R.string.home_exists_error, Toast.LENGTH_LONG).show();

                processUpdatedMetaPlaceForAnalytics(false, null);
                return;
            }
        } else if (metaPlace.isWork()) {
            if (user.hasWork() && !user.getWork().isEqualPlace(metaPlace)) {
                Toast.makeText(this, R.string.work_exists_error, Toast.LENGTH_LONG).show();
                return;
            }
        }

        metaPlace.setAddress(addPlaceAddressView.getText().toString());
        metaPlace.setLatLng(mMap.getCameraPosition().target);

        if (metaPlaceAdded) {
            editPlace();
        } else {
            addPlace();
        }
    }

    public void onPlaceNameClearClick(View view) {
        placeNameClearIcon.setVisibility(View.GONE);
        addPlaceNameView.setText("");

        // Remove & Add the Text Watcher Again
        addPlaceNameView.removeTextChangedListener(mPlaceNameTextWatcher);
        addPlaceNameView.addTextChangedListener(mPlaceNameTextWatcher);
    }

    public void onPlaceAddressClearClick(View view) {
        placeAddressClearIcon.setVisibility(View.GONE);
        addPlaceAddressView.setText("");

        // Remove & Add the Text Watcher Again
        addPlaceAddressView.removeTextChangedListener(mPlaceAddressTextWatcher);
        addPlaceAddressView.addTextChangedListener(mPlaceAddressTextWatcher);
    }

    private void addPlace() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Adding place");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        UserStore.sharedStore.addPlace(metaPlace, new SuccessErrorCallback() {
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
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Editing place");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        UserStore.sharedStore.editPlace(metaPlace, new SuccessErrorCallback() {
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
    }

    private void broadcastResultIntent() {
        Intent intent = new Intent();
        intent.putExtra(KEY_ADDED_OR_EDITED, metaPlaceAdded);
        intent.putExtra(KEY_UPDATED_PLACE, metaPlace);
        setResult(FAVORITE_PLACE_REQUEST_CODE, intent);
    }

    /**
     * Method to process updated User FavoritePlace to log Analytics Event
     *
     * @param status       Flag to indicate status of FavoritePlace Deletion event
     * @param errorMessage ErrorMessage in case of Failure
     */
    private void processUpdatedMetaPlaceForAnalytics(boolean status, String errorMessage) {

        if (metaPlace.isHome()) {
            if (metaPlaceAdded) {
                AnalyticsStore.getLogger().addedHome(status, errorMessage);
            } else {
                AnalyticsStore.getLogger().editedHome(status, errorMessage);
            }
        } else if (metaPlace.isWork()) {
            if (metaPlaceAdded) {
                AnalyticsStore.getLogger().addedWork(status, errorMessage);
            } else {
                AnalyticsStore.getLogger().editedWork(status, errorMessage);
            }
        } else {

            if (metaPlaceAdded) {
                User user = UserStore.sharedStore.getUser();

                // Initialize favoritesCount based on current MetaPlace update status
                int favoritesCount = status ? 1 : 0;

                if (user != null) {
                    RealmList<MetaPlace> userPlaces = user.getPlaces();
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
        Toast.makeText(this, R.string.add_place_error, Toast.LENGTH_LONG).show();
    }

    private void showEditPlaceError() {
        Toast.makeText(this, R.string.edit_place_error, Toast.LENGTH_LONG).show();
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

    @Override
    public void onTouchDown(MotionEvent event) {
    }

    @Override
    public void onTouchUp(MotionEvent event) {
        metaPlace.setGooglePlacesID(null);
        mAdapter.setBounds(getBounds(mMap.getCameraPosition().target, DISTANCE_IN_METERS));
        reverseGeocode(mMap.getCameraPosition().target);
        // set spinner for address text view
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
                        Toast.LENGTH_LONG).show();
            }
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
}
