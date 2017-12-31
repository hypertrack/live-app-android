package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Aman on 7/4/17.
 */

public class AcceptInviteModel implements Serializable {
    @SerializedName("account_id")
    private String accountID;

    @SerializedName("existing_user_id")
    private String existingUserID;

    public AcceptInviteModel(String accountID, String previousUserId) {
        this.accountID = accountID;
        existingUserID = previousUserId;
    }
}
