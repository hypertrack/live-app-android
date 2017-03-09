package com.hypertrack.lib.internal.common.logging;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.hypertrack.lib.BuildConfig;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;
import com.hypertrack.lib.internal.common.util.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by suhas on 04/02/16.
 */
public class HTLog {
    private static int logLevel = Log.WARN;
    private static Context mContext;
    private static DeviceLogList mDeviceLogList;

    public static void initHTLog(Context mContext, DeviceLogDataSource logDataSource) {
        initialize(mContext, logDataSource);
    }

    private static void initialize(Context mContext, DeviceLogDataSource logDataSource) {
        HTLog.mContext = mContext;

        if (mDeviceLogList == null) {
            synchronized (HTLog.class) {
                if (mDeviceLogList == null) {
                    mDeviceLogList = new DeviceLogList(logDataSource);
                }
            }
        }
    }

    /**
     * Sets the level of logging to display, where each level includes all those below it. The default
     * level is LOG_LEVEL_NONE. Please ensure this is set to Log#ERROR
     * or LOG_LEVEL_NONE before deploying your app to ensure no sensitive information is
     * logged. The levels are:
     * <ul>
     * <li>{@link Log#ASSERT}</li>
     * <li>{@link Log#VERBOSE}</li>
     * <li>{@link Log#DEBUG}</li>
     * <li>{@link Log#INFO}</li>
     * <li>{@link Log#WARN}</li>
     * <li>{@link Log#ERROR}</li>
     * </ul>
     *
     * @param logLevel The level of logcat logging that Parse should do.
     */
    public static void setLogLevel(int logLevel) {
        HTLog.logLevel = logLevel;
    }

    /**
     * Returns the level of logging that will be displayed.
     */
    public static int getLogLevel() {
        return logLevel;
    }

    private static void log(int messageLogLevel, String tag, String message, Throwable tr) {
        if (messageLogLevel >= logLevel) {
            if (tr == null) {
                Log.println(logLevel, tag, message);
            } else {
                Log.println(logLevel, tag, message + '\n' + Log.getStackTraceString(tr));
            }
        }
    }

    public static void v(String tag, String message, Throwable tr) {
        log(Log.VERBOSE, tag, message, tr);
    }

    public static void v(String tag, String message) {
        v(tag, message, null);
    }

    public static void d(String tag, String message, Throwable tr) {
        log(Log.DEBUG, tag, message, tr);
    }

    public static void d(String tag, String message) {
        d(tag, message, null);
    }

    public static void i(String tag, String message, Throwable tr) {
        log(Log.INFO, tag, message, tr);
        r(getFormattedMessage(Log.INFO, message));
    }

    public static void i(String tag, String message) {
        i(tag, message, null);
    }

    public static void w(String tag, String message, Throwable tr) {
        log(Log.WARN, tag, message, tr);
        r(getFormattedMessage(Log.WARN, message));
    }

    public static void w(String tag, String message) {
        w(tag, message, null);
    }

    public static void e(String tag, String message, Throwable tr) {
        log(Log.ERROR, tag, message, tr);
        r(getFormattedMessage(Log.ERROR, message));
    }

    public static void e(String tag, String message) {
        e(tag, message, null);
    }

    public static void a(String message) {
        r(getFormattedMessage(Log.ASSERT, message));
    }

    private static String getFormattedMessage(int logLevel, String message) {
        String prefix = getLogPrefix();
        prefix = prefix + getLogLevelName(logLevel);
        return getLogPrefix() + getLogLevelName(logLevel) + message;
    }

    public static String getLogPrefix() {

        String timeStamp = getTimeStamp();
        String senderName = BuildConfig.SDK_VERSION_NAME;
        String osVersion = "Android-" + Build.VERSION.RELEASE;

        String programName = "";
        String deviceUUID = "";

        if (mContext != null) {
            programName = HyperTrack.getPublishableKey(mContext);
            deviceUUID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        if (deviceUUID == null) {
            deviceUUID = "DeviceUUID";
        }

        if (TextUtils.isEmpty(programName)) {
            programName = "NONE";
        }

        return timeStamp + " " + senderName + " " + programName + ": " + osVersion + " | " + deviceUUID + " | ";
    }

    public static String getTimeStamp() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DateTimeUtility.HT_DATETIME_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone(DateTimeUtility.HT_TIMEZONE_UTC));
        return sdf.format(c.getTime());
    }

    private static void r(final String message) {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mContext == null) {
                            return;
                        }

                        if (mDeviceLogList == null) {
                            initialize(mContext, DeviceLogDatabaseHelper.getInstance(mContext));
                        }

                        mDeviceLogList.addDeviceLog(message);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }

    private static String getLogLevelName(int messageLogLevel) {

        String logLevelName;
        switch (messageLogLevel) {
            case Log.VERBOSE:
                logLevelName = "VERBOSE";
                break;
            case Log.DEBUG:
                logLevelName = "DEBUG";
                break;
            case Log.INFO:
                logLevelName = "INFO";
                break;
            case Log.WARN:
                logLevelName = "WARN";
                break;
            case Log.ERROR:
                logLevelName = "ERROR";
                break;
            case Log.ASSERT:
                logLevelName = "ASSERT";
                break;
            default:
                logLevelName = "NONE";
        }

        return "[" + logLevelName + "]" + ": ";
    }
}