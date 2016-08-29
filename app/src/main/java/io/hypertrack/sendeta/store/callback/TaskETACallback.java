package io.hypertrack.sendeta.store.callback;

import io.hypertrack.sendeta.model.TaskETAResponse;

/**
 * Created by piyush on 15/08/16.
 */
public abstract class TaskETACallback {
    public abstract void OnSuccess(TaskETAResponse etaResponse);
    public abstract void OnError();
}
