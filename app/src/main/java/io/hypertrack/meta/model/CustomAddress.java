package io.hypertrack.meta.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by suhas on 09/12/15.
 */
public class CustomAddress {

    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private MetaLocation location;

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
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", location=" + location +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
