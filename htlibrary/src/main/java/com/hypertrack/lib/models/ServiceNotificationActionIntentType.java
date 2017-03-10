package com.hypertrack.lib.models;

/**
 * Created by piyush on 11/08/16.
 */
public enum ServiceNotificationActionIntentType {

    ACTION_INTENT_TYPE_ACTIVITY("activity"),
    ACTION_INTENT_TYPE_SERVICE("service"),
    ACTION_INTENT_TYPE_BROADCAST("broadcast");

    private String actionIntentType;

    ServiceNotificationActionIntentType(String actionIntentType) {
        this.actionIntentType = actionIntentType;
    }

    public String toString() {
        return this.actionIntentType;
    }
}