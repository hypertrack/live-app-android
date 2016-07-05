package io.hypertrack.sendeta.view;

public interface VerifyView {
    void verificationFailed();
    void navigateToProfileScreen();
    void showValidationError();
    void showResendError();
    void didResendVerificationCode();
    void updateHeaderText(String text);
}
