package io.hypertrack.sendeta.adapter.callback;

import io.hypertrack.sendeta.model.UserPlace;

/**
 * Created by ulhas on 23/06/16.
 */
public abstract class PlaceAutoCompleteOnClickListener {
    public abstract void OnSuccess(UserPlace place);
    public abstract void OnError();
}
