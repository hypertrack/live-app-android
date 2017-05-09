package io.hypertrack.sendeta.util;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Locale;


/**
 * Created by piyush on 10/09/16.
 */
public class Utils {
    public static final int DISTANCE_IN_METERS = 10000;
    public static final long LOCATION_UPDATE_INTERVAL_TIME = 5000;
    public static final long INITIAL_LOCATION_UPDATE_INTERVAL_TIME = 500;

    public static String getCountryRegionFromPhone(Context paramContext) {
        TelephonyManager service = (TelephonyManager) paramContext.getSystemService(Context.TELEPHONY_SERVICE);

        String code = null;
        if (service != null) {
            code = service.getNetworkCountryIso();
        }

        if (!TextUtils.isEmpty(code)) {
            code = service.getSimCountryIso();
        }

        if (TextUtils.isEmpty(code)) {
            code = paramContext.getResources().getConfiguration().locale.getCountry();
        }

        if (code != null) {
            return code.toUpperCase();
        }

        return null;
    }

    public static String getCountryName(String isoCode) {
        if (!TextUtils.isEmpty(isoCode)) {
            Locale locale = new Locale(Locale.getDefault().getDisplayLanguage(), isoCode);
            return locale.getDisplayCountry().trim();
        }

        return null;
    }

    public static LatLngBounds getBounds(LatLng latLng, int mDistanceInMeters) {
        double latRadian = Math.toRadians(latLng.latitude);

        double degLatKm = 110.574235;
        double degLongKm = 110.572833 * Math.cos(latRadian);
        double deltaLat = mDistanceInMeters / 1000.0 / degLatKm;
        double deltaLong = mDistanceInMeters / 1000.0 / degLongKm;

        double minLat = latLng.latitude - deltaLat;
        double minLong = latLng.longitude - deltaLong;
        double maxLat = latLng.latitude + deltaLat;
        double maxLong = latLng.longitude + deltaLong;

        LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(new LatLng(minLat, minLong));
        b.include(new LatLng(maxLat, maxLong));

        return b.build();
    }

    /**
     * Method to show Keyboard implicitly with @param editText as the focus
     * @param context
     * @param view
     */
    public static void showKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Method to hide keyboard
     * @param context
     * @param view
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Method to get the device ID
     * @param context
     * @return
     */

    public static String getDeviceId(Context context) {
        String device_uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return device_uuid != null ? device_uuid : "";
    }
}
