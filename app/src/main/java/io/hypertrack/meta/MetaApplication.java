package io.hypertrack.meta;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by suhas on 11/11/15.
 */
public class MetaApplication extends Application {

    private static final String TAG = MetaApplication.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private static MetaApplication mInstance;
    private static final int TIMEOUT_MILLISECONDS = 60000;
    private static final int MAX_RETRIES = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        this.setupRealm();
    }

    public void setupRealm() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public static synchronized MetaApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        req.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MILLISECONDS, MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        req.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MILLISECONDS, MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
