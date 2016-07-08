package io.hypertrack.sendeta.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

/**
 * Created by piyush on 08/07/16.
 */
public class GpsLocationReceiver extends BroadcastReceiver{

    public static final String LOCATION_CHANGED = "LOCATION_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

            Intent locationChangedIntent = new Intent(LOCATION_CHANGED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(locationChangedIntent);
        }
    }
}
