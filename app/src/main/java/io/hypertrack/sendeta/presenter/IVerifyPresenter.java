package io.hypertrack.sendeta.presenter;

import android.content.Context;

/**
 * Created by Aman on 19/07/17.
 */

public interface IVerifyPresenter<V> extends Presenter<V> {

    public static String ERROR_INCORRECT_CODE = "Incorrect verification code";

    void verifyOTP(String OTP, Context context);

    void resendOTP(Context context);
}
