package io.hypertrack.sendeta.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;

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
        return json == null ? null : new Date(json.getAsLong());
    }
}
