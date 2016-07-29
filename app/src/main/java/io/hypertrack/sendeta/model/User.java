package io.hypertrack.sendeta.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    private RealmList<MetaPlace> places;

    @SerializedName("memberships")
    private RealmList<Membership> memberships;

    @Expose(serialize = false, deserialize = false)
    private int selectedMembershipAccountId;

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

    public RealmList<MetaPlace> getPlaces() {
        return places;
    }

    public void setPlaces(RealmList<MetaPlace> places) {
        this.places = places;
    }

    public RealmList<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(RealmList<Membership> memberships) {
        this.memberships = memberships;
    }

    public int getSelectedMembershipAccountId() {
        return selectedMembershipAccountId;
    }

    public void setSelectedMembershipAccountId(int selectedMembershipAccountId) {
        Membership membership = getMembershipForAccountId(selectedMembershipAccountId);
        if (membership != null && membership.isAccepted())
            this.selectedMembershipAccountId = selectedMembershipAccountId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", photo='" + photo + '\'' +
                ", selectedMembershipAccountId='" + selectedMembershipAccountId + '\'' +
                ", memberships='" + memberships.toString() + '\'' +
                '}';
    }

    public MetaPlace getHome() {
        if (this.getPlaces().size() == 0) {
            return null;
        }

        MetaPlace place = null;
        for (MetaPlace candidate: this.getPlaces()) {
            if (candidate.isHome()) {
                place = candidate;
                break;
            }
        }

        return place;
    }

    public MetaPlace getWork() {
        if (this.getPlaces().size() == 0) {
            return null;
        }

        MetaPlace place = null;
        for (MetaPlace candidate: this.getPlaces()) {
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

        for (MetaPlace candidate : this.getPlaces()) {
            if (candidate.isWork() || candidate.isHome()) {
                continue;
            }

            otherPlaces.add(candidate);
        }

        return otherPlaces;
    }

    public boolean hasPlace(MetaPlace place) {
        if (this.getPlaces().size() == 0) {
            return false;
        }

        for (MetaPlace candidate : this.getPlaces()) {
            if (candidate.getName().equalsIgnoreCase(place.getName())) {
                return true;
            }
        }

        return false;
    }

    public boolean isSynced(MetaPlace place) {
        if (this.getPlaces().size() == 0) {
            return false;
        }

        for (MetaPlace candidate : this.getPlaces()) {
            if (candidate.getId() == place.getId()) {
                return true;
            }
        }

        return false;
    }

    public boolean isFavorite(MetaPlace place) {
        if (this.getPlaces().size() == 0) {
            return false;
        }

        for (MetaPlace candidate : this.getPlaces()) {
            if (candidate.getId() == place.getId()
                    || candidate.getGooglePlacesID().equalsIgnoreCase(place.getGooglePlacesID())
                    || (candidate.getLatitude().equals(place.getLatitude()) && candidate.getLongitude().equals(place.getLongitude()))) {
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

    public Membership getMembershipForAccountId(int accountId) {
        if (this.getMemberships() == null || this.getMemberships().size() == 0) {
            return null;
        }

        Membership membership = null;
        for (Membership candidate: this.getMemberships()) {
            if (candidate.getAccountId() == accountId) {
                membership = candidate;
                break;
            }
        }

        return membership;
    }

    public List<Membership> getActiveMemberships() {
        List<Membership> activeMemberships = new ArrayList<>();

        for (Membership candidate: this.getMemberships()) {
            if (candidate.isAccepted() || (!candidate.isAccepted() && !candidate.isRejected())){
                activeMemberships.add(candidate);
            }
        }

        return activeMemberships;
    }

    public List<Membership> getActiveBusinessMemberships() {
        List<Membership> activeMemberships = new ArrayList<>();

        for (Membership candidate: this.getMemberships()) {
            if (!candidate.isPersonal() && candidate.isAccepted()
                    || (!candidate.isAccepted() && !candidate.isRejected())){
                activeMemberships.add(candidate);
            }
        }

        return activeMemberships;
    }

    public List<Membership> getPendingMemberships() {
        if (this.getMemberships() == null || this.getMemberships().size() == 0) {
            return null;
        }

        List<Membership> membershipsList = new ArrayList<>();

        for (Membership candidate: this.getMemberships()) {
            if (!candidate.isAccepted() && !candidate.isRejected()) {
                membershipsList.add(candidate);
            }
        }

        return membershipsList;
    }

    public List<Membership> getAcceptedMemberships() {
        if (this.getMemberships() == null || this.getMemberships().size() == 0) {
            return null;
        }

        Membership personalMembership = null;
        List<Membership> acceptedBusinessMemberships = new ArrayList<>();
        List<Membership> acceptedMemberships = new ArrayList<>();

        for (Membership candidate : this.getMemberships()) {
            if (candidate.isPersonal()) {
                personalMembership = candidate;
            }

            if (candidate.isAccepted() && !candidate.isPersonal()) {
                acceptedBusinessMemberships.add(candidate);
            }
        }

        if (personalMembership != null)
            acceptedMemberships.add(personalMembership);

        if (acceptedBusinessMemberships != null && acceptedBusinessMemberships.size() > 0)
            acceptedMemberships.addAll(acceptedBusinessMemberships);

        return acceptedMemberships;
    }

    public boolean isAcceptedMembership(int accountId) {
        if (this.getMemberships() == null || this.getMemberships().size() == 0)
            return false;

        for (Membership candidate : this.getMemberships()) {
            if (candidate.getAccountId() == accountId && candidate.isAccepted()) {
                return true;
            }
        }

        return false;
    }
}
