package io.hypertrack.meta.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import io.hypertrack.meta.interactor.OnRegisterListener;
import io.hypertrack.meta.interactor.RegisterInteractor;
import io.hypertrack.meta.view.RegisterView;

public class RegisterPresenter implements Presenter<RegisterView>, OnRegisterListener {

    private static final String TAG = RegisterPresenter.class.getSimpleName();
    private RegisterView view;
    private RegisterInteractor registerInteractor;

    @Override
    public void attachView(RegisterView view) {
        this.view = view;
        registerInteractor = new RegisterInteractor();
    }

    @Override
    public void detachView() {
        view = null;
    }

    public void attemptRegistration(String number, String isoCode) {

        if(!TextUtils.isEmpty(number) && number.length() < 20) {

            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumber = null;
            try {
                phoneNumber = phoneUtil.parse(number, isoCode);
                String internationalFormat = phoneUtil.format(
                        phoneNumber,
                        PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);

                registerInteractor.registerPhoneNumber(RegisterPresenter.this, internationalFormat);
                Log.v(TAG, "International Format: " + internationalFormat);

            } catch (NumberParseException e) {
                if (view != null) {
                    view.showValidationError();
                }
                Log.wtf(TAG, e);
            }

        } else {
            if (view != null) {
                view.showValidationError();
            }
        }
    }

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
}
