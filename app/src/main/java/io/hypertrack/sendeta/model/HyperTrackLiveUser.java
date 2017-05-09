package io.hypertrack.sendeta.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hypertrack.lib.models.User;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.hypertrack.sendeta.store.SharedPreferenceManager;

/**
 * Created by ulhas on 15/06/16.
 */
public class HyperTrackLiveUser extends User {

    private static HyperTrackLiveUser hyperTrackLiveUser;

    @SerializedName("code")
    private String countryCode;

    private File photoImage;

    @SerializedName("photoData")
    private byte[] photoData;

    private HyperTrackLiveUser() {
    }

    public static HyperTrackLiveUser sharedHyperTrackLiveUser() {
        if (hyperTrackLiveUser == null) {

            synchronized (HyperTrackLiveUser.class) {
                if (hyperTrackLiveUser == null) {
                    hyperTrackLiveUser = getHyperTrackLiveUser();
                }
            }
        }

        return hyperTrackLiveUser;
    }

    private static HyperTrackLiveUser getHyperTrackLiveUser() {
        HyperTrackLiveUser hyperTrackLiveUser = new HyperTrackLiveUser();

        if (SharedPreferenceManager.getHyperTrackLiveUser() != null) {
            hyperTrackLiveUser = SharedPreferenceManager.getHyperTrackLiveUser();
        }

        return hyperTrackLiveUser;
    }

    /**
     * IMPORTANT: Call this method on every update to setHyperTrackLiveUser data to get the changes
     * reflected in the SharedPreferences for future reference.
     */
    public static void setHyperTrackLiveUser() {
        SharedPreferenceManager.setHyperTrackLiveUser(hyperTrackLiveUser);
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
        if (TextUtils.isEmpty(phoneNo))
            return null;

        Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNo, getCountryCode());
        return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    public void saveFileAsBitmap(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.photoData = bytes;
    }
}
