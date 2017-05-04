package io.hypertrack.sendeta.presenter;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by ulhas on 19/05/16.
 */
public interface IProfilePresenter<V> extends Presenter<V> {
    void attemptLogin(String userFirstName, String number, String deviceID, String ISOCode, File profileImageFile,
                      Bitmap oldProfileImage, Bitmap updatedProfileImage);
}
