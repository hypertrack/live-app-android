package com.hypertrack.lib.internal.consumer.utils;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.models.HTAction;
import com.hypertrack.lib.internal.consumer.models.HTDisplay;

/**
 * Created by Aman on 4/03/2017.
 */
public class HTActionUtils {

    private static final String TAG = HTActionUtils.class.getSimpleName();

    public static Integer getActionDisplayETA(HTDisplay actionDisplay) {
        if (actionDisplay == null)
            return null;

        if (!TextUtils.isEmpty(actionDisplay.getDurationRemaining())) {
            Double etaInSeconds = Double.valueOf(actionDisplay.getDurationRemaining());

            if (etaInSeconds != null) {
                double etaInMinutes = Math.ceil((etaInSeconds / (float) 60));
                if (etaInMinutes < 1) {
                    etaInMinutes = 1;
                }

                return (int) etaInMinutes;
            }
        }

        return null;
    }
    // Action Status Methods
    public static String getActionStatus(HTAction action) {
        if (action == null)
            return null;

        return action.getStatus();
    }

    /*public static String getConnectionStatus(HTAction action) {
        if (action == null)
            return null;

        if (action.getConnectionStatus() != null) {
            return action.getConnectionStatus();
        }

        return null;
    }
*/
    public static String getActionDisplayStatus(HTAction action) {
        if (action == null)
            return null;

        if (action.getActionDisplay() != null) {
            return action.getActionDisplay().getStatusText();
        }

        return null;
    }


    public static String getActionDisplayStatus(HTDisplay actionDisplay) {
        if (actionDisplay == null || TextUtils.isEmpty(actionDisplay.getStatusText()))
            return null;
        return actionDisplay.getStatusText();
    }

    public static LatLng getExpectedPlaceLatLng(HTAction action) {
        if (action == null)
            return null;

        if (action.getExpectedPlace() != null) {
            if (action.getExpectedPlace().getLocation() != null) {
                return getLatLng(action.getExpectedPlace().getLocation());
            }
        }

        return null;
    }


    public static LatLng getCompletedPlaceLatLng(HTAction action) {
        if (action == null)
            return null;

        if (action.getCompletedPlace() != null) {
            if (action.getCompletedPlace().getLocation() != null) {
                return getLatLng(action.getCompletedPlace().getLocation());
            }

        }
        return null;
    }

    public static LatLng getStartPlaceLatLng(HTAction action) {
        if (action == null)
            return null;

        if (action.getStartPlace() != null) {
            if (action.getStartPlace().getLocation() != null) {
                return getLatLng(action.getStartPlace().getLocation());
            }

        }
        return null;
    }


    private static LatLng getLatLng(GeoJSONLocation geoJSONLocation) {

        byte LATITUDE_INDEX = 1;
        byte LONGITUDE_INDEX = 0;
        return new LatLng(geoJSONLocation.getCoordinates()[LATITUDE_INDEX], geoJSONLocation.getCoordinates()[LONGITUDE_INDEX]);
    }

    // Action Location Address methods
    public static String getStartedPlaceAddress(HTAction action) {
        if (action == null)
            return null;
        if (action.getStartPlace() != null && !TextUtils.isEmpty(action.getStartPlace().getAddress())) {
            return action.getStartPlace().getAddress();
        } else if (action.getStartPlace() != null && !TextUtils.isEmpty(action.getStartPlace().getDisplayString())) {
            return action.getStartPlace().getDisplayString();
        } else {
            return "";
        }
    }

    public static String getExpectedPlaceAddress(HTAction action) {
        if (action == null)
            return null;
        if (action.getExpectedPlace() != null && !TextUtils.isEmpty(action.getExpectedPlace().getAddress())) {
            return action.getExpectedPlace().getAddress();
        } else if (action.getExpectedPlace() != null && !TextUtils.isEmpty(action.getExpectedPlace().getDisplayString())) {
            return action.getExpectedPlace().getDisplayString();
        } else {
            return "";
        }
    }

    public static String getCompletedPlaceAddress(HTAction action) {
        if (action == null)
            return null;
        if (action.getCompletedPlace() != null && !TextUtils.isEmpty(action.getCompletedPlace().getAddress())) {
            return action.getCompletedPlace().getAddress();
        } else if (action.getCompletedPlace() != null && !TextUtils.isEmpty(action.getCompletedPlace().getDisplayString())) {
            return action.getCompletedPlace().getDisplayString();
        } else {
            return getExpectedPlaceAddress(action);
        }
    }

    // Action Metering Data Methods
    public static Integer getActionDurationInMinutes(HTAction action) {
        if (action == null)
            return null;

        if (action.getDurationInMinutes() != null) {
            return action.getDurationInMinutes();
        }

        return null;
    }

    public static String getActionDurationString(Context context, HTAction action) {
        if (action == null)
            return null;

        Integer actionDurationInMinutes = getActionDurationInMinutes(action);
        if (actionDurationInMinutes != null)
            return HTActionUtils.getFormattedTimeString(context, (double) (actionDurationInMinutes * 60));

        return null;
    }

    public static Double getActionDistanceInKMs(HTAction action) {
        if (action == null)
            return null;

        if (action.getDistanceInKMS() != null) {
            return action.getDistanceInKMS();
        }

        return null;
    }

    public static String getActionDistanceString(Context context, HTAction action) {
        if (action == null)
            return null;

        Double actionDistanceInKMs = getActionDistanceInKMs(action);
        if (actionDistanceInKMs != null)
            return HTActionUtils.getFormattedDistanceString(context, actionDistanceInKMs);

        return null;
    }

    public static String getActionMeteringString(Context context, HTAction action) {
        if (action == null)
            return null;

        String actionDuration = getActionDurationString(context, action);
        String actionDistance = getActionDistanceString(context, action);
        String actionMeteringText = null;

        try {
            if (!TextUtils.isEmpty(actionDistance)) {
                if (!TextUtils.isEmpty(actionDuration)) {
                    actionMeteringText = actionDuration + " • ";
                }

                actionMeteringText = (actionMeteringText != null ? actionMeteringText : "") + actionDistance;
            } else if (!TextUtils.isEmpty(actionDuration)) {
                actionMeteringText = actionDuration;
            }

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getActionMeteringString: " + e);
        }

        return actionMeteringText;
    }

    // Action Time & Distance Formatting Methods
    public static String getFormattedTimeString(Context context, Double timeInSeconds) {
        if (context == null || timeInSeconds == null || timeInSeconds < 0)
            return "1 min";

        int days = (int) (timeInSeconds / (3600 * 24));
        int remainder = (int) (timeInSeconds - (days * 3600 * 24));
        int hours = remainder / 3600;
        remainder = remainder - (hours * 3600);
        int mins = remainder / 60;

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days)
                    .append(" ")
                    .append(context.getResources().getQuantityString(com.hypertrack.lib.R.plurals.day_text, days))
                    .append(" ");
        }

        if (hours > 0) {
            builder.append(hours)
                    .append(" ")
                    .append(context.getResources().getQuantityString(com.hypertrack.lib.R.plurals.hour_text, hours))
                    .append(" ");
        }

        if (mins > 1) {
            builder.append(mins)
                    .append(" ");
        } else {
            builder.append("1 ");
        }

        builder.append(context.getResources().getQuantityString(com.hypertrack.lib.R.plurals.minute_text, mins));

        return builder.toString();
    }

    public static String getFormattedDistanceString(Context context, Double distanceInKMs) {
        if (context == null)
            return "0.00 KM";

        if (distanceInKMs == null || distanceInKMs <= 0.0)
            return "0.00 " + context.getString(com.hypertrack.lib.R.string.distance_km_text);

        return String.format("%.02f ", distanceInKMs) + context.getString(com.hypertrack.lib.R.string.distance_km_text);
    }
}
