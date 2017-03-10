package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

/**
 * Created by piyush on 07/07/16.
 */

public class TaskListResponse {

    @SerializedName("results")
    private List<HTTask> taskList;

    public List<HTTask> getTaskList() {
        return taskList;
    }

    @Override
    public String toString() {
        return "TaskListResponse{" +
                "taskList=" + Arrays.toString(taskList.toArray());
    }
}
