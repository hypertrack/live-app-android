package com.hypertrack.lib.internal.consumer.view;

/**
 * Created by piyush on 08/07/16.
 */

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.hypertrack.lib.internal.consumer.models.MapViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * package
 */
class MapViewModelList {

    private boolean shouldBindView = true;
    private boolean isInitialLocationPlaced = false;
    private boolean isOrderStatusBarEnabled = true;
    private boolean isDynamicZoomDisabled = false;
    private boolean isTrafficEnabled = true;

    private HashMap<String, MapViewModel> mapViewModelList;

    public MapViewModelList() {
        mapViewModelList = new HashMap<>();
    }

    public HashMap<String, MapViewModel> getMapViewModelList(){
        return mapViewModelList;
    }

    public MapViewModel getMapViewModelForActionID(String actionID) {
        return mapViewModelList.get(actionID);
    }

    public void addMapViewModelForActionID(String actionID) {
        mapViewModelList.put(actionID, new MapViewModel());
    }

    public void addMapViewModelForActionID(String actionID, MapViewModel mapViewModel) {
        mapViewModelList.put(actionID, mapViewModel);
    }

    public MapViewModel removeMapViewModelForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.remove(actionID);
        }

        return null;
    }

    public void clearAllMapViewModels() {
        if (mapViewModelList != null) {
            mapViewModelList.clear();
        }
    }

    public String getHeroMarkerIdForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getHeroMarkerId();
        }

        return null;
    }

    public Marker getHeroMarkerForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getHeroMarker();
        }

        return null;
    }

    public void setHeroMarkerForActionID(String actionID, Marker heroMarker) {
        if (mapViewModelList.get(actionID) == null) {
            mapViewModelList.put(actionID, new MapViewModel());
        }
        mapViewModelList.get(actionID).setHeroMarker(heroMarker);
    }

    public String getDestinationMarkerIdForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getDestinationMarkerId();
        }

        return null;
    }

    public Marker getDestinationMarkerForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getDestinationMarker();
        }

        return null;
    }

    public void setDestinationMarkerForActionID(String actionID, Marker destinationMarker) {
        if (mapViewModelList.get(actionID) == null) {
            mapViewModelList.put(actionID, new MapViewModel());
        }

        mapViewModelList.get(actionID).setDestinationMarker(destinationMarker);
    }

    public String getSourceMarkerIdForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getSourceMarkerId();
        }

        return null;
    }

    public Marker getSourceMarkerForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getSourceMarker();
        }

        return null;
    }

    public void setSourceMarkerForActionID(String actionID, Marker sourceMarker) {
        if (mapViewModelList.get(actionID) == null) {
            mapViewModelList.put(actionID, new MapViewModel());
        }

        mapViewModelList.get(actionID).setSourceMarker(sourceMarker);

    }

    public String getActionSummaryStartMarkerIdForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getActionSummaryStartMarkerId();
        }

        return null;
    }

    public Marker getActionSummaryStartMarkerForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getActionSummaryStartMarker();
        }

        return null;
    }

    public void setActionSummaryStartMarkerForActionID(String actionID, Marker actionSummaryStartMarker) {
        if (mapViewModelList.get(actionID) == null) {
            mapViewModelList.put(actionID, new MapViewModel());
        }

        mapViewModelList.get(actionID).setActionSummaryStartMarker(actionSummaryStartMarker);
    }

    public String getActionSummaryEndMarkerIdForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getActionSummaryEndMarkerId();
        }

        return null;
    }

    public Marker getActionSummaryEndMarkerForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getActionSummaryEndMarker();
        }

        return null;
    }

    public void setActionSummaryEndMarkerForActionID(String actionID, Marker actionSummaryEndMarker) {
        if (mapViewModelList.get(actionID) == null) {
            mapViewModelList.put(actionID, new MapViewModel());
        }

        mapViewModelList.get(actionID).setActionSummaryEndMarker(actionSummaryEndMarker);
    }

    public List<LatLng> getActionSummaryPolylineForActionID(String actionID) {
        if (mapViewModelList.get(actionID) != null) {
            return mapViewModelList.get(actionID).getActionSummaryPolyline();
        }

        return null;
    }

    public void setActionSummaryPolylineForActionID(String actionID, List<LatLng> actionSummaryPolyline) {
        if (mapViewModelList.get(actionID) == null) {
            mapViewModelList.put(actionID, new MapViewModel());
        }

        mapViewModelList.get(actionID).setActionSummaryPolyline(actionSummaryPolyline);
    }

    public ArrayList<String> getDestinationMarkersForAllActions() {
        if (mapViewModelList != null && !mapViewModelList.isEmpty()) {
            ArrayList<String> destinationMarkers = new ArrayList<>();

            for (String actionID : mapViewModelList.keySet()) {
                if (mapViewModelList.get(actionID) != null && mapViewModelList.get(actionID).getDestinationMarker() != null) {
                    destinationMarkers.add(actionID);
                }
            }

            return destinationMarkers;
        }

        return null;
    }

    public boolean shouldBindView() {
        return shouldBindView;
    }

    public void setShouldBindView(boolean shouldBindView) {
        this.shouldBindView = shouldBindView;
    }

    public boolean isOrderStatusBarEnabled() {
        return isOrderStatusBarEnabled;
    }

    public void setOrderStatusBarEnabled(boolean orderStatusBarEnabled) {
        this.isOrderStatusBarEnabled = orderStatusBarEnabled;
    }

    public boolean isInitialLocationPlaced() {
        return isInitialLocationPlaced;
    }

    public void setInitialLocationPlaced(boolean initialLocationPlaced) {
        isInitialLocationPlaced = initialLocationPlaced;
    }

    public boolean isDynamicZoomDisabled() {
        return isDynamicZoomDisabled;
    }

    public void setDynamicZoomDisabled(boolean dynamicZoomDisabled) {
        isDynamicZoomDisabled = dynamicZoomDisabled;
    }

    public boolean isTrafficEnabled() {
        return isTrafficEnabled;
    }

    public void setTrafficEnabled(boolean trafficEnabled) {
        isTrafficEnabled = trafficEnabled;
    }
}
