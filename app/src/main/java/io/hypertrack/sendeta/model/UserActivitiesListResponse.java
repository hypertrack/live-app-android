package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by piyush on 02/09/16.
 */
public class UserActivitiesListResponse {

    @SerializedName("results")
    private ArrayList<UserActivityDetails> userActivities;

    @SerializedName("next")
    private String next;

    public ArrayList<UserActivityDetails> getUserActivities() {
        return userActivities;
    }

    public void setUserActivities(ArrayList<UserActivityDetails> userActivities) {
        this.userActivities = userActivities;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
