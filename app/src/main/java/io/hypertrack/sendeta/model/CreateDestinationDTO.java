package io.hypertrack.sendeta.model;

import com.hypertrack.lib.internal.transmitter.models.HyperTrackLocation;


/**
 * Created by piyush on 22/10/16.
 */
public class CreateDestinationDTO {

    private HyperTrackLocation location;
    private String address;

    public CreateDestinationDTO(HyperTrackLocation location) {
        this.location = location;
    }

    public CreateDestinationDTO(String address, HyperTrackLocation location) {
        this.address = address;
        this.location = location;
    }
}
