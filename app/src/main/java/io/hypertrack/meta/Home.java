package io.hypertrack.meta;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hypertrack.android.sdk.base.network.HTConsumerClient;
import com.hypertrack.android.sdk.base.utils.HTCircleImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.hypertrack.lib.httransmitter.HyperTrack;
import io.hypertrack.lib.httransmitter.model.HTTrip;
import io.hypertrack.lib.httransmitter.model.HTTripParams;
import io.hypertrack.lib.httransmitter.model.HTTripParamsBuilder;
import io.hypertrack.lib.httransmitter.model.HTTripStatusCallback;
import io.hypertrack.lib.httransmitter.service.HTTransmitterService;
import io.hypertrack.meta.model.CustomAddress;
import io.hypertrack.meta.model.DeviceInfo;
import io.hypertrack.meta.model.ETAInfo;
import io.hypertrack.meta.model.ETARecipients;
import io.hypertrack.meta.model.MetaLocation;
import io.hypertrack.meta.model.UserTrip;
import io.hypertrack.meta.network.HTCustomGetRequest;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.HTConstants;
import io.hypertrack.meta.util.PhoneUtils;

public class Home extends AppCompatActivity implements ResultCallback<Status>, LocationListener, OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static final int LOITERING_DELAY_MS = 30000;
    public static final int EXPIRATION_DURATION = 600000;
    private static final String TAG = AppCompatActivity.class.getSimpleName();
    private static final String GEOFENCE_REQUEST_ID = "geofence";
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;
    private static final int REQUEST_SHARE_CONTACT_CODE = 1;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));
    private static final long INTERVAL_TIME = 5000;
    private static final int CUSTOM_ADDRESS_DATA = 101;
    protected GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
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
    private Handler handler;
    private InputMethodManager mIMEMgr;
    private Button endTripButton;
    private CustomAddress customAddress;
    private String endPlaceId;
    private Button addAddressButton;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private SupportMapFragment mMapFragment;
    private int etaInMinutes;

    final Runnable updateTask=new Runnable() {
        @Override
        public void run() {

            transmitterService.refreshTrip(tripId, new HTTripStatusCallback() {
                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Inside refresh trip error");
                }

                @Override
                public void onSuccess(HTTrip htTrip) {
                    Log.v(TAG, htTrip.toString());
                    updateETA(htTrip.getEstimatedTripEndTime());
                }
            });

            handler.postDelayed(this,60000);
        }
    };

    Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            profilePicBitmap = bitmap;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            profilePicBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_profile_pic);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            profilePicBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_profile_pic);
        }
    };

    private String metaId;
    private HTConsumerClient mHyperTrackClient;
    private String estimateArrivalOfTime;
    private Bitmap profilePicBitmap;
    //private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS = ;
    private HTCircleImageView profileViewProfileImage;
    private View customMarkerView;
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
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            boolean result = intent.getBooleanExtra("end_trip", false);

            if (result)
                endTrip();//endTripClicked();

            Log.d("receiver", "Got message: " + result);
        }
    };

    public static int getTheEstimatedTime(String estimatedTime) {

        String currentTime = getCurrentTime();

        long seconds = 0;
        int minutes = 0;

        String currentTimeString = currentTime.substring(0,currentTime.length()-5);
        String estimatedTimeString = estimatedTime.substring(0,estimatedTime.length()-1);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        try {

            Date startDate = df.parse(currentTimeString);
            Date endDate = df.parse(estimatedTimeString);
            seconds = (endDate.getTime() - startDate.getTime())/1000;
            minutes = (int)seconds/60;

        } catch(ParseException ex) {
            ex.printStackTrace();
        }

        if(minutes < 0) minutes=0;

        return minutes; // return in seconds - Check duration
    }

    private static String getCurrentTime() {

        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String cDateTime=dateFormat.format(new Date());
        return  cDateTime;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);



        /*
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            if (packageInfo.packageName.equals("io.hypertrack.meta.debug")) {
                setTitle(getTitle() + " (DEBUG)");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        */

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initGoogleClient();

        mIMEMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        SharedPreferences settings = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean isUserOnboard = settings.getBoolean("isUserOnboard", false);

        if (!isUserOnboard) {
            startActivity(new Intent(this, Login.class));
            finish();
        }

        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

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
                endTripClicked();
            }
        });

        /*
        String picUrl = getUserProfilePicFromSharedPreferences();
        Picasso.with(this)
                .load(picUrl)
                .into(target);
        */

        if (!TextUtils.equals("None", getImageToPreferences())) {
            profilePicBitmap = decodeToBase64(getImageToPreferences());
        } else {
            profilePicBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.default_profile_pic);
        }

        setUpShareEtaButton();
        setUpHyperTrackSDK();
        setUpInitView();
        updateDeviceInfo();
    }

    private String getImageToPreferences() {
        SharedPreferences myPrefrence = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return myPrefrence.getString(HTConstants.USER_PROFILE_PIC_ENCODED, "None");
    }

    public static Bitmap decodeToBase64(String decodeImageString) {
        byte[] decodedByte = Base64.decode(decodeImageString, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    private void initGoogleClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

    }

    private void setUpInitView() {
        if (getTripStatusFromSharedPreferences()) {

            mAutocompleteView.setVisibility(View.GONE);
            addAddressButton.setVisibility(View.GONE);
            endTripButton.setVisibility(View.VISIBLE);
            shareEtaButton.setVisibility(View.VISIBLE);



            setUpHyperTrackSDK();
            tripId = getTripFromSharedPreferences();
            String etaString = getTripEtaFromSharedPreferences();
            if (!TextUtils.equals(etaString,"None")) {
                etaInMinutes = Integer.valueOf(etaString);
            }
            updateShareETAButton();
            initETAUpateTask();
        }

        //initCustomMarkerView();
    }

    private void initCustomMarkerView() {

        customMarkerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_car_marker, null);
        profileViewProfileImage = (HTCircleImageView) customMarkerView.findViewById(R.id.profile_image);

        SharedPreferences settings = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String urlProfilePic = settings.getString(HTConstants.USER_PROFILE_PIC, null);

        if(!TextUtils.isEmpty(urlProfilePic)) {
            Picasso.with(this)
                    .load(urlProfilePic)
                    .error(R.drawable.default_profile_pic)
                    .into(profileViewProfileImage);
        }

    }

    private Bitmap createDrawableFromView(Context context, View view) {

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

    private void setUpHyperTrackSDK() {
        HyperTrack.setPublishableApiKey(BuildConfig.API_KEY);
        HyperTrack.setLogLevel(Log.DEBUG);
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
        //getMenuInflater().inflate(R.menu.menu_home, menu);
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
            endTripClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void endTrip() {

        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(HTConstants.TRIP_STATUS, false);
        editor.putString(HTConstants.TRIP_SHARE_URI, "None");
        editor.putString(HTConstants.TRIP_ETA, "None");
        editor.putString(HTConstants.TRIP_DESTINATION, "None");
        editor.apply();

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        resetViewsOnEndTrip();
    }

    private void resetViewsOnEndTrip() {

        if (handler != null)
         handler.removeCallbacks(updateTask);

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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        int paddingInDpForTop = 175;
        int paddingInDpForBottom = 50;
        final float scale = getResources().getDisplayMetrics().density;
        int paddingInPxForTop = (int) (paddingInDpForTop * scale + 0.5f);
        int paddingInPxForBottom = (int) (paddingInDpForBottom * scale + 0.5f);

        mMap.setPadding(0,paddingInPxForTop,0,paddingInPxForBottom);

        if (getTripStatusFromSharedPreferences()) {
            destinationLocation = getTripDestinationFromSharedPreferences();
            if (destinationLocation != null) {
                Log.v(TAG, "Destination Latlng: " + destinationLocation);
                addDestinationMarker(destinationLocation);
            }
        }
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

                mBounds = getBounds(location, 100000);

                mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, mBounds,
                        null);

                mAutocompleteView.setAdapter(mAdapter);

                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                updateCurrentMarkerLocation(location);
                //currentLocationMarker.showInfoWindow();

                if (currentLocation != null && destinationLocation != null) {

                    LatLngBounds.Builder b = new LatLngBounds.Builder();
                    b.include(currentLocation);
                    b.include(destinationLocation);
                    LatLngBounds bounds = b.build();

                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
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

    private void addDestinationMarker(LatLng destinationLocation) {

        this.destinationLocation = destinationLocation;

        updateDestinationMarker();

        if (currentLocation != null && destinationLocation != null) {
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            b.include(currentLocation);
            b.include(this.destinationLocation);
            LatLngBounds bounds = b.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                    100);
            mMap.moveCamera(cu);
        }

        saveTripDestinationSharedPreferences(destinationLocation);

        if (mGeofenceList == null)
            mGeofenceList = new ArrayList<Geofence>();

        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(GEOFENCE_REQUEST_ID)
                .setCircularRegion(
                        destinationLocation.latitude,
                        destinationLocation.longitude,
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(LOITERING_DELAY_MS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build());

        mGeofencePendingIntent = getGeofencePendingIntent();

    }

    private void updateDestinationMarker() {
        if (destinationLocationMarker != null) {
            destinationLocationMarker.remove();
        }

        View markerView = createCustomMarkerView();

        destinationLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(this.destinationLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,markerView))));
    }

    private View createCustomMarkerView() {

        View marker = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_destination_marker_layout, null);
        TextView etaTxt = (TextView) marker.findViewById(R.id.eta_txt);

        int eta = etaInMinutes;
        if (eta != 0) {
            etaTxt.setText(eta + "m");
        }

        return marker;
    }

    private void addMarkerToSelectedDestination(LatLng destinationLocation) {

        if (destinationLocation != null) {
            // Add a marker in destination and move the camera
            addDestinationMarker(destinationLocation);
        }

        mIMEMgr.hideSoftInputFromWindow(mAutocompleteView.getWindowToken(), 0);
        getEtaForDestination();
        updateMetaEndPlaceId();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private void updateDeviceInfo() {

        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String token = sharedpreferences.getString(HTConstants.GCM_REGISTRATION_TOKEN, "None");

        SharedPreferences settings = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int userId =  settings.getInt(HTConstants.USER_ID, -1);

        if (TextUtils.equals("None", token) || userId == -1){
            return;
        }

        String url = HTConstants.API_ENDPOINT + "/api/v1/users/"+ userId +"/add_device/";
        HTConstants.setPublishableApiKey(getTokenFromSharedPreferences());

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setRegistrationId(token);
        deviceInfo.setDeviceId(deviceId);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(deviceInfo);

        Log.d(TAG, "Device Info" + deviceInfo.toString());

        HTCustomPostRequest<String> requestObject = new HTCustomPostRequest<String>(1, url,
                jsonBody, String.class,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //200, 201
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //400
                    }
                }
        );

        MetaApplication.getInstance().addToRequestQueue(requestObject);

    }

    private void updateMetaEndPlaceId() {

        String url = HTConstants.API_ENDPOINT + "/api/v1/places/";
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

        String url = HTConstants.API_ENDPOINT + "/api/v1/eta/?origin="
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
                        shareEtaButton.setVisibility(View.INVISIBLE);
                        mAutocompleteView.setText("");
                        mIMEMgr.showSoftInputFromInputMethod(mAutocompleteView.getWindowToken(), 0);
                    }
                });

        MetaApplication.getInstance().addToRequestQueue(requestObject);

    }

    private void saveTripEtaInSharePreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(HTConstants.TRIP_ETA, String.valueOf(etaInMinutes));
        editor.commit();
    }

    private void showShareEtaButton(ETAInfo[] etaInfoList) {

        int eta = etaInfoList[0].getDuration();
        etaInMinutes = eta / 60;

        saveTripEtaInSharePreferences();

        if (destinationLocationMarker != null) {
            updateDestinationMarker();
        }

        updateShareETAButton();

        mProgressDialog.dismiss();
    }

    private void updateShareETAButton() {
        shareEtaButton.setText("Send ETA (" + etaInMinutes + " mins)");
        shareEtaButton.setVisibility(View.VISIBLE);
    }

    private void updateETA(String estimatedTripEndTime) {

        etaInMinutes = getTheEstimatedTime(estimatedTripEndTime);
        saveTripEtaInSharePreferences();

        if (destinationLocationMarker != null) {
            updateDestinationMarker();
        }

        shareEtaButton.setText("Send ETA (" + etaInMinutes + " mins)");
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
            String uri = getTripShareUrlInSharedPreferences();
            if (TextUtils.isEmpty(uri) || uri.equalsIgnoreCase("None")) {
                return;
            }
            //shareUrl();
            shareUrlViaShare();
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
            public void onSuccess(HTTrip tripDetails) {
                //Toast.makeText(Home.this, "Trip id: " + id, Toast.LENGTH_LONG).show();
                tripId = String.valueOf(tripDetails.getId());
                estimateArrivalOfTime = tripDetails.getEstimatedTripEndTime();
                getShareEtaURL(tripId);
                saveTripInSharedPreferences(tripId);
                requestForGeofenceSetup();

                initETAUpateTask();
                mAutocompleteView.setVisibility(View.GONE);
                addAddressButton.setVisibility(View.GONE);
                endTripButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initETAUpateTask(){

        setTimerForEtaUpdate();
    }

    private void setTimerForEtaUpdate() {
        handler = new Handler();
        handler.postDelayed(updateTask, 0);
    }

    public void saveTripInSharedPreferences(String tripId) {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(HTConstants.TRIP_ID, tripId);
        editor.putBoolean(HTConstants.TRIP_STATUS, true);
        editor.apply();
    }

    public void saveTripDestinationSharedPreferences(LatLng destinationLocation) {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(destinationLocation);
        editor.putString(HTConstants.TRIP_DESTINATION, json);
        editor.apply();
    }

    public LatLng getTripDestinationFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String latLngString = sharedpreferences.getString(HTConstants.TRIP_DESTINATION, "None");
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(latLngString)) {
            return gson.fromJson(latLngString,LatLng.class);
        }

        return null;
    }

    public String getTripFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(HTConstants.TRIP_ID, "None");
    }

    public String getUserProfilePicFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(HTConstants.USER_PROFILE_PIC, "None");
    }

    public String getTripShareUrlInSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(HTConstants.TRIP_SHARE_URI, "None");
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

    public void saveTripShareUrlInSharedPreferences(String tripUri) {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(HTConstants.TRIP_SHARE_URI, tripUri);
        editor.commit();
    }

    private void getShareEtaURL(String tripId) {

        UserTrip userTrip = new UserTrip(userId, tripId);
        Gson gson = new Gson();
        String jsonBody = gson.toJson(userTrip);

        String url = HTConstants.API_ENDPOINT + "/api/v1/trips/";

        HTCustomPostRequest<UserTrip> requestObject = new HTCustomPostRequest<UserTrip>(1, url,
                jsonBody, UserTrip.class,
                new Response.Listener<UserTrip>() {
                    @Override
                    public void onResponse(UserTrip response) {
                        //Toast.makeText(Home.this, "URL: " + response.toString(), Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();

                        String uri = response.getShareUrl();
                        metaId = response.getId();
                        saveTripShareUrlInSharedPreferences(uri);
                        //shareUrl();
                        shareUrlViaShare();
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

    private void shareUrl() {

        Uri uri1 = Uri.parse("content://contacts");
        Intent intent = new Intent(Intent.ACTION_PICK, uri1);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_SHARE_CONTACT_CODE);

    }

    private void shareUrlViaShare() {


        String shareBody = "I'm on my way. Will be there by "+ getEstimatedTimeOfArrival() + ". Track me live " + getTripShareUrlInSharedPreferences();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(sharingIntent);

    }

    private void notifySelectedContact(String number) {

        String url = HTConstants.API_ENDPOINT + "/api/v1/trips/" + metaId + "/send_eta/";
        String[] recipientArray = {number};

        ETARecipients etaRecipients = new ETARecipients();
        etaRecipients.setRecipients(recipientArray);

        Gson gson = new Gson();
        String jsonBody = gson.toJson(etaRecipients);

        HTCustomPostRequest<String> requestObject = new HTCustomPostRequest<String>(1, url,
                jsonBody, String.class,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       //Log.v(TAG,"Recipients Response:" + response.toString());
                        //200 - sending notification
                        //400, 201 - fall to smses
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        shareUrlViaShare();
                        Log.d(TAG,"Couldn't send notification to the selected number.");
                    }
                }
        );

        MetaApplication.getInstance().addToRequestQueue(requestObject);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "mGoogleApiClient is connected");
        addMarkerToCurrentLocation();
        requestForLocationUpdates();
    }

    private void requestForGeofenceSetup() {

        Log.v(TAG, "Adding geofencing");

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);
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
        updateCurrentMarkerLocation(location);
    }

    private void updateCurrentMarkerLocation(Location location) {

        if (currentLocationMarker != null)
            currentLocationMarker.remove();


        customMarkerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_car_marker, null);
        profileViewProfileImage = (HTCircleImageView) customMarkerView.findViewById(R.id.profile_image);
        profileViewProfileImage.setImageBitmap(profilePicBitmap);

        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        currentLocationMarker = mMap.addMarker(
                new MarkerOptions()
                        .position(currentLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,customMarkerView))));


    }

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
        if (requestCode == REQUEST_SHARE_CONTACT_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String[] projection = { ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME };

                Cursor cursor = getContentResolver().query(uri, projection,
                        null, null, null);
                cursor.moveToFirst();

                int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String number = cursor.getString(numberColumnIndex);

                int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String name = cursor.getString(nameColumnIndex);
                number = number.replaceAll("\\s","");
                Log.d(TAG, "Number : " + number + " , name : "+name);

                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                try {

                    String locale = PhoneUtils.getCountryRegionFromPhone(this);
                    Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, locale);
                    Log.v(TAG, String.valueOf(phoneNumber.hasCountryCode()));

                    boolean isValid = phoneUtil
                            .isValidNumber(phoneNumber);

                    if (isValid) {
                        String internationalFormat = phoneUtil.format(
                                phoneNumber,
                                PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);

                       number = internationalFormat;

                    }

                } catch (NumberParseException e) {
                    System.err.println("NumberParseException was thrown: " + e.toString());
                }

                notifySelectedContact(number);
                Log.d(TAG, "International Number Format: " + number + " , name : " + name);

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

        LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(new LatLng(minLat, minLong));
        b.include(new LatLng(maxLat, maxLong));
        LatLngBounds bounds = b.build();

        return bounds;

    }

    @Override
    protected void onResume() {
        super.onResume();


        if (!getTripStatusFromSharedPreferences() && !TextUtils.equals(getTripShareUrlInSharedPreferences(), "None")) {
            endTrip();
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("trip_ended"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private void endTripClicked() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Stopping trip ... ");
        mProgressDialog.show();

        if (tripId == null)
            tripId = getTripFromSharedPreferences();

        Toast.makeText(Home.this, "Is internet available: " + isNetworkAvailable(), Toast.LENGTH_SHORT).show();

        if (tripId.equalsIgnoreCase("None"))
            return;

        transmitterService.endTrip(new HTTripStatusCallback() {
            @Override
            public void onError(Exception e) {
                mProgressDialog.dismiss();
                Toast.makeText(Home.this, e.getMessage() + ". This request could not be processed. Please try again later.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(HTTrip tripDetails) {
                mProgressDialog.dismiss();
                endTrip();
            }
        });
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.v(TAG, "Geofencing added successfully");
        } else {
            Log.v(TAG, "Geofencing not added. There was an error");
        }
    }

    private String getEstimatedTimeOfArrival() {

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, etaInMinutes);
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(now.getTime());
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

}
