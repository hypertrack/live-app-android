package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.transmitter.models.ActivitySegment;
import com.hypertrack.lib.models.HyperTrackLocation;

import java.io.Serializable;

import io.hypertrack.sendeta.BuildConfig;

/**
 * Created by Aman on 14/09/17.
 */

public class ActivityFeedbackModel implements Serializable {

    //Activity Feedback Types
    public static final String ACTIVITY_ACCURATE = "accurate";
    public static final String ACTIVITY_EDITED = "edited";
    public static final String ACTIVITY_DELETED = "deleted";
    public static final String ACTIVITY_ADDED = "added";

    @SerializedName("user_id")
    private String userID;

    @SerializedName("lookup_id")
    private String lookupId;

    @SerializedName("feedback_type")
    private String feedbackType;

    @SerializedName("user_comments")
    private String userComments;

    @SerializedName("app_version")
    private String appVersion;

    @SerializedName("sdk_version")
    private String sdkVersion;

    @SerializedName("edited_type")
    private String editedType;

    @SerializedName("edited_start_location")
    private HyperTrackLocation editedStartLocation;

    @SerializedName("edited_end_location")
    private HyperTrackLocation editedEndLocation;

    @SerializedName("edited_distance")
    private int editedDistance;

    @SerializedName("edited_at_stop")
    private boolean editedAtStop;

    @SerializedName("edited_num_of_steps")
    private int editedNumOfSteps;

    @SerializedName("edited_started_at")
    private String editedStartedAt;

    @SerializedName("edited_ended_at")
    private String editedEndedAt;

    @SerializedName("is_start_location_accurate")
    private boolean isStartLocationAccurate;

    @SerializedName("is_end_location_accurate")
    private boolean isEndLocationAccurate;

    @SerializedName("is_at_stop_accurate")
    private boolean isAtStopAccurate;

    @SerializedName("is_num_of_steps_accurate")
    private boolean isNumOfStepsAccurate;

    @SerializedName("is_started_at_accurate")
    private boolean isStartedAtAccurate;

    @SerializedName("is_ended_at_accurate")
    private boolean isEndedAtAccurate;

    @SerializedName("is_distance_accurate")
    private boolean isDistanceAccurate;

    @SerializedName("is_type_accurate")
    private boolean isTypeAccurate;

    public ActivityFeedbackModel() {
        userID = HyperTrack.getUserId();
        appVersion = BuildConfig.VERSION_NAME;
        sdkVersion = HyperTrack.getSDKVersion();
    }

    public ActivityFeedbackModel(ActivitySegment activitySegment) {
        this();
        lookupId = activitySegment.getLookupId();
    }

    public ActivityFeedbackModel(ActivitySegment activitySegment, String feedbackType) {
        this(activitySegment);
        this.feedbackType = feedbackType;
    }

    public String getLookupId() {
        return lookupId;
    }

    public void setLookupId(String lookupId) {
        this.lookupId = lookupId;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getUserComments() {
        return userComments;
    }

    public void setUserComments(String userComments) {
        this.userComments = userComments;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getEditedType() {
        return editedType;
    }

    public void setEditedType(String editedType) {
        this.editedType = editedType;
    }

    public HyperTrackLocation getEditedStartLocation() {
        return editedStartLocation;
    }

    public void setEditedStartLocation(HyperTrackLocation editedStartLocation) {
        this.editedStartLocation = editedStartLocation;
    }

    public HyperTrackLocation getEditedEndLocation() {
        return editedEndLocation;
    }

    public void setEditedEndLocation(HyperTrackLocation editedEndLocation) {
        this.editedEndLocation = editedEndLocation;
    }

    public int getEditedDistance() {
        return editedDistance;
    }

    public void setEditedDistance(int editedDistance) {
        this.editedDistance = editedDistance;
    }

    public boolean isEditedAtStop() {
        return editedAtStop;
    }

    public void setEditedAtStop(boolean editedAtStop) {
        this.editedAtStop = editedAtStop;
    }

    public int getEditedNumOfSteps() {
        return editedNumOfSteps;
    }

    public void setEditedNumOfSteps(int editedNumOfSteps) {
        this.editedNumOfSteps = editedNumOfSteps;
    }

    public String getEditedStartedAt() {
        return editedStartedAt;
    }

    public void setEditedStartedAt(String editedStartedAt) {
        this.editedStartedAt = editedStartedAt;
    }

    public String getEditedEndedAt() {
        return editedEndedAt;
    }

    public void setEditedEndedAt(String editedEndedAt) {
        this.editedEndedAt = editedEndedAt;
    }

    public boolean isStartLocationAccurate() {
        return isStartLocationAccurate;
    }

    public void setStartLocationAccurate(boolean startLocationAccurate) {
        isStartLocationAccurate = startLocationAccurate;
    }

    public boolean isEndLocationAccurate() {
        return isEndLocationAccurate;
    }

    public void setEndLocationAccurate(boolean endLocationAccurate) {
        isEndLocationAccurate = endLocationAccurate;
    }

    public boolean isAtStopAccurate() {
        return isAtStopAccurate;
    }

    public void setAtStopAccurate(boolean atStopAccurate) {
        isAtStopAccurate = atStopAccurate;
    }

    public boolean isNumOfStepsAccurate() {
        return isNumOfStepsAccurate;
    }

    public void setNumOfStepsAccurate(boolean numOfStepsAccurate) {
        isNumOfStepsAccurate = numOfStepsAccurate;
    }

    public boolean isStartedAtAccurate() {
        return isStartedAtAccurate;
    }

    public void setStartedAtAccurate(boolean startedAtAccurate) {
        isStartedAtAccurate = startedAtAccurate;
    }

    public boolean isEndedAtAccurate() {
        return isEndedAtAccurate;
    }

    public void setEndedAtAccurate(boolean endedAtAccurate) {
        isEndedAtAccurate = endedAtAccurate;
    }

    public boolean isDistanceAccurate() {
        return isDistanceAccurate;
    }

    public void setDistanceAccurate(boolean distanceAccurate) {
        isDistanceAccurate = distanceAccurate;
    }

    public boolean isTypeAccurate() {
        return isTypeAccurate;
    }

    public void setTypeAccurate(boolean typeAccurate) {
        isTypeAccurate = typeAccurate;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "ActivityFeedbackModel{" +
                "userID='" + userID + '\'' +
                ", lookupId='" + lookupId + '\'' +
                ", feedbackType='" + feedbackType + '\'' +
                ", userComments='" + userComments + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", sdkVersion='" + sdkVersion + '\'' +
                ", editedType='" + editedType + '\'' +
                ", editedStartLocation=" + editedStartLocation +
                ", editedEndLocation=" + editedEndLocation +
                ", editedDistance=" + editedDistance +
                ", editedAtStop=" + editedAtStop +
                ", editedNumOfSteps=" + editedNumOfSteps +
                ", editedStartedAt=" + editedStartedAt +
                ", editedEndedAt=" + editedEndedAt +
                ", isStartLocationAccurate=" + isStartLocationAccurate +
                ", isEndLocationAccurate=" + isEndLocationAccurate +
                ", isAtStopAccurate=" + isAtStopAccurate +
                ", isNumOfStepsAccurate=" + isNumOfStepsAccurate +
                ", isStartedAtAccurate=" + isStartedAtAccurate +
                ", isEndedAtAccurate=" + isEndedAtAccurate +
                ", isDistanceAccurate=" + isDistanceAccurate +
                ", isTypeAccurate=" + isTypeAccurate +
                '}';
    }
}
