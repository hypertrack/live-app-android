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

import com.hypertrack.live.utils.AppUtils;
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
                final String verificationKey = verifyEditText.getText().toString();
                if (!TextUtils.isEmpty(verificationKey)) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(VERIFICATION_KEY, verificationKey);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
        loader = new LoaderDecorator(this);
    }
}
