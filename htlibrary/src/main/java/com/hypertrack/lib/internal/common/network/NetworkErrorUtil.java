package com.hypertrack.lib.internal.common.network;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.hypertrack.lib.models.Error;

import java.io.UnsupportedEncodingException;

public class NetworkErrorUtil {

    private static final String TAG = NetworkErrorUtil.class.getSimpleName();

    public static int getErrorCode(VolleyError error) {
        if (error == null || error.networkResponse == null)
            return Error.Code.UNHANDLED_ERROR;

        return error.networkResponse.statusCode;
    }

    public static String getMessage(VolleyError error) {
        String errorMessage = Error.Message.UNHANDLED_ERROR;

        if (error == null || error.networkResponse == null) {
            return errorMessage;
        }

        String json = "";
        try {
            json = new String(
                    error.networkResponse.data, HttpHeaderParser.parseCharset(error.networkResponse.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        switch (error.networkResponse.statusCode) {
            case Error.Code.BAD_REQUEST:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.BAD_REQUEST + " " + json;
                break;
            case Error.Code.AUTHORIZATION_TOKEN_NOT_PROVIDED:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.AUTHORIZATION_KEY_NOT_PROVIDED + " " + json;
                break;
            case Error.Code.FORBIDDEN_REQUEST:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.FORBIDDEN_REQUEST + " " + json;
                break;
            case Error.Code.NOT_FOUND:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.NOT_FOUND + " " + json;
                break;
            case Error.Code.METHOD_NOT_ALLOWED:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.METHOD_NOT_ALLOWED + " " + json;
                break;
            case Error.Code.NOT_ACCEPTABLE:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.NOT_ACCEPTABLE + " " + json;
                break;
            case Error.Code.REQUEST_TIMEOUT:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.REQUEST_TIMEOUT + " " + json;
                break;
            case Error.Code.GONE:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.GONE + " " + json;
                break;
            case Error.Code.TOO_MANY_REQUESTS:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.TOO_MANY_REQUESTS + " " + json;
                break;
            case Error.Code.INTERNAL_SERVER_ERROR:
            case Error.Code.BAD_GATEWAY:
            case Error.Code.SERVICE_UNAVAILABLE:
            case Error.Code.GATEWAY_TIMEOUT:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.INTERNAL_SERVER_ERROR + " " + json;
                break;
            case Error.Code.NOT_IMPLEMENTED_ON_SERVER:
                errorMessage = error.networkResponse.statusCode + ": " + Error.Message.SERVICE_UNAVAILABLE + " " + json;
                break;
        }

        return errorMessage;
    }

    public static Exception getException(VolleyError error) {
        return new RuntimeException(getMessage(error));
    }

    public static boolean isInvalidTokenError(VolleyError error) {
        return error != null && error.networkResponse != null &&
                (error.networkResponse.statusCode == Error.Code.AUTHORIZATION_TOKEN_NOT_PROVIDED
                        || error.networkResponse.statusCode == Error.Code.FORBIDDEN_REQUEST
                        || error.networkResponse.statusCode == Error.Code.NOT_FOUND);
    }

    public static boolean isInvalidRequest(VolleyError error) {
        return error != null && error.networkResponse != null && error.networkResponse.statusCode >= 400
                && error.networkResponse.statusCode < 500;
    }
}
