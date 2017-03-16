package io.hypertrack.sendeta.store.callback;

import java.util.List;

import io.hypertrack.sendeta.model.UserPlace;

/**
 * Created by ulhas on 21/06/16.
 */
public abstract class PlaceManagerGetPlacesCallback {
    public abstract void OnSuccess(List<UserPlace> places);
    public abstract void OnError();
}
