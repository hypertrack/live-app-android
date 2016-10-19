package io.hypertrack.sendeta;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.sendeta.model.DBMigration;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.util.DevDebugUtils;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by suhas on 11/11/15.
 */
public class MetaApplication extends Application {

    private static MetaApplication mInstance;
    private static final String TAG = MetaApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;

        // Set Publishable Key, if not set yet
        if (TextUtils.isEmpty(HyperTrack.getPublishableKey(getApplicationContext()))) {
            HyperTrack.setPublishableApiKey(BuildConfig.API_KEY, getApplicationContext());
        }

        // Initialize Realm to maintain app databases
        this.setupRealm();

        // Initialize AnalyticsStore to start logging Analytics Events
        AnalyticsStore.init(this);

        // Initialize Stetho to debug Databases
        // (NOTE: IFF current Build Variant is DEBUG)
        DevDebugUtils.installStetho(this);
        // Set HyperTrack LogLevel to VERBOSE
        DevDebugUtils.setHTLogLevel(Log.VERBOSE);
    }

    public static synchronized MetaApplication getInstance() {
        return mInstance;
    }

    private void setupRealm() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .schemaVersion(1)
                .migration(new DBMigration())
                .build();

        try {
            Realm.setDefaultConfiguration(realmConfiguration);

            // This will automatically trigger the migration if needed
            Realm realm = Realm.getDefaultInstance();
        } catch (RealmMigrationNeededException e) {
            e.printStackTrace();
            Crashlytics.logException(e);

            // Delete Realm Data in order to prevent further crashes
            Realm.deleteRealm(realmConfiguration);
            Realm realm = Realm.getDefaultInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        System.gc();
    }
}

