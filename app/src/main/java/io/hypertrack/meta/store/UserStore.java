package io.hypertrack.meta.store;

import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.callback.UserStoreGetTaskCallback;
import io.realm.Realm;

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

    }
}
