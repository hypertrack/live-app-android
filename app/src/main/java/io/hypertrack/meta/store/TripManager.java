package io.hypertrack.meta.store;

import android.app.PendingIntent;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import io.hypertrack.lib.common.HyperTrack;
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
import io.hypertrack.meta.store.callback.TripETACallback;
import io.hypertrack.meta.store.callback.TripManagerCallback;
import io.hypertrack.meta.store.callback.TripManagerListener;
import io.hypertrack.meta.store.callback.UserStoreGetTaskCallback;
import io.hypertrack.meta.util.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ulhas on 18/06/16.
 */
public class TripManager {
    private HTTransmitterService transmitter = HTTransmitterService.getInstance(MetaApplication.getInstance().getApplicationContext());
    private SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

    private TripManagerListener tripRefreshedListener;
    private TripManagerListener tripEndedListener;

    private Trip trip;
    private MetaPlace place;

    private HTTrip hyperTrackTrip;
    private HTDriverVehicleType vehicleType = HTDriverVehicleType.CAR;
    private Timer refreshTimer;
    private PendingIntent mGeofencePendingIntent;

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
                    callback.OnSuccess(etaResponses.get(0));
                } else {
                    callback.OnError();
                }
            }

            @Override
            public void onFailure(Call<List<TripETAResponse>> call, Throwable t) {
                callback.OnError();
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
                trip = response.body();
                callback.OnSuccess();
            }

            @Override
            public void onFailure(Call<Trip> call, Throwable t) {
                callback.OnError();
            }
        });
    }

    public void sendETA(List<String> phoneNumbers, final TripManagerCallback callback) {

    }

    public void startTrip(final TripManagerCallback callback) {
        this.transmitter.clearCurrentTrip(); //TODO: Remove this
        if (this.place == null) {
            callback.OnError();
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
                                callback.OnSuccess();
                            }

                            @Override
                            public void OnError() {
                                clearState();
                                callback.OnError();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.OnError();
                    }
                });
            }

            @Override
            public void OnError() {
                callback.OnError();
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
                        callback.OnError();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                callback.OnError();
            }
        });
    }

    public void clearState() {
        this.trip = null;
        this.place = null;
        this.hyperTrackTrip = null;
        this.vehicleType = null;
        this.stopRefreshingTrip();
        this.clearListeners();
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

    }

    private void stopRefreshingTrip() {
        if (this.refreshTimer != null) {
            this.refreshTimer.cancel();
            this.refreshTimer = null;
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

    private void setupGeofencing() {

    }

    public MetaPlace getPlace() {
        return place;
    }

    public void setPlace(MetaPlace place) {
        this.place = place;
    }

    public Trip getTrip() {
        return this.trip;
    }
}
