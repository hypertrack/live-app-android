package com.hypertrack.live;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class InstallReferrerReceiver extends BroadcastReceiver {
    @SuppressLint("ApplySharedPref")
    @Override
    public void onReceive(final Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        if (!TextUtils.isEmpty(referrer)) {
            Uri uri = Uri.parse("referrer?"+referrer);
            String htlink = uri.getQueryParameter("htlink");
            if (!TextUtils.isEmpty(htlink)) {
                context.getSharedPreferences(context.getString(R.string.app_name), Activity.MODE_PRIVATE).edit()
                        .putString("pub_key", htlink)
                        .commit();
            }
        }
    }
}
