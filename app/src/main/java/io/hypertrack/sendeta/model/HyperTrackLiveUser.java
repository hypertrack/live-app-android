
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

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.User;

import java.io.File;

import io.hypertrack.sendeta.store.SharedPreferenceManager;

/**
 * Created by ulhas on 15/06/16.
 */
public class HyperTrackLiveUser extends User {

    private static HyperTrackLiveUser hyperTrackLiveUser;

    @SerializedName("code")
    private String countryCode;

    private File photoImage;

    public static HyperTrackLiveUser sharedHyperTrackLiveUser(Context context) {
        if (hyperTrackLiveUser == null) {

            synchronized (HyperTrackLiveUser.class) {
                if (hyperTrackLiveUser == null) {
                    hyperTrackLiveUser = getHyperTrackLiveUser(context);
                }
            }
        }
        return hyperTrackLiveUser;
    }

    private static HyperTrackLiveUser getHyperTrackLiveUser(Context context) {
        HyperTrackLiveUser hyperTrackLiveUser = new HyperTrackLiveUser();

        if (SharedPreferenceManager.getHyperTrackLiveUser(context) != null) {
            hyperTrackLiveUser = SharedPreferenceManager.getHyperTrackLiveUser(context);
        }

        return hyperTrackLiveUser;
    }

    /**
     * IMPORTANT: Call this method on every update to setHyperTrackLiveUser data to get the changes
     * reflected in the SharedPreferences for future reference.
     */
    public static void setHyperTrackLiveUser(Context context) {
        SharedPreferenceManager.setHyperTrackLiveUser(context, hyperTrackLiveUser);
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }


    public File getPhotoImage() {
        return photoImage;
    }

    public void setPhotoImage(File photo) {
        this.photoImage = photo;
    }

    public String getInternationalNumber(String phoneNo) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if (HTTextUtils.isEmpty(phoneNo))
            return null;

        Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNo, getCountryCode());
        return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }
}
