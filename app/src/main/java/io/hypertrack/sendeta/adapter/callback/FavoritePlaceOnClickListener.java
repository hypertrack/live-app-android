package io.hypertrack.sendeta.adapter.callback;

import io.hypertrack.sendeta.model.UserPlace;

/**
 * Created by ulhas on 24/06/16.
 */
public interface FavoritePlaceOnClickListener {
    void OnAddHomeClick();

    void OnEditHomeClick(UserPlace place);
    void OnAddWorkClick();

    void OnEditWorkClick(UserPlace place);
    void OnAddPlaceClick();

    void OnEditPlaceClick(UserPlace place);

    void OnDeletePlace(UserPlace place);
}
