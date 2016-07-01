package io.hypertrack.sendeta.util;

/**
 * Created by piyush on 29/06/16.
 */
public class AnalyticsConstants {

    public class Event {
        // Signup Events
        public static final String ENTERED_PHONE_NO = "Entered phone number";
        public static final String ENTERED_CORRECT_OTP = "Entered correct OTP";
        public static final String RESEND_OTP = "Resend OTP";
        public static final String ENTERED_NAME = "Entered name";
        public static final String UPLOADED_PROFILE_PHOTO = "Uploaded profile photo";

        // Add Destination Address Events
        public static final String TYPED_AN_ADDRESS = "Typed an address";
        public static final String SELECTED_AN_ADDRESS = "Selected an address";

        // Trip Events
        public static final String STARTED_A_TRIP = "Started a trip";

        // Tapped on Share Via Events
        public static final String TAPPED_SHARE_VIA_ANOTHER_APP = "Tapped on share via another app";
        public static final String TAPPED_ON_3RD_PARTY_APP = "Tapped on a 3P app";

        // Trip Shared Via Events
        public static final String TRIP_SHARED_VIA_3RD_PARTY_APP = "Trip shared via a 3P app";

        //
        public static final String SELECTED_CONTACTS = "Selected contacts";
        public static final String TAPPED_SEND_TO_GROUP = "Tapped on Send to group";
        public static final String TEXT_MESSAGE_SENT = "Text message sent";

        // Tapped on Icons during a Live Trip
        public static final String TAPPED_SHARE_ICON = "Tapped on share icon on live trip";
        public static final String TAPPED_NAVIGATE_ICON = "Tapped on navigate icon on live trip";
        public static final String TAPPED_FAVORITE_ICON = "Tapped on favorite icon on live trip";
        public static final String TAPPED_END_TRIP = "Tapped on end trip CTA on live trip";
        public static final String AUTO_END_TRIP = "Trip ended automatically";

        // Profile Events
        public static final String TAPPED_ON_PROFILE = "Tapped on profile";
        public static final String TAPPED_ON_EDIT_PROFILE = "Tapped on edit_profile";
        public static final String EDITED_FIRST_NAME = "Edited first_name";
        public static final String EDITED_LAST_NAME = "Edited last_name";
        public static final String UPLOADED_PHOTO_VIA_GALLERY = "Uploaded photo via profile editor";
        public static final String REPLACED_PHOTO_VIA_GALLERY = "Replaced photo via profile editor";

        // Home Favorite Events
        public static final String ADDED_HOME = "Added home";
        public static final String EDITED_HOME = "Edited home";
        public static final String DELETED_HOME = "Deleted home";

        // Work Favorite Events
        public static final String ADDED_WORK = "Added work";
        public static final String EDITED_WORK = "Edited work";
        public static final String DELETED_WORK = "Deleted work";

        // Other Favorite Events
        public static final String ADDED_OTHER_FAVORITE = "Added other favorite";
        public static final String EDITED_OTHER_FAVORITE = "Edited other favorite";
        public static final String DELETED_OTHER_FAVORITE = "Deleted other favorite";
    }

    public class EventParam {
        public static final String STATUS = "status";
        public static final String ERROR_MESSAGE = "error_message";
        public static final String NO_OF_ATTEMPT = "No_of_attempt";
        public static final String IS_EXISTING_USER = "is_phone_number_existing";
        public static final String IS_ADDRESS_FAVORITE = "is_address_favorite";
        public static final String SHARING_APP = "sharing_app";
        public static final String NO_OF_CONTACTS_SELECTED = "number_of_contacts_selected";
        public static final String NO_OF_SEND_ETA_CONTACTS_SELECTED = "number_of_contacts_selected_with_SendETA";
        public static final String NO_OF_CONTACTS_SENT = "number_of_contacts_sent";
        public static final String SHARED_CURRENT_TRIP_BEFORE = "had_shared_this_trip_before";
        public static final String NO_OF_FAVORITES_ADDED = "number_of_fav_locations_added";
    }
}
