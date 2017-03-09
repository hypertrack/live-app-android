package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;
import com.hypertrack.lib.internal.common.models.GeoJSONLocation;

/**
 * Created by ulhas on 12/03/16.
 */
public class HTPlace {

    @SerializedName("id")
    private String id;

    @SerializedName("location")
    private GeoJSONLocation location;

    @SerializedName("address")
    private String address;

    @SerializedName("name")
    private String name;


    @SerializedName("landmark")
    private String landmark;

    @SerializedName("zip_code")
    private String zipCode;

    @SerializedName("city")
    private String city;

    @SerializedName("state")
    private String state;

    @SerializedName("country")
    private String country;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeoJSONLocation getLocation() {
        return location;
    }

    public void setLocation(GeoJSONLocation location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "HTPlace{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", address='" + address + '\'' +
                ", landmark='" + landmark + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                '}';
    }

    public String getDisplayString() {
        if (this.address != null && !this.address.isEmpty()) {
            return this.address;
        }

        if (this.landmark != null && !this.landmark.isEmpty()) {
            return this.landmark;
        }

        if (this.location != null && this.location.getDisplayString() != null) {
            return this.location.getDisplayString();
        }

        return null;
    }
}