package io.hypertrack.sendeta.util;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.UserStore;

/**
 * Created by piyush on 10/09/16.
 */
public class Utils {

    public static void setCrashlyticsKeys(Context context){
        User user = UserStore.sharedStore.getUser();
        if (user != null) {
            // Set UserID
            String userID = user.getId() != null ? user.getId().toString() : "NULL";
            Crashlytics.setUserIdentifier(userID);
            Crashlytics.setString(CrashlyticsKeys.USER_ID, userID);

            // Set UserName
            String userName = !TextUtils.isEmpty(user.getFullName()) ? user.getFullName() : "NULL";
            Crashlytics.setUserName(userName);
            Crashlytics.setString(CrashlyticsKeys.USER_NAME, userName);

            // Set UserPhone & UserDeviceID
            Crashlytics.setString(CrashlyticsKeys.USER_PHONE, !TextUtils.isEmpty(user.getPhoneNumber()) ? user.getPhoneNumber() : "NULL");
            String deviceUUID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            Crashlytics.setString(CrashlyticsKeys.USER_DEVICE_ID, !TextUtils.isEmpty(deviceUUID) ? deviceUUID : "NULL");
        }
    }

    public class CrashlyticsKeys {
        public static final String USER_ID = "id";
        public static final String USER_NAME = "name";
        public static final String USER_PHONE = "phone";
        public static final String USER_DEVICE_ID = "device_id";
    }
}
