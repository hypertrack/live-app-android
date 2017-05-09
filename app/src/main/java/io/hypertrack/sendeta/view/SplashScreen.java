package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.util.ArrayList;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AppDeepLink;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.util.DeepLinkUtil;

/**
 * Created by piyush on 23/07/16.
 */
public class SplashScreen extends BaseActivity {

    private static final String TAG = SplashScreen.class.getSimpleName();

    private AppDeepLink appDeepLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        prepareAppDeepLink();
        proceedToNextScreen();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        prepareAppDeepLink();
        proceedToNextScreen();
    }

    // Method to handle DeepLink Params
    private void prepareAppDeepLink() {
        appDeepLink = new AppDeepLink(DeepLinkUtil.DEFAULT);

        Intent intent = getIntent();
        // if started through deep link
        if (intent != null && !TextUtils.isEmpty(intent.getDataString())) {
            Log.d(TAG, "deeplink " + intent.getDataString());
            appDeepLink = DeepLinkUtil.prepareAppDeepLink(SplashScreen.this, intent.getData());
        }
    }

    private void proceedToNextScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user has signed up
                boolean isUserOnboard = !TextUtils.isEmpty(HyperTrack.getUserId());

                if (!isUserOnboard) {
                    if (HyperTrack.checkLocationPermission(SplashScreen.this)
                            && HyperTrack.checkLocationServices(SplashScreen.this)) {
                        Intent registerIntent = new Intent(SplashScreen.this, Profile.class);
                        registerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(registerIntent);
                        finish();
                    } else {
                        Intent registerIntent = new Intent(SplashScreen.this, ConfigurePermissions.class);
                        registerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(registerIntent);
                        finish();
                    }
                } else {
                    processAppDeepLink(appDeepLink);
                }
            }
        }, 500);
    }

    // Method to proceed to next screen with deepLink params
    private void processAppDeepLink(final AppDeepLink appDeepLink) {
        switch (appDeepLink.mId) {
            case DeepLinkUtil.TRACK:
                processTrackingDeepLink(appDeepLink);
                break;

            case DeepLinkUtil.DEFAULT:
            default:
                TaskStackBuilder.create(this)
                        .addNextIntentWithParentStack(new Intent(this, Home.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        .startActivities();
                finish();
                break;
        }
    }

    private void processTrackingDeepLink(AppDeepLink appDeepLink) {
        // Check if lookup_id is available from deeplink
        if (!TextUtils.isEmpty(appDeepLink.lookupId)) {
            handleTrackingDeepLinkSuccess(appDeepLink.lookupId, appDeepLink.taskID);
            return;
        }

        // Check if shortCode is empty and taskId is available
        if (TextUtils.isEmpty(appDeepLink.shortCode) && !TextUtils.isEmpty(appDeepLink.taskID)) {
            handleTrackingDeepLinkSuccess(null, appDeepLink.taskID);
            return;
        }

        displayLoader(true);

        // Fetch Action details (lookupId) for given short code
        HyperTrack.getActionForShortCode(appDeepLink.shortCode, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                if (SplashScreen.this.isFinishing())
                    return;
                displayLoader(false);

                if (response.getResponseObject() == null) {
                    // Handle getActionForShortCode API error
                    handleTrackingDeepLinkError();
                    return;
                }

                List<Action> actions = (List<Action>) response.getResponseObject();
                if (actions != null && !actions.isEmpty()) {
                    // Handle getActionForShortCode API success
                    handleTrackingDeepLinkSuccess(actions.get(0).getLookupID(), actions.get(0).getId());

                } else {
                    // Handle getActionForShortCode API error
                    handleTrackingDeepLinkError();
                }
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                if (SplashScreen.this.isFinishing())
                    return;
                displayLoader(false);

                // Handle getActionForShortCode API error
                handleTrackingDeepLinkError();
            }
        });
    }

    private void handleTrackingDeepLinkSuccess(String lookupId, String actionId) {
        // Check if current lookupId is same as the one active currently
        if (!TextUtils.isEmpty(lookupId) &&
                lookupId.equals(ActionManager.getSharedManager(this).getHyperTrackActionLookupId())) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(new Intent(this, Home.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    .startActivities();
            finish();
            return;
        }

        // Proceed with deeplink
        ArrayList<String> actionIds = new ArrayList<>();
        actionIds.add(actionId);

        Intent intent = new Intent()
                .putExtra(Track.KEY_ACTION_ID_LIST, actionIds)
                .putExtra(Track.KEY_TRACK_DEEPLINK, true)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Check if current user is sharing location or not
        if (SharedPreferenceManager.getActionID(this) == null) {
             intent.setClass(SplashScreen.this, Home.class)
                     .putExtra(Track.KEY_LOOKUP_ID, lookupId);
        } else {
            intent.setClass(SplashScreen.this, Track.class);
        }

        // Handle Deeplink on Track Screen with Live Location Sharing disabled
        TaskStackBuilder.create(SplashScreen.this)
                .addNextIntentWithParentStack(intent)
                .startActivities();
        finish();
    }

    private void handleTrackingDeepLinkError() {
        TaskStackBuilder.create(SplashScreen.this)
                .addNextIntentWithParentStack(new Intent(SplashScreen.this, Home.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                .startActivities();
        finish();
    }
}
