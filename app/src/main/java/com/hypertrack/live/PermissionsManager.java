package com.hypertrack.live;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsManager {

    public static boolean isAccessFineLocationPermissionApproved(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isActivityRecognitionPermissionApproved(Context context) {
        return android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isAllPermissionsApproved(Context context) {
        return isAccessFineLocationPermissionApproved(context)
                && isActivityRecognitionPermissionApproved(context);
    }

    public static void requestPermissions(Activity activity, int requestCode) {
        boolean permissionAccessCoarseLocationApproved = isAccessFineLocationPermissionApproved(activity);
        boolean permissionActivityRecognitionApproved = isActivityRecognitionPermissionApproved(activity);

        List<String> permissions = new ArrayList<>();
        if (!permissionAccessCoarseLocationApproved) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!permissionActivityRecognitionApproved) {
                permissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
            }
        }

        ActivityCompat.requestPermissions(activity, permissions.toArray(new String[0]), requestCode);
    }

}
