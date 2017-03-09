package com.hypertrack.lib.internal.common.network;

import org.eclipse.paho.client.mqttv3.IMqttToken;

/**
 * Created by piyush on 26/10/16.
 */
public class MQTTSubscription {

    private String topic;
    private MQTTMessageArrivedCallback messageArrivedCallback;
    private MQTTSubscriptionSuccessCallback subscriptionSuccessCallback;

    private IMqttToken subscribeToken;

    public MQTTSubscription(String topic, MQTTMessageArrivedCallback messageArrivedCallback,
                            MQTTSubscriptionSuccessCallback subscriptionSuccessCallback) {
        this.topic = topic;
        this.messageArrivedCallback = messageArrivedCallback;
        this.subscriptionSuccessCallback = subscriptionSuccessCallback;
    }

    public MQTTSubscription(String topic, MQTTMessageArrivedCallback messageArrivedCallback) {
        this(topic, messageArrivedCallback, null);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MQTTMessageArrivedCallback getMessageArrivedCallback() {
        return messageArrivedCallback;
    }

    public void setMessageArrivedCallback(MQTTMessageArrivedCallback messageArrivedCallback) {
        this.messageArrivedCallback = messageArrivedCallback;
    }

    public MQTTSubscriptionSuccessCallback getSubscriptionSuccessCallback() {
        return subscriptionSuccessCallback;
    }

    public void setSubscriptionSuccessCallback(MQTTSubscriptionSuccessCallback subscriptionSuccessCallback) {
        this.subscriptionSuccessCallback = subscriptionSuccessCallback;
    }

    public IMqttToken getSubscribeToken() {
        return subscribeToken;
    }

    public void setSubscribeToken(IMqttToken subscribeToken) {
        this.subscribeToken = subscribeToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MQTTSubscription that = (MQTTSubscription) o;

        if (!topic.equals(that.topic)) return false;
        if (!messageArrivedCallback.equals(that.messageArrivedCallback)) return false;
        return subscriptionSuccessCallback.equals(that.subscriptionSuccessCallback);

    }

    @Override
    public int hashCode() {
        int result = topic.hashCode();
        result = 31 * result + messageArrivedCallback.hashCode();
        result = 31 * result + subscriptionSuccessCallback.hashCode();
        return result;
    }
}
