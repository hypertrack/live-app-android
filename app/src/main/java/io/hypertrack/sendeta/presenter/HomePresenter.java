
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
import android.support.annotation.NonNull;
import android.util.Log;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
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

        if ((expectedPlace == null)) {
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
    public void stopSharing(final ActionManager actionManager) {
        actionManager.completeAction(new ActionManagerCallback() {
            @Override
            public void OnSuccess() {
                HyperTrack.clearServiceNotificationParams();
                Log.i(TAG, "Stopped sharing live location successfully.");
                if (view != null) {
                    view.showStopSharingSuccess();
                }
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
        if (!HTTextUtils.isEmpty(lookupID)) {
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
