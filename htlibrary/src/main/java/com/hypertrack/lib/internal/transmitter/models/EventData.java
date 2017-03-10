package com.hypertrack.lib.internal.transmitter.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by piyush on 27/02/17.
 */

public class EventData {

    public static final String STOP_ID_KEY = "stop_id";

    public static class Stop {
        @SerializedName("stop_id")
        private String stopId;

        public Stop(String stopId) {
            this.stopId = stopId;
        }
    }

    public static class Action {
        @SerializedName("action_id")
        private String actionId;

        public Action(String actionId) {
            this.actionId = actionId;
        }
    }
}
