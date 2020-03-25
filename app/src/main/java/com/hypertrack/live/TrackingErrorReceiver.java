package com.hypertrack.live;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hypertrack.live.utils.NotificationUtils;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.TrackingStateObserver;

public class TrackingErrorReceiver extends BroadcastReceiver {
    private static final String TAG = App.TAG + TrackingErrorReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!((App)context.getApplicationContext()).isForeground()) {
            int code = intent.getIntExtra(TrackingStateObserver.EXTRA_KEY_CODE_, 0);
            switch (code) {
                case TrackingStateObserver.EXTRA_EVENT_CODE_START:
                    NotificationUtils.cancelGpsDisabledError(context);
                    break;
                case TrackingError.INVALID_PUBLISHABLE_KEY_ERROR:
                case TrackingError.AUTHORIZATION_ERROR:
                    Log.e(TAG, "Authorization failed");
                    break;
                case TrackingError.GPS_PROVIDER_DISABLED_ERROR:
                    Log.e(TAG, "Tracking failed");
                    // User disabled GPS in device settings.
                    NotificationUtils.sendGpsDisabledError(context);
                    break;
                default:
                    // Some critical error in SDK.
            }
        }
    }
}
