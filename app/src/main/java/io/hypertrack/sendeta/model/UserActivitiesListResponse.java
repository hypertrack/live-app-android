package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by piyush on 02/09/16.
 */
public class UserActivitiesListResponse {

    @SerializedName("results")
    private ArrayList<UserActivity> userActivities;

    @SerializedName("next")
    private String next;

    public ArrayList<UserActivity> getUserActivities() {
        return userActivities;
    }

    public void setUserActivities(ArrayList<UserActivity> userActivities) {
        this.userActivities = userActivities;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
