package io.hypertrack.sendeta.callback;

import io.hypertrack.sendeta.model.ETAResponse;

/**
 * Created by piyush on 15/08/16.
 */
public abstract class ETACallback {
    public abstract void OnSuccess(ETAResponse etaResponse);
    public abstract void OnError();
}
