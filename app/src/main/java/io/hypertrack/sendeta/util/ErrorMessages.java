package io.hypertrack.sendeta.util;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.GeofenceStatusCodes;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 29/06/16.
 */
public class ErrorMessages {
    // Register ErrorMessages
    public static final String INVALID_PHONE_NUMBER = "Please enter a valid number";

    // Profile ErrorMessages
    public static final String PROFILE_UPDATE_FAILED = "We had problem connecting with the server. Please try again in sometime";
    public static final String PROFILE_PIC_UPLOAD_FAILED = "There was an error uploading the profile pic. Please try again";

    // Trip ErrorMessages
    public static final String SHARE_LIVE_LOCATION_FAILED = "There was an error sharing live location. Please try again";
    public static final String STOP_SHARING_FAILED = "There was an error while trying to stop sharing. Please try again";

    /**
     * Returns the error string for a geofencing error code.
     */
    public static String getGeofenceErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return mResources.getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return mResources.getString(R.string.geofence_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return mResources.getString(R.string.geofence_too_many_pending_intents);
            default:
                return mResources.getString(R.string.unknown_geofence_error);
        }
    }
}
