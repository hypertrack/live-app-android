package io.hypertrack.sendeta.store.callback;

import io.hypertrack.sendeta.model.TripETAResponse;

/**
 * Created by ulhas on 18/06/16.
 */
public abstract class TripETACallback {
    public abstract void OnSuccess(TripETAResponse etaResponse);
    public abstract void OnError();
}
