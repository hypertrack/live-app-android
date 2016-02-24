package io.hypertrack.meta;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.model.Country;
import io.hypertrack.meta.model.CountryMaster;
import io.hypertrack.meta.model.CountrySpinnerAdapter;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.HTConstants;
import io.hypertrack.meta.util.PhoneUtils;

public class Login extends AppCompatActivity {

    private static final String TAG = Login.class.getSimpleName();

    @Bind(R.id.phoneNumber)
    public EditText phoneNumberView;

    private ProgressDialog mProgressDialog;
    private CountryMaster cm;
    private Spinner spinner;
    private CountrySpinnerAdapter adapter;
    private String isoCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Verify");
        ButterKnife.bind(this);

        cm = CountryMaster.getInstance(this);
        final ArrayList<Country> countries = cm.getCountries();
        String countryIsoCode = cm.getDefaultCountryIso();
        Country country = cm.getCountryByIso(countryIsoCode);

        spinner = (Spinner) findViewById(R.id.spinner_countries);
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

        if(checkPermission()) {

        } else {
            requestPermission();
        }


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

        String number = phoneNumberView.getText().toString();

        if(!TextUtils.isEmpty(number) && number.length() < 20) {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumber = null;
            try {
                phoneNumber = phoneUtil.parse(number, isoCode);
                String internationalFormat = phoneUtil.format(
                        phoneNumber,
                        PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                sendPhoneNumber(internationalFormat);
                Log.v(TAG, "International Format: " + internationalFormat);
            } catch (NumberParseException e) {
                e.printStackTrace();
            }

        } else {
            phoneNumberView.setError("Please enter a valid number.");
        }
    }

    private void sendPhoneNumber(String number) {

        String url = HTConstants.API_ENDPOINT + "/api/v1/users/";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("registering phone number");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        User user = new User(number);
        Gson gson = new Gson();
        String jsonObjectBody = gson.toJson(user);

        Log.d("Response", "Request Body - " + jsonObjectBody);

        HTCustomPostRequest<User> request = new HTCustomPostRequest<User>(
                1,
                url,
                jsonObjectBody,
                User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        mProgressDialog.dismiss();

                        Log.d("Response", "ID :" + response.getId());
                        Log.d("Response", "First Name :" + response.getFirstName());
                        Log.d("Response", "Last name :" + response.getLastName());

                        Intent intent = new Intent(Login.this, Verify.class);
                        intent.putExtra(HTConstants.USER_ID, response.getId());

                        SharedPreferences settings = getSharedPreferences("io.hypertrack.meta", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt(HTConstants.USER_ID, response.getId());

                        if (!TextUtils.isEmpty(response.getFirstName())) {
                            editor.putString(HTConstants.USER_FIRSTNAME, response.getFirstName());
                        }

                        if (!TextUtils.isEmpty(response.getLastName())) {
                            editor.putString(HTConstants.USER_LASTNAME, response.getLastName());
                        }

                        if (!TextUtils.isEmpty(response.getPhoto())) {
                            editor.putString(HTConstants.USER_PROFILE_PIC, response.getPhoto());
                        }

                        editor.apply();

                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
                        Log.d("Response", "Inside OnError");
                        Toast.makeText(Login.this, "Apologies, we could process your request at this moment.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        MetaApplication.getInstance().addToRequestQueue(request);

    }

    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean checkPermission(){

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }

    }

    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

            Toast.makeText(this,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Toast.makeText(this,"Permission Granted, Now you can access location data.",Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(this,"Permission Denied, You cannot access location data.",Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

}
