
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
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
