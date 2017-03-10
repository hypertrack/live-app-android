package com.hypertrack.lib.internal.consumer.models;

import com.google.gson.annotations.SerializedName;

import com.hypertrack.lib.internal.common.models.GeoJSONLocation;

/**
 * Created by piyush on 25/07/16.
 */
public class UpdateDestinationRequest {

    @SerializedName("location")
    private GeoJSONLocation location;

    private UpdateDestinationRequest(){
    }

    public UpdateDestinationRequest(GeoJSONLocation location){
        this.location = location;
    }
}
