package io.hypertrack.meta.store;

import java.util.Map;
import java.util.Objects;

import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.model.PlaceDTO;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.retrofit.SendETAService;
import io.hypertrack.meta.network.retrofit.ServiceGenerator;
import io.hypertrack.meta.store.callback.UserStoreGetTaskCallback;
import io.hypertrack.meta.util.SharedPreferenceManager;
import io.realm.Realm;
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
    private SendETAService sendETAService = ServiceGenerator.createService(SendETAService.class, SharedPreferenceManager.getUserAuthToken());

    private UserStore() {
    }

    public void initializeUser() {
        this.user = realm.where(User.class).findFirst();
    }

    public void addUser(final User user) {
        this.user = user;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(user);
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
        PlaceDTO placeDTO = new PlaceDTO(place);

        Call<Map<String, Object>> call = sendETAService.createTask(this.user.getId(), placeDTO);
        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                Map<String, Object> responseDTO = response.body();
                callback.OnSuccess((String)responseDTO.get("id"));
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.OnError();
            }
        });
    }
}
