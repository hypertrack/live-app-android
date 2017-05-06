package io.hypertrack.sendeta.store;

/**
 * Created by piyush on 29/06/16.
 */
public interface AnalyticsInterface {

    // Signup Events
    void enteredName(boolean status, String errorMessage);
    void uploadedProfilePhoto(boolean status, String errorMessage);

    // Location Sharing Events
    void sharedLiveLocation(boolean status, String errorMessage);

    void tappedShareIcon(boolean tripShared);
    void tappedNavigate();

    void tappedStopSharing(boolean status, String errorMessage);
}
