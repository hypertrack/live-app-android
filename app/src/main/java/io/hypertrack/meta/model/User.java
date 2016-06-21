package io.hypertrack.meta.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by suhas on 12/11/15.
 */
public class User extends RealmObject {

    private Integer id;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("phone_number")
    private String phoneNumber;

    private String photo;

    @SerializedName("hypertrack_driver_id")
    private String hypertrackDriverID;

    private RealmList<MetaPlace> places;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getHypertrackDriverID() {
        return hypertrackDriverID;
    }

    public void setHypertrackDriverID(String hypertrackDriverID) {
        this.hypertrackDriverID = hypertrackDriverID;
    }

    public RealmList<MetaPlace> getPlaces() {
        return places;
    }

    public void setPlaces(RealmList<MetaPlace> places) {
        this.places = places;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", photo='" + photo + '\'' +
                ", hypertrackDriverID='" + hypertrackDriverID + '\'' +
                '}';
    }
}
