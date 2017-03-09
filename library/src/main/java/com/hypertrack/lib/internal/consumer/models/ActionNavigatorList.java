package com.hypertrack.lib.internal.consumer.models;

import android.os.Handler;

import com.hypertrack.lib.internal.common.util.TextUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by piyush on 06/07/16.
 */
public class ActionNavigatorList {

    private HashMap<String, ActionNavigator> actionNavigatorList;
    private ActionNavigatorCallback callback;
    private HashMap<String, Handler> actionNavigatorHandlerList;
    private HashMap<String, Handler> actionAnimationHandlerList;

    public ActionNavigatorList(ActionNavigatorCallback callback) {
        actionNavigatorList = new HashMap<>();
        actionNavigatorHandlerList = new HashMap<>();
        actionAnimationHandlerList = new HashMap<>();
        this.callback = callback;
    }

    /**
     * Method to get ActionNavigator from actionNavigatorList by ActionID
     *
     * @param actionID
     * @return
     */
    public ActionNavigator getActionNavigator(String actionID) {
        return actionNavigatorList.get(actionID);
    }

    public Handler getActionAnimationHandler(String actionID) {
        if (actionAnimationHandlerList.get(actionID) == null)
            actionAnimationHandlerList.put(actionID, new Handler());

        return actionAnimationHandlerList.get(actionID);
    }

    /**
     * Method to add a actionNavigator in actionNavigatorList
     *
     * @param actionID
     */
    public void addNavigatorForAction(String actionID) {
        if (actionNavigatorHandlerList.get(actionID) == null) {
            actionNavigatorHandlerList.put(actionID, new Handler());
        }

        if (actionAnimationHandlerList.get(actionID) == null) {
            actionAnimationHandlerList.put(actionID, new Handler());
        }

        ActionNavigator actionNavigator = new ActionNavigator(actionID, callback, actionNavigatorHandlerList.get(actionID));
        actionNavigatorList.put(actionNavigator.getActionID(), actionNavigator);
    }

    /**
     * Method to process Action's TimeAwarePolyline for navigation
     *
     * @param action
     */
    public void processTimeAwarePolyline(HTAction action) {
        if (actionNavigatorList.get(action.getId()) == null)
            addNavigatorForAction(action.getId());
        if (TextUtils.isEmpty(action.getTimeAwarePolyline())) {
            return;
        }

        actionNavigatorList.get(action.getId()).processTimeAwarePolyline(action);
    }

    /**
     * Method to Stop Polling for a ActionID
     *
     * @param actionID
     */
    public void stopPollingForActionID(String actionID) {
        if (actionNavigatorList.get(actionID) != null) {
            actionNavigatorList.get(actionID).stopPollers();
        }
    }

    /**
     * Method to Stop Polling for all ActionIDs
     */
    public void stopPollingForAllActions() {
        for (String actionID : actionNavigatorList.keySet()) {
            if (actionNavigatorList.get(actionID) != null) {
                actionNavigatorList.get(actionID).stopPollers();
            }
        }
    }

    /**
     * Method to remove a actionNavigator for a ActionID from actionNavigatorList
     *
     * @param actionID
     * @return
     */
    public ActionNavigator removeNavigator(String actionID) {
        if (actionNavigatorList.get(actionID) != null) {
            this.stopPollingForActionID(actionID);
            return actionNavigatorList.remove(actionID);
        }

        return null;
    }

    /**
     * Method to remove all actionNavigators corresponding to actionIDs not present in actionIDsToTrack List
     *
     * @param actionIDsToTrack
     */
    public void removeNavigatorsOtherThanActionIDs(List<String> actionIDsToTrack) {
        if (actionIDsToTrack != null) {

            Iterator<Map.Entry<String, ActionNavigator>> iterator = actionNavigatorList.entrySet().iterator();

            while (iterator.hasNext()) {
                // Check if this actionID is to be tracked
                Map.Entry<String, ActionNavigator> entry = iterator.next();

                if (!actionIDsToTrack.contains(entry.getKey()) && actionNavigatorList.get(entry.getKey()) != null) {
                    this.stopPollingForActionID(entry.getKey());
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Method to clear all actionNavigators from actionNavigatorList
     */
    public void clearAllNavigators() {
        if (actionNavigatorList != null) {
            for (ActionNavigator actionNavigator : actionNavigatorList.values()) {
                actionNavigator.stopPollers();
            }
            actionNavigatorList.clear();
        }
    }
}
