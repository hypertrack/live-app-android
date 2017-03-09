package com.hypertrack.lib.models;

import android.support.annotation.NonNull;

import com.hypertrack.lib.internal.common.util.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by piyush on 11/08/16.
 */
public class ServiceNotificationAction implements Serializable {

    private int actionIconResId;
    private String actionText;
    private String actionIntentClassName;
    private ArrayList<String> actionIntentExtras;
    private ServiceNotificationActionIntentType actionIntentType = ServiceNotificationActionIntentType.ACTION_INTENT_TYPE_ACTIVITY;

    private ServiceNotificationAction() {
    }

    /**
     * Call this constructor to instantiate an instance of ServiceNotificationAction to add Action
     * Buttons in Service Notification
     *
     * @param actionIconResId       Resource Id of the small icon representing the action.
     * @param actionText            Title of the action.
     * @param actionIntentClassName Name of the class to be invoked on Action Button click.
     * @param actionIntentType      Type of Intent to be set up for Action Button Click.
     * @param actionIntentExtras    ArrayList of String extras to be delivered along with Action Button clicked intent.
     */
    public ServiceNotificationAction(int actionIconResId, String actionText, @NonNull String actionIntentClassName,
                                     ServiceNotificationActionIntentType actionIntentType, ArrayList<String> actionIntentExtras) {
        this.actionIconResId = actionIconResId;
        this.actionText = actionText;
        this.actionIntentClassName = actionIntentClassName;
        if (!TextUtils.isEmpty(actionIntentType.toString())) {
            this.actionIntentType = actionIntentType;
        }
        this.actionIntentExtras = actionIntentExtras;
    }

    public int getActionIconResId() {
        return actionIconResId;
    }

    public void setActionIconResId(int actionIconResId) {
        this.actionIconResId = actionIconResId;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getActionIntentClassName() {
        return actionIntentClassName;
    }

    public void setActionIntentClassName(String actionIntentClassName) {
        this.actionIntentClassName = actionIntentClassName;
    }

    public ServiceNotificationActionIntentType getActionIntentType() {
        return actionIntentType;
    }

    public void setActionIntentType(ServiceNotificationActionIntentType actionIntentType) {
        this.actionIntentType = actionIntentType;
    }

    public ArrayList<String> getActionIntentExtras() {
        return actionIntentExtras;
    }

    public void setActionIntentExtras(ArrayList<String> actionIntentExtras) {
        this.actionIntentExtras = actionIntentExtras;
    }

    @Override
    public String toString() {
        return "ServiceNotificationAction{" +
                "actionIconResId=" + actionIconResId +
                ", actionText='" + (actionText != null ? actionText : "") + '\'' +
                ", actionIntentClassName=" + (actionIntentClassName != null ? actionIntentClassName : "") +
                ", actionIntentType=" + (actionIntentType != null ? actionIntentType : "") +
                ", actionIntentExtras=" + (actionIntentExtras != null ? actionIntentExtras : "") +
                '}';
    }
}