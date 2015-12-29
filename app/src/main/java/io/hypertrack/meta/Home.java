package io.hypertrack.meta;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import com.google.android.gms.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
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
import com.hypertrack.apps.assettracker.model.HTTripParams;
import com.hypertrack.apps.assettracker.model.HTTripParamsBuilder;
import com.hypertrack.apps.assettracker.model.HTTripStatusCallback;
import com.hypertrack.apps.assettracker.service.HTTransmitterService;

import butterknife.internal.ListenerClass;
import io.hypertrack.meta.model.CustomAddress;
import io.hypertrack.meta.model.ETAInfo;
import io.hypertrack.meta.model.MetaLocation;
import io.hypertrack.meta.model.UserTrip;
import io.hypertrack.meta.network.HTCustomGetRequest;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.HTConstants;

public class Home extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = AppCompatActivity.class.getSimpleName();

    private GoogleMap mMap;

    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView mAutocompleteView;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    private LatLngBounds mBounds;
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
    private Button endTripButton;
    private static final long INTERVAL_TIME = 5000;
    private CustomAddress customAddress;
    private String endPlaceId;
    private Button addAddressButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        mIMEMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        SharedPreferences settings = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
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

        addAddressButton = (Button) findViewById(R.id.customAddress_button);

        addAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCustomAddressFromTheUser();
            }
        });

        shareEtaButton = (Button) findViewById(R.id.shareEtaButton);
        endTripButton = (Button) findViewById(R.id.endtrip_button);
        endTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tripId == null)
                    tripId = getTripFromSharedPreferences();

                if (tripId.equalsIgnoreCase("None"))
                    return;

                transmitterService.endTrip(new HTTripStatusCallback() {
                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(Home.this, "Inside OnError", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(String s) {
                        Toast.makeText(Home.this, "Trip Stopped :)", Toast.LENGTH_LONG).show();
                        endTrip();
                    }
                });
            }
        });

        setUpShareEtaButton();
        setUpHyperTrackSDK();
        setUpInitView();
    }

    private void setUpInitView() {
        if (getTripStatusFromSharedPreferences()) {
            mAutocompleteView.setVisibility(View.GONE);
            addAddressButton.setVisibility(View.GONE);
            endTripButton.setVisibility(View.VISIBLE);

            if (!TextUtils.equals(getTripEtaFromSharedPreferences(), "None")) {
                shareEtaButton.setText(getTripEtaFromSharedPreferences() + " minutes - " + "SHARE ETA");
                shareEtaButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setUpHyperTrackSDK() {
        HyperTrack.setPublishableApiKey("pk_65801d4211efccf3128d74101254e7637e655356");
        HyperTrack.setLoggable(true);
        //Setup order details

        transmitterService = HTTransmitterService.getInstance(this);
        int userIdInt = getUserIdFromPreferences();
        if (userIdInt != -1)
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

            transmitterService.endTrip(new HTTripStatusCallback() {
                @Override
                public void onError(Exception e) {
                    Toast.makeText(Home.this, "Inside OnError", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(Home.this, "Trip Stopped :)", Toast.LENGTH_LONG).show();
                    endTrip();
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void endTrip() {

        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(HTConstants.TRIP_STATUS, false);
        editor.putString(HTConstants.TRIP_URI, "None");
        editor.putString(HTConstants.TRIP_ETA, "None");
        editor.commit();

        resetViewsOnEndTrip();
    }

    private void resetViewsOnEndTrip() {

        shareEtaButton.setVisibility(View.GONE);
        endTripButton.setVisibility(View.GONE);

        mAutocompleteView.setVisibility(View.VISIBLE);
        addAddressButton.setVisibility(View.VISIBLE);
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

            populateCustomAddress(place);

            addMarkerToSelectedDestination(place.getLatLng());
            places.release();

        }
    };

    private void populateCustomAddress(Place place) {

        //name and location are compulsary

        customAddress = new CustomAddress();

        if (place.getLatLng() != null) {
            double[] ll = {place.getLatLng().longitude, place.getLatLng().latitude};
            MetaLocation metaLocation = new MetaLocation();
            metaLocation.setType("Point");
            metaLocation.setCoordinates(ll);
            customAddress.setLocation(metaLocation);
        }

        if (!TextUtils.isEmpty(place.getId()))
        customAddress.setGooglePlacesId(place.getId());

        if (!TextUtils.isEmpty(place.getName()))
        customAddress.setName(place.getName().toString());

        if (!TextUtils.isEmpty(place.getAddress()))
        customAddress.setAddress(place.getAddress().toString());

    }

    private void addMarkerToCurrentLocation() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            Log.v(TAG, "Location: " + location);

            if (location != null) {

                if (currentLocationMarker != null)
                    currentLocationMarker.remove();

                Log.e(TAG, "Setting bounds");
                mBounds = getBounds(location, 100000);

                Log.e(TAG, "Setting adapter");
                mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, mBounds,
                        null);


                mAutocompleteView.setAdapter(mAdapter);

                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                currentLocationMarker.showInfoWindow();

                if (currentLocation != null && destinationLocation != null) {

                    LatLngBounds.Builder b = new LatLngBounds.Builder();
                    b.include(currentLocation);
                    b.include(destinationLocation);
                    LatLngBounds bounds = b.build();

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300);
                    mMap.animateCamera(cu, 1000, null);

                    return;

                }

                CameraPosition cameraPosition =
                        new CameraPosition.Builder()
                                .target(currentLocation)
                                .zoom(mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16)
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

            if (destinationLocationMarker != null) {
                destinationLocationMarker.remove();
            }

            destinationLocationMarker = mMap.addMarker(new MarkerOptions().position(this.destinationLocation).title("Your destination"));
            destinationLocationMarker.showInfoWindow();


            LatLngBounds.Builder b = new LatLngBounds.Builder();
            b.include(currentLocation);
            b.include(this.destinationLocation);
            LatLngBounds bounds = b.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300);
            mMap.animateCamera(cu, 3000, null);

            /*
            CameraPosition cameraPosition =
                    new CameraPosition.Builder()
                            .target(destinationLocation)
                            .zoom(mMap.getCameraPosition().zoom >= 10 ? mMap.getCameraPosition().zoom : 10)
                            .build();

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            */

        }

        mIMEMgr.hideSoftInputFromWindow(mAutocompleteView.getWindowToken(), 0);
        getEtaForDestination();
        updateMetaEndPlaceId();
    }

    private void updateMetaEndPlaceId() {

        String url = "https://meta-api-staging.herokuapp.com/api/v1/places/";
        HTConstants.setPublishableApiKey(getTokenFromSharedPreferences());

        Log.d(TAG, "Url: " + url + "Token: " + getTokenFromSharedPreferences());

        Gson gson = new Gson();
        String jsonBody = gson.toJson(customAddress);


        HTCustomPostRequest<CustomAddress> requestObject = new HTCustomPostRequest<CustomAddress>(1, url,
                jsonBody, CustomAddress.class,
                new Response.Listener<CustomAddress>() {
                    @Override
                    public void onResponse(CustomAddress response) {
                        endPlaceId = response.getHypertrackPlaceId();
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

    private void getEtaForDestination() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting ETA for the selected destination");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String url = "https://meta-api-staging.herokuapp.com/api/v1/eta/?origin="
                + currentLocation.latitude + "," + currentLocation.longitude
                + "&destination=" + destinationLocation.latitude + "," + destinationLocation.longitude;

        HTConstants.setPublishableApiKey(getTokenFromSharedPreferences());

        Log.d(TAG, "Url: " + url + "Token: " + getTokenFromSharedPreferences());

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

        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(HTConstants.TRIP_ETA, String.valueOf(etaInMinutes));
        editor.commit();

        shareEtaButton.setText(etaInMinutes + " minutes - " + "SHARE ETA");
        shareEtaButton.setVisibility(View.VISIBLE);
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
            mProgressDialog.setMessage("Fetching URL to share... ");
            mProgressDialog.show();
            startTrip();
        }
    }

    private void startTrip() {

        SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
        String courier_id = settings.getString(HTConstants.HYPERTRACK_COURIER_ID, "None");

        if (TextUtils.isEmpty(courier_id)) {
            return;
        }

        if (TextUtils.isEmpty(endPlaceId)) {
            return;
        }

        int courierId = Integer.valueOf(courier_id);
        int endPlace = Integer.valueOf(endPlaceId);

        Log.d(TAG, "courier_id: " + courier_id);

        if (TextUtils.equals(courier_id, "None")) {
            Toast.makeText(this, "User id not found", Toast.LENGTH_LONG).show();
            return;
        }

        HTTripParamsBuilder htTripParamsBuilder = new HTTripParamsBuilder();
        HTTripParams htTripParams = htTripParamsBuilder.setCourierId(courierId)
                .setEndPlaceId(endPlace)
                .createHTTripParams();

        transmitterService.startTrip(htTripParams, new HTTripStatusCallback() {
            @Override
            public void onError(Exception e) {
                Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();
            }

            @Override
            public void onSuccess(String id) {
                //Toast.makeText(Home.this, "Trip id: " + id, Toast.LENGTH_LONG).show();
                tripId = id;
                getShareEtaURL(id);
                saveTripInSharedPreferences(id);

                mAutocompleteView.setVisibility(View.GONE);
                addAddressButton.setVisibility(View.GONE);
                endTripButton.setVisibility(View.VISIBLE);
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

    public String getTokenFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(HTConstants.USER_AUTH_TOKEN, "None");
    }

    public String getTripEtaFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(HTConstants.TRIP_ETA, "None");
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
                        //Toast.makeText(Home.this, "URL: " + response.toString(), Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();

                        String uri = response.getShortUrl();
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

        String shareBody = "Track me @ " + uri;

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(sharingIntent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "mGoogleApiClient is connected");
        addMarkerToCurrentLocation();
        requestForLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void requestForLocationUpdates() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL_TIME);
        locationRequest.setFastestInterval(INTERVAL_TIME);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //addMarkerToCurrentLocation();
    }


    private static final int CUSTOM_ADDRESS_DATA = 101;

    private void getCustomAddressFromTheUser() {
        Intent intent = new Intent(this, AddAddress.class);
        startActivityForResult(intent, CUSTOM_ADDRESS_DATA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == CUSTOM_ADDRESS_DATA) {
            CustomAddress ca = (CustomAddress)data.getSerializableExtra("custom_address");
            if (ca != null) {
                customAddress = ca;
                Log.d(TAG, ca.toString());

                addMarkerToSelectedDestination(ca.getLocation().getLatLng());
            }
        }
    }

    private LatLngBounds getBounds(Location location, int mDistanceInMeters ){

        double latRadian = Math.toRadians(location.getLatitude());

        double degLatKm = 110.574235;
        double degLongKm = 110.572833 * Math.cos(latRadian);
        double deltaLat = mDistanceInMeters / 1000.0 / degLatKm;
        double deltaLong = mDistanceInMeters / 1000.0 / degLongKm;

        double minLat = location.getLatitude() - deltaLat;
        double minLong = location.getLongitude() - deltaLong;
        double maxLat = location.getLatitude() + deltaLat;
        double maxLong = location.getLongitude() + deltaLong;

        return new LatLngBounds(new LatLng(minLat, minLong), new LatLng(maxLat, maxLong));

    }

}
