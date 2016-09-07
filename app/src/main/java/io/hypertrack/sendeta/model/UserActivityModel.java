package io.hypertrack.sendeta.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by piyush on 07/09/16.
 */
public class UserActivityModel {

    private String taskID;

    private boolean inProcess;
    private String driverImageURL;
    private boolean disabledMainIcon = true;
    private String title;
    private String subtitle;
    private String date;
    private String startAddress;
    private String startTime;
    private String endAddress;
    private String endTime;
    private LatLng startLocation;
    private LatLng endLocation;
    private List<LatLng> polyline;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public boolean isInProcess() {
        return inProcess;
    }

    public void setInProcess(boolean inProcess) {
        this.inProcess = inProcess;
    }

    public String getDriverImageURL() {
        return driverImageURL;
    }

    public void setDriverImageURL(String driverImageURL) {
        this.driverImageURL = driverImageURL;
    }

    public boolean isDisabledMainIcon() {
        return disabledMainIcon;
    }

    public void setDisabledMainIcon(boolean disabledMainIcon) {
        this.disabledMainIcon = disabledMainIcon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }

    public List<LatLng> getPolyline() {
        return polyline;
    }

    public void setPolyline(List<LatLng> polyline) {
        this.polyline = polyline;
    }
}
