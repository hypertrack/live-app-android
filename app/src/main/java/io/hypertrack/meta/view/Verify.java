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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.R;
import io.hypertrack.meta.presenter.IVerifyPresenter;
import io.hypertrack.meta.presenter.VerifyPresenter;
import io.hypertrack.meta.util.ErrorMessages;
import io.hypertrack.meta.util.KeyboardUtils;
import io.hypertrack.meta.util.SMSReceiver;

public class Verify extends AppCompatActivity implements VerifyView {

    @Bind(R.id.verificationCode)
    public EditText verificationCodeView;

    @Bind(R.id.first_code)
    public TextView firstCodeTextView;

    @Bind(R.id.second_code)
    public TextView secondCodeTextView;

    @Bind(R.id.third_code)
    public TextView thirdCodeTextView;

    @Bind(R.id.fourth_code)
    public TextView fouthCodeTextView;

    @Bind(R.id.verify_header_text)
    public TextView headerTextView;

    private ProgressDialog mProgressDialog;
    private IVerifyPresenter<VerifyView> presenter = new VerifyPresenter();

    private int retryCount = -1, resendCount = 0;

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
        presenter.attachView(this);

        this.verificationCodeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Verify.this.onTextChanged();
            }
        });

        verificationCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Verify.this.verifyCode();
                    return true;
                }

                return false;
            }
        });

        this.verificationCodeView.requestFocus();
        this.addTouchListeners();
    }

    private void addTouchListeners() {
        this.firstCodeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.showKeyboard(Verify.this, verificationCodeView);
                return false;
            }
        });

        this.secondCodeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.showKeyboard(Verify.this, verificationCodeView);
                return false;
            }
        });

        this.thirdCodeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.showKeyboard(Verify.this, verificationCodeView);
                return false;
            }
        });

        this.fouthCodeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.showKeyboard(Verify.this, verificationCodeView);
                return false;
            }
        });
    }

    @Override
    public void navigateToProfileScreen() {
        mProgressDialog.dismiss();
        startActivity(new Intent(Verify.this, Profile.class));
    }

    @Override
    public void verificationFailed() {
        mProgressDialog.dismiss();
        Toast.makeText(Verify.this, ErrorMessages.PHONE_NO_VERIFICATION_FAILED, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showValidationError() {
        mProgressDialog.dismiss();
        verificationCodeView.setError(ErrorMessages.INVALID_VERIFICATION_CODE);
    }

    @Override
    public void didResendVerificationCode() {
        // Reset Retry Count on successful Resend OTP call
        retryCount = -1;

        mProgressDialog.dismiss();
    }

    @Override
    public void showResendError() {
        mProgressDialog.dismiss();
        Toast.makeText(Verify.this, ErrorMessages.RESEND_OTP_FAILED, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateHeaderText(String text) {
        this.headerTextView.setText(String.format(getString(R.string.verify_phone_number_hearder_note), text));
    }

    /** Action bar menu methods */

    private void verifyCode() {
        retryCount++;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.verifying_phone_number));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String verificationCode = verificationCodeView.getText().toString();
        presenter.attemptVerification(verificationCode, retryCount);
    }

    private void onTextChanged() {
        String code = this.verificationCodeView.getText().toString();

        this.firstCodeTextView.setText(code.length() >= 1 ? code.substring(0, 1) : "");
        this.secondCodeTextView.setText(code.length() >= 2 ? code.substring(1, 2) : "");
        this.thirdCodeTextView.setText(code.length() >= 3 ? code.substring(2, 3) : "");
        this.fouthCodeTextView.setText(code.length() >= 4 ? code.substring(3, 4) : "");

        if (code.length() >= 4) {
            this.verifyCode();
        }
    }

    @OnClick(R.id.btn_resend)
    public void resendButtonClicked(Button button) {
        resendCount++;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.resending_verification_code));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        presenter.resendVerificationCode(resendCount);
    }


    public void onVerifyButtonClicked(MenuItem menuItem) {
        verifyCode();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(SMSReceiver.SENDETA_SMS_RECIEVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_verify, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
