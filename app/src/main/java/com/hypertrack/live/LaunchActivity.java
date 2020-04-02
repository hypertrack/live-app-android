package com.hypertrack.live;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hypertrack.live.auth.DeeplinkLoginCredentials;
import com.hypertrack.live.auth.ConfirmFragment;
import com.hypertrack.live.auth.SignInFragment;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.utils.SharedHelper;

import io.branch.referral.Branch;
import io.branch.referral.validators.IntegrationValidator;


public class LaunchActivity extends AppCompatActivity {
    private SignInFragment mFragment;
    private DeeplinkLoginCredentials credentialsExtractor;
    private LoaderDecorator mLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        SharedHelper sharedHelper = SharedHelper.getInstance(this);
        final String hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();
        HTMobileClient htMobileClient = HTMobileClient.getInstance(this);
        credentialsExtractor = new DeeplinkLoginCredentials(this, htMobileClient);

        htMobileClient.initialize(new HTMobileClient.Callback() {
            @Override
            public void onSuccess(HTMobileClient mobileClient) {
                if (!mobileClient.isAuthorized() || TextUtils.isEmpty(hyperTrackPublicKey)) {
                    if (mobileClient.isAuthorized() && TextUtils.isEmpty(hyperTrackPublicKey)) {
                        addConfirmationFragment();
                        credentialsExtractor.setCancelled();
                    } else {
                        credentialsExtractor.savedLoginFailed(getLoginFallback());
                    }
                } else {
                    credentialsExtractor.setCancelled();
                    startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override public void onError(String message, Exception e) { showError(e); }
        });
    }

    public void showError(Exception e) {
        Toast.makeText(LaunchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
    }

    public void onLoginCompleted() {

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Branch.sessionBuilder(this)
                .withCallback(credentialsExtractor.getDeeplinkListener())
                .withData(getIntent() != null ? getIntent().getData() : null).init();
        IntegrationValidator.validate(LaunchActivity.this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // if activity is in foreground (or in backstack but partially visible) launching the same
        // activity will skip onStart, handle this case with reInitSession
        Branch.sessionBuilder(this).withCallback(credentialsExtractor.getDeeplinkListener()).reInit();
    }

    public void showLoader() {
        mLoader = new LoaderDecorator(this);
        mLoader.start();
    }

    public void removeLoader() {
        if (mLoader != null) {
            mLoader.stop();
            mLoader = null;
        }
    }

    private Runnable getLoginFallback() {
        return new Runnable() {@Override public void run() { addSigninFragment(); }};
    }

    private void addSigninFragment() {
        mFragment = new SignInFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_frame, mFragment, SignInFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    private void addConfirmationFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, new ConfirmFragment(), ConfirmFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }
}
