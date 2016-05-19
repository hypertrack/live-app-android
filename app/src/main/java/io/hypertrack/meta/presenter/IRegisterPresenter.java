package io.hypertrack.meta.presenter;

/**
 * Created by ulhas on 19/05/16.
 */
public interface IRegisterPresenter<V> extends Presenter<V> {
    void attemptRegistration(String number, String ISOCode);
}
