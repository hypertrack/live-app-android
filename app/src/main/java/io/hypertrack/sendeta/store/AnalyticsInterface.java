package io.hypertrack.sendeta.store;

/**
 * Created by piyush on 29/06/16.
 */
public interface AnalyticsInterface {

    // Signup Events
    void enteredCorrectOTP(boolean status, String errorMessage, int retries);
    void resendOTP(boolean status, String errorMessage);
    void enteredName(boolean status, String errorMessage);
    void uploadedProfilePhoto(boolean status, String errorMessage);

    // Add Destination Address Events
    void selectedAddress(int charactersTyped, boolean isFavorite);

    // Trip Events
    void startedTrip(boolean status, String errorMessage);

    void tappedShareIcon(boolean tripShared);
    void tappedNavigate();

    void tappedEndTrip(boolean status, String errorMessage);
}
