package io.hypertrack.meta.store.callback;

import java.util.List;

import io.hypertrack.meta.model.MetaPlace;

/**
 * Created by ulhas on 21/06/16.
 */
public abstract class PlaceManagerGetPlacesCallback {
    public abstract void OnSuccess(List<MetaPlace> places);
    public abstract void OnError();
}
