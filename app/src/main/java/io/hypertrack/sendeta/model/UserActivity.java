package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import io.hypertrack.lib.common.model.HTTask;

/**
 * Created by piyush on 02/09/16.
 */
public class UserActivity {

    @SerializedName("id")
    private int id;

    @SerializedName("user")
    private int userID;

    @SerializedName("hypertrack_task_id")
    private String taskID;

    @SerializedName("account_id")
    private int accountID;

    @SerializedName("is_pending")
    private boolean isPendingUserAcceptance;

    @SerializedName("is_live")
    private boolean inProcess;

    @SerializedName("share_url")
    private String shareUrl;

    @SerializedName("details")
    private HTTask taskDetails;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String  getTaskID() {
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

    public boolean isPendingUserAcceptance() {
        return isPendingUserAcceptance;
    }

    public void setPendingUserAcceptance(boolean pendingUserAcceptance) {
        isPendingUserAcceptance = pendingUserAcceptance;
    }

    public boolean isInProcess() {
        return inProcess;
    }

    public void setInProcess(boolean inProcess) {
        this.inProcess = inProcess;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public HTTask getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(HTTask taskDetails) {
        this.taskDetails = taskDetails;
    }

    @Override
    public String toString() {
        return "UserActivity{" +
                "id=" + id +
                ", userID=" + userID +
                ", taskID=" + taskID +
                ", accountID=" + accountID +
                ", isPendingUserAcceptance=" + isPendingUserAcceptance +
                ", inProcess=" + inProcess +
                ", shareUrl='" + (shareUrl != null ? shareUrl : "null") + '\'' +
                ", taskDetails=" + (taskDetails != null ? taskDetails + "" : "null") +
                '}';
    }
}
