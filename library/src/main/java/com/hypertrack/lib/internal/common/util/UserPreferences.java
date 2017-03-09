package com.hypertrack.lib.internal.common.util;

import android.support.annotation.NonNull;

import com.hypertrack.lib.internal.transmitter.models.HyperTrackStop;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;

import org.json.JSONObject;

/**
 * Created by piyush on 31/01/17.
 */
public interface UserPreferences {

    void clearUserData();
    void setSDKPlatform(String sdkPlatform);
    String getSDKPlatform();
    String getUserId();
    void setUserID(String userID);
    boolean isTracking();
    void setIsTracking(boolean isUserActive);
    boolean isFirstLocation();
    void setIsFirstLocation(boolean isFirstLocation);
    void clearIsFirstLocation();
    String getGcmToken();
    void setGcmToken(String gcmToken);
    String getFcmToken();
    void setFcmToken(String fcmToken);
    boolean isGcmTokenPushed(@NonNull JSONObject jsonObject);
    void setGcmTokenPushed(@NonNull JSONObject jsonObject);
    boolean isFcmTokenPushed(@NonNull JSONObject jsonObject);
    void setFcmTokenPushed(@NonNull JSONObject jsonObject);
    void setUserStop(HyperTrackStop hyperTrackStop);
    HyperTrackStop getUserStop();
    void clearUserStop();
    void setLastRecordedActivity(String activityName, int activityConfidence);
    String getLastRecordedActivityName();
    int getLastRecordedActivityConfidence();
    void clearLastRecordedActivity();
    void setLastRecordedLocation(HyperTrackLocation location);
    HyperTrackLocation getLastRecordedLocation();
    void clearLastRecordedLocation();
    void setLastPostToServerTime();
    Long getLastPostToServerTime();
    void clearLastPostToServerTime();
}