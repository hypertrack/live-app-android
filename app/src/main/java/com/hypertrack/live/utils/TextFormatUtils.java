package com.hypertrack.live.utils;

import android.content.Context;
import android.text.format.DateUtils;

import java.util.concurrent.TimeUnit;

public class TextFormatUtils {

    public static String getRelativeDateTimeString(Context context, long time) {
        return DateUtils.getRelativeDateTimeString(context, time,
                TimeUnit.SECONDS.toMillis(1), TimeUnit.DAYS.toMillis(7),
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY |
                        DateUtils.FORMAT_ABBREV_ALL).toString();
    }
}