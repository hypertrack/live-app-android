package io.hypertrack.sendeta.model;

import io.hypertrack.lib.common.model.HTLocation;

/**
 * Created by piyush on 22/10/16.
 */
public class CreateDestinationDTO {

    private HTLocation location;
    private String address;

    public CreateDestinationDTO(HTLocation location) {
        this.location = location;
    }

    public CreateDestinationDTO(String address, HTLocation location) {
        this.address = address;
        this.location = location;
    }
}
