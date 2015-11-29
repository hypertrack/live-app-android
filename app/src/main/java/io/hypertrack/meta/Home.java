package io.hypertrack.meta;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.hypertrack.apps.assettracker.HyperTrack;
import com.hypertrack.apps.assettracker.model.HTTripStatusCallback;
import com.hypertrack.apps.assettracker.service.HTTransmitterService;

import butterknife.internal.ListenerClass;
import io.hypertrack.meta.model.ETAInfo;
import io.hypertrack.meta.model.UserTrip;
import io.hypertrack.meta.network.HTCustomGetRequest;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.HTConstants;

public class Home extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = AppCompatActivity.class.getSimpleName();

    private GoogleMap mMap;

    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView mAutocompleteView;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    private LatLng currentLocation;
    private Marker currentLocationMarker;
    private LatLng destinationLocation;
    private Marker destinationLocationMarker;
    private Button shareEtaButton;
    private HTTransmitterService transmitterService;
    private String userId;
    private String tripId;
    private ProgressDialog mProgressDialog;

    private InputMethodManager mIMEMgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mIMEMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
        boolean isUserOnboard = settings.getBoolean("isUserOnboard", false);

        if (!isUserOnboard) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete_places);

        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                null);


        mAutocompleteView.setAdapter(mAdapter);

        shareEtaButton = (Button) findViewById(R.id.shareEtaButton);
        setUpShareEtaButton();
        setUpHyperTrackSDK();
    }

    private void setUpHyperTrackSDK() {
        HyperTrack.setAPIKey("cb50db86ff63f556f7856d7690ebc305a7a27c69");
        HyperTrack.setLoggable(true);
        //Setup order details

        transmitterService = HTTransmitterService.getInstance(this);
        int userIdInt = getUserIdFromPreferences();
        userId = String.valueOf(userIdInt);
    }

    private int getUserIdFromPreferences() {
        SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
        return settings.getInt(HTConstants.USER_ID, -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            if (tripId == null)
                tripId = getTripFromSharedPreferences();

            if (tripId.equalsIgnoreCase("None"))
                return true;

            transmitterService.endTrip(Integer.valueOf(tripId), new HTTripStatusCallback() {
                @Override
                public void onError(Exception e) {
                    Toast.makeText(Home.this, "Inside OnError", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(Home.this, "Trip Stopped :)", Toast.LENGTH_LONG).show();

                    SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(HTConstants.TRIP_STATUS, false);
                    editor.putString(HTConstants.TRIP_URI, "None");
                    resetViewsOnEndTrip();
                    editor.commit();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void resetViewsOnEndTrip() {

        shareEtaButton.setVisibility(View.GONE);

        mAutocompleteView.setVisibility(View.VISIBLE);
        mAutocompleteView.setText("");

        if ( destinationLocationMarker != null) {
            destinationLocationMarker.remove();
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

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.d(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.d(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }


            // Get the Place object from the buffer.
            final Place place = places.get(0);

           /* // Format details of the place for display and show it in a TextView.
            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                    place.getId(), place.getAddress(), place.getPhoneNumber(),
                    place.getWebsiteUri()));

            // Display the third party attributions if set.
            final CharSequence thirdPartyAttribution = places.getAttributions();
            if (thirdPartyAttribution == null) {
                mPlaceDetailsAttribution.setVisibility(View.GONE);
            } else {
                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
            }*/

            Log.i(TAG, "Place details received: " + place.getName());
            addMarkerToSelectedDestination(place.getLatLng());

            places.release();

        }
    };

    private void addMarkerToCurrentLocation() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            Log.v(TAG, "Location: " + location);

            if (location != null) {

                if (currentLocationMarker != null)
                    currentLocationMarker.remove();

                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                currentLocationMarker.showInfoWindow();

                CameraPosition cameraPosition =
                        new CameraPosition.Builder()
                                .target(currentLocation)
                                .zoom(mMap.getCameraPosition().zoom >= 10 ? mMap.getCameraPosition().zoom : 10)
                                .build();

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

        }


        //addMarkerToSelectedDestination(new LatLng(19.158004, 72.991996));
    }

    private void addMarkerToSelectedDestination(LatLng destinationLocation) {

        if (destinationLocation != null) {
            // Add a marker in destination and move the camera
            this.destinationLocation = destinationLocation;
            destinationLocationMarker = mMap.addMarker(new MarkerOptions().position(this.destinationLocation).title("Your destination"));
            destinationLocationMarker.showInfoWindow();

            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(destinationLocation)
                            .zoom(mMap.getCameraPosition().zoom >= 10 ? mMap.getCameraPosition().zoom : 10)
                            .build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

        mIMEMgr.hideSoftInputFromWindow(mAutocompleteView.getWindowToken(), 0);
        getEtaForDestination();
    }

    private void getEtaForDestination() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String url = "https://meta-api-staging.herokuapp.com/api/v1/eta/?origin="
                + currentLocation.latitude + "," + currentLocation.longitude
                + "&destination=" + destinationLocation.latitude + "," + destinationLocation.longitude;

        Log.d(TAG, "Url: " + url);

        HTCustomGetRequest<ETAInfo[]> requestObject =
                new HTCustomGetRequest<ETAInfo[]>(url, ETAInfo[].class, new Response.Listener<ETAInfo[]>() {
                    @Override
                    public void onResponse(ETAInfo[] response) {
                        Log.d("Response", "Inside onResponse");
                        showShareEtaButton(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
                        Log.d("Response", "Inside onError");
                        Toast.makeText(Home.this, "There was an error fetching ETA. Please try again.", Toast.LENGTH_LONG).show();
                        mAutocompleteView.setText("");
                        mIMEMgr.showSoftInputFromInputMethod(mAutocompleteView.getWindowToken(), 0);
                    }
                });

        MetaApplication.getInstance().addToRequestQueue(requestObject);

    }

    private void showShareEtaButton(ETAInfo[] etaInfoList) {

        int eta = etaInfoList[0].getDuration();
        int etaInMinutes = eta / 60;
        shareEtaButton.setText(etaInMinutes + " minutes - " + "SHARE ETA");
        shareEtaButton.setVisibility(View.VISIBLE);
        mAutocompleteView.setVisibility(View.GONE);

        mProgressDialog.dismiss();
    }

    private void setUpShareEtaButton() {
        shareEtaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initShareEtaFlow();
            }
        });
    }

    private void initShareEtaFlow() {

        if (getTripStatusFromSharedPreferences()) {
            String uri = getTripUriFromSharedPreferences();
            if (TextUtils.isEmpty(uri) || uri.equalsIgnoreCase("None")) {
                return;
            }
            shareUrl(uri);
        } else {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            startTrip();
        }
    }

    private void startTrip() {
        if (TextUtils.isEmpty(userId) || userId.equals("-1")) {
            Toast.makeText(this, "User id not found", Toast.LENGTH_LONG).show();
            return;
        }

        transmitterService.setOrderDetails(userId);
        transmitterService.startTrip(new HTTripStatusCallback() {
            @Override
            public void onError(Exception e) {
                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();
            }

            @Override
            public void onSuccess(String id) {
                Toast.makeText(Home.this, "Trip id: " + id, Toast.LENGTH_LONG).show();
                tripId = id;
                getShareEtaURL(id);
                saveTripInSharedPreferences(id);
            }
        });
    }

    public void saveTripInSharedPreferences(String tripId) {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(HTConstants.TRIP_ID, tripId);
        editor.putBoolean(HTConstants.TRIP_STATUS, true);
        editor.commit();
    }

    public String getTripFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(HTConstants.TRIP_ID, "None");
    }

    public String getTripUriFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(HTConstants.TRIP_URI, "None");
    }

    public boolean getTripStatusFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean(HTConstants.TRIP_STATUS, false);
    }

    public void saveTripUriInSharedPreferences(String tripUri) {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(HTConstants.TRIP_URI, tripUri);
        editor.commit();
    }


    private void getShareEtaURL(String tripId) {

        UserTrip userTrip = new UserTrip(userId, tripId);
        Gson gson = new Gson();
        String jsonBody = gson.toJson(userTrip);

        String url = "https://meta-api-staging.herokuapp.com/api/v1/trips/";

        HTCustomPostRequest<UserTrip> requestObject = new HTCustomPostRequest<UserTrip>(1, url,
                jsonBody, UserTrip.class,
                new Response.Listener<UserTrip>() {
                    @Override
                    public void onResponse(UserTrip response) {
                        Toast.makeText(Home.this, "URL: " + response.getTrackUri(), Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                        String uri = response.getTrackUri();
                        saveTripUriInSharedPreferences(uri);
                        shareUrl(uri);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Home.this, "Inside Error", Toast.LENGTH_LONG).show();
                    }
                }
        );

        MetaApplication.getInstance().addToRequestQueue(requestObject);
    }

    private void shareUrl(String uri) {

        String shareBody = "Track me @ https://meta-api-staging.herokuapp.com" + uri;

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(sharingIntent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "mGoogleApiClient is connected");
        addMarkerToCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
