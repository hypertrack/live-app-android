package com.hypertrack.lib.models;

/**
 * Created by piyush on 27/02/17.
 */

public class Error {
    public class Code {
        /**
         * SDK Level ErrorCodes
         */
        public static final short PUBLISHABLE_KEY_NOT_CONFIGURED = 100;
        public static final short SDK_NOT_INITIALIZED = 101;
        public static final short USER_ID_NOT_CONFIGURED = 102;
        public static final short PLAY_SERVICES_UNAVAILABLE = 103;
        public static final short PERMISSIONS_NOT_REQUESTED = 104;
        public static final short LOCATION_SETTINGS_DISABLED = 105;
        public static final short LOCATION_SETTINGS_LOW_ACCURACY = 106;
        public static final short NETWORK_CONNECTIVITY_ERROR = 107;

        public static final short GOOGLE_API_CLIENT_CONN_FAILED = 110;
        public static final short GOOGLE_API_CLIENT_CONN_SUSPENDED = 111;
        public static final short LOCATION_SETTINGS_CHANGE_UNAVAILABLE = 112;

        public static final short INVALID_LOCATION_RECEIVED = 121;

        public static final short INVALID_PARAM_ACTION_ID = 131;

        public static final short UNHANDLED_ERROR = 151;

        /**
         * API Level ErrorCodes
         */
        public static final short BAD_REQUEST = 400;
        public static final short AUTHORIZATION_TOKEN_NOT_PROVIDED = 401;
        public static final short FORBIDDEN_REQUEST = 403;
        public static final short NOT_FOUND = 404;
        public static final short METHOD_NOT_ALLOWED = 405;
        public static final short NOT_ACCEPTABLE = 406;
        public static final short REQUEST_TIMEOUT = 408;
        public static final short GONE = 410;
        public static final short TOO_MANY_REQUESTS = 429;

        public static final short INTERNAL_SERVER_ERROR = 500;
        public static final short NOT_IMPLEMENTED_ON_SERVER = 501;
        public static final short BAD_GATEWAY = 502;
        public static final short SERVICE_UNAVAILABLE = 503;
        public static final short GATEWAY_TIMEOUT = 504;
    }

    public class Message {
        public static final String PUBLISHABLE_KEY_NOT_CONFIGURED =
                "PublishableKey not configured. PublishableKey needs to be set before calling this API.";
        public static final String INVALID_PUBLISHABLE_KEY =
                "PublishableKey cannot be empty. Refer to " +
                        "https://docs.hypertrack.com/guides/authentication.html#publishable-key";
        public static final String SECRET_KEY_USED_AS_PUBLISHABLE_KEY =
                "SecretKey shouldn't be used as PublishableKey in SDK. Refer to " +
                        "https://docs.hypertrack.com/guides/authentication.html#publishable-key";
        public static final String SDK_NOT_INITIALIZED = "HyperTrack SDK not initialized. " +
                        "Call HyperTrack.initialize() method either in your Application file or Launcher activity.";
        public static final String USER_ID_NOT_CONFIGURED =
                "UserID not configured. UserId needs to be set before calling this API.";
        public static final String PLAY_SERVICES_UNAVAILABLE =
                "Play Services not available. Please update play services before calling this API.";
        public static final String PERMISSIONS_NOT_REQUESTED =
                "Location Permissions not granted. Request for Location Permission before calling this API.";
        public static final String LOCATION_SETTINGS_DISABLED =
                "Location Settings not enabled. Enable Location Settings before calling this API.";
        public static final String LOCATION_SETTINGS_LOW_ACCURACY =
                "Location Settings enabled in LOW_ACCURACY mode. Enable them in HIGH_ACCURACY mode for better results.";
        public static final String NETWORK_CONNECTIVITY_ERROR =
                "Network Connection disabled. Enable it before calling this API";
        public static final String GOOGLE_API_CLIENT_CONN_FAILED =
                "GoogleAPIClient connection failed. Please try again in some time.";
        public static final String GOOGLE_API_CLIENT_CONN_SUSPENDED =
                "GoogleAPIClient connection suspended. Please try again in some time.";
        public static final String LOCATION_SETTINGS_CHANGE_UNAVAILABLE =
                "Location Services change unavailable. Please check your settings and try again.";
        public static final String INVALID_LOCATION_RECEIVED =
                "Invalid Location received because of no GPS fix. Please try again in some time.";

        public static final String INVALID_PARAM_ACTION_ID =
                "Complete action failed. Action Id cannot be empty. Please try again with a valid Action Id.";

        public static final String UNHANDLED_ERROR =  "Something went wrong. Try again later";

        public static final String BAD_REQUEST
                = "The request could not be understood by the server due to malformed syntax.";
        public static final String AUTHORIZATION_KEY_NOT_PROVIDED
                = "Unauthorized. Verify your API key. You must use a valid publishable key.";
        public static final String FORBIDDEN_REQUEST
                = "You do not have permission to access the resource.";
        public static final String NOT_FOUND = "Not Found: The resource does not exist.";
        public static final String METHOD_NOT_ALLOWED
                = "You tried to access a resource with an invalid method.";
        public static final String NOT_ACCEPTABLE
                = "You requested a format that is not json.";
        public static final String REQUEST_TIMEOUT
                = "The request timed out. Please try again.";
        public static final String TOO_MANY_REQUESTS
                = "You have hit the rate limit for your account.";
        public static final String GONE
                = "The requested resource has been removed from our servers.";
        public static final String INTERNAL_SERVER_ERROR
                = "There was an error on the server and we have been notified. Try again later.";
        public static final String SERVICE_UNAVAILABLE
                = "We are temporarily offline for maintenance. Please try again later.";
    }

    public enum Type {
        PUBLISHABLE_KEY_NOT_CONFIGURED,
        SDK_NOT_INITIALIZED,
        USER_ID_NOT_CONFIGURED,

        PLAY_SERVICES_UNAVAILABLE,
        PERMISSIONS_NOT_REQUESTED,
        LOCATION_SETTINGS_DISABLED,
        LOCATION_SETTINGS_LOW_ACCURACY,
        NETWORK_CONNECTIVITY_ERROR,

        GOOGLE_API_CLIENT_CONN_FAILED,
        GOOGLE_API_CLIENT_CONN_SUSPENDED,
        LOCATION_SETTINGS_CHANGE_UNAVAILABLE,

        INVALID_LOCATION_RECEIVED,
        INVALID_PARAM_ACTION_ID,
        UNHANDLED_ERROR
    }
}
