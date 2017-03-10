package com.hypertrack.lib.callbacks;

import android.support.annotation.NonNull;

import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;
import com.hypertrack.lib.models.ErrorResponse;

/**
 * Created by piyush on 06/03/17.
 */

public abstract class HyperTrackEventCallback {

    /**
     * Called when an event occurs in the SDK.
     *
     * @param event Instance of HyperTrack event containing details about the event.
     */
    public abstract void onEvent(@NonNull HyperTrackEvent event);

    /**
     * Called when a validation error occurs, request times out, or fails.
     *
     * @param errorResponse The request status.
     */
    public abstract void onError(@NonNull ErrorResponse errorResponse);
}
