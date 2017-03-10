package com.hypertrack.lib.internal.common.network;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.exception.NoResponseException;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.common.util.Utils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import io.hypertrack.smart_scheduler.Job;
import io.hypertrack.smart_scheduler.SmartScheduler;

/**
 * Created by piyush on 18/09/16.
 */
public class MqttClient implements MqttCallbackExtended, SmartScheduler.JobScheduledCallback {

    private static final String TAG = MqttClient.class.getSimpleName();
    private static final int MQTT_RECONNECTION_JOB = 1;
    private static final String MQTT_RECONNECTION_JOB_TAG = "com.hypertrack:MQTTReconnectionJob";
    private static final int MQTT_KEEP_ALIVE_INTERVAL = 120;

    private static final String HEADER_ENCODING = "Content-Encoding";
    private static final String ENCODING_GZIP = "gzip";
    private boolean isGzipEnabled = false;

    private Context mContext;
    private String userID;
    private MqttAndroidClient mqttClient;
    private IMqttToken connectToken, disconnectToken;
    private MqttConnectOptions connectOptions;
    private SmartScheduler jobScheduler;

    private Map<String, MQTTSubscription> subscribers;
    private Map<String, MQTTSubscription> pendingSubscriptions;

    public MqttClient(Context mContext, SmartScheduler jobScheduler, String userID) {
        this.mContext = mContext;
        this.jobScheduler = jobScheduler;
        this.userID = userID;

        this.subscribers = new HashMap<>();
        this.pendingSubscriptions = new HashMap<>();
    }

    private static String generateClientID(Context context) {
        String device_uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (TextUtils.isEmpty(device_uuid)) {
            return org.eclipse.paho.client.mqttv3.MqttClient.generateClientId();
        } else {
            return System.nanoTime() + "hypertrack" + device_uuid;
        }
    }

    public void updateUserID(String userID) {
        if (!TextUtils.isEmpty(userID) && !userID.equalsIgnoreCase(this.userID)) {
            this.userID = userID;
            // TODO: 28/02/17 Add connectMqttClient impl code here
//            this.connectMqttClient(mContext, null);
        }
    }

    public SmartScheduler getJobScheduler() {
        return jobScheduler;
    }

    public boolean isConnected() {
        if (mqttClient == null)
            return false;

        try {
            return mqttClient.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean isTopicSubscribed(String topic) {
        if (TextUtils.isEmpty(topic) || mqttClient == null || subscribers.get(topic) == null)
            return false;

        IMqttToken subToken = subscribers.get(topic).getSubscribeToken();
        if (subToken == null || !subToken.isComplete() || subToken.getException() != null)
            return false;

        String[] topics = subToken.getTopics();
        return !(topics == null || topic.length() == 0 || !Arrays.asList(topics).contains(topic));
    }

    private boolean isConnectQueued() {
        return !(mqttClient == null || connectToken == null || connectToken.isComplete());
    }

    private void initializeMqttClient(Context context) {
        if (mqttClient == null) {
            synchronized (MqttClient.class) {
                if (mqttClient == null) {
                    mqttClient = new MqttAndroidClient(context, BuildConfig.MQTT_BROKER_URL,
                            MqttClient.generateClientID(context));
                }
            }
        }

        // Initialize MqttConnectOptions
        initializeMQTTConnectOptions();
    }

    /**
     * Method to Connect MQTT Client and initialize MqttClient instance
     *
     * @param context
     * @param callback
     */
    public void connectMqttClient(final Context context, final MQTTClientConnectionCallback callback) {
        try {
            if (isConnectQueued()) {
                if (callback != null)
                    callback.onMQTTConnectionError(connectToken,
                            new RuntimeException("connectMQTTClient request already queued"));
                return;
            }

            // Initialize MQTT Client & MQTTClient ConnectOptions
            initializeMqttClient(context);

            // Initiate MQTTClient connection
            mqttClient.setCallback(this);
            connectToken = mqttClient.connect(connectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // on MQTT Connection success
                    onMQTTConnectSuccess(callback);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Check if exception while connectMqttClient is an error or not
                    onMQTTConnectError(asyncActionToken, exception, callback);
                }
            });
        } catch (MqttException exception) {
            exception.printStackTrace();
            onMQTTConnectError(connectToken, exception, callback);
        }
    }

    private boolean onMQTTConnectError(IMqttToken connectToken, Throwable exception,
                                       MQTTClientConnectionCallback callback) {
        if (exception == null || exception.getMessage() == null) {
            HTLog.e(TAG, "Error occurred while connectMqttClient");
            if (callback != null) {
                callback.onMQTTConnectionError(connectToken, exception);
            }
            return true;
        }

        String msg = exception.getMessage();

        // Check MqttException's reasonCode
        if (exception instanceof MqttException) {
            int reasonCode = ((MqttException) exception).getReasonCode();
            if (reasonCode == MqttException.REASON_CODE_CLIENT_CONNECTED) {
                if (callback != null) {
                    callback.onMQTTConnectionSuccess();
                }
                return false;
            }

            msg = msg + ", code: " + reasonCode;

            if (reasonCode == MqttException.REASON_CODE_CLIENT_EXCEPTION) {
                if (exception.getCause() != null) {
                    msg = msg + ", cause: " + exception.getCause();
                } else {
                    msg = msg + (exception.getStackTrace() != null ? ", stacktrace: " +
                                    Arrays.toString(exception.getStackTrace()) : "");
                }
            }
        }

        HTLog.e(TAG, "Error occurred while connectMqttClient: " + msg);
        if (callback != null) {
            callback.onMQTTConnectionError(connectToken, exception);
        }
        return true;
    }

    private void onMQTTConnectSuccess(MQTTClientConnectionCallback callback) {
        // We are connected
        HTLog.i(TAG, "Mqtt connection successful" +
                (connectOptions.getWillDestination() != null ? " with lastWill: "
                        + connectOptions.getWillDestination() : ""));

        // Initiate subscribe to topics, in case userID is set
        movePendingToSubscribers();

        // Remove MQTT ReconnectionJob, if it still exists
        removeMQTTReconnectionJob();

        if (callback != null)
            callback.onMQTTConnectionSuccess();
    }

    private void initializeMQTTConnectOptions() {
        connectOptions = new MqttConnectOptions();
        connectOptions.setKeepAliveInterval(MQTT_KEEP_ALIVE_INTERVAL);
        connectOptions.setCleanSession(false);
        connectOptions.setAutomaticReconnect(true);

        // Set LWT if userID has been configured
        if (!TextUtils.isEmpty(userID)) {
            setLWT(connectOptions);
        }
    }

    private void setLWT(MqttConnectOptions connectOptions) {
        try {
            String topic = BuildConfig.MQTT_BASE_TOPIC + "UserConnection/" + userID;

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", userID);
            jsonObject.put("is_connected", false);

            String payload = getPayload(jsonObject);
            connectOptions.setWill(topic, payload.getBytes("UTF-8"), 1, false);
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while MQTTLastWillTestament");
        }
    }

    public void disconnectMqttClient(final NetworkManagerImpl.MQTTClientDisconnectionCallback callback) {
        try {
            // TODO: 24/02/17 Handle if MQTT connection is queued

            // Check if MQTTClient is connected or not
            if (!isConnected()) {
                if (callback != null)
                    callback.onMQTTDisconnectionSuccess();
                return;
            }

            // Initiate MQTTClient connection
             disconnectToken = mqttClient.disconnect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // do nothing
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Check if exception while connectMqttClient is an error or not
                    onMQTTDisconnectError(asyncActionToken, exception, callback);
                }
            });
        } catch (MqttException exception) {
            exception.printStackTrace();
            onMQTTDisconnectError(disconnectToken, exception, callback);
        }
    }

    public void onMQTTDisconnectError(IMqttToken disconnectToken, Throwable exception,
                                      NetworkManagerImpl.MQTTClientDisconnectionCallback callback) {
        try {
            if (exception == null || exception.getMessage() == null) {
                HTLog.e(TAG, "Error occurred while disconnectMqttClient");
                mqttClient.disconnectForcibly();
                if (callback != null) {
                    callback.onMQTTDisconnectionSuccess();
                }
                return;
            }

            String msg = exception.getMessage();

            // Check MqttException's reasonCode
            if (exception instanceof MqttException) {
                int reasonCode = ((MqttException) exception).getReasonCode();
                if (reasonCode == MqttException.REASON_CODE_CLIENT_ALREADY_DISCONNECTED
                        || reasonCode == MqttException.REASON_CODE_CLIENT_NOT_CONNECTED) {
                    if (callback != null) {
                        callback.onMQTTDisconnectionSuccess();
                    }
                    return;
                }

                msg = msg + ", code: " + reasonCode;

                if (reasonCode == MqttException.REASON_CODE_CLIENT_EXCEPTION) {
                    if (exception.getCause() != null) {
                        msg = msg + ", cause: " + exception.getCause();
                    } else {
                        msg = msg + (exception.getStackTrace() != null ? ", stacktrace: " +
                                Arrays.toString(exception.getStackTrace()) : "");
                    }
                }
            }

            HTLog.e(TAG, "Error occurred while disconnectMqttClient: " + msg);
            mqttClient.disconnectForcibly();
            if (callback != null) {
                callback.onMQTTDisconnectionSuccess();
            }
        } catch (MqttException e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while onMQTTDisconnectError: " + e);
            if (callback != null) {
                callback.onMQTTDisconnectionError(disconnectToken, e);
            }
        }
    }

    public void checkForMQTTConnection(final Context context, final MQTTClientConnectionCallback callback) {
        if (callback == null) {
            HTLog.e(TAG, "MQTTClientConnectionCallback is null");
            throw new IllegalArgumentException("MQTTClientConnectionCallback is null");
        }

        // Check if MQTT Client is connected
        if (this.isConnected()) {
            // Initiate subscribe to topics, in case userID is set
            movePendingToSubscribers();
            callback.onMQTTConnectionSuccess();
            return;
        }

        moveSubscribersToPending();

        // Connect MQTT Client
        this.connectMqttClient(context, new MQTTClientConnectionCallback() {
            @Override
            public void onMQTTConnectionSuccess() {
                callback.onMQTTConnectionSuccess();
            }

            @Override
            public void onMQTTConnectionError(IMqttToken asyncActionToken, Throwable exception) {
                callback.onMQTTConnectionError(asyncActionToken, exception);
            }
        });
    }

    public void executeMqttGET(final HyperTrackGetRequest subscribeRequest) {
        final MQTTSubscription subscription = new MQTTSubscription(subscribeRequest.getUrl(),
                subscribeRequest.getMessageArrivedCallback(), subscribeRequest.getSubscriptionSuccessCallback());

        // Add MQTTSubscription to pendingSubscriptions, if not already present
        addPendingSubscription(subscription);

        updateUserID(subscribeRequest.getLastWillUserID());

        // Check if MQTT Client is connected, Initiate connection in case not.
        this.checkForMQTTConnection(mContext, new MQTTClientConnectionCallback() {
            @Override
            public void onMQTTConnectionSuccess() {

                // MQTT Client is connected. Initiate postLocations call
                subscribe(subscription, new MQTTSubscribeTopicCallback() {
                    @Override
                    public void onMQTTSubscribeTopicSuccess() {
                        if (subscribeRequest.getListener() != null) {
                            subscribeRequest.getListener().onResponse(null);
                        }
                    }

                    @Override
                    public void onMQTTSubscribeTopicError(Exception exception) {
                        if (subscribeRequest.getErrorListener() != null) {
                            subscribeRequest.getErrorListener().onErrorResponse(null, exception);
                        }
                    }
                });
            }

            @Override
            public void onMQTTConnectionError(IMqttToken asyncActionToken, Throwable throwable) {
                if (subscribeRequest.getErrorListener() != null) {
                    subscribeRequest.getErrorListener().onErrorResponse(null,
                            new RuntimeException(throwable != null ? throwable.toString() : "Throwable is null"));
                }
            }
        });
    }

    public void executeMqttPOST(final HyperTrackPostRequest publishRequest) {

        // Check if MQTT Client is connected, Initiate connection in case not.
        this.checkForMQTTConnection(mContext, new MQTTClientConnectionCallback() {
            @Override
            public void onMQTTConnectionSuccess() {

                // MQTT Client is connected. Initiate postLocations call
                publish(publishRequest.getUrl(),
                        publishRequest.getRequestBody(), publishRequest.isRetained(), new MQTTPublishMessageCallback() {
                            @Override
                            public void onMQTTPublishMessageSuccess() {
                                if (publishRequest.getListener() != null) {
                                    publishRequest.getListener().onResponse(null);
                                }
                            }

                            @Override
                            public void onMQTTPublishMessageError(Exception exception) {
                                if (publishRequest.getErrorListener() != null) {
                                    publishRequest.getErrorListener().onErrorResponse(null, exception);
                                }
                            }
                        });
            }

            @Override
            public void onMQTTConnectionError(IMqttToken asyncActionToken, Throwable throwable) {
                if (publishRequest.getErrorListener() != null) {
                    publishRequest.getErrorListener().onErrorResponse(null,
                            new RuntimeException(throwable != null ? throwable.toString() : "Throwable is null"));
                }
            }
        });
    }

    /**
     * Call this method to Get Request Headers
     *
     * @return
     */
    private JSONObject getRequestHeaders() {

        JSONObject params = new JSONObject();

        try {
            // Get BatteryState & Device Headers
            final HashMap<String, String> additionalHeaders = new HashMap<>();
            additionalHeaders.putAll(Utils.getBatteryHeader(mContext));

            // Get Authorization, User-Agent & Device-time headers
            String token = HyperTrack.getPublishableKey(mContext);
            params.put("Authorization", "Token " + (token != null ? token : ""));
            params.put("Content-Type", "application/json");
            params.put("User-Agent", "HyperTrack " + HyperTrack.getSDKPlatform() + " " +
                    BuildConfig.SDK_VERSION_NAME + " (Android " + Build.VERSION.RELEASE + ")");
            params.put("Device-Time", DateTimeUtility.getCurrentTime());
            params.put("Device-ID", Utils.getDeviceId(mContext));

            if (isGzipEnabled) {
                params.put(HEADER_ENCODING, ENCODING_GZIP);
            }

            for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
                params.put(header.getKey(), header.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getRequestHeaders: " + e);
        }

        return params;
    }

    /*
     * Publish methods
     */

    private String getPayload(final Object body) {
        String payload = null;
        try {
            JSONObject payloadJSON = new JSONObject();
            payloadJSON.put("headers", getRequestHeaders());

            // Check if gzip is enabled and getCompressed RequestBody
            if (isGzipEnabled) {
                payloadJSON.put("body", getCompressed(body.toString()));
            } else {
                payloadJSON.put("body", body);
            }

            payload = payloadJSON.toString();

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while getPayload: " + e);
        }

        return payload;
    }

    private byte[] getCompressed(String requestBody) {
        if (requestBody != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(requestBody.length());
            GZIPOutputStream gzipOutputStream;

            try {
                gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream, 32);
                gzipOutputStream.write(requestBody.getBytes("UTF-8"));
                gzipOutputStream.close();
                byte[] compressed = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
                return compressed;
            } catch (Exception exception) {
                HTLog.e(TAG, "Exception occurred while getCompressed: " + exception);
            }
        }
        return null;
    }

    private void publish(final String topic, final Object body,
                         final boolean isRetained, final MQTTPublishMessageCallback callback) {
        if (callback == null) {
            HTLog.e(TAG, "Error occurred while publish: MQTT PublishMessage Callback is NULL");
            throw new IllegalArgumentException("Error occurred while publish: MQTT PublishMessage Callback is NULL");
        }

        if (TextUtils.isEmpty(topic)) {
            callback.onMQTTPublishMessageError(new IllegalArgumentException("Error occurred while publish: topic is NULL"));
            return;
        }

        if (body == null) {
            callback.onMQTTPublishMessageError(new IllegalArgumentException("Error occurred while publish: body is NULL"));
            return;
        }

        if (mqttClient == null || !mqttClient.isConnected()) {
            callback.onMQTTPublishMessageError(new NoResponseException("MQTT Client is NULL or Not Connected."));
            return;
        }

        // Get MQTT Message Payload (Headers + Body)
        String payload = getPayload(body);

        byte[] encodedPayload;
        try {
            // Initiate MQTT Message Publish
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(isRetained);
            mqttClient.publish(topic, message, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // No Exception occurred meaning MQTT Publish was successful
                    callback.onMQTTPublishMessageSuccess();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Exception occurred while MQTT Publish
                    RuntimeException runtimeException = new RuntimeException(
                            handleMQTTException("Error occurred while MQTT publish", exception));
                    callback.onMQTTPublishMessageError(runtimeException);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            RuntimeException runtimeException = new RuntimeException(
                    handleMQTTException("Error occurred while MQTT publish", e));
            callback.onMQTTPublishMessageError(runtimeException);
        }
    }

    private String handleMQTTException(String errorMessagePrefix, Throwable exception) {
        if (exception == null || exception.getMessage() == null) {
            HTLog.e(TAG, errorMessagePrefix);
            return "Exception is null";
        }

        String msg = exception.getMessage();

        // Check MqttException's reasonCode
        if (exception instanceof MqttException) {
            int reasonCode = ((MqttException) exception).getReasonCode();
            msg = msg + ", code: " + reasonCode;

            if (reasonCode == MqttException.REASON_CODE_CLIENT_EXCEPTION) {
                if (exception.getCause() != null) {
                    msg = msg + ", cause: " + exception.getCause();
                } else {
                    msg = msg + (exception.getStackTrace() != null ? ", stacktrace: " +
                            Arrays.toString(exception.getStackTrace()) : "");
                }
            }
        }

        return msg;
    }

    /**
     * Subscription Methods
     */

    private boolean isSubscriptionQueued(String topic) {
        if (TextUtils.isEmpty(topic) || mqttClient == null || pendingSubscriptions.get(topic) == null)
            return false;

        IMqttToken subToken = pendingSubscriptions.get(topic).getSubscribeToken();
        if (subToken == null || subToken.isComplete())
            return false;

        String[] topics = subToken.getTopics();
        return !(topics == null || topics.length == 0 || !Arrays.asList(topics).contains(topic));
    }

    private void subscribe(final MQTTSubscription subscription, final MQTTSubscribeTopicCallback callback) {
        // Check if MqttClient is connected here or not
        if (!this.isConnected()) {
            if (callback != null)
                callback.onMQTTSubscribeTopicError(new RuntimeException("MqttClient is NotConnected or NULL"));
            return;
        }

        // Check if already subscribed to the topic
        if (isTopicSubscribed(subscription.getTopic())) {
            // OnSubscribeSuccess
            onSubscribeSuccess(subscription, callback);
            return;
        }

        // Check if subscription request already queued for the topic
        if (isSubscriptionQueued(subscription.getTopic())) {
            if (callback != null)
                callback.onMQTTSubscribeTopicError(new RuntimeException("Subscription request already " +
                        "queued for topic: " + subscription.getTopic()));
            return;
        }

        Log.d(TAG, "Attempting Subscribe for topic: " + subscription.getTopic());

        int qos = 1;
        try {
            IMqttToken subToken = mqttClient.subscribe(subscription.getTopic(), qos, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    HTLog.i(TAG, "Subscribe to topic: " + subscription.getTopic() + " successful");

                    // OnSubscribeSuccess
                    onSubscribeSuccess(subscription, callback);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    HTLog.i(TAG, "Error occurred while Subscribe to topic: " + subscription.getTopic()
                            + ": " + (exception != null ? exception.toString() : "Throwable is null"));

                    RuntimeException runtimeException = new RuntimeException(
                            handleMQTTException("Error occurred while MQTT subscribe", exception));
                    if (callback != null)
                        callback.onMQTTSubscribeTopicError(runtimeException);
                }
            });

            // Set Subscription token
            subscription.setSubscribeToken(subToken);
            setPendingSubscriptionToken(subscription, subToken);

        } catch (Exception e) {
            e.printStackTrace();
            RuntimeException runtimeException = new RuntimeException(
                    handleMQTTException("Error occurred while MQTT subscribe", e));
            if (callback != null)
                callback.onMQTTSubscribeTopicError(runtimeException);
        }
    }

    private void onSubscribeSuccess(MQTTSubscription subscription, MQTTSubscribeTopicCallback callback) {
        // Move this topic from pendingSubscriptions to Subscribers list
        addPendingSubscriptionToSubscriber(subscription);

        if (callback != null)
            callback.onMQTTSubscribeTopicSuccess();

        if (subscription != null && subscription.getSubscriptionSuccessCallback() != null)
            subscription.getSubscriptionSuccessCallback().onMQTTSubscriptionSuccess(subscription.getTopic());
    }

    private boolean addPendingSubscription(MQTTSubscription subscription) {
        if (subscription == null || TextUtils.isEmpty(subscription.getTopic())
                || subscription.getMessageArrivedCallback() == null
                || pendingSubscriptions.get(subscription.getTopic()) != null
                || subscribers.get(subscription.getTopic()) != null)
            return false;

        HTLog.i(TAG, "Adding subscription to pendingSubscriptions: " + subscription.getTopic());
        pendingSubscriptions.put(subscription.getTopic(), subscription);
        return true;
    }

    private void setPendingSubscriptionToken(MQTTSubscription subscription, IMqttToken subToken) {
        if (subscription == null || TextUtils.isEmpty(subscription.getTopic())
                || subToken == null)
            return;

        pendingSubscriptions.put(subscription.getTopic(), subscription);
    }

    private void addPendingSubscriptionToSubscriber(MQTTSubscription subscription) {
        if (subscription == null || TextUtils.isEmpty(subscription.getTopic())
                || subscription.getMessageArrivedCallback() == null)
            return;

        if (subscribers.values().contains(subscription)) {
            pendingSubscriptions.remove(subscription.getTopic());
            return;
        }

        HTLog.i(TAG, "Adding PendingSubscription to subscribers: " + subscription.getTopic());

        subscribers.put(subscription.getTopic(), subscription);
        pendingSubscriptions.remove(subscription.getTopic());
    }

    private void moveSubscribersToPending() {
        if (subscribers != null && subscribers.size() > 0) {
            HTLog.i(TAG, "Moving " + subscribers.size() + " subscribers to pendingSubscriptions");

            pendingSubscriptions.putAll(subscribers);
            subscribers.clear();
        }
    }

    private void movePendingToSubscribers() {
        if (pendingSubscriptions.size() > 0) {
            for (Iterator<MQTTSubscription> it = pendingSubscriptions.values().iterator(); it.hasNext(); ) {
                subscribe(it.next(), null);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //do nothing
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // Check if there is any update for SDKControls topic
        HTLog.i(TAG, "messageArrived called for Topic: " + topic + " message: " + message);

        MQTTSubscription subscription = subscribers.get(topic);

        // Check if current subscription exists in pendingSubscriptions
        if (subscription == null && pendingSubscriptions.size() > 0) {
            for (Map.Entry<String, MQTTSubscription> entry : pendingSubscriptions.entrySet()) {
                if (topic.equalsIgnoreCase(entry.getKey())) {
                    subscription = entry.getValue();
                    subscribers.put(topic, subscription);
                    pendingSubscriptions.remove(topic);
                    break;
                }
            }
        }

        // Check if a valid subscription was found
        if (subscription != null) {
            subscription.getMessageArrivedCallback().onMessageArrived(topic, message.toString());
        } else {
            HTLog.e(TAG, "Error occurred while messageArrived, Subscription NOT present: " + topic);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        HTLog.e(TAG, "Connection Lost in HtMqttClient, cause: " + (cause != null ? cause : "null") +
                ", stacktrace: " + (cause != null && cause.getStackTrace() != null ?
                Arrays.toString(cause.getStackTrace()) : "null"));

        moveSubscribersToPending();
        // TODO: 24/02/17 Add once MQTT Client connection is figured out
//        setMQTTReconnectionJob();
//        broadcastConnectionLost();
    }

    private void setMQTTReconnectionJob() {
        try {
            // Create a Periodic HTJob object with BatchDuration * Multiplier as the interval
            Job mqttReconnectionJob = new Job.Builder(MQTT_RECONNECTION_JOB, this, Job.Type.JOB_TYPE_ALARM,
                    MQTT_RECONNECTION_JOB_TAG)
                    .setRequiredNetworkType(Job.NetworkType.NETWORK_TYPE_CONNECTED)
                    .setPeriodic(60 * 1000)
                    .build();

            // Schedule Periodic Job
            jobScheduler.addJob(mqttReconnectionJob);
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while setupLocationServiceAlarmJob: " + e);
        }
    }

    private void removeMQTTReconnectionJob() {
        if (jobScheduler != null && jobScheduler.contains(MQTT_RECONNECTION_JOB)) {
            HTLog.i(TAG, "MQTTReconnection successful. Removing MqttReconnectionJob");
            jobScheduler.removeJob(MQTT_RECONNECTION_JOB);
        }
    }

    private void broadcastConnectionLost() {
        Intent intent = new Intent("com.hypertrack.mqtt_connection_lost");
        mContext.sendBroadcast(intent);
    }

    @Override
    public void onJobScheduled(Context context, Job job) {
        if (job != null && job.getJobId() == MQTT_RECONNECTION_JOB) {
            this.checkForMQTTConnection(context, new MQTTClientConnectionCallback() {
                @Override
                public void onMQTTConnectionSuccess() {
                    // Remove MQTT Reconnection Job
                    removeMQTTReconnectionJob();
                }

                @Override
                public void onMQTTConnectionError(IMqttToken asyncActionToken, Throwable exception) {
                    HTLog.e(TAG, "MqttReconnection failed: " +
                            (exception != null ? exception.toString() : "Throwable is null"));
                }
            });
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        HTLog.i(TAG, "MQTTConnect successful, reconnect: " + reconnect + ", isConnected: " + isConnected());
        onMQTTConnectSuccess(null);
    }

    /**
     * Methods for MQTT Client Subscribe callback
     */
    private abstract class MQTTSubscribeTopicCallback {
        public abstract void onMQTTSubscribeTopicSuccess();

        public abstract void onMQTTSubscribeTopicError(Exception exception);
    }

    /**
     * Methods for MQTT Client Publish callback
     */
    private abstract class MQTTPublishMessageCallback {
        public abstract void onMQTTPublishMessageSuccess();

        public abstract void onMQTTPublishMessageError(Exception exception);
    }

    /**
     * Methods for MQTT Client Connection callback
     */
    private abstract class MQTTClientConnectionCallback {
        public abstract void onMQTTConnectionSuccess();
        public abstract void onMQTTConnectionError(IMqttToken asyncActionToken, Throwable exception);
    }


}