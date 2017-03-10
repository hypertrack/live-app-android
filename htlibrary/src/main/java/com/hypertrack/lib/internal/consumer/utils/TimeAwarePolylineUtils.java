package com.hypertrack.lib.internal.consumer.utils;

import com.hypertrack.lib.internal.consumer.models.GPXLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by piyush on 08/11/16.
 */
public class TimeAwarePolylineUtils {

    public static List<GPXLog> getDecodedPolyline(String polyline) {

        List<GPXLog> gpxLogs = new ArrayList<>();

        int index = 0;
        long polylineLength = polyline.length(), timePart = 0;
        double latPart = 0.0, lngPart = 0.0;

        while (index < polylineLength) {
            long timeOffset;
            double indexOffset, latOffset, lngOffset;
            double decodedDimension[];

            // assign index and latPart
            decodedDimension = getDecodedDimensionFromPolyline(polyline, index);
            indexOffset = decodedDimension[0];
            latOffset = decodedDimension[1];
            index = (int) indexOffset;
            latPart = latPart + latOffset;

            // assign index and lngPart
            decodedDimension = getDecodedDimensionFromPolyline(polyline, index);
            indexOffset = decodedDimension[0];
            lngOffset = decodedDimension[1];
            index = (int) indexOffset;
            lngPart = lngPart + lngOffset;

            // assign index and timePart
            decodedDimension = getDecodedDimensionFromPolyline(polyline, index);
            indexOffset = decodedDimension[0];
            timeOffset = (long) decodedDimension[1];
            index = (int) indexOffset;
            timePart = timePart + timeOffset;

            gpxLogs.add(getFormattedDimensions(latPart, lngPart, timePart));
        }

        return gpxLogs;
    }

    // returns locations [[lat, lng] ..] till timestamp from decoded polyline
    public static List<GPXLog> getLocationsTillTime(List<GPXLog> decodedPolyline, Date queryTime) {
        if (queryTime == null || decodedPolyline == null || decodedPolyline.size() == 0) {
            return new ArrayList<>();
        }

        List<GPXLog> locationsElapsed = new ArrayList<>();
        Date startTime = decodedPolyline.get(0).getDate();

        if ((queryTime.getTime() - startTime.getTime()) <= 0) {
            // queryTime is before start time
            locationsElapsed.add(new GPXLog(decodedPolyline.get(0).getLat(), decodedPolyline.get(0).getLng(),
                    decodedPolyline.get(0).getDate()));
            return locationsElapsed;
        }

        int decodedLength = decodedPolyline.size();
        List<GPXLog> currentPair = new ArrayList<>();

        for (int index = 0; index < decodedLength; index++) {
            currentPair.add(decodedPolyline.get(index));

            if (currentPair.size() == 2) {
                startTime = currentPair.get(0).getDate();
                Date endTime = currentPair.get(1).getDate();

                if ((queryTime.getTime() - startTime.getTime()) > 0 && (queryTime.getTime() - endTime.getTime()) <= 0) {
                    // location is in between the current pair
                    GPXLog midLocation = getMidLocation(currentPair.get(0), currentPair.get(1), queryTime);
                    locationsElapsed.add(midLocation);
                    return locationsElapsed;
                } else {
                    // remove first element from current pair
                    currentPair.remove(0);
                }
            }

            locationsElapsed.add(new GPXLog(currentPair.get(0).getLat(), currentPair.get(0).getLng(),
                    currentPair.get(0).getDate()));
        }

        return locationsElapsed;
    }

    // helper method for decoding
    public static double[] getDecodedDimensionFromPolyline(String polyline, int index) {
        // Method to decode one dimension of the polyline
        long result = 1, shift = 0;

        while (true) {
            int polylineChar = ((int) polyline.charAt(index)) - 63 - 1;
            index++;
            result += polylineChar << shift;
            shift += 5;

            if (polylineChar < 0x1f) {
                break;
            }
        }

        if ((result & 1) != 0) {
            return new double[]{index, ~result >> 1};
        } else {
            return new double[]{index, result >> 1};
        }
    }

    // Helper method for modifying format of decoded elements
    private static GPXLog getFormattedDimensions(double lat, double lng, long time) {
        return new GPXLog(lat / 100000.0, lng / 100000.0, new Date(time * 1000));
    }

    // Helper method to find mid location in a pair on a timestamp
    private static GPXLog getMidLocation(GPXLog startGPXLog, GPXLog endGPXLog, Date queryTime) {

        double timeSinceStart = queryTime.getTime() - startGPXLog.getDate().getTime(),
                intervalLength = endGPXLog.getDate().getTime() - startGPXLog.getDate().getTime();
        double ratio = timeSinceStart / intervalLength;

        double startLat = startGPXLog.getLat(), startLng = startGPXLog.getLng(),
                endLat = endGPXLog.getLat(), endLng = endGPXLog.getLng();

        return new GPXLog((startLat * (1 - ratio) + endLat * ratio), (startLng * (1 - ratio) + endLng * ratio),
                queryTime);
    }
}
