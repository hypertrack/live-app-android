package io.hypertrack.sendeta.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by piyush on 22/07/16.
 */
public class Membership extends RealmObject implements Serializable {

    public static final String DEFAULT = "default";

    @PrimaryKey
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

    public boolean isDefault() {
        return this.name.equalsIgnoreCase(DEFAULT);
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

    @Override
    public String toString() {
        return "Membership{" +
                "account_id=" + accountId +
                ", is_accepted='" + isAccepted + '\'' +
                ", is_rejected='" + isRejected + '\'' +
                ", name='" + name + '\'' +
                ", hypertrack_account_id='" + hypertrackAccountId + '\'' +
                ", hypertrack_driver_id='" + hypertrackDriverId + '\'' +
                '}';
    }
}
