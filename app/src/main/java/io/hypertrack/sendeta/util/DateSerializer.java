package io.hypertrack.sendeta.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by piyush on 16/11/16.
 */
public class DateSerializer implements JsonSerializer<Date> {
    private static DateSerializer dateSerializer;

    public static DateSerializer getInstance() {
        if (dateSerializer == null) {
            dateSerializer = new DateSerializer();
        }

        return dateSerializer;
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.getTime());
    }
}
