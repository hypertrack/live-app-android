package com.hypertrack.live.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AppUtils {
    public static final String SYS_PROP_VERSION_NAME = "ro.miui.ui.version.name";

    public static String getDeviceName(Context context) {

        String deviceName = Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);
        if (TextUtils.isEmpty(deviceName)) {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                deviceName = capitalize(model);
            } else {
                deviceName = capitalize(manufacturer) + " " + model;
            }
        }

        return deviceName;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            boolean isConnected = false;
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                isConnected = (activeNetwork != null) && (activeNetwork.isConnectedOrConnecting());
            }

            return isConnected;
        }

        return true;
    }

    public static boolean isGpsProviderEnabled(Context context) {
        if (context != null) {
            LocationManager cm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return cm != null && cm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        return true;
    }

    public static String getReadableModVersion(Context context) {
        return "";
    }

    public static String getSystemProperty(String propName) {
        String line = "unknown";
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }
}
