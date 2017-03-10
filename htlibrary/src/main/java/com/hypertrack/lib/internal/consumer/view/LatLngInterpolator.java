package com.hypertrack.lib.internal.consumer.view;

/**
 * Created by ulhas on 27/06/16.
 */

import com.google.android.gms.maps.model.LatLng;

/** package */ class LatLngInterpolator {

    public LatLng interpolate(float fraction, LatLng a, LatLng b) {
        double lat = (b.latitude - a.latitude) * fraction + a.latitude;
        double lng = (b.longitude - a.longitude) * fraction + a.longitude;
        return new LatLng(lat, lng);
    }
}
