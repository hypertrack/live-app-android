package com.hypertrack.lib.internal.consumer.models;

import com.hypertrack.lib.internal.consumer.utils.TimeAwarePolylineUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by suhas on 11/01/16.
 */

/**
 * package
 */
class GPXLogList {

    private List<GPXLog> gpsLogs;

    public GPXLogList() {
        gpsLogs = new ArrayList<>();
    }

    public Integer size() {
        return gpsLogs.size();
    }

    public void setTimedLocations(List<GPXLog> gpsLogs) {
        this.gpsLogs = gpsLogs;
    }

    public List<GPXLog> getGPXLogs(Date from) {
        List<GPXLog> fromLocations = TimeAwarePolylineUtils.getLocationsTillTime(gpsLogs, from);
        fromLocations.remove(fromLocations.size() - 1);

        List<GPXLog> toLocations = TimeAwarePolylineUtils.getLocationsTillTime(gpsLogs, gpsLogs.get(gpsLogs.size() - 1).getDate());
        toLocations.removeAll(fromLocations);

        return toLocations;
    }
}
