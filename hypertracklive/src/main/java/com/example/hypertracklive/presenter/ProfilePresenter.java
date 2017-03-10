package com.example.hypertracklive.presenter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.io.File;

import io.hypertrack.sendeta.interactor.ProfileInteractor;
import io.hypertrack.sendeta.interactor.callback.OnRegisterCallback;
import io.hypertrack.sendeta.model.OnboardingUser;
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
    private ProfileInteractor profileInteractor;
    private OnboardingManager onboardingManager = OnboardingManager.sharedManager();

    @Override
    public void attachView(ProfileView view) {
        this.view = view;
        profileInteractor = new ProfileInteractor();

        OnboardingUser user = this.onboardingManager.getUser();
        this.view.updateViews(user.getName(), user.getPhone(), user.getCountryCode(), user.getPhotoURL());
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void attemptLogin(String userName, String phone, String ISOCode, final File profileImage,
                             final Bitmap oldProfileImage, final Bitmap updatedProfileImage) {


        final OnboardingUser user = this.onboardingManager.getUser();
        if (!TextUtils.isEmpty(userName))
            user.setName(userName);
        if (!TextUtils.isEmpty(phone))
            user.setPhone(phone);
        if (!TextUtils.isEmpty(ISOCode))
            user.setCountryCode(ISOCode);
        if (profileImage != null && profileImage.length() > 0) {
            user.setPhotoImage(profileImage);
        }
        OnboardingUser.setOnboardingUser();
        if (TextUtils.isEmpty(userName) && TextUtils.isEmpty(phone)) {
            if (view != null) {
                view.navigateToHomeScreen();
                return;
            }
        }

        if (TextUtils.isEmpty(userName)) {
            HyperTrack.createUser(new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse successResponse) {
                    if (view != null) {
                        view.registrationSuccessful();

                    }
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {

                }
            });
        } else {
            HyperTrack.createUser(userName, new HyperTrackCallback() {
                @Override
                public void onSuccess(@NonNull SuccessResponse successResponse) {
                    AnalyticsStore.getLogger().enteredName(true, null);
                    AnalyticsStore.getLogger().completedProfileSetUp(user.isExistingUser());
                    AnalyticsStore.getLogger().uploadedProfilePhoto(true, null);
                    if (view != null) {
                        view.navigateToHomeScreen();
                    }
                }

                @Override
                public void onError(@NonNull ErrorResponse errorResponse) {
                    HTLog.i(TAG, "");
                    if (view != null) {
                        view.showErrorMessage();
                    }
                    AnalyticsStore.getLogger().enteredName(false, ErrorMessages.PROFILE_UPDATE_FAILED);

                }
            });
        }
    }

    @Override
    public void attemptRegistration(String number, String ISOCode) {
        if (!TextUtils.isEmpty(number)) {
            onboardingManager.getUser().setPhone(number);
            onboardingManager.getUser().setCountryCode(ISOCode);
            OnboardingUser.setOnboardingUser();
            profileInteractor.registerPhoneNumber(new OnRegisterCallback() {
                @Override
                public void OnSuccess() {
                    if (view != null) {
                        view.registrationSuccessful();
                    }

                    AnalyticsStore.getLogger().enteredPhoneNumber(true, null);
                }

                @Override
                public void OnError() {
                    if (view != null) {
                        view.registrationFailed();
                    }

                    AnalyticsStore.getLogger().enteredPhoneNumber(false, ErrorMessages.PHONE_NO_REGISTRATION_FAILED);
                }
            });
        } else {
            AnalyticsStore.getLogger().enteredPhoneNumber(false, ErrorMessages.INVALID_PHONE_NUMBER);
        }
    }
}

