package com.hypertrack.lib.callbacks;

import android.support.annotation.NonNull;

import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

/**
 * Created by piyush on 18/02/17.
 */
public abstract class HyperTrackCallback {

    /**
     * Called when a request succeeds.
     *
     * @param response The successful response containing the responseObject.
     */
    public abstract void onSuccess(@NonNull SuccessResponse response);

    /**
     * Called when a validation error occurs, request times out, or fails.
     *
     * @param errorResponse The request status.
     */
    public abstract void onError(@NonNull ErrorResponse errorResponse);
}
