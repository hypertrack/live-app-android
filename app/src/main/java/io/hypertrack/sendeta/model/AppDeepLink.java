package io.hypertrack.sendeta.model;

/**
 * Created by piyush on 26/07/16.
 */
public class AppDeepLink {
    //screen identifier
    public int mId;

    public int id;
    public String uuid;

    // Used for Push Destination Location feature
    public Double lat;
    public Double lng;

    // Used for RequestETA
    public String address;

    // Used for Track feature
    public String taskID;
    public String shortCode;
    public String lookupId;

    public AppDeepLink(int mId) {
        this.mId = mId;
    }
}
