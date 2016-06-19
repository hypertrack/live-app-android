package io.hypertrack.meta.model;

import com.google.gson.annotations.SerializedName;

import io.hypertrack.lib.common.model.HTLocation;

/**
 * Created by ulhas on 19/06/16.
 */
public class PlaceDTO {

    private int id;

    @SerializedName("hypertrack_destination_id")
    private String hyperTrackDestinationID;

    @SerializedName("google_places_id")
    private String googlePlacesID;

    private String name;

    private String address;

    private HTLocation location;

    public PlaceDTO(MetaPlace place) {
        this.id = place.getId();
        this.address = place.getAddress();
        this.googlePlacesID = place.getGooglePlacesID();
        this.hyperTrackDestinationID = place.getHyperTrackDestinationID();
        this.name = place.getName();
        this.location = new HTLocation(place.getLatitude(), place.getLongitude());
    }
}
