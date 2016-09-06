package io.hypertrack.sendeta.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.SocketException;

import io.hypertrack.sendeta.model.ErrorData;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;

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
