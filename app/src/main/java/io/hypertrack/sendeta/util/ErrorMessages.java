package io.hypertrack.sendeta.util;

/**
 * Created by piyush on 29/06/16.
 */
public class ErrorMessages {
    // Register ErrorMessages
    public static final String INVALID_PHONE_NUMBER = "Please enter a valid number";
    public static final String PHONE_NO_REGISTRATION_FAILED = "We could not process your request at this moment. Please try again";

    // Verify ErrorMessages
    public static final String INVALID_VERIFICATION_CODE = "Please enter valid verification code";
    public static final String PHONE_NO_VERIFICATION_FAILED = "Apologies, there was an error while verifying your number. Please try again";
    public static final String RESEND_OTP_FAILED = "There was an error while resending your verification code. Please try again";

    // Profile ErrorMessages
    public static final String PROFILE_UPDATE_FAILED = "We had problem connecting with the server. Please try again in sometime";
    public static final String PROFILE_PIC_UPLOAD_FAILED = "There was an error uploading the profile pic. Please try again";

    // Trip ErrorMessages
    public static final String START_TRIP_FAILED = "There was an error starting the trip. Please try again";
    public static final String END_TRIP_FAILED = "There was an error ending the trip. Please try again";
}
