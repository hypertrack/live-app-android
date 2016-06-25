package io.hypertrack.meta.presenter;

import android.text.TextUtils;

import io.hypertrack.meta.interactor.callback.OnVerificationCallback;
import io.hypertrack.meta.interactor.VerificationInteractor;
import io.hypertrack.meta.store.OnboardingManager;
import io.hypertrack.meta.util.SharedPreferenceManager;
import io.hypertrack.meta.view.VerifyView;

public class VerifyPresenter implements IVerifyPresenter<VerifyView> {

    private static final String TAG = VerifyPresenter.class.getSimpleName();
    private VerifyView view;
    private VerificationInteractor verificationInteractor;

    @Override
    public void attachView(VerifyView view) {
        this.view = view;
        verificationInteractor = new VerificationInteractor();

        this.view.updateHeaderText(OnboardingManager.sharedManager().getUser().getPhoneNumber());
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void attemptVerification(String verificationCode) {
        if (!TextUtils.isEmpty(verificationCode) && verificationCode.length() == 4) {
            verificationInteractor.validateVerificationCode(verificationCode, new OnVerificationCallback() {
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

    @Override
    public void resendVerificationCode() {
        verificationInteractor.resendVerificationCode(new OnVerificationCallback() {
                @Override
                public void OnSuccess() {
                    if (view != null) {
                        view.didResendVerificationCode();
                    }
                }

                @Override
                public void OnError() {
                    if (view != null) {
                        view.showResendError();
                    }
                }
            }

        );
    }
}
