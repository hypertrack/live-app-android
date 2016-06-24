package io.hypertrack.meta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
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

import io.hypertrack.meta.R;
import io.hypertrack.meta.adapter.AddPlaceAutocompleteAdapter;
import io.hypertrack.meta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.UserStore;
import io.hypertrack.meta.util.KeyboardUtils;
import io.hypertrack.meta.util.NetworkUtils;
import io.hypertrack.meta.util.SuccessErrorCallback;
import io.hypertrack.meta.util.images.LocationUtils;

/**
 * Created by piyush on 22/06/16.
 */
public class AddFavoritePlace extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, TouchableWrapper.TouchActionDown, TouchableWrapper.TouchActionUp {

    public static final int FAVORITE_PLACE_REQUEST_CODE = 100;

    private final String TAG = "AddFavoritePlace";
    private static final int DISTANCE_IN_METERS = 10000;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;

    private EditText addPlaceNameView;
    private EditText addPlaceAddressView;

    private RecyclerView mAutocompleteResults;
    private CardView mAutocompleteResultsLayout;
    private AddPlaceAutocompleteAdapter mAdapter;

    private ProgressDialog mProgressDialog;

    private MetaPlace metaPlace;

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
            mAdapter.getFilter().filter(constraint);
        }
    };

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(MetaPlace place) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 18.0f));
            mAdapter.setBounds(getBounds(place.getLatLng(), DISTANCE_IN_METERS));

            KeyboardUtils.hideKeyboard(AddFavoritePlace.this, addPlaceAddressView);

            //Hide the Results List on Selection
            mAutocompleteResults.setVisibility(View.GONE);
            mAutocompleteResultsLayout.setVisibility(View.GONE);
        }

        @Override
        public void OnError() {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_favorite_place);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fetch Meta Place object passed with intent
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("meta_place")) {
                metaPlace = (MetaPlace) intent.getSerializableExtra("meta_place");
            }
        }

        // Initialize GoogleApiClient & UI Views
        this.initGoogleClient();
        this.getMap();
        this.initNameAddressView();
        this.initAutocompleteResultsView();

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
        addPlaceAddressView = (EditText) findViewById(R.id.add_fav_place_address);

        if (metaPlace.getName() != null && !metaPlace.getName().isEmpty()) {
            addPlaceNameView.setText(metaPlace.getName());
        }

        if (metaPlace.getAddress() != null && !metaPlace.getAddress().isEmpty()) {
            addPlaceAddressView.setText(metaPlace.getAddress());
        } else {
            reverseGeocode(metaPlace.getLatLng());
        }

        addPlaceAddressView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addPlaceAddressView.addTextChangedListener(mTextWatcher);
                } else {
                    addPlaceAddressView.addTextChangedListener(null);
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

        com.google.android.gms.maps.model.LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(new LatLng(minLat, minLong));
        b.include(new LatLng(maxLat, maxLong));
        LatLngBounds bounds = b.build();

        return bounds;
    }

    public void onSaveButtonClicked(MenuItem v) {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        metaPlace.setName(addPlaceNameView.getText().toString());

        if (metaPlace.getName() == null || metaPlace.getName().isEmpty()) {
            Toast.makeText(this, R.string.place_name_error, Toast.LENGTH_LONG).show();
            return;
        }

        if (metaPlace.isHome()) {
            if (user.hasHome() && user.getHome().isEqualPlace(metaPlace)) {
                Toast.makeText(this, R.string.home_exists_error, Toast.LENGTH_LONG).show();
                return;
            }
        } else if (metaPlace.isWork()) {
            if (user.hasWork() && user.getWork().isEqualPlace(metaPlace)) {
                Toast.makeText(this, R.string.work_exists_error, Toast.LENGTH_LONG).show();
                return;
            }
        }

        metaPlace.setAddress(addPlaceAddressView.getText().toString());
        metaPlace.setLatLng(mMap.getCameraPosition().target);

        if (user.isSynced(metaPlace)) {
            this.editPlace();
        } else {
            this.addPlace();
        }
    }

    private void addPlace() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Adding place");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        UserStore.sharedStore.addPlace(metaPlace, new SuccessErrorCallback() {
            @Override
            public void OnSuccess() {
                mProgressDialog.dismiss();
                broadcastResultIntent();
                finish();
            }

            @Override
            public void OnError() {
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
                mProgressDialog.dismiss();
                broadcastResultIntent();
                finish();
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
                showEditPlaceError();
            }
        });
    }

    private void broadcastResultIntent() {
        Intent intent=new Intent();
        setResult(FAVORITE_PLACE_REQUEST_CODE, intent);
    }

    private void showAddPlaceError() {
        Toast.makeText(this, R.string.add_place_error, Toast.LENGTH_LONG).show();
    }

    private void showEditPlaceError() {
        Toast.makeText(this, R.string.edit_place_error, Toast.LENGTH_LONG).show();
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

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private void reverseGeocode(LatLng latLng) {
        addPlaceAddressView.setText(LocationUtils.getNameFromLatLng(this, latLng.latitude, latLng.longitude));
    }

    @Override
    public void onTouchDown(MotionEvent event) {

    }

    @Override
    public void onTouchUp(MotionEvent event) {
        mAdapter.setBounds(getBounds(mMap.getCameraPosition().target, DISTANCE_IN_METERS));
        reverseGeocode(mMap.getCameraPosition().target);
    }
}
