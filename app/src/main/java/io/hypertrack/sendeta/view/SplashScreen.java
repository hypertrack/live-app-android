package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

import io.hypertrack.sendeta.model.AppDeepLink;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.util.DeepLinkUtil;

/**
 * Created by piyush on 23/07/16.
 */
public class SplashScreen extends BaseActivity{

    private static final String TAG = SplashScreen.class.getSimpleName();

    private boolean isUserOnboard = false;
    private AppDeepLink appDeepLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        isUserOnboard = UserStore.isUserLoggedIn();

        handleDeepLink(getIntent());

        if (!isUserOnboard) {
            startActivity(new Intent(this, Register.class));
            finish();
        } else {
            UserStore.sharedStore.initializeUser();

            proceedToNextScreen(appDeepLink);
        }
    }

    // Method to handle DeepLink Params
    private void handleDeepLink(Intent intent) {
        appDeepLink = new AppDeepLink(DeepLinkUtil.DEFAULT);
        intent = getIntent();

        // if started through deep link
        if (intent != null && !TextUtils.isEmpty(intent.getDataString())) {
            Log.d(TAG, "deeplink " + intent.getDataString());
            appDeepLink = DeepLinkUtil.prepareAppDeepLink(intent.getDataString());
        }
    }

    // Method to proceed to next screen with deepLink params
    private void proceedToNextScreen(AppDeepLink appDeepLink) {
        switch (appDeepLink.mId) {
            case DeepLinkUtil.MEMBERSHIP:
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(new Intent(this, BusinessProfile.class)
                                    .putExtra(BusinessProfile.KEY_MEMBERSHIP_INVITE, true)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            .startActivities();
                finish();
                break;

            case DeepLinkUtil.DEFAULT:
            default:
                startActivity(new Intent(this, Home.class));
                finish();
                break;
        }
    }
}
