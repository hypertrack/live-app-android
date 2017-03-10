package com.example.hypertracklive.interactor;

import android.graphics.Bitmap;

import io.hypertrack.sendeta.interactor.callback.OnProfilePicUploadCallback;
import io.hypertrack.sendeta.interactor.callback.OnProfileUpdateCallback;
import io.hypertrack.sendeta.interactor.callback.OnRegisterCallback;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.callback.OnOnboardingCallback;
import io.hypertrack.sendeta.store.callback.OnOnboardingImageUploadCallback;

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

     public void updateUserProfilePic(final Bitmap oldProfileImage, final Bitmap updatedProfileImage,
                                     final OnProfilePicUploadCallback callback) {

        OnboardingManager.sharedManager().uploadPhoto(oldProfileImage, updatedProfileImage,
                new OnOnboardingImageUploadCallback() {
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

            @Override
            public void onImageUploadNotNeeded() {
                if (callback != null) {
                    callback.onImageUploadNotNeeded();
                }
            }
        });
    }
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
