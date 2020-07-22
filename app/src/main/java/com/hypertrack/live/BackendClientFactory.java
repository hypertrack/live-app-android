package com.hypertrack.live;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hypertrack.backend.AbstractBackendProvider;
import com.hypertrack.backend.HybridBackendProvider;
import com.hypertrack.live.utils.SharedHelper;

public class BackendClientFactory {

    @Nullable public static AbstractBackendProvider getBackendProvider(Context context, @NonNull String deviceId) {

        @NonNull String publishableKey = SharedHelper.getInstance(context).getHyperTrackPubKey();

        if (publishableKey.isEmpty()) return null;

        return HybridBackendProvider.getInstance(context, publishableKey, deviceId);

    }

}
