package com.hypertrack.lib.internal.consumer.models;

import java.util.List;

/**
 * Created by piyush on 07/07/16.
 */
public interface ActionListCallBack {
    // TODO: 07/07/16 Add implementation to return failure for selected Tasks
    void onSuccess(List<HTAction> actionList);

    void onError(Exception exception);
}
