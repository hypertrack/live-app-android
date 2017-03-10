package com.hypertrack.lib.internal.consumer.models;

import android.os.Handler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by piyush on 06/07/16.
 */
public class TaskNavigatorList {

    private HashMap<String, TaskNavigator> taskNavigatorList;
    private TaskNavigatorCallback callback;
    private HashMap<String, Handler> taskNavigatorHandlerList;
    private HashMap<String, Handler> taskAnimationHandlerList;

    public TaskNavigatorList(TaskNavigatorCallback callback) {
        taskNavigatorList = new HashMap<>();
        taskNavigatorHandlerList = new HashMap<>();
        taskAnimationHandlerList = new HashMap<>();
        this.callback = callback;
    }

    /**
     * Method to get TaskNavigator from taskNavigatorList by TaskID
     * @param taskID
     * @return
     */
    public TaskNavigator getTaskNavigator(String taskID) {
        return taskNavigatorList.get(taskID);
    }

    public Handler getTaskAnimationHandler(String taskID) {
        if (taskAnimationHandlerList.get(taskID) == null)
            taskAnimationHandlerList.put(taskID, new Handler());

        return taskAnimationHandlerList.get(taskID);
    }

    /**
     * Method to add a taskNavigator in taskNavigatorList
     * @param taskID
     */
    public void addNavigatorForTask(String taskID) {
        if (taskNavigatorHandlerList.get(taskID) == null) {
            taskNavigatorHandlerList.put(taskID, new Handler());
        }

        if (taskAnimationHandlerList.get(taskID) == null) {
            taskAnimationHandlerList.put(taskID, new Handler());
        }

        TaskNavigator taskNavigator = new TaskNavigator(taskID, callback, taskNavigatorHandlerList.get(taskID));
        taskNavigatorList.put(taskNavigator.getTaskID(), taskNavigator);
    }

    /**
     * Method to process Task's TimeAwarePolyline for navigation
     * @param task
     */
    public void processTimeAwarePolyline(HTTask task) {
        if (taskNavigatorList.get(task.getId()) == null)
            addNavigatorForTask(task.getId());

        taskNavigatorList.get(task.getId()).processTimeAwarePolyline(task);
    }

    /**
     * Method to Stop Polling for a TaskID
     * @param taskID
     */
    public void stopPollingForTaskID(String taskID) {
        if (taskNavigatorList.get(taskID) != null) {
            taskNavigatorList.get(taskID).stopPollers();
        }
    }

    /**
     * Method to Stop Polling for all TaskIDs
     */
    public void stopPollingForAllTasks() {
        for (String taskID : taskNavigatorList.keySet()) {
            if (taskNavigatorList.get(taskID) != null) {
                taskNavigatorList.get(taskID).stopPollers();
            }
        }
    }

    /**
     * Method to remove a taskNavigator for a TaskID from taskNavigatorList
     * @param taskID
     * @return
     */
    public TaskNavigator removeNavigator(String taskID) {
        if (taskNavigatorList.get(taskID) != null) {
            this.stopPollingForTaskID(taskID);
            return taskNavigatorList.remove(taskID);
        }

        return null;
    }

    /**
     * Method to remove all taskNavigators corresponding to taskIDs not present in taskIDsToTrack List
     * @param taskIDsToTrack
     */
    public void removeNavigatorsOtherThanTaskIDs(List<String> taskIDsToTrack) {
        if (taskIDsToTrack != null) {

            Iterator<Map.Entry<String, TaskNavigator>> iterator = taskNavigatorList.entrySet().iterator();

            while (iterator.hasNext()) {
                // Check if this taskID is to be tracked
                Map.Entry<String, TaskNavigator> entry = iterator.next();

                if (!taskIDsToTrack.contains(entry.getKey()) && taskNavigatorList.get(entry.getKey()) != null) {
                    this.stopPollingForTaskID(entry.getKey());
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Method to clear all taskNavigators from taskNavigatorList
     */
    public void clearAllNavigators() {
        if (taskNavigatorList != null) {
            for (TaskNavigator taskNavigator : taskNavigatorList.values()) {
                taskNavigator.stopPollers();
            }
            taskNavigatorList.clear();
        }
    }
}
