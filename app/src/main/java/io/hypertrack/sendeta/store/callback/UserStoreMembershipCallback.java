package io.hypertrack.sendeta.store.callback;

import io.hypertrack.sendeta.model.Membership;

/**
 * Created by piyush on 26/07/16.
 */
public abstract class UserStoreMembershipCallback {
    public abstract void OnSuccess(Membership membership);
    public abstract void OnError();
}
