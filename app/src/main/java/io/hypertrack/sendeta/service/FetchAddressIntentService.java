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
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FetchAddressIntentService extends IntentService {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "io.hypertrack.meta";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";

    private static final String TAG = "FetchAddressService";

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);

        // Get the location passed to this service through an extra.
        LatLng location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);

        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        StringBuffer addressString = new StringBuffer();

        try {
            addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
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
            deliverResultToReceiver(FAILURE_RESULT, errorMessage, receiver);
        } else {
            Address address = addresses.get(0);

            addressString.append(address.getAddressLine(0));
            for (int i = 1; i <= address.getMaxAddressLineIndex(); i++) {
                if (address.getAddressLine(i) != null) {
                    addressString.append(", " + address.getAddressLine(i));
                }
            }

            deliverResultToReceiver(SUCCESS_RESULT, addressString.toString(), receiver);
        }
    }

    private void deliverResultToReceiver(int resultCode, String message, ResultReceiver resultReceiver) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }
}
