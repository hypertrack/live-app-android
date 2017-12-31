
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

package io.hypertrack.sendeta.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Locale;

import io.hypertrack.sendeta.R;

/**
 * Created by suhas on 19/01/16.
 */
public class CountryMaster {
    private Context mContext;
    private ArrayList<Country> mCountries = new ArrayList<>();

    public CountryMaster(Context context) {
        mContext = context;
        Resources res = mContext.getResources();

        // builds country data from json
        InputStream is = res.openRawResource(R.raw.countries);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String jsonString = writer.toString();
        JSONArray json = new JSONArray();
        try {
            json = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < json.length(); i++) {
            JSONObject node = json.optJSONObject(i);
            if (node != null) {
                Country country = new Country();
                country.mCountryIso = node.optString("iso");
                country.mDialPrefix = node.optString("tel");
                country.mCountryName = getCountryName(node.optString("iso"));
                country.mImageId = getCountryFlagImageResource(node.optString("iso"));

                mCountries.add(country);
            }
        }
    }

    /**
     * Returns all {@link Country} objects as an ArrayList<Country>
     *
     * @return mCountries {@link CountryMaster#mCountries}
     */
    public ArrayList<Country> getCountries() {
        return mCountries;
    }

    /**
     * Fetches the image resource id from drawables
     *
     * @param isoCode		String
     * @return imageResId	Integer
     */
    @SuppressLint("DefaultLocale")
    private int getCountryFlagImageResource(String isoCode) {
        String imageName = isoCode.trim().toLowerCase();
        return mContext.getResources().getIdentifier("drawable/" + imageName, null, mContext.getPackageName());
    }

    /**
     * Returns the full name of the country
     *
     * @param isoCode	String
     * @return 			String
     */
    private String getCountryName(String isoCode) {
        Locale locale = new Locale(Locale.getDefault().getDisplayLanguage(), isoCode);
        return locale.getDisplayCountry().trim();
    }
}
