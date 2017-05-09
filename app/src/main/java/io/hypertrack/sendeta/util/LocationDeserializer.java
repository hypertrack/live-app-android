package io.hypertrack.sendeta.util;

import android.location.Location;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

/**
 * Created by piyush on 17/08/16.
 */
public class LocationDeserializer implements JsonDeserializer<Location> {

    private static LocationDeserializer locationDeserializer;

    public static LocationDeserializer getInstance() {
        if (locationDeserializer == null) {
            locationDeserializer = new LocationDeserializer();
        }

        return locationDeserializer;
    }

    public Location deserialize(JsonElement je, Type type,
                                JsonDeserializationContext jdc) {
        JsonObject jsonObject = je.getAsJsonObject();
        Location location = new Location(jsonObject.getAsJsonPrimitive("mProvider").getAsString());
        location.setAccuracy(jsonObject.getAsJsonPrimitive("mAccuracy").getAsFloat());
        location.setLatitude(jsonObject.getAsJsonPrimitive("mLatitude").getAsDouble());
        location.setLongitude(jsonObject.getAsJsonPrimitive("mLongitude").getAsDouble());
        location.setTime(jsonObject.getAsJsonPrimitive("mTime").getAsLong());
        location.setSpeed(jsonObject.getAsJsonPrimitive("mSpeed").getAsFloat());
        location.setBearing(jsonObject.getAsJsonPrimitive("mBearing").getAsFloat());
        return location;
    }
}
