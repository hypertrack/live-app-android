package io.hypertrack.meta.presenter;

import android.text.TextUtils;

import io.hypertrack.meta.interactor.callback.OnVerificationCallback;
import io.hypertrack.meta.interactor.VerificationInteractor;
import io.hypertrack.meta.store.AnalyticsStore;
import io.hypertrack.meta.store.OnboardingManager;
import io.hypertrack.meta.util.ErrorMessages;
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
    public void attemptVerification(String verificationCode, final int retryCount) {
        if (!TextUtils.isEmpty(verificationCode) && verificationCode.length() == 4) {
            verificationInteractor.validateVerificationCode(verificationCode, new OnVerificationCallback() {
                @Override
                public void OnSuccess() {
                    if (view != null) {
                        view.navigateToProfileScreen();
                    }

                    AnalyticsStore.getLogger().enteredCorrectOTP(true, null, retryCount);
                }

                @Override
                public void OnError() {
                    if (view != null) {
                        view.verificationFailed();
                    }

                    AnalyticsStore.getLogger().enteredCorrectOTP(false,
                            ErrorMessages.PHONE_NO_VERIFICATION_FAILED, retryCount);
                }
            });
        } else {
            if (view != null) {
                view.showValidationError();
            }

            AnalyticsStore.getLogger().enteredCorrectOTP(false, ErrorMessages.INVALID_VERIFICATION_CODE,
                    retryCount);
        }
    }

    @Override
    public void resendVerificationCode(final int resendCount) {
        verificationInteractor.resendVerificationCode(new OnVerificationCallback() {
                @Override
                public void OnSuccess() {
                    if (view != null) {
                        view.didResendVerificationCode();
                    }

                    AnalyticsStore.getLogger().resendOTP(true, null, resendCount);
                }

                @Override
                public void OnError() {
                    if (view != null) {
                        view.showResendError();
                    }

                    AnalyticsStore.getLogger().resendOTP(false, ErrorMessages.RESEND_OTP_FAILED, resendCount);
                }
            }

        );
    }
}
