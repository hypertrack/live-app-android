package io.hypertrack.meta.interactor;

import io.hypertrack.meta.interactor.callback.OnVerificationCallback;
import io.hypertrack.meta.store.callback.OnOnboardingCallback;
import io.hypertrack.meta.store.OnboardingManager;

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

    public void resendVerificationCode(final OnVerificationCallback callback) {
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
