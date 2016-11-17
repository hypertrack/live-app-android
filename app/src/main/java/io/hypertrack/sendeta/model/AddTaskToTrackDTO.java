package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 29/08/16.
 */
public class AddTaskToTrackDTO {

    @SerializedName("task_id")
    private String taskID;

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public AddTaskToTrackDTO(String taskID) {
        this.taskID = taskID;
    }
}
