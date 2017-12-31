
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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.hypertrack.lib.internal.common.util.HTTextUtils;

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
            CrashlyticsWrapper.log(e);
            e.printStackTrace();

            return deserialize12HourDateFormatString(json.getAsString());
        }
    }

    // Fallback for ParseException while deserializing
    private Date deserialize12HourDateFormatString(String date) {
        try {
            if (!HTTextUtils.isEmpty(date)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH);
                return dateFormat.parse(date);
            }
        } catch (Exception e) {
            CrashlyticsWrapper.log(e);
            e.printStackTrace();
            return deserialize24HourDateFormatString(date);
        }

        return null;
    }

    // Fallback for ParseException while deserializing
    private Date deserialize24HourDateFormatString(String date) {
        try {
            if (!HTTextUtils.isEmpty(date)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.ENGLISH);
                return dateFormat.parse(date);
            }
        } catch (Exception e) {
            CrashlyticsWrapper.log(e);
            e.printStackTrace();
        }

        return null;
    }
}