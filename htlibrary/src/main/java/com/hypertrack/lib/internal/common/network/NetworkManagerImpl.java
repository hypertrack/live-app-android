package com.hypertrack.lib.internal.common.network;

import android.content.Context;

import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.TextUtils;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * Created by piyush on 01/02/17.
 */
public class NetworkManagerImpl implements NetworkManager{
    private static final String TAG = NetworkManagerImpl.class.getSimpleName();

    private static final String ContextRequiredMessage = "Required Parameter: 'context' is required to execute a call";
    private static final String NetworkRequestRequiredMessage = "Required Parameter: 'networkRequest' is required to execute a call";

    private static NetworkManagerImpl networkManager;

    private String userID = null;
    private MqttClient mqttClient;
    private HTTPClient HTTPClient;

    private NetworkManagerImpl(Context context, String userID, SmartScheduler jobScheduler) {
        // Get MQTT & HTTP Client instances
        initializeMQTTClient(context, userID, jobScheduler);
        initializeHTTPClient(context);
    }

    public static NetworkManagerImpl getInstance(Context context, String userID, SmartScheduler jobScheduler) {
        if (networkManager == null) {
            synchronized (NetworkManagerImpl.class) {
                if (networkManager == null)
                    networkManager = new NetworkManagerImpl(context, userID, jobScheduler);
            }
        }

        // Update userID in case not set already
        if (!TextUtils.isEmpty(userID) && networkManager.userID == null) {
            networkManager.getMqttClient().updateUserID(userID);
        }

        return networkManager;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    private MqttClient initializeMQTTClient(Context mContext, String userID, SmartScheduler jobScheduler) {
        if (mqttClient == null) {
            mqttClient = new MqttClient(mContext, jobScheduler, userID);
        }

        this.userID = userID;

        return mqttClient;
    }

    private HTTPClient initializeHTTPClient(final Context context) {
        if (HTTPClient == null) {
            HTTPClient = new HTTPClient(context, TAG);
        }

        return HTTPClient;
    }

    @Override
    public void setUserID(String userID) {
        // Update userID in case not set already
        if (!TextUtils.isEmpty(userID) && !userID.equals(networkManager.userID)) {
            networkManager.getMqttClient().updateUserID(userID);
        }
    }

    @Override
    public void execute(final Context context, final HyperTrackNetworkRequest<?> networkRequest) {
        if (networkRequest == null) {
            HTLog.e(TAG, "Error occurred while NetworkManager.execute: " + NetworkRequestRequiredMessage);
            throw new IllegalArgumentException(NetworkRequestRequiredMessage);
        }

        if (context == null) {
            if (networkRequest.getErrorListener() != null) {
                networkRequest.getErrorListener().onErrorResponse(null, new IllegalArgumentException(ContextRequiredMessage));
            }
            HTLog.e(TAG, "Error occurred while NetworkManager.execute: " + ContextRequiredMessage);
            return;
        }

        switch (networkRequest.getNetworkRequestType()) {

            case HyperTrackNetworkRequest.HTNetworkRequestType.POST: {
                executePOST((HyperTrackPostRequest) networkRequest);
            }
            break;

            case HyperTrackNetworkRequest.HTNetworkRequestType.GET: {
                executeGET((HyperTrackGetRequest) networkRequest);
            }
            break;
        }
    }

    @Override
    public void cancel(Object tag) {
        if (tag == null) {
            return;
        }

        // Cancel All HTTP calls
        HTTPClient.cancelPendingRequests(tag);
    }

    @Override
    public void disconnect() {
        // Disconnect HTTP & MQTT Client
        mqttClient.disconnectMqttClient(new MQTTClientDisconnectionCallback() {
            @Override
            public void onMQTTDisconnectionSuccess() {
                // do nothing
            }

            @Override
            public void onMQTTDisconnectionError(IMqttToken asyncActionToken, Throwable exception) {
                HTLog.e(TAG, "Error while disconnect: " + exception);
            }
        });
    }

    private void executePOST(final HyperTrackPostRequest<?> postRequest) {
        // Initiate executeMqttPOST call, if NetworkClient is MQTT
        if (postRequest.getNetworkClient() == HyperTrackNetworkRequest.HTNetworkClient.HT_NETWORK_CLIENT_MQTT) {

            // MQTT Client is connected. Initiate postLocations call
            mqttClient.executeMqttPOST(postRequest);

        } else {
            // Initiate HTTP POST call
            HTTPClient.executeHttpPOST(postRequest);
        }
    }

    private void executeGET(final HyperTrackGetRequest getRequest) {
        // Initiate MQTT subscribe call
        if (getRequest.getNetworkClient() == HyperTrackNetworkRequest.HTNetworkClient.HT_NETWORK_CLIENT_MQTT) {

            // MQTT Client is connected. Initiate postLocations call
            mqttClient.executeMqttGET(getRequest);

        } else {
            // Initiate HTTP GET call
            HTTPClient.executeHttpGET(getRequest);
        }
    }

    /**
     * Methods for MQTT Client Disconnection callback
     */
    public abstract class MQTTClientDisconnectionCallback {
        public abstract void onMQTTDisconnectionSuccess();
        public abstract void onMQTTDisconnectionError(IMqttToken asyncActionToken, Throwable exception);
    }
}
