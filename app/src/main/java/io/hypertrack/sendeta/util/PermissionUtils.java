
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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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

    public static boolean checkForPermission(@NonNull final Activity activity,
                                             @NonNull final String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }

    /**
     * Return true if the permission available, else starts permission request from user
     */
    public static boolean requestPermission(@NonNull final Activity activity,
                                            @NonNull final String permission) {

        if (checkForPermission(activity, permission)) {
            return true;
        }

        int requestCode = getRequestCodeByPermission(permission);

        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                requestCode);
        return false;
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

    public static AlertDialog showRationaleMessageAsDialog(@NonNull final Activity activity,
                                                           @NonNull final String permission,
                                                           @NonNull final String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
                                                                  @NonNull final String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
}
