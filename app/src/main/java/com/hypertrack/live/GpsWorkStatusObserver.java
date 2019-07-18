package com.hypertrack.live;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Build;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GpsWorkStatusObserver {

    private final LocationManager locationManager;

    private final Map<OnStatusChangedListener, Object> listeners = new ConcurrentHashMap<>();

    public GpsWorkStatusObserver(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean isGpsEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    public synchronized void register(final OnStatusChangedListener gpsWorkStatusListener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GnssStatus.Callback innerListener = new GnssStatus.Callback() {
                @Override
                public void onStarted() {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        gpsWorkStatusListener.onGpsStarted();
                    }
                }

                @Override
                public void onStopped() {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        gpsWorkStatusListener.onGpsStopped();
                    }
                }

                @Override
                public void onFirstFix(int ttffMillis) {
                }

                @Override
                public void onSatelliteStatusChanged(GnssStatus status) {
                }
            };
            listeners.put(gpsWorkStatusListener, innerListener);
            locationManager.registerGnssStatusCallback(innerListener);
        } else {
            GpsStatus.Listener innerListener = new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int status) {
                    if (status == GpsStatus.GPS_EVENT_STARTED
                            && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        gpsWorkStatusListener.onGpsStarted();
                    } else if (status == GpsStatus.GPS_EVENT_STOPPED
                            && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        gpsWorkStatusListener.onGpsStopped();
                    }
                }
            };
            listeners.put(gpsWorkStatusListener, innerListener);
            locationManager.addGpsStatusListener(innerListener);
        }
    }

    public synchronized void unregister(final OnStatusChangedListener gpsWorkStatusListener) {
        Object innerListener = listeners.get(gpsWorkStatusListener);
        listeners.remove(gpsWorkStatusListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locationManager.unregisterGnssStatusCallback((GnssStatus.Callback) innerListener);
        } else {
            locationManager.removeGpsStatusListener((GpsStatus.Listener) innerListener);
        }
    }

    public interface OnStatusChangedListener {

        void onGpsStarted();

        void onGpsStopped();

    }

}
