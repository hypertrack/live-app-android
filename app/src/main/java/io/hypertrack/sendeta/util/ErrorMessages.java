package io.hypertrack.sendeta.util;

/**
 * Created by piyush on 29/06/16.
 */
public class ErrorMessages {
    public static final String GENERIC_ERROR_MSG = "Something went wrong! Please try again";
    public static final String NETWORK_ISSUE = "Please check your internet connection and try again";
    public static final String REQUEST_TIMED_OUT
            = "Your request is taking too long to process. Please check your internet connection and try again";

    // Register ErrorMessages
    public static final String INVALID_PHONE_NUMBER = "Please enter a valid number";
    public static final String PHONE_NO_REGISTRATION_FAILED = "We could not process your request at this moment. Please try again";

    // Verify ErrorMessages
    public static final String INVALID_VERIFICATION_CODE = "Please enter valid verification code";
    public static final String PHONE_NO_VERIFICATION_FAILED = "Apologies, there was an error while verifying your number. Please try again";
    public static final String RESEND_OTP_FAILED = "There was an error while resending your verification code. Please try again";

    //
    public static final String PROFILE_UPDATE_FAILED = "We had problem connecting with the server. Please try again in sometime";
    public static final String PROFILE_PIC_UPLOAD_FAILED = "There was an error uploading the profile pic. Please try again";
    public static final String PROFILE_PIC_CHOOSE_CANCELLED = "Profile Pic update was cancelled by the user";
    public static final String PROFILE_PIC_CHOOSE_FAILED = "There was an error choosing profile image. Please try again";

    public static final String START_TRIP_FAILED = "There was an error starting the trip. Please try again";

    public static final String END_TRIP_FAILED = "There was an error ending the trip. Please try again";
    public static final String AUTO_END_TRIP_FAILED = "There was an error Auto ending the trip. Please try again";

    public static final String PLACE_NAME_REQUIRED_ERROR = "Place name is mandatory. Please enter a name and try again";
    public static final String HOME_ALREADY_EXISTS_ERROR = "You have already added a home. Please select a different name";
    public static final String WORK_ALREADY_EXISTS_ERROR = "You have already added a work. Please select a different name";
    public static final String PLACE_EXISTS_ERROR = "You have already added this place as a favorite.";
    public static final String ADDING_FAVORITE_PLACE_FAILED = "There was an error adding place. Please try again";
    public static final String EDITING_FAVORITE_PLACE_FAILED = "There was an error editing place. Please try again";
    public static final String EDITING_ALREADY_SAVED_PLACE_ERROR = "The place you are trying to add already exists in your favorites";

    public static final String DELETING_FAVORITE_PLACE_FAILED = "There was error deleting place. Please try again";

}
