package com.hypertrack.lib.models;

/**
 * Created by piyush on 25/02/17.
 */
public class SuccessResponse {

    private Object responseObject;

    public Object getResponseObject() {
        return responseObject;
    }

    public SuccessResponse(Object responseObject) {
        this.responseObject = responseObject;
    }
}
