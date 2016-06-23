package io.hypertrack.meta.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import io.hypertrack.meta.R;
import io.hypertrack.meta.adapter.AddPlaceAutocompleteAdapter;
import io.hypertrack.meta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.util.KeyboardUtils;
import io.hypertrack.meta.util.NetworkUtils;
import io.hypertrack.meta.util.images.LocationUtils;

/**
 * Created by piyush on 22/06/16.
 */
public class AddFavoritePlace extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private final String TAG = "AddFavoritePlace";
    private static final long INTERVAL_TIME = 5000;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Marker locationMarker;

    private Toolbar toolbar;
    private EditText addPlaceNameView;
    private EditText addPlaceAddressView;

    private RecyclerView mAutocompleteResults;
    private CardView mAutocompleteResultsLayout;
    private AddPlaceAutocompleteAdapter mAdapter;

    private MetaPlace metaPlace;

    private boolean placeSelected = false;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (!placeSelected) {
                String constraint = s != null ? s.toString() : "";
                mAdapter.getFilter().filter(constraint);
            } else {
                // If TextChanged because selectedPlace Address was set
                placeSelected = false;
            }
        }
    };

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(MetaPlace place) {

            placeSelected = true;

            updatePlaceData(place);

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Fetch Meta Place object passed with intent
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("meta_place")) {
//                metaPlace = (MetaPlace) intent.getSerializableExtra("meta_place");
            }
        }

        // For Testing purpose
        metaPlace = new MetaPlace();
        metaPlace.setLatitude(28.411877);
        metaPlace.setLongitude(77.041965);

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

    private void updatePlaceData(MetaPlace metaPlace) {

        try {
            if (!TextUtils.isEmpty(metaPlace.getAddress())) {
                addPlaceAddressView.setText(metaPlace.getAddress());

                // TODO: 23/06/16 Set Place Name from MetaPlace name variable
//                if (!TextUtils.isEmpty(metaPlace.getName())) {
//                    addPlaceNameView.setText(metaPlace.getName());
//                }

            } else if (metaPlace.getLatitude() != null && metaPlace.getLongitude() != null) {

                String address = LocationUtils.getNameFromLatLng(this, metaPlace.getLatitude(), metaPlace.getLongitude());
                if (!TextUtils.isEmpty(address)) {
                    addPlaceAddressView.setText(address);
                }
            }

            updateLocationMarker(new LatLng(metaPlace.getLatitude(), metaPlace.getLongitude()));

            LatLng latLng = new LatLng(metaPlace.getLatitude(), metaPlace.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .build();
    }

    private void getMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initNameAddressView() {
        addPlaceNameView = (EditText) findViewById(R.id.add_fav_place_name);
        addPlaceAddressView = (EditText) findViewById(R.id.add_fav_place_address);
        addPlaceAddressView.addTextChangedListener(mTextWatcher);
    }

    private void initAutocompleteResultsView() {
        mAutocompleteResults = (RecyclerView) findViewById(R.id.add_fav_places_results);
        mAutocompleteResultsLayout = (CardView) findViewById(R.id.add_fav_places_results_layout);

        mAdapter = new AddPlaceAutocompleteAdapter(this, mGoogleApiClient, mPlaceAutoCompleteListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);
        mAutocompleteResults.setLayoutManager(layoutManager);
        mAutocompleteResults.setAdapter(mAdapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        updatePlaceData(metaPlace);

        // TODO: 23/06/16 Detect Changed Lat,Lng on user interaction on Map & Update Place accordingly

        mAdapter.setBounds(getBounds(new LatLng(metaPlace.getLatitude(), metaPlace.getLongitude()), 10000));
    }

    private void updateLocationMarker(LatLng latLng) {
        if (mMap != null) {
            if (locationMarker == null) {
                locationMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_marker)));
            } else {
                locationMarker.setPosition(latLng);
            }
        }
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
        // TODO: 22/06/16 Add Loader while fetching Places Autocomplete data
//        ((Home) context).customLoader.setVisibility(View.GONE);
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
        // TODO: 23/06/16 Add Save Btn functionality
        finish();
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
}
