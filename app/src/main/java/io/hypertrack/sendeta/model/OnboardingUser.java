package io.hypertrack.sendeta.model;

import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hypertrack.lib.internal.consumer.models.HTUser;

import java.io.File;

import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.util.SharedPreferenceManager;

/**
 * Created by ulhas on 15/06/16.
 */
public class OnboardingUser extends HTUser {
    public static OnboardingUser onboardingUser;
    private static String TAG = OnboardingManager.class.getSimpleName();
    private String countryCode;
    private File photoImage;
    private String actionID;
    private boolean isExistingUser;

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

    public static OnboardingUser getOnboardingUser() {
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

    public String getActionID() {
        return actionID;
    }

    public void setActionID(String actionID) {
        this.actionID = actionID;
    }

    /**
     * Method to update OnboardingUser Data
     *
     * @param user User Object containing the updated OnboardingUser Data
     */
    public void update(OnboardingUser user) {
        this.setId(user.getId());

        setName(user.getName());

        setPhone(user.getPhone());

        this.setPhotoImage(user.getPhotoImage());
        this.setPhotoURL(user.getPhotoURL());

        setOnboardingUser();

        // IMPORTANT: Do not update isExistingUser Flag while updating OnboardingUser
        // isExistingUser Flag is received while User registers his number (Login)
        // this.isExistingUser = this.isExistingUser;

        // IMPORTANT: Do not update CountryCode & ContactNumber Flag while updating OnboardingUser
        // These data are received during his registration
        // this.setCountryCode(user.getCountryCode());
        // this.setContactNumber(user.getContactNumber());
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

    public boolean isExistingUser() {
        return isExistingUser;
    }

    public void setExistingUser(boolean existingUser) {
        isExistingUser = existingUser;
    }

    public String getInternationalNumber() throws NumberParseException {
        if (TextUtils.isEmpty(getPhone()))
            return null;

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber number = phoneUtil.parse(getPhone(), getCountryCode());
        return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }


}
