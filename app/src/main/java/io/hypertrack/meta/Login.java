package io.hypertrack.meta;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Login extends AppCompatActivity {

    @Bind(R.id.phoneNumber)
    public EditText phoneNumberView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Verify");
        ButterKnife.bind(this);

        //poulatePhoneNumberIfAvailable();

    }

    private void  poulatePhoneNumberIfAvailable() {
        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String number = tm.getLine1Number();
        if(!TextUtils.isEmpty(number)) {
            phoneNumberView.setText(number);
        }
    }

    @OnClick(R.id.verify)
    public void verifyPhoneNumber() {
        String phoneNumber = phoneNumberView.getText().toString();
        if(!TextUtils.isEmpty(phoneNumber) && phoneNumber.length() == 10) {
            Toast.makeText(Login.this, String.format("Verifying phone number %s",phoneNumber), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Login.this, Verify.class));
            finish();
        } else {
            phoneNumberView.setError("Please enter a valid number");
        }
    }

}
