package io.hypertrack.meta.presenter;

import android.text.TextUtils;

import java.io.File;

import io.hypertrack.meta.interactor.ProfileInteractor;
import io.hypertrack.meta.interactor.callback.OnProfileUpdateCallback;
import io.hypertrack.meta.model.OnboardingUser;
import io.hypertrack.meta.store.OnboardingManager;
import io.hypertrack.meta.view.ProfileView;

/**
 * Created by suhas on 24/02/16.
 */
public class ProfilePresenter implements IProfilePresenter<ProfileView> {

    private ProfileView view;
    private ProfileInteractor profileInteractor;
    private OnboardingManager onboardingManager = OnboardingManager.sharedManager();

    @Override
    public void attachView(ProfileView view) {
        this.view = view;
        profileInteractor = new ProfileInteractor();

        OnboardingUser user = this.onboardingManager.getUser();
        this.view.updateViews(user.getFirstName(), user.getLastName(), user.getPhotoURL());
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void attemptLogin(String userFirstName, String userLastName, final File profileImage) {

        if (TextUtils.isEmpty(userFirstName)) {
            if (view != null) {
                view.showFirstNameValidationError();
            }
            return;
        }

        if (TextUtils.isEmpty(userLastName)) {
            if (view != null) {
                view.showLastNameValidationError();
            }
            return;
        }

        OnboardingUser user = this.onboardingManager.getUser();
        user.setFirstName(userFirstName);
        user.setLastName(userLastName);

        if (profileImage != null) {
            user.setPhotoImage(profileImage);
        }

        profileInteractor.updateUserProfile(new OnProfileUpdateCallback() {
            @Override
            public void OnSuccess() {
                if (view != null) {
                    view.navigateToHomeScreen();
                }

                if (profileImage != null) {
                    profileInteractor.updateUserProfilePic(profileImage);
                }
            }

            @Override
            public void OnError() {
                if (view != null) {
                    view.showErrorMessage();
                }
            }
        });
    }
}
