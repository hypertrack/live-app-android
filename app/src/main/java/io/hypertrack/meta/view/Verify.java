package io.hypertrack.meta.view;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.R;
import io.hypertrack.meta.presenter.IVerifyPresenter;
import io.hypertrack.meta.presenter.VerifyPresenter;
import io.hypertrack.meta.util.SMSReceiver;
import io.hypertrack.meta.util.SharedPreferenceManager;

public class Verify extends AppCompatActivity implements VerifyView {

    @Bind(R.id.verificationCode)
    public EditText verificationCodeView;

    private ProgressDialog mProgressDialog;
    private IVerifyPresenter<VerifyView> presenter = new VerifyPresenter();
    private SharedPreferenceManager sharedPreferenceManager;

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String code = intent.getStringExtra(SMSReceiver.VERIFICATION_CODE);
            if (!TextUtils.isEmpty(code)) {
                verificationCodeView.setText(code);
                verifyCode();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ButterKnife.bind(this);
        sharedPreferenceManager = new SharedPreferenceManager(MetaApplication.getInstance());
        presenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
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

    /** Action bar menu methods */

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_verify, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    public void onVerifyButtonClicked(MenuItem menuItem) {
        this.verifyCode();
    }

    private void verifyCode() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.verifying_phone_number));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String verificationCode = verificationCodeView.getText().toString();
        int userId = sharedPreferenceManager.getUserId();
        presenter.attemptVerification(verificationCode, userId);
    }
}
