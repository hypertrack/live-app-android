package com.hypertrack.live.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.crashlytics.android.Crashlytics;
import com.hypertrack.live.BuildConfig;
import com.hypertrack.live.R;
import com.hypertrack.live.MyTrackingStateListener;
import com.hypertrack.sdk.Config;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingError;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int VERIFICATION_REQUEST = 414;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 515;
    public static final int PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = 616;

    private LoaderDecorator loader;

    private MyTrackingStateListener myTrackingStateListener = new MyTrackingStateListener() {
        @Override
        public void onError(TrackingError trackingError) {
            loader.stop();
            switch (trackingError.getCode()) {
                case TrackingError.INVALID_PUBLISHABLE_KEY_ERROR:
                    Log.e(TAG, "Need to check publish key");
                    // Check your publishable key and initialize SDK once again.
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                    sharedPreferences.edit()
                            .remove("pub_key")
                            .remove("is_tracking")
                            .commit();
                    addFragment(WelcomeFragment.newInstance(false));
                    break;
                case TrackingError.AUTHORIZATION_ERROR:
                    Log.e(TAG, "Need to check account or renew subscription");
                    // Handle this error if needed.
                    break;
                case TrackingError.PERMISSION_DENIED_ERROR:
                    // User refused permission or they were not requested.
                    // Request permission from the user yourself or leave it to SDK.
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
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
                    alertDialog.show();
                    break;
                case TrackingError.GPS_PROVIDER_DISABLED_ERROR:
                    // User disabled GPS in device settings.
                    break;
                case TrackingError.UNKNOWN_ERROR:
                    // Some error we can't recognize. It may be connected with network or some device features.
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());

        loader = new LoaderDecorator(this);

        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String installReferrer = sharedPreferences.getString("_install_referrer", "");
        String hyperTrackPublicKey = sharedPreferences.getString("pub_key", "");
        boolean shouldStartTracking = sharedPreferences.getBoolean("is_tracking", true);

        if (!TextUtils.isEmpty(installReferrer)) {
            hyperTrackPublicKey = installReferrer;
            shouldStartTracking = true;
            sharedPreferences.edit()
                    .remove("_install_referrer")
                    .putString("pub_key", hyperTrackPublicKey)
                    .putBoolean("is_tracking", true)
                    .apply();
        }

        if (TextUtils.isEmpty(hyperTrackPublicKey) ||
                PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            addFragment(WelcomeFragment.newInstance(!TextUtils.isEmpty(hyperTrackPublicKey)));
        } else {
            initializeHyperTrack(hyperTrackPublicKey, shouldStartTracking);
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_frame, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode & 0x0000ffff) == VERIFICATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                String hyperTrackPublicKey = data.getStringExtra(VerificationActivity.VERIFICATION_KEY);
                getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit()
                        .putString("pub_key", hyperTrackPublicKey)
                        .putBoolean("is_tracking", true)
                        .apply();
                addFragment(WelcomeFragment.newInstance(true));
            }
        } else if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                String hyperTrackPublicKey = sharedPreferences.getString("pub_key", "");
                boolean shouldStartTracking = sharedPreferences.getBoolean("is_tracking", true);
                initializeHyperTrack(hyperTrackPublicKey, shouldStartTracking);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                    String packageName = getPackageName();
                    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        Uri uri = Uri.parse("package:" + packageName);
                        intent.setData(uri);
                        startActivityForResult(intent, MainActivity.PERMISSIONS_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    }
                }
                break;
            }
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                    String hyperTrackPublicKey = sharedPreferences.getString("pub_key", "");
                    boolean shouldStartTracking = sharedPreferences.getBoolean("is_tracking", true);
                    initializeHyperTrack(hyperTrackPublicKey, shouldStartTracking);
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
                break;
            }
        }
    }

    private void initializeHyperTrack(final String hyperTrackPublicKey, boolean shouldStartTracking) {
        Config config = new Config.Builder()
                .enableAutoStartTracking(shouldStartTracking)
                .enableDebugLogging(BuildConfig.DEBUG)
                .build();
        HyperTrack.initialize(this, hyperTrackPublicKey, config);
        HyperTrack.addTrackingStateListener(myTrackingStateListener);
        HyperTrack.addNotificationIconsAndTitle(
                R.drawable.ic_status_bar,
                R.drawable.ic_notification,
                null, null);
        addFragment(TrackingFragment.newInstance(hyperTrackPublicKey));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HyperTrack.removeTrackingStateListener(myTrackingStateListener);
    }
}
