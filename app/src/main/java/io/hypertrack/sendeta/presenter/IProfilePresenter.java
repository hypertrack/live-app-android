package io.hypertrack.sendeta.presenter;

import java.io.File;

/**
 * Created by ulhas on 19/05/16.
 */
public interface IProfilePresenter<V> extends Presenter<V> {
    void attemptLogin(String userFirstName, String userLastName, File profileImage);
}
