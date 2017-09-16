
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;
import com.hypertrack.lib.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.hypertrack.sendeta.BuildConfig;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AcceptInviteModel;
import io.hypertrack.sendeta.model.AppDeepLink;
import io.hypertrack.sendeta.network.retrofit.CallUtils;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.util.CrashlyticsWrapper;
import io.hypertrack.sendeta.util.DeepLinkUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by piyush on 23/07/16.
 */
public class SplashScreen extends BaseActivity {

    private static final String TAG = SplashScreen.class.getSimpleName();

    private AppDeepLink appDeepLink;
    private Button enableLocation;
    private TextView permissionText;
    private ProgressBar progressBar;
    private JSONObject branchParams = new JSONObject();
    private boolean autoAccept;
    private String userID, accountID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareAppDeepLink();
        //Check if location permission and location service is ON
        if (isLocationOn()) {
            //If app is open without clicking any link don't show splash screen except app first launch.
            if (appDeepLink.mId == DeepLinkUtil.DEFAULT && SharedPreferenceManager.getHyperTrackLiveUser() != null) {
                proceedToNextScreen();
                finish();
                return;
            }
            setContentView(R.layout.activity_splash);
            initUI();
            initiateBranch();
        } else {
            setContentView(R.layout.activity_splash);
            initUI();
            progressBar.setVisibility(View.INVISIBLE);
            permissionText.setVisibility(View.VISIBLE);
            enableLocation.setVisibility(View.VISIBLE);
        }

        if (HTTextUtils.isEmpty(BuildConfig.HYPERTRACK_PK)
                || BuildConfig.HYPERTRACK_PK.equalsIgnoreCase("YOUR_HYPERTRACK_PUBLISHABLE_KEY")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("HyperTrack Publishable Key is not configured!")
                    .setMessage("Add HyperTrack Publishable Key to keys.properties file")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .show();
        }
    }

    public void initUI() {
        enableLocation = (Button) findViewById(R.id.enable_location);
        permissionText = (TextView) findViewById(R.id.permission_text);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        enableLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestForLocationSettings();
            }
        });

        final ImageView locationRipple = (ImageView) findViewById(R.id.location_ripple);

        final ScaleAnimation growAnim = new ScaleAnimation(0.9f, 1.05f, 0.9f, 1.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(1.05f, 0.9f, 1.05f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        growAnim.setDuration(800);
        shrinkAnim.setDuration(800);

        locationRipple.setAnimation(growAnim);
        growAnim.start();

        growAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                locationRipple.setAnimation(shrinkAnim);
                shrinkAnim.start();
            }
        });

        shrinkAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                locationRipple.setAnimation(growAnim);
                growAnim.start();
            }
        });
    }

    private void initiateBranch() {
        Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(final JSONObject referringParams, BranchError error) {
                if (error == null) {
                    Log.d(TAG, "onInitFinished: Data: " + referringParams.toString());
                    try {
                        if (referringParams.has(Invite.USER_ID_KEY))
                            userID = referringParams.getString(Invite.USER_ID_KEY);
                        if (referringParams.has(Invite.AUTO_ACCEPT_KEY))
                            autoAccept = referringParams.getBoolean(Invite.AUTO_ACCEPT_KEY);
                        if (referringParams.has(Invite.ACCOUNT_ID_KEY)) {
                            accountID = referringParams.getString(Invite.ACCOUNT_ID_KEY);
                        }
                        branchParams = referringParams;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "onInitFinished: Error " + error.getMessage());
                }
                proceedToNextScreen();
            }
        }, this.getIntent().getData(), this);
    }

    // Method to handle DeepLink Params
    private void prepareAppDeepLink() {
        appDeepLink = new AppDeepLink(DeepLinkUtil.DEFAULT);

        Intent intent = getIntent();
        // if started through deep link
        if (intent != null && !HTTextUtils.isEmpty(intent.getDataString())) {
            Log.d(TAG, "deeplink " + intent.getDataString());
            int flags = intent.getFlags();
            if ((flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
                return;
            }
            appDeepLink = DeepLinkUtil.prepareAppDeepLink(SplashScreen.this, intent.getData());
        }
    }

    private void proceedToNextScreen() {
        // Check if user has signed up
        boolean isUserOnboard = !HTTextUtils.isEmpty(HyperTrack.getUserId());

        //Open profile screen when user is new or clicked on branch deep link and user id from branch payload is not equal to current userID
        if (!isUserOnboard || (HTTextUtils.isEmpty(userID) && autoAccept) || (!HTTextUtils.isEmpty(userID) &&
                !userID.equalsIgnoreCase(HyperTrack.getUserId()))) {

            if (HyperTrack.checkLocationPermission(SplashScreen.this)
                    && HyperTrack.checkLocationServices(SplashScreen.this)) {

                final Intent[] registerIntent = {new Intent(SplashScreen.this, Profile.class)};

                //If clicked on branch deep link then stop tracking for current userID and set userID from branch payload
                if (!HTTextUtils.isEmpty(userID) || autoAccept) {

                    if (isUserOnboard) {
                        if (!HTTextUtils.isEmpty(userID)) {
                            SharedPreferenceManager.setPreviousUserId(HyperTrack.getUserId());
                            HyperTrack.stopTracking();
                            SharedPreferenceManager.resetBackgroundTracking();
                            HyperTrack.setUserId(userID);
                        } else {
                            acceptInvite(HyperTrack.getUserId(), accountID, new HyperTrackCallback() {
                                @Override
                                public void onSuccess(@NonNull SuccessResponse response) {
                                    registerIntent[0] = new Intent(SplashScreen.this, Placeline.class);
                                    registerIntent[0].putExtra("class_from", SplashScreen.class.getSimpleName());
                                    registerIntent[0].setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(registerIntent[0]);
                                    finish();
                                }

                                @Override
                                public void onError(@NonNull ErrorResponse errorResponse) {
                                    registerIntent[0] = new Intent(SplashScreen.this, Placeline.class);
                                    registerIntent[0].putExtra("class_from", SplashScreen.class.getSimpleName());
                                    registerIntent[0].setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(registerIntent[0]);
                                    finish();
                                }
                            });
                            return;
                        }
                    }
                    registerIntent[0].putExtra("branch_params", branchParams.toString());
                }

                registerIntent[0].putExtra("class_from", SplashScreen.class.getSimpleName());
                registerIntent[0].setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(registerIntent[0]);
                finish();
            }

            //If autoAccept params from branch payload is true then don't show invite screen.
        } else if (!HTTextUtils.isEmpty(userID) && !autoAccept) {
            Intent registerIntent = new Intent(SplashScreen.this, Invite.class);
            registerIntent.putExtra("branch_params", branchParams.toString());
            registerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(registerIntent);
            finish();

        } else {
            CrashlyticsWrapper.setCrashlyticsKeys(SplashScreen.this);
            processAppDeepLink(appDeepLink);
        }
    }

    private void acceptInvite(String userID, String accountID, final HyperTrackCallback callback) {
        HyperTrackService acceptInviteService = HyperTrackServiceGenerator.createService(HyperTrackService.class,this);
        Call<User> call = acceptInviteService.acceptInvite(userID, new AcceptInviteModel(accountID, HyperTrack.getUserId()));

        CallUtils.enqueueWithRetry(call, new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    HTLog.d(TAG, "Invite Accepted");

                } else {
                    Log.d(TAG, "onResponse: There is some error occurred in accept invite. Please try again");
                    Toast.makeText(SplashScreen.this, "There is some error occurred. Please try again", Toast.LENGTH_SHORT).show();
                }
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "onInviteFailure: " + t.getMessage());
                t.printStackTrace();
                Log.d(TAG, "onFailure: There is some error occurred in accept invite. Please try again");
                Toast.makeText(SplashScreen.this, "There is some error occurred. Please try again", Toast.LENGTH_SHORT).show();
                callback.onError(null);
            }
        });
    }

    // Method to proceed to next screen with deepLink params
    private void processAppDeepLink(final AppDeepLink appDeepLink) {
        switch (appDeepLink.mId) {
            case DeepLinkUtil.TRACK:
                processTrackingDeepLink(appDeepLink);
                break;

            case DeepLinkUtil.DEFAULT:
            default:
                final ActionManager actionManager = ActionManager.getSharedManager(this);
                //Check if there is any existing task to be restored
                if (actionManager.shouldRestoreState()) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(new Intent(this, Home.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            .startActivities();
                } else {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(new Intent(this, Placeline.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            .startActivities();
                }
                finish();
                break;
        }
    }

    private void processTrackingDeepLink(AppDeepLink appDeepLink) {
        // Check if lookup_id is available from deeplink
        if (!HTTextUtils.isEmpty(appDeepLink.lookupId)) {
            handleTrackingDeepLinkSuccess(appDeepLink.lookupId, appDeepLink.taskID);
            return;
        }

        // Check if shortCode is empty and taskId is available
        if (HTTextUtils.isEmpty(appDeepLink.shortCode) && !HTTextUtils.isEmpty(appDeepLink.taskID)) {
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
                    handleTrackingDeepLinkSuccess(actions.get(0).getLookupId(), actions.get(0).getId());

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
        if (!HTTextUtils.isEmpty(lookupId) &&
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

        Action action = SharedPreferenceManager.getTrackingAction(this);
        // Check if current user is sharing location or not
        if (SharedPreferenceManager.getActionID(this) == null ||
                (action != null && actionId.equalsIgnoreCase(action.getId()))) {
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
        ActionManager actionManager = ActionManager.getSharedManager(this);
        //Check if there is any existing task to be restored
        if (actionManager.shouldRestoreState()) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(new Intent(this, Home.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    .startActivities();
        } else {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(new Intent(this, Placeline.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    .startActivities();
        }
        finish();
        actionManager = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Handle Location permission successfully granted response
                requestForLocationSettings();

            } else {
                // Handle Location permission request denied error
                showSnackBar();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Handle Location services successfully enabled response
                requestForLocationSettings();

            } else {
                // Handle Location services request denied error
                showSnackBar();
            }
        }
    }

    private boolean isLocationOn() {
        if (!HyperTrack.checkLocationPermission(this) || !HyperTrack.checkLocationServices(this)) {
            return false;
        }
        return true;
    }

    private void requestForLocationSettings() {
        // Check for Location permission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this);
            return;
        }

        // Check for Location settings
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this);
            return;
        }

        // Location Permissions and Settings have been enabled
        // Proceed with your app logic here
        if (appDeepLink.mId == DeepLinkUtil.DEFAULT && SharedPreferenceManager.getHyperTrackLiveUser() != null)
            proceedToNextScreen();
        else {
            progressBar.setVisibility(View.VISIBLE);
            initiateBranch();
        }
    }


    private void showSnackBar() {
        if (!HyperTrack.checkLocationPermission(this)) {
            // Handle Location permission request denied error
            Snackbar.make(findViewById(R.id.parent_layout), R.string.location_permission_snackbar_msg,
                    Snackbar.LENGTH_INDEFINITE).setAction("Allow", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestForLocationSettings();
                }
            }).show();

        } else if (HyperTrack.checkLocationServices(this)) {
            // Handle Location services request denied error
            Snackbar.make(findViewById(R.id.parent_layout), R.string.location_services_snackbar_msg,
                    Snackbar.LENGTH_INDEFINITE).setAction("Enable", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestForLocationSettings();
                }
            }).show();
        }
    }
}
