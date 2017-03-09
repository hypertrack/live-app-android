package com.hypertrack.lib;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.UserPreferences;
import com.hypertrack.lib.internal.transmitter.utils.Constants;

/**
 * Created by ulhas on 15/04/16.
 */
public class ActivityRecognitionService extends IntentService {

    private static final String TAG = "ActivityRecognService";

    public ActivityRecognitionService() {
        super(TAG);
    }

    private static String getActivityName(DetectedActivity activity) {
        String activityName = Constants.USER_ACTIVITY_UNKNOWN;

        switch (activity.getType()) {
            case DetectedActivity.STILL:
                activityName = Constants.USER_ACTIVITY_STATIONARY;
                break;

            case DetectedActivity.ON_BICYCLE:
                activityName = Constants.USER_ACTIVITY_CYCLING;
                break;

            case DetectedActivity.ON_FOOT:
            case DetectedActivity.WALKING:
                activityName = Constants.USER_ACTIVITY_WALKING;
                break;

            case DetectedActivity.RUNNING:
                activityName = Constants.USER_ACTIVITY_RUNNING;
                break;

            case DetectedActivity.IN_VEHICLE:
                activityName = Constants.USER_ACTIVITY_AUTOMOTIVE;
                break;

            default:
                break;
        }

        return activityName;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Check if User is tracking currently or not
        if (!HyperTrack.isTracking())
            return;

        if (ActivityRecognitionResult.hasResult(intent)) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            this.onUserActivityUpdated(mostProbableActivity, HyperTrackImpl.getInstance().userPreferences);
        } else {
            HTLog.e(TAG, "ActivityRecognitionResult has no result.");
        }
    }

    private void onUserActivityUpdated(DetectedActivity activity, UserPreferences userPreferences) {
        if (activity == null)
            return;

        String activityName = getActivityName(activity);

        // Check if updated UserActivity is UNKNOWN
        if (Constants.USER_ACTIVITY_UNKNOWN.equals(activityName))
            return;

        // Check if user activity was updated from the last time
        String lastRecordedActivityName = userPreferences.getLastRecordedActivityName();
        if (lastRecordedActivityName == null || !lastRecordedActivityName.equalsIgnoreCase(activityName)) {
            HyperTrackImpl.getInstance().transmitterClient.getServiceManager()
                    .onUserActivityChanged(activityName, activity.getConfidence());
        }
    }
}
