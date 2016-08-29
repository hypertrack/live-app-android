package io.hypertrack.sendeta.store.callback;

import java.util.Map;

/**
 * Created by ulhas on 19/06/16.
 */
public abstract class UserStoreGetTaskCallback {
    public abstract void OnSuccess(Map<String, Object> response);
    public abstract void OnError();
}
