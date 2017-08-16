package io.hypertrack.sendeta.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hypertrack.lib.internal.common.logging.HTLog;
import com.hypertrack.lib.internal.common.util.HTTextUtils;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.presenter.IVerifyPresenter;
import io.hypertrack.sendeta.presenter.VerifyPresenter;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import io.hypertrack.sendeta.util.CrashlyticsWrapper;

/**
 * Created by Aman on 19/07/17.
 */

public class Verify extends BaseActivity implements VerifyView {

    private static final String TAG = Verify.class.getSimpleName();
    EditText otpEditText;
    Button verifyOTP;
    ProgressBar progressBar;
    TextView timerText;
    IVerifyPresenter<VerifyView> presenter = new VerifyPresenter();
    static final int RESEND_CODE_TIMER = 30 * 1000;
    //SMSReceiver smsReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        initView();
        presenter.attachView(this);
        countDownTimer.start();
        //smsReceiver = new SMSReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        //registerReceiver(smsReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(smsReceiver);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        otpEditText = (EditText) findViewById(R.id.otp_edit_text);
        verifyOTP = (Button) findViewById(R.id.verify_otp);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        timerText = (TextView) findViewById(R.id.timer);
        verifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HTTextUtils.isEmpty(otpEditText.getText().toString())) {
                    Toast.makeText(Verify.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgress(true);
                presenter.verifyOTP(otpEditText.getText().toString());
            }
        });
        timerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerText.isClickable()) {
                    presenter.resendOTP();
                    showProgress(true);
                }
            }
        });
        otpEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        otpEditText.requestFocus();
    }

    @Override
    public void showError(String message) {
        if (message.equalsIgnoreCase(presenter.ERROR_INCORRECT_CODE)) {
            timerText.setClickable(true);
            showProgress(false);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void codeVerified() {
        if (getIntent() != null && getIntent().getStringExtra("branch_params") != null) {
            Intent intent = new Intent(Verify.this, Invite.class);
            intent.putExtra("branch_params", getIntent().getStringExtra("branch_params"));
            startActivity(intent);
            finish();
            return;
        }
        SharedPreferenceManager.deletePreviousUserId();
        CrashlyticsWrapper.setCrashlyticsKeys(this);
        // Clear Existing running trip on Registration Successful
        SharedPreferenceManager.deleteAction();
        SharedPreferenceManager.deletePlace();

        HTLog.i(TAG, "User Registration successful: Clearing Active Trip, if any");
        Intent intent = new Intent(Verify.this, Placeline.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder.create(Verify.this)
                .addNextIntentWithParentStack(intent)
                .startActivities();
        finish();
        showProgress(false);
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void codeResent() {
        Toast.makeText(this, "Successfully sent verification code", Toast.LENGTH_SHORT).show();
        timerText.setClickable(false);
        progressBar.setVisibility(View.GONE);
        countDownTimer.cancel();
        countDownTimer.start();
    }

    private CountDownTimer countDownTimer = new CountDownTimer(RESEND_CODE_TIMER, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            timerText.setText("Resend code in " + String.valueOf((int) (millisUntilFinished / 1000) + " sec"));
        }

        @Override
        public void onFinish() {
            timerText.setText("Resend Code");
            timerText.setClickable(true);
        }
    };

    @Override
    protected void onDestroy() {
        // Detach View from Presenter
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    public class SMSReceiver extends BroadcastReceiver {
        private Bundle bundle;
        private SmsMessage currentSMS;
        private String message;

        public SMSReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdu_Objects = (Object[]) bundle.get("pdus");
                    if (pdu_Objects != null) {

                        for (Object aObject : pdu_Objects) {
                            currentSMS = getIncomingMessage(aObject, bundle);

                            String senderNo = currentSMS.getDisplayOriginatingAddress();

                            message = currentSMS.getDisplayMessageBody();
                            if (message.length() > 4) {
                                try {
                                    if (Integer.valueOf(message.substring(0, 4)) > -1) {
                                        otpEditText.setText(message.substring(0, 4));
                                        verifyOTP.performClick();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        this.abortBroadcast();
                    }
                }
            }
        }
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }

        return currentSMS;
    }
}
