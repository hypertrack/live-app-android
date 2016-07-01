package io.hypertrack.sendeta.store;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

import java.math.BigDecimal;
import java.util.Currency;

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.util.AnalyticsConstants.Event;
import io.hypertrack.sendeta.util.AnalyticsConstants.EventParam;

/**
 * Created by piyush on 29/06/16.
 */
public class AnalyticsStore implements AnalyticsInterface {

    private static final String TAG = "AnalyticsStore";
    private static AnalyticsStore analyticsStore;
    private AppEventsLogger fbAnalyticsLogger;

    private AnalyticsStore(){}

    private AnalyticsStore(Application mApplication) {
        Context context = mApplication.getApplicationContext();
        this.fbAnalyticsLogger = AppEventsLogger.newLogger(context);
    }

    public static AnalyticsStore getLogger() {
        return analyticsStore;
    }

    public static synchronized void init(Application application) {

        // Initialize FB SDK & Activate FB Tracking
        FacebookSdk.sdkInitialize(application.getApplicationContext());
        AppEventsLogger.activateApp(application);

        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        }

        if (analyticsStore == null) {
            analyticsStore = new AnalyticsStore(application);
        }
    }

    private void logEvent(String eventName) {
        try {
            fbAnalyticsLogger.logEvent(eventName);
            fbAnalyticsLogger.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logEvent(String eventName, Bundle params) {
        try {
            fbAnalyticsLogger.logEvent(eventName, params);
            fbAnalyticsLogger.flush();
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
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.ENTERED_NAME, params);
    }

    @Override
    public void enteredCorrectOTP(boolean status, String errorMessage, int retries) {
        Bundle params = getBundle(status, errorMessage);
        params.putString(EventParam.NO_OF_ATTEMPT, String.valueOf(retries));

        logEvent(Event.ENTERED_CORRECT_OTP, params);
    }

    @Override
    public void resendOTP(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.RESEND_OTP, params);
    }

    @Override
    public void enteredName(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.ENTERED_NAME, params);
    }

    @Override
    public void uploadedProfilePhoto(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.UPLOADED_PROFILE_PHOTO, params);
    }

    @Override
    public void completedProfileSetUp(boolean isExistingUser) {
        Bundle params = getBundle();
        params.putString(EventParam.IS_EXISTING_USER, String.valueOf(isExistingUser));

        // Using FB Event for Completed Profile Set-Up
        logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params);
    }

    // Add Destination Address Events
//    @Override
//    public void typedAddress() {
//        logEvent(Event.TYPED_AN_ADDRESS);
//    }

    @Override
    public void selectedAddress(int charactersTyped, boolean isFavorite) {
        Bundle params = getBundle();
        params.putString(EventParam.IS_ADDRESS_FAVORITE, String.valueOf(isFavorite));

        logEvent(Event.SELECTED_AN_ADDRESS, params);
    }

    // Trip Events
    @Override
    public void startedTrip(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.STARTED_A_TRIP, params);
        logPurchaseEvent();
    }

    @Override
    public void tappedShareIcon(boolean tripShared) {
        Bundle params = getBundle();
        params.putString(EventParam.SHARED_CURRENT_TRIP_BEFORE, String.valueOf(tripShared));

        logEvent(Event.TAPPED_SHARE_ICON, params);
    }

//    @Override
//    public void tappedOnShareViaAnotherApp() {
//        logEvent(Event.TAPPED_SHARE_VIA_ANOTHER_APP);
//    }

    @Override
    public void tappedOn3rdPartyApp(String appName) {
        Bundle params = getBundle();
        params.putString(EventParam.SHARING_APP, appName);

        logEvent(Event.TAPPED_ON_3RD_PARTY_APP, params);
    }

    @Override
    public void tripSharedVia3rdPartyApp(String appName) {
        Bundle params = getBundle();
        params.putString(EventParam.SHARING_APP, appName);

        logEvent(Event.TRIP_SHARED_VIA_3RD_PARTY_APP, params);
    }

//    @Override
//    public void selectedContacts(int contactsCount, int sendETAContactsCount) {
//        Bundle params = getBundle();
//        params.putString(EventParam.NO_OF_CONTACTS_SELECTED, String.valueOf(contactsCount));
//        params.putString(EventParam.NO_OF_SEND_ETA_CONTACTS_SELECTED, String.valueOf(sendETAContactsCount));
//
//        logEvent(Event.SELECTED_CONTACTS, params);
//    }

//    @Override
//    public void tappedSendToGroup(int contactsCount, int sendETAContactsCount) {
//        Bundle params = getBundle();
//        params.putString(EventParam.NO_OF_CONTACTS_SELECTED, String.valueOf(contactsCount));
//        params.putString(EventParam.NO_OF_SEND_ETA_CONTACTS_SELECTED, String.valueOf(sendETAContactsCount));
//
//        logEvent(Event.TAPPED_SEND_TO_GROUP, params);
//    }

//    @Override
//    public void textMessageSent(int contactsSentCount) {
//        Bundle params = getBundle();
//        params.putString(EventParam.NO_OF_CONTACTS_SENT, String.valueOf(contactsSentCount));
//
//        logEvent(Event.TEXT_MESSAGE_SENT, params);
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
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.TAPPED_END_TRIP, params);
    }

    @Override
    public void autoTripEnded(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.AUTO_END_TRIP, params);
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
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.EDITED_FIRST_NAME, params);
    }

    @Override
    public void editedLastName(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.EDITED_LAST_NAME, params);
    }

    @Override
    public void uploadedPhotoViaPhotoEditor(boolean status, String errorMessage) {
        logEvent(Event.UPLOADED_PHOTO_VIA_GALLERY);
    }

    @Override
    public void replacedPhotoViaPhotoEditor(boolean status, String errorMessage) {
        logEvent(Event.REPLACED_PHOTO_VIA_GALLERY);
    }

    // Home Favorite Events
    @Override
    public void addedHome(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.ADDED_HOME, params);
    }

    @Override
    public void editedHome(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.EDITED_HOME, params);
    }

    @Override
    public void deletedHome(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.DELETED_HOME, params);
    }

    // Work Favorite Events
    @Override
    public void addedWork(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.ADDED_WORK, params);
    }

    @Override
    public void editedWork(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.EDITED_WORK, params);
    }

    @Override
    public void deletedWork(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.DELETED_WORK, params);
    }

    // Other Favorite Events
    @Override
    public void addedOtherFavorite(boolean status, String errorMessage, int favoritesCount) {
        Bundle params = getBundle(status, errorMessage);
        params.putString(EventParam.NO_OF_FAVORITES_ADDED, String.valueOf(favoritesCount));

        logEvent(Event.ADDED_OTHER_FAVORITE, params);
    }

    @Override
    public void editedOtherFavorite(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.EDITED_OTHER_FAVORITE, params);
    }

    @Override
    public void deletedOtherFavorite(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.DELETED_OTHER_FAVORITE, params);
    }

    /**
     * Method to get params Bundle object
     *
     * @return
     */
    private Bundle getBundle() {
        return new Bundle();
    }

    /**
     * Method to get params Bundle object
     *
     * @param status        Flag to denote the Success/Failure Event
     * @param errorMessage  Message displayed to User in case of failure
     * @return
     */
    private Bundle getBundle(boolean status, String errorMessage) {
        Bundle params = new Bundle();
        params.putString(EventParam.STATUS, String.valueOf(status));
        params.putString(EventParam.ERROR_MESSAGE, errorMessage != null ? errorMessage : "");

        return params;
    }
}
