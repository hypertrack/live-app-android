package io.hypertrack.sendeta.model;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by piyush on 10/06/16.
 */
public class MetaPlace extends RealmObject implements Serializable{

    public final static String HOME = "Home";
    public final static String WORK = "Work";

    @PrimaryKey
    private int id;

    @SerializedName("google_places_id")
    private String googlePlacesID;

    private String name;

    private String address;

    private Double latitude = 0.0;
    private Double longitude = 0.0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGooglePlacesID() {
        return googlePlacesID;
    }

    public void setGooglePlacesID(String googlePlacesID) {
        this.googlePlacesID = googlePlacesID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "MetaPlace{" +
                "id=" + id +
                ", googlePlacesID='" + googlePlacesID + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public MetaPlace() {

    }

    public MetaPlace(Double latitude, Double longitude) {
        if (latitude == null || longitude == null)
            return;

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MetaPlace(LatLng latLng) {
        if (latLng == null)
            return;

        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public MetaPlace(String name, LatLng latLng) {
        this.name = name;

        if (latLng == null) {
            return;
        }

        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public MetaPlace(String name, Double latitude, Double longitude) {
        this.name = name;

        if (latitude == null || longitude == null)
            return;

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MetaPlace(MetaPlace place) {
        this.name = place.getName();
        this.googlePlacesID = place.getGooglePlacesID();
        this.address = place.getAddress();
        this.id = place.getId();
        this.longitude = place.getLongitude();
        this.latitude = place.getLatitude();
    }

    public void update(MetaPlace place) {
        this.name = place.getName();
        this.googlePlacesID = place.getGooglePlacesID();
        this.address = place.getAddress();
        this.id = place.getId();
        this.longitude = place.getLongitude();
        this.latitude = place.getLatitude();
    }

    public MetaPlace(Place place) {
        this.name = place.getName().toString();
        this.googlePlacesID = place.getId();
        this.address = place.getAddress().toString();

        LatLng latLng = place.getLatLng();
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public boolean isHome() {
        return this.name.equalsIgnoreCase(HOME);
    }

    public boolean isWork() {
        return this.name.equalsIgnoreCase(WORK);
    }

    public boolean hasDestination() {
        return this.id != 0;
    }

    public boolean isEqualPlace(MetaPlace place) {
        if (this.id == 0 && place.id == 0) {
            return false;
        }

        return (this.id == place.getId());
    }

    public MetaPlace(String name) {
        this.name = name;
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public void setLatLng(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }
}
