package com.hypertrack.live;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hypertrack.backend.BackendProvider;
import com.hypertrack.backend.ResultHandler;
import com.hypertrack.live.auth.ConfirmFragment;
import com.hypertrack.live.auth.SignInFragment;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.utils.SharedHelper;
import com.hypertrack.sdk.HyperTrack;


public class LaunchActivity extends AppCompatActivity {

    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        sharedHelper = SharedHelper.getInstance(this);
        final String hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();

        HTMobileClient.getInstance(this).initialize(new HTMobileClient.Callback() {
            @Override
            public void onSuccess(HTMobileClient mobileClient) {
                if (!mobileClient.isAuthorized() || TextUtils.isEmpty(hyperTrackPublicKey)) {
                    if (mobileClient.isAuthorized() && TextUtils.isEmpty(hyperTrackPublicKey)) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_frame, new ConfirmFragment(), ConfirmFragment.class.getSimpleName())
                                .commitAllowingStateLoss();
                    } else {
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.fragment_frame, new SignInFragment(), SignInFragment.class.getSimpleName())
                                .commitAllowingStateLoss();
                    }
                } else {
                    startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onError(String message, Exception e) {
                Toast.makeText(LaunchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onLoginCompleted() {

        startActivity(new Intent(this, MainActivity.class));
        finish();

        final String hyperTrackPublicKey = sharedHelper.getHyperTrackPubKey();
        final HyperTrack hyperTrack = HyperTrack.getInstance(this, hyperTrackPublicKey);
        BackendProvider backendProvider = HTMobileClient.getBackendProvider(this);
        backendProvider.start(hyperTrack.getDeviceID(), new ResultHandler<String>() {
            @Override
            public void onResult(String result) {
                hyperTrack.syncDeviceSettings();
            }

            @Override
            public void onError(@NonNull Exception error) {
                Log.e("Sign in", "login completed error:" + error.getMessage());
            }
        });
    }
}
