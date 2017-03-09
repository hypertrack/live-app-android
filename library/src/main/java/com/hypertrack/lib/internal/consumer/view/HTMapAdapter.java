package com.hypertrack.lib.internal.consumer.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.hypertrack.lib.R;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.HTConsumerClient;
import com.hypertrack.lib.internal.consumer.models.ActionListCallBack;
import com.hypertrack.lib.internal.consumer.models.HTAction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can be overridden to customise HyperTrackMapFragment interface.
 * <br>
 * An instance of MapAdapter needs to be passed to the method
 * {@link HyperTrackMapFragment#setHTMapAdapter(HTMapAdapter)} as parameter.
 * <br>
 * The primary API you can override is {@link #getActionIDsToTrack(HyperTrackMapFragment)} where you provide
 * a list of ActionIDs to be tracked on {@link HyperTrackMapFragment}.
 * <br>
 * Other APIs in MapAdapter can be used to customise UI elements in HyperTrackMapFragment.
 */
public class HTMapAdapter {

    private static final String TAG = HTMapAdapter.class.getSimpleName();

    private static final String ActionsDestinationLocationDifferentMessage = "Actions to be tracked have different destinations." +
            " Please initiate multiple action tracking for actions with same destination.";
    private final boolean showDestinationMarkerForMultipleActionsView = true;
    private final boolean showAddressInfoLayoutForMultipleActionsView = true;
    private final boolean showUserInfoLayoutForMultipleActionsView = true;
    private final boolean rotateHeroMarker = true;
    private final boolean isDestinationMarkerVisible = true;
    private final boolean isActionSummaryInfoVisible = true;
    private final boolean isAddressInfoVisible = true;
    private final boolean isUserInfoVisible = true;
    private final boolean isOrderDetailsButtonVisible = false;
    private final boolean isSourceMarkerVisible = false;
    private final boolean isCallButtonVisible = true;
    private final boolean disableEditDestination = false;
    private final boolean showUserLocationMissingAlert = true;
    private final boolean showEditDestinationFailureAlert = true;
    private final boolean shouldBindView = true;
    private final boolean isOrderStatusBarEnabled = true;
    private final boolean isDynamicZoomDisabled = false;
    private final boolean isTrafficEnabled = true;
    private Context mContext;
    private MapUpdateInterface mapUpdateInterface;
    private HTConsumerClient consumerClient;
    private HTMapViewModelList mapViewModelList;
    private MapFragmentCallback mapFragmentCallback;
    private List<String> actionIDsBeingTracked;
    private List<String> actionIDsSetupForTrackingFailed;
    private List<String> actionIDsAddedForTracking;

    public HTMapAdapter(Context mContext) {
        this.mContext = mContext;
        this.actionIDsAddedForTracking = new ArrayList<>();
        this.actionIDsBeingTracked = new ArrayList<>();
        this.actionIDsSetupForTrackingFailed = new ArrayList<>();
    }

    /**
     * Call this method to provide a List of Active Actions that have to be tracked on the Map View.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero ic_ht_hero_marker is placed.
     * @return List of ActionIDs to be tracked.
     */
    public List<String> getActionIDsToTrack(HyperTrackMapFragment hyperTrackMapFragment) {
        return consumerClient.getActionIDList() != null ? consumerClient.getActionIDList() : new ArrayList<String>();
    }

    /**
     * Call this method to set initial state for Map View in HyperTrackMapFragment.
     * This method requires an instance of CameraUpdate object which defines a camera move. This
     * CameraUpdate instance is used to modify the map's initial camera state.
     *
     * @param hyperTrackMapFragment The fragment in which the Map view is placed.
     * @return Instance of CameraUpdate defining the initial state for map's camera.
     */
    public CameraUpdate getMapFragmentInitialState(HyperTrackMapFragment hyperTrackMapFragment) {
        return null;
    }

    /**
     * Call this method to display Google Map's MyLocation ic_ht_hero_marker on the MapView.
     * <br>
     *
     * @param hyperTrackMapFragment The fragment in which the Map view is placed.
     * @return boolean flag to enable/disable GoogleMap's MyLocation ic_ht_hero_marker in the map view.
     * <br>
     * REQUIRED: To enable MyLocation Marker, ACCESS_FINE_LOCATION Permission must be granted to
     * the application. Refer to <a href="https://developer.android.com/training/permissions/declaring.html">
     * Android docs</a> on adding Location Permissions to the app.
     */
    public boolean setMyLocationEnabled(HyperTrackMapFragment hyperTrackMapFragment) {
        return false;
    }

    /**
     * Call this method to display Google Map's MyLocationButton on the MapView.
     * <br>
     * REQUIRED: To enable MyLocationButton, ACCESS_FINE_LOCATION Permission must be granted to
     * the application. Refer to <a href="https://developer.android.com/training/permissions/declaring.html">
     * Android docs</a> on adding Location Permissions to the app.
     * <br>
     *
     * @param hyperTrackMapFragment The fragment in which the Map view is placed.
     * @return boolean flag to enable/disable GoogleMap's MyLocationButton in the map view.
     */
    public boolean setMyLocationButtonEnabled(HyperTrackMapFragment hyperTrackMapFragment) {
        return false;
    }

    /**
     * Call this method to provide a Custom Icon to the hero ic_ht_hero_marker.
     * <br>
     * NOTE: The icon should be oriented to the north so that bearing of ic_ht_hero_marker is perfect.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Hero Marker's Icon is being provided.
     * @return ResourceId of the resource you want to set as HeroMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getHeroMarkerIconForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {

        int resourceId = R.drawable.ic_ht_hero_marker;

        return resourceId;
    }


    /**
     * Call this method to provide a Custom View to the Hero Marker.
     * <br>
     * NOTE: The icon should be oriented to the north so that bearing of ic_ht_hero_marker is perfect.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Hero Marker's Custom View is being provided.
     * @return View of the layout you want to set as HeroMarker View.
     */
    public View getHeroMarkerViewForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return null;
    }

    /**
     * Call this method to set Anchor values for the Hero Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the ic_ht_hero_marker's position on the Earth's surface.
     * <br>
     * NOTE:
     * The anchor point is specified in the continuous space [0.0, 1.0] x [0.0, 1.0],
     * where (0, 0) is the top-left corner of the image, and (1, 1) is the bottom-right corner.
     * The anchoring point in a W x H image is the nearest discrete grid point in a (W + 1) x (H + 1)
     * grid, obtained by scaling the then rounding. For example, in a 4 x 2 image, the anchor point
     * (0.7, 0.6) resolves to the grid point at (3, 1).
     * <br>
     * u-coordinate of the anchor, as a ratio of the image width (in the range [0, 1])
     * v-coordinate of the anchor, as a ratio of the image height (in the range [0, 1])
     *
     * @param hyperTrackMapFragment The fragment in which the Hero ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Hero Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as HeroMarker's Anchor.
     */
    public float[] getHeroMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return new float[]{0.5f, 0.5f};
    }

    /**
     * Call this method to Set or Unset rotation for the Hero Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero Marker is placed.
     * @param actionID              The ActionID for which the Hero Marker's rotation is being enabled/disabled.
     * @return boolean flag for the rotation of Hero Marker.
     */
    public boolean rotateHeroMarker(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).rotateHeroMarker();

        return rotateHeroMarker;
    }

    /**
     * Call this method to Show/Hide the Destination Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Destination Marker is being shown/hidden.
     * @return boolean to show/hide Destination Marker.
     */
    public boolean showDestinationMarkerForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).isDestinationMarkerVisible();

        return isDestinationMarkerVisible;
    }

    /**
     * Call this method to provide a Custom Icon to the Destination Marker.
     * <br>
     * NOTE: The Destination Marker is static and doesn't rotate in terms of bearing.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Destination Marker's Custom Icon is being provided.
     * @return ResourceId of the resource you want to set as DestinationMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getDestinationMarkerIconForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return com.hypertrack.lib.R.drawable.ic_ht_destination_marker;
    }

    /**
     * Call this method to provide a Custom View to the Destination Marker.
     * <br>
     * NOTE: The Destination Marker is static and doesn't rotate in terms of bearing.
     * Also, with a Custom View, ETA time would not be displayed on the Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Destination Marker's Custom View is being provided.
     * @return View of the layout you want to set as DestinationMarker View.
     */
    @NonNull
    public View getDestinationMarkerViewForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(com.hypertrack.lib.R.layout.ht_custom_marker_layout, null);
    }

    /**
     * Call this method to set Anchor values for the Destination Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the ic_ht_hero_marker's position on the Earth's surface.
     * <br>
     * NOTE:
     * The anchor point is specified in the continuous space [0.0, 1.0] x [0.0, 1.0],
     * where (0, 0) is the top-left corner of the image, and (1, 1) is the bottom-right corner.
     * The anchoring point in a W x H image is the nearest discrete grid point in a (W + 1) x (H + 1)
     * grid, obtained by scaling the then rounding. For example, in a 4 x 2 image, the anchor point
     * (0.7, 0.6) resolves to the grid point at (3, 1).
     * <br>
     * u-coordinate of the anchor, as a ratio of the image width (in the range [0, 1])
     * v-coordinate of the anchor, as a ratio of the image height (in the range [0, 1])
     *
     * @param hyperTrackMapFragment The fragment in which the Destination ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Destination Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as DestinationMarker's Anchor.
     */
    public float[] getDestinationMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return new float[]{0.5f, 0.915f};
    }

    /**
     * Call this method to Show Destination Marker for common layout while tracking Multiple Actions
     * <br>
     * To show/hide Multiple Actions Destination Marker for specific actions use {@link #showDestinationMarkerForActionID(HyperTrackMapFragment, String)}
     *
     * @param hyperTrackMapFragment The fragment in which the Multiple Actions Destination Marker is placed.
     * @return boolean to show/hide Multiple Actions Destination Marker.
     */
    public boolean showMultipleActionsDestinationMarker(HyperTrackMapFragment hyperTrackMapFragment) {
        return showDestinationMarkerForMultipleActionsView;
    }

    /**
     * Call this method to provide a Custom Icon for common Destination Marker while tracking Multiple Actions
     * <br>
     * <br>
     * NOTE: The Destination Marker is static and doesn't rotate in terms of bearing.
     *
     * @param hyperTrackMapFragment The fragment in which the Multiple Actions Destination ic_ht_hero_marker is placed.
     * @return ResourceId of the resource you want to set as Multiple Actions DestinationMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getMultipleActionsDestinationMarkerIcon(HyperTrackMapFragment hyperTrackMapFragment) {
        return com.hypertrack.lib.R.drawable.ic_ht_destination_marker_default;
    }

    /**
     * Call this method to provide a Custom View for common Destination Marker while tracking Multiple Actions
     * <br>
     * NOTE: The Destination Marker is static and doesn't rotate in terms of bearing.
     * Also, with a Custom View, ETA time would not be displayed on the Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Multiple Actions Destination ic_ht_hero_marker is placed.
     * @return View of the layout you want to set as DestinationMarker View.
     */
    public View getMultipleActionsDestinationMarkerView(HyperTrackMapFragment hyperTrackMapFragment) {
        return null;
    }

    /**
     * Call this method to set Anchor values for common Destination Marker while tracking Multiple Actions
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the ic_ht_hero_marker's position on the Earth's surface.
     * <br>
     * NOTE:
     * The anchor point is specified in the continuous space [0.0, 1.0] x [0.0, 1.0],
     * where (0, 0) is the top-left corner of the image, and (1, 1) is the bottom-right corner.
     * The anchoring point in a W x H image is the nearest discrete grid point in a (W + 1) x (H + 1)
     * grid, obtained by scaling the then rounding. For example, in a 4 x 2 image, the anchor point
     * (0.7, 0.6) resolves to the grid point at (3, 1).
     * <br>
     * u-coordinate of the anchor, as a ratio of the image width (in the range [0, 1])
     * v-coordinate of the anchor, as a ratio of the image height (in the range [0, 1])
     *
     * @param hyperTrackMapFragment The fragment in which the Multiple Actions Destination ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Multiple Actions Destination Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as Multiple Actions DestinationMarker's Anchor.
     */
    public float[] getMultipleActionsDestinationMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return new float[]{0.5f, 0.915f};
    }

    /**
     * Call this method to Show/Hide the Source Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Source ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Source Marker is being shown/hidden.
     * @return boolean to show/hide Source Marker.
     */
    public boolean showSourceMarkerForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).isSourceMarkerVisible();

        return isSourceMarkerVisible;
    }

    /**
     * Call this method to provide a Custom Icon to the Source Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Source ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Source Marker's Icon is being provided.
     * @return ResourceId of the resource you want to set as SourceMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getSourceMarkerIconForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return com.hypertrack.lib.R.drawable.ic_ht_source_marker;
    }

    /**
     * Call this method to provide a Custom View to the Source Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Source ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Source Marker's Custom View is being provided.
     * @return View of the layout you want to set as SourceMarker View.
     */
    public View getSourceMarkerViewForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return null;
    }

    /**
     * Call this method to set Anchor values for the Source Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the ic_ht_hero_marker's position on the Earth's surface.
     * <br>
     * NOTE:
     * The anchor point is specified in the continuous space [0.0, 1.0] x [0.0, 1.0],
     * where (0, 0) is the top-left corner of the image, and (1, 1) is the bottom-right corner.
     * The anchoring point in a W x H image is the nearest discrete grid point in a (W + 1) x (H + 1)
     * grid, obtained by scaling the then rounding. For example, in a 4 x 2 image, the anchor point
     * (0.7, 0.6) resolves to the grid point at (3, 1).
     * <br>
     * u-coordinate of the anchor, as a ratio of the image width (in the range [0, 1])
     * v-coordinate of the anchor, as a ratio of the image height (in the range [0, 1])
     *
     * @param hyperTrackMapFragment The fragment in which the Source ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the Source Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as SourceMarker's Anchor.
     */
    public float[] getSourceMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return new float[]{0.5f, 1.0f};
    }

    /**
     * Call this method to provide a Custom Icon to the ActionSummaryStart Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the ActionSummaryStart Marker is placed.
     * @param actionID              The ActionID for which the ActionSummaryStart Marker's Icon is being provided.
     * @return ResourceId of the resource you want to set as ActionSummaryStartMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getActionSummaryStartMarkerIconForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return com.hypertrack.lib.R.drawable.ic_action_summary_start_marker;
    }

    /**
     * Call this method to set Anchor values for the ActionSummaryStart Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the ic_ht_hero_marker's position on the Earth's surface.
     * <br>
     * NOTE:
     * The anchor point is specified in the continuous space [0.0, 1.0] x [0.0, 1.0],
     * where (0, 0) is the top-left corner of the image, and (1, 1) is the bottom-right corner.
     * The anchoring point in a W x H image is the nearest discrete grid point in a (W + 1) x (H + 1)
     * grid, obtained by scaling the then rounding. For example, in a 4 x 2 image, the anchor point
     * (0.7, 0.6) resolves to the grid point at (3, 1).
     * <br>
     * u-coordinate of the anchor, as a ratio of the image width (in the range [0, 1])
     * v-coordinate of the anchor, as a ratio of the image height (in the range [0, 1])
     *
     * @param hyperTrackMapFragment The fragment in which the ActionSummaryStart ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the ActionSummaryStart Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as ActionSummaryStartMarker's Anchor.
     */
    public float[] getActionSummaryStartMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return new float[]{0.5f, 0.5f};
    }

    /**
     * Call this method to provide a Custom Icon to the ActionSummaryEnd Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the ActionSummaryEnd Marker is placed.
     * @param actionID              The ActionID for which the ActionSummaryEnd Marker's Icon is being provided.
     * @return ResourceId of the resource you want to set as ActionSummaryEndMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getActionSummaryEndMarkerIconForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return com.hypertrack.lib.R.drawable.ic_action_summary_end_marker;
    }

    /**
     * Call this method to set Anchor values for the ActionSummaryEnd Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the ic_ht_hero_marker's position on the Earth's surface.
     * <br>
     * NOTE:
     * The anchor point is specified in the continuous space [0.0, 1.0] x [0.0, 1.0],
     * where (0, 0) is the top-left corner of the image, and (1, 1) is the bottom-right corner.
     * The anchoring point in a W x H image is the nearest discrete grid point in a (W + 1) x (H + 1)
     * grid, obtained by scaling the then rounding. For example, in a 4 x 2 image, the anchor point
     * (0.7, 0.6) resolves to the grid point at (3, 1).
     * <br>
     * u-coordinate of the anchor, as a ratio of the image width (in the range [0, 1])
     * v-coordinate of the anchor, as a ratio of the image height (in the range [0, 1])
     *
     * @param hyperTrackMapFragment The fragment in which the ActionSummaryEnd ic_ht_hero_marker is placed.
     * @param actionID              The ActionID for which the ActionSummaryEnd Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as ActionSummaryEndMarker's Anchor.
     */
    public float[] getActionSummaryEndMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        return new float[]{0.5f, 0.5f};
    }

    /**
     * Call this method to Show/Hide the Action Summary View (View having the Action Summary Info and Polyline).
     *
     * @param hyperTrackMapFragment The fragment in which the Action Summary View is placed.
     * @param actionID              The ActionID for which the Action Summary View is being shown/hidden.
     * @return boolean to show/hide Action Summary View.
     */
    public boolean showActionSummaryForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).isActionSummaryInfoVisible();

        return isActionSummaryInfoVisible;
    }

    /**
     * Call this method to Show the Address Info View (View having the Source and Destination Address Info).
     *
     * @param hyperTrackMapFragment The fragment in which the Address Info View is placed.
     * @param actionID              The ActionID for which the Address Info View is being shown/hidden.
     * @return boolean to show/hide Address Info view.
     */
    public boolean showAddressInfoViewForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).isAddressInfoVisible();

        return isAddressInfoVisible;
    }

    /**
     * Call this method to Show Address Info View (View having the Source and Destination Address Info)
     * for common layout while tracking Multiple Actions
     * <br>
     * To show/hide Address Info layout for specific actions use {@link #showAddressInfoViewForActionID(HyperTrackMapFragment, String)}
     *
     * @param hyperTrackMapFragment The fragment in which the Address Info View is placed.
     * @return Boolean to show/hide Address Info view for all actions, null if
     * {@link #showAddressInfoViewForActionID(HyperTrackMapFragment, String)} has been overridden.
     */
    public boolean showAddressInfoViewForMultipleActionsView(HyperTrackMapFragment hyperTrackMapFragment) {
        return showAddressInfoLayoutForMultipleActionsView;
    }

    /**
     * Call this method to Show the User Info View (View having the User Photo, Name and Call button).
     *
     * @param hyperTrackMapFragment The fragment in which the User Info View is placed.
     * @param actionID              The ActionID for which the User Info View is being shown/hidden.
     * @return boolean to show/hide User Info view.
     */
    public boolean showUserInfoForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).isUserInfoVisible();

        return isUserInfoVisible;
    }

    /**
     * Call this method to Show the User Info View (View having the User Photo, Name and Call button)
     * for for common layout while tracking Multiple Actions
     * <br>
     * To show/hide User Info layout for specific actions use {@link #showUserInfoForActionID(HyperTrackMapFragment, String)}
     *
     * @param hyperTrackMapFragment The fragment in which the User Info View is placed.
     * @return Boolean to show/hide User Info view for all actions, null if
     * {@link #showUserInfoForActionID(HyperTrackMapFragment, String)} has been overridden.
     */
    public boolean showUserInfoForMultipleActionsView(HyperTrackMapFragment hyperTrackMapFragment) {
        return showUserInfoLayoutForMultipleActionsView;
    }

    /**
     * Call this method to Show the Call to User Button.
     *
     * @param hyperTrackMapFragment The fragment in which the Call Button is placed.
     * @param actionID              The ActionID for which the Call Button is being shown/hidden.
     * @return boolean to show/hide Call Button.
     */
    public boolean showCallButtonForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).isCallButtonVisible();

        return isCallButtonVisible;
    }

    /**
     * Call this method to Show the Order Details Button.
     *
     * @param hyperTrackMapFragment The fragment in which the Order Details Button is placed.
     * @param actionID              The ActionID for which the Order Details Button is being shown/hidden.
     * @return boolean to show/hide Order Details Button.
     */
    public boolean showOrderDetailsButtonForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).isOrderDetailsButtonVisible();

        return isOrderDetailsButtonVisible;
    }

    /**
     * Call this method to Show/Hide OrderStatus Toolbar displayed on HyperTrackMapFragment
     * <br>
     * OrderStatus Toolbar is to reflect OrderStatus of the current order being tracked.
     * <br>
     * For SINGLE ACTIVE ORDER being tracked, OrderStatus will be displayed on the Toolbar.
     * For MULTIPLE ACTIVE ORDERS being tracked, OrderStatus of a given actionID will be displayed
     * when markers corresponding to that actionID are clicked.
     * <br>
     * IMPORTANT: Inorder to change the OrderStatus Toolbar theme to support your App Theme,
     * add a style resource containing desired Toolbar Style Settings in your styles.xml
     * with the name "HTOrderStatusToolbarTheme". This theme in your styles.xml file will override
     * OrderStatus Toolbar's default style settings.
     *
     * @param hyperTrackMapFragment The fragment in which the Traffic Layer is visible.
     * @return boolean to show/hide OrderStatus Toolbar on HyperTrackMapFragment.
     */
    public boolean showOrderStatusToolbar(HyperTrackMapFragment hyperTrackMapFragment) {
        if (mapViewModelList != null)
            return mapViewModelList.isOrderStatusBarEnabled();

        return isOrderStatusBarEnabled;
    }

    /**
     * Call this method to set Default Title for OrderStatus Toolbar
     * <br>
     * This Default Title will be displayed in the cases where either No Active Order or
     * multiple Active orders are being tracked on HyperTrackMapFragment.
     * <br>
     * In case of multiple orders being tracked, OrderStatus Toolbar will display Order Status on
     * clicking markers corresponding to that actionID
     * <br>
     * IMPORTANT: Inorder to change the OrderStatus Toolbar theme to support your App Theme,
     * add a style resource containing desired Toolbar Style Settings in your styles.xml
     * with the name "HTOrderStatusToolbarTheme". This theme in your styles.xml file will override
     * OrderStatus Toolbar's default style settings.
     *
     * @param hyperTrackMapFragment The fragment in which the Order Status Toolbar is visible.
     * @return Default Title Text for OrderStatus Toolbar on HyperTrackMapFragment.
     */
    public String getOrderStatusToolbarDefaultTitle(HyperTrackMapFragment hyperTrackMapFragment) {
        return "Track";
    }

    /**
     * Call this method to set Title for OrderStatus Toolbar in case of Multiple Orders to same
     * destination are being tracked
     * <br>
     * This text will be displayed in the cases where multiple actions for same destination are
     * being tracked on HyperTrackMapFragment. On clicking markers corresponding to a actionID, OrderStatus
     * Toolbar will display Order Status for this action.
     * <br>
     * IMPORTANT: Inorder to change the OrderStatus Toolbar theme to support your App Theme,
     * add a style resource containing desired Toolbar Style Settings in your styles.xml
     * with the name "HTOrderStatusToolbarTheme". This theme in your styles.xml file will override
     * OrderStatus Toolbar's default style settings.
     *
     * @param hyperTrackMapFragment The fragment in which the Order Status Toolbar is visible.
     * @return Text for OrderStatus Toolbar on HyperTrackMapFragment in case of Multiple Actions being tracked.
     */
    public String getMultipleActionsOrderStatusToolbarTitle(HyperTrackMapFragment hyperTrackMapFragment) {
        return null;
    }

    /**
     * Call this method to disable Edit Destination feature.
     *
     * @param hyperTrackMapFragment The fragment in which the Edit Destination Layout is visible.
     * @param actionID              The ActionID for which the Edit Destination Layout is being shown/hidden.
     * @return Bool to disable dynamic zoom
     */
    public boolean disableEditDestinationForActionID(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).disableEditDestination();

        return disableEditDestination;
    }

    /**
     * Call this method to enable/disable showing location missing alert while Editing Destination.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination address is being edited.
     * @param actionID              The List of ActionIDs for which the Destination address is being edited.
     * @return boolean to show location missing alert
     */
    boolean shouldShowUserLocationMissingAlert(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).shouldShowUserLocationMissingAlert();

        return showUserLocationMissingAlert;
    }

    /**
     * Returns a boolean to show/hide edit destination failure alert while Editing Destination.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination address is being edited.
     * @param actionID              The List of ActionIDs for which the Destination address is being edited.
     * @return boolean to show edit destination failure alert
     */
    boolean shouldShowEditDestinationFailureAlert(HyperTrackMapFragment hyperTrackMapFragment, String actionID) {
        if (mapViewModelList != null && mapViewModelList.getHTMapViewModelForActionID(actionID) != null)
            return mapViewModelList.getHTMapViewModelForActionID(actionID).shouldShowEditDestinationFailureAlert();

        return showEditDestinationFailureAlert;
    }

    /**
     * Call this method to Show the Traffic Layer on hyperTrackMapFragment.
     *
     * @param hyperTrackMapFragment The fragment in which the Traffic Layer is visible.
     * @return boolean to show/hide Traffic Layer on map.
     */
    public boolean showTrafficLayer(HyperTrackMapFragment hyperTrackMapFragment) {
        if (mapViewModelList != null)
            return mapViewModelList.isTrafficEnabled();

        return isTrafficEnabled;
    }

    /**
     * Call this method to Disable the Dynamic Zoom on Map.
     *
     * @param hyperTrackMapFragment The fragment in which the the Markers are visible.
     * @return boolean to enable/disable Dynamic Zoom on map.
     */
    public boolean disableDynamicZoom(HyperTrackMapFragment hyperTrackMapFragment) {
        if (mapViewModelList != null)
            return mapViewModelList.isDynamicZoomDisabled();

        return isDynamicZoomDisabled;
    }

    /**
     * Call this method to reload all the UI elements that are being/should be displayed on the map,
     * including Markers, User info view and Address Info View. The Map View's callback or
     * Adapter calls this method when the Map View has to completely reload its UI and its elements.
     */
    public final void notifyDataSetChanged() {
        mapUpdateInterface.notifyChanged();
    }

    // TODO: 12/09/16 Check what to do in case destination location gets updated
    protected final List<String> actionIDsToTrack(final HyperTrackMapFragment hyperTrackMapFragment) {
        // Fetch the actionIDs to be tracked by the user
        List<String> actionIDList = getActionIDsToTrack(hyperTrackMapFragment);

        // If there are no actionIDs to be tracked, clear ActionIDsBeingTracked List and return an empty list
        if (actionIDList == null || actionIDList.isEmpty()) {
            actionIDsBeingTracked.clear();
            return new ArrayList<>();
        }

        // Setup ActionIDs in case not already done
        if (actionIDsBeingTracked == null || actionIDsBeingTracked.isEmpty() || !actionIDsBeingTracked.containsAll(actionIDList)) {
            setupActionIDsToTrack(hyperTrackMapFragment, actionIDList);
        } else {
            // Reset actionIDsBeingTracked to actionIDList to remove any extra actionIDs
            actionIDsBeingTracked.clear();
            actionIDsBeingTracked.addAll(actionIDList);
        }

        return actionIDsBeingTracked;
    }

    protected final boolean setupActionIDsToTrack(final HyperTrackMapFragment hyperTrackMapFragment, final List<String> actionIDList) {
//        final List<HTAction> actionsToTrack = new ArrayList<>();
        final List<String> actionIDsToTrack = new ArrayList<>();
        final List<String> actionIDsToAddForTracking = new ArrayList<>();

        if (actionIDList != null && !actionIDList.isEmpty()) {

            // Check if User provided actionIDs are being tracked or not
            for (String actionID : actionIDList) {

                // Check if this actionID is being tracked by HTConsumerClient
                HTAction action = consumerClient.actionForActionID(actionID);
                if (action != null) {
                    actionIDsToTrack.add(actionID);
//                    actionsToTrack.add(action);

                } else if (!actionIDsAddedForTracking.contains(actionID)) {
                    //Check if this actionID has already been added for tracking
                    actionIDsToAddForTracking.add(actionID);
                }
            }

            if (actionIDsToAddForTracking.size() > 0) {
                // Call trackAction for actionIDs which are not currently being tracked
                addActionIDsForTracking(hyperTrackMapFragment, actionIDsToAddForTracking);
            }

            if (actionIDsSetupForTrackingFailed != null && actionIDsSetupForTrackingFailed.containsAll(actionIDsToTrack)) {
                return false;
            }
        }

        // TODO: 13/09/16 Add Error in case of actions with different destinations
        // Check if all the actions being currently tracked belong to the same destination location
//        if (HTConsumerClient.checkIfActionsHaveSameDestination(actionsToTrack)) {

        // Post a success callback on Start tracking actions
        if (mapFragmentCallback != null)
            mapFragmentCallback.onMapFragmentSucceed(hyperTrackMapFragment, actionIDsToTrack);

        actionIDsBeingTracked = actionIDsToTrack;
        return true;
//        }

        // Post an error callback on actions belonging to different locations
//        if (mapFragmentCallback != null) {
//            mapFragmentCallback.onMapFragmentFailed(hyperTrackMapFragment, actionIDsToTrack,
//                    ActionsDestinationLocationDifferentMessage);
//        }

//        actionIDsSetupForTrackingFailed.clear();
//        actionIDsSetupForTrackingFailed.addAll(actionIDsToTrack);

//        return false;
    }

    protected final void addActionIDsForTracking(final HyperTrackMapFragment hyperTrackMapFragment, final List<String> actionIDsToAddForTracking) {
        consumerClient.trackAction(actionIDsToAddForTracking, (Activity) mContext, new ActionListCallBack() {
            @Override
            public void onSuccess(List<HTAction> actionList) {

                // Setup all the actionIDsBeingTracked and notify in case of success
                if (setupActionIDsToTrack(hyperTrackMapFragment, getActionIDsToTrack(hyperTrackMapFragment))) {
                    notifyDataSetChanged();

                    // Remove successfully added actionIDs from actionIDsAddedForTracking
                    if (actionList != null) {
                        for (HTAction action : actionList) {
                            if (action != null && !TextUtils.isEmpty(action.getId()))
                                actionIDsAddedForTracking.remove(action.getId());
                        }
                    }
                }
            }

            @Override
            public void onError(Exception exception) {
                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onMapFragmentFailed(hyperTrackMapFragment, actionIDsToAddForTracking,
                            exception.getMessage());
                }

                actionIDsAddedForTracking.removeAll(actionIDsToAddForTracking);
            }
        });
        actionIDsAddedForTracking.addAll(actionIDsToAddForTracking);
    }

    protected final int getActionIDsToTrackCount(HyperTrackMapFragment hyperTrackMapFragment) {
        return actionIDsToTrack(hyperTrackMapFragment).size();
    }

    protected final boolean shouldBindView() {
        if (mapViewModelList != null)
            return mapViewModelList.shouldBindView();

        return shouldBindView;
    }

    protected final void setConsumerClient(@NonNull HTConsumerClient consumerClient) {
        this.consumerClient = consumerClient;
    }

    protected final void setMapUpdateInterface(@NonNull MapUpdateInterface mapUpdateInterface) {
        this.mapUpdateInterface = mapUpdateInterface;
    }

    protected final void setHTMapViewModelList(@NonNull HTMapViewModelList mapViewModelList) {
        this.mapViewModelList = mapViewModelList;
    }

    protected final void setMapFragmentCallback(MapFragmentCallback mapFragmentCallback) {
        this.mapFragmentCallback = mapFragmentCallback;
    }
}
