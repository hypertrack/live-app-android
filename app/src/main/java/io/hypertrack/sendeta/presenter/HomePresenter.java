
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.presenter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.internal.consumer.utils.ActionUtils;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;

import java.util.List;
import java.util.UUID;

import io.hypertrack.sendeta.callback.ActionManagerCallback;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
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
    public void shareLiveLocation(final ActionManager actionManager, String collectionId, final String lookupID, final Place expectedPlace) {
        User user = OnboardingManager.sharedManager().getUser();
        if (user == null) {
            if (view != null)
                view.showShareLiveLocationError(new ErrorResponse());
            return;
        }

        ActionParamsBuilder builder = new ActionParamsBuilder();

        if (!HTTextUtils.isEmpty(collectionId) || HTTextUtils.isEmpty(lookupID)) {
            builder.setCollectionId(collectionId != null ? collectionId : UUID.randomUUID().toString());
        } else {
            builder.setLookupId(lookupID);
        }
        builder.setType(Action.ACTION_TYPE_VISIT);

        if (expectedPlace == null) {
        } else if (!HTTextUtils.isEmpty(expectedPlace.getId())) {
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
                    actionManager.setHyperTrackAction(action);
                    actionManager.onActionStart();

                    if (action.getUser().getPendingActions().size() > 1) {
                        String previousActionID = action.getUser().getPendingActions().get(0);
                        HyperTrack.completeAction(previousActionID);
                    }

                    HyperTrack.clearServiceNotificationParams();
                    HTLog.i(TAG, "Share Live Location successful for userID: " + HyperTrack.getUserId());

                    if (view != null)
                        view.showShareLiveLocationSuccess(action);

                } else {
                    if (view != null)
                        view.showShareLiveLocationError(new ErrorResponse());
                    Log.e(TAG, "onSuccess: Response Object is null", null);
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
    public void stopSharing(final ActionManager actionManager, final boolean fromGeofence) {

        actionManager.completeAction(new ActionManagerCallback() {
            @Override
            public void OnSuccess() {
                HyperTrack.clearServiceNotificationParams();
                HTLog.i(TAG, "Stopped sharing live location successfully" + (fromGeofence ? " by geofence." : "."));
                if (view != null) {
                    if (!fromGeofence)
                        view.showStopSharingSuccess();
                    else {
                        view.hideBottomCard();
                    }
                }
            }

            @Override
            public void OnError() {
                HTLog.i(TAG, "Error occurred while trying to stop sharing.");
                if (view != null)
                    view.showStopSharingError();
            }

        });
    }

    @Override
    public void openCustomShareCard(Context context, ActionManager actionManager) {
        if (actionManager.getHyperTrackAction() == null)
            return;

        Action action = actionManager.getHyperTrackAction();
        if (action.getActionDisplay() != null &&
                !HTTextUtils.isEmpty(action.getActionDisplay().getDurationRemaining())) {
            Integer eta = Integer.parseInt(action.getActionDisplay().getDurationRemaining());
            String remainingTime = ActionUtils.getFormattedTimeString(context, Double.valueOf(eta));
            if (HTTextUtils.isEmpty(remainingTime)) {
                if (view != null)
                    view.showCustomShareCardError(action.getTrackingURL());
            } else {
                if (view != null)
                    view.showCustomShareCardSuccess(remainingTime,
                            action.getTrackingURL());
            }

        } else if (view != null)
            view.showCustomShareCardError(action.getTrackingURL());
    }

    @Override
    public void shareTrackingURL(ActionManager actionManager) {
        if (actionManager.getHyperTrackAction() == null)
            return;

        String shareMessage = actionManager.getHyperTrackAction().getShareMessage();
        if (view != null) {
            if (!HTTextUtils.isEmpty(shareMessage))
                view.showShareTrackingURLSuccess(shareMessage);
            else
                view.showShareTrackingURLError();
        }

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
    public void trackActionsOnMap(String collectionId, final String lookupID, final List<String> actionIDs,
                                  final ActionManager actionManager, final Context context) {
        if (!HTTextUtils.isEmpty(collectionId)) {
            HyperTrack.trackActionByCollectionId(collectionId, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    onTrackActionSuccess(context, actionManager, actionIDs, response);
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    onTrackActionError(errorResponse);
                }
            });
        } else if (!HTTextUtils.isEmpty(lookupID)) {
            HyperTrack.trackActionByLookupId(lookupID, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    onTrackActionSuccess(context, actionManager, actionIDs, response);
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    onTrackActionError(errorResponse);
                }
            });

        } else {
            // Check if a valid ActionID list is available
            if (actionIDs != null && !actionIDs.isEmpty()) {
                HyperTrack.trackAction(actionIDs, new HyperTrackCallback() {
                    @Override
                    public void onSuccess(@NonNull SuccessResponse response) {

                        List<Action> actions = (List<Action>) response.getResponseObject();
                        if (actions != null && !actions.isEmpty()) {
                            Action action = actions.get(0);
                            Place expectedPlace = action.getExpectedPlace();
                            actionManager.setPlace(expectedPlace);
                            String remainingTime = null;
                            if (action.getActionDisplay() != null &&
                                    !HTTextUtils.isEmpty(action.getActionDisplay().getDurationRemaining())) {
                                Integer eta = Integer.parseInt(action.getActionDisplay().getDurationRemaining());
                                remainingTime = ActionUtils.getFormattedTimeString(context, Double.valueOf(eta));
                            }
                            if (view != null) {
                                if (actions.size() == 1 && !actions.contains(actionManager.getHyperTrackActionId())) {
                                    SharedPreferenceManager.setTrackingAction(action);
                                    view.showShareBackCard(remainingTime);
                                    return;
                                } else if (actions.size() > 1 && actions.contains(actionManager.getHyperTrackActionId())) {
                                    if (actions.get(0).getId().equalsIgnoreCase(actionIDs.get(0)))
                                        SharedPreferenceManager.setTrackingAction(action);
                                    else {
                                        SharedPreferenceManager.setTrackingAction(actions.get(1));
                                    }
                                }
                                view.showTrackActionsOnMapSuccess(actions);
                            }

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

    private void onTrackActionSuccess(Context context, ActionManager actionManager, List<String> actionIDs, SuccessResponse response) {
        List<Action> actions = (List<Action>) response.getResponseObject();
        if (actions != null && !actions.isEmpty()) {
            Action action = actions.get(0);
            Place expectedPlace = action.getExpectedPlace();
            actionManager.setPlace(expectedPlace);
            String remainingTime = null;
            if (action.getActionDisplay() != null &&
                    !HTTextUtils.isEmpty(action.getActionDisplay().getDurationRemaining())) {
                Integer eta = Integer.parseInt(action.getActionDisplay().getDurationRemaining());
                remainingTime = ActionUtils.getFormattedTimeString(context, Double.valueOf(eta));
            }
            if (view != null) {
                if (actions.size() == 1 && !actions.contains(actionManager.getHyperTrackActionId())) {
                    if (action.hasActionFinished()) {
                        view.hideBottomCard();
                        return;
                    }
                    view.showShareBackCard(remainingTime);
                    SharedPreferenceManager.setTrackingAction(action);
                    return;
                } else if (actions.size() > 1 && actions.contains(actionManager.getHyperTrackActionId())) {
                    if (actions.get(0).getId().equalsIgnoreCase(actionIDs.get(0)))
                        SharedPreferenceManager.setTrackingAction(action);
                    else {
                        SharedPreferenceManager.setTrackingAction(actions.get(1));
                    }
                } else if (actions.size() > 1) {
                    view.hideBottomCard();
                }
                view.showTrackActionsOnMapSuccess(actions);
            }
        } else {
            if (view != null)
                view.showTrackActionsOnMapError(new ErrorResponse());
        }
    }

    private void onTrackActionError(ErrorResponse errorResponse) {
        if (view != null)
            view.showTrackActionsOnMapError(errorResponse);
    }
}
