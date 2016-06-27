package io.hypertrack.meta.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.MetaApplication;
import io.hypertrack.meta.R;
import io.hypertrack.meta.model.Country;
import io.hypertrack.meta.model.CountryMaster;
import io.hypertrack.meta.model.CountrySpinnerAdapter;
import io.hypertrack.meta.presenter.IRegisterPresenter;
import io.hypertrack.meta.presenter.RegisterPresenter;
import io.hypertrack.meta.util.Constants;
import io.hypertrack.meta.util.PhoneUtils;
import io.hypertrack.meta.util.SharedPreferenceManager;

public class Register extends AppCompatActivity implements RegisterView {

    private static final String TAG = Register.class.getSimpleName();

    @Bind(R.id.phoneNumber)
    public EditText phoneNumberView;

    @Bind(R.id.spinner_countries)
    public Spinner spinner;

    private ProgressDialog mProgressDialog;
    private CountrySpinnerAdapter adapter;
    private String isoCode;
    private IRegisterPresenter<RegisterView> registerPresenter = new RegisterPresenter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.title_activity_login));

        ButterKnife.bind(this);
        registerPresenter.attachView(this);

        initCountryFlagSpinner();
//        poulatePhoneNumberIfAvailable();
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

    private void initCountryFlagSpinner() {
        CountryMaster cm = CountryMaster.getInstance(this);
        final ArrayList<Country> countries = cm.getCountries();
        //Country country = cm.getCountryByIso(countryIsoCode);

        adapter = new CountrySpinnerAdapter(this, R.layout.view_country_list_item, countries);
        spinner.setAdapter(adapter);

        String isoCountryCode = PhoneUtils.getCountryRegionFromPhone(this);
        Log.v(TAG, "Region ISO: " + isoCountryCode);

        if (!TextUtils.isEmpty(isoCountryCode)) {
            for (Country c : countries) {
                if (c.mCountryIso.equalsIgnoreCase(isoCountryCode)) {
                    spinner.setSelection(adapter.getPosition(c));
                }
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isoCode = countries.get(position).mCountryIso;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

//    private void poulatePhoneNumberIfAvailable() {
//        TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
//        String number = tm.getLine1Number();
//
//        if(!TextUtils.isEmpty(number)) {
//            phoneNumberView.setText(number);
//        }
//    }

    @Override
    public void registrationFailed() {
        mProgressDialog.dismiss();
        Toast.makeText(Register.this, "Wwe could not process your request at this moment. Please try again.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToVerificationScreen() {
        mProgressDialog.dismiss();

        Intent intent = new Intent(Register.this, Verify.class);
        startActivity(intent);
    }

    @Override
    public void showValidationError() {
        mProgressDialog.dismiss();
        phoneNumberView.setError("Please enter a valid number.");
    }

    /** Action bar menu methods */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onNextButtonClicked(MenuItem menuItem) {
        this.registerPhoneNumber();
    }

    private void registerPhoneNumber() {
        String number = phoneNumberView.getText().toString();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.registration_phone_number));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        registerPresenter.attemptRegistration(number, isoCode);
    }
}
