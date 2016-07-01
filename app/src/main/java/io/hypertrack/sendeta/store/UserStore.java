package io.hypertrack.sendeta.store;

import android.graphics.Bitmap;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.PlaceDTO;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.network.retrofit.SendETAService;
import io.hypertrack.sendeta.network.retrofit.ServiceGenerator;
import io.hypertrack.sendeta.store.callback.PlaceManagerCallback;
import io.hypertrack.sendeta.store.callback.PlaceManagerGetPlacesCallback;
import io.hypertrack.sendeta.store.callback.UserStoreGetTaskCallback;
import io.hypertrack.sendeta.util.SharedPreferenceManager;
import io.hypertrack.sendeta.util.SuccessErrorCallback;
import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ulhas on 18/06/16.
 */
public class UserStore {

    private static String TAG = UserStore.class.getSimpleName();
    public static UserStore sharedStore = new UserStore();

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

    public void addUser(final User userToAdd) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (user != null) {
                    userToAdd.setPlaces(user.getPlaces());
                }
                user = realm.copyToRealmOrUpdate(userToAdd);
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

    public User getUser() {
        return this.user;
    }

    public static boolean isUserLoggedIn() {
        Realm realm = Realm.getDefaultInstance();
        return realm.where(User.class).findAll().size() > 0;
    }

    public void getTask(MetaPlace place, final UserStoreGetTaskCallback callback) {
        if (this.user == null || place == null) {
            if (callback != null) {
                callback.OnError();
            }

            return;
        }

        if (place.hasDestination()) {
            this.getTaskForDestination(place.getHyperTrackDestinationID(), callback);
        } else {
            this.getTaskForPlace(place, callback);
        }
    }

    private void getTaskForDestination(String destinationID, final UserStoreGetTaskCallback callback) {
        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<Map<String, Object>> call = sendETAService.createTask(this.user.getId(), destinationID);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {

                if (response.isSuccessful()) {
                    Map<String, Object> responseDTO = response.body();

                    if (callback != null) {
                        if (responseDTO != null) {
                            callback.OnSuccess((String) responseDTO.get("id"));
                        } else {
                            callback.OnError();
                        }
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

    private void getTaskForPlace(MetaPlace place, final UserStoreGetTaskCallback callback) {
        PlaceDTO placeDTO = new PlaceDTO(place);

        SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

        Call<Map<String, Object>> call = sendETAService.createTask(this.user.getId(), placeDTO);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {

                if (response.isSuccessful()) {
                    Map<String, Object> responseDTO = response.body();

                    if (callback != null) {
                        if (responseDTO != null) {
                            callback.OnSuccess((String) responseDTO.get("id"));
                        } else {
                            callback.OnError();
                        }
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

    public void addPlace(MetaPlace place, final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();
        placeManager.addPlace(place, new PlaceManagerCallback() {
            @Override
            public void OnSuccess(MetaPlace place) {
                addPlace(place);
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
            public void OnSuccess(List<MetaPlace> places) {
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

        final RealmList<MetaPlace> places = this.user.getPlaces();
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

    private void addPlaces(final List<MetaPlace> places) {
        if (this.user == null) {
            return;
        }

        if (places.isEmpty()) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<MetaPlace> placesToAdd = realm.copyToRealm(places);
                RealmList<MetaPlace> placesList = new RealmList<>(placesToAdd.toArray(new MetaPlace[placesToAdd.size()]));
                user.setPlaces(placesList);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    private void addPlace(final MetaPlace place) {
        if (this.user == null) {
            return;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                MetaPlace managedPlace = realm.copyToRealm(place);
                user.getPlaces().add(managedPlace);
                user = realm.copyToRealmOrUpdate(user);
            }
        });
    }

    public void editPlace(MetaPlace place, final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();
        placeManager.editPlace(place, new PlaceManagerCallback() {
            @Override
            public void OnSuccess(MetaPlace place) {
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

    private void editPlace(final MetaPlace place) {
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

    public void deletePlace(final MetaPlace place, final SuccessErrorCallback callback) {
        PlaceManager placeManager = new PlaceManager();
        placeManager.deletePlace(place, new PlaceManagerCallback() {
            @Override
            public void OnSuccess(MetaPlace place) {
                deletePlace(place);
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

    private void deletePlace(final MetaPlace place) {
        if (this.user == null) {
            return;
        }

        final MetaPlace managedPlaceToDelete = realm.where(MetaPlace.class).equalTo("id", place.getId()).findFirst();

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
                if (firstName.equals(updatedUserDetails.get("first_name")))
                    AnalyticsStore.getLogger().editedFirstName(true, null);

                // Check if Last Name was edited & Log Analytics Event
                if (lastName.equals(updatedUserDetails.get("last_name")))
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

        RequestBody requestBody = RequestBody.create(MediaType.parse("image*//*"), updatePhoto);

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
}
