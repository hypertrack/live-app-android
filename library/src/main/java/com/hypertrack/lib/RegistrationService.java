package com.hypertrack.lib;

/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.hypertrack.lib.internal.common.logging.HTLog;

public class RegistrationService extends IntentService {

    private static final String TAG = "RegistrationService";

    public RegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            HyperTrackImpl.getInstance().initialize(getApplicationContext(), null);
            HyperTrackImpl.getInstance().handleIntent(getApplicationContext(), intent);

            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getResources().getString(com.hypertrack.lib.R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            HTLog.d(TAG, "HyperTrack GCM token: " + token);
            // [END get_token]

            // Save GcmToken to UserPreferences
            HyperTrackImpl.getInstance().userPreferences.setGcmToken(token);
            HTLog.i(TAG, "Transmitter Gcm Token registered successfully.");

            // Post GcmToken fetched to server
            HyperTrackImpl.getInstance().transmitterClient.postRegistrationToken();

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "Exception occurred while RegistrationService.onHandleIntent: " + e);
        }
    }
}