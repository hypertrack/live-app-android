package io.hypertrack.sendeta.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import io.hypertrack.sendeta.util.SharedPreferenceManager;

/**
 * Created by ulhas on 15/06/16.
 */
public class OnboardingUser extends User {

    private static OnboardingUser onboardingUser;

    @SerializedName("code")
    private String countryCode;

    private File photoImage;

    @SerializedName("photoData")
    private byte[] photoData;

    private OnboardingUser() {
    }

    public static OnboardingUser sharedOnboardingUser() {
        if (onboardingUser == null) {

            synchronized (OnboardingUser.class) {
                if (onboardingUser == null) {
                    onboardingUser = getOnboardingUser();
                }
            }
        }

        return onboardingUser;
    }

    private static OnboardingUser getOnboardingUser() {
        OnboardingUser onboardingUser = new OnboardingUser();

        if (SharedPreferenceManager.getOnboardingUser() != null) {
            onboardingUser = SharedPreferenceManager.getOnboardingUser();
        }

        return onboardingUser;
    }

    /**
     * IMPORTANT: Call this method on every update to onBoardingUser data to get the changes
     * reflected in the SharedPreferences for future reference.
     */
    public static void setOnboardingUser() {
        SharedPreferenceManager.setOnboardingUser(onboardingUser);
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

    public Bitmap getImageBitmap() {
        if (this.photoData == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(this.photoData, 0, this.photoData.length);
    }

    public byte[] getImageByteArray() {
        if (this.photoData == null) {
            return null;
        }
        return this.photoData;
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

    public void addImage(final File file) {
        saveFileAsBitmap(file);
//        update(this);
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }
}
