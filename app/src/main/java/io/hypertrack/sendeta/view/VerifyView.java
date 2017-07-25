package io.hypertrack.sendeta.view;

/**
 * Created by Aman on 19/07/17.
 */

public interface VerifyView {

    void showError(String message);

    void codeVerified();

    void codeResent();
}
