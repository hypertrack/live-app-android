package com.hypertrack.lib.internal.common.logging;

import com.google.gson.annotations.Expose;

/**
 * Created by piyush on 22/08/16.
 */
/** package */ class DeviceLog {

    @Expose(serialize = false, deserialize = false)
    private int id;

    private String deviceLog;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceLog() {
        return deviceLog;
    }

    public void setDeviceLog(String deviceLog) {
        this.deviceLog = deviceLog;
    }

    public DeviceLog(String deviceLog) {
        this.deviceLog = deviceLog;
    }

    public DeviceLog(int id, String deviceLog) {
        this.id = id;
        this.deviceLog = deviceLog;
    }
}