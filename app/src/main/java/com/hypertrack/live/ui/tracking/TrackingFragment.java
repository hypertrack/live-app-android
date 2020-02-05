package com.hypertrack.live.ui.tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.places.SearchPlaceFragment;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.views.dao.Trip;

import java.util.concurrent.TimeUnit;

public class TrackingFragment extends SupportMapFragment
        implements TrackingPresenter.View, OnMapReadyCallback {

    public static final int PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 616;
    public static final int AUTOCOMPLETE_REQUEST_CODE = 111;
    public static final int SET_ON_MAP_REQUEST_CODE = 112;

    private Snackbar turnOnLocationSnackbar;
    private View blockingView;
    private FloatingActionButton trackingStatusButton;
    private FloatingActionButton locationButton;
    private View tripInfo;
    private View tripSummaryInfo;
    private View share;
    private TextView destinationStatus;
    private TextView destinationAddress;
    private TextView stats;
    private TextView destination;
    private Button shareButton;
    private Button endTripButton;
    private Button closeButton;
    private LoaderDecorator loader;

    private GoogleMap mGoogleMap;
    private MapStyleOptions mapStyleOptions;
    private MapStyleOptions mapStyleOptionsSilver;

    private TrackingPresenter presenter;

    private boolean isMapStyleChanged = false;

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
        View fragmentLayout = inflater.inflate(R.layout.fragment_tracking, container, false);
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

        blockingView = view.findViewById(R.id.blocking_view);
        trackingStatusButton = view.findViewById(R.id.tracking_status_button);
        locationButton = view.findViewById(R.id.location_button);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                presenter.setCameraFixedEnabled(true);
                locationButton.hide();
                blockingView.setOnTouchListener(new android.view.View.OnTouchListener() {

                    @Override
                    public boolean onTouch(android.view.View view, MotionEvent motionEvent) {
                        presenter.setCameraFixedEnabled(false);
                        locationButton.show();
                        blockingView.setOnTouchListener(null);
                        return false;
                    }
                });
            }
        });

        share = view.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.share();
            }
        });

        tripInfo = view.findViewById(R.id.trip_info);
        destinationStatus = view.findViewById(R.id.destination_status);
        destinationAddress = view.findViewById(R.id.destination_address);
        tripSummaryInfo = view.findViewById(R.id.trip_summary_info);
        stats = view.findViewById(R.id.stats);
        destination = view.findViewById(R.id.destination);

        shareButton = view.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.shareHyperTrackUrl();
            }
        });
        endTripButton = view.findViewById(R.id.endTripButton);
        endTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.endTrip();
            }
        });
        closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.removeTrip();
            }
        });

        turnOnLocationSnackbar = Snackbar.make(locationButton, "", Snackbar.LENGTH_INDEFINITE)
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
        trackingStatusButton.setImageResource(R.drawable.sharing_status);
    }


    @Override
    public void onTrackingStop() {
        trackingStatusButton.setImageResource(R.drawable.sharing_status_disable);
    }

    @Override
    public void onError(TrackingError trackingError) {
        if (trackingError.code == TrackingError.GPS_PROVIDER_DISABLED_ERROR) {
            showTurnOnLocationSnackbar();
        }
        onDisabled();
    }

    @Override
    public void onActive() {
        if (turnOnLocationSnackbar.isShown()) {
            turnOnLocationSnackbar.dismiss();
        }
        if (isMapStyleChanged && mGoogleMap != null) {
            mGoogleMap.setMapStyle(mapStyleOptions);
            isMapStyleChanged = false;
        }
        shareButton.setEnabled(true);
        endTripButton.setEnabled(true);

    }

    @Override
    public void onDisabled() {
        if (mGoogleMap != null) {
            mGoogleMap.setMapStyle(mapStyleOptionsSilver);
            isMapStyleChanged = true;
        }
        shareButton.setEnabled(false);
        endTripButton.setEnabled(false);
    }

    @Override
    public void onDestinationChanged(String address) {
        if (getActivity() != null) {
            SearchPlaceFragment fragment = (SearchPlaceFragment) getActivity().getSupportFragmentManager().findFragmentByTag(SearchPlaceFragment.class.getSimpleName());
            if (fragment != null) {
                fragment.updateAddress(address);
            }
        }
    }

    @Override
    public void showTripInfo(Trip trip) {
        if (getActivity() != null) {
            if (trip.getDestination() == null) {

                destinationStatus.setVisibility(View.GONE);
                destinationAddress.setVisibility(View.GONE);
            } else {

                if (trip.getDestination().getArrivedDate() != null) {
                    destinationStatus.setText(R.string.arrived);
                } else if (trip.getEstimate() != null && trip.getEstimate().getRoute() != null
                        && trip.getEstimate().getRoute().getDuration() != null) {
                    int remainingDuration = trip.getEstimate().getRoute().getDuration();
                    if (remainingDuration < 120) {
                        destinationStatus.setText(getString(R.string.arriving_now));
                    } else {
                        destinationStatus.setText(
                                String.format(getString(R.string._away), TimeUnit.SECONDS.toMinutes(remainingDuration))
                        );
                    }
                } else {
                    destinationStatus.setText("");
                }
                destinationAddress.setText(trip.getDestination().getAddress());

                destinationStatus.setVisibility(View.VISIBLE);
                destinationAddress.setVisibility(View.VISIBLE);
            }

            share.setVisibility(View.GONE);
            tripSummaryInfo.setVisibility(View.GONE);

            tripInfo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showTripSummaryInfo(Trip trip) {
        if (getActivity() != null) {
            if (trip.getDestination() == null && trip.getSummary() == null) {

                stats.setVisibility(View.GONE);
                destination.setVisibility(View.GONE);
            } else {

                if (trip.getSummary() != null) {
                    double miles = trip.getSummary().getDistance() * 0.000621371;
                    long mins = TimeUnit.SECONDS.toMinutes(trip.getSummary().getDuration());
                    String statsText = String.format(getString(R.string.miles_mins), miles, mins);
                    stats.setText(statsText);
                }
                if (trip.getDestination() != null) {
                    destination.setText(trip.getDestination().getAddress());
                }

                stats.setVisibility(View.VISIBLE);
                destination.setVisibility(View.VISIBLE);
            }

            share.setVisibility(View.GONE);
            tripInfo.setVisibility(View.GONE);

            tripSummaryInfo.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void dismissTrip() {
        share.setVisibility(View.VISIBLE);

        tripInfo.setVisibility(View.GONE);
        tripSummaryInfo.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
    }

    @Override
    public void showProgressBar() {
        loader.start();
    }

    @Override
    public void hideProgressBar() {
        loader.stop();
    }

    @Override
    public void addFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void popBackStack() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
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
            turnOnLocationSnackbar.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }
}
