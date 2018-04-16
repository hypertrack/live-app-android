
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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackConstants;
import com.hypertrack.lib.HyperTrackUtils;
import com.hypertrack.lib.MapFragmentCallback;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.HyperTrackError;
import com.hypertrack.lib.models.HyperTrackLocation;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.placeline.Placeline;
import com.hypertrack.lib.tracking.BaseMVP.BaseView;
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
import io.hypertrack.sendeta.receiver.GpsLocationReceiver;
import io.hypertrack.sendeta.receiver.NetworkChangeReceiver;
import io.hypertrack.sendeta.service.FetchLocationIntentService;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.util.Constants;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.Utils;

public class Home extends BaseActivity implements HomeView, CTAButton.OnClickListener,
        Placeline.PlacelineViewListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = Home.class.getSimpleName();
    private GoogleMap mMap;
    private Location defaultLocation = new Location("default");
    private Place expectedPlace;

    private float zoomLevel = 16.0f;

    HyperTrackMapFragment hyperTrackMapFragment;
    private HomeMapAdapter adapter;

    private String collectionId = null;

    private TextView infoMessageViewText;
    private LinearLayout infoMessageView;
    private ProgressDialog mProgressDialog;

    private IHomePresenter<HomeView> presenter = new HomePresenter();

    boolean showCurrentLocationMarker = true;
    boolean isRestoreLocationSharing = false, isHandleTrackingUrlDeeplink = false,
            isShortcut = false, isEditing = false, isCreateAction = false,
            isUpdateExpectedPlace = false;

    private boolean isPlaceUpdating;

    BottomCardItemView stopLocationSharingButton;
    BottomCardItemView updateExpectedPlaceButton;
    RelativeLayout locationSharingActiveButton;
    TextView mActionText;

    List<BottomCardItemView> bottomCardItemViews;
    BaseView mSummaryView, mSelectExpectedPlaceView;
    LocationSharingView mLocationSharingView;

    private boolean fromPlaceline = false;

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
        public void onMapReadyCallback(Context context, GoogleMap map) {
            onMapReady(map);
        }

        @Override
        public void onExpectedPlaceSelected(Place expectedPlace) {
            // Check if destination place was selected
            if (expectedPlace != null) {
                onSelectPlace(expectedPlace);
                addDynamicShortcut(expectedPlace);
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
        public void onChooseOnMapSelected() {

        }

        @Override
        public void onPlaceSelectorViewShown() {
            Log.d(TAG, "onPlaceSelectorViewShown: ");
        }

        @Override
        public void onPlaceSelectorViewClosed() {
            setLocationSharingView();
//            if (isUpdateExpectedPlace) {
//                setTopButtonToUpdateExpectedPlace();
//            }

        }

        @Override
        public void onBackButtonIconPressed() {
            if (expectedPlace == null) {
                //finish();
                onBackPressed();
            }
        }

        @Override
        public void onLiveLocationSharingSummaryCardShown() {
        }

        @Override
        public void onHeaderActionButtonClicked(String metaData) {
            super.onHeaderActionButtonClicked(metaData);
            presenter.getShareMessage();
        }

        @Override
        public void onCallButtonClicked(Context context, String actionID) {
            super.onCallButtonClicked(context, actionID);
            Toast.makeText(context, "Call Button Clicked " + actionID, Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onBackButtonPressed() {
            if (hyperTrackMapFragment == null)
                return false;

            if (hyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.LIVE_LOCATION_SHARING) {
                hyperTrackMapFragment.hideBackButton();
                if (mSummaryView == null)
                    mSummaryView = hyperTrackMapFragment.setUseCaseType(MapFragmentView.Type.PLACELINE_SUMMARY);
                else {
                    mSummaryView.setType(MapFragmentView.Type.PLACELINE_SUMMARY);
                    hyperTrackMapFragment.setUseCase(mSummaryView);
                }
                HyperTrack.removeActions(null);
                mLocationSharingView = null;
                return true;
            }
            return false;
        }

        @Override
        public boolean onPlacelineViewClosed() {
            mSummaryView.setType(MapFragmentView.Type.PLACELINE_SUMMARY);
            hyperTrackMapFragment.setUseCase(mSummaryView);
            return true;
        }

        @Override
        public void onPlacelineViewShown() {
            if (isRestoreLocationSharing) {
                locationSharingActiveButton.setVisibility(View.GONE);
                //AnimationUtils.collapse(locationSharingActiveButton);
            }
        }

        @Override
        public void onBottomBaseViewCreated(@MapFragmentView.Type int useCaseType) {
            switch (useCaseType) {
                case MapFragmentView.Type.LIVE_LOCATION_SHARING:
                    updateLiveLocationSharingView();
                    break;
                case MapFragmentView.Type.PLACELINE_SUMMARY:
                    if (!isRestoreLocationSharing) {
                        setTopButtonToCreateAction();
                        locationSharingActiveButton.setVisibility(View.VISIBLE);
                    } else {
                        mSummaryView.hideCTAButton();
                        locationSharingActiveButton.setVisibility(View.VISIBLE);
                    }
                    hyperTrackMapFragment.hideBackButton();
                    break;
            }
        }
    };

    private void updateLiveLocationSharingView() {
        hyperTrackMapFragment.showBackButton();
        if (mLocationSharingView == null && !mLocationSharingView.isViewCreated()) {
            mLocationSharingView = (LocationSharingView)
                    hyperTrackMapFragment.setUseCaseType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
            return;
        }
        // ActionManager actionManager = ActionManager.getSharedManager(this);
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

    private void addDynamicShortcut(Place place) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                String name = place.getPlaceDisplayString();

                //If there is no address or name in place model then don't add as dynamic shortcut
                // because shortlabel couldn't be empty.
                if (HTTextUtils.isEmpty(name))
                    return;

                int count = 0;
                if (shortcutManager.getDynamicShortcuts() != null) {
                    count = shortcutManager.getDynamicShortcuts().size();
                }
                if (count > 2) {
                    String id = shortcutManager.getDynamicShortcuts().get(0).getId();
                    List<String> shortcutIds = new ArrayList<>();
                    shortcutIds.add(id);
                    shortcutManager.removeDynamicShortcuts(shortcutIds);
                }

                List<ShortcutInfo> shortcut = new ArrayList<>();

                shortcut.add(0, new ShortcutInfo.Builder(this, place.getLocation().getLatLng().toString())
                        .setShortLabel(name)
                        .setIcon(Icon.createWithResource(this, R.drawable.ic_result_place))
                        .setIntent(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("share.location://hypertrack")))
                        .build());
                shortcutManager.addDynamicShortcuts(shortcut);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initialize Map Fragment added in Activity Layout to getMapAsync
        hyperTrackMapFragment = (HyperTrackMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.htMapfragment);

        adapter = new HomeMapAdapter(this);
        hyperTrackMapFragment.setMapAdapter(adapter);
        hyperTrackMapFragment.setMapCallback(callback);

        // Initialize UI Views
        initializeUIViews();

        // Get Default User Location from his CountryCode
        // SKIP: if Location Permission is Granted and Location is Enabled
        if (!HyperTrack.checkLocationServices(this) || !HyperTrack.checkLocationPermission(this)) {
            geocodeUserCountryName();
        }

        // Check & Prompt User if Internet is Not Connected
        if (!HyperTrackUtils.isInternetConnected(this)) {
            Toast.makeText(this, R.string.network_issue, Toast.LENGTH_SHORT).show();
        }

        // Attach View Presenter to View
        presenter.attachView(this);

        presenter.setActionManager(ActionManager.getSharedManager(this));

        //Check if user clicked on shortcut icon to share location
        if (handleShortcut())
            isShortcut = true;

        //Check if user click on someone else tracking link
        isHandleTrackingUrlDeeplink = handleDeepLinkTrackingUrl();

        // Check if location is already being shared
        isRestoreLocationSharing = presenter.restoreLocationSharing();

        //If user clicked on deep-link url or clicked on shortcut then set tracking view as
        // LiveLocationSharing View
        if (isHandleTrackingUrlDeeplink || isShortcut) {
            mLocationSharingView = (LocationSharingView) hyperTrackMapFragment.
                    setUseCaseType(MapFragmentView.Type.LIVE_LOCATION_SHARING);

            if (isShortcut) {
                showLoading(getString(R.string.sharing_live_location_message));
                shareLiveLocation();
            } else if (isHandleTrackingUrlDeeplink) {
                showLoading(getString(R.string.fetching_details_msg));
                // Call trackActionsOnMap method
                presenter.trackActionsOnMap(collectionId, true);
            }
        } else if (!isShortcut) {
            mSummaryView = hyperTrackMapFragment.setUseCaseType(MapFragmentView.Type.PLACELINE_SUMMARY);
            if (isRestoreLocationSharing) {
                locationSharingActiveButton.setVisibility(View.VISIBLE);
                //AnimationUtils.expand(locationSharingActiveButton);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActionManager actionManager = ActionManager.getSharedManager(this);
        Action action = actionManager.getHyperTrackAction();

        if (action != null
                && hyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.LIVE_LOCATION_SHARING) {

            mLocationSharingView.addBottomViewItems(bottomCardItemViews);
            actionManager.setActionComletedListener(actionCompletedListener);
            collectionId = action.getCollectionId();
            presenter.trackActionsOnMap(collectionId, false);
        }

        // Check if Location & Network are Enabled
        updateInfoMessageView();

        // Re-register BroadcastReceiver for Location_Change, Network_Change & GCM
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationChangeReceiver,
                new IntentFilter(GpsLocationReceiver.LOCATION_CHANGED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mConnectivityChangeReceiver,
                new IntentFilter(NetworkChangeReceiver.NETWORK_CHANGED));

        registerBroadcastReceiver();
    }

    public void setTopButtonToCreateAction() {
        mSummaryView.setCTAButtonTitle(getString(R.string.share_your_location));
        mSummaryView.setCTAButtonClickListener(this);
        mSummaryView.showCTAButton();
        isCreateAction = true;
        if (hyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.PLACELINE_SUMMARY) {
            collectionId = null;
            ActionManager.getSharedManager(this).deleteTrackingAction();
        }
    }

    public void setTopButtonToUpdateExpectedPlace() {
        if (hyperTrackMapFragment.getUseCaseType() == MapFragmentView.Type.LIVE_LOCATION_SHARING) {
            mLocationSharingView.setCTAButtonTitle(getString(R.string.share_eta));
            mLocationSharingView.setCTAButtonClickListener(this);
            mLocationSharingView.showCTAButton();
            isCreateAction = false;
            isUpdateExpectedPlace = true;
        }
    }

    private void initializeUIViews() {

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        if (HyperTrack.isTracking()) {
            navigationView.getMenu().findItem(R.id.start_tracking_toggle).setTitle(R.string.stop_tracking);
        }
        navigationView.setNavigationItemSelectedListener(this);

        infoMessageView = findViewById(R.id.home_info_message_view);
        infoMessageViewText = findViewById(R.id.home_info_message_text);

        locationSharingActiveButton = findViewById(R.id.location_sharing_active);
        mActionText = findViewById(R.id.action_text);
        mActionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationSharingView == null) {
                    setLocationSharingView();
                } else {
                    mLocationSharingView.setType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
                    hyperTrackMapFragment.setUseCase(mLocationSharingView);
                }

                presenter.trackActionsOnMap(ActionManager.getSharedManager(Home.this).getHyperTrackActionCollectionId(),
                        false);
                locationSharingActiveButton.setVisibility(View.GONE);

                //AnimationUtils.collapse(locationSharingActiveButton);
            }
        });

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
                            hyperTrackMapFragment.setUseCaseType(MapFragmentView.Type.PLACE_SELECTOR);
                } else {
                    mSelectExpectedPlaceView.setType(MapFragmentView.Type.PLACE_SELECTOR);
                    hyperTrackMapFragment.setUseCase(mSelectExpectedPlaceView);
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

    private void stopTracking() {
        if (HyperTrack.checkLocationPermission(Home.this)
                && HyperTrack.checkLocationServices(Home.this)) {
            isRestoreLocationSharing = false;
            presenter.stopSharing(false);
            collectionId = null;
            HyperTrack.removeActions(null);
            if (mSummaryView == null)
                mSummaryView = hyperTrackMapFragment.setUseCaseType(MapFragmentView.Type.PLACELINE_SUMMARY);
            else {
                mSummaryView.setType(MapFragmentView.Type.PLACELINE_SUMMARY);
                hyperTrackMapFragment.setUseCase(mSummaryView);
            }
            setTopButtonToCreateAction();
            mLocationSharingView = null;
            hyperTrackMapFragment.setPlacelineViewListener(this);
        } else {
            if (!HyperTrack.checkLocationServices(Home.this)) {
                HyperTrack.requestLocationServices(Home.this);
            } else {
                HyperTrack.requestPermissions(Home.this);
            }
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

    private boolean handleShortcut() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("shortcut", false)) {

            if (isRestoreLocationSharing) {
                Toast.makeText(this, "Previous trip is already active.",
                        Toast.LENGTH_SHORT).show();
                return false;
            }

            Place shortcutPlace = ActionManager.getSharedManager(this).getShortcutPlace();

            if (shortcutPlace == null || shortcutPlace.getLocation() == null) {
                return false;
            }
            expectedPlace = shortcutPlace;
            return true;
        }
        return false;
    }

    @Override
    public void showTrackActionsOnMapError(ErrorResponse errorResponse) {
        Toast.makeText(this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
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

    private void initializeProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.loading));
        mProgressDialog.setCancelable(false);
    }

    @Override
    public void hideLoading() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    @Override
    public void showPlacePickerButton() {
        setTopButtonToUpdateExpectedPlace();
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
    public void showShareLocationButton() {
        setTopButtonToCreateAction();
    }

    @Override
    public void showStopSharingButton() {
        stopLocationSharingButton.setVisibility(View.VISIBLE);
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
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(16.9f);
        LatLng latLng;
        ActionManager actionManager = ActionManager.getSharedManager(this);
        if (actionManager.getHyperTrackActionId() == null) {

            if (googleMap != null && googleMap.isMyLocationEnabled() && googleMap.getMyLocation() != null) {
                SharedPreferenceManager.setLastKnownLocation(Home.this, googleMap.getMyLocation());
                latLng = new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

            } else {
                // Set Default View for map according to User's LastKnownLocation
                if (SharedPreferenceManager.getLastKnownLocation(Home.this) != null) {
                    defaultLocation = SharedPreferenceManager.getLastKnownLocation(Home.this);
                }

                // Else Set Default View for map according to either User's Default Location
                // (If Country Info was available) or (0.0, 0.0)
                if (defaultLocation != null && defaultLocation.getLatitude() != 0.0
                        && defaultLocation.getLongitude() != 0.0) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(defaultLocation.getLatitude(), defaultLocation.getLongitude()), zoomLevel));
                }
            }
        }

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        checkForLocationSettings();
    }

    /*private void updateMapView() {
        if (mMap == null || !isMapLoaded) {
            return;
        }

        if (currentLocationMarker == null && expectedPlace == null) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (currentLocationMarker != null) {
            LatLng current = currentLocationMarker.getPosition();
            builder.include(current);
        }

        if (expectedPlace != null && expectedPlace.getLocation() != null &&
                expectedPlace.getLocation().getLatLng() != null) {
            LatLng destination = expectedPlace.getLocation().getLatLng();
            builder.include(destination);
        }

        LatLngBounds bounds = builder.build();

        try {
            CameraUpdate cameraUpdate;
            if (expectedPlace != null && currentLocationMarker != null) {
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.12);
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            } else {
                LatLng latLng = currentLocationMarker != null ?
                        currentLocationMarker.getPosition() : expectedPlace.getLocation().getLatLng();
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
            }

            mMap.animateCamera(cameraUpdate, 1000, null);

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }*/

    private void createAction() {
        User user = OnboardingManager.sharedManager(this).getUser();
        presenter.shareLiveLocation(user);
    }

    @Override
    public void showShareLiveLocationSuccess(Action action) {
        // bottomButtonCard.hideBottomCardLayout();
        isRestoreLocationSharing = true;
        if (mLocationSharingView == null) {
            setLocationSharingView();
        } else {
            mLocationSharingView.setType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
            hyperTrackMapFragment.setUseCase(mLocationSharingView);
        }

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
//        if (htMapFragment != null) {
//            htMapFragment.notifyChanged();
//        }
    }

    @Override
    public void showStopSharingSuccess() {
        stopSharingLiveLocation();
    }

    /**
     * Method to update State Variables & UI to reflect Task Ended
     */
    private void stopSharingLiveLocation() {

        ActionManager.getSharedManager(Home.this).clearState();
        expectedPlace = null;
        showCurrentLocationMarker = true;
        isRestoreLocationSharing = false;
        isHandleTrackingUrlDeeplink = false;
        isUpdateExpectedPlace = false;
        setTopButtonToCreateAction();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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

    private void geocodeUserCountryName() {
        // Fetch Country Level Location only if no cached location is available
        Location lastKnownCachedLocation = SharedPreferenceManager.getLastKnownLocation(this);
        if (lastKnownCachedLocation == null || lastKnownCachedLocation.getLatitude() == 0.0
                || lastKnownCachedLocation.getLongitude() == 0.0) {

            OnboardingManager onboardingManager = OnboardingManager.sharedManager(this);
            String countryName = Utils.getCountryName(onboardingManager.getUser().getCountryCode());
            if (!HTTextUtils.isEmpty(countryName)) {
                Intent intent = new Intent(this, FetchLocationIntentService.class);
                intent.putExtra(FetchLocationIntentService.RECEIVER, new GeocodingResultReceiver(new Handler()));
                intent.putExtra(FetchLocationIntentService.ADDRESS_DATA_EXTRA, countryName);
                startService(intent);
            }
        }
    }

    @Override
    public void onTitleButtonClick() {
        if (isCreateAction) {
            shareLiveLocation();
        } else if (isUpdateExpectedPlace) {
            if (mSelectExpectedPlaceView == null) {
                mSelectExpectedPlaceView =
                        hyperTrackMapFragment.setUseCaseType(MapFragmentView.Type.PLACE_SELECTOR);
            } else {
                mSelectExpectedPlaceView.setType(MapFragmentView.Type.PLACE_SELECTOR);
                hyperTrackMapFragment.setUseCase(mSelectExpectedPlaceView);
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
    public void onPlacelineViewShown() {


    }

    @Override
    public void onPlacelineViewExpanded() {

    }

    @Override
    public void onPlacelineViewCollapsed() {

    }

    @Override
    public void onPlacelineViewClosed() {

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
                                locationSharingActiveButton.performClick();
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

    @SuppressLint("ParcelCreator")
    private class GeocodingResultReceiver extends ResultReceiver {
        GeocodingResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchLocationIntentService.SUCCESS_RESULT) {
                LatLng latLng = resultData.getParcelable(FetchLocationIntentService.RESULT_DATA_KEY);
                if (latLng == null)
                    return;
                defaultLocation.setLatitude(latLng.latitude);
                defaultLocation.setLongitude(latLng.longitude);
                Log.d(TAG, "Geocoding for Country Name Successful: " + latLng.toString());

                if (mMap != null) {
                    if (defaultLocation.getLatitude() != 0.0 || defaultLocation.getLongitude() != 0.0)
                        zoomLevel = 16.9f;

                    // Check if any Location Data is available, meaning Country zoom level need not be used
                    Location lastKnownCachedLocation = SharedPreferenceManager.getLastKnownLocation(Home.this);
                    if (lastKnownCachedLocation != null && lastKnownCachedLocation.getLatitude() != 0.0
                            && lastKnownCachedLocation.getLongitude() != 0.0) {
                        return;
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                }
            }
        }
    }

    BroadcastReceiver mLocationChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfoMessageView();
        }
    };

    BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfoMessageView();
        }
    };

    private void updateInfoMessageView() {
        if (!HyperTrackUtils.isLocationEnabled(Home.this)) {
            infoMessageView.setVisibility(View.VISIBLE);

            if (!HyperTrackUtils.isInternetConnected(this)) {
                infoMessageViewText.setText(R.string.location_off_info_message);
            } else {
                infoMessageViewText.setText(R.string.location_off_info_message);
            }
        } else {
            infoMessageView.setVisibility(View.VISIBLE);

            if (!HyperTrackUtils.isInternetConnected(this)) {
                infoMessageViewText.setText(R.string.internet_off_info_message);
            } else {
                // Both Location & Network Enabled, Hide the Info Message View
                infoMessageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectivityChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationChangeReceiver);
        unRegisterBroadcastReceiver();
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
        }/* else if (!fromPlaceline) {
            startActivity(new Intent(Home.this, Placeline.class));
        }*/
        //finish();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        presenter.clearTrackingAction();
        // Detach View from Presenter
        presenter.detachView();
        super.onDestroy();
    }

    private void registerBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HyperTrackConstants.HT_USER_CURRENT_ACTIVITY_INTENT);
        intentFilter.addAction(HyperTrackConstants.HT_USER_CURRENT_LOCATION_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);
    }

    private void unRegisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(HyperTrackConstants.HT_USER_CURRENT_LOCATION_INTENT)) {
                    HyperTrackLocation location = (HyperTrackLocation)
                            intent.getSerializableExtra(HyperTrackConstants.HT_USER_CURRENT_LOCATION_KEY);
                    //updateCurrentLocationMarker(location);
                }
            }
        }
    };

    void setLocationSharingView() {
        if (mLocationSharingView == null) {
            mLocationSharingView = (LocationSharingView) hyperTrackMapFragment.
                    setUseCaseType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
        } else {
            mLocationSharingView.setType(MapFragmentView.Type.LIVE_LOCATION_SHARING);
            hyperTrackMapFragment.setUseCase(mLocationSharingView);
        }

    }
}