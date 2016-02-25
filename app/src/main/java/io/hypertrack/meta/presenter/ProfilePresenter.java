package io.hypertrack.meta.presenter;

import android.text.TextUtils;

import java.io.File;

import io.hypertrack.meta.interactor.ProfileInteractor;
import io.hypertrack.meta.interactor.ProfileUpdateListener;
import io.hypertrack.meta.util.HTConstants;
import io.hypertrack.meta.view.ProfileView;

/**
 * Created by suhas on 24/02/16.
 */
public class ProfilePresenter implements Presenter<ProfileView>, ProfileUpdateListener {

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

    public void attemptLogin(String userFirstName, String userLastName, int userId, File profileImage) {

        if (TextUtils.isEmpty(userFirstName)) {
            view.showFirstNameValidationError();
            return;
        }

        if (TextUtils.isEmpty(userLastName)) {
            view.showLastNameValidationError();
            return;
        }

        profileInteractor.updateUserProfileRetro(this, userFirstName, userLastName, userId, profileImage);

    }

    @Override
    public void OnSuccess() {
        view.navigateToHomeScreen();
    }

    @Override
    public void OnError() {
        view.showErrorMessage();
    }

}
