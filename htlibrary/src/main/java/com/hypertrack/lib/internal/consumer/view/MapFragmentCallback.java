package com.hypertrack.lib.internal.consumer.view;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by suhas on 09/02/16.
 */
public class MapFragmentCallback {

    /**
     * Invoked when the onMapReady Callback is received.
     *
     * @param hyperTrackMapFragment The fragment in which the the map is being added.
     * @param map         The GoogleMap instance which will be added in HyperTrackMapFragment.
     */
    public void onMapReadyCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
    }

    /**
     * Invoked when the map.onMapLoaded Callback is received.
     *
     * @param hyperTrackMapFragment The fragment in which the the map is being loaded.
     * @param map         The GoogleMap instance which is being loaded in HyperTrackMapFragment.
     */
    public void onMapLoadedCallback(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
    }

    /**
     * Invoked when the map.onMapClickListener Callback is received.
     *
     * @param hyperTrackMapFragment The fragment in which the the map is being loaded.
     * @param map         The GoogleMap instance which is being loaded in HyperTrackMapFragment.
     */
    public void onMapClick(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
    }

    /**
     * Invoked when the map.onMyLocationButtonClickListener Callback is received.
     *
     * @param hyperTrackMapFragment The fragment in which the the map is being loaded.
     * @param map         The GoogleMap instance which is being loaded in HyperTrackMapFragment.
     */
    public void onMapMyLocationButtonClick(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
    }

    /**
     * Invoked before map.clear() will be called.
     *
     * @param hyperTrackMapFragment The fragment in which the the map is being cleared.
     * @param map         The GoogleMap instance which is being loaded in HyperTrackMapFragment.
     */
    public void onMapWillClear(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
    }

    /**
     * Invoked after map.clear() will be called.
     *
     * @param hyperTrackMapFragment The fragment in which the the map was cleared.
     * @param map         The GoogleMap instance which is being loaded in HyperTrackMapFragment.
     */
    public void onMapCleared(HyperTrackMapFragment hyperTrackMapFragment, GoogleMap map) {
    }

    /**
     * Invoked when the Hero Marker is clicked.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero marker is placed.
     * @param taskID      The TaskID for which the Hero Marker is clicked.
     * @param heroMarker  The Hero Marker Instance which is clicked.
     */
    public void onHeroMarkerClicked(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker heroMarker) {
    }

    /**
     * Invoked when the Hero Marker will move on the map.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero marker is placed.
     * @param taskID      The TaskID for which the Hero Marker will move.
     * @param heroMarker  The Hero Marker instance which will move.
     * @param toLocation  The Position to which the Hero Marker will move.
     */
    public void onHeroMarkerWillMove(HyperTrackMapFragment hyperTrackMapFragment, String taskID,
                                     Marker heroMarker, LatLng toLocation) {
    }

    /**
     * Invoked when the Hero Marker is added.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero marker is added.
     * @param taskID      The TaskID for which the Hero Marker is added.
     * @param heroMarker  The Hero Marker Instance which is added.
     */
    public void onHeroMarkerAdded(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker heroMarker) {
    }

    /**
     * Invoked when the Hero Marker is removed.
     *
     * @param hyperTrackMapFragment The fragment in which the Hero marker is removed.
     * @param taskID      The TaskID for which the Hero Marker is removed.
     * @param heroMarker  The Hero Marker Instance which is removed.
     */
    public void onHeroMarkerRemoved(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker heroMarker) {
    }

    /**
     * Invoked when the Source Marker is clicked.
     *
     * @param hyperTrackMapFragment  The fragment in which the Source marker is placed.
     * @param taskID       The TaskID for which the Source Marker is clicked.
     * @param sourceMarker The Source Marker Instance which is clicked.
     */
    public void onSourceMarkerClicked(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker sourceMarker) {
    }

    /**
     * Invoked when the Source Marker is added.
     *
     * @param hyperTrackMapFragment  The fragment in which the Source marker is added.
     * @param taskID       The TaskID for which the Source Marker is added.
     * @param sourceMarker The Source Marker Instance which is added.
     */
    public void onSourceMarkerAdded(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker sourceMarker) {
    }

    /**
     * Invoked when the Source Marker is removed.
     *
     * @param hyperTrackMapFragment  The fragment in which the Source marker is removed.
     * @param taskID       The TaskID for which the Source Marker is removed.
     * @param sourceMarker The Source Marker Instance which is removed.
     */
    public void onSourceMarkerRemoved(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker sourceMarker) {
    }

    /**
     * Invoked when the Destination Marker is clicked.
     *
     * @param hyperTrackMapFragment       The fragment in which the Destination marker is placed.
     * @param taskID            The TaskID for which the Destination Marker is clicked.
     * @param destinationMarker The Destination Marker Instance which is clicked.
     */
    public void onDestinationMarkerClicked(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker destinationMarker) {
    }

    /**
     * Invoked when the Destination Marker is added.
     *
     * @param hyperTrackMapFragment       The fragment in which the Destination marker is added.
     * @param taskID            The TaskID for which the Destination Marker is added.
     * @param destinationMarker The Destination Marker Instance which is added.
     */
    public void onDestinationMarkerAdded(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker destinationMarker) {
    }

    /**
     * Invoked when the Destination Marker is removed.
     *
     * @param hyperTrackMapFragment       The fragment in which the Destination marker is removed.
     * @param taskID            The TaskID for which the Destination Marker is removed.
     * @param destinationMarker The Destination Marker Instance which is removed.
     */
    public void onDestinationMarkerRemoved(HyperTrackMapFragment hyperTrackMapFragment, String taskID, Marker destinationMarker) {
    }

    /**
     * Invoked when the Multiple Tasks Destination Marker is clicked.
     *
     * @param hyperTrackMapFragment       The fragment in which the Multiple Tasks Destination marker is placed.
     * @param destinationMarker The Multiple Tasks Destination Marker Instance which is clicked.
     */
    public void onMultipleTasksDestinationMarkerClicked(HyperTrackMapFragment hyperTrackMapFragment, Marker destinationMarker) {
    }

    /**
     * Invoked when the Multiple Tasks Destination Marker is added.
     *
     * @param hyperTrackMapFragment       The fragment in which the Multiple Tasks Destination marker is added.
     * @param destinationMarker The Multiple Tasks Destination Marker Instance which is added.
     */
    public void onMultipleTasksDestinationMarkerAdded(HyperTrackMapFragment hyperTrackMapFragment, Marker destinationMarker) {
    }

    /**
     * Invoked when the Multiple Tasks Destination Marker is removed.
     *
     * @param hyperTrackMapFragment       The fragment in which the Multiple Tasks Destination marker is removed.
     * @param destinationMarker The Multiple Tasks Destination Marker Instance which is removed.
     */
    public void onMultipleTasksDestinationMarkerRemoved(HyperTrackMapFragment hyperTrackMapFragment, Marker destinationMarker) {
    }

    /**
     * Invoked when a Custom Marker is clicked.
     *
     * @param hyperTrackMapFragment  The fragment in which the custom marker is placed.
     * @param customMarker The Custom Marker Instance which is clicked.
     */
    public void onCustomMarkerClicked(HyperTrackMapFragment hyperTrackMapFragment, Marker customMarker) {
    }

    /**
     * Invoked when a Custom Marker is added.
     *
     * @param hyperTrackMapFragment  The fragment in which the custom marker is added.
     * @param customMarker The Custom Marker Instance which is added.
     */
    public void onCustomMarkerAdded(HyperTrackMapFragment hyperTrackMapFragment, Marker customMarker) {
    }

    /**
     * Invoked when a Custom Marker is removed.
     *
     * @param hyperTrackMapFragment  The fragment in which the custom marker is removed.
     * @param customMarker The Custom Marker Instance which is removed.
     */
    public void onCustomMarkerRemoved(HyperTrackMapFragment hyperTrackMapFragment, Marker customMarker) {
    }

    /**
     * Invoked when the Call button on the user info view is clicked.
     *
     * @param hyperTrackMapFragment The fragment in which the Call Button on the User Info View is placed.
     * @param taskID      The TaskID for which the Call Button is clicked.
     */
    public void onCallButtonClicked(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
    }

    /**
     * Invoked when the Order Details button on the User Info View is clicked.
     *
     * @param hyperTrackMapFragment The fragment in which the Order Deails Button is placed.
     * @param taskID      The TaskID for which the CTA Button is clicked.
     */
    public void onOrderDetailsButtonClicked(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
    }

    /**
     * Invoked when the List of Tasks are added successfully. This method indicates these tasks
     * can be tracked now if these taskIDs are added in getTaskIDsToTrack() in MapAdapter.
     *
     * @param hyperTrackMapFragment The fragment in which AddTask succeeded.
     * @param taskID      The List of TaskIDs for which the AddTask succeeded.
     */
    public void onMapFragmentSucceed(HyperTrackMapFragment hyperTrackMapFragment, List<String> taskID) {
    }

    /**
     * Invoked when Adding Task failed. You should remove views corresponding to these taskIDs
     * from the hierarchy or show an appropriate error to the user.
     *
     * @param hyperTrackMapFragment  The fragment in which AddTask failed.
     * @param taskID       The List of TaskIDs for which the AddTask failed.
     * @param errorMessage ErrorMessage indicating the reason for failure.
     */
    public void onMapFragmentFailed(HyperTrackMapFragment hyperTrackMapFragment, List<String> taskID, String errorMessage) {
    }

    /**
     * Invoked when when editing of destination begins.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination address is being edited.
     * @param taskID      The List of TaskIDs for which the Destination address is being edited.
     */
    public void onBeginEditingDestination(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
    }

    /**
     * Invoked when when editing of destination is canceled.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination address is being edited.
     * @param taskID      The List of TaskIDs for which the Destination address is being edited.
     */
    public void onCanceledEditingDestination(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
    }

    /**
     * Invoked when editing of destination is successfully completed.
     *
     * @param hyperTrackMapFragment The fragment in which the Destination address is being edited.
     * @param taskID      The List of TaskIDs for which the Destination address is being edited.
     */
    public void onEndEditingDestination(HyperTrackMapFragment hyperTrackMapFragment, String taskID) {
    }

    /**
     * Invoked when user location is missing on edit destination mode
     *
     * @param hyperTrackMapFragment  The fragment in which the Destination address is being edited.
     * @param taskID       The List of TaskIDs for which the Destination address is being edited.
     * @param errorMessage ErrorMessage indicating the reason for failure.
     */
    public void onReceiveUserLocationMissingError(HyperTrackMapFragment hyperTrackMapFragment, String taskID, String errorMessage) {
    }

    /**
     * Invoked when edit destination fails
     *
     * @param hyperTrackMapFragment  The fragment in which the Destination address is being edited.
     * @param taskID       The List of TaskIDs for which the Destination address is being edited.
     * @param errorMessage ErrorMessage indicating the reason for failure.
     */
    public void onReceiveEditDestinationError(HyperTrackMapFragment hyperTrackMapFragment, String taskID, String errorMessage) {
    }
}