
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
import android.animation.ValueAnimator;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.hypertrack.hyperlog.HyperLog;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.HyperTrackConstants;
import com.hypertrack.lib.HyperTrackUtils;
import com.hypertrack.lib.MapFragmentCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.HyperTrackError;
import com.hypertrack.lib.models.HyperTrackLocation;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.User;
import com.hypertrack.lib.tracking.BaseMVP.BaseTrackingView;
import com.hypertrack.lib.tracking.BottomCardItemView;
import com.hypertrack.lib.tracking.MapProvider.GoogleMapFragmentView;

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

public class Home extends BaseActivity implements HomeView, View.OnClickListener {

    private static final String TAG = Home.class.getSimpleName();
    private GoogleMap mMap;
    Marker currentLocationMarker;
    GroundOverlay circle;
    ValueAnimator valueAnimator = null;
    private Location defaultLocation = new Location("default");
    private Place expectedPlace;

    private float zoomLevel = 16.0f;
    int circleRadius = 160;

    private boolean isMapLoaded = false;
    private boolean fromPlaceline = false;

    GoogleMapFragmentView googleMapFragmentView;
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

    BottomCardItemView stopLocationSharingButton;
    BottomCardItemView updateExpectedPlaceButton;

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
        public void onCameraIdleCallback(Context context, GoogleMap map) {
           /* float zoom = map.getCameraPosition().zoom;
            float roundZoom = (float) ((5 * (Math.round(zoom * 10 / 5))) / 10.0);
            if (roundZoom == (int) roundZoom) {
                circleRadius = (int) (10 * Math.pow(2, 20 - roundZoom));
            } else {
                circleRadius = (int) (10 * Math.pow(2, (20 - (int) roundZoom)));
                circleRadius -= circleRadius / 4;
            }
            if (circle != null) {
                startPulse(true);
            }*/
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
        public void onMapLoadedCallback(Context context, GoogleMap map) {
            isMapLoaded = true;
        }

        @Override
        public void onActionRefreshed(List<String> refreshedActionIds, List<Action> refreshedActions) {
            ActionManager actionManager = ActionManager.getSharedManager(Home.this);

            if (actionManager.getHyperTrackAction() != null) {
                //Get the index of active action from refreshed action Ids
                int index = refreshedActionIds.indexOf(actionManager.getHyperTrackActionId());

                if (index >= 0) {
                    //Get refreshed action Data
                    Action action = refreshedActions.get(refreshedActionIds.indexOf(
                            actionManager.getHyperTrackActionId()));
                    //Update action data to Shared Preference
                    actionManager.setHyperTrackAction(action);

                    if (action.getExpectedPlace() != null && action.getExpectedPlace().getLocation() != null &&
                            action.getUser().getId().equalsIgnoreCase(HyperTrack.getUserId())) {
                        actionManager.setShortcutPlace(action.getExpectedPlace());
                    }

                    if (refreshedActionIds.size() > 1) {
                        actionManager.setTrackingAction(refreshedActions.get(Math.abs(index - 1)));
                    }
                }
            }
        }

        @Override
        public void onChooseOnMapSelected() {

        }

        @Override
        public void onPlaceSelectorViewShown() {
        }

        @Override
        public void onPlaceSelectorViewClosed() {
            if (isUpdateExpectedPlace) {
                setTopButtonToUpdateExpectedPlace();
            } else if (isCreateAction) {
                setTopButtonToCreateAction();
            }
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
        public void onBeginEditingExpectedPlace(Context context, String actionID) {
            isEditing = true;
        }

        @Override
        public void onCanceledEditingExpectedPlace(Context context, String actionID) {
            isEditing = false;
        }

        @Override
        public void onEndEditingExpectedPlace(Context context, String actionID, Place chooseOnMapDestinationPlace) {
            isEditing = false;
            ActionManager actionManager = ActionManager.getSharedManager(Home.this);
            actionManager.setPlace(chooseOnMapDestinationPlace);
            actionManager.onActionStart();
        }

        @Override
        public void onReceiveEditExpectedPlaceError(Context context, String actionID, String errorMessage) {
            isEditing = false;
        }
    };

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
                        .setIcon(Icon.createWithResource(this, R.drawable.ic_marker_gray))
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

        if (getIntent() != null && getIntent().hasExtra("class_from")) {
            if (getIntent().getStringExtra("class_from").
                    equalsIgnoreCase(Placeline.class.getSimpleName()))
                fromPlaceline = true;
        }

        //Initialize Map Fragment added in Activity Layout to getMapAsync
        googleMapFragmentView = (GoogleMapFragmentView)
                getSupportFragmentManager().findFragmentById(R.id.htMapfragment);

        adapter = new HomeMapAdapter(this);
        googleMapFragmentView.setMapAdapter(adapter);
        googleMapFragmentView.setCallback(callback);

        googleMapFragmentView.setUseCaseType(BaseTrackingView.Type.LIVE_LOCATION_SHARING);

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

        // Check if location is already being shared
        isRestoreLocationSharing = presenter.restoreLocationSharing();

        //Check if user click on someone else tracking link
        isHandleTrackingUrlDeeplink = handleDeepLinkTrackingUrl();

        //Check if user clicked on shortcut icon to share location
        if (handleShortcut())
            isShortcut = true;

        if (!isRestoreLocationSharing && !isHandleTrackingUrlDeeplink && !isShortcut) {
            setTopButtonToCreateAction();
        }
    }

    public void setTopButtonToCreateAction() {
        googleMapFragmentView.setTopButtonText(getString(R.string.share_your_location));
        googleMapFragmentView.setTopButtonClickListener(this);
        googleMapFragmentView.showTopButton();
        isCreateAction = true;
    }

    public void setTopButtonToUpdateExpectedPlace() {
        googleMapFragmentView.setTopButtonText(getString(R.string.share_eta));
        googleMapFragmentView.setTopButtonClickListener(this);
        googleMapFragmentView.showTopButton();
        isCreateAction = false;
        isUpdateExpectedPlace = true;
    }

    private void initializeUIViews() {
        // Setup Info message view layouts
        infoMessageView = (LinearLayout) findViewById(R.id.home_info_message_view);
        infoMessageViewText = (TextView) findViewById(R.id.home_info_message_text);

        updateExpectedPlaceButton = new BottomCardItemView(this);
        updateExpectedPlaceButton.setDescription("Share meeting location");
        updateExpectedPlaceButton.setDescriptionTextColor(R.color.info_box_destination);
        updateExpectedPlaceButton.setActionButtonIcon(R.drawable.ic_keyboard_arrow_right_black_18dp);
        updateExpectedPlaceButton.showOnlyActionButtonIcon();
        updateExpectedPlaceButton.setVisibility(View.GONE);
        updateExpectedPlaceButton.setActionButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMapFragmentView.openPlacePicker();
                googleMapFragmentView.hideTopButton();
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

        List<BottomCardItemView> bottomCardItemViews = new ArrayList<>();
        bottomCardItemViews.add(updateExpectedPlaceButton);
        bottomCardItemViews.add(stopLocationSharingButton);
        googleMapFragmentView.addBottomViewItems(bottomCardItemViews);
    }

    private void stopTracking() {
        if (HyperTrack.checkLocationPermission(Home.this)
                && HyperTrack.checkLocationServices(Home.this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.stop));
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("Stop", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    presenter.stopSharing(false);
                    collectionId = null;
                    HyperTrack.removeActions(null);
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
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
            showLoading(getString(R.string.fetching_details_msg));
            // Get required parameters for tracking Actions on map
            collectionId = intent.getStringExtra(Track.KEY_COLLECTION_ID);
            // Call trackActionsOnMap method
            presenter.trackActionsOnMap(collectionId,true);
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

            showLoading(getString(R.string.sharing_live_location_message));
            expectedPlace = shortcutPlace;
            shareLiveLocation();
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
        updateExpectedPlaceButton.setVisibility(View.GONE);
        if (isUpdateExpectedPlace)
            googleMapFragmentView.hideTopButton();
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
        //updateMapView();
        //updateMapPadding();
    }

    /*private void updateCurrentLocationMarker(final HyperTrackLocation location) {
        if (!showCurrentLocationMarker) {
            if (currentLocationMarker != null)
                currentLocationMarker.remove();
            currentLocationMarker = null;
            return;
        }
        if (ActionManager.getSharedManager(this).isActionLive()) {
            if (circle != null) {
                stopPulse();
            }
            return;
        }

        if (location == null || location.getGeoJSONLocation() == null ||
                location.getGeoJSONLocation().getLatLng() == null) {
            HyperTrack.getCurrentLocation(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse response) {
                    Log.d(TAG, "onSuccess: Current Location Recieved");
                    HyperTrackLocation hyperTrackLocation =
                            new HyperTrackLocation((Location) response.getResponseObject());
                    SharedPreferenceManager.setLastKnownLocation(Home.this, (Location) response.getResponseObject());
                    updateCurrentLocationMarker(hyperTrackLocation);
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    Log.d(TAG, "onError: Current Location Receiving error");
                    Log.d(TAG, "onError: " + errorResponse.getErrorMessage());
                }
            });
            return;
        }
        LatLng latLng = location.getGeoJSONLocation().getLatLng();
        if (currentLocationMarker == null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions().
                    position(latLng).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_source_place_marker))
                    .anchor(0.5f, 0.5f));
            addPulseRing(latLng);
        } else {
            currentLocationMarker.setVisible(true);
            circle.setPosition(latLng);
            circle.setVisible(true);
//            MarkerAnimation.animateMarker(currentLocationMarker, latLng);
        }
        startPulse(false);
        if (!isRestoreLocationSharing && !isHandleTrackingUrlDeeplink)
            updateMapView();
    }*/

    /*private void addPulseRing(LatLng latLng) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setSize(500, 500);
        d.setColor(ContextCompat.getColor(this, R.color.pulse_color));

        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth()
                , d.getIntrinsicHeight()
                , Bitmap.Config.ARGB_8888);

        // Convert the drawable to bitmap
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);

        // Radius of the circle
        final int radius = 100;

        // Add the circle to the map
        circle = mMap.addGroundOverlay(new GroundOverlayOptions()
                .position(latLng, 2 * radius).image(BitmapDescriptorFactory.fromBitmap(bitmap)));
    }
*/
   /* private void startPulse(boolean reset) {

        if (!HyperTrackUtils.isInternetConnected(this)) {
            if (circle != null) {
                circle.remove();
            }
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
        }
        if (valueAnimator == null || reset) {
            if (valueAnimator != null)
                valueAnimator.end();
            final int[] radius = {circleRadius};
            valueAnimator = new ValueAnimator();
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setIntValues(0, (int) radius[0]);
            valueAnimator.setDuration(2000);
            valueAnimator.setEvaluator(new IntEvaluator());
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    circle.setDimensions(animatedFraction * (int) radius[0]);
                    circle.setTransparency(animatedFraction);
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    circle.setTransparency(1f);
                    circle.setVisible(true);
                    if (currentLocationMarker != null)
                        currentLocationMarker.setVisible(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    ObjectAnimator.ofFloat(circle, "transparency", 1f, 0f).setDuration(500).start();
                    circle.setVisible(false);
                    if (currentLocationMarker != null)
                        currentLocationMarker.setVisible(false);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    //radius[0] = circleRadius;
                }
            });
        }

        valueAnimator.start();
    }

    private void stopPulse() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }*/

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

    private void updateMapPadding() {
        if (mMap != null) {
            int top = getResources().getDimensionPixelSize(R.dimen.map_top_padding);
            int left = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            int right = expectedPlace == null ? getResources().getDimensionPixelSize(R.dimen.map_side_padding) :
                    getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            int bottom = getResources().getDimensionPixelSize(R.dimen.map_side_padding);
            mMap.setPadding(left, top, right, bottom);
        }
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
    public void onClick(View topButton) {
        if (isCreateAction) {
            shareLiveLocation();
        } else if (isUpdateExpectedPlace) {
            if (googleMapFragmentView != null) {
                googleMapFragmentView.openPlacePicker();
                googleMapFragmentView.hideTopButton();
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        ActionManager actionManager = ActionManager.getSharedManager(this);
        Action action = actionManager.getHyperTrackAction();

        if (action != null && !action.isCompleted()) {
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
        HyperTrack.removeActions(null);

       /* ActionManager actionManager = ActionManager.getSharedManager(this);
        //If tracking action has completed and summary view is visible then on back press clear the view
        // so that user can share new tracking url without reopening the app.
        if (actionManager.getHyperTrackAction() != null &&
                actionManager.getHyperTrackAction().hasFinished()) {

            // Reset uniqueId variable
            stopSharingLiveLocation();
            ActionManager.getSharedManager(this).clearState();
        } else if (!fromPlaceline) {
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
}