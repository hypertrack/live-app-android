package io.hypertrack.sendeta.model;

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

    @SerializedName("account_name")
    private String accountName;

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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public boolean isDefault() {
        return this.accountName.equalsIgnoreCase(DEFAULT);
    }

    public Membership() {
    }

    @Override
    public String toString() {
        return "Membership{" +
                "account_id=" + accountId +
                ", is_accepted='" + isAccepted + '\'' +
                ", is_rejected='" + isRejected + '\'' +
                ", name='" + accountName + '\'' +
                '}';
    }
}
