package io.hypertrack.sendeta.store.callback;

/**
 * Created by piyush on 29/07/16.
 */
public abstract class UserStoreDeleteMembershipCallback {
    public abstract void OnSuccess(String accountName);
    public abstract void OnError();
}
