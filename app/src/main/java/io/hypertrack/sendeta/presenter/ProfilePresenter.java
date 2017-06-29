package io.hypertrack.sendeta.presenter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.hypertrack.sendeta.callback.OnOnboardingImageUploadCallback;
import io.hypertrack.sendeta.model.HyperTrackLiveUser;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.view.Profile;
import io.hypertrack.sendeta.view.ProfileView;

/**
 * Created by suhas on 24/02/16.
 */
public class ProfilePresenter implements IProfilePresenter<ProfileView> {

    private static final String TAG = Profile.class.getSimpleName();
    private ProfileView view;
    private OnboardingManager onboardingManager = OnboardingManager.sharedManager();

    @Override
    public void attachView(ProfileView view) {
        this.view = view;
        HyperTrackLiveUser user = this.onboardingManager.getUser();
        this.view.updateViews(user.getName(), user.getPhone(), user.getCountryCode(), user.getPhoto());
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void attemptLogin(final String userName, String phone, String ISOCode, final String deviceID, final File profileImage,
                             final Bitmap oldProfileImage, final Bitmap updatedProfileImage) {
        final HyperTrackLiveUser user = this.onboardingManager.getUser();

        // Update Country Code from device's current location
        if (!HTTextUtils.isEmpty(ISOCode))
            user.setCountryCode(ISOCode);

        String encodedImage = null;
        // Set user's profile image
        if (profileImage != null && profileImage.length() > 0) {
            user.setPhotoImage(profileImage);
            byte[] bytes = convertFiletoByteArray(profileImage);
            if (bytes != null && bytes.length > 0)
                encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);
        }

        HyperTrackLiveUser.setHyperTrackLiveUser();

        try {
            HyperTrack.getOrCreateUser(userName, user.getInternationalNumber(phone), encodedImage, user.getInternationalNumber(phone) + "_" + deviceID,
                    new HyperTrackCallback() {
                        @Override
                        public void onSuccess(@NonNull SuccessResponse successResponse) {
                            AnalyticsStore.getLogger().enteredName(true, null);
                            AnalyticsStore.getLogger().uploadedProfilePhoto(true, null);

                            if (profileImage != null && profileImage.length() > 0) {
                                onboardingManager.uploadPhoto(oldProfileImage, updatedProfileImage, new OnOnboardingImageUploadCallback() {
                                    @Override
                                    public void onSuccess() {
                                        if (view != null) {
                                            view.showProfilePicUploadSuccess();
                                            view.navigateToPlacelineScreen();
                                        }
                                    }

                                    @Override
                                    public void onError() {
                                        Log.i(TAG, "Profile Image not saved in local database");
                                        if (view != null) {
                                            view.showProfilePicUploadError();
                                        }
                                    }

                                    @Override
                                    public void onImageUploadNotNeeded() {
                                    }
                                });
                            } else {
                                if (view != null) {
                                    view.navigateToPlacelineScreen();
                                }
                            }
                        }

                        @Override
                        public void onError(@NonNull ErrorResponse errorResponse) {
                            if (view != null) {
                                view.showErrorMessage();
                            }
                            AnalyticsStore.getLogger().enteredName(false, ErrorMessages.PROFILE_UPDATE_FAILED);

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            if (view != null) {
                view.showErrorMessage();
            }
            AnalyticsStore.getLogger().enteredName(false, ErrorMessages.INVALID_PHONE_NUMBER);
        }
    }

    private byte[] convertFiletoByteArray(File file) {
        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
            for (int i = 0; i < b.length; i++) {
                System.out.print((char) b[i]);
            }
            return b;
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
        return b;
    }
}

