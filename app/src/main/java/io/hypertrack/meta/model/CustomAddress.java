package io.hypertrack.meta.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by suhas on 09/12/15.
 */
public class CustomAddress implements Serializable {

    private Integer id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private MetaLocation location;

    @SerializedName("hypertrack_place_id")
    private String hypertrackPlaceId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHypertrackPlaceId() {
        return hypertrackPlaceId;
    }

    public void setHypertrackPlaceId(String hypertrackPlaceId) {
        this.hypertrackPlaceId = hypertrackPlaceId;
    }

    @SerializedName("google_places_id")
    private String googlePlacesId;

    public String getGooglePlacesId() {
        return googlePlacesId;
    }

    public void setGooglePlacesId(String googlePlacesId) {
        this.googlePlacesId = googlePlacesId;
    }

    @SerializedName("postal_code")
    private String postalCode;

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public MetaLocation getLocation() {
        return location;
    }

    public void setLocation(MetaLocation location) {
        this.location = location;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String toString() {
        return "CustomAddress{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", location=" + location +
                ", hypertrackPlaceId='" + hypertrackPlaceId + '\'' +
                ", googlePlacesId='" + googlePlacesId + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
