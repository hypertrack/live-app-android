
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 11/07/16.
 */
public class FetchLocationIntentService extends IntentService{

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "io.hypertrack.meta";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_ADDRESS_KEY";
    public static final String ADDRESS_DATA_EXTRA = PACKAGE_NAME +
            ".ADDRESS_DATA_EXTRA";

    private static final String TAG = "FetchAddressService";

    public FetchLocationIntentService() {
        super("FetchLocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);

        // Get the location passed to this service through an extra.
        String addressToGeocode = intent.getStringExtra(ADDRESS_DATA_EXTRA);

        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocationName(addressToGeocode,
                    // In this sample, get just a single address.
                     1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_lng_used);
            Log.e(TAG, errorMessage, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(FAILURE_RESULT, new LatLng(0.0, 0.0), receiver);
        } else {
            Address address = addresses.get(0);

            if (address.getLatitude() != 0.0 || address.getLongitude() != 0.0) {
                deliverResultToReceiver(SUCCESS_RESULT, new LatLng(address.getLatitude(), address.getLongitude()), receiver);
            } else {
                deliverResultToReceiver(FAILURE_RESULT, new LatLng(0.0, 0.0), receiver);
            }
        }
    }

    private void deliverResultToReceiver(int resultCode, LatLng latLng, ResultReceiver resultReceiver) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RESULT_DATA_KEY, latLng);
        resultReceiver.send(resultCode, bundle);
    }
}
