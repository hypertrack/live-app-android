package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;

import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.MembershipSharedPrefsManager;
import io.hypertrack.sendeta.store.UserStore;

/**
 * Created by piyush on 23/07/16.
 */
public class SplashScreen extends BaseActivity{

    private boolean isUserOnboard = false;

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

            // Migrate the Existing User to Memberships by adding his currently existent Personal Membership
            if (MembershipSharedPrefsManager.getMembershipForName(this, "Personal") == null) {
                User user = UserStore.sharedStore.getUser();

                if (user != null) {
                    Membership personalProfile = new Membership(user.getId(), "Personal",
                            user.getHypertrackDriverID(), BuildConfig.API_KEY);
                    MembershipSharedPrefsManager.addMembership(this, personalProfile);
                }
            }

            startActivity(new Intent(this, Home.class));
            finish();
        }
    }

    // Method to handle DeepLink Params
    private void handleDeepLink(Intent intent) {
        
    }
}
