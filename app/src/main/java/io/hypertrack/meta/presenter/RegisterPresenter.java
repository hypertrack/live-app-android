package io.hypertrack.meta.presenter;

import android.text.TextUtils;

import io.hypertrack.meta.interactor.callback.OnRegisterCallback;
import io.hypertrack.meta.interactor.RegisterInteractor;
import io.hypertrack.meta.store.OnboardingManager;
import io.hypertrack.meta.view.RegisterView;

public class RegisterPresenter implements IRegisterPresenter<RegisterView> {

    private static final String TAG = RegisterPresenter.class.getSimpleName();
    private RegisterView view;
    private RegisterInteractor registerInteractor;
    private OnboardingManager onboardingManager = OnboardingManager.sharedManager();

    @Override
    public void attachView(RegisterView view) {
        this.view = view;
        registerInteractor = new RegisterInteractor();
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void attemptRegistration(String number, String isoCode) {

        if(!TextUtils.isEmpty(number) && number.length() < 20) {
            onboardingManager.getUser().setContactNumber(number);
            onboardingManager.getUser().setCountryCode(isoCode);

            registerInteractor.registerPhoneNumber(new OnRegisterCallback() {
                @Override
                public void OnSuccess() {
                    if (view != null) {
                        view.navigateToVerificationScreen();
                    }
                }

                @Override
                public void OnError() {
                    if (view != null) {
                        view.registrationFailed();
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
