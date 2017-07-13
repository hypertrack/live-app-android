package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.hypertrack.lib.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.AcceptInviteModel;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Aman Jain on 09/03/17.
 */

public class AccountPermissions extends BaseActivity {

    private static final String TAG = AccountPermissions.class.getSimpleName();
    private Button accept;
    private TextView cancel, permissionText;
    private ProgressBar progressBar;
    private String ACCOUNT_ID_KEY = "account_id";
    private String USER_ID_KEY = "user_id";
    private String HAS_ACCEPTED_KEY = "has_accepted";
    private String ACCOUNT_NAME_KEY = "account_name";
    private String accountID, userID;

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
                Log.d(TAG, "onClick: Accept");
                HyperTrackService getPlacelineService = HyperTrackServiceGenerator.createService(HyperTrackService.class);
                Call<User> call = getPlacelineService.acceptInvite(userID, new AcceptInviteModel(accountID));
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        Log.d(TAG, "onResponse: " + response.body());
                        HyperTrack.setUserId(userID);
                        startActivity(new Intent(AccountPermissions.this, SplashScreen.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.d(TAG, "onFailure: " + t.getMessage());
                        t.printStackTrace();
                    }
                });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Cancel");
                startActivity(new Intent(AccountPermissions.this, SplashScreen.class));
                finish();
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

    private void proceedToProfileScreen() {
        startActivity(new Intent(AccountPermissions.this, Profile.class));
        finish();
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
                    try {
                        boolean hasAccepted = referringParams.getBoolean(HAS_ACCEPTED_KEY);
                        if (!hasAccepted) {
                            accept.setVisibility(View.VISIBLE);
                            cancel.setVisibility(View.VISIBLE);
                            accountID = referringParams.getString(ACCOUNT_ID_KEY);
                            userID = referringParams.getString(USER_ID_KEY);
                            String accountName = referringParams.getString(ACCOUNT_NAME_KEY);
                            SpannableStringBuilder str = new SpannableStringBuilder(accountName + " wants access to your location data collected on HyperTrack Live");
                            str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, accountName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            permissionText.setText(str);
                            // permissionText.setText("Zomato wants access to your location data collected on HyperTrack Live");
                        } else {
                            accept.setVisibility(View.INVISIBLE);
                            cancel.setText("Continue");
                            cancel.setVisibility(View.VISIBLE);
                            permissionText.setText("You are already sharing your location with Zomato");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        accept.setVisibility(View.INVISIBLE);
                    }
                    Log.d(TAG, "onInitFinished: Data: " + referringParams.toString());
                } else {
                    Log.d(TAG, "onInitFinished: Error " + error.getMessage());
                    startActivity(new Intent(AccountPermissions.this, SplashScreen.class));
                    finish();
                }
            }
        }, this.getIntent().getData(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.onNewIntent(intent);
    }
}
