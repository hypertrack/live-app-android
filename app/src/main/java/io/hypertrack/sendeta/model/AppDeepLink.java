package io.hypertrack.sendeta.model;

/**
 * Created by piyush on 26/07/16.
 */
public class AppDeepLink {


    //screen identifier
    public int mId;

    public int id;

    // Used for Push Destination Location feature
    public Double lat;
    public Double lng;

    public AppDeepLink(int mId) {
        this.mId = mId;
    }
}
