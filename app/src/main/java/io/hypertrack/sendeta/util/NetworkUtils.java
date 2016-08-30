package io.hypertrack.sendeta.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.SocketException;

import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;
import retrofit2.Response;

/**
 * Created by piyush on 22/06/16.
 */
public class NetworkUtils {

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    /**
     * check's network call status
     * success - if statusCode is between 200 && 400
     * failure - else cases are failure
     * @param context
     * @param response
     * @return true or false
     */
    public static boolean isCallSuccess(final Context context, Response response) {

        int code = response.code();
        if (code >= 200 && code < 400) {
            return true;
        }

        return false;
    }

    public static ErrorData processFailure(Throwable t) {
        ErrorData errorData = new ErrorData();

        try {
            if (t instanceof SocketException) {
                errorData.setMessage(ErrorMessages.REQUEST_TIMED_OUT);
                errorData.setCode(ErrorCodes.REQUEST_TIMED_OUT);
            } else if (t instanceof IOException) {
                errorData.setMessage(ErrorMessages.NETWORK_ISSUE);
                errorData.setCode(ErrorCodes.NO_INTERNET);
            }
            t.printStackTrace();
        } catch (Exception e) {}
        return errorData;
    }
}
