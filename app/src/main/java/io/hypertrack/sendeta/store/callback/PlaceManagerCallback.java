package io.hypertrack.sendeta.store.callback;

import io.hypertrack.sendeta.model.UserPlace;

/**
 * Created by ulhas on 21/06/16.
 */
public abstract class PlaceManagerCallback {
    public abstract void OnSuccess(UserPlace place);
    public abstract void OnError();
}
