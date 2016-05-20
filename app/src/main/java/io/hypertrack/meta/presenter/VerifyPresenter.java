package io.hypertrack.meta.presenter;

import android.text.TextUtils;

import io.hypertrack.meta.interactor.callback.OnVerificationCallback;
import io.hypertrack.meta.interactor.VerificationInteractor;
import io.hypertrack.meta.view.VerifyView;

public class VerifyPresenter implements IVerifyPresenter<VerifyView> {

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

    @Override
    public void attemptVerification(String verificationCode, int userId) {
        if (!TextUtils.isEmpty(verificationCode) && verificationCode.length() == 4) {
            verificationInteractor.validateVerificationCode(verificationCode, userId, new OnVerificationCallback() {
                @Override
                public void OnSuccess() {
                    if (view != null) {
                        view.navigateToProfileScreen();
                    }
                }

                @Override
                public void OnError() {
                    if (view != null) {
                        view.verificationFailed();
                    }
                }
            });
        } else {
            if (view != null) {
                view.showValidationError();
            }
        }
    }
}
