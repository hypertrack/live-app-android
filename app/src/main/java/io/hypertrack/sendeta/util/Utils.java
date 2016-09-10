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
            Crashlytics.setInt(CrashlyticsKeys.USER_ID, user.getId() != null ? user.getId() : -1);
            Crashlytics.setString(CrashlyticsKeys.USER_NAME, !TextUtils.isEmpty(user.getFullName()) ? user.getFullName() : "null");
            Crashlytics.setString(CrashlyticsKeys.USER_PHONE, !TextUtils.isEmpty(user.getPhoneNumber()) ? user.getPhoneNumber() : "null");
            String deviceUUID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            Crashlytics.setString(CrashlyticsKeys.USER_DEVICE_ID, !TextUtils.isEmpty(deviceUUID) ? deviceUUID : "null");
        }
    }

    public class CrashlyticsKeys {
        public static final String USER_ID = "id";
        public static final String USER_NAME = "name";
        public static final String USER_PHONE = "phone";
        public static final String USER_DEVICE_ID = "device_id";
    }
}
