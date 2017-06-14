package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.models.User;

import java.util.Date;
import java.util.List;


/**
 * Created by Aman Jain on 24/05/17.
 */

public class UserTimelineData extends User {

    @SerializedName("segments")
    private List<Segment> segmentList;

    @SerializedName("timeline_date")
    private Date timelineDate;

    public List<Segment> getSegmentList() {
        return segmentList;
    }

    public void setSegmentList(List<Segment> segmentList) {
        this.segmentList = segmentList;
    }

    public Date getTimelineDate() {
        return timelineDate;
    }

    public void setTimelineDate(Date timelineDate) {
        this.timelineDate = timelineDate;
    }

    @Override
    public String toString() {
        return "UserTimelineData{" +
                ", segmentList=" + segmentList +
                ", timelineDate=" + timelineDate +
                '}';
    }
}
