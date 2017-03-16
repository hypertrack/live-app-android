package io.hypertrack.sendeta.store;

import android.content.Context;
import android.text.TextUtils;

import com.hypertrack.lib.HyperTrack;

import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.model.UserPlace;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by ulhas on 18/06/16.
 */
public class UserStore {

    public static UserStore sharedStore = new UserStore();
    private static String TAG = UserStore.class.getSimpleName();
    private User user;
    private Realm realm = Realm.getDefaultInstance();

    private UserStore() {
    }

    public void initializeUser() {
        if (this.user != null) {
            return;
        }

        this.user = realm.where(User.class).findFirst();
    }

   /* public void addUser(final User userToAdd) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (user != null) {
                    userToAdd.setPlaces(user.getPlaces());
                    userToAdd.setMemberships(user.getMemberships());
                }
                user = realm.copyToRealmOrUpdate(userToAdd);
            }
        });
    }

    private void updateUserData(final User updatedUser) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (user != null) {
                    List<Membership> membershipsToAdd = realm.copyToRealmOrUpdate(updatedUser.getMemberships());
                    RealmList<Membership> membershipsList = new RealmList<>(membershipsToAdd.toArray(new Membership[membershipsToAdd.size()]));
                    user.setMemberships(membershipsList);
                }

                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    private void updateInfo(final String firstName, final String lastName) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }
*/
   /* public User getUser() {
        return this.user;
    }*/

    public boolean isUserLoggedIn(Context context) {
        // Check if user has signed up
        if (!TextUtils.isEmpty(HyperTrack.getUserId())) {
            if (TextUtils.isEmpty(OnboardingManager.sharedManager().getUser().getId())) {
                OnboardingManager.sharedManager().getUser().setId(HyperTrack.getUserId());
            }
            return true;
        }


        initializeUser();
        //Check if old user exist with data
        if (user != null && user.isValid()) {
            OnboardingUser onboardingUser = OnboardingManager.sharedManager().getUser();
            onboardingUser.setId("");
            onboardingUser.setName(user.getFullName());
            onboardingUser.setPhone(user.getPhoneNumber());
            onboardingUser.setPhotoURL(user.getPhoto());
            onboardingUser.setPhotoData(user.getImageByteArray());
            setPlaces(user.getPlaces(), onboardingUser);
            OnboardingUser.setOnboardingUser();

            // Check if DriverId exists for current user
            String hyperTrackDriverID = SharedPreferenceManager.getHyperTrackDriverID(context);
            if (!TextUtils.isEmpty(hyperTrackDriverID)) {
                OnboardingManager.sharedManager().getUser().setId(hyperTrackDriverID);
                HyperTrack.setUserId(hyperTrackDriverID);
                deleteUser();
            }
            return true;
        }

        return false;
    }

    public void deleteUser() {
        if (user != null && user.isValid()) {
            realm.beginTransaction();
            user.deleteFromRealm();
            realm.commitTransaction();
        }
    }

    public void setPlaces(RealmList<MetaPlace> places, OnboardingUser onboardingUser) {
        if (places != null && places.size() > 0) {
            for (MetaPlace metaPlace : places) {
                UserPlace userPlace = new UserPlace(metaPlace.getName(), metaPlace.getLatLng());
                userPlace.setGooglePlacesID(metaPlace.getGooglePlacesID());
                userPlace.setAddress(metaPlace.getAddress());
                userPlace.setUserPlaceID(metaPlace.getId());
                onboardingUser.addPlace(userPlace);
            }
        }
    }

    /*public void addPlace(final UserPlace placeToBeAdded, final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();

        //addPlace(placeToBeAdded);
        if (callback != null) {
            callback.OnSuccess();
        }


  placeManager.addPlace(placeToBeAdded, new PlaceManagerCallback() {
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
        });
    }


 public void updatePlaces(final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();

        placeManager.getPlaces(new PlaceManagerGetPlacesCallback() {
            @Override
            public void OnSuccess(List<UserPlace> places) {


  clearPlaces();
                addPlaces(places);

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
        });
    }

    private void clearPlaces() {
        if (this.user == null) {
            return;
        }

        final RealmList<UserPlace> places = this.user.getPlaces();
        if (places == null || places.isEmpty()) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                places.deleteAllFromRealm();
                user.getPlaces().removeAll(places);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    public void addImage(final File file) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.saveFileAsBitmap(file);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    public void addBitmap(final Bitmap bitmap) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.saveImageBitmap(bitmap);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    private void addPlaces(final List<UserPlace> places) {
        if (this.user == null) {
            return;
        }

        if (places.isEmpty()) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<UserPlace> placesToAdd = realm.copyToRealmOrUpdate(places);
                RealmList<UserPlace> placesList = new RealmList<>(placesToAdd.toArray(new UserPlace[placesToAdd.size()]));
                user.setPlaces(placesList);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }


private void addPlace(final UserPlace place) {
        if (this.user == null || place == null) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                UserPlace managedPlace = realm.copyToRealmOrUpdate(place);
                user.getPlaces().add(managedPlace);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    public void editPlace(UserPlace place, final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();
        placeManager.editPlace(place, new PlaceManagerCallback() {
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
        });
    }


  public void editPlace(final UserPlace place) {
        if (this.user == null) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(place);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    public void deletePlace(final UserPlace place, final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();
        processDeletedMetaPlaceForAnalytics(true, null, place);
        deletePlace(place);
        if (callback != null) {
            callback.OnSuccess();
        }


  placeManager.deletePlace(place, new PlaceManagerCallback() {
            @Override
            public void OnSuccess(UserPlace place) {

                processDeletedMetaPlaceForAnalytics(true, null, place);

                deletePlace(place);
                if (callback != null) {
                    callback.OnSuccess();
                }
            }

            @Override
            public void OnError() {

                processDeletedMetaPlaceForAnalytics(false, ErrorMessages.DELETING_FAVORITE_PLACE_FAILED,
                        place);

                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }


*
     * Method to process deleted User Favorite Data to log Analytics Event
     *
     * @param status       Flag to indicate status of FavoritePlace Deletion event
     * @param errorMessage ErrorMessage in case of Failure
     * @param metaPlace    The Place object which is being deleted


    private void processDeletedMetaPlaceForAnalytics(boolean status, String errorMessage, UserPlace metaPlace) {

        try {
            // Check if UserPlace to be deleted is User's Home
            if (metaPlace.isHome()) {
                AnalyticsStore.getLogger().deletedHome(status, errorMessage);

                // Check if UserPlace to be deleted is User's Work
            } else if (metaPlace.isWork()) {
                AnalyticsStore.getLogger().deletedWork(status, errorMessage);

                // Check if UserPlace to be deleted is User's Other Favorite
            } else {
                AnalyticsStore.getLogger().deletedOtherFavorite(status, errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 private void deletePlace(final UserPlace place) {
        if (this.user == null) {
            return;
        }

        final UserPlace managedPlaceToDelete = realm.where(UserPlace.class).equalTo("id", place.getId()).findFirst();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                managedPlaceToDelete.deleteFromRealm();
                user.getPlaces().remove(place);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }


   public void updateInfo(final String updatedFirstName, final String updatedLastName, final SuccessErrorCallback callback) {
        if (this.user == null) {
            if (callback != null) {
                callback.OnError();
            }
            return;
        }

        // Existing User Info
        final String firstName = this.user.getFirstName();
        final String lastName = this.user.getLastName();

        // Updated User Info
        final HashMap<String, String> updatedUserDetails = new HashMap<>();
        updatedUserDetails.put("first_name", updatedFirstName);
        updatedUserDetails.put("last_name", updatedLastName);

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        Call<User> call = sendETAService.updateUserName(this.user.getId(), updatedUserDetails);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(final Call<User> call, Response<User> response) {
                User user = response.body();
                if (user == null) {
                    if (callback != null) {
                        callback.OnError();
                    }
                    return;
                }

                // Check if First Name was edited & Log Analytics Event
                if (!firstName.equals(updatedUserDetails.get("first_name")))
                    AnalyticsStore.getLogger().editedFirstName(true, null);

                // Check if Last Name was edited & Log Analytics Event
                if (!lastName.equals(updatedUserDetails.get("last_name")))
                    AnalyticsStore.getLogger().editedLastName(true, null);

                updateInfo(user.getFirstName(), user.getLastName());

                if (callback != null) {
                    callback.OnSuccess();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.OnError();
            }
        });
    }


    public void updatePhoto(final File updatePhoto, final SuccessErrorCallback callback) {
        if (updatePhoto == null) {
            callback.OnError();
            return;
        }

        //addImage(updatePhoto);

        if (callback != null) {
            callback.OnSuccess();
        }


RequestBody requestBody = RequestBody.create(MediaType.parse("image

"), updatePhoto);
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        String uuid = UUID.randomUUID().toString();
        String fileName = "photo\"; filename=\"" + uuid + ".jpg";
        requestBodyMap.put(fileName, requestBody);
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        Call<Map<String, Object>> call = sendETAService.updateUserProfilePic(this.user.getId(), requestBodyMap);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    addImage(updatePhoto);
                    if (callback != null) {
                        callback.OnSuccess();
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });

    }

    public int getSelectedMembershipAccountId() {
        return this.user.getSelectedMembershipAccountId();
    }

    public void updateSelectedMembership(final int selectedMembershipAccountId) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.setSelectedMembershipAccountId(selectedMembershipAccountId);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    public void getUserData(final UserStoreGetUserDataCallback callback) {
        if (user == null) {
            initializeUser();
        }

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<User> call = sendETAService.getUserData(this.user.getId());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {

                    updateUserData(response.body());

                    if (callback != null) {
                        callback.OnSuccess(response.body());
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void acceptMembership(Membership membership, final UserStoreMembershipCallback callback) {
        if (user == null) {
            initializeUser();
        }

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        MembershipDTO membershipDTO = new MembershipDTO(membership);

        Call<Membership> call = sendETAService.acceptMembership(this.user.getId(), membershipDTO);
        call.enqueue(new Callback<Membership>() {
            @Override
            public void onResponse(Call<Membership> call, Response<Membership> response) {
                if (response.isSuccessful()) {

                    editMembership(response.body());

                    if (callback != null) {
                        callback.OnSuccess(response.body());
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<Membership> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void rejectMembership(Membership membership, final UserStoreMembershipCallback callback) {
        if (user == null) {
            initializeUser();
        }

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        MembershipDTO membershipDTO = new MembershipDTO(membership);

        Call<Membership> call = sendETAService.rejectMembership(this.user.getId(), membershipDTO);
        call.enqueue(new Callback<Membership>() {
            @Override
            public void onResponse(Call<Membership> call, Response<Membership> response) {
                if (response.isSuccessful()) {

                    editMembership(response.body());

                    if (callback != null) {
                        callback.OnSuccess(response.body());
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<Membership> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    public void deleteMembership(final Membership membership, final UserStoreDeleteMembershipCallback callback) {
        if (user == null) {
            initializeUser();
        }

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());
        MembershipDTO membershipDTO = new MembershipDTO(membership);
        final String accountName = new String(membership.getAccountName());

        Call<ResponseBody> call = sendETAService.deleteMembership(this.user.getId(), membershipDTO);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    removeMembership(membership);

                    if (callback != null) {
                        callback.OnSuccess(new String(accountName));
                    }
                } else {
                    if (callback != null) {
                        callback.OnError();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (callback != null) {
                    callback.OnError();
                }
            }
        });
    }

    private void addMembership(final Membership membership) {
        if (this.user == null || membership == null) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Membership managedMembership = realm.copyToRealmOrUpdate(membership);
                user.getMemberships().add(managedMembership);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    private void editMembership(final Membership membership) {
        if (this.user == null || membership == null) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(membership);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    private void removeMembership(final Membership membership) {
        if (this.user == null || membership == null) {
            return;
        }

        final Membership managedMembershipToDelete = realm.where(Membership.class).equalTo("accountId", membership.getAccountId()).findFirst();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                managedMembershipToDelete.deleteFromRealm();
                user.getPlaces().remove(membership);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }*/
}
