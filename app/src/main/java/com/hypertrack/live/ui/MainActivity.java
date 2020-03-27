package com.hypertrack.live.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.wrappers.InstantApps;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.hypertrack.backend.BackendProvider;
import com.hypertrack.backend.ResultHandler;
import com.hypertrack.live.App;
import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.LaunchActivity;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.tracking.TrackingFragment;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.live.utils.SharedHelper;
import com.hypertrack.live.views.Snackbar;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.ServiceNotificationConfig;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.TrackingStateObserver;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = App.TAG + "MainActivity";

    public static final int VERIFICATION_REQUEST = 414;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 515;

    public static final String PUBLISHABLE_KEY = "publishable_key";

    private SharedHelper sharedHelper;

    private BroadcastReceiver trackingStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int code = intent.getIntExtra(TrackingStateObserver.EXTRA_KEY_CODE_, 0);
            switch (code) {
                case TrackingStateObserver.EXTRA_EVENT_CODE_START:
                    onTrackingStart();
                    break;
                case TrackingStateObserver.EXTRA_EVENT_CODE_STOP:
                    onTrackingStop();
                    break;
                default:
                    onError(code);
            }
        }
    };
    private Snackbar turnOnLocationSnackbar;
    private TextView trackingStatus;
    private TextView trackingStatusText;

    private MapStyleOptions mapStyleOptions;
    private MapStyleOptions mapStyleOptionsSilver;
    private MapStyleOptions currentMapStyle;
    private GoogleMap mMap;

    private final BroadcastReceiver shareBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedHelper = SharedHelper.getInstance(this);

        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map);
        mapStyleOptionsSilver = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map_silver);

        trackingStatusText = findViewById(R.id.tracking_status_text);
        trackingStatusText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackingStatusText.setVisibility(View.GONE);
            }
        });
        trackingStatus = findViewById(R.id.tracking_status);
        trackingStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (trackingStatusText.getVisibility() == View.VISIBLE) {
                    trackingStatusText.setVisibility(View.GONE);
                } else {
                    trackingStatusText.setVisibility(View.VISIBLE);
                }
            }
        });
        turnOnLocationSnackbar = Snackbar.make(trackingStatus, R.layout.snackbar_gps_enable, Snackbar.LENGTH_INDEFINITE)
                .setAction(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

        registerReceiver(trackingStateReceiver, new IntentFilter(TrackingStateObserver.ACTION_TRACKING_STATE));
        registerReceiver(shareBroadcastReceiver, new IntentFilter(AppUtils.SHARE_BROADCAST_ACTION));

        if (!AppUtils.isGpsProviderEnabled(this)) {
            showTurnOnLocationSnackbar();
        }

        onStateUpdate();
    }

    public void onStateUpdate() {

        String hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();

        if (TextUtils.isEmpty(hyperTrackPublicKey) ||
                PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            beginFragmentTransaction(WelcomeFragment.newInstance(hyperTrackPublicKey))
                    .commitAllowingStateLoss();
        } else if (!sharedHelper.sharedPreferences().contains("user_name")) {
            beginFragmentTransaction(Covid19Fragment.newInstance(hyperTrackPublicKey))
                    .commitAllowingStateLoss();
        } else {
            initializeHyperTrack(hyperTrackPublicKey);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppUtils.isGpsProviderEnabled(this)) {
            onMapActive();
        } else {
            onMapDisabled();
        }
        ((App) getApplication()).setForeground(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((App) getApplication()).setForeground(false);
    }

    public FragmentTransaction beginFragmentTransaction(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName());
        return transaction;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode & 0x0000ffff) == VERIFICATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                String hyperTrackPublicKey = data.getStringExtra(VerificationActivity.VERIFICATION_KEY);
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                sharedPreferences.edit()
                        .putString("pub_key", hyperTrackPublicKey)
                        .apply();
                onStateUpdate();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                onStateUpdate();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingByDefault(sharedHelper.getHyperTrackPubKey());
                onStateUpdate();
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.parse("package:" + getPackageName());
                                intent.setData(uri);
                                startActivityForResult(intent, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        })
                        .setTitle(R.string.app_settings)
                        .setMessage(R.string.you_can_allow)
                        .create();
                alertDialog.show();
            }
        }
    }

    private void initializeHyperTrack(final String hyperTrackPublicKey) {

        if (!TextUtils.isEmpty(hyperTrackPublicKey)) {
            ServiceNotificationConfig notificationConfig = new ServiceNotificationConfig.Builder()
                    .setSmallIcon(R.drawable.ic_status_bar)
                    .setLargeIcon(R.drawable.ic_notification)
                    .build();
            HyperTrack hyperTrack = HyperTrack.getInstance(this, hyperTrackPublicKey)
                    .setTrackingNotificationConfig(notificationConfig);
            hyperTrack.requestPermissionsIfNecessary();

            if (hyperTrack.isRunning()) {
                onTrackingStart();
            } else {
                onTrackingStop();
            }

            beginFragmentTransaction(new TrackingFragment()).commitAllowingStateLoss();
            Log.i("deviceId", hyperTrack.getDeviceID());
        }
    }

    private void startTrackingByDefault(String hyperTrackPublicKey) {
        final HyperTrack hyperTrack = HyperTrack.getInstance(this, hyperTrackPublicKey);
        BackendProvider backendProvider = HTMobileClient.getBackendProvider(this);
        backendProvider.start(hyperTrack.getDeviceID(), new ResultHandler<String>() {
            @Override
            public void onResult(String result) {
                Toast.makeText(MainActivity.this, getString(R.string.tracking_is_started), Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onError(@NonNull Exception error) {
                Log.e("Sign in", "login completed error:" + error.getMessage());
            }
        });
    }

    public void onTrackingStart() {
        trackingStatus.setActivated(true);
        trackingStatus.setText(R.string.active);
        trackingStatusText.setVisibility(View.GONE);
        trackingStatusText.setText(
                String.format(getString(R.string.tracking_is), getString(R.string.active).toLowerCase())
        );
    }


    public void onTrackingStop() {
        trackingStatus.setActivated(false);
        trackingStatus.setText(R.string.inactive);
        trackingStatusText.setText(String.format(getString(R.string.tracking_is), getString(R.string.disabled).toLowerCase()));
    }

    public void onError(int errorCode) {
        switch (errorCode) {
            case TrackingError.INVALID_PUBLISHABLE_KEY_ERROR:
                Log.e(TAG, "Need to check publishable key");
                // Check your publishable key and initialize SDK once again.
                if (AppUtils.isNetworkConnected(MainActivity.this)) {
                    initializationFailed();
                } else {
                    networkNotConnected();
                }
                break;
            case TrackingError.AUTHORIZATION_ERROR:
                Log.e(TAG, "Need to check account or renew subscription");
                AlertDialog billingAlertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String url = "https://dashboard.hypertrack.com/billing";
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                            }
                        })
                        .setTitle(getString(R.string.events_expired_this_month))
                        .setMessage(getString(R.string.upgrade_to_prod))
                        .create();
                billingAlertDialog.show();
                break;
            case TrackingError.PERMISSION_DENIED_ERROR:
                // User refused permission or they were not requested.
                // Request permission from the user yourself or leave it to SDK.
                AlertDialog settingsAlertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(R.string.open, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }
                        })
                        .setTitle(R.string.app_settings)
                        .setMessage(R.string.you_can_allow)
                        .create();
                settingsAlertDialog.show();
                break;
            case TrackingError.GPS_PROVIDER_DISABLED_ERROR:
                showTurnOnLocationSnackbar();
                break;
            case TrackingError.UNKNOWN_ERROR:
                // Some error we can't recognize. It may be connected with network or some device features.
                break;
        }
        onMapDisabled();
    }

    public void onMapActive() {
        if (turnOnLocationSnackbar.isShown()) {
            turnOnLocationSnackbar.dismiss();
        }
        if (currentMapStyle != mapStyleOptions && mMap != null) {
            mMap.setMapStyle(mapStyleOptions);
            currentMapStyle = mapStyleOptions;
        }
    }

    public void onMapDisabled() {
        if (currentMapStyle != mapStyleOptionsSilver && mMap != null) {
            mMap.setMapStyle(mapStyleOptionsSilver);
            currentMapStyle = mapStyleOptionsSilver;
        }
    }

    private void showTurnOnLocationSnackbar() {
        if (!InstantApps.isInstantApp(this)) {
            turnOnLocationSnackbar.show();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_frame);
        if (!(fragment instanceof OnBackPressedListener) || !((OnBackPressedListener) fragment).onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(shareBroadcastReceiver);
        unregisterReceiver(trackingStateReceiver);
    }

    private void initializationFailed() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sharedHelper.sharedPreferences().edit()
                                .remove("pub_key")
                                .apply();
                        beginFragmentTransaction(WelcomeFragment.newInstance(sharedHelper.getHyperTrackPubKey()))
                                .commitAllowingStateLoss();
                    }
                })
                .setTitle(R.string.valid_publishable_key_required)
                .setMessage(R.string.publishable_key_you_entered_is_invalid)
                .create();
        alertDialog.show();
    }

    private void networkNotConnected() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.app_settings, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setTitle(R.string.valid_publishable_key_required)
                .setMessage(R.string.check_your_network_connection)
                .create();
        alertDialog.show();
    }

    public void getMapAsync(@NonNull final OnMapReadyCallback onMapReadyCallback) {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    UiSettings uiSettings = googleMap.getUiSettings();
                    uiSettings.setMapToolbarEnabled(false);

                    onMapReadyCallback.onMapReady(googleMap);
                }
            });
        } else {
            onMapReadyCallback.onMapReady(mMap);
        }

    }
}
