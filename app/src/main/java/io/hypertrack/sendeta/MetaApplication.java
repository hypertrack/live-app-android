package io.hypertrack.sendeta;

import android.app.Application;
import android.content.Context;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by suhas on 11/11/15.
 */
public class MetaApplication extends Application {

    private static MetaApplication mInstance;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        this.mContext = getApplicationContext();

        HyperTrack.setPublishableApiKey(BuildConfig.API_KEY, getApplicationContext());
        this.setupRealm();

        AnalyticsStore.init(this);
    }

    public void setupRealm() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public Context getAppContext() {
        return this.mContext;
    }

    public static synchronized MetaApplication getInstance() {
        return mInstance;
    }

}
