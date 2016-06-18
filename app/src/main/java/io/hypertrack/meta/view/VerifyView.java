package io.hypertrack.meta.view;

public interface VerifyView {
    void verificationFailed();
    void navigateToProfileScreen();
    void showValidationError();
    void showResendError();
    void didResendVerificationCode();
}
