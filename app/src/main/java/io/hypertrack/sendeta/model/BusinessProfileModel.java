package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by piyush on 22/07/16.
 */
public class BusinessProfileModel extends RealmObject implements Serializable{

    @PrimaryKey
    private int id;

    @SerializedName("verified")
    private boolean verified;

    @SerializedName("company_name")
    private String companyName;

    @SerializedName("account_key")
    private String accountKey;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }
}
