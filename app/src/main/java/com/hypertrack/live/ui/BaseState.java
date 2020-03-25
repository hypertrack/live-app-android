package com.hypertrack.live.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.hypertrack.live.R;

public class BaseState {
    protected final SharedPreferences preferences;
    private final String hyperTrackPublicKey;

    protected BaseState(Context context) {
        preferences = context.getSharedPreferences(context.getString(R.string.app_name), Activity.MODE_PRIVATE);
        hyperTrackPublicKey = preferences.getString("pub_key", "");
    }

    public String getHyperTrackPubKey() {
        return hyperTrackPublicKey;
    }
}
