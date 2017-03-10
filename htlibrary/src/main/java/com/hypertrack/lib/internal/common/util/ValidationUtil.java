package com.hypertrack.lib.internal.common.util;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackUtils;
import com.hypertrack.lib.models.Error;
import com.hypertrack.lib.models.ErrorResponse;

/**
 * Created by ulhas on 09/05/16.
 */

public class ValidationUtil {

    public static ErrorResponse getStartTrackingValidationError(Context context) {
        if (!HyperTrackUtils.isPublishableKeyConfigured(context)) {
            return new ErrorResponse(Error.Type.PUBLISHABLE_KEY_NOT_CONFIGURED);
        }

        if (TextUtils.isEmpty(HyperTrack.getUserId())) {
            return new ErrorResponse(Error.Type.USER_ID_NOT_CONFIGURED);
        }

        if (HyperTrackUtils.isPlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
            return new ErrorResponse(Error.Type.PLAY_SERVICES_UNAVAILABLE);
        }

        if (!HyperTrackUtils.isLocationPermissionAvailable(context)) {
            return new ErrorResponse(Error.Type.PERMISSIONS_NOT_REQUESTED);
        }

        if (!HyperTrackUtils.isLocationEnabled(context)) {
            return new ErrorResponse(Error.Type.LOCATION_SETTINGS_DISABLED);
        }

        if (!HyperTrackUtils.isLocationAccuracyHigh(context)) {
            return new ErrorResponse(Error.Type.LOCATION_SETTINGS_LOW_ACCURACY);
        }

        return null;
    }

    public static ErrorResponse getValidationError(Context context) {
        if (!HyperTrackUtils.isPublishableKeyConfigured(context)) {
            return new ErrorResponse(Error.Type.PUBLISHABLE_KEY_NOT_CONFIGURED);
        }

        if (TextUtils.isEmpty(HyperTrack.getUserId())) {
            return new ErrorResponse(Error.Type.USER_ID_NOT_CONFIGURED);
        }

        return null;
    }

    public static ErrorResponse getNetworkCallValidationError(Context context) {
        if (!HyperTrackUtils.isPublishableKeyConfigured(context)) {
            return new ErrorResponse(Error.Type.PUBLISHABLE_KEY_NOT_CONFIGURED);
        }

        if (!HyperTrackUtils.isInternetConnected(context)) {
            return new ErrorResponse(Error.Type.NETWORK_CONNECTIVITY_ERROR);
        }

        return null;
    }
}
