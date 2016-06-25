package io.hypertrack.meta.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
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
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.hypertrack.lib.common.model.HTDriverVehicleType;
import io.hypertrack.lib.consumer.utils.HTCircleImageView;
import io.hypertrack.lib.transmitter.model.HTTrip;
import io.hypertrack.meta.R;
import io.hypertrack.meta.adapter.PlaceAutocompleteAdapter;
import io.hypertrack.meta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.model.TripETAResponse;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.LocationStore;
import io.hypertrack.meta.store.TripManager;
import io.hypertrack.meta.store.UserStore;
import io.hypertrack.meta.store.callback.TripETACallback;
import io.hypertrack.meta.store.callback.TripManagerCallback;
import io.hypertrack.meta.store.callback.TripManagerListener;
import io.hypertrack.meta.util.AnimationUtils;
import io.hypertrack.meta.util.Constants;
import io.hypertrack.meta.util.KeyboardUtils;
import io.hypertrack.meta.util.NetworkUtils;
import io.hypertrack.meta.util.PhoneUtils;

public class Home extends AppCompatActivity implements ResultCallback<Status>, LocationListener,
        OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = AppCompatActivity.class.getSimpleName();
    private static final int REQUEST_SHARE_CONTACT_CODE = 1;
    private static final long INTERVAL_TIME = 5000;

    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    private SupportMapFragment mMapFragment;

    private PlaceAutocompleteAdapter mAdapter;

    private Toolbar toolbar;
    private AppBarLayout appBarLayout;

    private TextView enterDestinationText;
    private TextView mAutocompletePlacesView;
    private RelativeLayout enterDestinationLayout;
    private FrameLayout mAutocompletePlacesLayout;
    public CardView mAutocompleteResultsLayout;
    public RecyclerView mAutocompleteResults;
    private Marker currentLocationMarker;
    private Marker destinationLocationMarker;
    private Button sendETAButton;
    private Button shareButton;
    private Button navigateButton;
    private Bitmap profilePicBitmap;
    private HTCircleImageView profileViewProfileImage;
    private View customMarkerView;
    private ProgressDialog mProgressDialog;
    private ImageButton favoriteButton;

    private boolean enterDestinationLayoutClicked = false;

    private void onSelectPlace(final MetaPlace place) {
        if (place == null) {
            return;
        }

        this.getEtaForDestination(place.getLatLng(), new TripETACallback() {
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
            }
        });
    }

    private void onETASuccess(TripETAResponse response, MetaPlace place) {
        this.updateViewForETASuccess((int)response.getDuration()/60, place.getLatLng());
        TripManager.getSharedManager().setPlace(place);
    }

    private void updateViewForETASuccess(int etaInMinutes, LatLng latLng) {
        showSendETAButton();
        updateDestinationMarker(latLng, etaInMinutes);
        updateMapView();
    }

    private PlaceAutoCompleteOnClickListener mPlaceAutoCompleteListener = new PlaceAutoCompleteOnClickListener() {
        @Override
        public void OnSuccess(MetaPlace place) {
            //Restore Default State for Enter Destination Layout
            onEnterDestinationBackClick(null);

            // Set the Selected Place Name in the Enter Destination Layout
            enterDestinationText.setText(place.getName());
            KeyboardUtils.hideKeyboard(Home.this, mAutocompletePlacesView);
            onSelectPlace(place);
        }

        @Override
        public void OnError() {

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
        }
    };

    private AdapterView.OnClickListener enterDestinationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TripManager.getSharedManager().clearState();
            OnTripEnd();

            enterDestinationText.setText(getString(R.string.autocomplete_hint));
            enterDestinationLayoutClicked = true;

            // Hide the AppBar
            AnimationUtils.collapse(appBarLayout, 200);

            // Reset the Autocomplete TextView
            mAutocompletePlacesView.setText("");
            mAutocompletePlacesView.requestFocus();
            KeyboardUtils.showKeyboard(Home.this, mAutocompletePlacesView);

            enterDestinationLayout.setVisibility(View.GONE);
            mAutocompletePlacesLayout.setVisibility(View.VISIBLE);

            showAutocompleteResults(true);

            updateAutoCompleteResults();
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

    public void onEnterDestinationBackClick(View view) {
        enterDestinationLayoutClicked = false;

        // Show the AppBar
        AnimationUtils.expand(appBarLayout, 200);

        showAutocompleteResults(false);

        enterDestinationLayout.setVisibility(View.VISIBLE);
        mAutocompletePlacesLayout.setVisibility(View.GONE);

        KeyboardUtils.hideKeyboard(Home.this, mAutocompletePlacesView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapsInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_home);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        setSupportActionBar(toolbar);

        this.initGoogleClient();
        this.setupEnterDestinationView();
        this.setupAutoCompleteView();
        this.setupShareButton();
        this.setupSendETAButton();
        this.initCustomMarkerView();
        this.setupNavigateButton();
        this.setupFavoriteButton();

        checkIfUserIsOnBoard();

        if (!NetworkUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, "We could not detect internet on your mobile or there seems to be connectivity issues.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setupNavigateButton() {
        navigateButton = (Button) findViewById(R.id.navigateButton);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigate();
            }
        });
    }

    private void setupEnterDestinationView() {
        enterDestinationText = (TextView) findViewById(R.id.enter_destination_text);
        enterDestinationLayout = (RelativeLayout) findViewById(R.id.enter_destination_layout);

        enterDestinationLayout.setOnClickListener(enterDestinationClickListener);
    }

    private void setupAutoCompleteView() {
        mAutocompletePlacesView = (AutoCompleteTextView) findViewById(R.id.autocomplete_places);
        mAutocompletePlacesLayout = (FrameLayout) findViewById(R.id.autocomplete_places_layout);
        mAutocompleteResults = (RecyclerView) findViewById(R.id.autocomplete_places_results);
        mAutocompleteResultsLayout = (CardView) findViewById(R.id.autocomplete_places_results_layout);

        mAutocompletePlacesView.addTextChangedListener(mTextWatcher);

        LinearLayoutManager layoutManager = new LinearLayoutManager(Home.this);
        layoutManager.setAutoMeasureEnabled(true);
        mAutocompleteResults.setLayoutManager(layoutManager);

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, mPlaceAutoCompleteListener);
        mAutocompleteResults.setAdapter(mAdapter);
    }

    public void processPublishedResults(ArrayList<AutocompletePrediction> results) {

        if (results != null && results.size() > 0) {
            showAutocompleteResults(true);
        } else {
            showAutocompleteResults(false);
        }
        // TODO: 22/06/16 Add Loader while fetching Places Autocomplete data 
//        ((Home) context).customLoader.setVisibility(View.GONE);
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

    private void setupShareButton() {
        shareButton = (Button) findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    private void restoreTripStateIfNeeded() {
        final TripManager tripManager = TripManager.getSharedManager();
        tripManager.restoreState(new TripManagerCallback() {
            @Override
            public void OnSuccess() {
                Log.v(TAG, "Trip is active");

                MetaPlace place = tripManager.getPlace();
                updateViewForETASuccess(0, place.getLatLng());
                enterDestinationText.setText(place.getAddress());

                onTripStart();
            }

            @Override
            public void OnError() {
                Log.v(TAG, "Trip is not active");
            }
        });
    }

    private void setupSendETAButton() {
        sendETAButton = (Button) findViewById(R.id.sendETAButton);
        sendETAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TripManager.getSharedManager().isTripActive()) {
                    startTrip();
                } else {
                    endTrip();
                }
            }
        });
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

    private String getImageToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(Constants.USER_PROFILE_PIC_ENCODED, "None");
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

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                restoreTripStateIfNeeded();
            }
        });
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

    private void initCustomMarkerView() {

        customMarkerView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_car_marker, null);
        profileViewProfileImage = (HTCircleImageView) customMarkerView.findViewById(R.id.profile_image);

        SharedPreferences settings = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String urlProfilePic = settings.getString(Constants.USER_PROFILE_PIC, null);

        if(!TextUtils.isEmpty(urlProfilePic)) {
            Picasso.with(this)
                    .load(urlProfilePic)
                    .error(R.drawable.default_profile_pic)
                    .into(profileViewProfileImage);
        }

    }

    private void updateDestinationMarker(LatLng destinationLocation, int etaInMinutes) {
        if (destinationLocationMarker != null) {
            destinationLocationMarker.remove();
            destinationLocationMarker = null;
        }

        View markerView = this.getDestinationMarkerView(etaInMinutes);

        this.destinationLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(destinationLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(getBitMapForView(this,markerView))));
    }

    private View getDestinationMarkerView(int etaInMinutes) {
        View marker = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_destination_marker_layout, null);
        TextView etaTextView = (TextView) marker.findViewById(R.id.eta_txt);
        this.updateTextViewForMinutes(etaTextView, etaInMinutes);
        return marker;
    }

    private void getEtaForDestination(LatLng destinationLocation, final TripETACallback callback) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting ETA for the selected destination");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        if (currentLocationMarker == null || currentLocationMarker.getPosition() == null || destinationLocation == null) {
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

    private void showSendETAButton() {
        sendETAButton.setText("Send ETA");
        sendETAButton.setVisibility(View.VISIBLE);
    }

    private void startTrip() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Fetching URL to share... ");
        mProgressDialog.show();

        TripManager.getSharedManager().startTrip(new TripManagerCallback() {
            @Override
            public void OnSuccess() {
                mProgressDialog.dismiss();
                share();
                onTripStart();
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "mGoogleApiClient is connected");
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

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this);
        } catch (SecurityException exception) {
            Toast.makeText(Home.this, "Unable to request for location. Please check permissions in app settings.", Toast.LENGTH_LONG).show();
        }
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
        } else {
            currentLocationMarker.setPosition(latLng);
        }

        updateMapView();

        mAdapter.setBounds(getBounds(latLng, 10000));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SHARE_CONTACT_CODE) {
            if (resultCode == RESULT_OK) {
                this.didSelectContact(data);
            }
        } else if (requestCode == AddFavoritePlace.FAVORITE_PLACE_REQUEST_CODE) {
            this.updateFavoritesButton();
        }
    }

    private void didSelectContact(Intent data) {
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
    }

    private LatLngBounds getBounds(LatLng latLng, int mDistanceInMeters ){
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
            }

            @Override
            public void OnError() {
                mProgressDialog.dismiss();
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }

    // New Methods

    private void updateMapView() {
        LatLng current = currentLocationMarker.getPosition();

        if (destinationLocationMarker != null) {
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(current)
                    .include(destinationLocationMarker.getPosition())
                    .build();

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            mMap.animateCamera(cameraUpdate);

            return;
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(current));
    }

    private void updateTextViewForMinutes(TextView textView, int etaInMinutes) {
        if (etaInMinutes == 0) {
            textView.setText("--");
        } else {
            textView.setText(etaInMinutes + " m");
        }
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

        this.updateDestinationMarker(destinationLocation, etaInMinutes);
    }

    private void onTripStart() {
        sendETAButton.setText("End Trip");
        shareButton.setVisibility(View.VISIBLE);
        navigateButton.setVisibility(View.VISIBLE);
        enterDestinationLayout.setOnClickListener(null);
        favoriteButton.setVisibility(View.VISIBLE);
        this.updateFavoritesButton();

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
    }

    private void OnTripEnd() {
        sendETAButton.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);
        navigateButton.setVisibility(View.GONE);

        mAutocompletePlacesView.setVisibility(View.VISIBLE);
        favoriteButton.setVisibility(View.GONE);
        // Reset Enter Destination Layout Text
        enterDestinationText.setText("");

        if (destinationLocationMarker != null) {
            destinationLocationMarker.remove();
            destinationLocationMarker = null;
        }

        enterDestinationLayout.setOnClickListener(enterDestinationClickListener);
    }

    private void share() {
        String shareMessage = TripManager.getSharedManager().getShareMessage();
        if (shareMessage == null) {
            return;
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivity(sharingIntent);
    }

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

    public void onProfileButtonClicked(MenuItem menuItem) {
        Intent profileIntent = new Intent(this, UserProfile.class);
        startActivity(profileIntent);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void OnFavoriteClick(View view) {
        TripManager tripManager = TripManager.getSharedManager();
        MetaPlace place = tripManager.getPlace();

        if (place == null) {
            return;
        }

        showAddPlace(place);
    }

    private void showAddPlace(MetaPlace place) {
        // For Testing purpose
        Intent addPlace = new Intent(this, AddFavoritePlace.class);
        addPlace.putExtra("meta_place", place);
        startActivityForResult(addPlace, AddFavoritePlace.FAVORITE_PLACE_REQUEST_CODE, null);
    }

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
                this.markAsFavorite();
            } else {
                this.markAsNotFavorite();
            }
        }
    }

    private void markAsFavorite() {
        favoriteButton.setSelected(true);
        favoriteButton.setClickable(false);
        favoriteButton.setImageDrawable(getDrawable(R.drawable.ic_star));
    }

    private void markAsNotFavorite() {
        favoriteButton.setSelected(false);
        favoriteButton.setImageDrawable(getDrawable(R.drawable.ic_star_faded));
        favoriteButton.setClickable(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.updateFavoritesButton();
    }

    private void setupFavoriteButton() {
        favoriteButton = (ImageButton) findViewById(R.id.favorite_button);
        favoriteButton.setVisibility(View.GONE);
    }
}