package io.hypertrack.sendeta.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by piyush on 22/07/16.
 */
public class AccountProfile implements Serializable {

    @SerializedName("account_id")
    private int accountId;

    @SerializedName("is_accepted")
    private boolean isAccepted;

    @SerializedName("name")
    private String name;

    @SerializedName("publishable_key")
    private String publishableKey;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublishableKey() {
        return publishableKey;
    }

    public void setPublishableKey(String publishableKey) {
        this.publishableKey = publishableKey;
    }

    public String getHypertrackDriverId() {
        return hypertrackDriverId;
    }

    public void setHypertrackDriverId(String hypertrackDriverId) {
        this.hypertrackDriverId = hypertrackDriverId;
    }

    private AccountProfile() {
    }

    public AccountProfile(int accountId, @NonNull String name, @NonNull String hypertrackDriverId, @NonNull String publishableKey) {
        this.accountId = accountId;
        this.name = name;
        this.isAccepted = true;
        this.publishableKey = publishableKey;
        this.hypertrackDriverId = hypertrackDriverId;
    }
}
