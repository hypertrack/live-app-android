package io.hypertrack.sendeta.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import io.hypertrack.sendeta.R;

/**
 * Created by piyush on 30/06/16.
 */
public class PermissionUtils {

    public static final int REQUEST_CODE_PERMISSION_GET_ACCOUNTS = 1;
    public static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    public static final int REQUEST_CODE_PERMISSION_READ_PHONE_STATE = 3;
    public static final int REQUEST_CODE_PERMISSION_CALL = 4;
    public static final int REQUEST_CODE_PERMISSION_SMS_RECEIVER = 5;
    public static final int REQUEST_CODE_PERMISSION_RECORD_AUDIO = 6;
    public static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 7;

    public static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1009;

    /**
     * Return true if the permission available, else starts permission request from user
     */
    public static boolean requestPermission(@NonNull final Activity activity,
                                            @NonNull final String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        int requestCode = getRequestCodeByPermission(permission);

        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                requestCode);
        return false;
    }

    /**
     * @param permission Permission for which Snackbar has to be shown
     */
    private static int getPermissionDeclinedMessage(final String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return R.string.permission_denied_location;
            default:
                return R.string.permission_declined_default_msg;
        }
    }

    private static int getRequestCodeByPermission(final String permission) {
        switch (permission) {
            case Manifest.permission.GET_ACCOUNTS:
                return REQUEST_CODE_PERMISSION_GET_ACCOUNTS;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return REQUEST_CODE_PERMISSION_LOCATION;
            case Manifest.permission.READ_PHONE_STATE:
                return REQUEST_CODE_PERMISSION_READ_PHONE_STATE;
            case Manifest.permission.CALL_PHONE:
                return REQUEST_CODE_PERMISSION_CALL;
            case Manifest.permission.RECEIVE_SMS:
                return REQUEST_CODE_PERMISSION_SMS_RECEIVER;
            case Manifest.permission.RECORD_AUDIO:
                return REQUEST_CODE_PERMISSION_RECORD_AUDIO;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE;
            default:
                return -1;
        }
    }

    public static void requestCallPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
    }

    public static AlertDialog showRationaleMessageAsDialog(@NonNull final Activity activity,
                                                           @NonNull final String permission,
                                                           @NonNull final String title,
                                                           @NonNull final String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PermissionUtils.requestPermission(activity, permission);
            }
        });
        return builder.show();
    }

    public static void openSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    /**
     * @param activity   Context where the Snackbar will be shown
     * @param permission Permission for which Snackbar has to be shown,
     *                   helps in deciding the message string for Snackbar
     * @return snackbar snackbar instance which can be useful to set callbacks,if needed
     */
    public static AlertDialog.Builder showPermissionDeclineDialog(@NonNull final Activity activity,
                                                       @NonNull final String permission,
                                                       @NonNull final String title,
                                                       @NonNull final String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(activity.getString(R.string.action_settings), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                openSettings(activity);
            }
        });
        builder.setNegativeButton(activity.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

        return builder;
    }

    /**
     * @param activity   Context where the Snackbar will be shown
     * @param permission Permission for which Snackbar has to be shown,
     *                   helps in deciding the message string for Snackbar
     * @return snackbar snackbar instance which can be useful to set callbacks,if needed
     */
    public static Snackbar showPermissionDeclineMessage(@NonNull final Activity activity,
                                                        @NonNull final String permission) {
        Snackbar snackbar = Snackbar
                .make(activity.findViewById(android.R.id.content), getPermissionDeclinedMessage(
                        permission), Snackbar.LENGTH_LONG);

        snackbar.setAction(R.string.action_settings, new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                openSettings(activity);
            }
        });
        snackbar.setActionTextColor(ContextCompat.getColor(activity, android.R.color.white)).show();
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
            }
        });
        return snackbar;
    }
}
