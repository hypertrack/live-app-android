package io.hypertrack.meta.store;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

import io.hypertrack.meta.util.AnalyticsConstants.Event;
import io.hypertrack.meta.util.AnalyticsConstants.EventParam;

/**
 * Created by piyush on 29/06/16.
 */
public class AnalyticsStore implements AnalyticsInterface {

    private static AnalyticsStore analyticsStore;
    private AppEventsLogger fbAnalyticsLogger;

    private AnalyticsStore(){}

    private AnalyticsStore(Application mApplication) {
        Context context = mApplication.getApplicationContext();
        this.fbAnalyticsLogger = AppEventsLogger.newLogger(context, context.getPackageName());
    }

    public static AnalyticsStore getLogger() {
        return analyticsStore;
    }

    public static synchronized void init(Application application) {

        // Initialize FB SDK & Activate FB Tracking
        FacebookSdk.sdkInitialize(application.getApplicationContext());
        AppEventsLogger.activateApp(application, application.getPackageName());

        if (analyticsStore == null) {
            analyticsStore = new AnalyticsStore(application);
        }
    }

    private void logEvent(String eventName) {
        try {
            fbAnalyticsLogger.logEvent(eventName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logEvent(String eventName, Bundle eventParams) {
        try {
            fbAnalyticsLogger.logEvent(eventName, eventParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Signup Events
    @Override
    public void enteredPhoneNumber(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.ENTERED_NAME, eventParams);
    }

    @Override
    public void enteredCorrectOTP(boolean status, String errorMessage, int retries) {
        Bundle eventParams = getBundle(status, errorMessage);
        eventParams.putInt(EventParam.NO_OF_ATTEMPT, retries);

        logEvent(Event.ENTERED_CORRECT_OTP, eventParams);
    }

    @Override
    public void resendOTP(boolean status, String errorMessage, int resendCount) {
        Bundle eventParams = getBundle(status, errorMessage);
        eventParams.putInt(EventParam.NO_OF_ATTEMPT, resendCount);

        logEvent(Event.RESEND_OTP, eventParams);
    }

    @Override
    public void enteredName(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.ENTERED_NAME, eventParams);
    }

    @Override
    public void uploadedProfilePhoto(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.UPLOADED_PROFILE_PHOTO, eventParams);
    }

    @Override
    public void completedProfileSetUp(boolean isExistingUser) {
        Bundle eventParams = getBundle();
        eventParams.putBoolean(EventParam.IS_EXISTING_USER, isExistingUser);

        // Using FB Event for Completed Profile Set-Up
        logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, eventParams);
    }

    // Add Destination Address Events
//    @Override
//    public void typedAddress() {
//        logEvent(Event.TYPED_AN_ADDRESS);
//    }

    @Override
    public void selectedAddress(int charactersTyped, boolean isFavorite) {
        Bundle eventParams = getBundle();
        eventParams.putBoolean(EventParam.IS_ADDRESS_FAVORITE, isFavorite);

        logEvent(Event.SELECTED_AN_ADDRESS, eventParams);
    }

    // Trip Events
    @Override
    public void startedTrip() {
        logEvent(Event.STARTED_A_TRIP);
    }

    @Override
    public void tappedShareIcon(boolean sharedCurrentTripBefore) {
        Bundle eventParams = getBundle();
        eventParams.putBoolean(EventParam.SHARED_CURRENT_TRIP_BEFORE, sharedCurrentTripBefore);

        logEvent(Event.TAPPED_SHARE_ICON, eventParams);
    }

    @Override
    public void tappedOnShareViaAnotherApp() {
        logEvent(Event.TAPPED_SHARE_VIA_ANOTHER_APP);
    }

    @Override
    public void tappedOn3rdPartyApp(String appName) {
        Bundle eventParams = getBundle();
        eventParams.putString(EventParam.SHARING_APP, appName);

        logEvent(Event.TAPPED_ON_3RD_PARTY_APP, eventParams);
    }

    @Override
    public void tripSharedVia3rdPartyApp(String appName) {
        Bundle eventParams = getBundle();
        eventParams.putString(EventParam.SHARING_APP, appName);

        logEvent(Event.TRIP_SHARED_VIA_3RD_PARTY_APP, eventParams);
    }

    @Override
    public void selectedContacts(int contactsCount, int sendETAContactsCount) {
        Bundle eventParams = getBundle();
        eventParams.putInt(EventParam.NO_OF_CONTACTS_SELECTED, contactsCount);
        eventParams.putInt(EventParam.NO_OF_SEND_ETA_CONTACTS_SELECTED, sendETAContactsCount);

        logEvent(Event.SELECTED_CONTACTS, eventParams);
    }

    @Override
    public void tappedSendToGroup(int contactsCount, int sendETAContactsCount) {
        Bundle eventParams = getBundle();
        eventParams.putInt(EventParam.NO_OF_CONTACTS_SELECTED, contactsCount);
        eventParams.putInt(EventParam.NO_OF_SEND_ETA_CONTACTS_SELECTED, sendETAContactsCount);

        logEvent(Event.TAPPED_SEND_TO_GROUP, eventParams);
    }

    @Override
    public void textMessageSent(int contactsSentCount) {
        Bundle eventParams = getBundle();
        eventParams.putInt(EventParam.NO_OF_CONTACTS_SENT, contactsSentCount);

        logEvent(Event.TEXT_MESSAGE_SENT, eventParams);
    }

    @Override
    public void tappedNavigate() {
        logEvent(Event.TAPPED_NAVIGATE_ICON);
    }

    @Override
    public void tappedFavorite() {
        logEvent(Event.TAPPED_FAVORITE_ICON);
    }

    @Override
    public void tappedEndTrip() {
        logEvent(Event.TAPPED_END_TRIP);
    }

    // Profile Events
    @Override
    public void tappedProfile() {
        logEvent(Event.TAPPED_ON_PROFILE);
    }

    @Override
    public void tappedEditProfile() {
        logEvent(Event.TAPPED_ON_EDIT_PROFILE);
    }

    @Override
    public void editedFirstName() {
        logEvent(Event.EDITED_FIRST_NAME);
    }

    @Override
    public void editedLastName() {
        logEvent(Event.EDITED_LAST_NAME);
    }

    @Override
    public void uploadedPhotoViaPhotoEditor() {
        logEvent(Event.UPLOADED_PHOTO_VIA_GALLERY);
    }

    @Override
    public void replacedPhotoViaPhotoEditor() {
        logEvent(Event.REPLACED_PHOTO_VIA_GALLERY);
    }

    // Home Favorite Events
    @Override
    public void addedHome() {
        logEvent(Event.ADDED_HOME);
    }

    @Override
    public void editedHome() {
        logEvent(Event.EDITED_HOME);
    }

    @Override
    public void deletedHome() {
        logEvent(Event.DELETED_HOME);
    }

    // Work Favorite Events
    @Override
    public void addedWork() {
        logEvent(Event.ADDED_WORK);
    }

    @Override
    public void editedWork() {
        logEvent(Event.EDITED_WORK);
    }

    @Override
    public void deletedWork() {
        logEvent(Event.DELETED_WORK);
    }

    // Other Favorite Events
    @Override
    public void addedOtherFavorite(int favoritesCount) {
        Bundle eventParams = getBundle();
        eventParams.putInt(EventParam.NO_OF_FAVORITES_ADDED, favoritesCount);

        logEvent(Event.ADDED_OTHER_FAVORITE, eventParams);
    }

    @Override
    public void editedOtherFavorite() {
        logEvent(Event.EDITED_OTHER_FAVORITE);
    }

    @Override
    public void deletedOtherFavorite() {
        logEvent(Event.DELETED_OTHER_FAVORITE);
    }

    private Bundle getBundle() {
        return new Bundle();
    }

    private Bundle getBundle(boolean status, String errorMessage) {
        Bundle eventParams = new Bundle();
        eventParams.putBoolean(EventParam.STATUS, status);
        eventParams.putString(EventParam.ERROR_MESSAGE, errorMessage != null ? errorMessage : "");

        return eventParams;
    }
}
