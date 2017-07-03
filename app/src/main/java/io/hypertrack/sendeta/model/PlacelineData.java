package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.models.User;

import java.util.Date;
import java.util.List;


/**
 * Created by Aman Jain on 24/05/17.
 */

public class PlacelineData extends User {

    @SerializedName("segments")
    private List<Segment> segmentList;

    public List<Segment> getSegmentList() {
        return segmentList;
    }

    public void setSegmentList(List<Segment> segmentList) {
        this.segmentList = segmentList;
    }

    @Override
    public String toString() {
        return "PlacelineData{" +
                "segmentList=" + segmentList +
                '}';
    }
}
