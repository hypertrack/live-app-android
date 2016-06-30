package io.hypertrack.sendeta.store;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

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

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

}
