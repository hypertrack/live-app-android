package io.hypertrack.sendeta.view;

import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.Place;

import java.util.List;

import io.hypertrack.sendeta.model.ETAResponse;

/**
 * Created by suhas on 25/02/16.
 */
public interface HomeView {
    void showGetETAForExpectedPlaceSuccess(ETAResponse etaResponse, Place expectedPlace);

    void showGetETAForExpectedPlaceError(ErrorResponse errorResponse, Place expectedPlace);

    void showShareLiveLocationError(ErrorResponse errorResponse);

    void showShareLiveLocationSuccess(Action action);

    void showStopSharingError();

    void showStopSharingSuccess();

    void showShareTrackingUrlError();

    void showShareTrackingUrlSuccess(String shareMessage);

    void showOpenNavigationError();

    void showOpenNavigationSuccess(double latitude, double longitude);

    void showTrackActionsOnMapSuccess(List<Action> actions);

    void showTrackActionsOnMapError(ErrorResponse errorResponse);
}
