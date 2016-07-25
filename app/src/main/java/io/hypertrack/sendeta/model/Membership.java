package io.hypertrack.sendeta.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by piyush on 22/07/16.
 */
public class Membership implements Serializable {

    @SerializedName("account_id")
    private int accountId;

    @SerializedName("is_accepted")
    private boolean isAccepted;

    @SerializedName("is_rejected")
    private boolean isRejected;

    @SerializedName("name")
    private String name;

    @SerializedName("account_publishable_key")
    private String accountPublishableKey;

    @SerializedName("hypertrack_account_id")
    private String hypertrackAccountId;

    @SerializedName("hypertrack_driver_id")
    private String hypertrackDriverId;

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        this.isAccepted = accepted;
    }

    public boolean isRejected() {
        return isRejected;
    }

    public void setRejected(boolean rejected) {
        isRejected = rejected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountPublishableKey() {
        return accountPublishableKey;
    }

    public void setAccountPublishableKey(String accountPublishableKey) {
        this.accountPublishableKey = accountPublishableKey;
    }

    public String getHypertrackAccountId() {
        return hypertrackAccountId;
    }

    public void setHypertrackAccountId(String hypertrackAccountId) {
        this.hypertrackAccountId = hypertrackAccountId;
    }

    public String getHypertrackDriverId() {
        return hypertrackDriverId;
    }

    public void setHypertrackDriverId(String hypertrackDriverId) {
        this.hypertrackDriverId = hypertrackDriverId;
    }

    private Membership() {
    }

    public Membership(int accountId, @NonNull String name, @NonNull String hypertrackDriverId, @NonNull String accountPublishableKey) {
        this.accountId = accountId;
        this.name = name;
        this.isAccepted = true;
        this.accountPublishableKey = accountPublishableKey;
        this.hypertrackDriverId = hypertrackDriverId;
    }
}
