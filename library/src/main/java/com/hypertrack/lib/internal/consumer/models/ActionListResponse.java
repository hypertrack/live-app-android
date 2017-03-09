package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;


/**
 * Created by Aman on 04/03/17.
 */

/**
 * package
 */
public class ActionListResponse {
    @SerializedName("results")
    private List<HTAction> actionList;

    public List<HTAction> getActionList() {
        return actionList;
    }

    @Override
    public String toString() {
        return "ActionListResponse{" +
                "actionList=" + Arrays.toString(actionList.toArray());
    }
}
