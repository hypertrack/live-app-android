package com.hypertrack.live;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hypertrack.live.utils.NotificationUtils;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingError;

public class TrackingErrorReceiver extends BroadcastReceiver {
    private static final String TAG = TrackingErrorReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        int errorCode = intent.getIntExtra("code", 0);
        switch (errorCode) {
            case TrackingError.INVALID_PUBLISHABLE_KEY_ERROR:
            case TrackingError.AUTHORIZATION_ERROR:
                Log.e(TAG, "Authorization failed");
                break;
            case TrackingError.GPS_PROVIDER_DISABLED_ERROR:
                Log.e(TAG, "Tracking failed");
                // User disabled GPS in device settings.
                NotificationUtils.sendGpsDisabledError(context);
                HyperTrack.addTrackingStateListener(new MyTrackingStateListener() {
                    @Override
                    public void onTrackingStart() {
                        NotificationUtils.cancelGpsDisabledError(context);
                        HyperTrack.removeTrackingStateListener(this);
                    }
                });
                break;
            default:
                // Some critical error in SDK.
                break;
        }
    }
}
