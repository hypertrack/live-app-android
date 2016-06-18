package io.hypertrack.meta.interactor;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.interactor.callback.OnVerificationCallback;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.store.OnOnboardingCallback;
import io.hypertrack.meta.store.OnboardingManager;
import io.hypertrack.meta.util.Constants;
import io.hypertrack.meta.util.SharedPreferenceManager;

public class VerificationInteractor {

    public void validateVerificationCode(String verificationCode, final OnVerificationCallback onVerificationCallback) {
        OnboardingManager.sharedManager().verifyCode(verificationCode, new OnOnboardingCallback() {
            @Override
            public void onSuccess() {
                onVerificationCallback.OnSuccess();
            }

            @Override
            public void onError() {
                onVerificationCallback.OnError();
            }
        });
    }

    public void resendVerificationCode(int userID, final OnVerificationCallback callback) {
        OnboardingManager.sharedManager().resendVerificationCode(new OnOnboardingCallback() {
            @Override
            public void onSuccess() {
                callback.OnSuccess();
            }

            @Override
            public void onError() {
                callback.OnError();
            }
        });
    }
}
