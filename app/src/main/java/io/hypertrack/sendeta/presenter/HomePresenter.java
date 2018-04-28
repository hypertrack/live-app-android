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

import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
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
import io.hypertrack.sendeta.view.HomeView;

/**
 * Created by piyush on 07/05/17.
 */

public class HomePresenter implements IHomePresenter<HomeView> {

    private static final String TAG = HomePresenter.class.getSimpleName();
    private HomeView mView;
    private ActionManager actionManager;
    String userId = "";

    public HomePresenter() {
        userId = HyperTrack.getUserId();
        if (HTTextUtils.isEmpty(userId))
            userId = "";
    }

    @Override
    public void attachView(HomeView view) {
        this.mView = view;
    }

    @Override
    public void detachView() {
        this.mView = null;
    }

    @Override
    public boolean isViewAttached() {
        return mView != null;
    }

    @Override
    public void shareLiveLocation(final User user) {

        if (!isViewAttached())
            return;

        if (user == null) {
            mView.showShareLiveLocationError(new ErrorResponse("User is not configured"));
            return;
        }

        String collectionId = null;
        Place expectedPlace = null;
        Action trackingAction = actionManager.getTrackingAction();
        if (trackingAction != null) {
            collectionId = trackingAction.getCollectionId();
            expectedPlace = trackingAction.getExpectedPlace();
        }

        ActionParamsBuilder actionParamsBuilder = new ActionParamsBuilder();
        actionParamsBuilder.setCollectionId(collectionId != null ?
                collectionId : UUID.randomUUID().toString());

        actionParamsBuilder.setType(Action.TYPE_MEETUP);

        if (expectedPlace != null) {
            if (!HTTextUtils.isEmpty(expectedPlace.getId())) {
                actionParamsBuilder.setExpectedPlaceId(expectedPlace.getId());
            } else {
                actionParamsBuilder.setExpectedPlace(expectedPlace);
            }
        }

        mView.showLoading("Sharing your location...");

        // Call assignAction to start the tracking action
        HyperTrack.createAction(actionParamsBuilder.build(), new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                if (response.getResponseObject() != null) {
                    Action action = (Action) response.getResponseObject();
                    actionManager.setHyperTrackAction(action);
                    actionManager.onActionStart();

                    HyperTrack.clearServiceNotificationParams();
                    HyperLog.i(TAG, "Share Live Location successful for userID: " +
                            HyperTrack.getUserId());

                    if (!isViewAttached())
                        return;

                    mView.showShareLiveLocationSuccess(action);

                } else {
                    Log.e(TAG, "onSuccess: Response Object is null", null);
                    if (mView != null)
                        mView.showShareLiveLocationError(new ErrorResponse());
                }
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                HyperLog.e(TAG, "Share Live Location failed with error: " +
                        errorResponse.getErrorMessage());
                if (!isViewAttached())
                    return;

                mView.hideLoading();
                mView.showShareLiveLocationError(errorResponse);
            }
        });
    }

    @Override
    public void stopSharing(final boolean fromGeofence) {
        actionManager.completeAction(new ActionManagerCallback() {
            @Override
            public void OnSuccess() {
                HyperTrack.clearServiceNotificationParams();
                HyperLog.i(TAG, "Stopped sharing live location successfully"
                        + (fromGeofence ? " by geofence." : "."));
                if (!isViewAttached())
                    return;

                if (!fromGeofence)
                    mView.showStopSharingSuccess();
            }

            @Override
            public void OnError() {
                HyperLog.e(TAG, "Error occurred while trying to stop sharing.");
                if (isViewAttached())
                    mView.showStopSharingError();
            }

        });
    }

    @Override
    public void getShareMessage() {
        if (actionManager == null || !isViewAttached())
            return;

        Action action;


        action = actionManager.getHyperTrackAction();

        if (action == null)
            action = actionManager.getTrackingAction();

        if (action == null)
            return;

        String shareMessage = action.getShareMessage();
        mView.showShareCard(shareMessage);
    }

    @Override
    public void trackActionsOnMap(String collectionId, final boolean isDeepLinkTrackingAction) {
        if (!HTTextUtils.isEmpty(collectionId)) {
            HyperTrack.trackActionByCollectionId(collectionId, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    List<Action> actions = (List<Action>) response.getResponseObject();
                    if (actions == null || actions.isEmpty()) {
                        mView.showTrackActionsOnMapError(new ErrorResponse("Error Occurred"));
                        mView.hideLoading();
                        return;
                    }
                    refreshView(actions, isDeepLinkTrackingAction);

                    mView.hideLoading();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    if (!isViewAttached())
                        return;
                    mView.showTrackActionsOnMapError(errorResponse);
                    mView.hideLoading();
                }
            });
        }
    }

    @Override
    public void refreshView(List<Action> actions, boolean isDeepLinkTrackingAction) {
        Action action = actions.get(0);
        Place expectedPlace = action.getExpectedPlace();
        actionManager.setPlace(expectedPlace);

        if (!isViewAttached())
            return;

        for (Action tempAction : actions) {
            if ((tempAction.getUser().getId().equalsIgnoreCase(userId) && !tempAction.hasFinished())
                    || (actionManager.getHyperTrackAction() != null &&
                    actionManager.getHyperTrackAction().getId().equalsIgnoreCase(tempAction.getId()))) {
                actionManager.setHyperTrackAction(tempAction);
            } else if (isDeepLinkTrackingAction) {
                actionManager.setTrackingAction(tempAction);
            }
        }
        mView.onActionRefreshed();
    }

    @Override
    public boolean restoreLocationSharing() {
        return actionManager != null && isViewAttached() && actionManager.shouldRestoreState();

    }

    @Override
    public void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    @Override
    public void updateExpectedPlace(Place place) {
        if (actionManager != null) {
            actionManager.setPlace(place);
        }

        if (!isViewAttached())
            return;

        String collectionId = actionManager.getHyperTrackActionCollectionId();
        if (HTTextUtils.isEmpty(collectionId) && actionManager.getTrackingAction() != null) {
            collectionId = actionManager.getTrackingAction().getCollectionId();
        }

        if (HTTextUtils.isEmpty(collectionId)) {
            mView.updateExpectedPlaceFailure("Collection Id is empty");
            return;
        }

        mView.showLoading("Sharing ETA...");

        HyperTrack.updateActionPlaceByCollectionId(collectionId, place, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                if (!isViewAttached())
                    return;

                mView.onActionRefreshed();
                mView.showUpdatePlaceLoading();
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                if (!isViewAttached())
                    return;

                mView.updateExpectedPlaceFailure(errorResponse.getErrorMessage());
                mView.hideLoading();
                mView.showUpdatePlaceLoading();
            }
        });
    }

    @Override
    public void clearTrackingAction() {
        if (actionManager == null)
            return;

        actionManager.deleteTrackingAction();
    }
}
