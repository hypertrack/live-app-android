package io.hypertrack.sendeta.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by piyush on 08/07/16.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String NETWORK_CHANGED = "NETWORK_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent networkChangedIntent = new Intent(NETWORK_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(networkChangedIntent);
    }
}
