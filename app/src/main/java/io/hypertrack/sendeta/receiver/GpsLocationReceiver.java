
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.hypertrack.lib.HyperTrack;

import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;

/**
 * Created by piyush on 08/07/16.
 */
public class GpsLocationReceiver extends BroadcastReceiver {

    public static final String LOCATION_CHANGED = "LOCATION_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

            if (HyperTrack.checkLocationServices(context) && SharedPreferenceManager.getGeofencingRequest(context) != null) {
                // Add Geofencing Request
                ActionManager.getSharedManager(context).setGeofencingRequest(SharedPreferenceManager.getGeofencingRequest(context));
                ActionManager.getSharedManager(context).addGeofencingRequest();
            }

            Intent locationChangedIntent = new Intent(LOCATION_CHANGED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(locationChangedIntent);
        }
    }
}
