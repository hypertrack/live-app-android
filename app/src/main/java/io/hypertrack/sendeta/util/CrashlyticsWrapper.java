
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

package io.hypertrack.sendeta.util;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.hypertrack.lib.internal.common.util.HTTextUtils;

import io.fabric.sdk.android.services.common.ApiKey;
import io.hypertrack.sendeta.model.HyperTrackLiveUser;
import io.hypertrack.sendeta.store.OnboardingManager;

/**
 * Created by Aman on 12/07/17.
 */

public class CrashlyticsWrapper {

    public static void log(Exception e) {
        // TODO: 27/09/17 Check if context can be passed here
//        String apiKey = null;
//        apiKey = ApiKey.getApiKey(context);
//        if (!HTTextUtils.isEmpty(apiKey)) {
        try {
            Crashlytics.logException(e);
        } catch (Exception e1) {
            // do nothing
        }
//        }
    }

    private class CrashlyticsKeys {
        static final String USER_ID = "id";
        static final String USER_NAME = "name";
        static final String USER_PHONE = "phone";
        static final String USER_DEVICE_ID = "device_id";
    }

    public static void setCrashlyticsKeys(Context mContext) {
        try {
            HyperTrackLiveUser user = OnboardingManager.sharedManager(mContext).getUser();
            String apiKey = ApiKey.getApiKey(mContext);
            if (!HTTextUtils.isEmpty(apiKey)) {
                if (user != null) {
                    // Set UserID
                    String userID = user.getId() != null ? user.getId() : "NULL";
                    Crashlytics.setUserIdentifier(userID);
                    Crashlytics.setString(CrashlyticsKeys.USER_ID, userID);

                    // Set UserName
                    String userName = !HTTextUtils.isEmpty(user.getName()) ? user.getName() : "NULL";
                    Crashlytics.setUserName(userName);
                    Crashlytics.setString(CrashlyticsKeys.USER_NAME, userName);

                    // Set UserPhone & UserDeviceID
                    Crashlytics.setString(CrashlyticsKeys.USER_PHONE, !HTTextUtils.isEmpty(user.getPhone()) ? user.getPhone() : "NULL");
                    String deviceUUID = com.hypertrack.lib.internal.common.util.Utils.getDeviceId(mContext);
                    Crashlytics.setString(CrashlyticsKeys.USER_DEVICE_ID, !HTTextUtils.isEmpty(deviceUUID) ? deviceUUID : "NULL");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}