package io.hypertrack.meta.model;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

/**
 * Created by piyush on 10/06/16.
 */
public class MetaPlace extends RealmObject {

    public final static String HOME = "Home";
    public final static String WORK = "Work";

    private int id;

    @SerializedName("hypertrack_destination_id")
    private String hyperTrackDestinationID;

    @SerializedName("google_places_id")
    private String googlePlacesID;

    private String name;

    private String address;

    private Double latitude;
    private Double longitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHyperTrackDestinationID() {
        return hyperTrackDestinationID;
    }

    public void setHyperTrackDestinationID(String hyperTrackDestinationID) {
        this.hyperTrackDestinationID = hyperTrackDestinationID;
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
                ", hyperTrackDestinationID='" + hyperTrackDestinationID + '\'' +
                ", googlePlacesID='" + googlePlacesID + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public MetaPlace() {

    }

    public MetaPlace(MetaPlace place) {
        this.name = place.getName();
        this.googlePlacesID = place.getGooglePlacesID();
        this.address = place.getAddress();
        this.id = place.getId();
        this.longitude = place.getLongitude();
        this.latitude = place.getLatitude();
        this.hyperTrackDestinationID = place.getHyperTrackDestinationID();
    }

    public void update(MetaPlace place) {
        this.name = place.getName();
        this.googlePlacesID = place.getGooglePlacesID();
        this.address = place.getAddress();
        this.id = place.getId();
        this.longitude = place.getLongitude();
        this.latitude = place.getLatitude();
        this.hyperTrackDestinationID = place.getHyperTrackDestinationID();
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
        return (this.hyperTrackDestinationID != null && !this.hyperTrackDestinationID.isEmpty());
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
}
