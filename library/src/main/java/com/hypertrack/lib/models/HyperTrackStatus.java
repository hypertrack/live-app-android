package com.hypertrack.lib.models;

/**
 * Created by piyush on 18/02/17.
 */
public class HyperTrackStatus {

    public static final short SUCCESS = 0x01;
    public static final short OFFLINE_SUCCESS = 0x02;

    public static final short ERROR_PUBLISHABLE_KEY = 0x10;
    public static final short ERROR_USER_ID = 0x11;
    public static final short ERROR_PERMISSIONS = 0x12;

    public static final short ERROR_LOCATION = 0x20;
    public static final short ERROR_NETWORK = 0x21;
    public static final short ERROR_UNAUTHORIZED = 0x22;

    public static final short ERROR_SERVER = 0x30;
    public static final short ERROR_UNKNOWN = 0x31;

    public static final short UNKNOWN = 0x40;
}
