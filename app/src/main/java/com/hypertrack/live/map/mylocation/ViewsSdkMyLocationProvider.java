package com.hypertrack.live.map.mylocation;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.views.DeviceUpdatesHandler;
import com.hypertrack.sdk.views.HyperTrackViews;
import com.hypertrack.sdk.views.dao.MovementStatus;
import com.hypertrack.sdk.views.dao.StatusUpdate;
import com.hypertrack.sdk.views.dao.Trip;

public class ViewsSdkMyLocationProvider implements MyLocationProvider {

    private HyperTrackViews hypertrackView;
    private static Location lastKnownLocation;

    public ViewsSdkMyLocationProvider(Context context, String hyperTrackPublicKey) {
        hypertrackView = HyperTrackViews.getInstance(context, hyperTrackPublicKey);
    }

    public static Location convertFrom(@NonNull com.hypertrack.sdk.views.dao.Location locationModel) {
        Location location = new Location(locationModel.recordedAt);
        location.setLatitude(locationModel.latitude);
        location.setLongitude(locationModel.longitude);
        location.setAltitude(locationModel.altitude != null ? locationModel.altitude : 0.0f);
        location.setAccuracy(locationModel.accuracy != null ? locationModel.accuracy.floatValue() : 0.0f);
        location.setBearing(locationModel.bearing != null ? locationModel.bearing.floatValue() : 0.0f);
        location.setSpeed(locationModel.speed != null ? locationModel.speed.floatValue() : 0.0f);
        location.setElapsedRealtimeNanos(System.nanoTime());
        return location;

    }

    @Override
    public boolean startLocationProvider(final MyLocationConsumer myLocationConsumer) {
        hypertrackView.getDeviceMovementStatus(HyperTrack.getDeviceId(), new Consumer<MovementStatus>() {
            @Override
            public void accept(MovementStatus movementStatus) {
                if (movementStatus != null && movementStatus.location != null) {
                    lastKnownLocation = convertFrom(movementStatus.location);
                    myLocationConsumer.onLocationChanged(lastKnownLocation,
                            ViewsSdkMyLocationProvider.this);
                }
            }
        });
        hypertrackView.subscribeToDeviceUpdates(HyperTrack.getDeviceId(), new DeviceUpdatesHandler() {
            @Override
            public void onLocationUpdateReceived(@NonNull com.hypertrack.sdk.views.dao.Location location) {
                lastKnownLocation = convertFrom(location);
                myLocationConsumer.onLocationChanged(lastKnownLocation,
                        ViewsSdkMyLocationProvider.this);
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
