package com.hypertrack.lib.internal.consumer.models;

import java.util.ArrayList;

/**
 * Created by Aman on 4/03/2017.
 */
public interface ActionFetchCallback {
    void onFetchAction(boolean result, ArrayList<String> actionID);
}
