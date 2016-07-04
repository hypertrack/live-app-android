package io.hypertrack.sendeta.view;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.presenter.IVerifyPresenter;
import io.hypertrack.sendeta.presenter.VerifyPresenter;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.KeyboardUtils;
import io.hypertrack.sendeta.util.SMSReceiver;

public class Verify extends BaseActivity implements VerifyView {

    public EditText verificationCodeView;
    public TextView firstCodeTextView, secondCodeTextView, thirdCodeTextView, fouthCodeTextView,
            headerTextView;
    private ProgressDialog mProgressDialog;

    private IVerifyPresenter<VerifyView> presenter = new VerifyPresenter();

    private int retryCount = -1;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onVerificationCodeChanged();
        }
    };

    private OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCode();
                return true;
            }

            return false;
        }
    };

    BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String code = intent.getStringExtra(SMSReceiver.VERIFICATION_CODE);
            if (!TextUtils.isEmpty(code)) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                verificationCodeView.setText(code);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        // Initialize Toolbar
        initToolbar(getString(R.string.title_activity_verify));

        // Initialize UI Views before Attaching View Presenter
        verificationCodeView = (EditText) findViewById(R.id.verify_verification_code);
        firstCodeTextView = (TextView) findViewById(R.id.verify_first_code);
        secondCodeTextView = (TextView) findViewById(R.id.verify_second_code);
        thirdCodeTextView = (TextView) findViewById(R.id.verify_third_code);
        fouthCodeTextView = (TextView) findViewById(R.id.verify_fourth_code);
        headerTextView = (TextView) findViewById(R.id.verify_header_text);

        // Attach View Presenter to View
        presenter.attachView(this);

        // Initialize UI Action Listeners
        verificationCodeView.addTextChangedListener(mTextWatcher);
        verificationCodeView.setOnEditorActionListener(mEditorActionListener);

        // Request Focus to Verification Code View
        verificationCodeView.requestFocus();

        // Add Touch Listeners to Code Text Views
        addCodeTextViewTouchListeners();
    }

    private void addCodeTextViewTouchListeners() {
        firstCodeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.showKeyboard(Verify.this, verificationCodeView);
                return false;
            }
        });

        secondCodeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.showKeyboard(Verify.this, verificationCodeView);
                return false;
            }
        });

        thirdCodeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.showKeyboard(Verify.this, verificationCodeView);
                return false;
            }
        });

        fouthCodeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.showKeyboard(Verify.this, verificationCodeView);
                return false;
            }
        });
    }

    public void onResendButtonClicked(View view) {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.resending_verification_code));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        presenter.resendVerificationCode();
    }


    public void onVerifyButtonClicked(MenuItem menuItem) {
        verifyCode();
    }

    @Override
    public void updateHeaderText(String text) {
        headerTextView.setText(String.format(getString(R.string.verify_phone_number_hearder_note), text));
    }

    @Override
    public void showValidationError() {
        mProgressDialog.dismiss();
        verificationCodeView.setError(getResources().getString(R.string.invalid_verification_code));
    }

    @Override
    public void navigateToProfileScreen() {
        mProgressDialog.dismiss();
        startActivity(new Intent(Verify.this, Profile.class));
    }

    @Override
    public void verificationFailed() {
        mProgressDialog.dismiss();
        Toast.makeText(Verify.this, R.string.phone_no_verification_failed, Toast.LENGTH_SHORT).show();
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
        Toast.makeText(Verify.this, R.string.resend_otp_failed, Toast.LENGTH_SHORT).show();
    }

    private void onVerificationCodeChanged() {
        String code = verificationCodeView.getText().toString();

        firstCodeTextView.setText(code.length() >= 1 ? code.substring(0, 1) : "");
        secondCodeTextView.setText(code.length() >= 2 ? code.substring(1, 2) : "");
        thirdCodeTextView.setText(code.length() >= 3 ? code.substring(2, 3) : "");
        fouthCodeTextView.setText(code.length() >= 4 ? code.substring(3, 4) : "");

        // Proceed to verify code if 4 digits have been entered
        if (code.length() >= 4) {
            verifyCode();
        }
    }

    private void verifyCode() {
        // Increment retries taken by user to enter verification code
        retryCount++;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.verifying_phone_number));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        // Verify code entered with Server
        String verificationCode = verificationCodeView.getText().toString();
        presenter.attemptVerification(verificationCode, retryCount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_verify, menu);
        return super.onCreateOptionsMenu(menu);
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
}