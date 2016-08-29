package io.hypertrack.sendeta;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.fabric.sdk.android.Fabric;
import io.hypertrack.lib.consumer.network.HTConsumerClient;
import io.hypertrack.lib.transmitter.service.HTTransmitterService;
import io.hypertrack.sendeta.model.DBMigration;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.store.AnalyticsStore;
import io.hypertrack.sendeta.store.RealmStore;
import io.hypertrack.sendeta.util.DevDebugUtils;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by suhas on 11/11/15.
 */
public class MetaApplication extends Application {

    private static MetaApplication mInstance;
    private Context mContext;
    private static final String TAG = MetaApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        this.mContext = getApplicationContext();

        // Initialize HyperTrack SDKs
        HTTransmitterService.initHTTransmitter(getApplicationContext());
        HTConsumerClient.initHTConsumerClient(getApplicationContext());

        // Initialize Realm to maintain app databases
        this.setupRealm();

        // Initialize AnalyticsStore to start logging Analytics Events
        AnalyticsStore.init(this);

        // Initialize Stetho to debug Databases
        // (NOTE: IFF current Build Variant is DEBUG)
        DevDebugUtils.installStetho(this);
    }

    public Context getAppContext() {
        return this.mContext;
    }

    public static synchronized MetaApplication getInstance() {
        return mInstance;
    }

    private void migrateRealmDB() {
        // Or you can add the migration code to the configuration. This will run the migration code without throwing
        // a RealmMigrationNeededException.
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name("default1")
                .schemaVersion(2)
                .migration(new DBMigration())
                .build();

        RealmStore.getShareStore().setConfig(config);
        RealmStore.getShareStore().setRealm(Realm.getInstance(config)); // Automatically run migration if needed

        Toast.makeText(this, "Default1", Toast.LENGTH_SHORT).show();
        showStatus(RealmStore.getShareStore().getRealm());
    }

    private void showStatus(Realm realm) {
        String txt = realmString(realm);
        Log.i(TAG, txt);
        Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
    }

    private String realmString(Realm realm) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Membership membership : realm.where(Membership.class).findAll()) {
            stringBuilder.append(membership.toString()).append("\n");
        }

        return (stringBuilder.length() == 0) ? "<data was deleted>" : stringBuilder.toString();
    }

    private String copyBundledRealmFile(InputStream inputStream, String outFileName) {
        try {
            File file = new File(this.getFilesDir(), outFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupRealm() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .schemaVersion(1)
                .migration(new DBMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);

        // This will automatically trigger the migration if needed
        Realm realm = Realm.getDefaultInstance();
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

