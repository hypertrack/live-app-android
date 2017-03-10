package com.hypertrack.lib.internal.consumer.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

/**
 * Created by piyush on 08/07/16.
 */
public class MapViewModel {

    // Markers
    private Marker heroMarker;
    private Marker destinationMarker;
    private Marker sourceMarker;
    private Marker taskSummaryStartMarker;
    private Marker taskSummaryEndMarker;
    private List<LatLng> taskSummaryPolyline;

    //Task-Specific Settings
    private boolean rotateHeroMarker = true;
    private boolean isDestinationMarkerVisible = true;
    private boolean isTaskSummaryInfoVisible = true;
    private boolean isAddressInfoVisible = true;
    private boolean isUserInfoVisible = true;
    private boolean isOrderDetailsButtonVisible = false;
    private boolean isSourceMarkerVisible = false;
    private boolean isCallButtonVisible = true;

    private boolean disableEditDestination = false;
    private boolean showUserLocationMissingAlert = true;
    private boolean showEditDestinationFailureAlert = true;

    public MapViewModel() {
        this(null, null, null, null, null);
    }

    public MapViewModel(Marker heroMarker, Marker destinationMarker, Marker sourceMarker) {
        this(heroMarker, destinationMarker, sourceMarker, null, null);
    }

    public MapViewModel(Marker heroMarker, Marker destinationMarker, Marker sourceMarker,
                        Marker taskSummaryStartMarker, Marker taskSummaryEndMarker) {
        this.heroMarker = heroMarker;
        this.destinationMarker = destinationMarker;
        this.sourceMarker = sourceMarker;
        this.taskSummaryStartMarker = taskSummaryStartMarker;
        this.taskSummaryEndMarker = taskSummaryEndMarker;
    }

    public Marker getHeroMarker() {
        return heroMarker;
    }

    public void setHeroMarker(Marker heroMarker) {
        this.heroMarker = heroMarker;
    }

    public String getHeroMarkerId() {
        if (heroMarker != null)
            return heroMarker.getId();

        return null;
    }

    public Marker getDestinationMarker() {
        return destinationMarker;
    }

    public void setDestinationMarker(Marker destinationMarker) {
        this.destinationMarker = destinationMarker;
    }

    public String getDestinationMarkerId() {
        if (destinationMarker != null)
            return destinationMarker.getId();

        return null;
    }

    public Marker getSourceMarker() {
        return sourceMarker;
    }

    public void setSourceMarker(Marker sourceMarker) {
        this.sourceMarker = sourceMarker;
    }

    public String getSourceMarkerId() {
        if (sourceMarker != null)
            return sourceMarker.getId();

        return null;
    }

    public Marker getTaskSummaryStartMarker() {
        return taskSummaryStartMarker;
    }

    public void setTaskSummaryStartMarker(Marker taskSummaryStartMarker) {
        this.taskSummaryStartMarker = taskSummaryStartMarker;
    }

    public String getTaskSummaryStartMarkerId() {
        if (taskSummaryStartMarker != null)
            return taskSummaryStartMarker.getId();

        return null;
    }

    public Marker getTaskSummaryEndMarker() {
        return taskSummaryEndMarker;
    }

    public void setTaskSummaryEndMarker(Marker taskSummaryEndMarker) {
        this.taskSummaryEndMarker = taskSummaryEndMarker;
    }

    public String getTaskSummaryEndMarkerId() {
        if (taskSummaryEndMarker != null)
            return taskSummaryEndMarker.getId();

        return null;
    }

    public List<LatLng> getTaskSummaryPolyline() {
        return taskSummaryPolyline;
    }

    public void setTaskSummaryPolyline(List<LatLng> taskSummaryPolyline) {
        this.taskSummaryPolyline = taskSummaryPolyline;
    }

    public boolean rotateHeroMarker() {
        return rotateHeroMarker;
    }

    public void setRotateHeroMarker(boolean rotateHeroMarker) {
        this.rotateHeroMarker = rotateHeroMarker;
    }

    public boolean isDestinationMarkerVisible() {
        return isDestinationMarkerVisible;
    }

    public void setDestinationMarkerVisible(boolean destinationMarkerVisible) {
        isDestinationMarkerVisible = destinationMarkerVisible;
    }

    public boolean disableEditDestination() {
        return disableEditDestination;
    }

    public void setDisableEditDestination(boolean disableEditDestination) {
        this.disableEditDestination = disableEditDestination;
    }

    public boolean isTaskSummaryInfoVisible() {
        return isTaskSummaryInfoVisible;
    }

    public void setTaskSummaryInfoVisible(boolean taskSummaryInfoVisible) {
        isTaskSummaryInfoVisible = taskSummaryInfoVisible;
    }

    public boolean isAddressInfoVisible() {
        return isAddressInfoVisible;
    }

    public void setAddressInfoVisible(boolean addressInfoVisible) {
        isAddressInfoVisible = addressInfoVisible;
    }

    public boolean isUserInfoVisible() {
        return isUserInfoVisible;
    }

    public void setUserInfoVisible(boolean userInfoVisible) {
        isUserInfoVisible = userInfoVisible;
    }

    public boolean isOrderDetailsButtonVisible() {
        return isOrderDetailsButtonVisible;
    }

    public void setOrderDetailsButtonVisible(boolean orderDetailsButtonVisible) {
        isOrderDetailsButtonVisible = orderDetailsButtonVisible;
    }

    public boolean isSourceMarkerVisible() {
        return isSourceMarkerVisible;
    }

    public void setSourceMarkerVisible(boolean sourceMarkerVisible) {
        isSourceMarkerVisible = sourceMarkerVisible;
    }

    public boolean isCallButtonVisible() {
        return isCallButtonVisible;
    }

    public void setCallButtonVisible(boolean callButtonVisible) {
        isCallButtonVisible = callButtonVisible;
    }

    public boolean shouldShowUserLocationMissingAlert() {
        return showUserLocationMissingAlert;
    }

    public void setShowUserLocationMissingAlert(boolean showUserLocationMissingAlert) {
        this.showUserLocationMissingAlert = showUserLocationMissingAlert;
    }

    public boolean shouldShowEditDestinationFailureAlert() {
        return showEditDestinationFailureAlert;
    }

    public void setShowEditDestinationFailureAlert(boolean showEditDestinationFailureAlert) {
        this.showEditDestinationFailureAlert = showEditDestinationFailureAlert;
    }
}
