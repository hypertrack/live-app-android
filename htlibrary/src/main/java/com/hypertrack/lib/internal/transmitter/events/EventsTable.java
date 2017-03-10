package com.hypertrack.lib.internal.transmitter.events;

/**
 * Created by piyush on 18/02/17.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** package */ class EventsTable {

    private static final String TAG = EventsTable.class.getSimpleName();
    private static final int EVENTS_REQUEST_QUERY_LIMIT = 50;

    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_RECORDED_AT = "recorded_at";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EVENTS = "events";
    private static final String COLUMN_EVENT_TYPE = "event_type";

    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_EVENTS
            + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_RECORDED_AT + " TEXT,"
            + COLUMN_USER_ID + " TEXT, "
            + COLUMN_EVENT_TYPE + " TEXT, "
            + COLUMN_EVENTS + " TEXT NOT NULL"
            + ");";

    private static final String BULK_INSERT_EVENTS_WITH_USER_ID = "INSERT INTO "
            + TABLE_EVENTS
            + " (" + COLUMN_USER_ID + ", " + COLUMN_RECORDED_AT + ", " + COLUMN_EVENT_TYPE + ", " + COLUMN_EVENTS + ")"
            + " VALUES (?,?,?);";

    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<HyperTrackEvent>() {}.getType();

    static void onCreate(SQLiteDatabase db) {
        if (db == null) {
            return;
        }

        try {
            db.execSQL(DATABASE_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "LocationTable: Exception occurred while onCreate: " + e);
        }
    }

    static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (db == null) {
            return;
        }

        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "LocationTable: Exception occurred while onUpgrade: " + e);
        }
    }

    static long getCount(SQLiteDatabase db, @NonNull String userID) {
        try {
            if (db == null) {
                return 0;
            }

            return DatabaseUtils.queryNumEntries(db, TABLE_EVENTS, COLUMN_USER_ID + "=?", new String[]{userID});
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "LocationTable: Exception occurred while getCount: " + e);
            return 0L;
        }
    }

    static void addEvent(SQLiteDatabase db, HyperTrackEvent event) {
        if (db == null || event == null || TextUtils.isEmpty(event.getUserID())) {
            return;
        }
        ContentValues contentValues = new ContentValues();

        String eventsJSON = gson.toJson(event);

        contentValues.put(COLUMN_EVENTS, eventsJSON);
        contentValues.put(COLUMN_RECORDED_AT, event.getRecordedAt() != null ? event.getRecordedAt() :
                DateTimeUtility.getCurrentTime());
        contentValues.put(COLUMN_USER_ID, event.getUserID());
        contentValues.put(COLUMN_EVENT_TYPE, event.getEventType());

        try {
            db.insert(TABLE_EVENTS, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "EventsTable: Exception occurred while addEvent: " + e.getMessage());
        }
    }

    static void addEvents(SQLiteDatabase db, List<HyperTrackEvent> events) {
        if (db == null || events == null || events.isEmpty()) {
            return;
        }

        SQLiteStatement statement = db.compileStatement(BULK_INSERT_EVENTS_WITH_USER_ID);

        try {
            db.beginTransaction();
            for (HyperTrackEvent event : events) {
                if (TextUtils.isEmpty(event.getUserID()))
                    continue;

                String eventsJSON = gson.toJson(event);

                statement.clearBindings();
                statement.bindString(1, (event.getUserID() != null ? event.getUserID() : ""));
                statement.bindString(2, (event.getRecordedAt() != null ? event.getRecordedAt() : DateTimeUtility.getCurrentTime()));
                statement.bindString(3, event.getEventType());
                statement.bindString(4, eventsJSON);
                statement.execute();
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "EventsTable: Exception occurred while addEvents: " + e.getMessage());
        }
    }

    static String getLastEventRecordedAt(SQLiteDatabase db, String userID) {
        if (db == null || userID == null) {
            return null;
        }

        Cursor cursor = db.query(TABLE_EVENTS, new String[]{COLUMN_RECORDED_AT}, COLUMN_USER_ID + "=?",
                new String[]{userID}, null, null, COLUMN_RECORDED_AT + " DESC", "1");

        if (cursor == null || cursor.isClosed())
            return null;

        String timestamp = null;

        try {
            if (cursor.moveToFirst()) {
                if (!cursor.isClosed()) {
                    timestamp = cursor.getString(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "EventsTable: Exception occurred while getLastEventRecordedAt: " + e);
        } finally {
            cursor.close();
        }

        return timestamp;
    }

    static List<HyperTrackEvent> getEventsForUserID(SQLiteDatabase db, String userID) {
        if (db == null || userID == null) {
            return null;
        }

        Cursor cursor = db.query(TABLE_EVENTS, new String[]{COLUMN_ID, COLUMN_EVENTS},
                COLUMN_USER_ID + "=?", new String[]{userID}, null, null, COLUMN_RECORDED_AT,
                String.valueOf(EVENTS_REQUEST_QUERY_LIMIT));

        if (cursor == null || cursor.isClosed()) {
            return null;
        }

        ArrayList<HyperTrackEvent> events = null;

        try {
            if (cursor.moveToFirst()) {
                events = new ArrayList<>();

                do {
                    if (cursor.isClosed()) {
                        break;
                    }

                    HyperTrackEvent event = gson.fromJson(cursor.getString(1), type);
                    // Set RowId for Event record
                    Integer rowId = Integer.valueOf(cursor.getString(0));
                    event.setId(rowId != null ? rowId : -1);
                    events.add(event);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "EventsTable: Exception occurred while getEventsForUserID: " + e.getMessage());
        } finally {
            cursor.close();
        }

        return events;
    }

    static List<HyperTrackEvent> getEventsForUserIDBeforeTimestamp(SQLiteDatabase db, String userID, String timestamp) {
        if (db == null || userID == null) {
            return null;
        }

        Cursor cursor = db.query(TABLE_EVENTS, new String[]{COLUMN_ID, COLUMN_EVENTS},
                COLUMN_USER_ID + "=? AND " + COLUMN_RECORDED_AT + " <= ?", new String[]{userID, timestamp},
                null, null, COLUMN_RECORDED_AT, String.valueOf(EVENTS_REQUEST_QUERY_LIMIT));

        if (cursor == null || cursor.isClosed()) {
            return null;
        }

        ArrayList<HyperTrackEvent> events = null;

        try {
            if (cursor.moveToFirst()) {
                events = new ArrayList<>();

                do {
                    if (cursor.isClosed()) {
                        break;
                    }

                    HyperTrackEvent event = gson.fromJson(cursor.getString(1), type);
                    // Set RowId for Event record
                    Integer rowId = Integer.valueOf(cursor.getString(0));
                    event.setId(rowId != null ? rowId : -1);
                    events.add(event);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "EventsTable: Exception occurred while getEventsForUserID: " + e.getMessage());
        } finally {
            cursor.close();
        }

        return events;
    }

    static void deleteEvents(SQLiteDatabase db, List<HyperTrackEvent> eventsList) {
        if (db == null)
            return;

        StringBuilder builder = new StringBuilder();
        for (HyperTrackEvent event : eventsList) {
            if (event != null && event.getId() > 0) {
                builder.append(event.getId())
                        .append(",");
            }
        }

        if (builder.length() == 0) {
            return;
        }

        try {
            String ids = builder.toString();
            ids = ids.substring(0, ids.length() - 1);

            String whereClause = COLUMN_ID + " IN (" + ids + ")";
            db.delete(TABLE_EVENTS, whereClause, null);

        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "EventsTable: Exception occurred while deleteEvents: " + e.getMessage());
        }
    }

    static void deleteAllEvents(SQLiteDatabase db) {
        if (db == null) {
            return;
        }

        try {
            db.delete(TABLE_EVENTS, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            HTLog.e(TAG, "EventsTable: Exception occurred while deleteAllEvents: " + e.getMessage());
        }
    }
}

