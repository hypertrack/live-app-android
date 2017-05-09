package io.hypertrack.sendeta.presenter;

import com.hypertrack.lib.models.Place;

import java.util.List;

import io.hypertrack.sendeta.store.ActionManager;

/**
 * Created by piyush on 07/05/17.
 */

public interface IHomePresenter<V> extends Presenter<V> {
    void getETAForExpectedPlace(final Place expectedPlace, final String selectedVehicleType, final ActionManager actionManager);

    void shareLiveLocation(final ActionManager actionManager, final String lookupID, final Place expectedPlace);

    void stopSharing(final ActionManager actionManager);

    void shareTrackingUrl(final ActionManager actionManager);

    void openNavigationForExpectedPlace(final ActionManager actionManager);

    void trackActionsOnMap(String lookupID, List<String> actionIDs, ActionManager actionManager);
}
