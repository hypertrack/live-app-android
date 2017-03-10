package com.hypertrack.lib.models;

import com.android.volley.VolleyError;
import com.hypertrack.lib.internal.common.network.NetworkErrorUtil;

/**
 * Created by piyush on 25/02/17.
 */
public class ErrorResponse {
    private int errorCode;
    private String errorMessage;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ErrorResponse() {
        errorCode = Error.Code.UNHANDLED_ERROR;
        errorMessage = Error.Message.UNHANDLED_ERROR;
    }

    public ErrorResponse(VolleyError error) {
        errorCode = NetworkErrorUtil.getErrorCode(error);
        errorMessage = NetworkErrorUtil.getMessage(error);
    }

    public ErrorResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorResponse(Error.Type type) {
        switch (type) {
            case PUBLISHABLE_KEY_NOT_CONFIGURED:
                errorCode = Error.Code.PUBLISHABLE_KEY_NOT_CONFIGURED;
                errorMessage = Error.Message.PUBLISHABLE_KEY_NOT_CONFIGURED;
                break;
            case USER_ID_NOT_CONFIGURED:
                errorCode = Error.Code.USER_ID_NOT_CONFIGURED;
                errorMessage = Error.Message.USER_ID_NOT_CONFIGURED;
                break;

            case PLAY_SERVICES_UNAVAILABLE:
                errorCode = Error.Code.PLAY_SERVICES_UNAVAILABLE;
                errorMessage = Error.Message.PLAY_SERVICES_UNAVAILABLE;
                break;
            case PERMISSIONS_NOT_REQUESTED:
                errorCode = Error.Code.PERMISSIONS_NOT_REQUESTED;
                errorMessage = Error.Message.PERMISSIONS_NOT_REQUESTED;
                break;
            case LOCATION_SETTINGS_DISABLED:
                errorCode = Error.Code.LOCATION_SETTINGS_DISABLED;
                errorMessage = Error.Message.LOCATION_SETTINGS_DISABLED;
                break;
            case LOCATION_SETTINGS_LOW_ACCURACY:
                errorCode = Error.Code.LOCATION_SETTINGS_LOW_ACCURACY;
                errorMessage = Error.Message.LOCATION_SETTINGS_LOW_ACCURACY;
                break;
            case NETWORK_CONNECTIVITY_ERROR:
                errorCode = Error.Code.NETWORK_CONNECTIVITY_ERROR;
                errorMessage = Error.Message.NETWORK_CONNECTIVITY_ERROR;
                break;

            case GOOGLE_API_CLIENT_CONN_FAILED:
                errorCode = Error.Code.GOOGLE_API_CLIENT_CONN_FAILED;
                errorMessage = Error.Message.GOOGLE_API_CLIENT_CONN_FAILED;
                break;
            case GOOGLE_API_CLIENT_CONN_SUSPENDED:
                errorCode = Error.Code.GOOGLE_API_CLIENT_CONN_SUSPENDED;
                errorMessage = Error.Message.GOOGLE_API_CLIENT_CONN_SUSPENDED;
                break;
            case LOCATION_SETTINGS_CHANGE_UNAVAILABLE:
                errorCode = Error.Code.LOCATION_SETTINGS_CHANGE_UNAVAILABLE;
                errorMessage = Error.Message.LOCATION_SETTINGS_CHANGE_UNAVAILABLE;
                break;

            case INVALID_LOCATION_RECEIVED:
                errorCode = Error.Code.INVALID_LOCATION_RECEIVED;
                errorMessage = Error.Message.INVALID_LOCATION_RECEIVED;
                break;

            case INVALID_PARAM_ACTION_ID:
                errorCode = Error.Code.INVALID_PARAM_ACTION_ID;
                errorMessage = Error.Message.INVALID_PARAM_ACTION_ID;
                break;

            default:
                errorCode = Error.Code.UNHANDLED_ERROR;
                errorMessage = Error.Message.UNHANDLED_ERROR;
                break;
        }
    }
}
