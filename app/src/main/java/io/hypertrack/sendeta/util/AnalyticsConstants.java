package io.hypertrack.sendeta.util;

/**
 * Created by piyush on 29/06/16.
 */
public class AnalyticsConstants {

    public class Event {
        // Signup Events
        public static final String ENTERED_NAME = "Entered name";
        public static final String UPLOADED_PROFILE_PHOTO = "Uploaded profile photo";

        // Location Sharing Events
        public static final String STARTED_SHARING = "Started a trip";

        // Tapped on Icons during a Live Trip
        public static final String TAPPED_SHARE_ICON = "Tapped on share icon on live trip";
        public static final String TAPPED_NAVIGATE_ICON = "Tapped on navigate icon on live trip";
        public static final String TAPPED_STOP_SHARING = "Tapped on end trip CTA on live trip";
    }

    public class EventParam {
        public static final String STATUS = "status";
        public static final String ERROR_MESSAGE = "error_message";
        public static final String SHARED_CURRENT_TRIP_BEFORE = "had_shared_this_trip_before";
    }
}
