package io.hypertrack.sendeta.presenter;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;

import java.util.List;
import java.util.UUID;

import io.hypertrack.sendeta.callback.ActionManagerCallback;
import io.hypertrack.sendeta.callback.ETACallback;
import io.hypertrack.sendeta.model.ETAResponse;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.view.HomeView;

/**
 * Created by piyush on 07/05/17.
 */

public class HomePresenter implements IHomePresenter<HomeView> {

    private static final String TAG = HomePresenter.class.getSimpleName();
    private HomeView view;

    @Override
    public void attachView(HomeView view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void getETAForExpectedPlace(final Place expectedPlace, final String selectedVehicleType, final ActionManager actionManager) {
        if (expectedPlace == null || expectedPlace.getLocation() == null) {
            if (view != null)
                view.showGetETAForExpectedPlaceError(new ErrorResponse(), expectedPlace);
            return;
        }

        HyperTrack.getCurrentLocation(new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                final Location currentLocation = (Location) response.getResponseObject();
                actionManager.getETA(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        expectedPlace.getLocation().getLatitude(), expectedPlace.getLocation().getLongitude(),
                        selectedVehicleType, new ETACallback() {
                            @Override
                            public void OnSuccess(ETAResponse etaResponse) {
                                if (view != null)
                                    view.showGetETAForExpectedPlaceSuccess(etaResponse, expectedPlace);
                            }

                            @Override
                            public void OnError() {
                                ErrorResponse errorResponse = new ErrorResponse();
                                if (view != null)
                                    view.showGetETAForExpectedPlaceError(errorResponse, expectedPlace);
                                Log.e(TAG, "Error occurred in getETAForExpectedPlace: " + errorResponse.getErrorMessage());
                            }
                        });
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                if (view != null)
                    view.showGetETAForExpectedPlaceError(errorResponse, expectedPlace);
                Log.e(TAG, "Error occurred in getETAForExpectedPlace: " + errorResponse.getErrorMessage());
            }
        });
    }

    @Override
    public void shareLiveLocation(final ActionManager actionManager, final String lookupID, final Place expectedPlace) {
        User user = OnboardingManager.sharedManager().getUser();
        if (user == null) {
            if (view != null)
                view.showShareLiveLocationError(new ErrorResponse());
            return;
        }

        ActionParamsBuilder builder = new ActionParamsBuilder()
                .setLookupId(lookupID != null ? lookupID : UUID.randomUUID().toString())
                .setType(Action.ACTION_TYPE_VISIT);

        if (!TextUtils.isEmpty(expectedPlace.getId())) {
            builder.setExpectedPlaceId(expectedPlace.getId());
        } else {
            builder.setExpectedPlace(expectedPlace);
        }

        // Call assignAction to start the tracking action
        HyperTrack.createAndAssignAction(builder.build(), new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                if (response.getResponseObject() != null) {
                    Action action = (Action) response.getResponseObject();
//                    action.getActionDisplay().setDurationRemaining(String.valueOf(etaInMinutes));
                    actionManager.setHyperTrackAction(action);
                    actionManager.onActionStart();

                    HyperTrack.clearServiceNotificationParams();
                    Log.i(TAG, "Share Live Location successful for userID: " + HyperTrack.getUserId());

                    if (view != null)
                        view.showShareLiveLocationSuccess(action);

                } else {
                    if (view != null)
                        view.showShareLiveLocationError(new ErrorResponse());
                }
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                if (view != null)
                    view.showShareLiveLocationError(errorResponse);
                HTLog.e(TAG, "Share Live Location failed with error: " + errorResponse.getErrorMessage());
            }
        });
    }

    @Override
    public void stopSharing(ActionManager actionManager) {
        actionManager.completeAction(new ActionManagerCallback() {
            @Override
            public void OnSuccess() {
                HyperTrack.clearServiceNotificationParams();
                Log.i(TAG, "Stopped sharing live location successfully.");

                if (view != null)
                    view.showStopSharingSuccess();
            }

            @Override
            public void OnError() {
                Log.i(TAG, "Error occurred while trying to stop sharing.");

                if (view != null)
                    view.showStopSharingError();
            }
        });
    }

    @Override
    public void shareTrackingUrl(ActionManager actionManager) {
        if (actionManager.getHyperTrackAction() == null)
            return;

        String shareMessage = actionManager.getHyperTrackAction().getShareMessage();
        if (shareMessage == null) {
            if (view != null)
                view.showShareTrackingUrlError();
            return;
        }

        if (view != null)
            view.showShareTrackingUrlSuccess(actionManager.getHyperTrackAction().getShareMessage());
    }

    @Override
    public void openNavigationForExpectedPlace(ActionManager actionManager) {
        Place place = actionManager.getPlace();
        if (place == null || place.getLocation() == null) {
            if (view != null)
                view.showOpenNavigationError();
            return;
        }

        double latitude = place.getLocation().getLatitude();
        double longitude = place.getLocation().getLongitude();

        if (latitude == 0.0 || longitude == 0.0) {
            if (view != null)
                view.showOpenNavigationError();
            return;
        }

        if (view != null)
            view.showOpenNavigationSuccess(latitude, longitude);
    }

    @Override
    public void trackActionsOnMap(final String lookupID, final List<String> actionIDs,
                                  final ActionManager actionManager) {
        if (!TextUtils.isEmpty(lookupID)) {
            HyperTrack.trackActionByLookupId(lookupID, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    List<Action> actions = (List<Action>) response.getResponseObject();
                    if (actions != null && !actions.isEmpty()) {
                        Action action = actions.get(0);
                        Place expectedPlace = action.getExpectedPlace();
                        actionManager.setPlace(expectedPlace);

                        if (view != null)
                            view.showTrackActionsOnMapSuccess(actions);

                    } else {
                        if (view != null)
                            view.showTrackActionsOnMapError(new ErrorResponse());
                    }
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    if (view != null)
                        view.showTrackActionsOnMapError(errorResponse);
                }
            });

        } else {
            // Check if a valid ActionID list is available
            if (actionIDs != null && !actionIDs.isEmpty()) {
                HyperTrack.trackAction(actionIDs, new HyperTrackCallback() {
                    @Override
                    public void onSuccess(@NonNull SuccessResponse response) {
                        if (response.getResponseObject() != null) {
                            if (view != null)
                                view.showTrackActionsOnMapSuccess((List<Action>) response.getResponseObject());

                        } else {
                            if (view != null)
                                view.showTrackActionsOnMapError(new ErrorResponse());
                        }
                    }

                    @Override
                    public void onError(@NonNull ErrorResponse errorResponse) {
                        if (view != null)
                            view.showTrackActionsOnMapError(new ErrorResponse());
                    }
                });

            } else {
                if (view != null)
                    view.showTrackActionsOnMapError(new ErrorResponse());
            }
        }
    }
}
