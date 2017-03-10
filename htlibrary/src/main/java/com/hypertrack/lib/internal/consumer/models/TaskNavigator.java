package com.hypertrack.lib.internal.consumer.models;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;
import com.hypertrack.lib.internal.consumer.utils.TimeAwarePolylineUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by suhas on 29/08/15.
 */
public class TaskNavigator {

    private static final String TAG = TaskNavigator.class.getSimpleName();
    public static final int NAVIGATION_INTERVAL = 2300;

    private String taskID;
    private Handler mHandler;
    private GPXLogList gpxLogList;
    private TaskNavigatorCallback callback;

    private Handler navigationHandler = new Handler();

    private GPXLog lastLog;

    public TaskNavigator(String taskID, TaskNavigatorCallback callback, Handler mHandler) {
        this.taskID = taskID;
        this.mHandler = mHandler;
        this.gpxLogList = new GPXLogList();
        this.callback = callback;
    }

    public String getTaskID() {
        return taskID;
    }

    public void processTimeAwarePolyline(HTTask task) {
        List<GPXLog> timedLocations = TimeAwarePolylineUtils.getDecodedPolyline(task.getTimeAwarePolyline());

        if (timedLocations.size() > 0) {
            gpxLogList.setTimedLocations(timedLocations);

            // Get updated Navigation Logs
            final Date animStartDate = timedLocations.get(timedLocations.size() - 1).getDate();
            final List<GPXLog> gpxLogsForNavigation = gpxLogsForNavigation(animStartDate.getTime());
            if (gpxLogsForNavigation.size() != 0) {

                // Stop current NavigationHandler, if any
                stopNavigationToNextLog();

                // Start Navigation for the first log
                final int timeForNavigation = timeIntervalForGPXLogs(gpxLogsForNavigation);
                navigateToNextLog(gpxLogsForNavigation, timeForNavigation, 0);
            } else {
                HTLog.i(TAG, "No GPXlogs available for current navigation");
            }

        } else {
            HTLog.i(TAG, "No GPXlogs available");
        }
    }

    public void navigateToNextLog(List<GPXLog> gpxLogsForNavigation, int timeForNavigation, int index) {
        if (index < gpxLogsForNavigation.size()) {
            GPXLog gpxLog = gpxLogsForNavigation.get(index);
            lastLog = gpxLog;

            if (callback != null && isValidGPXLog(gpxLog)) {
                callAnimation(gpxLog, timeForNavigation);

            } else {
                HTLog.e(TAG, "Error occurred while navigateToLogs: GPXLog is not valid!");
            }

            // Schedule job for next navigation
            scheduleNavigationToNextLog(gpxLogsForNavigation, index + 1, timeForNavigation);
        }
    }

    private void scheduleNavigationToNextLog(final List<GPXLog> gpxLogsForNavigation,
                                             final int indexForNavigation, final int timeForNavigation) {
        Runnable navigationRunnable = new Runnable() {
            @Override
            public void run() {
                navigateToNextLog(gpxLogsForNavigation, timeForNavigation, indexForNavigation);
            }
        };
        navigationHandler.postDelayed(navigationRunnable, timeForNavigation);
    }

    /**
     * Method to start Animation to a location in give timeDuration
     * @param location
     * @param timeDuration
     */
    private void callAnimation(final GPXLog location, final long timeDuration) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                logPointDisplayed(location);
                callback.moveToLocationWithTimeInterval(taskID,
                        new LatLng(location.getLat(), location.getLng()), timeDuration);
            }
        });
    }

    private void logPointDisplayed(GPXLog gpxLog) {
        String server = "Production";
        if (BuildConfig.DEBUG) {
            server = "Staging";
        }

        HTLog.a("Point displayed: Task " + taskID + ", Display time " + DateTimeUtility.getCurrentTime()
                + ", Recorded at " + DateTimeUtility.getFormattedTime(gpxLog.getDate()) +
                ", Agent Android, Server " + server);
    }

    /**
     * Method to compute GPXLogs for current navigation
     * @return
     */
    private List<GPXLog> gpxLogsForNavigation(long startTime) {
        Date offset = lastLog != null ? lastLog.getDate() : new Date(startTime - NAVIGATION_INTERVAL);
        return gpxLogList.getGPXLogs(offset);
    }

    /**
     * Method to compute timeInterval for each animation to be completed in NAVIGATION_INTERVAL
     * @param gpxLogsForNavigation
     * @return
     */
    private int timeIntervalForGPXLogs(List<GPXLog> gpxLogsForNavigation) {
        int count = gpxLogsForNavigation.size();
        return count > 2 ? NAVIGATION_INTERVAL / (count - 1) : NAVIGATION_INTERVAL;
    }

    /**
     * Method to check if current GPXLog is valid or not
     * @param gpxLog
     * @return
     */
    private boolean isValidGPXLog(GPXLog gpxLog) {
        return lastLog == null || lastLog.getDate() != null || (gpxLog.getDate().getTime() - lastLog.getDate().getTime() >= 0);
    }

    /**
     * Method to stop Navigation for next log
     */
    public void stopPollers() {
        stopNavigationToNextLog();
    }

    private void stopNavigationToNextLog() {
        if (navigationHandler != null)
            navigationHandler.removeCallbacksAndMessages(null);
    }
}
