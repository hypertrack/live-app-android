package io.hypertrack.meta.store;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.hypertrack.lib.common.model.HTDriverVehicleType;
import io.hypertrack.lib.transmitter.model.HTTrip;
import io.hypertrack.lib.transmitter.model.HTTripParams;
import io.hypertrack.lib.transmitter.model.HTTripParamsBuilder;
import io.hypertrack.lib.transmitter.model.callback.HTCompleteTaskStatusCallback;
import io.hypertrack.lib.transmitter.model.callback.HTTripStatusCallback;
import io.hypertrack.lib.transmitter.service.HTTransmitterService;
import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.model.Trip;
import io.hypertrack.meta.model.TripETAResponse;
import io.hypertrack.meta.network.retrofit.SendETAService;
import io.hypertrack.meta.network.retrofit.ServiceGenerator;
import io.hypertrack.meta.service.GeofenceTransitionsIntentService;
import io.hypertrack.meta.store.callback.TripETACallback;
import io.hypertrack.meta.store.callback.TripManagerCallback;
import io.hypertrack.meta.store.callback.TripManagerListener;
import io.hypertrack.meta.store.callback.UserStoreGetTaskCallback;
import io.hypertrack.meta.util.Constants;
import io.hypertrack.meta.util.SharedPreferenceManager;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ulhas on 18/06/16.
 */
public class TripManager implements GoogleApiClient.ConnectionCallbacks {

    public static final int LOITERING_DELAY_MS = 30000;
    private static final String GEOFENCE_REQUEST_ID = "io.hypertrack.meta:GeoFence";
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;

    private static final String TAG = TripManager.class.getSimpleName();
    private static final long REFRESH_DELAY = 60000;

    private HTTransmitterService transmitter = HTTransmitterService.getInstance(MetaApplication.getInstance().getApplicationContext());
    private SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

    private TripManagerListener tripRefreshedListener;
    private TripManagerListener tripEndedListener;
    private TripManagerListener tripOnRestoreStateListener;

    private Realm realm = Realm.getDefaultInstance();

    private Trip trip;
    private MetaPlace place;

    private HTTrip hyperTrackTrip;
    private HTDriverVehicleType vehicleType = HTDriverVehicleType.CAR;

    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleAPIClient;

    private Handler handler;

    private static TripManager sSharedManager;

    public static TripManager getSharedManager() {
        if (sSharedManager == null) {
            sSharedManager = new TripManager();
        }

        return sSharedManager;
    }

    private TripManager() {
        this.setupGoogleAPIClient();
        this.restoreState();
    }

    private void setupGoogleAPIClient() {
        this.mGoogleAPIClient = new GoogleApiClient.Builder(MetaApplication.getInstance().getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

        this.mGoogleAPIClient.connect();
    }

    private void restoreState() {
        this.trip = realm.where(Trip.class).findFirst();
        this.place = SharedPreferenceManager.getPlace();

        if (this.trip != null
                && this.place != null
                && transmitter.isTripActive()
                && transmitter.getActiveTripID().equalsIgnoreCase(this.trip.getHypertrackTripID())) {
            transmitter.refreshTrip(new HTTripStatusCallback() {
                @Override
                public void onSuccess(HTTrip htTrip) {
                    hyperTrackTrip = htTrip;
                    onTripStart();
                    onTripRefresh();
                }

                @Override
                public void onError(Exception e) {
                    clearState();
                }
            });

            if (tripOnRestoreStateListener != null) {
                tripOnRestoreStateListener.OnCallback();
            }
        } else {
            this.clearState();
        }
    }

    final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            transmitter.refreshTrip(new HTTripStatusCallback() {
                @Override
                public void onSuccess(HTTrip htTrip) {
                    hyperTrackTrip = htTrip;
                    onTripRefresh();
                }

                @Override
                public void onError(Exception e) {

                }
            });

            handler.postDelayed(this, REFRESH_DELAY);
        }
    };

    private void onTripRefresh() {
        if (this.tripRefreshedListener != null) {
            this.tripRefreshedListener.OnCallback();
        }
    }

    public boolean isTripActive() {
        return (this.hyperTrackTrip != null);
    }

    public void getETA(LatLng origin, LatLng destination, final TripETACallback callback) {
        String originQueryParam = origin.latitude + "," + origin.longitude;
        String destinationQueryParam = destination.latitude + "," + destination.longitude;

        Call<List<TripETAResponse>> call = sendETAService.getETA(originQueryParam, destinationQueryParam);
        call.enqueue(new Callback<List<TripETAResponse>>() {
            @Override
            public void onResponse(Call<List<TripETAResponse>> call, Response<List<TripETAResponse>> response) {
                List<TripETAResponse> etaResponses = response.body();

                if (etaResponses.size() > 0) {
                    if (callback != null) {
                        callback.OnSuccess(etaResponses.get(0));
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TripETAResponse>> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void addTrip(final TripManagerCallback callback) {
        HashMap<String, String> tripDetails = new HashMap<>();
        tripDetails.put("hypertrack_trip_id", this.hyperTrackTrip.getId());
        tripDetails.put("hypertrack_task_id", this.hyperTrackTrip.getTaskIDs().get(0));

        Call<Trip> call = sendETAService.addTrip(tripDetails);
        call.enqueue(new Callback<Trip>() {
            @Override
            public void onResponse(Call<Trip> call, Response<Trip> response) {
                setTrip(response.body());
                if (callback != null) {
                    callback.OnSuccess();
                }
            }

            @Override
            public void onFailure(Call<Trip> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void sendETA(List<String> phoneNumbers, final TripManagerCallback callback) {

    }

    public void startTrip(final TripManagerCallback callback) {
        this.transmitter.clearCurrentTrip(); //TODO: Remove this
        if (this.place == null) {
            if (callback != null) {
                callback.OnError();
            }
        }

        UserStore.sharedStore.getTask(this.place, new UserStoreGetTaskCallback() {
            @Override
            public void OnSuccess(String taskID) {
                HTTripParams tripParams = getTripParams(taskID);
                transmitter.startTrip(tripParams, new HTTripStatusCallback() {
                    @Override
                    public void onSuccess(HTTrip htTrip) {
                        hyperTrackTrip = htTrip;

                        addTrip(new TripManagerCallback() {
                            @Override
                            public void OnSuccess() {
                                onTripStart();
                                if (callback != null) {
                                    callback.OnSuccess();
                                }
                            }

                            @Override
                            public void OnError() {
                                clearState();
                                transmitter.clearCurrentTrip();
                                if (callback != null) {
                                    callback.OnError();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        if (callback != null) {
                            callback.OnError();
                        }
                    }
                });
            }

            @Override
            public void OnError() {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void endTrip(final TripManagerCallback callback) {
        String taskID = this.hyperTrackTrip.getTaskIDs().get(0);

        transmitter.completeTask(taskID, new HTCompleteTaskStatusCallback() {
            @Override
            public void onSuccess(String s) {
                transmitter.endTrip(new HTTripStatusCallback() {
                    @Override
                    public void onSuccess(HTTrip htTrip) {
                        callback.OnSuccess();
                        clearState();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (callback != null) {
                            callback.OnError();
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void clearState() {
        this.trip = null;
        this.hyperTrackTrip = null;
        this.vehicleType = null;
        this.stopRefreshingTrip();
        this.stopGeofencing();
        this.clearListeners();
        this.clearPlace();
        this.clearTrip();
        transmitter.clearCurrentTrip();
    }

    private void clearListeners() {
        this.tripEndedListener = null;
        this.tripRefreshedListener = null;
    }

    private void onTripStart() {
        this.setupGeofencing();
        this.startRefreshingTrip();
    }

    private void startRefreshingTrip() {
        handler = new Handler();
        handler.postDelayed(refreshTask, REFRESH_DELAY);
    }

    private void stopRefreshingTrip() {
        if (this.handler != null) {
            this.handler.removeCallbacks(refreshTask);
            this.handler = null;
        }
    }

    private HTTripParams getTripParams(String taskID) {
        ArrayList<String> taskIDs = new ArrayList<>();
        taskIDs.add(taskID);

        return new HTTripParamsBuilder()
                .setDriverID(UserStore.sharedStore.getUser().getHypertrackDriverID())
                .setTaskIDs(taskIDs)
                .setVehicleType(this.vehicleType)
                .createHTTripParams();
    }

    private void stopGeofencing() {
        if (this.mGeofencePendingIntent != null) {
            LocationServices.GeofencingApi.removeGeofences(mGoogleAPIClient, mGeofencePendingIntent);
            mGeofencePendingIntent = null;
        }
    }

    private void setupGeofencing() {
        GeofencingRequest request = this.getGeofencingRequest();

        Context context = MetaApplication.getInstance().getAppContext();
        Intent geofencingIntent = new Intent(context, GeofenceTransitionsIntentService.class);
        mGeofencePendingIntent = PendingIntent.getService(context, 0, geofencingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleAPIClient, request, mGeofencePendingIntent).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.v(TAG, "Geofencing added successfully");
                    } else {
                        Log.v(TAG, "Geofencing not added. There was an error");
                    }
                }
            });
        } catch (SecurityException exception) {
            Log.v(TAG, "Exeption for geo fence");
        }
    }

    public void OnGeoFenceSuccess() {
        this.endTrip(new TripManagerCallback() {
            @Override
            public void OnSuccess() {
                if (tripEndedListener != null) {
                    tripEndedListener.OnCallback();
                }
            }

            @Override
            public void OnError() {

            }
        });
    }

    private GeofencingRequest getGeofencingRequest() {

        List<Geofence> geoFenceList = new ArrayList<Geofence>();
        geoFenceList.add(new Geofence.Builder()
                .setRequestId(GEOFENCE_REQUEST_ID)
                .setCircularRegion(
                        this.place.getLatitude(),
                        this.place.getLongitude(),
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(LOITERING_DELAY_MS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build());

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geoFenceList);
        return builder.build();
    }

    public MetaPlace getPlace() {
        return place;
    }

    public void setPlace(MetaPlace place) {
        this.place = place;
        this.savePlace();
    }

    private void clearPlace() {
        this.deletePlace();
        this.place = null;
    }

    public Trip getTrip() {
        return this.trip;
    }

    public void setTripRefreshedListener(TripManagerListener listener) {
        this.tripRefreshedListener = listener;
    }

    public void setTripEndedListener(TripManagerListener listener) {
        this.tripEndedListener = listener;
    }

    public HTTrip getHyperTrackTrip() {
        return this.hyperTrackTrip;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void savePlace() {
        SharedPreferenceManager.setPlace(this.place);
    }

    private void deletePlace() {
        SharedPreferenceManager.deletePlace();
    }

    private void clearTrip() {
        final Trip tripToDelete = realm.where(Trip.class).findFirst();
        if (tripToDelete == null) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                tripToDelete.deleteFromRealm();
            }
        });

        this.trip = null;
    }

    private void setTrip(final Trip tripToSave) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                trip = realm.copyToRealm(tripToSave);
            }
        });
    }
}
