package io.hypertrack.sendeta.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.hypertrack.lib.common.util.DateTimeUtility;

/**
 * Created by piyush on 30/08/16.
 */
public class DateTypeAdapter implements JsonSerializer<Date>,
        JsonDeserializer<Date> {

    private final DateFormat mDateFormat;
    private final DateFormat mShortDateFormat;

    public DateTypeAdapter() {
        mDateFormat = new SimpleDateFormat(DateTimeUtility.HT_DATETIME_FORMAT, Locale.US);
        mDateFormat.setTimeZone(TimeZone.getTimeZone(DateTimeUtility.HT_TIMEZONE_UTC));

        mShortDateFormat = new SimpleDateFormat(DateTimeUtility.HT_DATETIME_SHORT_FORMAT, Locale.US);
        mShortDateFormat.setTimeZone(TimeZone.getTimeZone(DateTimeUtility.HT_TIMEZONE_UTC));
    }

    @Override
    public synchronized JsonElement serialize(Date date, Type type,
                                              JsonSerializationContext jsonSerializationContext) {
        synchronized (mDateFormat) {
            String dateFormatAsString = mDateFormat.format(date);
            return new JsonPrimitive(dateFormatAsString);
        }
    }

    @Override
    public synchronized Date deserialize(JsonElement jsonElement, Type type,
                                         JsonDeserializationContext jsonDeserializationContext) {
        try {
            synchronized (mDateFormat) {
                return mDateFormat.parse(jsonElement.getAsString());
            }
        } catch (ParseException e) {
            try {
                synchronized (mShortDateFormat) {
                    return mShortDateFormat.parse(jsonElement.getAsString());
                }
            } catch (ParseException exception) {
                throw new JsonSyntaxException(jsonElement.getAsString(), exception);
            }
        }
    }
}
