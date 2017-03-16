package io.hypertrack.sendeta.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hypertrack.lib.internal.consumer.models.HTUser;
import com.hypertrack.lib.models.Place;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.store.OnboardingManager;
import io.hypertrack.sendeta.store.PlaceManager;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.util.SuccessErrorCallback;

/**
 * Created by ulhas on 15/06/16.
 */
public class OnboardingUser extends HTUser {

    public static OnboardingUser onboardingUser;
    private static String TAG = OnboardingManager.class.getSimpleName();

    @SerializedName("code")
    private String countryCode;

    private File photoImage;

    @SerializedName("photo")
    private byte[] photoData;

    private boolean isExistingUser;

    @SerializedName("places")
    private List<UserPlace> places;

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

    public List<UserPlace> getPlaces() {
        return places;
    }

    public void setPlaces(List<UserPlace> places) {
        this.places = places;
    }

    public void addPlace(UserPlace place) {
        if (this.places == null || this.places.size() == 0) {
            this.places = new ArrayList<UserPlace>();
        }
        this.places.add(place);
    }

    public void addPlace(final UserPlace placeToBeAdded, final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();

        addPlace(placeToBeAdded);
        if (callback != null) {
            callback.OnSuccess();
        }

      /*  placeManager.addPlace(placeToBeAdded, new PlaceManagerCallback() {
            @Override
            public void OnSuccess(UserPlace place) {
                addPlace(place);

                // Update PlaceID fetched from server
                placeToBeAdded.setId(place.getId());

                if (callback != null) {
                    callback.OnSuccess();
                }
            }

            @Override
            public void OnError() {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });*/
    }

    public void editPlace(UserPlace place, final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();
        editPlace(place);
       /* placeManager.editPlace(place, new PlaceManagerCallback() {
            @Override
            public void OnSuccess(UserPlace place) {
                editPlace(place);
                if (callback != null) {
                    callback.OnSuccess();
                }
            }

            @Override
            public void OnError() {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });*/
    }

    public void editPlace(final UserPlace place) {
        addPlace(place);

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
        update(this);
    }

    public boolean isSynced(Place place) {
        if (this.getPlaces().size() == 0) {
            return false;
        }

        for (Place candidate : this.getPlaces()) {
            if (candidate.getId() == place.getId()) {
                return true;
            }
        }

        return false;
    }

    public UserPlace getHome() {
        if (this.getPlaces().size() == 0) {
            return null;
        }

        UserPlace place = null;
        for (UserPlace candidate : this.getPlaces()) {
            if (candidate.isHome()) {
                place = candidate;
                break;
            }
        }

        return place;
    }

    public UserPlace getWork() {
        if (this.getPlaces().size() == 0) {
            return null;
        }

        UserPlace place = null;
        for (UserPlace candidate : this.getPlaces()) {
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


    public boolean isFavorite(Place place) {
        if (place == null)
            return false;
        if (this.getPlaces().size() == 0) {
            return false;
        }

        for (Place candidate : this.getPlaces()) {
            if (candidate.getId() == place.getId()
                    || (((candidate.getLocation().getLatitude()) == (place.getLocation().getLatitude()))
                    && ((candidate.getLocation().getLongitude()) == (place.getLocation().getLongitude())))) {
                return true;
            }
        }

        return false;
    }


}
