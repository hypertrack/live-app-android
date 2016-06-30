package io.hypertrack.sendeta.interactor;

import io.hypertrack.sendeta.interactor.callback.OnProfilePicUploadCallback;
import io.hypertrack.sendeta.interactor.callback.OnProfileUpdateCallback;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.callback.OnOnboardingCallback;

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

    public void updateUserProfilePic(final OnProfilePicUploadCallback callback) {

        OnboardingManager.sharedManager().uploadPhoto(new OnOnboardingCallback() {
            @Override
            public void onSuccess() {

                if (callback != null) {
                    callback.OnSuccess();
                }
            }

            @Override
            public void onError() {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }
}
