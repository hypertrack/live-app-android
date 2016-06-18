package io.hypertrack.meta.presenter;

/**
 * Created by ulhas on 19/05/16.
 */
public interface IVerifyPresenter<V> extends Presenter<V> {
    void attemptVerification(String verificationCode);
    void resendVerificationCode();
}
