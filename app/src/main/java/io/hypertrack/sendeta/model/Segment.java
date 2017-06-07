
package io.hypertrack.sendeta.model;

import android.location.Location;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.models.HyperTrackLocation;
import com.hypertrack.lib.models.Place;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.hypertrack.lib.internal.common.util.DateTimeUtility.HT_DATETIME_FORMAT;


public class Segment implements Serializable
{
    public static String SEGMENT_TYPE_STOP = "stop";
    public static String SEGMENT_TYPE_TRIP = "trip";
    public static String SEGMENT_TYPE_LOCATION_VOID = "location_void";
    public static String SEGMENT_TYPE_NO_INFORMATION = "no_information";

    @SerializedName("id")
    public String id;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("activity_type")
    public String activityType;

    @SerializedName("place")
    public Place place;

    @SerializedName("started_at")
    public Date startedAt;

    @SerializedName("start_location")
    public HyperTrackLocation startLocation;

    @SerializedName("ended_at")
    public Date endedAt;

    @SerializedName("end_location")
    public HyperTrackLocation endLocation;

    @SerializedName("distance")
    public Integer distance;

    @SerializedName("duration")
    public Double duration;

    @SerializedName("location")
    public Location location;

    @SerializedName("lookup_id")
    public String lookupId;

    @SerializedName("encoded_polyline")
    public String encodedPolyline;

    @SerializedName("time_aware_polyline")
    public String timeAwarePolyline;

    @SerializedName("created_at")
    public Date createdAt;

    @SerializedName("modified_at")
    public Date modifiedAt;

    @SerializedName("type")
    public String type;

    public Segment(String id, String userId, String activityType, Place place, Date startedAt,
                   HyperTrackLocation startLocation, Date endedAt, HyperTrackLocation endLocation,
                   Integer distance, Double duration, Location location, String lookupId,
                   String encodedPolyline, String timeAwarePolyline, Date createdAt,
                   Date modifiedAt, String type) {

        this.id = id;
        this.userId = userId;
        this.activityType = activityType;
        this.place = place;
        this.startedAt = startedAt;
        this.startLocation = startLocation;
        this.endedAt = endedAt;
        this.endLocation = endLocation;
        this.distance = distance;
        this.duration = duration;
        this.location = location;
        this.lookupId = lookupId;
        this.encodedPolyline = encodedPolyline;
        this.timeAwarePolyline = timeAwarePolyline;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.type = type;
    }

    public Segment() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public HyperTrackLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(HyperTrackLocation startLocation) {
        this.startLocation = startLocation;
    }

    public HyperTrackLocation getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(HyperTrackLocation endLocation) {
        this.endLocation = endLocation;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getLookupId() {
        return lookupId;
    }

    public void setLookupId(String lookupId) {
        this.lookupId = lookupId;
    }

    public String getEncodedPolyline() {
        return encodedPolyline;
    }

    public void setEncodedPolyline(String encodedPolyline) {
        this.encodedPolyline = encodedPolyline;
    }

    public String getTimeAwarePolyline() {
        return timeAwarePolyline;
    }

    public void setTimeAwarePolyline(String timeAwarePolyline) {
        this.timeAwarePolyline = timeAwarePolyline;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Date endedAt) {
        this.endedAt = endedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isStop(){
        if(TextUtils.isEmpty(type) || !type.equalsIgnoreCase(SEGMENT_TYPE_STOP))
            return false;

        return true;
    }

    public boolean isTrip(){
        if(TextUtils.isEmpty(type) || !type.equalsIgnoreCase(SEGMENT_TYPE_TRIP))
            return false;

        return true;
    }

    public boolean isLocationVoid(){
        if(TextUtils.isEmpty(type) || !type.equalsIgnoreCase(SEGMENT_TYPE_LOCATION_VOID))
            return false;

        return true;
    }

    public boolean isNoInformation(){
        if(TextUtils.isEmpty(type) || !type.equalsIgnoreCase(SEGMENT_TYPE_NO_INFORMATION))
            return false;

        return true;
    }

    public String getDistanceInKMS() {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return  decimalFormat.format((distance) / 1000.0f) + " km" ;
    }

    public String getFormatedDuration(){

        String durationText = null;
        int minute = (int) ((Math.round(duration/60)) % 60)  ;
        int hour = (int) Math.round(duration / 3600);

        if (minute == 0) {
            durationText = "1 min";
        } else{
            durationText = (hour > 0 ? hour+" hr " : "") +(minute > 0 ? minute+" min" : "");
        }

        return durationText;
    }

    public String getDistanceAndDuration(){
        String value = getFormatedDuration() + "  |  "+ getDistanceInKMS();
        return value;
    }

    public String formatDate(Date date){

        if(date == null)
            return "";

        DateFormat format = new SimpleDateFormat(HT_DATETIME_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateFormatar = new SimpleDateFormat("MMM d, yyyy");

        return dateFormatar.format(date);
    }

    public String formatTime(Date date){

        if(date == null)
            return "";

        DateFormat format = new SimpleDateFormat(HT_DATETIME_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat timeFormatar = new SimpleDateFormat("h:mm a");

        return timeFormatar.format(date);
    }

    @Override
    public String toString() {
        return "Segment{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", activityType='" + activityType + '\'' +
                ", place=" + place +
                ", startedAt='" + startedAt + '\'' +
                ", startLocation=" + startLocation +
                ", endedAt='" + endedAt + '\'' +
                ", endLocation=" + endLocation +
                ", distance=" + distance +
                ", duration=" + duration +
                ", location=" + location +
                ", lookupId='" + lookupId + '\'' +
                ", encodedPolyline='" + encodedPolyline + '\'' +
                ", timeAwarePolyline='" + timeAwarePolyline + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", modifiedAt='" + modifiedAt + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
