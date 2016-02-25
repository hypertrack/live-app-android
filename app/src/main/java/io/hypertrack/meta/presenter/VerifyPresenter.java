package io.hypertrack.meta.presenter;

import android.text.TextUtils;

import io.hypertrack.meta.interactor.OnVerificationListener;
import io.hypertrack.meta.interactor.VerificationInteractor;
import io.hypertrack.meta.view.VerifyView;

public class VerifyPresenter implements Presenter<VerifyView>, OnVerificationListener {

    private static final String TAG = VerifyPresenter.class.getSimpleName();
    private VerifyView view;
    private VerificationInteractor verificationInteractor;

    @Override
    public void attachView(VerifyView view) {
        this.view = view;
        verificationInteractor = new VerificationInteractor();
    }

    @Override
    public void detachView() {
        view = null;
    }

    public void attemptVerification(String verificationCode, int userId) {
        if (!TextUtils.isEmpty(verificationCode) && verificationCode.length() == 4) {
            verificationInteractor.validateVerificationCode(this, verificationCode, userId);
        } else {
            view.showValidationError();
        }
    }

    @Override
    public void OnSuccess() {
        view.navigateToProfileScreen();
    }

    @Override
    public void OnError() {
        view.verificationFailed();
    }
}
