package io.hypertrack.sendeta.model;

import android.content.Context;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.network.retrofit.ErrorCodes;

public class ErrorData {

    private String message;
    private String code;

    public ErrorData() {}

    public ErrorData(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage(Context context) {
        return message == null ? context.getString(R.string.generic_error_message) : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code == null ? ErrorCodes.GENERIC_ERROR : code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
