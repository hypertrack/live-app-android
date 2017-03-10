package com.hypertrack.lib.internal.consumer.models;

import java.util.List;

/**
 * Created by piyush on 07/07/16.
 */
public interface TaskListCallBack {
    // TODO: 07/07/16 Add implementation to return failure for selected Tasks
    void onSuccess(List<HTTask> taskList);
    void onError(Exception exception);
}
