package com.hypertrack.live.debug;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hypertrack.live.BuildConfig;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.ui.TrackingFragment;
import com.hypertrack.sdk.Config;
import com.hypertrack.sdk.HyperTrack;

import java.util.Timer;

public class DebugHelper {
    private static final String TAG = TrackingFragment.class.getSimpleName();

    public static final String DEV_DOMAIN_KEY = "DEV_DOMAIN";

    public static final String RESTART_ACTION = "com.hypertrack.live.debug.RESTART_ACTION";

    private static Timer timer = new Timer();

    public static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(context.getPackageName() + "-debug", Context.MODE_PRIVATE);
    }

    public static void start(final Context context) {
        if (BuildConfig.DEBUG) {
        }
    }

    public static void onMainActivity(final MainActivity activity) {
        if (BuildConfig.DEBUG) {
            HyperTrack.enableDebugLogging();
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }

            View debugButton = activity.findViewById(R.id.debugButton);
            debugButton.setVisibility(View.VISIBLE);
            debugButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.startActivity(new Intent(activity, DebugActivity.class));
                }
            });
            LocalBroadcastManager.getInstance(activity).registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String domain = DebugHelper.getSharedPreferences(activity).getString(DebugHelper.DEV_DOMAIN_KEY, "");
                    HyperTrack.initialize(activity, "", new Config.Builder()
                            .enableAutoStartTracking(false)
                            .baseApiUrl(domain)
                            .build());
                    Intent activityIntent = activity.getIntent();
                    activity.finish();
                    activity.startActivity(activityIntent);
                }
            }, new IntentFilter(DebugHelper.RESTART_ACTION));
        }
    }

    public static void firebaseInstanceId(Context context) {
        if (BuildConfig.DEBUG) {
            Log.e("getToken()", FirebaseInstanceId.getInstance().getToken() + "");
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            Log.e("getInstanceId()", token);
                        }
                    });
        }
    }

    public static void onTrackingFragment(final Activity activity) {
        if (BuildConfig.DEBUG) {
        }
    }
}
