package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.File;

import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.util.SharedPreferenceManager;

/**
 * Created by ulhas on 15/06/16.
 */
public class OnboardingUser {
    private static String TAG = OnboardingManager.class.getSimpleName();

    private Integer id;
    private String countryCode;
    private String contactNumber;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("phone_number")
    private String phoneNumber;

    private File photoImage;
    private String token;

    @SerializedName("photo")
    private String photoURL;

    private boolean isExistingUser;

    private OnboardingUser(){
    }

    public static OnboardingUser onboardingUser;

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
    public static void setOnboardingUser(){
        SharedPreferenceManager.setOnboardingUser(onboardingUser);
    }

    /**
     * Method to update OnboardingUser Data
     *
     * @param user  User Object containing the updated OnboardingUser Data
     */
    public void update(OnboardingUser user) {
        this.setId(user.getId());

        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());

        this.setPhoneNumber(user.getPhoneNumber());

        this.setPhotoImage(user.getPhotoImage());
        this.setToken(user.getToken());
        this.setPhotoURL(user.getPhotoURL());

        this.setOnboardingUser();

        // IMPORTANT: Do not update isExistingUser Flag while updating OnboardingUser
        // isExistingUser Flag is received while User registers his number (Login)
        // this.isExistingUser = this.isExistingUser;

        // IMPORTANT: Do not update CountryCode & ContactNumber Flag while updating OnboardingUser
        // These data are received during his registration
        // this.setCountryCode(user.getCountryCode());
        // this.setContactNumber(user.getContactNumber());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public File getPhotoImage() {
        return photoImage;
    }

    public void setPhotoImage(File photo) {
        this.photoImage = photo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public boolean isExistingUser() {
        return isExistingUser;
    }

    public void setExistingUser(boolean existingUser) {
        isExistingUser = existingUser;
    }

    public String getInternationalNumber() throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        Phonenumber.PhoneNumber number = phoneUtil.parse(getContactNumber(), getCountryCode());
        return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "OnboardingUser{" +
                "id=" + id +
                ", countryCode='" + countryCode + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photoImage=" + photoImage +
                ", token='" + token + '\'' +
                ", photoURL='" + photoURL + '\'' +
                '}';
    }
}
