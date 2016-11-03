package io.hypertrack.sendeta.store;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.common.util.HTLog;
import io.hypertrack.lib.transmitter.model.HTShift;
import io.hypertrack.lib.transmitter.model.HTShiftParams;
import io.hypertrack.lib.transmitter.model.HTShiftParamsBuilder;
import io.hypertrack.lib.transmitter.model.ServiceNotificationParams;
import io.hypertrack.lib.transmitter.model.ServiceNotificationParamsBuilder;
import io.hypertrack.lib.transmitter.model.TransmitterConstants;
import io.hypertrack.lib.transmitter.model.callback.HTShiftStatusCallback;
import io.hypertrack.lib.transmitter.service.HTTransmitterService;
import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.store.callback.ShiftManagerListener;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.view.SplashScreen;

/**
 * Created by piyush on 12/09/16.
 */
public class ShiftManager {

    private static final String TAG = TaskManager.class.getSimpleName();

    private Context mContext;
    private HTTransmitterService transmitter;

    private HTShift hyperTrackShift;

    private ShiftManagerListener mShiftCompletedListener;
    private BroadcastReceiver mDriverNotLiveReceiver;

    private static ShiftManager sharedManager;

    private ShiftManager(Context mContext) {
        this.mContext = mContext;
        transmitter = HTTransmitterService.getInstance(mContext);

        initializeShiftManager();
    }

    public static ShiftManager getSharedManager(Context context) {
        if (sharedManager == null) {
            sharedManager = new ShiftManager(context);
        }

        return sharedManager;
    }

    // Method to initialize ShiftManager instance
    private void initializeShiftManager() {
        this.setServiceNotification();
    }

    // Method to set TransmitterSDK ServiceNotification
    private void setServiceNotification() {
        //Customize Notification Settings
        ServiceNotificationParamsBuilder builder = new ServiceNotificationParamsBuilder();
        ServiceNotificationParams notificationParams = builder
                .setSmallIconBGColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                .setContentIntentActivityClass(SplashScreen.class)
                .build();
        transmitter.setServiceNotificationParams(notificationParams);
    }

    // Method to get saved ShiftData
    private void getSavedShiftData() {
        this.hyperTrackShift = SharedPreferenceManager.getShift(mContext);
    }

    public boolean shouldRestoreState() {
        this.getSavedShiftData();

        // Check if current Task exists in Shared Preference or not
        if (this.hyperTrackShift != null) {
            // Restore the current shift with locally cached data
            // Start Refreshing the shift without any delay

            // Added a delay to initiate RestoreTaskStart Call (to account for delay in onMapLoadedCallback)
            this.registerForDriverNotLiveBroadcast();
            return true;

        } else {
            // TODO: 17/08/16 Check what to do for this as the Task might not have completed on SDK
            if (this.isShiftActive()) {
                HTLog.e(TAG, "Error occurred while shouldRestoreState: HypertrackShift is NULL even though isShiftActive For Driver");
            }
            this.clearState();
            return false;
        }
    }

    public boolean isShiftActive() {
        HTShift shift = getHyperTrackShift();
        return shift != null;
    }

    public void startShift(final String hyperTrackDriverID, final HTShiftStatusCallback callback) {
        // Set HyperTrack Publishable Key in case empty
        if (!TextUtils.isEmpty(HyperTrack.getPublishableKey(mContext))) {
            Toast.makeText(mContext, "Setting HyperTrack Publishable Key to Personal Account", Toast.LENGTH_SHORT).show();
            HyperTrack.setPublishableApiKey(BuildConfig.API_KEY, mContext);
        }

        // Start HyperTrack Shift
        HTShiftParams shiftParams = getShiftParams(hyperTrackDriverID);
        transmitter.startShift(shiftParams, new HTShiftStatusCallback() {
            @Override
            public void onSuccess(HTShift shift) {

                if (shift != null) {
                    SharedPreferenceManager.setShift(shift);
                }

                registerForDriverNotLiveBroadcast();

                if (callback != null) {
                    callback.onSuccess(shift);
                }
            }

            @Override
            public void onError(Exception e) {
                hyperTrackShift = null;

                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void endShift(final HTShiftStatusCallback callback) {
        transmitter.endShift(new HTShiftStatusCallback() {
            @Override
            public void onSuccess(HTShift shift) {
                if (callback != null)
                    callback.onSuccess(shift);

                clearState();
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Call this method once the task has been completed successfully on the SDK.
     */
    public void clearState() {
        HTLog.i(TAG, "Calling clearState to reset SendETA shift state");
        this.hyperTrackShift = null;
        SharedPreferenceManager.deleteShift(mContext);
        this.clearListeners();
        this.unregisterForDriverNotLiveBroadcast();
    }

    private void clearListeners() {
        this.mShiftCompletedListener = null;
    }

    private HTShiftParams getShiftParams(String hyperTrackDriverID) {
        return new HTShiftParamsBuilder()
                .setDriverID(hyperTrackDriverID)
                .createHTShiftParams();
    }

    public void setShiftCompletedListener(ShiftManagerListener listener) {
        this.mShiftCompletedListener = listener;
    }

    public HTShift getHyperTrackShift() {
        if (this.hyperTrackShift == null) {
            this.hyperTrackShift = SharedPreferenceManager.getShift(mContext);
        }

        return this.hyperTrackShift;
    }

    private void registerForDriverNotLiveBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TransmitterConstants.HT_ON_DRIVER_NOT_ACTIVE_INTENT);

        mDriverNotLiveReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mShiftCompletedListener != null) {
                    mShiftCompletedListener.OnCallback();
                }
                clearState();
            }
        };

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDriverNotLiveReceiver, filter);
    }

    private void unregisterForDriverNotLiveBroadcast() {
        if (this.mDriverNotLiveReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDriverNotLiveReceiver);
            this.mDriverNotLiveReceiver = null;
        }
    }
}