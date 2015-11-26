package io.hypertrack.meta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by suhas on 12/11/15.
 */
public class Verification {

    @SerializedName("verification_code")
    public String verificationCode;

    public Verification(String code) {
        verificationCode = code;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    @Override
    public String toString() {
        return "Verification{" +
                "verificationCode='" + verificationCode + '\'' +
                '}';
    }
}
