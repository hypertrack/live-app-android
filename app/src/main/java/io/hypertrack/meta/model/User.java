package io.hypertrack.meta.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by suhas on 12/11/15.
 */
public class User extends RealmObject {

    @PrimaryKey
    private Integer id;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("phone_number")
    private String phoneNumber;

    private String photo;

    private byte[] photoData;

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

    public MetaPlace getHome() {
        if (this.places.size() == 0) {
            return null;
        }

        MetaPlace place = null;
        for (MetaPlace candidate: this.places) {
            if (candidate.isHome()) {
                place = candidate;
                break;
            }
        }

        return place;
    }

    public MetaPlace getWork() {
        if (this.places.size() == 0) {
            return null;
        }

        MetaPlace place = null;
        for (MetaPlace candidate: this.places) {
            if (candidate.isWork()) {
                place = candidate;
                break;
            }
        }

        return place;
    }

    public boolean hasHome() {
        return this.getHome() != null;
    }

    public boolean hasWork() {
        return this.getWork() != null;
    }

    public List<MetaPlace> getOtherPlaces() {
        List<MetaPlace> otherPlaces = new ArrayList<>();

        for (MetaPlace candidate : this.places) {
            if (candidate.isWork() || candidate.isHome()) {
                continue;
            }

            otherPlaces.add(candidate);
        }

        return otherPlaces;
    }

    public boolean hasPlace(MetaPlace place) {
        if (this.places.size() == 0) {
            return false;
        }

        for (MetaPlace candidate : this.places) {
            if (candidate.getName().equalsIgnoreCase(place.getName())) {
                return true;
            }
        }

        return false;
    }

    public boolean isSynced(MetaPlace place) {
        if (this.places.size() == 0) {
            return false;
        }

        for (MetaPlace candidate : this.places) {
            if (candidate.getId() == place.getId()) {
                return true;
            }
        }

        return false;
    }

    public boolean isFavorite(MetaPlace place) {
        if (this.places.size() == 0) {
            return false;
        }

        for (MetaPlace candidate : this.places) {
            if (candidate.getId() == place.getId()
                    || candidate.getGooglePlacesID().equalsIgnoreCase(place.getGooglePlacesID())
                    || candidate.getHyperTrackDestinationID().equalsIgnoreCase(place.getHyperTrackDestinationID())) {
                return true;
            }
        }

        return false;
    }

    public String getFullName() {
        String fullName = "";
        if (this.firstName != null && !this.firstName.isEmpty()) {
            fullName = fullName + this.firstName;
        }

        if (this.lastName != null && !this.lastName.isEmpty()) {
            if (fullName.length() > 0) {
                fullName = fullName + " ";
            }

            fullName = fullName + this.lastName;
        }

        return fullName;
    }

    public void saveImageBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        this.photoData = byteArrayOutputStream .toByteArray();
    }

    public Bitmap getImageBitmap() {
        if (this.photoData == null) {
            return null;
        }

        return BitmapFactory.decodeByteArray(this.photoData, 0, this.photoData.length);
    }
}
