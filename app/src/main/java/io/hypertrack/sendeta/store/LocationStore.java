package io.hypertrack.sendeta.store;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import io.hypertrack.sendeta.util.SharedPreferenceManager;

/**
 * Created by ulhas on 24/06/16.
 */
public class LocationStore {

    private Location currentLocation;
    private static LocationStore sLocationStore;

    public static LocationStore sharedStore() {
        if (sLocationStore == null) {
            sLocationStore = new LocationStore();
        }

        return sLocationStore;
    }

    private LocationStore() {
    }

    public LatLng getCurrentLatLng() {
        if (this.currentLocation == null) {
            return null;
        }

        return new LatLng(this.currentLocation.getLatitude(), this.currentLocation.getLongitude());
    }

    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    public Location getLastKnownUserLocation() {
        return SharedPreferenceManager.getLastKnownLocation() != null ? SharedPreferenceManager.getLastKnownLocation() : new Location("default");
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
        SharedPreferenceManager.setLastKnownLocation(location);
    }

}
