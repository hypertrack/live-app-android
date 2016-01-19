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
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.network.HTCustomPostRequest;
import io.hypertrack.meta.util.HTConstants;

public class Login extends AppCompatActivity {

    @Bind(R.id.phoneNumber)
    public EditText phoneNumberView;

    @Bind(R.id.countryCode)
    public EditText countryCodeView;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Verify");
        ButterKnife.bind(this);

        //poulatePhoneNumberIfAvailable();

        /*
        if(BuildConfig.DEBUG) {
            String number = phoneNumberView.getText().toString();
            if(TextUtils.isEmpty(number))
                phoneNumberView.setText("9819705422");
        }
        */

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

        String phoneNumber = phoneNumberView.getText().toString();
        String countryCode = countryCodeView.getText().toString();

        if(!TextUtils.isEmpty(phoneNumber) && phoneNumber.length() == 10 && !TextUtils.isEmpty(countryCode)) {
            //Toast.makeText(Login.this, String.format("Verifying phone number %s",phoneNumber), Toast.LENGTH_SHORT).show();
            sendPhoneNumber(countryCode+phoneNumber);

        } else {
            phoneNumberView.setError("Please enter a valid number");
        }
    }

    private void sendPhoneNumber(String number) {

        String url = "https://meta-api-staging.herokuapp.com/api/v1/users/";

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

                        editor.apply();

                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
                        Log.d("Response", "Inside OnError");
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
