
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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
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
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AcceptInviteModel;
import io.hypertrack.sendeta.model.AppDeepLink;
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
    private Button accept;
    private TextView cancel, permissionText;
    private ProgressBar progressBar;
    private String ACCOUNT_ID_KEY = "account_id";
    private String USER_ID_KEY = "user_id";
    private String HAS_ACCEPTED_KEY = "has_accepted";
    private String ACCOUNT_NAME_KEY = "account_name";
    private String accountID, userID;
    private boolean hasAccepted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_permission);
        initUI();
    }

    public void initUI() {
        accept = (Button) findViewById(R.id.accept);
        cancel = (TextView) findViewById(R.id.cancel);
        permissionText = (TextView) findViewById(R.id.permission_text);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!hasAccepted) {
                    if (progressBar != null)
                        progressBar.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onClick: Accept");
                    HyperTrackService getPlacelineService = HyperTrackServiceGenerator.createService(HyperTrackService.class);
                    Call<User> call = getPlacelineService.acceptInvite(userID, new AcceptInviteModel(accountID));
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            Log.d(TAG, "onResponse: " + response.body());
                            HyperTrack.stopTracking();
                            HyperTrack.setUserId(userID);
                            SharedPreferenceManager.resetBackgroundTracking();
                            hasAccepted = true;
                            prepareAppDeepLink();
                            proceedToNextScreen();
                            if (progressBar != null)
                                progressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t.getMessage());
                            t.printStackTrace();
                            if (progressBar != null)
                                progressBar.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Log.d(TAG, "onClick: Continue");
                    HyperTrack.setUserId(userID);
                    prepareAppDeepLink();
                    proceedToNextScreen();
                    if (progressBar != null)
                        progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Cancel");
                prepareAppDeepLink();
                proceedToNextScreen();
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

    @Override
    protected void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                progressBar.setVisibility(View.GONE);

                if (error == null) {
                    Log.d(TAG, "onInitFinished: Data: " + referringParams.toString());
                    try {
                        hasAccepted = referringParams.getBoolean(HAS_ACCEPTED_KEY);
                        accountID = referringParams.getString(ACCOUNT_ID_KEY);
                        userID = referringParams.getString(USER_ID_KEY);
                        String accountName;
                        accountName = referringParams.getString(ACCOUNT_NAME_KEY);
                        if (!hasAccepted) {
                            accept.setVisibility(View.VISIBLE);
                            cancel.setVisibility(View.VISIBLE);
                            SpannableStringBuilder str = new SpannableStringBuilder(accountName + " wants access to your location data collected on HyperTrack Live");
                            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, accountName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            permissionText.setText(str);
                            // permissionText.setText("Zomato wants access to your location data collected on HyperTrack Live");
                        } else {
                            cancel.setVisibility(View.INVISIBLE);
                            accept.setText("Continue");
                            accept.setVisibility(View.VISIBLE);
                            String temp = "You are already sharing your location with ";
                            SpannableStringBuilder str = new SpannableStringBuilder(temp + accountName);
                            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), temp.length(), accountName.length() + temp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            permissionText.setText(str);
                        }
                    } catch (JSONException e) {
                        prepareAppDeepLink();
                        proceedToNextScreen();
                        e.printStackTrace();
                        accept.setVisibility(View.INVISIBLE);
                    }

                } else {
                    Log.d(TAG, "onInitFinished: Error " + error.getMessage());
                    prepareAppDeepLink();
                    proceedToNextScreen();
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.onNewIntent(intent);
    }

    // Method to handle DeepLink Params
    private void prepareAppDeepLink() {
        appDeepLink = new AppDeepLink(DeepLinkUtil.DEFAULT);

        Intent intent = getIntent();
        // if started through deep link
        if (intent != null && !HTTextUtils.isEmpty(intent.getDataString())) {
            Log.d(TAG, "deeplink " + intent.getDataString());
            appDeepLink = DeepLinkUtil.prepareAppDeepLink(SplashScreen.this, intent.getData());
        }
    }

    private void proceedToNextScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user has signed up
                boolean isUserOnboard = !HTTextUtils.isEmpty(HyperTrack.getUserId());

                if (!isUserOnboard || hasAccepted) {
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
                    CrashlyticsWrapper.setCrashlyticsKeys(SplashScreen.this);
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
}
