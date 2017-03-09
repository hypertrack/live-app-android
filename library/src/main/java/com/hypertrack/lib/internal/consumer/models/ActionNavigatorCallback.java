package com.hypertrack.lib.internal.consumer.models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by suhas on 30/08/15.
 */
public interface ActionNavigatorCallback {
    void moveToLocationWithTimeInterval(String actionID, LatLng cordinate, long timeDuration);
}
