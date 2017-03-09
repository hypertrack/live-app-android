package com.hypertrack.lib.internal.common.logging;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.hypertrack.lib.internal.common.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by piyush on 22/08/16.
 */
/** package */ class DeviceLogTable {

    private static final String TAG = DeviceLogTable.class.getSimpleName();
    private static final int DEVICE_LOG_REQUEST_QUERY_LIMIT = 500;

    private static final String TABLE_NAME = "device_logs";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DEVICE_LOG = "device_log";

    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_DEVICE_LOG + " TEXT"
            + ");";

    public static void onCreate(SQLiteDatabase db) {
        if (db == null) {
            return;
        }

        try {
            db.execSQL(DATABASE_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "DeviceLogTable: Exception occurred while onCreate: " + e);
        }
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db == null) {
            return;
        }

        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);

            HTLog.i(TAG, "DeviceLogTable onUpgrade called. Executing drop_table query to clear old logs.");
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "DeviceLogTable: Exception occurred while onUpgrade: " + e);
        }
    }

    public static long getCount(SQLiteDatabase db) {
        try {
            if (db == null) {
                return 0;
            }

            return DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "DeviceLogTable: Exception occurred while getCount: " + e);
            return 0L;
        }
    }

    public static void addDeviceLog(SQLiteDatabase db, String deviceLog) {
        if (db == null || TextUtils.isEmpty(deviceLog)) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DEVICE_LOG, deviceLog);

        try {
            db.insert(TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "DeviceLogTable: Exception occurred while addDeviceLog: " + e);
        }
    }

    public static void deleteDeviceLog(SQLiteDatabase db, List<DeviceLog> deviceLogList) {
        if (db == null)
            return;

        StringBuilder builder = new StringBuilder();
        for (DeviceLog deviceLog : deviceLogList) {
            if (deviceLog != null && deviceLog.getId() > 0) {
                builder.append(deviceLog.getId())
                        .append(",");
            }
        }

        if (builder.length() == 0) {
            return;
        }

        try {
            String ids = builder.toString();
            ids = ids.substring(0, ids.length() - 1);

            String whereClause = COLUMN_ID +
                    " IN (" +
                    ids +
                    ")";

            db.delete(TABLE_NAME, whereClause, null);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "DeviceLogTable: Exception occurred while deleteDeviceLog: " + e);
        }
    }

    public static void deleteAllDeviceLogs(SQLiteDatabase db) {
        if (db == null) {
            return;
        }

        try {
            db.delete(TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "DeviceLogTable: Exception occurred while deleteAllDeviceLogs: " + e);
        }
    }

    public static List<DeviceLog> getDeviceLogs(SQLiteDatabase db) {
        if (db == null) {
            return null;
        }

        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_DEVICE_LOG}, null, null,
                null, null, null, String.valueOf(DEVICE_LOG_REQUEST_QUERY_LIMIT));
        if (cursor == null || cursor.isClosed()) {
            return null;
        }

        ArrayList<DeviceLog> deviceLogList = null;

        try {
            if (cursor.moveToFirst()) {
                deviceLogList = new ArrayList<>();

                do {
                    if (cursor.isClosed()) {
                        break;
                    }

                    String deviceLogString = cursor.getString(1);
                    if (!TextUtils.isEmpty(deviceLogString)) {
                        DeviceLog deviceLog = new DeviceLog(deviceLogString);

                        // Get RowId for DeviceLog
                        Integer rowId = Integer.valueOf(cursor.getString(0));
                        deviceLog.setId(rowId != null ? rowId : 0);

                        deviceLogList.add(deviceLog);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "DeviceLogTable: Exception occurred while getDeviceLogs: " + e);
        } finally {
            cursor.close();
        }

        return deviceLogList;
    }
}