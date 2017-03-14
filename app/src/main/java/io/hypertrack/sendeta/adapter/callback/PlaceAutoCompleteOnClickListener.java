package io.hypertrack.sendeta.adapter.callback;

import com.hypertrack.lib.models.Place;

import io.hypertrack.sendeta.model.MetaPlace;

/**
 * Created by ulhas on 23/06/16.
 */
public abstract class PlaceAutoCompleteOnClickListener {
    public abstract void OnSuccess(MetaPlace place);
    public abstract void OnError();
}
