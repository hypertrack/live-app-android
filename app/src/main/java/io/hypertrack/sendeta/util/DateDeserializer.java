package io.hypertrack.sendeta.util;

import android.text.TextUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by piyush on 16/11/16.
 */
public class DateDeserializer implements JsonDeserializer<Date> {
    private static DateDeserializer dateDeserializer;

    public static DateDeserializer getInstance() {
        if (dateDeserializer == null) {
            dateDeserializer = new DateDeserializer();
        }

        return dateDeserializer;
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return json == null ? null : new Date(json.getAsLong());

        } catch (NumberFormatException e) {
            e.printStackTrace();

            return deserialize12HourDateFormatString(json.getAsString());
        }
    }

    // Fallback for ParseException while deserializing
    private Date deserialize12HourDateFormatString(String date) {
        try {
            if (!TextUtils.isEmpty(date)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
                return dateFormat.parse(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return deserialize24HourDateFormatString(date);
        }

        return null;
    }

    // Fallback for ParseException while deserializing
    private Date deserialize24HourDateFormatString(String date) {
        try {
            if (!TextUtils.isEmpty(date)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.ENGLISH);
                return dateFormat.parse(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
