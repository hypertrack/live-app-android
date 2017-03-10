package com.hypertrack.lib.internal.consumer.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by piyush on 06/07/16.
 */
public class TaskList {

    private HashMap<String, HTTask> taskList;

    public TaskList() {
        taskList = new HashMap();
    }

    /**
     * Method to return a list of all TaskIDs in taskList
     * @return
     */
    public ArrayList<String> getTaskIDList() {
        if (taskList == null)
            return null;

        return new ArrayList<>(taskList.keySet());
    }

    /**
     * Method to get list of all tasks in taskList
     * @return
     */
    public ArrayList<HTTask> getTaskList() {
        if (taskList == null)
            return null;

        return new ArrayList<>(taskList.values());
    }

    /**
     * Method to return a list of TaskIDs of currently active tasks in the taskList
     * @return
     */
    public ArrayList<String> getActiveTaskIDList() {

        ArrayList<String> activeTaskIDList = new ArrayList<>();

        for (String taskID : taskList.keySet()) {
            //Check if current task is Not Completed
            if (!taskList.get(taskID).isCompleted()) {
                activeTaskIDList.add(taskID);
            }
        }

        return activeTaskIDList;
    }

    /**
     * Method to get Task from TaskList by TaskID
     * @param taskID
     * @return
     */
    public HTTask getTask(String taskID) {
        if (taskList != null) {
            return taskList.get(taskID);
        }

        return null;
    }

    /**
     * Method to add a task in TaskList
     * @param task
     */
    public void addTask (HTTask task) {
        if (taskList == null) {
            taskList = new HashMap<>();
        }

        taskList.put(task.getId(), task);
    }

    /**
     * Method to add a List of tasks in taskList
     * @param taskListToBeAdded
     */
    public void addTaskList(List<HTTask> taskListToBeAdded) {
        if (taskListToBeAdded != null) {
            for (HTTask htTask : taskListToBeAdded) {
                taskList.put(htTask.getId(), htTask);
            }
        }
    }

    /**
     * Method to remove a task from taskList
     * @param taskID
     * @return
     */
    public HTTask removeTask (String taskID) {
        return taskList.remove(taskID);
    }

    /**
     * Method to clear all tasks from taskList
     */
    public void clearAllTasks() {
        taskList.clear();
    }

    /**
     * Method to return if any Task is not completed
     * @return
     */
    public boolean areAllTasksCompleted() {

        for (HTTask task : taskList.values()) {
            if (task != null && !task.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method to return if any of the Tasks has started
     * @return
     */
    public boolean anyTaskStarted() {
        for (HTTask task : taskList.values()) {
            if (task != null && task.notStarted()){
                return true;
            }
        }

        return false;
    }

    public boolean isTaskCompleted(String taskID) {
        return (taskList != null && !taskList.isEmpty() && taskList.get(taskID) != null && taskList.get(taskID).isCompleted());
    }

    public boolean isTaskFinished(String taskID) {
        return (taskList == null || taskList.isEmpty() || taskList.get(taskID) == null || (taskList.get(taskID).hasTaskFinished()));
    }
}
