package com.hypertrack.lib;

import android.app.IntentService;
import android.content.Intent;

import com.hypertrack.lib.internal.common.network.HyperTrackNetworkRequest.HTNetworkClient;

/**
 * Created by piyush on 20/10/16.
 */
public class BootReceiverService extends IntentService {

    public BootReceiverService(){
        super(BootReceiverService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BootReceiverService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Check if LocationService is live and restart the service, if applicable
        if (HyperTrack.isTracking()) {
            HyperTrackImpl.getInstance().transmitterClient.startTracking(true, null);
        }

        // Post DeviceLogs To Server
        HyperTrackImpl.getInstance().logsManager.postDeviceLogs(HTNetworkClient.HT_NETWORK_CLIENT_HTTP);
    }
}
