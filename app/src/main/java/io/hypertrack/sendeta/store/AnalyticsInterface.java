package io.hypertrack.sendeta.store;

/**
 * Created by piyush on 29/06/16.
 */
public interface AnalyticsInterface {

    // Signup Events
    void enteredPhoneNumber(boolean status, String errorMessage);
    void enteredCorrectOTP(boolean status, String errorMessage, int retries);
    void resendOTP(boolean status, String errorMessage);
    void enteredName(boolean status, String errorMessage);
    void uploadedProfilePhoto(boolean status, String errorMessage);
    void completedProfileSetUp(boolean isNewUser);

    // Add Destination Address Events
    void selectedAddress(int charactersTyped, boolean isFavorite);

    // Trip Events
    void startedTrip(boolean status, String errorMessage);

//    void tappedOnShareViaAnotherApp();
    void tappedOn3rdPartyApp(String appName);
    void tripSharedVia3rdPartyApp(String appName);
//    void selectedContacts(int contactsCount, int sendETAContactsCount);
//    void tappedSendToGroup(int contactsCount, int sendETAContactsCount);
//    void textMessageSent(int contactsSentCount);

    void tappedShareIcon(boolean tripShared);
    void tappedNavigate();
    void tappedFavorite();

    void tappedEndTrip(boolean status, String errorMessage);

    void autoTripEnded(boolean status, String errorMessage);

    // Profile Events
    void tappedProfile();
    void tappedEditProfile();
    void editedFirstName(boolean status, String errorMessage);
    void editedLastName(boolean status, String errorMessage);
    void uploadedPhotoViaPhotoEditor(boolean status, String errorMessage, String source);
    void replacedPhotoViaPhotoEditor(boolean status, String errorMessage, String source);

    // Home Favorite Events
    void addedHome(boolean status, String errorMessage);
    void editedHome(boolean status, String errorMessage);
    void deletedHome(boolean status, String errorMessage);

    // Work Favorite Events
    void addedWork(boolean status, String errorMessage);
    void editedWork(boolean status, String errorMessage);
    void deletedWork(boolean status, String errorMessage);

    // Other Favorite Events
    void addedOtherFavorite(boolean status, String errorMessage, int favoritesCount);
    void editedOtherFavorite(boolean status, String errorMessage);
    void deletedOtherFavorite(boolean status, String errorMessage);
}
