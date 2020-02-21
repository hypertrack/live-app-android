package com.hypertrack.live.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.maps.google.widget.GoogleMapConfig;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MapUtils {

    public static GoogleMapConfig.Builder getBuilder(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return GoogleMapConfig.newBuilder(context)
                .boundingBoxDimensions(width, (int) (height / 1.9));
    }

    public static Single<String> getAddress(Context context, final LatLng latLng) {
        final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (!addresses.isEmpty()) {
                        String formattedAddress = "";
                        Address address = addresses.get(0);
                        if (address.getSubThoroughfare() != null) {
                            formattedAddress += address.getSubThoroughfare();
                        }
                        if (address.getThoroughfare() != null) {
                            if (!formattedAddress.isEmpty()) {
                                formattedAddress += " ";
                            }
                            formattedAddress += address.getThoroughfare();
                        }
                        if (address.getLocality() != null) {
                            if (!formattedAddress.isEmpty()) {
                                formattedAddress += ", ";
                            }
                            formattedAddress += address.getLocality();
                        }
                        return formattedAddress;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "";
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
