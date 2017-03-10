package com.hypertrack.lib.internal.consumer.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.hypertrack.lib.internal.common.models.HTUserVehicleType;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.ConsumerClient;
import com.hypertrack.lib.internal.consumer.models.HTTask;
import com.hypertrack.lib.internal.consumer.models.TaskListCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can be overridden to customise HyperTrackMapFragment interface.
 * <br>
 * An instance of MapAdapter needs to be passed to the method
 * {@link HyperTrackMapFragment#setHTMapAdapter(MapAdapter)} as parameter.
 * <br>
 * The primary API you can override is {@link #getTaskIDsToTrack(HyperTrackMapFragment)} where you provide
 * a list of TaskIDs to be tracked on {@link HyperTrackMapFragment}.
 * <br>
 * Other APIs in MapAdapter can be used to customise UI elements in HyperTrackMapFragment.
 */
public class MapAdapter {

    private static final String TAG = MapAdapter.class.getSimpleName();

    private static final String TasksDestinationLocationDifferentMessage = "Tasks to be tracked have different destinations." +
            " Please initiate multiple task tracking for tasks with same destination.";
    private final boolean showDestinationMarkerForMultipleTasksView = true;
    private final boolean showAddressInfoLayoutForMultipleTasksView = true;
    private final boolean showUserInfoLayoutForMultipleTasksView = true;
    private final boolean rotateHeroMarker = true;
    private final boolean isDestinationMarkerVisible = true;
    private final boolean isTaskSummaryInfoVisible = true;
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
    private ConsumerClient consumerClient;
    private MapViewModelList mapViewModelList;
    private MapFragmentCallback mapFragmentCallback;
    private List<String> taskIDsBeingTracked;
    private List<String> taskIDsSetupForTrackingFailed;
    private List<String> taskIDsAddedForTracking;

    public MapAdapter(Context mContext) {
        this.mContext = mContext;
        this.taskIDsAddedForTracking = new ArrayList<>();
        this.taskIDsBeingTracked = new ArrayList<>();
        this.taskIDsSetupForTrackingFailed = new ArrayList<>();
    }

    /**
     * Call this method to provide a List of Active Tasks that have to be tracked on the Map View.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero marker is placed.
     * @return List of TaskIDs to be tracked.
     */
    public List<String> getTaskIDsToTrack(HyperTrackMapFragment hyperTrackMapFragment) {
        return consumerClient.getTaskIDList() != null ? consumerClient.getTaskIDList() : new ArrayList<String>();
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
     * Call this method to display Google Map's MyLocation marker on the MapView.
     * <br>
     * @param hyperTrackMapFragment The fragment in which the Map view is placed.
     * @return boolean flag to enable/disable GoogleMap's MyLocation marker in the map view.
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
     * @param hyperTrackMapFragment The fragment in which the Map view is placed.
     * @return boolean flag to enable/disable GoogleMap's MyLocationButton in the map view.
     */
    public boolean setMyLocationButtonEnabled(HyperTrackMapFragment hyperTrackMapFragment) {
        return false;
    }

    /**
     * Call this method to provide a Custom Icon to the hero marker.
     * <br>
     * NOTE: The icon should be oriented to the north so that bearing of marker is perfect.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero marker is placed.
     * @param taskID      The TaskID for which the Hero Marker's Icon is being provided.
     * @return ResourceId of the resource you want to set as HeroMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getHeroMarkerIconForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {

        HTUserVehicleType vehicleType = consumerClient.getVehicleTypeString(taskID);
        int resourceId = com.hypertrack.lib.R.drawable.ic_ht_hero_marker_bike;

        if (HTUserVehicleType.CAR.equals(vehicleType)) {
            resourceId = com.hypertrack.lib.R.drawable.ic_ht_hero_marker_car;
        } else if (HTUserVehicleType.VAN.equals(vehicleType)) {
            resourceId = com.hypertrack.lib.R.drawable.ic_ht_hero_marker_van;
        } else if (HTUserVehicleType.WALK.equals(vehicleType)) {
            resourceId = com.hypertrack.lib.R.drawable.ic_ht_hero_marker_walk;
        }

        return resourceId;
    }

    /**
     * Call this method to provide a Custom View to the Hero Marker.
     * <br>
     * NOTE: The icon should be oriented to the north so that bearing of marker is perfect.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero marker is placed.
     * @param taskID      The TaskID for which the Hero Marker's Custom View is being provided.
     * @return View of the layout you want to set as HeroMarker View.
     */
    public View getHeroMarkerViewForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return null;
    }

    /**
     * Call this method to set Anchor values for the Hero Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the marker's position on the Earth's surface.
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
     * @param hyperTrackMapFragment The fragment in which the Hero marker is placed.
     * @param taskID      The TaskID for which the Hero Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as HeroMarker's Anchor.
     */
    public float[] getHeroMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return new float[]{0.5f, 0.5f};
    }

    /**
     * Call this method to Set or Unset rotation for the Hero Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero Marker is placed.
     * @param taskID      The TaskID for which the Hero Marker's rotation is being enabled/disabled.
     * @return boolean flag for the rotation of Hero Marker.
     */
    public boolean rotateHeroMarker(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).rotateHeroMarker();

        return rotateHeroMarker;
    }

    /**
     * Call this method to Show/Hide the Destination Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination marker is placed.
     * @param taskID      The TaskID for which the Destination Marker is being shown/hidden.
     * @return boolean to show/hide Destination Marker.
     */
    public boolean showDestinationMarkerForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).isDestinationMarkerVisible();

        return isDestinationMarkerVisible;
    }

    /**
     * Call this method to provide a Custom Icon to the Destination Marker.
     * <br>
     * NOTE: The Destination Marker is static and doesn't rotate in terms of bearing.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination marker is placed.
     * @param taskID      The TaskID for which the Destination Marker's Custom Icon is being provided.
     * @return ResourceId of the resource you want to set as DestinationMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getDestinationMarkerIconForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return com.hypertrack.lib.R.drawable.ic_ht_destination_marker;
    }

    /**
     * Call this method to provide a Custom View to the Destination Marker.
     * <br>
     * NOTE: The Destination Marker is static and doesn't rotate in terms of bearing.
     * Also, with a Custom View, ETA time would not be displayed on the Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination marker is placed.
     * @param taskID      The TaskID for which the Destination Marker's Custom View is being provided.
     * @return View of the layout you want to set as DestinationMarker View.
     */
    @NonNull
    public View getDestinationMarkerViewForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(com.hypertrack.lib.R.layout.ht_custom_marker_layout, null);
    }

    /**
     * Call this method to set Anchor values for the Destination Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the marker's position on the Earth's surface.
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
     * @param hyperTrackMapFragment The fragment in which the Destination marker is placed.
     * @param taskID      The TaskID for which the Destination Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as DestinationMarker's Anchor.
     */
    public float[] getDestinationMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return new float[]{0.5f, 0.915f};
    }

    /**
     * Call this method to Show Destination Marker for common layout while tracking Multiple Tasks
     * <br>
     * To show/hide Multiple Tasks Destination Marker for specific tasks use {@link #showDestinationMarkerForTaskID(HyperTrackMapFragment, String)}
     *
     * @param hyperTrackMapFragment The fragment in which the Multiple Tasks Destination Marker is placed.
     * @return boolean to show/hide Multiple Tasks Destination Marker.
     */
    public boolean showMultipleTasksDestinationMarker(HyperTrackMapFragment hyperTrackMapFragment) {
        return showDestinationMarkerForMultipleTasksView;
    }

    /**
     * Call this method to provide a Custom Icon for common Destination Marker while tracking Multiple Tasks
     * <br>
     * <br>
     * NOTE: The Destination Marker is static and doesn't rotate in terms of bearing.
     *
     * @param hyperTrackMapFragment The fragment in which the Multiple Tasks Destination marker is placed.
     * @return ResourceId of the resource you want to set as Multiple Tasks DestinationMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getMultipleTasksDestinationMarkerIcon(HyperTrackMapFragment hyperTrackMapFragment) {
        return com.hypertrack.lib.R.drawable.ic_ht_destination_marker_default;
    }

    /**
     * Call this method to provide a Custom View for common Destination Marker while tracking Multiple Tasks
     * <br>
     * NOTE: The Destination Marker is static and doesn't rotate in terms of bearing.
     * Also, with a Custom View, ETA time would not be displayed on the Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Multiple Tasks Destination marker is placed.
     * @return View of the layout you want to set as DestinationMarker View.
     */
    public View getMultipleTasksDestinationMarkerView(HyperTrackMapFragment hyperTrackMapFragment) {
        return null;
    }

    /**
     * Call this method to set Anchor values for common Destination Marker while tracking Multiple Tasks
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the marker's position on the Earth's surface.
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
     * @param hyperTrackMapFragment The fragment in which the Multiple Tasks Destination marker is placed.
     * @param taskID      The TaskID for which the Multiple Tasks Destination Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as Multiple Tasks DestinationMarker's Anchor.
     */
    public float[] getMultipleTasksDestinationMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return new float[]{0.5f, 0.915f};
    }

    /**
     * Call this method to Show/Hide the Source Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Source marker is placed.
     * @param taskID      The TaskID for which the Source Marker is being shown/hidden.
     * @return boolean to show/hide Source Marker.
     */
    public boolean showSourceMarkerForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).isSourceMarkerVisible();

        return isSourceMarkerVisible;
    }

    /**
     * Call this method to provide a Custom Icon to the Source Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Source marker is placed.
     * @param taskID      The TaskID for which the Source Marker's Icon is being provided.
     * @return ResourceId of the resource you want to set as SourceMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getSourceMarkerIconForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return com.hypertrack.lib.R.drawable.ic_ht_source_marker;
    }

    /**
     * Call this method to provide a Custom View to the Source Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the Source marker is placed.
     * @param taskID      The TaskID for which the Source Marker's Custom View is being provided.
     * @return View of the layout you want to set as SourceMarker View.
     */
    public View getSourceMarkerViewForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return null;
    }

    /**
     * Call this method to set Anchor values for the Source Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the marker's position on the Earth's surface.
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
     * @param hyperTrackMapFragment The fragment in which the Source marker is placed.
     * @param taskID      The TaskID for which the Source Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as SourceMarker's Anchor.
     */
    public float[] getSourceMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return new float[]{0.5f, 1.0f};
    }

    /**
     * Call this method to provide a Custom Icon to the TaskSummaryStart Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the TaskSummaryStart Marker is placed.
     * @param taskID      The TaskID for which the TaskSummaryStart Marker's Icon is being provided.
     * @return ResourceId of the resource you want to set as TaskSummaryStartMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getTaskSummaryStartMarkerIconForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return com.hypertrack.lib.R.drawable.ic_task_summary_start_marker;
    }

    /**
     * Call this method to set Anchor values for the TaskSummaryStart Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the marker's position on the Earth's surface.
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
     * @param hyperTrackMapFragment The fragment in which the TaskSummaryStart marker is placed.
     * @param taskID      The TaskID for which the TaskSummaryStart Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as TaskSummaryStartMarker's Anchor.
     */
    public float[] getTaskSummaryStartMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return new float[]{0.5f, 0.5f};
    }

    /**
     * Call this method to provide a Custom Icon to the TaskSummaryEnd Marker.
     *
     * @param hyperTrackMapFragment The fragment in which the TaskSummaryEnd Marker is placed.
     * @param taskID      The TaskID for which the TaskSummaryEnd Marker's Icon is being provided.
     * @return ResourceId of the resource you want to set as TaskSummaryEndMarker Icon.
     * It is suggested to use R.drawable.* to preserve cross functionality and avoid conflicts.
     */
    public int getTaskSummaryEndMarkerIconForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return com.hypertrack.lib.R.drawable.ic_task_summary_end_marker;
    }

    /**
     * Call this method to set Anchor values for the TaskSummaryEnd Marker.
     * <br>
     * The anchor specifies the point in the icon image that is anchored to the marker's position on the Earth's surface.
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
     * @param hyperTrackMapFragment The fragment in which the TaskSummaryEnd marker is placed.
     * @param taskID      The TaskID for which the TaskSummaryEnd Marker's Anchor is being set.
     * @return Array of float values of size 2 which you want to set as TaskSummaryEndMarker's Anchor.
     */
    public float[] getTaskSummaryEndMarkerAnchorValues(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        return new float[]{0.5f, 0.5f};
    }

    /**
     * Call this method to Show/Hide the Task Summary View (View having the Task Summary Info and Polyline).
     *
     * @param hyperTrackMapFragment The fragment in which the Task Summary View is placed.
     * @param taskID      The TaskID for which the Task Summary View is being shown/hidden.
     * @return boolean to show/hide Task Summary View.
     */
    public boolean showTaskSummaryForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).isTaskSummaryInfoVisible();

        return isTaskSummaryInfoVisible;
    }

    /**
     * Call this method to Show the Address Info View (View having the Source and Destination Address Info).
     *
     * @param hyperTrackMapFragment The fragment in which the Address Info View is placed.
     * @param taskID      The TaskID for which the Address Info View is being shown/hidden.
     * @return boolean to show/hide Address Info view.
     */
    public boolean showAddressInfoViewForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).isAddressInfoVisible();

        return isAddressInfoVisible;
    }

    /**
     * Call this method to Show Address Info View (View having the Source and Destination Address Info)
     * for common layout while tracking Multiple Tasks
     * <br>
     * To show/hide Address Info layout for specific tasks use {@link #showAddressInfoViewForTaskID(HyperTrackMapFragment, String)}
     *
     * @param hyperTrackMapFragment The fragment in which the Address Info View is placed.
     * @return Boolean to show/hide Address Info view for all tasks, null if
     * {@link #showAddressInfoViewForTaskID(HyperTrackMapFragment, String)} has been overridden.
     */
    public boolean showAddressInfoViewForMultipleTasksView(HyperTrackMapFragment hyperTrackMapFragment) {
        return showAddressInfoLayoutForMultipleTasksView;
    }

    /**
     * Call this method to Show the User Info View (View having the User Photo, Name and Call button).
     *
     * @param hyperTrackMapFragment The fragment in which the User Info View is placed.
     * @param taskID      The TaskID for which the User Info View is being shown/hidden.
     * @return boolean to show/hide User Info view.
     */
    public boolean showUserInfoForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).isUserInfoVisible();

        return isUserInfoVisible;
    }

    /**
     * Call this method to Show the User Info View (View having the User Photo, Name and Call button)
     * for for common layout while tracking Multiple Tasks
     * <br>
     * To show/hide User Info layout for specific tasks use {@link #showUserInfoForTaskID(HyperTrackMapFragment, String)}
     *
     * @param hyperTrackMapFragment The fragment in which the User Info View is placed.
     * @return Boolean to show/hide User Info view for all tasks, null if
     * {@link #showUserInfoForTaskID(HyperTrackMapFragment, String)} has been overridden.
     */
    public boolean showUserInfoForMultipleTasksView(HyperTrackMapFragment hyperTrackMapFragment) {
        return showUserInfoLayoutForMultipleTasksView;
    }

    /**
     * Call this method to Show the Call to User Button.
     *
     * @param hyperTrackMapFragment The fragment in which the Call Button is placed.
     * @param taskID      The TaskID for which the Call Button is being shown/hidden.
     * @return boolean to show/hide Call Button.
     */
    public boolean showCallButtonForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).isCallButtonVisible();

        return isCallButtonVisible;
    }

    /**
     * Call this method to Show the Order Details Button.
     *
     * @param hyperTrackMapFragment The fragment in which the Order Details Button is placed.
     * @param taskID      The TaskID for which the Order Details Button is being shown/hidden.
     * @return boolean to show/hide Order Details Button.
     */
    public boolean showOrderDetailsButtonForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).isOrderDetailsButtonVisible();

        return isOrderDetailsButtonVisible;
    }

    /**
     * Call this method to Show/Hide OrderStatus Toolbar displayed on HyperTrackMapFragment
     * <br>
     * OrderStatus Toolbar is to reflect OrderStatus of the current order being tracked.
     * <br>
     * For SINGLE ACTIVE ORDER being tracked, OrderStatus will be displayed on the Toolbar.
     * For MULTIPLE ACTIVE ORDERS being tracked, OrderStatus of a given taskID will be displayed
     * when markers corresponding to that taskID are clicked.
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
     * clicking markers corresponding to that taskID
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
     * This text will be displayed in the cases where multiple tasks for same destination are
     * being tracked on HyperTrackMapFragment. On clicking markers corresponding to a taskID, OrderStatus
     * Toolbar will display Order Status for this task.
     * <br>
     * IMPORTANT: Inorder to change the OrderStatus Toolbar theme to support your App Theme,
     * add a style resource containing desired Toolbar Style Settings in your styles.xml
     * with the name "HTOrderStatusToolbarTheme". This theme in your styles.xml file will override
     * OrderStatus Toolbar's default style settings.
     *
     * @param hyperTrackMapFragment The fragment in which the Order Status Toolbar is visible.
     * @return Text for OrderStatus Toolbar on HyperTrackMapFragment in case of Multiple Tasks being tracked.
     */
    public String getMultipleTasksOrderStatusToolbarTitle(HyperTrackMapFragment hyperTrackMapFragment) {
        return null;
    }

    /**
     * Call this method to disable Edit Destination feature.
     *
     * @param hyperTrackMapFragment The fragment in which the Edit Destination Layout is visible.
     * @param taskID      The TaskID for which the Edit Destination Layout is being shown/hidden.
     * @return Bool to disable dynamic zoom
     */
    public boolean disableEditDestinationForTaskID(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).disableEditDestination();

        return disableEditDestination;
    }

    /**
     * Call this method to enable/disable showing location missing alert while Editing Destination.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination address is being edited.
     * @param taskID      The List of TaskIDs for which the Destination address is being edited.
     * @return boolean to show location missing alert
     */
    boolean shouldShowUserLocationMissingAlert(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).shouldShowUserLocationMissingAlert();

        return showUserLocationMissingAlert;
    }

    /**
     * Returns a boolean to show/hide edit destination failure alert while Editing Destination.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination address is being edited.
     * @param taskID      The List of TaskIDs for which the Destination address is being edited.
     * @return boolean to show edit destination failure alert
     */
    boolean shouldShowEditDestinationFailureAlert(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
        if (mapViewModelList != null && mapViewModelList.getMapViewModelForTaskID(taskID) != null)
            return mapViewModelList.getMapViewModelForTaskID(taskID).shouldShowEditDestinationFailureAlert();

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
    protected final List<String> taskIDsToTrack(final HyperTrackMapFragment hyperTrackMapFragment) {
        // Fetch the taskIDs to be tracked by the user
        List<String> taskIDList = getTaskIDsToTrack(hyperTrackMapFragment);

        // If there are no taskIDs to be tracked, clear TaskIDsBeingTracked List and return an empty list
        if (taskIDList == null || taskIDList.isEmpty()) {
            taskIDsBeingTracked.clear();
            return new ArrayList<>();
        }

        // Setup TaskIDs in case not already done
        if (taskIDsBeingTracked == null || taskIDsBeingTracked.isEmpty() || !taskIDsBeingTracked.containsAll(taskIDList)) {
            setupTaskIDsToTrack(hyperTrackMapFragment, taskIDList);
        } else {
            // Reset taskIDsBeingTracked to taskIDList to remove any extra taskIDs
            taskIDsBeingTracked.clear();
            taskIDsBeingTracked.addAll(taskIDList);
        }

        return taskIDsBeingTracked;
    }

    protected final boolean setupTaskIDsToTrack(final HyperTrackMapFragment hyperTrackMapFragment, final List<String> taskIDList) {
//        final List<HTTask> tasksToTrack = new ArrayList<>();
        final List<String> taskIDsToTrack = new ArrayList<>();
        final List<String> taskIDsToAddForTracking = new ArrayList<>();

        if (taskIDList != null && !taskIDList.isEmpty()) {

            // Check if User provided taskIDs are being tracked or not
            for (String taskID : taskIDList) {

                // Check if this taskID is being tracked by ConsumerClient
                HTTask task = consumerClient.taskForTaskID(taskID);
                if (task != null) {
                    taskIDsToTrack.add(taskID);
//                    tasksToTrack.add(task);

                } else if (!taskIDsAddedForTracking.contains(taskID)) {
                    //Check if this taskID has already been added for tracking
                    taskIDsToAddForTracking.add(taskID);
                }
            }

            if (taskIDsToAddForTracking.size() > 0) {
                // Call trackTask for taskIDs which are not currently being tracked
                addTaskIDsForTracking(hyperTrackMapFragment, taskIDsToAddForTracking);
            }

            if (taskIDsSetupForTrackingFailed != null && taskIDsSetupForTrackingFailed.containsAll(taskIDsToTrack)) {
                return false;
            }
        }

        // TODO: 13/09/16 Add Error in case of tasks with different destinations
        // Check if all the tasks being currently tracked belong to the same destination location
//        if (ConsumerClient.checkIfTasksHaveSameDestination(tasksToTrack)) {

        // Post a success callback on Start tracking tasks
        if (mapFragmentCallback != null)
            mapFragmentCallback.onMapFragmentSucceed(hyperTrackMapFragment, taskIDsToTrack);

        taskIDsBeingTracked = taskIDsToTrack;
        return true;
//        }

        // Post an error callback on tasks belonging to different locations
//        if (mapFragmentCallback != null) {
//            mapFragmentCallback.onMapFragmentFailed(hyperTrackMapFragment, taskIDsToTrack,
//                    TasksDestinationLocationDifferentMessage);
//        }

//        taskIDsSetupForTrackingFailed.clear();
//        taskIDsSetupForTrackingFailed.addAll(taskIDsToTrack);

//        return false;
    }

    protected final void addTaskIDsForTracking(final HyperTrackMapFragment hyperTrackMapFragment, final List<String> taskIDsToAddForTracking) {
        consumerClient.trackTask(taskIDsToAddForTracking, (Activity) mContext, new TaskListCallBack() {
            @Override
            public void onSuccess(List<HTTask> taskList) {

                // Setup all the taskIDsBeingTracked and notify in case of success
                if (setupTaskIDsToTrack(hyperTrackMapFragment, getTaskIDsToTrack(hyperTrackMapFragment))) {
                    notifyDataSetChanged();

                    // Remove successfully added taskIDs from taskIDsAddedForTracking
                    if (taskList != null) {
                        for (HTTask task : taskList) {
                            if (task != null && !TextUtils.isEmpty(task.getId()))
                                taskIDsAddedForTracking.remove(task.getId());
                        }
                    }
                }
            }

            @Override
            public void onError(Exception exception) {
                if (mapFragmentCallback != null) {
                    mapFragmentCallback.onMapFragmentFailed(hyperTrackMapFragment, taskIDsToAddForTracking,
                            exception.getMessage());
                }

                taskIDsAddedForTracking.removeAll(taskIDsToAddForTracking);
            }
        });
        taskIDsAddedForTracking.addAll(taskIDsToAddForTracking);
    }

    protected final int getTaskIDsToTrackCount(HyperTrackMapFragment hyperTrackMapFragment) {
        return taskIDsToTrack(hyperTrackMapFragment).size();
    }

    protected final boolean shouldBindView() {
        if (mapViewModelList != null)
            return mapViewModelList.shouldBindView();

        return shouldBindView;
    }

    protected final void setConsumerClient(@NonNull ConsumerClient consumerClient) {
        this.consumerClient = consumerClient;
    }

    protected final void setMapUpdateInterface(@NonNull MapUpdateInterface mapUpdateInterface) {
        this.mapUpdateInterface = mapUpdateInterface;
    }

    protected final void setMapViewModelList(@NonNull MapViewModelList mapViewModelList) {
        this.mapViewModelList = mapViewModelList;
    }

    protected final void setMapFragmentCallback(MapFragmentCallback mapFragmentCallback) {
        this.mapFragmentCallback = mapFragmentCallback;
    }
}
