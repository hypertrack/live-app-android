package io.hypertrack.meta.view;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.R;
import io.hypertrack.meta.model.CustomAddress;
import io.hypertrack.meta.model.MetaLocation;

public class AddAddress extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    @Bind(R.id.locationName)
    public EditText locationNameEditText;

    @Bind(R.id.address)
    public EditText addressEditText;

    @Bind(R.id.postalCode)
    public EditText postalCodeEditText;

    @Bind(R.id.city)
    public EditText cityEditText;

    @Bind(R.id.state)
    public EditText stateEditText;

    @Bind(R.id.country)
    public EditText countryEditText;

    @Bind(R.id.setCustomAddress)
    public Button setCustomAddressButton;

    private static final String TAG = AddAddress.class.getSimpleName();
    private GoogleMap mMap;
    private MapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private Marker currentLocationMarker;
    private LatLng currentLocation;
    private LatLng recievedLocation;
    private static final int SELECTED_LOCATION_DATA = 102;
    private MetaLocation metaLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        ButterKnife.bind(this);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(io.hypertrack.lib.consumer.R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

    }

    @OnClick(R.id.setCustomAddress)
    public void setCustomAddress() {

        if (validated()) {

            Intent intent=new Intent();

            CustomAddress customAddress = new CustomAddress();

            if (!TextUtils.isEmpty(locationNameEditText.getText().toString()))
                customAddress.setName(locationNameEditText.getText().toString());

            if (!TextUtils.isEmpty(addressEditText.getText().toString()))
                customAddress.setAddress(addressEditText.getText().toString());

            if (!TextUtils.isEmpty(cityEditText.getText().toString()))
                customAddress.setCity(cityEditText.getText().toString());

            if (!TextUtils.isEmpty(stateEditText.getText().toString()))
                customAddress.setState(stateEditText.getText().toString());

            if (!TextUtils.isEmpty(postalCodeEditText.getText().toString()))
                customAddress.setPostalCode(postalCodeEditText.getText().toString());

            if (!TextUtils.isEmpty(countryEditText.getText().toString()))
                customAddress.setCountry(countryEditText.getText().toString());

            if (metaLocation == null) {
                double[] ll = {currentLocation.longitude, currentLocation.latitude};
                metaLocation = new MetaLocation();
                metaLocation.setType("Point");
                metaLocation.setCoordinates(ll);
            }

            customAddress.setLocation(metaLocation);

            intent.putExtra("custom_address", customAddress);
            setResult(101, intent);

            finish();
        }

    }

    private boolean validated() {
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent intent = new Intent(AddAddress.this, SelectDestination.class);
                startActivityForResult(intent, SELECTED_LOCATION_DATA);
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
            addMarkerToCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void addMarkerToCurrentLocation() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location != null) {

                if (currentLocationMarker != null)
                    currentLocationMarker.remove();

                Log.v(TAG, "Adding current location to the map.");

                if (recievedLocation != null) {
                    currentLocation = recievedLocation;
                } else {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }

                currentLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)));
                currentLocationMarker.showInfoWindow();

                CameraPosition cameraPosition =
                        new CameraPosition.Builder()
                                .target(currentLocation)
                                .zoom(mMap.getCameraPosition().zoom >= 16 ? mMap.getCameraPosition().zoom : 16)
                                .build();

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == SELECTED_LOCATION_DATA) {
            LatLng latLng = data.getParcelableExtra("selected_location");
            Log.d(TAG, "Received selected Location: " + latLng.toString());
            if (latLng != null) {
                double[] ll = {latLng.longitude, latLng.latitude};
                metaLocation = new MetaLocation();
                metaLocation.setType("Point");
                metaLocation.setCoordinates(ll);

                recievedLocation = latLng;
            }
        }
    }
}
