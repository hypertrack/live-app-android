package io.hypertrack.meta;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by suhas on 04/01/16.
 */
public class MyInstanceIDListenerService  extends InstanceIDListenerService {
    private static final String TAG = "MyInstanceIDLS";
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
