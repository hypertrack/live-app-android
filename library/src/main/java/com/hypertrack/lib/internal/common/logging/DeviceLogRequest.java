package com.hypertrack.lib.internal.common.logging;

import java.util.List;

/**
 * Created by piyush on 22/08/16.
 */
/** package */ class DeviceLogRequest {
    private List<DeviceLog> deviceLog;
    private boolean completed;

    public List<DeviceLog> getDeviceLog() {
        return deviceLog;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public DeviceLogRequest(List<DeviceLog> deviceLog) {
        this.deviceLog = deviceLog;
    }
}