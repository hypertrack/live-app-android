package com.hypertrack.live.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.hypertrack.live.AppUtils;
import com.hypertrack.live.BuildConfig;
import com.hypertrack.live.R;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingInitDelegate;
import com.hypertrack.sdk.TrackingInitError;

public class VerificationActivity extends AppCompatActivity {
    private static final String TAG = VerificationActivity.class.getSimpleName();
    public static final String VERIFICATION_KEY = "VERIFICATION_KEY";

    private LoaderDecorator loader;
    private EditText verifyEditText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        setTitle(R.string.verify_your_publishable_key);

        verifyEditText = findViewById(R.id.verifyEditText);
        final Button verifyButton = findViewById(R.id.verifyButton);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyButton.setEnabled(false);
                final String verificationKey = verifyEditText.getText().toString();
                if (!TextUtils.isEmpty(verificationKey)) {
                    loader.start();
                    HyperTrack.initialize(VerificationActivity.this, verificationKey,
                            false, false, new TrackingInitDelegate() {
                        @Override
                        public void onError(@NonNull TrackingInitError error) {
                            Log.e(TAG, "Initialization failed with error");
                            error.printStackTrace();

                            loader.stop();
                            verifyButton.setEnabled(true);
                            if (AppUtils.isNetworkConnected(VerificationActivity.this)) {
                                initializationFailed();
                            } else {
                                networkNotConnected();
                            }
                        }

                        @Override
                        public void onSuccess() {
                            verifyButton.setEnabled(true);
                            loader.stop();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(VERIFICATION_KEY, verificationKey);
                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        }
                    });
                }
            }
        });
        loader = new LoaderDecorator(this);
    }

    private void initializationFailed() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        verifyEditText.setText("");
                    }
                })
                .setTitle(R.string.valid_publishable_key_required)
                .setMessage(R.string.publishable_key_you_entered_is_invalid)
                .create();
        alertDialog.show();
    }

    private void networkNotConnected() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.app_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setTitle(R.string.valid_publishable_key_required)
                .setMessage(R.string.check_your_network_connection)
                .create();
        alertDialog.show();
    }
}
