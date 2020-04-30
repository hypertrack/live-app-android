package com.hypertrack.live.ui;

import android.content.Context;

import com.hypertrack.live.utils.SharedHelper;
import com.hypertrack.sdk.HyperTrack;

public class BaseState {

    protected final Context mContext;
    protected final SharedHelper sharedHelper;
    private final String hyperTrackPublicKey;
    protected final HyperTrack hyperTrack;

    protected BaseState(Context context) {
        mContext = context;
        sharedHelper = SharedHelper.getInstance(context);
        hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();
        hyperTrack = HyperTrack.getInstance(context, hyperTrackPublicKey);
    }

    public String getHyperTrackPubKey() {
        return hyperTrackPublicKey;
    }

}
