package io.hypertrack.meta.util.images;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by piyush on 23/06/16.
 */
public class LocationUtils {

    public static String getNameFromLatLng(Context context, Double lat, Double lng) {
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        StringBuffer placeName = new StringBuffer();
        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);

                placeName.append(address.getAddressLine(0));
                for (int i = 1; i <= address.getMaxAddressLineIndex(); i++) {
                    if (address.getAddressLine(i) != null) {
                        placeName.append(", " + address.getAddressLine(i));
                    }
                }
//                if (address.getSubLocality() != null) {
//                    placeName.append(address.getSubLocality() + ", ");
//                }
//
//                if (address.getLocality() != null) {
//                    placeName.append(address.getLocality() + ", ");
//                }
//
//                if (address.getAdminArea() != null) {
//                    placeName.append(address.getAdminArea());
//                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return placeName.toString();
    }
}
