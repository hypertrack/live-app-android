package io.hypertrack.sendeta.presenter;

import android.text.TextUtils;

import io.hypertrack.sendeta.interactor.RegisterInteractor;
import io.hypertrack.sendeta.interactor.callback.OnRegisterCallback;
import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.view.RegisterView;

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

        if(!TextUtils.isEmpty(number)) {
            onboardingManager.getUser().setCountryCode(isoCode);
            OnboardingUser.setOnboardingUser();

            registerInteractor.registerPhoneNumber(new OnRegisterCallback() {
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
            if (view != null) {
                view.showValidationError();
            }

            AnalyticsStore.getLogger().enteredPhoneNumber(false, ErrorMessages.INVALID_PHONE_NUMBER);
        }
    }
}
