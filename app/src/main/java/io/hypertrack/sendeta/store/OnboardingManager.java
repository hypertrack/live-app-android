package io.hypertrack.sendeta.store;

import android.graphics.Bitmap;

import java.io.File;

import io.hypertrack.sendeta.callback.OnOnboardingImageUploadCallback;
import io.hypertrack.sendeta.model.HyperTrackLiveUser;

/**
 * Created by ulhas on 18/06/16.
 */
public class OnboardingManager {

    private static OnboardingManager sSharedManager = null;
    private HyperTrackLiveUser hyperTrackLiveUser;

    private OnboardingManager() {
        this.hyperTrackLiveUser = HyperTrackLiveUser.sharedHyperTrackLiveUser();
    }

    public static OnboardingManager sharedManager() {
        if (sSharedManager == null) {
            sSharedManager = new OnboardingManager();
        }

        return sSharedManager;
    }

    public HyperTrackLiveUser getUser() {
        return hyperTrackLiveUser;
    }

    public void uploadPhoto(final Bitmap oldProfileImage, final Bitmap updatedProfileImage,
                            final OnOnboardingImageUploadCallback callback) {
        File profileImage = this.hyperTrackLiveUser.getPhotoImage();

        if (profileImage != null && profileImage.length() > 0) {

            // Check if the profile image has changed from the existing one
            if (updatedProfileImage != null && updatedProfileImage.getByteCount() > 0
                    && !updatedProfileImage.sameAs(oldProfileImage)) {
                this.hyperTrackLiveUser.saveFileAsBitmap(profileImage);
                HyperTrackLiveUser.setHyperTrackLiveUser();
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
