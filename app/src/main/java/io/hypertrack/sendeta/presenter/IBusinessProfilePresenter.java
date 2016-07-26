package io.hypertrack.sendeta.presenter;

/**
 * Created by piyush on 22/07/16.
 */
public interface IBusinessProfilePresenter<V> extends Presenter<V> {
    void getMembershipForAccountId(int accountId);
    void attemptVerifyPendingBusinessProfile();
}
