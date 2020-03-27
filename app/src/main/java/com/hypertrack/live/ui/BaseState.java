package com.hypertrack.live.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.hypertrack.live.utils.SharedHelper;
import com.hypertrack.sdk.HyperTrack;

public class BaseState {

    protected final SharedHelper sharedHelper;
    private final String hyperTrackPublicKey;
    protected final HyperTrack hyperTrack;

    protected BaseState(Context context) {
        sharedHelper = SharedHelper.getInstance(context);
        hyperTrackPublicKey = sharedHelper.sharedPreferences().getString(SharedHelper.PUB_KEY, "");
        hyperTrack = HyperTrack.getInstance(context, hyperTrackPublicKey);
    }

    public String getHyperTrackPubKey() {
        return hyperTrackPublicKey;
    }

    protected SharedPreferences preferences() {
        return sharedHelper.sharedPreferences();
    }
}
