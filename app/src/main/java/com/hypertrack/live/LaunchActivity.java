package com.hypertrack.live;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hypertrack.live.auth.ConfirmFragment;
import com.hypertrack.live.auth.SignInFragment;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.utils.SharedHelper;



public class LaunchActivity extends AppCompatActivity {
    private LoaderDecorator mLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        SharedHelper sharedHelper = SharedHelper.getInstance(this);
        final String hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();
        HTMobileClient htMobileClient = HTMobileClient.getInstance(this);

        htMobileClient.initialize(new HTMobileClient.Callback() {
            @Override
            public void onSuccess(HTMobileClient mobileClient) {
                if (!mobileClient.isAuthorized() || TextUtils.isEmpty(hyperTrackPublicKey)) {
                    if (mobileClient.isAuthorized() && TextUtils.isEmpty(hyperTrackPublicKey)) {
                        addConfirmationFragment();
                    } else {
                        addSigninFragment();
                    }
                } else {
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

    private void addSigninFragment() {
        SignInFragment fragment = new SignInFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_frame, fragment, SignInFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    private void addConfirmationFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, new ConfirmFragment(), ConfirmFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }
}
