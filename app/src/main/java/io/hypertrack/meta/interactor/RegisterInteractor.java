package io.hypertrack.meta.interactor;

import io.hypertrack.meta.interactor.callback.OnRegisterCallback;
import io.hypertrack.meta.store.callback.OnOnboardingCallback;
import io.hypertrack.meta.store.OnboardingManager;

public class RegisterInteractor {

    public void registerPhoneNumber(final OnRegisterCallback onRegisterCallback) {
        OnboardingManager.sharedManager().onboardUser(new OnOnboardingCallback() {
            @Override
            public void onSuccess() {
                onRegisterCallback.OnSuccess();
            }

            @Override
            public void onError() {
                onRegisterCallback.OnError();
            }
        });
    }
}
