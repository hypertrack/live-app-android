package io.hypertrack.meta;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SelectDestination extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private MapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private Marker currentLocationMarker;
    private LatLng currentLocation;
    private ImageButton imageButton;
    private LatLng newDestinationLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_destination);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(com.hypertrack.android.sdk.R.id.map);
        mapFragment.getMapAsync(this);

        imageButton = (ImageButton) findViewById(R.id.imageButton);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        addMarkerToCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                    newDestinationLocation = cameraPosition.target;
                Intent intent = new Intent();
                intent.putExtra("selected_location", newDestinationLocation);
                setResult(102, intent);
            }
        });
    }

    private void addMarkerToCurrentLocation() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location != null) {

                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                CameraPosition cameraPosition =
                        new CameraPosition.Builder()
                                .target(currentLocation)
                                .zoom(mMap.getCameraPosition().zoom >= 14 ? mMap.getCameraPosition().zoom : 14)
                                .build();

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

        }
    }
}
