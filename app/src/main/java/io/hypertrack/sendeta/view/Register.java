package io.hypertrack.sendeta.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Country;
import io.hypertrack.sendeta.model.CountryMaster;
import io.hypertrack.sendeta.model.CountrySpinnerAdapter;
import io.hypertrack.sendeta.presenter.IRegisterPresenter;
import io.hypertrack.sendeta.presenter.RegisterPresenter;
import io.hypertrack.sendeta.util.ErrorMessages;
import io.hypertrack.sendeta.util.PermissionUtils;
import io.hypertrack.sendeta.util.PhoneUtils;

public class Register extends BaseActivity implements RegisterView {

    private static final String TAG = "Register";

    public EditText phoneNumberView;
    public Spinner countryCodeSpinner;
    private ProgressDialog mProgressDialog;

    private String isoCode;

    private CountrySpinnerAdapter adapter;
    private IRegisterPresenter<RegisterView> registerPresenter = new RegisterPresenter();

    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                registerPhoneNumber();
                return true;
            }

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Toolbar without Home Button
        initToolbar(getResources().getString(R.string.title_activity_login), false);

        // Initialize UI Views before Attaching View Presenter
        phoneNumberView = (EditText) findViewById(R.id.register_phone_number);
        countryCodeSpinner = (Spinner) findViewById(R.id.register_country_codes_spinner);

        // Attach View Presenter to View
        registerPresenter.attachView(this);

        // Initialize UI Action Listeners
        phoneNumberView.setOnEditorActionListener(mEditorActionListener);

        // Check If READ_PHONE_STATE Permission is available & Initialize CountryFlagSpinner
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            initCountryFlagSpinner();

        } else {
            // Show Rationale & Request for READ_PHONE_STATE permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                PermissionUtils.showRationaleMessageAsDialog(this, Manifest.permission.READ_PHONE_STATE,
                        getString(R.string.read_phone_state_permission_title), getString(R.string.read_phone_state_msg));
            } else {
                PermissionUtils.requestPermission(this, Manifest.permission.READ_PHONE_STATE);
            }
        }
    }

    private void initCountryFlagSpinner() {
        CountryMaster cm = CountryMaster.getInstance(this);
        final ArrayList<Country> countries = cm.getCountries();

        adapter = new CountrySpinnerAdapter(this, R.layout.view_country_list_item, countries);
        countryCodeSpinner.setAdapter(adapter);

        String isoCountryCode = PhoneUtils.getCountryRegionFromPhone(this);
        Log.v(TAG, "Region ISO: " + isoCountryCode);

        if (!TextUtils.isEmpty(isoCountryCode)) {
            for (Country c : countries) {
                if (c.mCountryIso.equalsIgnoreCase(isoCountryCode)) {
                    countryCodeSpinner.setSelection(adapter.getPosition(c));
                }
            }
        }

        countryCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isoCode = countries.get(position).mCountryIso;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void onNextButtonClicked(MenuItem menuItem) {
        registerPhoneNumber();
    }

    private void registerPhoneNumber() {
        String number = phoneNumberView.getText().toString();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.registration_phone_number));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        registerPresenter.attemptRegistration(number, isoCode);
    }

    @Override
    public void registrationSuccessful() {
        mProgressDialog.dismiss();

        // Check If RECEIVE_SMS Permission is available & Navigate to Verification Screen
        if (ContextCompat.checkSelfPermission(Register.this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            navigateToVerificationScreen();

        } else {
            // Show Rationale & Request for RECEIVE_SMS permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(Register.this, Manifest.permission.RECEIVE_SMS)) {
                PermissionUtils.showRationaleMessageAsDialog(Register.this, Manifest.permission.RECEIVE_SMS,
                        getString(R.string.sms_receive_permission_title), getString(R.string.sms_receive_permission_msg));
                return;
            } else {
                PermissionUtils.requestPermission(Register.this, Manifest.permission.RECEIVE_SMS);
                return;
            }
        }
    }

    private void navigateToVerificationScreen() {
        Intent intent = new Intent(Register.this, Verify.class);
        startActivity(intent);
    }

    @Override
    public void registrationFailed() {
        mProgressDialog.dismiss();
        Toast.makeText(Register.this, ErrorMessages.PHONE_NO_REGISTRATION_FAILED, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showValidationError() {
        mProgressDialog.dismiss();
        phoneNumberView.setError(getResources().getString(R.string.invalid_phone_number));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_PERMISSION_READ_PHONE_STATE:
                // Get Devices's Country Code for both Positive & Negative cases
                initCountryFlagSpinner();
                break;
            case PermissionUtils.REQUEST_CODE_PERMISSION_SMS_RECEIVER:
                // Navigate to Verification Screen for both Positive & Negative cases
                navigateToVerificationScreen();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        registerPresenter.detachView();
        super.onDestroy();
    }
}
