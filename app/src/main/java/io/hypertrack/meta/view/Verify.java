package io.hypertrack.meta.view;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.Profile;
import io.hypertrack.meta.R;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.model.Verification;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.presenter.VerifyPresenter;
import io.hypertrack.meta.util.HTConstants;
import io.hypertrack.meta.util.SMSReceiver;
import io.hypertrack.meta.util.SharedPreferenceManager;

public class Verify extends AppCompatActivity implements VerifyView {

    @Bind(R.id.verificationCode)
    public EditText verificationCodeView;

    @Bind(R.id.register)
    public Button registerButtonView;

    private ProgressDialog mProgressDialog;
    private VerifyPresenter presenter;
    private SharedPreferenceManager sharedPreferenceManager;

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String code = intent.getStringExtra(SMSReceiver.VERIFICATION_CODE);
            if (!TextUtils.isEmpty(code)) {
                verificationCodeView.setText(code);
                registerButtonView.callOnClick();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);

        sharedPreferenceManager = new SharedPreferenceManager(MetaApplication.getInstance());

        presenter = new VerifyPresenter();
        presenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    protected void onResume() {

        super.onResume();

        IntentFilter filter = new IntentFilter(SMSReceiver.SENDETA_SMS_RECIEVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @OnClick(R.id.register)
    public void registerPhoneNumber() {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("verifying code");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String verificationCode = verificationCodeView.getText().toString();
        int userId = sharedPreferenceManager.getUserId();
        presenter.attemptVerification(verificationCode, userId);

    }

    @Override
    public void verificationFailed() {
        mProgressDialog.dismiss();
        Toast.makeText(Verify.this, "Apologies, there was an error while verifying your number. Please try again.",Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToProfileScreen() {
        mProgressDialog.dismiss();
        startActivity(new Intent(Verify.this, Profile.class));
    }

    @Override
    public void showValidationError() {
        mProgressDialog.dismiss();
        verificationCodeView.setError("Please enter valid verification code");
    }
}
