
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.PermissionUtils;

public class Home extends AppCompatActivity implements HomeView, CTAButton.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = Home.class.getSimpleName();
    private Place expectedPlace;

    HyperTrackMapFragment mHyperTrackMapFragment;
    private HomeMapAdapter mMapAdapter;

    private String collectionId = null;

    private ProgressDialog mProgressDialog;

    private IHomePresenter<HomeView> presenter = new HomePresenter();

    boolean isRestoreLocationSharing = false, isHandleTrackingUrlDeeplink = false;

    private boolean isPlaceUpdating, isCreateAction, isUpdateExpectedPlace;

    BottomCardItemView stopLocationSharingButton;
    BottomCardItemView updateExpectedPlaceButton;

    List<BottomCardItemView> bottomCardItemViews;
    PlaceSelector mSelectExpectedPlaceView;
    StartView mStartView;
    LocationSharingView mLocationSharingView;

    private DrawerLayout drawer;
    NavigationView navigationView;

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

            if (actionManager.getHyperTrackAction() != null) {
                //Get the index of active action from refreshed action Ids
                int index = refreshedActionIds.indexOf(actionManager.getHyperTrackActionId());

                if (index >= 0) {
                    //Get refreshed action Data
                    Action action = refreshedActions.get(index);
                    //Update action data to Shared Preference
                    actionManager.setHyperTrackAction(action);

                    if (action.getExpectedPlace() != null &&
                            action.getExpectedPlace().getLocation() != null &&
                            action.getUser().getId().equalsIgnoreCase(HyperTrack.getUserId())) {
                        actionManager.setShortcutPlace(action.getExpectedPlace());
                    }

                    if (refreshedActionIds.size() > 1) {
                        actionManager.setTrackingAction(refreshedActions.get(Math.abs(index - 1)));
                    }
                }
            }

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
        public void onHeaderActionButtonClicked(String metaData) {
            super.onHeaderActionButtonClicked(metaData);
            presenter.getShareMessage();
        }

        @Override
        public boolean onBackButtonPressed() {
            if (mHyperTrackMapFragment == null)
                return false;

            if (mHyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.PLACE_SELECTOR) {
                setLocationSharingView();
                return true;
            }

            return false;
        }

        @Override
        public void onBottomBaseViewCreated(@MapFragmentView.Type int useCaseType) {
            switch (useCaseType) {
                case MapFragmentView.Type.LIVE_LOCATION_SHARING:
                    updateLiveLocationSharingView();
                    break;
                case MapFragmentView.Type.PLACE_SELECTOR:
                    mHyperTrackMapFragment.hideResetBoundButton();
                    break;
                case MapFragmentView.Type.CUSTOM:
                    setTopButtonToCreateAction();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

        //If user clicked on deep-link url or clicked on shortcut then set tracking view as
        // LiveLocationSharing View
        if (isHandleTrackingUrlDeeplink || isRestoreLocationSharing) {
            expectedPlace = ActionManager.getSharedManager(this).getPlace();

            setLocationSharingView();

            showLoading(getString(R.string.fetching_details_msg));
            // Call trackActionsOnMap method
            presenter.trackActionsOnMap(collectionId, true);
        } else {
            setStartViewUseCase();
        }
    }

    private void initializeUIViews() {

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        if (HyperTrack.isTracking()) {
            navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.stop_tracking);
        }
        navigationView.setNavigationItemSelectedListener(this);

        updateExpectedPlaceButton = new BottomCardItemView(this);
        updateExpectedPlaceButton.setDescription("Share meeting location");
        updateExpectedPlaceButton.setDescriptionTextColor(R.color.info_box_destination);
        updateExpectedPlaceButton.setActionButtonIcon(R.drawable.ic_chevron_right);
        updateExpectedPlaceButton.showOnlyActionButtonIcon();
        updateExpectedPlaceButton.setVisibility(View.GONE);
        updateExpectedPlaceButton.setActionButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectExpectedPlaceView == null) {
                    mSelectExpectedPlaceView =
                            (PlaceSelector) mHyperTrackMapFragment.setUseCaseType(
                                    MapFragmentView.Type.PLACE_SELECTOR);
                } else {
                    mHyperTrackMapFragment.setUseCase(mSelectExpectedPlaceView);
                }
                mLocationSharingView = null;
            }
        });

        stopLocationSharingButton = new BottomCardItemView(this);
        stopLocationSharingButton.setDescription("SHARING YOUR LIVE LOCATION");
        stopLocationSharingButton.setActionButtonText("STOP");
        stopLocationSharingButton.setVisibility(View.GONE);
        stopLocationSharingButton.setActionButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTracking();
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
        //updateCurrentLocationMarker(null);
        presenter.updateExpectedPlace(place);
        ActionManager.getSharedManager(this).setPlace(expectedPlace);
        ActionManager.getSharedManager(this).setShortcutPlace(place);
        setLocationSharingView();
    }

    private void createAction() {
        User user = OnboardingManager.sharedManager(this).getUser();
        presenter.shareLiveLocation(user);
    }

    /**
     * Method to update State Variables & UI to reflect Task Ended
     */
    private void stopSharingLiveLocation() {

        ActionManager.getSharedManager(Home.this).clearState();
        expectedPlace = null;
        isRestoreLocationSharing = false;
        isHandleTrackingUrlDeeplink = false;
        isUpdateExpectedPlace = false;
        setTopButtonToCreateAction();
    }

    private void checkForLocationSettings() {
        // Check If LOCATION Permission is available & then if Location is enabled
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestLocationServices(this);
            return;
        }

        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
        }
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
    public void showPlacePickerButtonAtBottom() {
        updateExpectedPlaceButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void hidePlacePickerButton() {
        if (isUpdateExpectedPlace)
            mLocationSharingView.hideCTAButton();
    }

    @Override
    public void hideBottomPlacePickerButton() {
        updateExpectedPlaceButton.setVisibility(View.GONE);
    }

    @Override
    public void showUpdatePlaceLoading() {
        isPlaceUpdating = true;
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
    public void updateExpectedPlaceFailure(String errorMessage) {
        HyperLog.e(TAG, "updateExpectedPlaceFailure: " + errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        if (isUpdateExpectedPlace)
            setTopButtonToUpdateExpectedPlace();
        else {
            showPlacePickerButtonAtBottom();
        }
    }

    @Override
    public void showShareLiveLocationSuccess(Action action) {
        isRestoreLocationSharing = true;
        setLocationSharingView();
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
        stopSharingLiveLocation();
    }

    @Override
    public void showShareTrackingURLSuccess(String shareMessage) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
        startActivityForResult(Intent.createChooser(sharingIntent, "Share via"),
                Constants.SHARE_REQUEST_CODE);
    }

    @Override
    public void showShareTrackingURLError() {
        Toast.makeText(Home.this, R.string.share_message_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOpenNavigationSuccess(double latitude, double longitude) {
        String navigationString = Double.toString(latitude) + "," + Double.toString(longitude) + "&mode=d";
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + navigationString);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        //Check if map application is installed or not.
        try {
            startActivity(mapIntent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                Toast.makeText(this, "Please install a map application", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void showOpenNavigationError() {
        Toast.makeText(Home.this, R.string.navigate_to_expected_place_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTitleButtonClick() {
        if (isCreateAction) {
            shareLiveLocation();
        } else if (isUpdateExpectedPlace) {
            if (mSelectExpectedPlaceView == null) {
                mHyperTrackMapFragment.hideResetBoundButton();
                mSelectExpectedPlaceView =
                        (PlaceSelector) mHyperTrackMapFragment.setUseCaseType(
                                MapFragmentView.Type.PLACE_SELECTOR);
            } else {
                mHyperTrackMapFragment.setUseCase(mSelectExpectedPlaceView);
            }
        }
    }

    @Override
    public void onLeftButtonClick() {

    }

    @Override
    public void onRightButtonClick() {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawers();
        if (item.getItemId() == R.id.edit_profile)
            startActivity(new Intent(this, Profile.class));

        else if (item.getItemId() == R.id.start_tracking_toggle) {
            if (ActionManager.getSharedManager(this).shouldRestoreState()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Can't do pause tracking.");
                builder.setMessage("Ongoing location sharing trip is active. Stop trip first.");
                builder.setNegativeButton("No", null);
                builder.setPositiveButton("Goto live trip",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setLocationSharingView();
                            }
                        });
                builder.show();
                return true;
            }
            startHyperTrackTracking();
        } else if (item.getItemId() == R.id.push_logs) {
            HyperTrack.pushDeviceLogs();
            return true;
        }
        return true;
    }

    private void startHyperTrackTracking() {
        if (!HyperTrack.isTracking()) {
            HyperTrack.resumeTracking(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.stop_tracking);
                    Toast.makeText(Home.this, "Tracking resumed successfully.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    HyperLog.e(TAG, errorResponse.getErrorMessage());
                    Toast.makeText(Home.this, "Tracking resumed Failed." +
                            errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            HyperTrack.pauseTracking();
            navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.start_tracking);
            Toast.makeText(this, "Tracking paused successfully.", Toast.LENGTH_SHORT).show();
        }

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void stopTracking() {
        if (HyperTrack.checkLocationPermission(Home.this)
                && HyperTrack.checkLocationServices(Home.this)) {
            isRestoreLocationSharing = false;
            presenter.stopSharing(false);
            collectionId = null;
            HyperTrack.removeActions(null);
            setStartViewUseCase();
            setTopButtonToCreateAction();
            mLocationSharingView = null;
        } else {
            if (!HyperTrack.checkLocationServices(Home.this)) {
                HyperTrack.requestLocationServices(Home.this);
            } else {
                HyperTrack.requestPermissions(Home.this);
            }
        }
    }

    private void setLocationSharingView() {
        if (mLocationSharingView == null) {
            mLocationSharingView = (LocationSharingView) mHyperTrackMapFragment.
                    setUseCaseType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
        } else {
            mHyperTrackMapFragment.setUseCase(mLocationSharingView);
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
            mLocationSharingView = (LocationSharingView)
                    mHyperTrackMapFragment.setUseCaseType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
            return;
        }

        if (!mLocationSharingView.isViewCreated()) {
            return;
        }

        if (isRestoreLocationSharing) {
            if (expectedPlace != null) {
                mLocationSharingView.hideCTAButton();
            } else {
                setTopButtonToUpdateExpectedPlace();
            }
            stopLocationSharingButton.setVisibility(View.VISIBLE);
            updateExpectedPlaceButton.setVisibility(View.GONE);
        } else {
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
        if (drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawers();
            return;
        }

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