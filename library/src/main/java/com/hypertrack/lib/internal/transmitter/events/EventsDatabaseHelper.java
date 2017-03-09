package com.hypertrack.lib.internal.transmitter.events;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.transmitter.models.HyperTrackEvent;

import java.util.List;

/**
 * Created by piyush on 18/02/17.
 */
public class EventsDatabaseHelper extends SQLiteOpenHelper implements EventsDataSource {

    private static final String TAG = EventsDatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "com.hypertrack.events.db";
    private static final int DATABASE_VERSION = 1;

    private static EventsDatabaseHelper eventsDatabaseHelper;
    private SQLiteDatabase database;

    private EventsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.initializeDatabase();
    }

    public static EventsDatabaseHelper getInstance(Context context) {
        if (eventsDatabaseHelper == null) {
            synchronized (EventsDatabaseHelper.class) {
                if (eventsDatabaseHelper == null)
                    eventsDatabaseHelper = new EventsDatabaseHelper(context);
            }
        }
        return eventsDatabaseHelper;
    }

    private void initializeDatabase() {
        if (database == null)
            database = this.getWritableDatabase();
    }

    @Override
    public long getCount(String userID) {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        return EventsTable.getCount(database, userID);
    }

    @Override
    public void addEvent(HyperTrackEvent event) {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        EventsTable.addEvent(database, event);
    }

    @Override
    public void addEvents(List<HyperTrackEvent> eventList) {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        EventsTable.addEvents(database, eventList);
    }

    @Override
    public String getEventLastRecordedAt(String userID) {
        try {
            // Initialize SQLiteDatabase if null
            initializeDatabase();

            return EventsTable.getLastEventRecordedAt(database, userID);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while getEventsForUserID: " + e);
        }

        return null;
    }

    @Override
    public List<HyperTrackEvent> getEventsForUserID(String userID) {
        try {
            // Initialize SQLiteDatabase if null
            initializeDatabase();

            return getCount(userID) > 0 ? EventsTable.getEventsForUserID(database, userID) : null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while getEventsForUserID: " + e);
        }

        return null;
    }

    @Override
    public List<HyperTrackEvent> getEventsForUserIDBeforeTimestamp(String userID, @NonNull String timestamp) {
        try {
            // Initialize SQLiteDatabase if null
            initializeDatabase();

            return getCount(userID) > 0 ? EventsTable.getEventsForUserIDBeforeTimestamp(database, userID, timestamp) : null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            HTLog.e(TAG, "Exception occurred while getEventsForUserID: " + e);
        }

        return null;
    }

    @Override
    public void deleteEvents(List<HyperTrackEvent> eventList) {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        EventsTable.deleteEvents(database, eventList);
    }

    @Override
    public void deleteAllEvents() {
        // Initialize SQLiteDatabase if null
        initializeDatabase();

        EventsTable.deleteAllEvents(database);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        EventsTable.onCreate(db);
        HTLog.i(TAG, "EventsDatabaseHelper onCreate called.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        EventsTable.onUpgrade(db, oldVersion, newVersion);
        HTLog.i(TAG, "EventsDatabaseHelper onUpgrade called.");
    }
}
