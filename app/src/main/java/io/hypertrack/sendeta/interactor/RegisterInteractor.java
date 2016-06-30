package io.hypertrack.sendeta.interactor;

import io.hypertrack.sendeta.interactor.callback.OnRegisterCallback;
import io.hypertrack.sendeta.store.callback.OnOnboardingCallback;
import io.hypertrack.sendeta.store.OnboardingManager;

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
