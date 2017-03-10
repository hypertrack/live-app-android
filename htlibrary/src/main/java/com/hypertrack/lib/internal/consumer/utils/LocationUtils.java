package com.hypertrack.lib.internal.consumer.utils;

import com.google.android.gms.maps.model.LatLng;

import com.hypertrack.lib.internal.common.models.GeoJSONLocation;

/**
 * Created by piyush on 30/08/16.
 */
public class LocationUtils {
    /**
     * Utility to decide if two locations are same.
     * We use the assumption that if the latitudes and longitudes
     * for the two locations are equal upto five places after the decimal point,
     * then they are same or equal.
     *
     * @param param1 First LatLng parameter
     * @param param2 Second LatLng parameter
     * @return true if the two locations are same, else false
     */
    public static boolean areLocationsNearby(LatLng param1, LatLng param2) {
        if (param1 == null || param2 == null)
            return false;

        double multiplier = 1000d;
        double lat1 = ((double) Math.round(param1.latitude * multiplier) / multiplier);
        double lat2 = ((double) Math.round(param2.latitude * multiplier) / multiplier);
        double lng1 = ((double) Math.round(param1.longitude * multiplier) / multiplier);
        double lng2 = ((double) Math.round(param2.longitude * multiplier) / multiplier);

        if (Math.abs(lat1 - lat2) <= 1.0 && Math.abs(lng1 - lng2) <= 1.0) {
            return true;
        }
        return false;
    }

    /**
     * Utility to decide if two locations are same.
     * We use the assumption that if the latitudes and longitudes
     * for the two locations are equal upto five places after the decimal point,
     * then they are same or equal.
     *
     * @param location1 First GeoJSONLocation parameter
     * @param location2 Second GeoJSONLocation parameter
     * @return true if the two locations are same, else false
     */
    public static boolean areLocationsNearby(GeoJSONLocation location1, GeoJSONLocation location2) {
        if (location1 == null || location2 == null)
            return false;

        double[] coordinates1 = location1.getCoordinates();
        double[] coordinates2 = location2.getCoordinates();

        if (coordinates1[0] == 0.0 || coordinates1[1] == 0.0 || coordinates2[0] == 0.0 || coordinates2[1] == 0.0)
            return false;

        double multiplier = 1000d;
        double lat1 = ((double) Math.round(coordinates1[1] * multiplier));
        double lat2 = ((double) Math.round(coordinates2[1] * multiplier));
        double lng1 = ((double) Math.round(coordinates1[0] * multiplier));
        double lng2 = ((double) Math.round(coordinates2[0] * multiplier));

        if (Math.abs(lat1 - lat2) <= 1.0 && Math.abs(lng1 - lng2) <= 1.0) {
            return true;
        }
        return false;
    }
}
