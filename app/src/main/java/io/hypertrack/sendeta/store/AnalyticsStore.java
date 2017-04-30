package io.hypertrack.sendeta.store;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.facebook.FacebookSdk;
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

//        if (BuildConfig.DEBUG) {
//            FacebookSdk.setIsDebugEnabled(true);
//            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
//        }

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

    // Add Destination Address Events
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

    @Override
    public void tappedNavigate() {
        logEvent(Event.TAPPED_NAVIGATE_ICON);
    }

    @Override
    public void tappedEndTrip(boolean status, String errorMessage) {
        Bundle params = getBundle(status, errorMessage);

        logEvent(Event.TAPPED_END_TRIP, params);
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
