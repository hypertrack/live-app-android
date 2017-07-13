package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Aman on 7/4/17.
 */

public class AcceptInviteModel implements Serializable {
    @SerializedName("account_id")
    String accountID;

    public AcceptInviteModel(String accountID) {
        this.accountID = accountID;
    }

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }
}
