package com.hypertrack.lib.internal.common.logging;

import com.hypertrack.lib.internal.common.util.TextUtils;

import java.util.List;

/**
 * Created by piyush on 22/08/16.
 */
/** package */ class DeviceLogList {
    private DeviceLogDataSource mDeviceLogDataSource;

    public DeviceLogList(DeviceLogDataSource mDeviceLogDataSource) {
        this.mDeviceLogDataSource = mDeviceLogDataSource;
    }

    public void addDeviceLog(String deviceLog) {
        if (TextUtils.isEmpty(deviceLog)) {
            return;
        }

        this.mDeviceLogDataSource.addDeviceLog(deviceLog);
    }

    public void clearSavedDeviceLogs() {
        this.mDeviceLogDataSource.deleteAllDeviceLogs();
    }

    public List<DeviceLog> getDeviceLogs() {
        return this.mDeviceLogDataSource.getDeviceLogs();
    }

    public void clearDeviceLogs(List<DeviceLog> pushedDeviceLogs) {
        if (pushedDeviceLogs == null || pushedDeviceLogs.isEmpty())
            return;

        this.mDeviceLogDataSource.deleteDeviceLog(pushedDeviceLogs);
    }

    public long count() {
        return this.mDeviceLogDataSource.getDeviceLogCount();
    }
}