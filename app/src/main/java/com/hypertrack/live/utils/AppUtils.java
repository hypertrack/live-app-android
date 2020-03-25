package com.hypertrack.live.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hypertrack.live.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AppUtils {

    public static final String SHARE_BROADCAST_ACTION = "com.hypertrack.live.SHARE_TRIP";

    public static void shareTrackMessage(@NonNull Context context, String shareableMessage) {
        if (shareableMessage.isEmpty()) return;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareableMessage);
        sendIntent.setType("text/plain");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent intent = new Intent(SHARE_BROADCAST_ACTION);
            intent.setPackage(context.getPackageName());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.share_trip_via), pendingIntent.getIntentSender());
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } else {
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sendIntent);
            Intent intent = new Intent(SHARE_BROADCAST_ACTION);
            context.sendBroadcast(intent);
        }
    }

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
