package com.hypertrack.live;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.hypertrack.live.auth.SignInFragment;
import com.hypertrack.live.ui.MainActivity;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        final String hyperTrackPublicKey = sharedPreferences.getString("pub_key", "");

        HTMobileClient.getInstance(this).initialize(new HTMobileClient.Callback() {
            @Override
            public void onSuccess(HTMobileClient mobileClient) {
                if (!mobileClient.isAuthorized() || TextUtils.isEmpty(hyperTrackPublicKey)) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.add(R.id.fragment_frame, new SignInFragment(), SignInFragment.class.getSimpleName());
                    transaction.commitAllowingStateLoss();
                } else {
                    startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }
}
