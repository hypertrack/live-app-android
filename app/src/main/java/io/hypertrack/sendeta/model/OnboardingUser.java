package io.hypertrack.sendeta.model;

import com.google.gson.annotations.SerializedName;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.File;

/**
 * Created by ulhas on 15/06/16.
 */
public class OnboardingUser {
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
