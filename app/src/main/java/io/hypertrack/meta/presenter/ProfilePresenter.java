package io.hypertrack.meta.presenter;

import android.text.TextUtils;

import java.io.File;

import io.hypertrack.meta.interactor.ProfileInteractor;
import io.hypertrack.meta.interactor.callback.OnProfileUpdateCallback;
import io.hypertrack.meta.view.ProfileView;

/**
 * Created by suhas on 24/02/16.
 */
public class ProfilePresenter implements IProfilePresenter<ProfileView> {

    private ProfileView view;
    private ProfileInteractor profileInteractor;

    @Override
    public void attachView(ProfileView view) {
        this.view = view;
        profileInteractor = new ProfileInteractor();
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void attemptLogin(String userFirstName, String userLastName, int userId, File profileImage) {

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

        profileInteractor.updateUserProfileRetro(userFirstName, userLastName, userId, profileImage, new OnProfileUpdateCallback() {
            @Override
            public void OnSuccess() {
                if (view != null) {
                    view.navigateToHomeScreen();
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
