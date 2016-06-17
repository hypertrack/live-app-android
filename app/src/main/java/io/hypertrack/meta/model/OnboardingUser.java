package io.hypertrack.meta.model;

import android.media.Image;

/**
 * Created by ulhas on 15/06/16.
 */
public class OnboardingUser {
    private Integer id;
    private String countryCode;
    private String phoneNumber;
    private Image photo;
    private String token;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Image getPhoto() {
        return photo;
    }

    public void setPhoto(Image photo) {
        this.photo = photo;
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

    public String combinedPhoneNumber() {
        String combinedPhoneNumber = "";
        if (this.countryCode != null) {
            combinedPhoneNumber = combinedPhoneNumber + this.countryCode;
        }

        if (this.phoneNumber != null) {
            combinedPhoneNumber = combinedPhoneNumber + this.phoneNumber;
        }

        return combinedPhoneNumber;
    }
}
