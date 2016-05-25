package io.hypertrack.meta;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import io.hypertrack.meta.util.Constants;

/**
 * Created by suhas on 04/01/16.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    public RegistrationIntentService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM Registration Token: " + token);
            updateRegistrationToken(token);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateRegistrationToken(String token) {

            SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Constants.GCM_REGISTRATION_TOKEN, token);
            editor.apply();

    }
}
