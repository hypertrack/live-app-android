package io.hypertrack.meta.adapter.callback;

import io.hypertrack.meta.model.MetaPlace;

/**
 * Created by ulhas on 24/06/16.
 */
public interface FavoritePlaceOnClickListener {
    void OnAddHomeClick();
    void OnEditHomeClick(MetaPlace place);
    void OnAddWorkClick();
    void OnEditWorkClick(MetaPlace place);
    void OnAddPlaceClick();
    void OnEditPlaceClick(MetaPlace place);
    void OnDeletePlace(MetaPlace place);
}
