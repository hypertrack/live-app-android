package com.hypertrack.lib.models;

import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piyush on 11/08/16.
 */
public class ServiceNotificationParams {

    private int smallIconResId = 0;
    private int smallIconBGColor = 0;
    private int largeIconResId = 0;
    private String contentTitle = null;
    private String contentText = null;
    private String contentIntentActivityClassName = null;
    private ArrayList<String> contentIntentExtras = null;
    private RemoteViews contentView;
    private List<ServiceNotificationAction> actionsList = null;

    public int getSmallIconResId() {
        return smallIconResId;
    }

    public void setSmallIconResId(int smallIconResId) {
        this.smallIconResId = smallIconResId;
    }

    public int getSmallIconBGColor() {
        return smallIconBGColor;
    }

    public void setSmallIconBGColor(int smallIconBGColor) {
        this.smallIconBGColor = smallIconBGColor;
    }

    public int getLargeIconResId() {
        return largeIconResId;
    }

    public void setLargeIconResId(int largeIconResId) {
        this.largeIconResId = largeIconResId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public List<ServiceNotificationAction> getActionsList() {
        return actionsList;
    }

    public void setActionsList(List<ServiceNotificationAction> actionsList) {
        this.actionsList = actionsList;
    }

    public RemoteViews getContentView() {
        return contentView;
    }

    public void setContentView(RemoteViews contentView) {
        this.contentView = contentView;
    }

    public String getContentIntentActivityClassName() {
        return contentIntentActivityClassName;
    }

    public void setContentIntentActivityClassName(String contentIntentActivityClassName) {
        this.contentIntentActivityClassName = contentIntentActivityClassName;
    }

    public ArrayList<String> getContentIntentExtras() {
        return contentIntentExtras;
    }

    public void setContentIntentExtras(ArrayList<String> contentIntentExtras) {
        this.contentIntentExtras = contentIntentExtras;
    }

    public ServiceNotificationParams(String contentTitle, String contentIntentActivityClassName) {
        this.contentTitle = contentTitle;
        this.contentIntentActivityClassName = contentIntentActivityClassName;
    }

    public ServiceNotificationParams(String contentTitle, String contentText, String contentIntentActivityClassName) {
        this(contentTitle, contentIntentActivityClassName);
        this.contentText = contentText;
    }

    public ServiceNotificationParams(String contentTitle, String contentText, String contentIntentActivityClassName,
                                     List<ServiceNotificationAction> actionsList) {
        this(contentTitle, contentText, contentIntentActivityClassName);
        this.actionsList = actionsList;
    }

    public ServiceNotificationParams(String contentTitle, String contentText, String contentIntentActivityClassName,
                                     List<ServiceNotificationAction> actionsList, RemoteViews contentView) {
        this(contentTitle, contentText, contentIntentActivityClassName, actionsList);
        this.contentView = contentView;
    }

    public ServiceNotificationParams(String contentTitle, String contentText, String contentIntentActivityClassName,
                                     List<ServiceNotificationAction> actionsList, RemoteViews contentView,
                                     int smallIconResId, int largeIconResId) {
        this(contentTitle, contentText, contentIntentActivityClassName, actionsList, contentView);
        this.smallIconResId = smallIconResId;
        this.largeIconResId = largeIconResId;
    }

    public ServiceNotificationParams(String contentTitle, String contentText, String contentIntentActivityClassName,
                                     List<ServiceNotificationAction> actionsList, RemoteViews contentView,
                                     int smallIconResId, int largeIconResId, int smallIconBGColor) {
        this(contentTitle, contentText, contentIntentActivityClassName, actionsList, contentView,
                smallIconResId, largeIconResId);
        this.smallIconBGColor = smallIconBGColor;
    }

    public ServiceNotificationParams(String contentTitle, String contentText, String contentIntentActivityClassName,
                                     List<ServiceNotificationAction> actionsList, RemoteViews contentView,
                                     int smallIconResId, int largeIconResId, int smallIconBGColor,
                                     ArrayList<String> contentIntentExtras) {
        this(contentTitle, contentText, contentIntentActivityClassName, actionsList, contentView,
                smallIconResId, largeIconResId, smallIconBGColor);
        this.contentIntentExtras = contentIntentExtras;
    }

    @Override
    public String toString() {
        return "ServiceNotificationParams{" +
                "smallIconResId='" + smallIconResId + '\'' +
                ", smallIconBGColor='" + smallIconBGColor + '\'' +
                ", largeIconResId='" + largeIconResId + '\'' +
                ", title='" + (contentTitle != null ? contentTitle : "null") + '\'' +
                ", text='" + (contentText != null ? contentText : "null") + '\'' +
                ", contentIntentActivityClassName='" + (contentIntentActivityClassName != null ? contentIntentActivityClassName : "null") + '\'' +
                ", contentIntentExtras=" + (contentIntentExtras != null ? contentIntentExtras : "null") + '\'' +
                ", actionsList=" + (actionsList != null ? actionsList : "null") + '\'' +
                ", contentView=" + (contentView != null ? contentView : "null") +
                '}';
    }
}
