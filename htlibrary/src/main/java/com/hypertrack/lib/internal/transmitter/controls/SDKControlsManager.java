package com.hypertrack.lib.internal.transmitter.controls;

/**
 * Created by Arjun on 31/05/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.hypertrack.lib.internal.transmitter.utils.Constants;

public class SDKControlsManager {
    private static final String SDK_CONTROLS_PREFERENCE_KEY = "com.hypertrack:SDKControls";

    /**
     * Default values for sdkcontrols.
     */
    private static final Integer PASSIVE_DEFAULT_MINIMUM_DURATION = 10; // seconds
    private static final Integer PASSIVE_DEFAULT_MINIMUM_DISPLACEMENT = 30; // meters
    private static final Integer PASSIVE_DEFAULT_BATCH_DURATION = 90; // seconds

    private static final Integer ACTIVE_DEFAULT_MINIMUM_DURATION = 10; // seconds
    private static final Integer ACTIVE_DEFAULT_MINIMUM_DISPLACEMENT = 30; // meters
    private static final Integer ACTIVE_DEFAULT_BATCH_DURATION = 90; // seconds

    private Context mContext;

    public SDKControlsManager(Context context) {
        this.mContext = context;
    }

    public void setSDKControls(SDKControls newControls, @NonNull final UpdateControlsCallback callback) {
        if (newControls == null) {
            return;
        }

        SDKControls oldControls = getSDKControls();
        saveSDKControls(newControls);

        // Check if GO_OFFLINE command was sent
        if (newControls.isFlushDataCommand()) {
            callback.onFlushDataCommand();
        }

        // Check if GO_OFFLINE command was sent
        if (newControls.isGoOfflineCommand()) {
            callback.onGoOfflineCommand();
            return;
        }

        boolean resetSchedulingControls = false;
        boolean resetCollectionControls = false;

        if (oldControls != null) {
            if (!oldControls.getBatchDuration().equals(newControls.getBatchDuration())) {
                resetSchedulingControls = true;
            }

            if (!oldControls.getMinimumDisplacement().equals(newControls.getMinimumDisplacement()) ||
                    !oldControls.getMinimumDuration().equals(newControls.getMinimumDuration())) {
                resetCollectionControls = true;
            }
        }

        if (newControls.isGoOnlineCommand()) {
            callback.onGoOnlineCommand(resetSchedulingControls, resetCollectionControls);

        } else if (newControls.isGoActiveCommand()) {
            callback.onGoActiveCommand(resetSchedulingControls, resetCollectionControls);
        }
    }

    public SDKControls getSDKControls() {
        SDKControls savedControls = getSavedControls();
        if (savedControls == null) {
            return getDefaultControls();
        }

        return savedControls;
    }

    public SDKControls getTTLExpiredSDKControls() {
        SDKControls sdkControls = getSDKControls();

        if (sdkControls.getTtl() == null)
            return sdkControls;

        // Check for SDK's current tracking mode
        if (sdkControls.isGoActiveCommand()) {
            return getActiveModeDefaultSDKControls();
        }

        return getPassiveModeDefaultSDKControls();
    }

    private SDKControls getDefaultControls() {
        return new SDKControls("", SDKControls.RunCommand.GO_OFFLINE, PASSIVE_DEFAULT_BATCH_DURATION, PASSIVE_DEFAULT_MINIMUM_DURATION,
                PASSIVE_DEFAULT_MINIMUM_DISPLACEMENT);
    }

    private SDKControls getSavedControls() {
        SharedPreferences preferences = mContext.getSharedPreferences(Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String controlsString = preferences.getString(SDK_CONTROLS_PREFERENCE_KEY, null);

        if (controlsString == null) {
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(controlsString, SDKControls.class);
    }

    private SDKControls getActiveModeDefaultSDKControls() {
        SDKControls sdkControls = getSDKControls();
        sdkControls.setBatchDuration(ACTIVE_DEFAULT_BATCH_DURATION);
        sdkControls.setMinimumDisplacement(ACTIVE_DEFAULT_MINIMUM_DISPLACEMENT);
        sdkControls.setMinimumDuration(ACTIVE_DEFAULT_MINIMUM_DURATION);
        sdkControls.setTtl(null);
        return sdkControls;
    }

    private SDKControls getPassiveModeDefaultSDKControls() {
        SDKControls sdkControls = getSDKControls();
        sdkControls.setBatchDuration(PASSIVE_DEFAULT_BATCH_DURATION);
        sdkControls.setMinimumDisplacement(PASSIVE_DEFAULT_MINIMUM_DISPLACEMENT);
        sdkControls.setMinimumDuration(PASSIVE_DEFAULT_MINIMUM_DURATION);
        sdkControls.setTtl(null);
        return sdkControls;
    }

    private void saveSDKControls(SDKControls controls) {
        SharedPreferences.Editor editor = this.getEditor();
        Gson gson = new Gson();
        String serializedControls = gson.toJson(controls);
        editor.putString(SDK_CONTROLS_PREFERENCE_KEY, serializedControls);
        editor.apply();
    }

    public void clearControls() {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(SDK_CONTROLS_PREFERENCE_KEY);
        editor.apply();
    }

    private SharedPreferences.Editor getEditor() {
        SharedPreferences preferences = mContext.getSharedPreferences(Constants.HT_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        return preferences.edit();
    }
}