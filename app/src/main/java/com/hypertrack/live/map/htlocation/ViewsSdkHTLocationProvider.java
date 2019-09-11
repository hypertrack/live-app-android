package com.hypertrack.live.map.htlocation;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.views.DeviceUpdatesHandler;
import com.hypertrack.sdk.views.HyperTrackViews;
import com.hypertrack.sdk.views.dao.MovementStatus;
import com.hypertrack.sdk.views.dao.StatusUpdate;
import com.hypertrack.sdk.views.dao.Trip;

public class ViewsSdkHTLocationProvider implements HTLocationProvider {

    private HyperTrackViews hypertrackView;
    private static Location lastKnownLocation;

    public ViewsSdkHTLocationProvider(Context context, String hyperTrackPublicKey) {
        hypertrackView = HyperTrackViews.getInstance(context, hyperTrackPublicKey);
    }

    public static Location convertFrom(@NonNull com.hypertrack.sdk.views.dao.Location locationModel) {
        Location location = new Location(locationModel.getRecordedAt());
        location.setLatitude(locationModel.getLatitude());
        location.setLongitude(locationModel.getLongitude());
        location.setAltitude(locationModel.getAltitude() != null ? locationModel.getAltitude() : 0.0f);
        location.setAccuracy(locationModel.getAccuracy() != null ? locationModel.getAccuracy().floatValue() : 0.0f);
        location.setBearing(locationModel.getBearing() != null ? locationModel.getBearing().floatValue() : 0.0f);
        location.setSpeed(locationModel.getSpeed() != null ? locationModel.getSpeed().floatValue() : 0.0f);
        location.setElapsedRealtimeNanos(System.nanoTime());
        return location;

    }

    @Override
    public boolean startLocationProvider(final HTLocationConsumer myLocationConsumer) {
        hypertrackView.getDeviceMovementStatus(HyperTrack.getDeviceId(), new Consumer<MovementStatus>() {
            @Override
            public void accept(MovementStatus movementStatus) {
                Log.e("accept", "call");
                if (movementStatus != null && movementStatus.location != null) {
                    lastKnownLocation = convertFrom(movementStatus.location);
                    myLocationConsumer.onLocationChanged(lastKnownLocation,
                            ViewsSdkHTLocationProvider.this);
                }
            }
        });
        hypertrackView.subscribeToDeviceUpdates(HyperTrack.getDeviceId(), new DeviceUpdatesHandler() {
            @Override
            public void onLocationUpdateReceived(@NonNull com.hypertrack.sdk.views.dao.Location location) {
                Log.e("onLocationUpdateReceived", "call");
                lastKnownLocation = convertFrom(location);
                if (hypertrackView != null) {
                    myLocationConsumer.onLocationChanged(lastKnownLocation,
                            ViewsSdkHTLocationProvider.this);
                }
            }

            @Override
            public void onBatteryStateUpdateReceived(int i) {

            }

            @Override
            public void onStatusUpdateReceived(@NonNull StatusUpdate statusUpdate) {

            }

            @Override
            public void onTripUpdateReceived(@NonNull Trip trip) {

            }

            @Override
            public void onError(Exception e, String s) {

            }

            @Override
            public void onCompleted(String s) {

            }
        });
        return false;
    }

    @Override
    public void stopLocationProvider() {
        hypertrackView.stopAllUpdates();
    }

    @Override
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    @Override
    public void destroy() {
        hypertrackView.stopAllUpdates();
        hypertrackView = null;
    }
}
