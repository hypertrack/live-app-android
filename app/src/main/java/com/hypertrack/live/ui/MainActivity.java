package com.hypertrack.live.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.hypertrack.live.App;
import com.hypertrack.live.MyTrackingStateListener;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.tracking.TrackingFragment;
import com.hypertrack.live.utils.AppUtils;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.ServiceNotificationConfig;
import com.hypertrack.sdk.TrackingError;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int VERIFICATION_REQUEST = 414;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 515;

    private HyperTrack hyperTrack;
    private MyTrackingStateListener myTrackingStateListener = new MyTrackingStateListener() {

        @Override
        public void onError(TrackingError trackingError) {
            loader.stop();
            switch (trackingError.code) {
                case TrackingError.INVALID_PUBLISHABLE_KEY_ERROR:
                    Log.e(TAG, "Need to check publish key");
                    // Check your publishable key and initialize SDK once again.
                    if (AppUtils.isNetworkConnected(MainActivity.this)) {
                        initializationFailed();
                    } else {
                        networkNotConnected();
                    }
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
    private LoaderDecorator loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loader = new LoaderDecorator(this);

        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String hyperTrackPublicKey = sharedPreferences.getString("pub_key", "");

        if (TextUtils.isEmpty(hyperTrackPublicKey) ||
                PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            addFragment(WelcomeFragment.newInstance(!TextUtils.isEmpty(hyperTrackPublicKey)));
        } else {
            initializeHyperTrack(hyperTrackPublicKey);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((App) getApplication()).setForeground(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((App) getApplication()).setForeground(false);
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName());
        transaction.commitAllowingStateLoss();
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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                        || PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    initializeHyperTrack(hyperTrackPublicKey);
                } else {
                    addFragment(WelcomeFragment.newInstance(true));
                }
            }
        } else if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                String hyperTrackPublicKey = sharedPreferences.getString("pub_key", "");
                initializeHyperTrack(hyperTrackPublicKey);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                    String hyperTrackPublicKey = sharedPreferences.getString("pub_key", "");
                    initializeHyperTrack(hyperTrackPublicKey);
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

    private void initializeHyperTrack(final String hyperTrackPublicKey) {

        if (!TextUtils.isEmpty(hyperTrackPublicKey)) {
            HyperTrack.enableDebugLogging();
            ServiceNotificationConfig notificationConfig = new ServiceNotificationConfig.Builder()
                    .setSmallIcon(R.drawable.ic_status_bar)
                    .setLargeIcon(R.drawable.ic_notification)
                    .build();
            hyperTrack = HyperTrack.getInstance(this, hyperTrackPublicKey)
                    .setTrackingNotificationConfig(notificationConfig)
                    .addTrackingListener(myTrackingStateListener)
                    .setDeviceName(AppUtils.getDeviceName(this));
            addFragment(TrackingFragment.newInstance(hyperTrackPublicKey));
            Log.i("getDeviceId", hyperTrack.getDeviceID());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hyperTrack != null) {
            hyperTrack.removeTrackingListener(myTrackingStateListener);
        }
    }

    private void initializationFailed() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                        sharedPreferences.edit()
                                .remove("pub_key")
                                .remove("is_tracking")
                                .commit();
                        addFragment(WelcomeFragment.newInstance(false));
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
}
