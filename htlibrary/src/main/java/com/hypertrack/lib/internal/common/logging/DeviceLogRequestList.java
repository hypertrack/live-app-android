package com.hypertrack.lib.internal.common.logging;

import com.hypertrack.lib.internal.common.util.ListUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piyush on 22/08/16.
 */
/** package */ class DeviceLogRequestList {
    private static final int DEVICE_LOG_BATCH_SIZE = 50;

    private List<DeviceLogRequest> requests;

    public List<DeviceLogRequest> getRequests() {
        return requests;
    }

    public DeviceLogRequestList(List<DeviceLog> deviceLog) {
        if (deviceLog == null || deviceLog.isEmpty()) {
            return;
        }

        List<List<DeviceLog>> deviceLogBatches = ListUtility.partition(deviceLog, DEVICE_LOG_BATCH_SIZE);

        List<DeviceLogRequest> requests = new ArrayList<>();
        for (List<DeviceLog> deviceLogBatch : deviceLogBatches) {
            requests.add(new DeviceLogRequest(deviceLogBatch));
        }

        this.requests = requests;
    }
}