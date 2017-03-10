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

    public MapViewModel getMapViewModelForTaskID(String taskID) {
        return mapViewModelList.get(taskID);
    }

    public void addMapViewModelForTaskID(String taskID) {
        mapViewModelList.put(taskID, new MapViewModel());
    }

    public void addMapViewModelForTaskID(String taskID, MapViewModel mapViewModel) {
        mapViewModelList.put(taskID, mapViewModel);
    }

    public MapViewModel removeMapViewModelForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.remove(taskID);
        }

        return null;
    }

    public void clearAllMapViewModels() {
        if (mapViewModelList != null) {
            mapViewModelList.clear();
        }
    }

    public String getHeroMarkerIdForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getHeroMarkerId();
        }

        return null;
    }

    public Marker getHeroMarkerForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getHeroMarker();
        }

        return null;
    }

    public void setHeroMarkerForTaskID(String taskID, Marker heroMarker) {
        if (mapViewModelList.get(taskID) == null) {
            mapViewModelList.put(taskID, new MapViewModel());
        }
        mapViewModelList.get(taskID).setHeroMarker(heroMarker);
    }

    public String getDestinationMarkerIdForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getDestinationMarkerId();
        }

        return null;
    }

    public Marker getDestinationMarkerForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getDestinationMarker();
        }

        return null;
    }

    public void setDestinationMarkerForTaskID(String taskID, Marker destinationMarker) {
        if (mapViewModelList.get(taskID) == null) {
            mapViewModelList.put(taskID, new MapViewModel());
        }

        mapViewModelList.get(taskID).setDestinationMarker(destinationMarker);
    }

    public String getSourceMarkerIdForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getSourceMarkerId();
        }

        return null;
    }

    public Marker getSourceMarkerForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getSourceMarker();
        }

        return null;
    }

    public void setSourceMarkerForTaskID(String taskID, Marker sourceMarker) {
        if (mapViewModelList.get(taskID) == null) {
            mapViewModelList.put(taskID, new MapViewModel());
        }

        mapViewModelList.get(taskID).setSourceMarker(sourceMarker);

    }

    public String getTaskSummaryStartMarkerIdForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getTaskSummaryStartMarkerId();
        }

        return null;
    }

    public Marker getTaskSummaryStartMarkerForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getTaskSummaryStartMarker();
        }

        return null;
    }

    public void setTaskSummaryStartMarkerForTaskID(String taskID, Marker taskSummaryStartMarker) {
        if (mapViewModelList.get(taskID) == null) {
            mapViewModelList.put(taskID, new MapViewModel());
        }

        mapViewModelList.get(taskID).setTaskSummaryStartMarker(taskSummaryStartMarker);
    }

    public String getTaskSummaryEndMarkerIdForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getTaskSummaryEndMarkerId();
        }

        return null;
    }

    public Marker getTaskSummaryEndMarkerForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getTaskSummaryEndMarker();
        }

        return null;
    }

    public void setTaskSummaryEndMarkerForTaskID(String taskID, Marker taskSummaryEndMarker) {
        if (mapViewModelList.get(taskID) == null) {
            mapViewModelList.put(taskID, new MapViewModel());
        }

        mapViewModelList.get(taskID).setTaskSummaryEndMarker(taskSummaryEndMarker);
    }

    public List<LatLng> getTaskSummaryPolylineForTaskID(String taskID) {
        if (mapViewModelList.get(taskID) != null) {
            return mapViewModelList.get(taskID).getTaskSummaryPolyline();
        }

        return null;
    }

    public void setTaskSummaryPolylineForTaskID(String taskID, List<LatLng> taskSummaryPolyline) {
        if (mapViewModelList.get(taskID) == null) {
            mapViewModelList.put(taskID, new MapViewModel());
        }

        mapViewModelList.get(taskID).setTaskSummaryPolyline(taskSummaryPolyline);
    }

    public ArrayList<String> getDestinationMarkersForAllTasks() {
        if (mapViewModelList != null && !mapViewModelList.isEmpty()) {
            ArrayList<String> destinationMarkers = new ArrayList<>();

            for (String taskID : mapViewModelList.keySet()) {
                if (mapViewModelList.get(taskID) != null && mapViewModelList.get(taskID).getDestinationMarker() != null) {
                    destinationMarkers.add(taskID);
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
