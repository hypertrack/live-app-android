package io.hypertrack.sendeta.store;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

import java.math.BigDecimal;
import java.util.Currency;

import io.hypertrack.sendeta.util.AnalyticsConstants.Event;
import io.hypertrack.sendeta.util.AnalyticsConstants.EventParam;

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
        AppEventsLogger.activateApp(application);

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

    private void logPurchaseEvent() {
        try {
            fbAnalyticsLogger.logPurchase(BigDecimal.valueOf(1.0), Currency.getInstance("USD"));
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
        eventParams.putString(EventParam.NO_OF_ATTEMPT, String.valueOf(retries));

        logEvent(Event.ENTERED_CORRECT_OTP, eventParams);
    }

    @Override
    public void resendOTP(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

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
        eventParams.putString(EventParam.IS_EXISTING_USER, String.valueOf(isExistingUser));

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
        eventParams.putString(EventParam.IS_ADDRESS_FAVORITE, String.valueOf(isFavorite));

        logEvent(Event.SELECTED_AN_ADDRESS, eventParams);
    }

    // Trip Events
    @Override
    public void startedTrip(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.STARTED_A_TRIP, eventParams);
        logPurchaseEvent();
    }

    @Override
    public void tappedShareIcon(boolean tripShared) {
        Bundle eventParams = getBundle();
        eventParams.putString(EventParam.SHARED_CURRENT_TRIP_BEFORE, String.valueOf(tripShared));

        logEvent(Event.TAPPED_SHARE_ICON, eventParams);
    }

//    @Override
//    public void tappedOnShareViaAnotherApp() {
//        logEvent(Event.TAPPED_SHARE_VIA_ANOTHER_APP);
//    }

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

//    @Override
//    public void selectedContacts(int contactsCount, int sendETAContactsCount) {
//        Bundle eventParams = getBundle();
//        eventParams.putString(EventParam.NO_OF_CONTACTS_SELECTED, String.valueOf(contactsCount));
//        eventParams.putString(EventParam.NO_OF_SEND_ETA_CONTACTS_SELECTED, String.valueOf(sendETAContactsCount));
//
//        logEvent(Event.SELECTED_CONTACTS, eventParams);
//    }

//    @Override
//    public void tappedSendToGroup(int contactsCount, int sendETAContactsCount) {
//        Bundle eventParams = getBundle();
//        eventParams.putString(EventParam.NO_OF_CONTACTS_SELECTED, String.valueOf(contactsCount));
//        eventParams.putString(EventParam.NO_OF_SEND_ETA_CONTACTS_SELECTED, String.valueOf(sendETAContactsCount));
//
//        logEvent(Event.TAPPED_SEND_TO_GROUP, eventParams);
//    }

//    @Override
//    public void textMessageSent(int contactsSentCount) {
//        Bundle eventParams = getBundle();
//        eventParams.putString(EventParam.NO_OF_CONTACTS_SENT, String.valueOf(contactsSentCount));
//
//        logEvent(Event.TEXT_MESSAGE_SENT, eventParams);
//    }

    @Override
    public void tappedNavigate() {
        logEvent(Event.TAPPED_NAVIGATE_ICON);
    }

    @Override
    public void tappedFavorite() {
        logEvent(Event.TAPPED_FAVORITE_ICON);
    }

    @Override
    public void tappedEndTrip(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.TAPPED_END_TRIP, eventParams);
    }

    @Override
    public void autoTripEnded(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.AUTO_END_TRIP, eventParams);
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
    public void editedFirstName(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.EDITED_FIRST_NAME, eventParams);
    }

    @Override
    public void editedLastName(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.EDITED_LAST_NAME, eventParams);
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
    public void addedHome(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.ADDED_HOME, eventParams);
    }

    @Override
    public void editedHome(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.EDITED_HOME, eventParams);
    }

    @Override
    public void deletedHome(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.DELETED_HOME, eventParams);
    }

    // Work Favorite Events
    @Override
    public void addedWork(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.ADDED_WORK, eventParams);
    }

    @Override
    public void editedWork(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.EDITED_WORK, eventParams);
    }

    @Override
    public void deletedWork(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.DELETED_WORK, eventParams);
    }

    // Other Favorite Events
    @Override
    public void addedOtherFavorite(boolean status, String errorMessage, int favoritesCount) {
        Bundle eventParams = getBundle(status, errorMessage);
        eventParams.putString(EventParam.NO_OF_FAVORITES_ADDED, String.valueOf(favoritesCount));

        logEvent(Event.ADDED_OTHER_FAVORITE, eventParams);
    }

    @Override
    public void editedOtherFavorite(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.EDITED_OTHER_FAVORITE, eventParams);
    }

    @Override
    public void deletedOtherFavorite(boolean status, String errorMessage) {
        Bundle eventParams = getBundle(status, errorMessage);

        logEvent(Event.DELETED_OTHER_FAVORITE, eventParams);
    }

    /**
     * Method to get eventParams Bundle object
     *
     * @return
     */
    private Bundle getBundle() {
        return new Bundle();
    }

    /**
     * Method to get eventParams Bundle object
     *
     * @param status        Flag to denote the Success/Failure Event
     * @param errorMessage  Message displayed to User in case of failure
     * @return
     */
    private Bundle getBundle(boolean status, String errorMessage) {
        Bundle eventParams = new Bundle();
        eventParams.putString(EventParam.STATUS, String.valueOf(status));
        eventParams.putString(EventParam.ERROR_MESSAGE, errorMessage != null ? errorMessage : "");

        return eventParams;
    }
}
