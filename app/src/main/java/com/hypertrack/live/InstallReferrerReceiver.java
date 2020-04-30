package com.hypertrack.live;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.hypertrack.live.utils.SharedHelper;

public class InstallReferrerReceiver extends BroadcastReceiver {
    @SuppressLint("ApplySharedPref")
    @Override
    public void onReceive(final Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        if (!TextUtils.isEmpty(referrer)) {
            Uri uri = Uri.parse("referrer?"+referrer);
            String publishableKey = uri.getQueryParameter("htlink");
            if (!TextUtils.isEmpty(publishableKey)) {
                SharedHelper.getInstance(context).setHyperTrackPubKey(publishableKey);
            }
        }
    }
}
