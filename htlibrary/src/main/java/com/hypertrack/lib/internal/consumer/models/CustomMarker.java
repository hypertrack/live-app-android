package com.hypertrack.lib.internal.consumer.models;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by piyush on 13/10/16.
 */
public class CustomMarker {
    private MarkerOptions markerOptions;
    private Marker marker;

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public CustomMarker() {
    }

    public CustomMarker(MarkerOptions markerOptions, Marker marker) {
        this.markerOptions = markerOptions;
        this.marker = marker;
    }
}
