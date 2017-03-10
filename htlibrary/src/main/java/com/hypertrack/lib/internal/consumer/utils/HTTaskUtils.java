package com.hypertrack.lib.internal.consumer.utils;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.consumer.models.HTTask;
import com.hypertrack.lib.internal.consumer.models.HTTaskDisplay;

/**
 * Created by piyush on 23/09/16.
 */
public class HTTaskUtils {

    private static final String TAG = HTTaskUtils.class.getSimpleName();

    public static Integer getTaskDisplayETA(HTTaskDisplay taskDisplay) {
        if (taskDisplay == null)
            return null;

        if (!TextUtils.isEmpty(taskDisplay.getDurationRemaining())) {
            Double etaInSeconds = Double.valueOf(taskDisplay.getDurationRemaining());

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

    // Task Status Methods
    public static String getTaskStatus(HTTask task) {
        if (task == null)
            return null;

        return task.getStatus();
    }

    public static String getConnectionStatus(HTTask task) {
        if (task == null)
            return null;

        if (task.getConnectionStatus() != null) {
            return task.getConnectionStatus();
        }

        return null;
    }

    public static String getTaskDisplayStatus(HTTask task) {
        if (task == null)
            return null;

        if (task.getTaskDisplay() != null) {
            return task.getTaskDisplay().getStatus();
        }

        return null;
    }

    public static Integer getTaskDisplayStatus(HTTaskDisplay taskDisplay) {
        if (taskDisplay == null || TextUtils.isEmpty(taskDisplay.getStatus()))
            return null;

        String taskDisplayStatus = taskDisplay.getStatus();

        switch (taskDisplayStatus) {
            case HTTask.TASK_STATUS_NOT_STARTED:
                return com.hypertrack.lib.R.string.task_status_not_started;
            case HTTask.TASK_STATUS_DISPATCHING:
                return com.hypertrack.lib.R.string.task_status_dispatching;
            case HTTask.TASK_STATUS_USER_ON_THE_WAY:
                return com.hypertrack.lib.R.string.task_status_user_on_the_way;
            case HTTask.TASK_STATUS_USER_ARRIVING:
                return com.hypertrack.lib.R.string.task_status_user_arriving;
            case HTTask.TASK_STATUS_USER_ARRIVED:
                return com.hypertrack.lib.R.string.task_status_user_arrived;
            case HTTask.TASK_STATUS_COMPLETED:
                return com.hypertrack.lib.R.string.task_status_completed;
            case HTTask.TASK_STATUS_CANCELED:
                return com.hypertrack.lib.R.string.task_status_canceled;
            case HTTask.TASK_STATUS_ABORTED:
                return com.hypertrack.lib.R.string.task_status_aborted;
            case HTTask.TASK_STATUS_SUSPENDED:
                return com.hypertrack.lib.R.string.task_status_suspended;
            case HTTask.TASK_STATUS_NO_LOCATION:
                return com.hypertrack.lib.R.string.task_status_no_location;
            case HTTask.TASK_STATUS_LOCATION_LOST:
                return com.hypertrack.lib.R.string.task_status_location_lost;
            case HTTask.TASK_STATUS_CONNECTION_LOST:
                return com.hypertrack.lib.R.string.task_status_connection_lost;
            default:
                return null;
        }
    }

    // Task Location LatLng methods
    public static LatLng getStartLatLng(HTTask task) {
        if (task == null)
            return null;

        if (task.getStartLocation() != null) {
            return getLatLng(task.getStartLocation());
        }

        return null;
    }

    public static LatLng getDestinationLatLng(HTTask task) {
        if (task == null)
            return null;

        if (task.getDestination() != null) {
            if (task.getDestination().getLocation() != null) {
                return getLatLng(task.getDestination().getLocation());
            }
        } else if (task.getHub() != null) {
            if (task.getHub().getLocation() != null) {
                return getLatLng(task.getHub().getLocation());
            }
        }

        return null;
    }

    public static LatLng getCompletionLatLng(HTTask task) {
        if (task == null)
            return null;

        if (task.getCompletionLocation() != null) {
            return getLatLng(task.getCompletionLocation());
        } else {
            return getDestinationLatLng(task);
        }
    }

    private static LatLng getLatLng(GeoJSONLocation geoJSONLocation) {

        byte LATITUDE_INDEX = 1;
        byte LONGITUDE_INDEX = 0;
        return new LatLng(geoJSONLocation.getCoordinates()[LATITUDE_INDEX], geoJSONLocation.getCoordinates()[LONGITUDE_INDEX]);
    }

    // Task Location Address methods
    public static String getStartAddress(HTTask task) {
        if (task == null)
            return null;

        if (!TextUtils.isEmpty(task.getStartAddress())) {
            return task.getStartAddress();
        } else if (task.getStartLocation() != null && !TextUtils.isEmpty(task.getStartLocation().getDisplayString())) {
            return task.getStartLocation().getDisplayString();
        }

        return null;
    }

    public static String getDestinationAddress(HTTask task) {
        if (task == null)
            return null;

        if (task.getDestination() != null) {
            if (task.getDestination().getAddress() != null) {
                return task.getDestination().getAddress();
            }
        } else if (task.getHub() != null) {
            if (task.getHub().getAddress() != null) {
                return task.getHub().getAddress();
            }
        }

        return null;
    }

    public static String getCompletionAddress(HTTask task) {
        if (task == null)
            return null;

        if (!TextUtils.isEmpty(task.getCompletionAddress())) {
            return task.getCompletionAddress();
        } else if (task.getDestination() != null && !TextUtils.isEmpty(task.getDestination().getDisplayString())) {
            return task.getDestination().getDisplayString();
        } else {
            return getDestinationAddress(task);
        }
    }

    // Task Metering Data Methods
    public static Integer getTaskDurationInMinutes(HTTask task) {
        if (task == null)
            return null;

        if (task.getDurationInMinutes() != null) {
            return task.getDurationInMinutes();
        }

        return null;
    }

    public static String getTaskDurationString(Context context, HTTask task) {
        if (task == null)
            return null;

        Integer taskDurationInMinutes = getTaskDurationInMinutes(task);
        if (taskDurationInMinutes != null)
            return HTTaskUtils.getFormattedTimeString(context, (double) (taskDurationInMinutes * 60));

        return null;
    }

    public static Double getTaskDistanceInKMs(HTTask task) {
        if (task == null)
            return null;

        if (task.getDistanceInKMS() != null) {
            return task.getDistanceInKMS();
        }

        return null;
    }

    public static String getTaskDistanceString(Context context, HTTask task) {
        if (task == null)
            return null;

        Double taskDistanceInKMs = getTaskDistanceInKMs(task);
        if (taskDistanceInKMs != null)
            return HTTaskUtils.getFormattedDistanceString(context, taskDistanceInKMs);

        return null;
    }

    public static String getTaskMeteringString(Context context, HTTask task) {
        if (task == null)
            return null;

        String taskDuration = getTaskDurationString(context, task);
        String taskDistance = getTaskDistanceString(context, task);
        String taskMeteringText = null;

        try {
            if (!TextUtils.isEmpty(taskDistance)) {
                if (!TextUtils.isEmpty(taskDuration)) {
                    taskMeteringText = taskDuration + " • ";
                }

                taskMeteringText = (taskMeteringText != null ? taskMeteringText : "") + taskDistance;
            }

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getTaskMeteringString: " + e);
        }

        return taskMeteringText;
    }

    // Task Time & Distance Formatting Methods
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
