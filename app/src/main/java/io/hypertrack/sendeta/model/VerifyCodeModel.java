package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Aman on 19/07/17.
 */

public class VerifyCodeModel implements Serializable {

    @SerializedName("verification_code")
    String verificationCode;

    public VerifyCodeModel(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
