package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by piyush on 29/08/16.
 */
public class TrackTaskResponse {

    @SerializedName("group_task_ids")
    private ArrayList<String> activeTaskIDList;

    @SerializedName("task_id")
    private String taskID;

    @SerializedName("account_id")
    private int accountID;

    @SerializedName("publishable_key")
    private String publishableKey;

    public ArrayList<String> getActiveTaskIDList() {
        return activeTaskIDList;
    }

    public void setActiveTaskIDList(ArrayList<String> activeTaskIDList) {
        this.activeTaskIDList = activeTaskIDList;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public void setPublishableKey(String publishableKey) {
        this.publishableKey = publishableKey;
    }
}
