package com.hypertrack.lib.internal.consumer.view;

/**
 * Created by piyush on 08/07/16.
 */

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.hypertrack.lib.internal.consumer.models.HTMapViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * package
 */
class HTMapViewModelList {

    private boolean shouldBindView = true;
    private boolean isInitialLocationPlaced = false;
    private boolean isOrderStatusBarEnabled = true;
    private boolean isDynamicZoomDisabled = false;
    private boolean isTrafficEnabled = true;

    private HashMap<String, HTMapViewModel> HTMapViewModelList;

    public HTMapViewModelList() {
        HTMapViewModelList = new HashMap<>();
    }

    public HashMap<String, HTMapViewModel> getHTMapViewModelList() {
        return HTMapViewModelList;
    }

    public HTMapViewModel getHTMapViewModelForActionID(String actionID) {
        return HTMapViewModelList.get(actionID);
    }

    public void addHTMapViewModelForActionID(String actionID) {
        HTMapViewModelList.put(actionID, new HTMapViewModel());
    }

    public void addHTMapViewModelForActionID(String actionID, HTMapViewModel HTMapViewModel) {
        HTMapViewModelList.put(actionID, HTMapViewModel);
    }

    public HTMapViewModel removeHTMapViewModelForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.remove(actionID);
        }

        return null;
    }

    public void clearAllHTMapViewModels() {
        if (HTMapViewModelList != null) {
            HTMapViewModelList.clear();
        }
    }

    public String getHeroMarkerIdForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getHeroMarkerId();
        }

        return null;
    }

    public Marker getHeroMarkerForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getHeroMarker();
        }

        return null;
    }

    public void setHeroMarkerForActionID(String actionID, Marker heroMarker) {
        if (HTMapViewModelList.get(actionID) == null) {
            HTMapViewModelList.put(actionID, new HTMapViewModel());
        }
        HTMapViewModelList.get(actionID).setHeroMarker(heroMarker);
    }

    public String getDestinationMarkerIdForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getDestinationMarkerId();
        }

        return null;
    }

    public Marker getDestinationMarkerForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getDestinationMarker();
        }

        return null;
    }

    public void setDestinationMarkerForActionID(String actionID, Marker destinationMarker) {
        if (HTMapViewModelList.get(actionID) == null) {
            HTMapViewModelList.put(actionID, new HTMapViewModel());
        }

        HTMapViewModelList.get(actionID).setDestinationMarker(destinationMarker);
    }

    public String getSourceMarkerIdForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getSourceMarkerId();
        }

        return null;
    }

    public Marker getSourceMarkerForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getSourceMarker();
        }

        return null;
    }

    public void setSourceMarkerForActionID(String actionID, Marker sourceMarker) {
        if (HTMapViewModelList.get(actionID) == null) {
            HTMapViewModelList.put(actionID, new HTMapViewModel());
        }

        HTMapViewModelList.get(actionID).setSourceMarker(sourceMarker);

    }

    public String getActionSummaryStartMarkerIdForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getActionSummaryStartMarkerId();
        }

        return null;
    }

    public Marker getActionSummaryStartMarkerForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getActionSummaryStartMarker();
        }

        return null;
    }

    public void setActionSummaryStartMarkerForActionID(String actionID, Marker actionSummaryStartMarker) {
        if (HTMapViewModelList.get(actionID) == null) {
            HTMapViewModelList.put(actionID, new HTMapViewModel());
        }

        HTMapViewModelList.get(actionID).setActionSummaryStartMarker(actionSummaryStartMarker);
    }

    public String getActionSummaryEndMarkerIdForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getActionSummaryEndMarkerId();
        }

        return null;
    }

    public Marker getActionSummaryEndMarkerForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getActionSummaryEndMarker();
        }

        return null;
    }

    public void setActionSummaryEndMarkerForActionID(String actionID, Marker actionSummaryEndMarker) {
        if (HTMapViewModelList.get(actionID) == null) {
            HTMapViewModelList.put(actionID, new HTMapViewModel());
        }

        HTMapViewModelList.get(actionID).setActionSummaryEndMarker(actionSummaryEndMarker);
    }

    public List<LatLng> getActionSummaryPolylineForActionID(String actionID) {
        if (HTMapViewModelList.get(actionID) != null) {
            return HTMapViewModelList.get(actionID).getActionSummaryPolyline();
        }

        return null;
    }

    public void setActionSummaryPolylineForActionID(String actionID, List<LatLng> actionSummaryPolyline) {
        if (HTMapViewModelList.get(actionID) == null) {
            HTMapViewModelList.put(actionID, new HTMapViewModel());
        }

        HTMapViewModelList.get(actionID).setActionSummaryPolyline(actionSummaryPolyline);
    }

    public ArrayList<String> getDestinationMarkersForAllActions() {
        if (HTMapViewModelList != null && !HTMapViewModelList.isEmpty()) {
            ArrayList<String> destinationMarkers = new ArrayList<>();

            for (String actionID : HTMapViewModelList.keySet()) {
                if (HTMapViewModelList.get(actionID) != null && HTMapViewModelList.get(actionID).getDestinationMarker() != null) {
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
