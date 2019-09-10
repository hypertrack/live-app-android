package com.hypertrack.live.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.wrappers.InstantApps;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hypertrack.live.ApiHelper;
import com.hypertrack.live.R;
import com.hypertrack.live.debug.DebugHelper;
import com.hypertrack.live.map.TripsManager;
import com.hypertrack.live.map.htlocation.HTLocationGoogleMap;
import com.hypertrack.live.map.htlocation.ViewsSdkHTLocationProvider;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.TrackingStateObserver;

public class TrackingFragment extends SupportMapFragment implements TrackingStateObserver.OnTrackingStateChangeListener {
    private static final String TAG = TrackingFragment.class.getSimpleName();

    private Snackbar turnOnLocationSnackbar;
    private FloatingActionButton trackingButton;
    private FloatingActionButton locationButton;
    private View trackingButtonTips;
    private Button shareButton;
    private LoaderDecorator loader;

    private GoogleMap mMap;
    private HTLocationGoogleMap myLocationGoogleMap;

    private String hyperTrackPublicKey;
    private ApiHelper apiHelper;
    private TripsManager tripsManager;

    public LoaderDecorator getLoader() {
        return loader;
    }

    public ApiHelper getApiHelper() {
        return apiHelper;
    }

    public TripsManager getTripsManager() {
        return tripsManager;
    }

    public static Fragment newInstance(String hyperTrackPublicKey) {
        TrackingFragment fragment = new TrackingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("HYPER_TRACK_PUBLIC_KEY", hyperTrackPublicKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            hyperTrackPublicKey = bundle.getString("HYPER_TRACK_PUBLIC_KEY");
        }

    }

    @Nullable
    @Override
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

        apiHelper = new ApiHelper(getContext(), hyperTrackPublicKey);
        myLocationGoogleMap = new HTLocationGoogleMap(getActivity());
        tripsManager = new TripsManager(getActivity());

        trackingButton = view.findViewById(R.id.trackingButton);
        trackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getActivity()
                        .getSharedPreferences(getString(R.string.app_name), Activity.MODE_PRIVATE)
                        .edit();
                if (HyperTrack.isTracking()) {
                    HyperTrack.stopTracking();
                    editor.putBoolean("is_tracking", false).apply();
                } else {
                    if (AppUtils.isGpsProviderEnabled(getActivity())) {
                        HyperTrack.startTracking();
                        editor.putBoolean("is_tracking", true).apply();
                    } else {
                        actionLocationSourceSettings();
                    }
                }
            }
        });
        locationButton = view.findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myLocationGoogleMap.moveToMyLocation(mMap);
            }
        });
        trackingButtonTips = view.findViewById(R.id.trackingButtonTips);
        shareButton = view.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareTracking();
            }
        });

        loader = new LoaderDecorator(getContext());

        HyperTrack.addTrackingStateListener(this);

        if (!AppUtils.isGpsProviderEnabled(getActivity())) {
            showTurnOnLocationSnackbar();
        }

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(getString(R.string.app_name), Activity.MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !InstantApps.isInstantApp(getActivity())
                && !sharedPreferences.getBoolean("ibo_requested", false)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS},
                    MainActivity.PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            sharedPreferences.edit().putBoolean("ibo_requested", true).apply();
        }

        DebugHelper.onTrackingFragment(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (HyperTrack.isTracking()) {
            onTrackingStart();
        } else {
            onTrackingStop();
        }
    }

    @Override
    public void onError(TrackingError trackingError) {
        Log.e("onError", "code: " + trackingError.code);
        if (trackingError.code == TrackingError.GPS_PROVIDER_DISABLED_ERROR) {
            showTurnOnLocationSnackbar();
        }
    }

    @Override
    public void onTrackingStart() {
        if (turnOnLocationSnackbar != null) {
            turnOnLocationSnackbar.dismiss();
            turnOnLocationSnackbar = null;
        }
        trackingButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        trackingButton.setImageResource(R.drawable.ic_on);
        locationButton.show();
        shareButton.setEnabled(true);
        trackingButtonTips.setVisibility(View.GONE);
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                updateMap(googleMap);
                if (!AppUtils.isNetworkConnected(getActivity())) {
                    myLocationGoogleMap.addTo(googleMap);
                } else {
                    myLocationGoogleMap.addTo(googleMap, new ViewsSdkHTLocationProvider(getContext(), hyperTrackPublicKey));
                }
                tripsManager.addTo(googleMap);
            }
        });
    }

    @Override
    public void onTrackingStop() {
        trackingButton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        trackingButton.setImageResource(R.drawable.ic_on_disabled);
        locationButton.hide();
        shareButton.setEnabled(false);
        trackingButtonTips.setVisibility(View.VISIBLE);
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.clear();
                myLocationGoogleMap.removeFrom(googleMap);
                tripsManager.removeFrom(googleMap);
                updateMap(googleMap);
            }
        });
    }

    private void updateMap(GoogleMap googleMap) {
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        if (getActivity() != null) {
            int mapStyle = turnOnLocationSnackbar == null ? R.raw.style_map : R.raw.style_map_silver;
            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                boolean success = googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                                getActivity(), mapStyle));

                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style. Error: ", e);
            }
        }
    }

    private void showTurnOnLocationSnackbar() {
        if (getActivity() != null && !InstantApps.isInstantApp(getActivity())) {
            trackingButtonTips.setVisibility(View.GONE);
            turnOnLocationSnackbar = Snackbar.make(trackingButton, "", Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.tap_to_turn_location_settings), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            actionLocationSourceSettings();
                        }
                    });
            turnOnLocationSnackbar.setActionTextColor(Color.WHITE);
            turnOnLocationSnackbar.show();
            trackingButtonTips.setVisibility(View.GONE);
        }
    }

    private void actionLocationSourceSettings() {
        if (getActivity() != null && !InstantApps.isInstantApp(getActivity())) {
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void shareTracking() {
        if (getContext() != null && !TextUtils.isEmpty(hyperTrackPublicKey)) {
            loader.start();
            final String shareUrl = "https://trck.at/%s";

            apiHelper.getTrackingId(new Response.Listener<String>() {
                @Override
                public void onResponse(String trackingId) {
                    loader.stop();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(shareUrl, trackingId));
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loader.stop();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myLocationGoogleMap.removeFrom(mMap);
        HyperTrack.removeTrackingStateListener(this);
    }
}
