package com.hypertrack.live.ui;

import android.content.Context;

import com.hypertrack.live.utils.SharedHelper;

public class BaseState {

    protected final Context mContext;
    protected final SharedHelper sharedHelper;
    private final String hyperTrackPublicKey;

    protected BaseState(Context context) {
        mContext = context;
        sharedHelper = SharedHelper.getInstance(context);
        hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();
    }

    public String getHyperTrackPubKey() {
        return hyperTrackPublicKey;
    }

}
