package io.hypertrack.meta.interactor;

import java.io.File;

import io.hypertrack.meta.interactor.callback.OnProfileUpdateCallback;
import io.hypertrack.meta.store.callback.OnOnboardingCallback;
import io.hypertrack.meta.store.OnboardingManager;

public class ProfileInteractor {

    private static final String TAG = ProfileInteractor.class.getSimpleName();

     public void updateUserProfile(final OnProfileUpdateCallback onProfileUpdateCallback) {

         OnboardingManager.sharedManager().updateInfo(new OnOnboardingCallback() {
             @Override
             public void onSuccess() {
                onProfileUpdateCallback.OnSuccess();
             }

             @Override
             public void onError() {
                onProfileUpdateCallback.OnError();
             }
         });
    }

    public void updateUserProfilePic(File profileImage) {

        OnboardingManager.sharedManager().uploadPhoto(new OnOnboardingCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

            }
        });
    }
}
