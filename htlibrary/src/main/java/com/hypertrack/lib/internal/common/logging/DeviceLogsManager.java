package com.hypertrack.lib.internal.common.logging;

import android.content.Context;

import com.android.volley.VolleyError;
import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.internal.common.HTConstants;
import com.hypertrack.lib.internal.common.network.HTNetworkResponse;
import com.hypertrack.lib.internal.common.network.HyperTrackNetworkRequest;
import com.hypertrack.lib.internal.common.network.HyperTrackPostRequest;
import com.hypertrack.lib.internal.common.network.NetworkManager;
import com.hypertrack.lib.internal.common.util.Utils;

import org.json.JSONArray;

import java.util.List;

/**
 * Created by piyush on 01/02/17.
 */
public class DeviceLogsManager {
    private final String TAG = DeviceLogsManager.class.getSimpleName();

    private Context mContext;
    private DeviceLogDataSource logsDataSource;
    private NetworkManager networkManager;
    private static DeviceLogsManager deviceLogsManager;

    private DeviceLogsManager(Context mContext, DeviceLogDataSource logsDataSource,
                              NetworkManager networkManager) {
        this.mContext = mContext;
        this.networkManager = networkManager;
        this.logsDataSource = logsDataSource;
    }

    public static DeviceLogsManager getInstance(Context context, DeviceLogDataSource logsDataSource,
                                                NetworkManager networkManager) {
        if (deviceLogsManager == null) {
            synchronized (DeviceLogsManager.class) {
                if (deviceLogsManager == null) {
                    deviceLogsManager = new DeviceLogsManager(context, logsDataSource, networkManager);
                }
            }
        }

        return deviceLogsManager;
    }

    public boolean hasPendingDeviceLogs() {
        final DeviceLogList mDeviceLogList = new DeviceLogList(logsDataSource);

        long deviceLogsCount = mDeviceLogList.count();
        return deviceLogsCount > 0L;
    }

    public void postDeviceLogs(final HyperTrackNetworkRequest.HTNetworkClient networkClient) {
        try {
            final DeviceLogList mDeviceLogList = new DeviceLogList(logsDataSource);

            final List<DeviceLog> deviceLogList = mDeviceLogList.getDeviceLogs();
            if (deviceLogList == null || deviceLogList.isEmpty()) {
                return;
            }

            final DeviceLogRequestList requestList = new DeviceLogRequestList(deviceLogList);
            final List<DeviceLogRequest> requests = requestList.getRequests();

            if (requests == null || requests.isEmpty()) {
                return;
            }

            for (final DeviceLogRequest request : requests) {
                final List<DeviceLog> requestDeviceLog = request.getDeviceLog();
                JSONArray jsonArray = this.getJSONArray(requestDeviceLog);
                if (jsonArray == null) {
                    HTLog.e(TAG, "JsonArray for one of the requestDeviceLog was null");
                    return;
                }

                this.postDeviceLogs(networkClient, jsonArray, new PostDeviceLogCallback() {
                    @Override
                    public void onPostDeviceLogSuccess() {
                        // Clear Pushed DeviceLogs from DeviceLogDB
                        mDeviceLogList.clearDeviceLogs(requestDeviceLog);
                        request.setCompleted(true);
                        requestDeviceLog.clear();
                    }

                    @Override
                    public void onError(Exception error) {
                        request.setCompleted(true);
                        HTLog.e(TAG, "OnPostingDeviceLog Failure. Count: " + requestDeviceLog.size()
                                + " Exception: " + (error != null ? error : "null"));
                    }
                });
            }
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while postDeviceLogs: " + e);
        } catch (OutOfMemoryError error) {
            HTLog.e(TAG, "OutOfMemory Error occurred while postDeviceLogs: " + error);
        }
    }

    /**
     * package
     */
    abstract class PostDeviceLogCallback {
        public abstract void onPostDeviceLogSuccess();
        public abstract void onError(Exception exception);
    }

    private JSONArray getJSONArray(List<DeviceLog> deviceLogList) {
        JSONArray jsonArray = null;
        if (deviceLogList != null) {
            jsonArray = new JSONArray();

            for (DeviceLog deviceLog : deviceLogList) {
                jsonArray.put(deviceLog.getDeviceLog());
            }
        }

        return jsonArray;
    }

    private void postDeviceLogs(final HyperTrackNetworkRequest.HTNetworkClient networkClient,
                                final JSONArray jsonArray, final PostDeviceLogCallback callback) {

        String url = BuildConfig.CORE_API_BASE_URL + HTConstants.DEVICE_LOGS_URL;

        // Get Topic for postDeviceLogs executeMqttPOST call
        if (networkClient == HyperTrackNetworkRequest.HTNetworkClient.HT_NETWORK_CLIENT_MQTT) {
            url = BuildConfig.MQTT_BASE_TOPIC + HTConstants.DEVICE_LOGS_BASE_TOPIC + Utils.getDeviceId(mContext);
        }

        HyperTrackPostRequest<JSONArray> postNetworkRequest = new HyperTrackPostRequest<>(TAG, mContext,
                url, networkClient, jsonArray, JSONArray.class,
                new HTNetworkResponse.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (callback != null)
                            callback.onPostDeviceLogSuccess();
                    }
                },
                new HTNetworkResponse.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error, Exception exception) {
                        if (callback != null)
                            callback.onError(exception);
                    }
                });

        networkManager.execute(mContext, postNetworkRequest);
    }

    public void cancelPendingRequests() {
        networkManager.cancel(TAG);
    }
}
