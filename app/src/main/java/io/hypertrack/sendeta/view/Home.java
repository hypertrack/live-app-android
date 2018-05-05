
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

package io.hypertrack.sendeta.view;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackUtils;
import com.hypertrack.lib.MapFragmentCallback;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.internal.consumer.view.PlaceSelector.PlaceSelector;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.HyperTrackError;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.tracking.BottomCardItemView;
import com.hypertrack.lib.tracking.CTAButton;
import com.hypertrack.lib.tracking.MapProvider.HyperTrackMapFragment;
import com.hypertrack.lib.tracking.MapProvider.MapFragmentView;
import com.hypertrack.lib.tracking.UseCase.LocationSharing.LocationSharingView;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.callback.ActionManagerListener;
import io.hypertrack.sendeta.presenter.HomePresenter;
import io.hypertrack.sendeta.presenter.IHomePresenter;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.util.AnimationUtils;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.PermissionUtils;

public class Home extends AppCompatActivity implements HomeView, CTAButton.OnClickListener {

    private static final String TAG = Home.class.getSimpleName();
    private Place expectedPlace;

    HyperTrackMapFragment mHyperTrackMapFragment;
    private HomeMapAdapter mMapAdapter;
    private GoogleMap mMap;

    private String collectionId = null;

    private ProgressDialog mProgressDialog;

    private IHomePresenter<HomeView> presenter = new HomePresenter();

    boolean isRestoreLocationSharing = false, isHandleTrackingUrlDeeplink = false;

    private boolean isPlaceUpdating, isCreateAction, isUpdateExpectedPlace, isClose;

    BottomCardItemView stopLocationSharingButton;
    BottomCardItemView updateExpectedPlaceButton;

    List<BottomCardItemView> bottomCardItemViews;
    PlaceSelector mPlaceSelector;
    StartView mStartView;
    LocationSharingView mLocationSharingView;

    private Boolean isExpectedPlaceAdded = null;

    private ActionManagerListener actionCompletedListener = new ActionManagerListener() {
        @Override
        public void OnCallback() {
            // Initiate Stop Sharing on UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HyperLog.i(TAG, "Inside runOnUIThread: ");
                    presenter.stopSharing(true);
                }
            });
        }
    };

    public MapFragmentCallback callback = new MapFragmentCallback() {

        @Override
        public void onMapReadyCallback(Context context, GoogleMap map) {
            mMap = map;
            if (!isRestoreLocationSharing && checkForLocationSettings())
                mMap.setMyLocationEnabled(true);
            super.onMapReadyCallback(context, map);
        }

        @Override
        public void onExpectedPlaceSelected(Place expectedPlace) {
            // Check if destination place was selected
            if (expectedPlace != null) {
                onSelectPlace(expectedPlace);
            }
        }

        @Override
        public void onActionRefreshed(List<String> refreshedActionIds,
                                      final List<Action> refreshedActions) {
            ActionManager actionManager = ActionManager.getSharedManager(Home.this);

            /*
              Refresh tracking view when you are tracking some other user and he has added the
              expected place.
             */
            if (actionManager.getTrackingAction() != null && !isPlaceUpdating &&
                    (isExpectedPlaceAdded == null || !isExpectedPlaceAdded)) {
                int index = refreshedActionIds.indexOf(actionManager.getTrackingAction().getId());
                if (index >= 0) {
                    isExpectedPlaceAdded = refreshedActions.get(index).getExpectedPlace() != null;
                    if (isExpectedPlaceAdded && presenter != null) {
                        presenter.refreshView(refreshedActions, true);
                    }
                }
            }

            if (actionManager.getHyperTrackAction() != null) {
                //Get the index of active action from refreshed action Ids
                int index = refreshedActionIds.indexOf(actionManager.getHyperTrackActionId());

                if (index >= 0) {
                    //Get refreshed action Data
                    Action action = refreshedActions.get(index);

                    //Update action data to Shared Preference
                    actionManager.setHyperTrackAction(action);

                    if (refreshedActionIds.size() > 1) {
                        actionManager.setTrackingAction(refreshedActions.get(Math.abs(index - 1)));
                    }
                }
            }

            /*
              Hide Loading View and refresh tracking view after few seconds of updating expected
              place because updating expected place of actions and tracking those action
              is completely independent.
             */
            if (isPlaceUpdating) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (presenter != null) {
                            presenter.refreshView(refreshedActions, false);
                        }
                        hideLoading();
                    }
                }, 1500);
                isPlaceUpdating = false;
            }
        }

        @Override
        public void onActionStatusChanged(List<String> changedStatusActionIds, List<Action> changedStatusActions) {
            presenter.refreshView(changedStatusActions, true);
        }

        @Override
        public void onHeaderActionButtonClicked(String metaData) {
            super.onHeaderActionButtonClicked(metaData);
            presenter.getShareMessage();
        }

        @Override
        public boolean onBackButtonPressed() {
            if (mHyperTrackMapFragment == null)
                return false;

            if (mHyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.PLACE_SELECTOR) {
                setLocationSharingViewUseCase();
                return true;
            }

            return false;
        }

        @Override
        public void onBottomBaseViewCreated(@MapFragmentView.Type int useCaseType) {
            switch (useCaseType) {
                case MapFragmentView.Type.LIVE_LOCATION_SHARING:
                    updateLiveLocationSharingView();
                    if (checkForLocationSettings() && mMap != null)
                        mMap.setMyLocationEnabled(false);
                    break;
                case MapFragmentView.Type.PLACE_SELECTOR:
                    mHyperTrackMapFragment.hideResetBoundButton();
                    mPlaceSelector.setEditTextHint("Enter your destination");
                    break;
                case MapFragmentView.Type.CUSTOM:
                    setTopButtonToCreateAction();
                    if (checkForLocationSettings()) {

                        HyperTrack.getCurrentLocation(new HyperTrackCallback() {
                            @Override
                            public void onSuccess(@NonNull SuccessResponse response) {
                                if (mMap != null)
                                    mMap.setMyLocationEnabled(true);
                                Location location = (Location) response.getResponseObject();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),16f));
                            }

                            @Override
                            public void onError(@NonNull ErrorResponse errorResponse) {

                            }
                        });
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home);

        //Initialize Map Fragment added in Activity Layout to getMapAsync
        mHyperTrackMapFragment = (HyperTrackMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.htMapfragment);

        mMapAdapter = new HomeMapAdapter(this);
        mHyperTrackMapFragment.setMapAdapter(mMapAdapter);
        mHyperTrackMapFragment.setMapCallback(callback);

        // Initialize UI Views
        initializeUIViews();

        // Check & Prompt User if Internet is Not Connected
        if (!HyperTrackUtils.isInternetConnected(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
        }

        // Attach View Presenter to View
        presenter.attachView(this);

        presenter.setActionManager(ActionManager.getSharedManager(this));

        //Check if user click on someone else tracking link
        isHandleTrackingUrlDeeplink = handleDeepLinkTrackingUrl();

        // Check if location is already being shared
        isRestoreLocationSharing = presenter.restoreLocationSharing();

        /*
         If user clicked on deep-link url or then set tracking view as LiveLocationSharing View
         */
        if (isHandleTrackingUrlDeeplink || isRestoreLocationSharing) {
            expectedPlace = ActionManager.getSharedManager(this).getPlace();

            setLocationSharingViewUseCase();

            showLoading(getString(R.string.fetching_details_msg));
            // Call trackActionsOnMap method
            presenter.trackActionsOnMap(collectionId, true);
        } else {
            setStartViewUseCase();
        }
    }

    private void initializeUIViews() {

        /*
        Create bottom items to show updating expected place button
         */
        updateExpectedPlaceButton = new BottomCardItemView(this);
        updateExpectedPlaceButton.setDescription("Share meeting location");
        updateExpectedPlaceButton.setDescriptionTextColor(R.color.info_box_destination);
        updateExpectedPlaceButton.setActionButtonIcon(R.drawable.ic_chevron_right);
        updateExpectedPlaceButton.showOnlyActionButtonIcon();
        updateExpectedPlaceButton.setVisibility(View.GONE);
        updateExpectedPlaceButton.setActionButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlaceSelectorViewUseCase();
                mLocationSharingView = null;
            }
        });

        /*
         Create bottom items to show stop location sharing button
         */
        stopLocationSharingButton = new BottomCardItemView(this);
        stopLocationSharingButton.setDescription("SHARING YOUR LIVE LOCATION");
        stopLocationSharingButton.setActionButtonText("STOP");
        stopLocationSharingButton.setVisibility(View.GONE);
        stopLocationSharingButton.setActionButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.stopSharing(false);
            }
        });

        bottomCardItemViews = new ArrayList<>();
        bottomCardItemViews.add(updateExpectedPlaceButton);
        bottomCardItemViews.add(stopLocationSharingButton);
    }

    private void initializeProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionManager actionManager = ActionManager.getSharedManager(this);
        Action action = actionManager.getHyperTrackAction();

        if (action != null
                && mHyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.LIVE_LOCATION_SHARING) {

            mLocationSharingView.addBottomViewItems(bottomCardItemViews);
            actionManager.setActionComletedListener(actionCompletedListener);
            collectionId = action.getCollectionId();
            presenter.trackActionsOnMap(collectionId, false);
        }
    }

    public void setTopButtonToCreateAction() {
        if (mHyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.CUSTOM) {
            mStartView.setCTAButtonClickListener(this);
            isCreateAction = true;
            collectionId = null;
            ActionManager.getSharedManager(this).deleteTrackingAction();
        } else if (mHyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.LIVE_LOCATION_SHARING) {
            mLocationSharingView.setCTAButtonTitle(getString(R.string.share_your_location));
            mLocationSharingView.setCTAButtonClickListener(this);
            mLocationSharingView.showCTAButton();
            isCreateAction = true;
        }
    }

    public void setTopButtonToUpdateExpectedPlace() {
        if (mHyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.LIVE_LOCATION_SHARING) {
            mLocationSharingView.setCTAButtonTitle(getString(R.string.share_eta));
            mLocationSharingView.setCTAButtonClickListener(this);
            mLocationSharingView.showCTAButton();
            isCreateAction = false;
            isUpdateExpectedPlace = true;
        }
    }

    private void shareLiveLocation() {
        //Check if Location Permission has been granted & Location has been enabled
        if (HyperTrack.checkLocationPermission(this) && HyperTrack.checkLocationServices(this)) {
            //Check if user has already shared his tracking link
            Action action = ActionManager.getSharedManager(this).getHyperTrackAction();
            if (action == null || action.hasFinished()) {
                createAction();
            } else if (!action.hasFinished()) {
                presenter.restoreLocationSharing();
            }
        } else {
            checkForLocationSettings();
        }
    }

    /**
     * Method to be called when user selects an expected place to be used for
     * sharing his live location via the tracking url.
     *
     * @param place Expected place for the user
     */
    private void onSelectPlace(final Place place) {
        if (place == null || place.getLocation() == null || this.isFinishing()) {
            return;
        }
        presenter.updateExpectedPlace(place);
        ActionManager.getSharedManager(this).setPlace(expectedPlace);
        setLocationSharingViewUseCase();
    }

    private void createAction() {
        User user = SharedPreferenceManager.getHyperTrackLiveUser(this);
        presenter.shareLiveLocation(user);
    }

    /**
     * Method to update State Variables & UI to reflect Task Ended
     */
    private void stopSharingLiveLocation() {
        isRestoreLocationSharing = false;
        collectionId = null;
        HyperTrack.removeActions(null);
        setStartViewUseCase();
        mLocationSharingView = null;
        ActionManager.getSharedManager(Home.this).clearState();
        expectedPlace = null;
        isRestoreLocationSharing = false;
        isHandleTrackingUrlDeeplink = false;
        isUpdateExpectedPlace = false;
        isExpectedPlaceAdded = null;
        isClose = false;
    }

    private boolean checkForLocationSettings() {
        // Check If LOCATION Permission is available & then if Location is enabled
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestLocationServices(this);
            return false;
        }

        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
            return false;
        }

        return true;
    }

    /**
     * Method to handle Tracking url deeplinks to enable live location sharing amongst friends
     */
    private boolean handleDeepLinkTrackingUrl() {
        Intent intent = getIntent();

        if (intent != null && intent.getBooleanExtra(Track.KEY_TRACK_DEEPLINK, false)) {
            // Get required parameters for tracking Actions on map
            collectionId = intent.getStringExtra(Track.KEY_COLLECTION_ID);
            if (HTTextUtils.isEmpty(collectionId)) {
                HyperLog.e(TAG, "handleDeepLinkTrackingUrl: CollectionId is empty");
                Toast.makeText(this, "Tracking Url is corrupt", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void showLoading() {
        if (mProgressDialog == null)
            initializeProgressDialog();

        if (mProgressDialog != null)
            mProgressDialog.cancel();

        if (mProgressDialog != null)
            mProgressDialog.show();
    }

    @Override
    public void showLoading(String message) {
        if (mProgressDialog == null)
            initializeProgressDialog();

        mProgressDialog.setMessage(message);
        showLoading();
    }

    @Override
    public void hideLoading() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    @Override
    public void showTrackActionsOnMapError(ErrorResponse errorResponse) {
        Toast.makeText(this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActionRefreshed() {
        expectedPlace = ActionManager.getSharedManager(this).getPlace();
        isRestoreLocationSharing = !HTTextUtils.isEmpty(ActionManager.getSharedManager(this).getHyperTrackActionId());
        updateLiveLocationSharingView();
    }

    @Override
    public void showShareCard(String shareMessage) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivityForResult(Intent.createChooser(sharingIntent, "Share your tracking URL"),
                Constants.SHARE_REQUEST_CODE);
    }

    @Override
    public void showUpdatePlaceLoading() {
        isPlaceUpdating = true;
    }

    @Override
    public void updateExpectedPlaceFailure(String errorMessage) {
        HyperLog.e(TAG, "updateExpectedPlaceFailure: " + errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        updateLiveLocationSharingView();
    }

    @Override
    public void showShareLiveLocationSuccess(Action action) {
        isRestoreLocationSharing = true;
        stopLocationSharingButton.setDescription("SHARING YOUR LIVE LOCATION");
        stopLocationSharingButton.setActionButtonText("STOP");
        stopLocationSharingButton.hideProgressLoading();
        setLocationSharingViewUseCase();
        presenter.trackActionsOnMap(action.getCollectionId(), false);
        presenter.getShareMessage();
    }

    @Override
    public void showShareLiveLocationError(ErrorResponse errorResponse) {
        switch (errorResponse.getErrorCode()) {
            case HyperTrackError.Code.PUBLISHABLE_KEY_NOT_CONFIGURED:
            case HyperTrackError.Code.SDK_NOT_INITIALIZED:
            case HyperTrackError.Code.USER_ID_NOT_CONFIGURED:
            case HyperTrackError.Code.PLAY_SERVICES_UNAVAILABLE:
            case HyperTrackError.Code.PERMISSIONS_NOT_REQUESTED:
            case HyperTrackError.Code.LOCATION_SETTINGS_DISABLED:
            case HyperTrackError.Code.LOCATION_SETTINGS_LOW_ACCURACY:
            case HyperTrackError.Code.NETWORK_CONNECTIVITY_ERROR:
            case HyperTrackError.Code.LOCATION_SETTINGS_CHANGE_UNAVAILABLE:
                Toast.makeText(this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                return;
            default:
                Toast.makeText(this, ErrorMessages.SHARE_LIVE_LOCATION_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showStopSharingError() {
        Toast.makeText(this, getString(R.string.stop_sharing_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showStopSharingSuccess() {
        stopLocationSharingButton.showProgressLoading("STOPPING LOCATION SHARING");
    }

    @Override
    public void onTitleButtonClick() {
        if (isClose) {
            stopSharingLiveLocation();
        } else if (isCreateAction) {
            shareLiveLocation();
        } else if (isUpdateExpectedPlace) {
            setPlaceSelectorViewUseCase();
        }
    }

    @Override
    public void onLeftButtonClick() {

    }

    @Override
    public void onRightButtonClick() {

    }

    private void setLocationSharingViewUseCase() {
        if (mLocationSharingView == null) {
            mLocationSharingView = (LocationSharingView) mHyperTrackMapFragment.
                    setUseCaseType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
        } else {
            mHyperTrackMapFragment.setUseCase(mLocationSharingView);
        }
    }

    private void setPlaceSelectorViewUseCase() {
        if (mPlaceSelector == null) {
            mHyperTrackMapFragment.hideResetBoundButton();
            mPlaceSelector =
                    (PlaceSelector) mHyperTrackMapFragment.setUseCaseType(
                            MapFragmentView.Type.PLACE_SELECTOR);
        } else {
            mHyperTrackMapFragment.setUseCase(mPlaceSelector);
        }
    }

    private void setStartViewUseCase() {
        if (mStartView == null) {
            mStartView = StartView.newInstance();
            mHyperTrackMapFragment.setUseCase(mStartView);
        } else {
            mHyperTrackMapFragment.setUseCase(mStartView);
        }
    }

    private void updateLiveLocationSharingView() {
        if (mLocationSharingView == null) {
            mLocationSharingView = (LocationSharingView) mHyperTrackMapFragment.
                    setUseCaseType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
            return;
        }

        if (!mLocationSharingView.isViewCreated()) {
            return;
        }

        ActionManager actionManager = ActionManager.getSharedManager(this);
        Action action = actionManager.getHyperTrackAction();
        Action trackingAction = actionManager.getTrackingAction();
        boolean isMultipleAction = trackingAction != null && action != null;
        /*
        If location is sharing from this device and expected place is not set then hide updateExpectedPlace
        bottom button and show stoplocationsharing bottom.

        And if this device is tracking some other user then setup top button click listener to share
         this device location and if expected place is not set then show updateExpectedPlace
        bottom button and hide stoplocationsharing bottom.
         */

        if (isRestoreLocationSharing) {
            if (action.hasFinished()) {
                AnimationUtils.collapse(stopLocationSharingButton);
                updateExpectedPlaceButton.setVisibility(View.GONE);
                isClose = true;
                mLocationSharingView.setCTAButtonTitle("CLOSE");
                mLocationSharingView.showCTAButton();
                mLocationSharingView.setCTAButtonClickListener(this);
                return;
            }
            if (expectedPlace != null) {
                mLocationSharingView.hideCTAButton();
            } else {
                setTopButtonToUpdateExpectedPlace();
            }
            stopLocationSharingButton.setVisibility(View.VISIBLE);
            updateExpectedPlaceButton.setVisibility(View.GONE);
        } else {
            if (!isMultipleAction && trackingAction != null && trackingAction.hasFinished()) {
                stopLocationSharingButton.setVisibility(View.GONE);
                updateExpectedPlaceButton.setVisibility(View.GONE);
                isClose = true;
                mLocationSharingView.setCTAButtonTitle("CLOSE");
                mLocationSharingView.showCTAButton();
                mLocationSharingView.setCTAButtonClickListener(this);
                return;
            }
            if (expectedPlace != null) {
                updateExpectedPlaceButton.setVisibility(View.GONE);
            } else {
                updateExpectedPlaceButton.setVisibility(View.VISIBLE);
            }
            stopLocationSharingButton.setVisibility(View.GONE);
            setTopButtonToCreateAction();
        }

        mLocationSharingView.addBottomViewItems(bottomCardItemViews);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkForLocationSettings();

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionUtils.showPermissionDeclineDialog(this, Manifest.permission.ACCESS_FINE_LOCATION,
                        getString(R.string.location_permission_never_allow));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                checkForLocationSettings();
            } else {
                // Handle Location services request denied error
                Snackbar.make(findViewById(R.id.parent_layout), R.string.location_services_snackbar_msg,
                        Snackbar.LENGTH_INDEFINITE).setAction("Enable Location", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkForLocationSettings();
                    }
                }).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        HyperTrack.removeActions(null);

        ActionManager actionManager = ActionManager.getSharedManager(this);
        //If tracking action has completed and summary view is visible then on back press clear the view
        // so that user can share new tracking url without reopening the app.
        if (actionManager.getHyperTrackAction() != null &&
                actionManager.getHyperTrackAction().hasFinished()) {
            // Reset uniqueId variable
            stopSharingLiveLocation();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        presenter.clearTrackingAction();
        // Detach View from Presenter
        presenter.detachView();
        super.onDestroy();
    }

}