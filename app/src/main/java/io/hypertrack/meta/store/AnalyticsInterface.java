package io.hypertrack.meta.store;

import android.os.Bundle;

/**
 * Created by piyush on 29/06/16.
 */
public interface AnalyticsInterface {

    // Signup Events
    void enteredPhoneNumber(boolean status, String errorMessage);
    void enteredCorrectOTP(boolean status, String errorMessage, int retries);
    void resendOTP(boolean status, String errorMessage, int resendCount);
    void enteredName(boolean status, String errorMessage);
    void uploadedProfilePhoto(boolean status, String errorMessage);
    void completedProfileSetUp(boolean isNewUser);

    // Add Destination Address Events
//    void typedAddress();
    void selectedAddress(int charactersTyped, boolean isFavorite);

    // Trip Events
    void startedTrip();

    void tappedShareIcon(boolean sharedAgain);
    void tappedOnShareViaAnotherApp();
    void tappedOn3rdPartyApp(String appName);
    void tripSharedVia3rdPartyApp(String appName);
    void selectedContacts(int contactsCount, int sendETAContactsCount);
    void tappedSendToGroup(int contactsCount, int sendETAContactsCount);
    void textMessageSent(int contactsSentCount);

    void tappedNavigate();
    void tappedFavorite();

    void tappedEndTrip();

    // Profile Events
    void tappedProfile();
    void tappedEditProfile();
    void editedFirstName();
    void editedLastName();
    void uploadedPhotoViaPhotoEditor();
    void replacedPhotoViaPhotoEditor();

    // Home Favorite Events
    void addedHome();
    void editedHome();
    void deletedHome();

    // Work Favorite Events
    void addedWork();
    void editedWork();
    void deletedWork();

    // Other Favorite Events
    void addedOtherFavorite(int favoritesCount);
    void editedOtherFavorite();
    void deletedOtherFavorite();
}
