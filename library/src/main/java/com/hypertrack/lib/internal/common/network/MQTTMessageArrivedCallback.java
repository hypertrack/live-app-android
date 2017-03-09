package com.hypertrack.lib.internal.common.network;


/**
 * MessageArrivedCallback will be called on arrival of a message on the subscribed topic
 */
public interface MQTTMessageArrivedCallback {
    void onMessageArrived(String topic, String message);
}
