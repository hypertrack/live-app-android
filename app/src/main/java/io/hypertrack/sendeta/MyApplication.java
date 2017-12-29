
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
package io.hypertrack.sendeta;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.squareup.leakcanary.LeakCanary;

import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.common.ApiKey;
import io.hypertrack.sendeta.util.DevDebugUtils;

/**
 * Created by suhas on 11/11/15.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        LeakCanary.install(this);

        try {
            if (!HTTextUtils.isEmpty(ApiKey.getApiKey(this))) {
                Fabric.with(this, new Crashlytics());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize HyperTrack SDK
        HyperTrack.initialize(this.getApplicationContext(), BuildConfig.HYPERTRACK_PK);
        HyperTrack.enableMockLocations(true);
        HyperTrack.disablePersistentNotification(false);

        // Initialize Branch.io
        Branch.getAutoInstance(this);

        // (NOTE: IFF current Build Variant is DEBUG)
        // Initialize Stetho to debug Databases
        DevDebugUtils.installStetho(this);
        // Enable HyperTrack Debug Logging
        DevDebugUtils.setHyperLogLevel(Log.VERBOSE);
        // Log HyperTrack SDK Version
        DevDebugUtils.sdkVersionMessage();
    }
}