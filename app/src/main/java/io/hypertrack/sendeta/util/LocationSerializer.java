package io.hypertrack.sendeta.util;

import android.location.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by piyush on 17/08/16.
 */
public class LocationSerializer implements JsonSerializer<Location> {

    private static LocationSerializer locationSerializer;

    public static LocationSerializer getInstance() {
        if (locationSerializer == null) {
            locationSerializer = new LocationSerializer();
        }

        return locationSerializer;
    }

    private LocationSerializer() {
    }

    public JsonElement serialize(Location t, Type type,
                                 JsonSerializationContext jsc) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mProvider", t.getProvider());
        jsonObject.addProperty("mAccuracy", t.getAccuracy());
        jsonObject.addProperty("mLatitude", t.getLatitude());
        jsonObject.addProperty("mLongitude", t.getLongitude());
        jsonObject.addProperty("mTime", t.getTime());
        jsonObject.addProperty("mSpeed", t.getSpeed());
        jsonObject.addProperty("mBearing", t.getBearing());
        return jsonObject;
    }
}
