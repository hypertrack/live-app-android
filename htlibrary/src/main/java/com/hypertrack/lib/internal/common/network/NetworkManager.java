package com.hypertrack.lib.internal.common.network;

import android.content.Context;

/**
 * Created by piyush on 18/09/16.
 */
public interface NetworkManager {
    MqttClient getMqttClient();
    void setUserID(String userID);
    void execute(final Context context, final HyperTrackNetworkRequest<?> networkRequest);
    void cancel(Object tag);
    void disconnect();
}
