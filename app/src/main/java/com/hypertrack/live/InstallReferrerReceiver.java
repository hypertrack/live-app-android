package com.hypertrack.live;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class InstallReferrerReceiver extends BroadcastReceiver {
    @SuppressLint("ApplySharedPref")
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String referrer = intent.getStringExtra("referrer");

        if (!TextUtils.isEmpty(referrer)) {
            context.getSharedPreferences(context.getString(R.string.app_name), Activity.MODE_PRIVATE).edit()
                    .putString("_install_referrer", referrer)
                    .commit();
        }
    }
}
