package com.hypertrack.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by piyush on 19/02/17.
 */
public class HyperTrackReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        HyperTrackImpl.getInstance().initialize(context.getApplicationContext(), null);
        HyperTrackImpl.getInstance().handleIntent(context, intent);
    }
}
