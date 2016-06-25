package io.hypertrack.meta.store.callback;

import io.hypertrack.meta.model.TripETAResponse;
import io.hypertrack.meta.util.SuccessErrorCallback;

/**
 * Created by ulhas on 18/06/16.
 */
public abstract class TripETACallback {
    public abstract void OnSuccess(TripETAResponse etaResponse);
    public abstract void OnError();
}
