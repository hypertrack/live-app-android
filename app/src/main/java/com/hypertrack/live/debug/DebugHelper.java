package com.hypertrack.live.debug;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hypertrack.live.BuildConfig;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.ui.TrackingFragment;
import com.hypertrack.sdk.Config;
import com.hypertrack.sdk.HyperTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

public class DebugHelper {
    private static final String TAG = TrackingFragment.class.getSimpleName();

    public static final String DEV_DOMAIN_KEY = "DEV_DOMAIN";
    public static final String DEV_API_DOMAIN_KEY = "DEV_API_DOMAIN";
    public static final String DEV_ACCOUNTID_KEY = "DEV_ACCOUNTID";
    public static final String DEV_SECRETKEY_KEY = "DEV_SECRETKEY";

    public static final String RESTART_ACTION = "com.hypertrack.live.debug.RESTART_ACTION";

    private static Timer timer = new Timer();

    public static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences(context.getPackageName() + "-debug", Context.MODE_PRIVATE);
    }

    public static String getDomain(final Context context) {
        return getSharedPreferences(context).getString(DebugHelper.DEV_DOMAIN_KEY, "live-api.htprod.hypertrack.com");
    }

    public static String getApiDomain(final Context context) {
        return getSharedPreferences(context).getString(DebugHelper.DEV_API_DOMAIN_KEY, "v3.api.hypertrack.com");
    }

    public static boolean isAlive(final Context context) {
        if (BuildConfig.DEBUG) {
            return "live-api.htprod.hypertrack.com".equals(getDomain(context));
        }
        return true;
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

            activity.findViewById(R.id.debugLayout).setVisibility(View.VISIBLE);
            View debugButton = activity.findViewById(R.id.debugButton);
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

    public static void onTrackingFragment(final TrackingFragment fragment) {
        if (BuildConfig.DEBUG) {
            final SharedPreferences sharedPreferences = DebugHelper.getSharedPreferences(fragment.getActivity());
            final FloatingActionButton createTripButton = fragment.getActivity().findViewById(R.id.createTripButton);

            String currentTripId = "";
            try {
                JSONObject currentTrip = new JSONObject(sharedPreferences.getString("current_trip", ""));
                currentTripId = currentTrip.getString("trip_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(currentTripId)) {
                createTripButton.setBackgroundTintList(
                        ColorStateList.valueOf(fragment.getActivity().getResources().getColor(R.color.colorPrimary)));
            } else {
                createTripButton.setBackgroundTintList(
                        ColorStateList.valueOf(fragment.getActivity().getResources().getColor(R.color.colorAccent)));
            }
            createTripButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String accountid = sharedPreferences.getString(DebugHelper.DEV_ACCOUNTID_KEY, "");
                    String secretkey = sharedPreferences.getString(DebugHelper.DEV_SECRETKEY_KEY, "");
                    if (TextUtils.isEmpty(accountid) || TextUtils.isEmpty(secretkey)) {
                        fragment.getActivity().startActivity(new Intent(fragment.getActivity(), DebugActivity.class));
                        Toast.makeText(fragment.getActivity(), "You need to provide all data in \"Set REST API\"", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String currentTripId = "";
                    try {
                        JSONObject currentTrip = new JSONObject(sharedPreferences.getString("current_trip", ""));
                        currentTripId = currentTrip.getString("trip_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (TextUtils.isEmpty(currentTripId)) {
                        LatLng dest = fragment.getTripsManager().getDestLatLng();
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("device_id", HyperTrack.getDeviceId());

                            if (dest != null) {
                                JSONArray coordinates = new JSONArray();
                                coordinates.put(dest.longitude);
                                coordinates.put(dest.latitude);

                                JSONObject geometry = new JSONObject();
                                geometry.put("type", "Point");
                                geometry.put("coordinates", coordinates);

                                JSONObject destination = new JSONObject();
                                destination.put("geometry", geometry);
                                destination.put("radius", 100);

                                jsonObject.put("destination", destination);
                            }

                            fragment.getLoader().start();
                            fragment.getApiHelper().createTrip(jsonObject, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    sharedPreferences.edit().putString("current_trip", response.toString()).commit();
                                    if (fragment.getActivity() != null) {
                                        fragment.getLoader().stop();
                                        createTripButton.setBackgroundTintList(
                                                ColorStateList.valueOf(fragment.getActivity().getResources().getColor(R.color.colorAccent)));
                                        Toast.makeText(fragment.getActivity(), "Trip is created", Toast.LENGTH_SHORT).show();
                                        fragment.onTrackingStart();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (fragment.getActivity() != null) {
                                        fragment.getLoader().stop();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        fragment.getLoader().start();
                        final Response.Listener<String> listener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                sharedPreferences.edit().putString("current_trip", "").apply();
                                if (fragment.getActivity() != null) {
                                    fragment.getLoader().stop();
                                    createTripButton.setBackgroundTintList(
                                            ColorStateList.valueOf(fragment.getActivity().getResources().getColor(R.color.colorPrimary)));
                                    Toast.makeText(fragment.getActivity(), "Trip is completed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                        fragment.getApiHelper().completeTrip(currentTripId, listener, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                listener.onResponse("");
                            }
                        });
                    }
                }
            });
        }
    }
}
