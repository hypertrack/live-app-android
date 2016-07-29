package io.hypertrack.sendeta.store;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by piyush on 27/07/16.
 */
public class RealmStore {

    private Realm realm;
    private RealmConfiguration config;

    private static RealmStore sSharedStore;

    private RealmStore(){
    }

    public static RealmStore getShareStore() {
        if (sSharedStore == null) {
            sSharedStore = new RealmStore();
        }

        return sSharedStore;
    }

    public Realm getRealm() {
        return realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }

    public RealmConfiguration getConfig() {
        return config;
    }

    public void setConfig(RealmConfiguration config) {
        this.config = config;
    }
}
