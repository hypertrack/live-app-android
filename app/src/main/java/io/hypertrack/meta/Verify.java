package io.hypertrack.meta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Verify extends AppCompatActivity {

    @Bind(R.id.verificationCode)
    public EditText verificationCodeView;

    @Bind(R.id.register)
    public Button registerButtonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.register)
    public void registerPhoneNumber() {

        String verificationCode = verificationCodeView.getText().toString();

        if (!TextUtils.isEmpty(verificationCode) && verificationCode.length() == 4) {

            Toast.makeText(Verify.this, "Registered", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(Verify.this, Profile.class));
            finish();
        } else {
            verificationCodeView.setError("Please enter valid verification code");
        }
    }

}
