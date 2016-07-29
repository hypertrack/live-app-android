package io.hypertrack.sendeta.service;

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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import io.hypertrack.sendeta.util.SharedPreferenceManager;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken("932617501442",
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);
            SharedPreferenceManager.setGcmToken(token);

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
