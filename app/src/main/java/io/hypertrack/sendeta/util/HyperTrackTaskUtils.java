package io.hypertrack.sendeta.util;

import android.content.Context;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.common.model.HTTaskDisplay;

/**
 * Created by piyush on 03/09/16.
 */
public class HyperTrackTaskUtils {

    public static Integer getTaskDisplayETA(HTTaskDisplay taskDisplay) {
        if (taskDisplay != null && !TextUtils.isEmpty(taskDisplay.getDurationRemaining())) {
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

    public static String getFormattedTimeString(Context context, Integer timeInMinutes) {
        if (context == null || timeInMinutes == null || timeInMinutes < 0)
            return "1 min";

        int days = timeInMinutes / (60 * 24);
        int remainder = timeInMinutes - (days * 60 * 24);
        int hours = remainder / 60;
        remainder = remainder - (hours * 60);
        int mins = remainder;

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days)
                    .append(" ")
                    .append(context.getResources().getQuantityString(io.hypertrack.lib.consumer.R.plurals.day_text, days))
                    .append(" ");
        }

        if (hours > 0) {
            builder.append(hours)
                    .append(" ")
                    .append(context.getResources().getQuantityString(io.hypertrack.lib.consumer.R.plurals.hour_text, hours))
                    .append(" ");
        }

        if (mins > 1) {
            builder.append(mins)
                    .append(" ");
        } else {
            builder.append("1 ");
        }

        builder.append(context.getResources().getQuantityString(io.hypertrack.lib.consumer.R.plurals.minute_text, mins));

        return builder.toString();
    }

    public static String getFormattedTaskDurationAndDistance(Context context, HTTask task) {
        StringBuilder duration = new StringBuilder();
        if (task.getDurationInMinutes() != null) {
            duration.append(getFormattedTimeString(context, task.getDurationInMinutes()));
        }

        if (task.getDistanceInKMS() != null) {
            if (duration.length() != 0) {
                duration.append(" • ");
            }
            duration.append(String.format("%.02f KMs", task.getDistanceInKMS()));
        }

        return duration.toString();
    }

    public static String getTaskDateString(HTTask task) {
        if (task == null || task.getCompletionTime() == null) {
            return null;
        }

        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        return dateFormat.format(task.getCompletionTime());
    }

    public static String getTaskLocationString(HTTask task) {
        if (task == null || task.getStartLocation() == null || task.getStartLocation().getCoordinates() == null)
            return null;

        double[] coordinate = task.getStartLocation().getCoordinates();

        if (coordinate[0] == 0.0 || coordinate[1] == 0.0)
            return null;

        return String.format("%.1f° N, %.1f° E", coordinate[1], coordinate[0]);
    }

    public static Integer getTaskDisplayStatus(HTTaskDisplay taskDisplay) {
        if (taskDisplay == null || TextUtils.isEmpty(taskDisplay.getStatus()))
            return null;

        String taskDisplayStatus = taskDisplay.getStatus();

        switch (taskDisplayStatus) {
            case HTTask.TASK_STATUS_NOT_STARTED:
                return io.hypertrack.lib.consumer.R.string.task_status_not_started;
            case HTTask.TASK_STATUS_DISPATCHING:
                return io.hypertrack.lib.consumer.R.string.task_status_dispatching;
            case HTTask.TASK_STATUS_DRIVER_ON_THE_WAY:
                return io.hypertrack.lib.consumer.R.string.task_status_driver_on_the_way;
            case HTTask.TASK_STATUS_DRIVER_ARRIVING:
                return io.hypertrack.lib.consumer.R.string.task_status_driver_arriving;
            case HTTask.TASK_STATUS_DRIVER_ARRIVED:
                return io.hypertrack.lib.consumer.R.string.task_status_driver_arrived;
            case HTTask.TASK_STATUS_COMPLETED:
                return io.hypertrack.lib.consumer.R.string.task_status_completed;
            case HTTask.TASK_STATUS_CANCELED:
                return io.hypertrack.lib.consumer.R.string.task_status_canceled;
            case HTTask.TASK_STATUS_ABORTED:
                return io.hypertrack.lib.consumer.R.string.task_status_aborted;
            case HTTask.TASK_STATUS_SUSPENDED:
                return io.hypertrack.lib.consumer.R.string.task_status_suspended;
            case HTTask.TASK_STATUS_NO_LOCATION:
                return io.hypertrack.lib.consumer.R.string.task_status_no_location;
            case HTTask.TASK_STATUS_LOCATION_LOST:
                return io.hypertrack.lib.consumer.R.string.task_status_location_lost;
            case HTTask.TASK_STATUS_CONNECTION_LOST:
                return io.hypertrack.lib.consumer.R.string.task_status_connection_lost;
            default:
                return null;
        }
    }
}
