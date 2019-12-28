package com.hypertrack.live.ui.tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.wrappers.InstantApps;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.views.dao.Trip;

import java.util.Arrays;
import java.util.List;

public class TrackingFragment extends SupportMapFragment
        implements TrackingPresenter.View, OnMapReadyCallback {

    public static final int PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 616;
    public static final int AUTOCOMPLETE_REQUEST_CODE = 111;

    private Snackbar turnOnLocationSnackbar;
    private FloatingActionButton trackingButton;
    private FloatingActionButton locationButton;
    private View trackingButtonTips;
    private Button shareButton;
    private Button tripButton;
    private LoaderDecorator loader;

    private GoogleMap mGoogleMap;
    private MapStyleOptions mapStyleOptions;
    private MapStyleOptions mapStyleOptionsSilver;

    private TrackingPresenter presenter;

    private boolean isMapStyleChanged = false;
    private TextView destination;

    public static Fragment newInstance(String hyperTrackPublicKey) {
        TrackingFragment fragment = new TrackingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("HYPER_TRACK_PUBLIC_KEY", hyperTrackPublicKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mapView = super.onCreateView(inflater, container, savedInstanceState);
        View fragmentLayout = inflater.inflate(R.layout.fragment_tracking, null);
        FrameLayout frameLayout = fragmentLayout.findViewById(R.id.content_frame);
        frameLayout.addView(mapView);
        return fragmentLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String hyperTrackPublicKey = getArguments() != null ?
                getArguments().getString("HYPER_TRACK_PUBLIC_KEY") : null;
        if (TextUtils.isEmpty(hyperTrackPublicKey)) {
            return;
        }

        presenter = new TrackingPresenter(view.getContext(), this, hyperTrackPublicKey);
        loader = new LoaderDecorator(getContext());

        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(view.getContext(), R.raw.style_map);
        mapStyleOptionsSilver = MapStyleOptions.loadRawResourceStyle(view.getContext(), R.raw.style_map_silver);

        trackingButton = view.findViewById(R.id.trackingButton);
        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.performTracking();
            }
        });
        locationButton = view.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.moveToMyLocation();
            }
        });
        trackingButtonTips = view.findViewById(R.id.trackingButtonTips);

        view.findViewById(R.id.bottomSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlaceSearch();
            }
        });
        destination = view.findViewById(R.id.destination_address);

        shareButton = view.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.shareTracking();
            }
        });
        tripButton = view.findViewById(R.id.tripButton);
        tripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.startTrip();
            }
        });
        turnOnLocationSnackbar = Snackbar.make(trackingButton, "", Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.tap_to_turn_location_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.actionLocationSourceSettings();
                    }
                })
                .setActionTextColor(Color.WHITE);

        getMapAsync(this);

        if (!AppUtils.isGpsProviderEnabled(getActivity())) {
            showTurnOnLocationSnackbar();
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.app_name), Activity.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !InstantApps.isInstantApp(getActivity())
                && !sharedPreferences.getBoolean("ibo_requested", false)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS},
                    PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            sharedPreferences.edit().putBoolean("ibo_requested", true).apply();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        presenter.initMap(googleMap);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onTrackingStart() {
        if (turnOnLocationSnackbar.isShown()) {
            turnOnLocationSnackbar.dismiss();
        }
        if (isMapStyleChanged && mGoogleMap != null) {
            mGoogleMap.setMapStyle(mapStyleOptions);
            isMapStyleChanged = false;
        }
        trackingButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorHyperTrack)));
        trackingButton.setImageResource(R.drawable.ic_on);
        locationButton.show();
        shareButton.setEnabled(true);
        tripButton.setEnabled(true);
        trackingButtonTips.setVisibility(View.GONE);

        presenter.setMyLocationEnabled(true);
    }


    @Override
    public void onTrackingStop() {
        trackingButton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        trackingButton.setImageResource(R.drawable.ic_on_disabled);
        locationButton.hide();
        shareButton.setEnabled(false);
        tripButton.setEnabled(false);
        trackingButtonTips.setVisibility(View.VISIBLE);

        presenter.setMyLocationEnabled(false);
    }

    @Override
    public void onError(TrackingError trackingError) {
        if (trackingError.code == TrackingError.GPS_PROVIDER_DISABLED_ERROR) {
            showTurnOnLocationSnackbar();
            if (mGoogleMap != null) {
                mGoogleMap.setMapStyle(mapStyleOptionsSilver);
                isMapStyleChanged = true;
            }
        }
    }

    @Override
    public void onDestinationChanged(String address) {
        if (TextUtils.isEmpty(address)) {
            tripButton.setVisibility(View.INVISIBLE);
        } else {
            tripButton.setVisibility(View.VISIBLE);
        }
        destination.setText(address);
    }

    @Override
    public void onTripChanged(Trip trip) {
        if (trip == null || !"active".equals(trip.getStatus())) {
            tripButton.setText(R.string.start_trip);
            tripButton.setBackgroundResource(R.drawable.button);
            tripButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.startTrip();
                }
            });
        } else {
            tripButton.setVisibility(View.VISIBLE);
            tripButton.setText(R.string.end_trip);
            tripButton.setBackgroundResource(R.drawable.button_cancel);
            tripButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    presenter.endTrip();
                }
            });
        }
    }

    @Override
    public void showProgressBar() {
        loader.start();
    }

    @Override
    public void hideProgressBar() {
        loader.stop();
    }

    private void openPlaceSearch() {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        );
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(getActivity());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressLint({"NewApi", "BatteryLife"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (getActivity() != null) {
            switch (requestCode) {
                case TrackingFragment.PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        PowerManager pm = (PowerManager) getActivity().getSystemService(Activity.POWER_SERVICE);
                        String packageName = getActivity().getPackageName();
                        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            Uri uri = Uri.parse("package:" + packageName);
                            intent.setData(uri);
                            getActivity().startActivityForResult(intent, TrackingFragment.PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        }
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    private void showTurnOnLocationSnackbar() {
        if (getActivity() != null && !InstantApps.isInstantApp(getActivity())) {
            trackingButtonTips.setVisibility(View.GONE);
            turnOnLocationSnackbar.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }
}
