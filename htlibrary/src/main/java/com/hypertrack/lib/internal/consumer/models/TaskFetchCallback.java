package com.hypertrack.lib.internal.consumer.models;

import java.util.ArrayList;

/**
 * Created by suhas on 30/08/15.
 */
public interface TaskFetchCallback {
    void onFetchTask(boolean result, ArrayList<String> taskID);
}
