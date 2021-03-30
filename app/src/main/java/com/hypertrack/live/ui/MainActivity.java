package com.hypertrack.live.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.wrappers.InstantApps;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.hypertrack.backend.AbstractBackendProvider;
import com.hypertrack.backend.ResultHandler;
import com.hypertrack.backend.models.GeofenceLocation;
import com.hypertrack.live.App;
import com.hypertrack.live.BackendClientFactory;
import com.hypertrack.live.CognitoClient;
import com.hypertrack.live.LaunchActivity;
import com.hypertrack.live.PermissionsManager;
import com.hypertrack.live.R;
import com.hypertrack.live.models.PlaceModel;
import com.hypertrack.live.ui.tracking.TrackingFragment;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.live.utils.SharedHelper;
import com.hypertrack.live.views.Snackbar;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.ServiceNotificationConfig;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.TrackingStateObserver;

import org.jetbrains.annotations.NotNull;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = App.TAG + "MainActivity";

    public static final int PERMISSIONS_REQUEST = 515;

    private SharedHelper sharedHelper;

    private HyperTrack hyperTrack;
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
    private AbstractBackendProvider mBackendProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedHelper = SharedHelper.getInstance(this);

        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map);
        mapStyleOptionsSilver = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map_silver);

        setupDrawerLayout();

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

    private void setupDrawerLayout() {

        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });

        String emailAddress = sharedHelper.getAccountEmail();
                ((TextView)drawerLayout.findViewById(R.id.email_address)).setText(emailAddress);
        View logoutButton = drawerLayout.findViewById(R.id.logout);
        if (sharedHelper.getLoginType().equals(SharedHelper.LOGIN_TYPE_COGNITO)) {
            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) { logout(); }
            });

        } else {
            logoutButton.setVisibility(View.INVISIBLE);
        }
        View inviteButton = drawerLayout.findViewById(R.id.invite_member);
        if (sharedHelper.getInviteLink().isEmpty()) {
            inviteButton.setVisibility(View.GONE);

        } else {
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    AppUtils.shareAction(
                            MainActivity.this,
                            sharedHelper.getInviteLink(),
                            getString(R.string.invite_via));
                }
            });
        }
    }

    private void logout() {

        mBackendProvider.stop();
        CognitoClient.getInstance(MainActivity.this).logout();
        sharedHelper.logout();
        Intent intent = new Intent(MainActivity.this, LaunchActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(intent);
        finish();
    }

    public void onStateUpdate() {

        String hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();

        if (PermissionsManager.isAllPermissionsApproved(this)) {
            initializeHyperTrack(hyperTrackPublicKey, sharedHelper.getUserName());
            mBackendProvider = BackendClientFactory.getBackendProvider(this, hyperTrack.getDeviceID());
            //noinspection ConstantConditions
            beginFragmentTransaction(new TrackingFragment(mBackendProvider))
                    .commitAllowingStateLoss();

            if (sharedHelper.getAccountEmail().isEmpty()) getAccountEmail(true);
            if (sharedHelper.getInviteLink().isEmpty()) getInviteLink(true);
            if (!sharedHelper.isHomePlaceSet()) fetchHomeFromBackend();

        } else {
            beginFragmentTransaction(new PermissionRationalFragment()).commitAllowingStateLoss();
        }


        if (hyperTrack != null) {
            if (hyperTrack.isRunning()) {
                onTrackingStart();
            } else {
                onTrackingStop();
            }
        }
    }

    private void fetchHomeFromBackend() {
        mBackendProvider.getHomeGeofenceLocation(new ResultHandler<GeofenceLocation>() {
            @Override public void onResult(GeofenceLocation result) {
                PlaceModel newPlace = new PlaceModel();
                newPlace.latLng = new LatLng(result.getLatitude(), result.getLongitude());
                newPlace.populateAddressFromGeocoder(MainActivity.this);
                sharedHelper.setHomePlace(newPlace);
            }
            @Override public void onError(@NotNull Exception error) { }
        });
    }

    private void getAccountEmail(final boolean shouldRetry) {
        mBackendProvider.getAccountName(new ResultHandler<String>() {
            @Override
            public void onResult(String result) {
                 sharedHelper.setAccountEmail(result);
                 setupDrawerLayout();
            }

            @Override
            public void onError(@NonNull Exception error) {
                Log.w(TAG, "Error getting account email ", error);
                if (shouldRetry)
                    getAccountEmail(false);
            }
        });
    }

    private void getInviteLink(final boolean shouldRetry) {
        mBackendProvider.getInviteLink(new ResultHandler<String>() {
            @Override
            public void onResult(String result) {
                 sharedHelper.setInviteLink(result);
                 setupDrawerLayout();
            }

            @Override
            public void onError(@NonNull Exception error) {
                Log.w(TAG, "Error getting account email ", error);
                if (shouldRetry)
                    getInviteLink(false);
            }
        });
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

        if (requestCode == PERMISSIONS_REQUEST) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                onStateUpdate();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                                startActivityForResult(intent, PERMISSIONS_REQUEST);
                            }
                        })
                        .setTitle(R.string.app_settings)
                        .setMessage(R.string.you_can_allow)
                        .create();
                alertDialog.show();
            }
        }
    }

    private void initializeHyperTrack(@NonNull final String hyperTrackPublicKey, @Nullable String email) {

        if (hyperTrack == null && !TextUtils.isEmpty(hyperTrackPublicKey)) {
            ServiceNotificationConfig notificationConfig = new ServiceNotificationConfig.Builder()
                    .setSmallIcon(R.drawable.ic_status_bar)
                    .setLargeIcon(R.drawable.ic_notification)
                    .build();
            hyperTrack = HyperTrack.getInstance(hyperTrackPublicKey)
                    .setTrackingNotificationConfig(notificationConfig)
                    .setDeviceName(email);
            Log.i("deviceId", hyperTrack.getDeviceID());
        }
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
                                startActivityForResult(intent, PERMISSIONS_REQUEST);
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
                        // Navigate to login
                        MainActivity.this.startActivity(new Intent(MainActivity.this, LaunchActivity.class));
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
