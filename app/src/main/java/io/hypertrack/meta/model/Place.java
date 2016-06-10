package io.hypertrack.meta.model;

/**
 * Created by piyush on 10/06/16.
 */
public class Place {

    private int id;
    private String hyperTrackId;
    private String googlePlaceId;
    private String name;
    private String address;
    private Double lat;
    private Double lng;

    public Place(int id, String name, String address, Double lat, Double lng) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
