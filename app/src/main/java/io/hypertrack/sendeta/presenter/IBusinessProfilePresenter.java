package io.hypertrack.sendeta.presenter;

import io.hypertrack.sendeta.model.Membership;

/**
 * Created by piyush on 22/07/16.
 */
public interface IBusinessProfilePresenter<V> extends Presenter<V> {
    void getMembershipForAccountId();
    void attemptVerifyPendingBusinessProfile(final Membership membership);
}
