package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 26/07/16.
 */
public class MembershipDTO {

    @SerializedName("account_id")
    private int accountId;

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public MembershipDTO(Membership membership) {
        this.accountId = membership.getAccountId();
    }
}


