package io.hypertrack.sendeta.store;

import android.graphics.Bitmap;

import java.io.File;
import java.util.HashMap;

import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.store.callback.OnOnboardingCallback;
import io.hypertrack.sendeta.store.callback.OnOnboardingImageUploadCallback;

/**
 * Created by ulhas on 18/06/16.
 */
public class OnboardingManager {

    private static String TAG = OnboardingManager.class.getSimpleName();
    private static OnboardingManager sSharedManager = null;
    private OnboardingUser onboardingUser;

    private OnboardingManager() {
        this.onboardingUser = OnboardingUser.sharedOnboardingUser();
    }

    public static OnboardingManager sharedManager() {
        if (sSharedManager == null) {
            sSharedManager = new OnboardingManager();
        }

        return sSharedManager;
    }

    public OnboardingUser getUser() {
        return onboardingUser;
    }

    public void verifyCode(String code, final OnOnboardingCallback callback) {
        HashMap<String, String> verificationParams = new HashMap<>();
        verificationParams.put("verification_code", code);
        verificationParams.put("phone_number", onboardingUser.getPhone());
    }

    public void resendVerificationCode(final OnOnboardingCallback callback) {
        HashMap<String, String> phoneNumber = new HashMap<>();
        phoneNumber.put("phone_number", this.onboardingUser.getPhone());

      /*  Call<Map<String, Object>> call = sendETAService.resendCode(phoneNumber);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.onError();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (callback != null) {
                    callback.onError();
                }
            }
        });*/
    }

    public void uploadPhoto(final Bitmap oldProfileImage, final Bitmap updatedProfileImage,
                            final OnOnboardingImageUploadCallback callback) {
        File profileImage = this.onboardingUser.getPhotoImage();

        if (profileImage != null && profileImage.length() > 0) {
            //UserStore.sharedStore.addImage(profileImage);

            // Check if the profile image has changed from the existing one
            if (updatedProfileImage != null && updatedProfileImage.getByteCount() > 0
                    && !updatedProfileImage.sameAs(oldProfileImage)) {
                this.onboardingUser.saveFileAsBitmap(profileImage);
                OnboardingUser.setOnboardingUser();
                if (callback != null) {
                    callback.onSuccess();
                }
            } else {
                // No need to upload Profile Image since there was no change in it
                if (callback != null) {
                    callback.onImageUploadNotNeeded();
                }
            }
        } else {
            if (callback != null) {
                callback.onError();
            }
        }
    }
}
