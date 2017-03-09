package com.hypertrack.lib.internal.transmitter.utils;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;

/**
 * Created by Arjun on 11/06/16.
 */

public class GPSStatusListener implements GpsStatus.Listener {

    private LocationManager locationManager;
    private boolean gpsFix;

    public GPSStatusListener(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public void onGpsStatusChanged(int changeType) {
        if (locationManager != null) {
            GpsStatus status = locationManager.getGpsStatus(null);

            switch(changeType) {
                case GpsStatus.GPS_EVENT_FIRST_FIX: // Received first fix
                    gpsFix = true;
                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS: // Check if satellites are in fix
                    for(GpsSatellite sat : status.getSatellites()) {
                        if (sat.usedInFix()) {
                            gpsFix = true;
                            break;
                        } else {
                            gpsFix = false;
                        }
                    }

                    break;

                case GpsStatus.GPS_EVENT_STARTED: // GPS turned on
                    gpsFix = false;
                    break;

                case GpsStatus.GPS_EVENT_STOPPED: // GPS turned off
                    gpsFix = false;
                    break;

                default:
                    gpsFix = false;
                    return;
            }
        }
    }

    public String getProvider() {
        if (gpsFix) {
            return "gps";
        } else {
            return "non_gps";
        }
    }
}
