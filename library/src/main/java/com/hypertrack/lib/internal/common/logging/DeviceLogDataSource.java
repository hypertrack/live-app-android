package com.hypertrack.lib.internal.common.logging;

import java.util.List;

/**
 * Created by piyush on 22/08/16.
 */

/** package */ interface DeviceLogDataSource {
    long getDeviceLogCount();
    void addDeviceLog(String deviceLog);
    void deleteDeviceLog(List<DeviceLog> deviceLogList);
    void deleteAllDeviceLogs();
    List<DeviceLog> getDeviceLogs();
}