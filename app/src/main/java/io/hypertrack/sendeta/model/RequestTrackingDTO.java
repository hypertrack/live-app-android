package io.hypertrack.sendeta.model;


import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;

/**
 * Created by piyush on 09/11/16.
 */
public class RequestTrackingDTO {

    private HyperTrackLocation location = null;
    private String address = null;
    private String name = null;

    public HyperTrackLocation getLocation() {
        return location;
    }

    public void setLocation(HyperTrackLocation location) {
        this.location = location;
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

    public RequestTrackingDTO(String address) {
        this.address = address;
    }

    public RequestTrackingDTO(HyperTrackLocation location, String address, String name) {
        this.location = location;
        this.address = address;
        this.name = name;
    }
}
