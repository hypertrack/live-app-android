package io.hypertrack.sendeta.model;

import com.hypertrack.lib.models.GeoJSONLocation;

/**
 * Created by piyush on 09/11/16.
 */
public class RequestTrackingDTO {

    private GeoJSONLocation location = null;
    private String address = null;
    private String name = null;


    public RequestTrackingDTO(String address) {
        this.address = address;
    }

    public RequestTrackingDTO(GeoJSONLocation location, String address, String name) {
        this.location = location;
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeoJSONLocation getLocation() {
        return location;
    }

    public void setLocation(GeoJSONLocation location) {
        this.location = location;
    }
}
