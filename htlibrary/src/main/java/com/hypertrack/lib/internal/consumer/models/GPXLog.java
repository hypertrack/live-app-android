package com.hypertrack.lib.internal.consumer.models;

import java.util.Date;

/**
 * Created by piyush on 08/11/16.
 */
public class GPXLog {

    private double lat = 0.0;
    private double lng = 0.0;
    private Date date = new Date();

    public GPXLog(double lat, double lng, Date date) {
        this.lat = lat;
        this.lng = lng;
        this.date = date;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GPXLog gpxLog = (GPXLog) o;

        if (Double.compare(gpxLog.lat, lat) != 0) return false;
        if (Double.compare(gpxLog.lng, lng) != 0) return false;
        return date != null ? date.equals(gpxLog.date) : gpxLog.date == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }
}
