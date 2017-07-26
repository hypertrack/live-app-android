
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
