package io.hypertrack.sendeta.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by piyush on 20/08/16.
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
    public static boolean areLocationsSame(LatLng param1, LatLng param2) {
        if (param1 == null || param2 == null)
            return false;

        double multiplier = 100000d;
        double lat1 = ((double) Math.round(param1.latitude * multiplier) / multiplier);
        double lat2 = ((double) Math.round(param2.latitude * multiplier) / multiplier);
        double lng1 = ((double) Math.round(param1.longitude * multiplier) / multiplier);
        double lng2 = ((double) Math.round(param2.longitude * multiplier) / multiplier);

        if (lat1 == lat2 && lng1 == lng2) {
            return true;
        }
        return false;
    }
}
