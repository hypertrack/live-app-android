package com.hypertrack.live;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hypertrack.live.auth.ConfirmFragment;
import com.hypertrack.live.auth.SignInFragment;
import com.hypertrack.live.auth.SignUpFragment;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.utils.SharedHelper;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;



public class LaunchActivity extends AppCompatActivity {
    private static final String TAG = "LaunchActivity";
    private LoaderDecorator mLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        final String hyperTrackPublicKey = SharedHelper.getInstance(this).getHyperTrackPubKey();
        if (hyperTrackPublicKey.isEmpty()) {
            mLoader = new LoaderDecorator(this);
            Branch.getAutoInstance(getApplicationContext());
            CognitoClient cognitoClient = CognitoClient.getInstance(this);
            cognitoClient.initialize(new CognitoClient.Callback() {
                @Override
                public void onSuccess(CognitoClient mobileClient) {
                    if (!mobileClient.isAuthorized() || TextUtils.isEmpty(hyperTrackPublicKey)) {
                        if (mobileClient.isAuthorized() && TextUtils.isEmpty(hyperTrackPublicKey)) {
                            addConfirmationFragment();
                        } else {
                            addSignUpFragment();
                        }
                    } else {
                        LaunchActivity.this.onLoginCompleted();
                    }
                }

                @Override public void onError(String message, Exception e) { showError(e); }
            });

        } else {
            onLoginCompleted();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        mLoader.start();
        Branch.sessionBuilder(this)
                .withCallback(getCallback())
                .withData(getIntent() != null ? getIntent().getData() : null)
                .init();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // if activity is in foreground (or in backstack but partially visible) launching the same
        // activity will skip onStart, handle this case with reInitSession
        Branch.sessionBuilder(this).withCallback(getCallback()).reInit();
    }

    private Branch.BranchReferralInitListener getCallback() {
        return new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(@Nullable JSONObject referringParams, @Nullable BranchError error) {
                Log.d(TAG, "onInitFinished: with params: " + (referringParams == null ? "null" : referringParams)
                + ", with error " + (error == null ? "null" : error.getMessage()));
                mLoader.stop();
                if (referringParams == null) return;
                String key = referringParams.optString("publishable_key");
                String userId = referringParams.optString("driver_id");
                if (!key.isEmpty()) {
                    Log.d(TAG, "Got publishable key from branch.io payload" + key);
                    SharedHelper sharedHelper = SharedHelper.getInstance(LaunchActivity.this);
                    sharedHelper.setHyperTrackPubKey(key);
                    sharedHelper.setLoginType(SharedHelper.LOGIN_TYPE_DEEPLINK);
                    if (!userId.isEmpty()) {
                        sharedHelper.setUserNameAndPhone(userId, null);
                    }
                    LaunchActivity.this.onLoginCompleted();
                }
            }
        };
    }

    public void showError(Exception e) {
        Toast.makeText(LaunchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    public void onLoginCompleted() {
        Log.d(TAG, "onLoginCompleted: ");
        // TODO Denys: We need to start tracking via API after login
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void addSignUpFragment() {
        SignUpFragment fragment = new SignUpFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_frame, fragment, SignUpFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    public void addSignInFragment() {
        SignInFragment fragment = new SignInFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_frame, fragment, SignInFragment.class.getSimpleName())
                .addToBackStack(SignInFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    private void addConfirmationFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, new ConfirmFragment(), ConfirmFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }
}
