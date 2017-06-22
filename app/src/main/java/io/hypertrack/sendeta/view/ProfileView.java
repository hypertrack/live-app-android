package io.hypertrack.sendeta.view;

/**
 * Created by suhas on 24/02/16.
 */
public interface ProfileView {
    void showProfilePicUploadError();

    void showProfilePicUploadSuccess();

    void navigateToPlacelineScreen();

    void showErrorMessage();

    void updateViews(String name,String phone,String ISOCode ,String profileURL);
}
