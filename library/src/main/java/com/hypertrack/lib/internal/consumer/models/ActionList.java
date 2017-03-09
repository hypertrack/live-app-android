package com.hypertrack.lib.internal.consumer.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Aman on 4/03/2017.
 */
public class ActionList {

    private HashMap<String, HTAction> actionList;

    public ActionList() {
        actionList = new HashMap();
    }

    /**
     * Method to return a list of all ActionIDs in actionList
     *
     * @return
     */
    public ArrayList<String> getActionIDList() {
        if (actionList == null)
            return null;

        return new ArrayList<>(actionList.keySet());
    }

    /**
     * Method to get list of all actions in actionList
     *
     * @return
     */
    public ArrayList<HTAction> getActionList() {
        if (actionList == null)
            return null;

        return new ArrayList<>(actionList.values());
    }

    /**
     * Method to return a list of ActionIDs of currently active actions in the actionList
     *
     * @return
     */
    public ArrayList<String> getActiveActionIDList() {

        ArrayList<String> activeActionIDList = new ArrayList<>();

        for (String actionID : actionList.keySet()) {
            //Check if current action is Not Completed
            if (!actionList.get(actionID).isCompleted()) {
                activeActionIDList.add(actionID);
            }
        }

        return activeActionIDList;
    }

    /**
     * Method to get Action from ActionList by ActionID
     *
     * @param actionID
     * @return
     */
    public HTAction getAction(String actionID) {
        if (actionList != null) {
            return actionList.get(actionID);
        }

        return null;
    }

    /**
     * Method to add a action in ActionList
     *
     * @param action
     */
    public void addAction(HTAction action) {
        if (actionList == null) {
            actionList = new HashMap<>();
        }

        actionList.put(action.getId(), action);
    }

    /**
     * Method to add a List of actions in actionList
     *
     * @param actionListToBeAdded
     */
    public void addActionList(List<HTAction> actionListToBeAdded) {
        if (actionListToBeAdded != null) {
            for (HTAction htAction : actionListToBeAdded) {
                actionList.put(htAction.getId(), htAction);
            }
        }
    }

    /**
     * Method to remove a action from actionList
     *
     * @param actionID
     * @return
     */
    public HTAction removeAction(String actionID) {
        return actionList.remove(actionID);
    }

    /**
     * Method to clear all actions from actionList
     */
    public void clearAllActions() {
        actionList.clear();
    }

    /**
     * Method to return if any Action is not completed
     *
     * @return
     */
    public boolean areAllActionsCompleted() {

        for (HTAction action : actionList.values()) {
            if (action != null && !action.isCompleted()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method to return if any of the Actions has started
     *
     * @return
     */
    public boolean anyActionStarted() {
        for (HTAction action : actionList.values()) {
            if (action != null && action.notStarted()) {
                return true;
            }
        }

        return false;
    }

    public boolean isActionCompleted(String actionID) {
        return (actionList != null && !actionList.isEmpty() && actionList.get(actionID) != null && actionList.get(actionID).isCompleted());
    }

    public boolean isActionFinished(String actionID) {
        return (actionList == null || actionList.isEmpty() || actionList.get(actionID) == null || (actionList.get(actionID).hasActionFinished()));
    }
}
