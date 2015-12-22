package io.hypertrack.meta;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hypertrack.android.sdk.base.network.HyperTrack;
import com.hypertrack.android.sdk.base.network.HyperTrackClient;
import com.hypertrack.android.sdk.base.view.HTMapFragment;

import io.hypertrack.meta.model.UserTrip;
import io.hypertrack.meta.network.HTCustomGetRequest;
import io.hypertrack.meta.util.HTConstants;

public class Trip extends AppCompatActivity {

    private HTMapFragment htMapFragment;
    private HyperTrackClient mHyperTrackClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        //"cb50db86ff63f556f7856d7690ebc305a7a27c69"
        HyperTrack.setPublishableApiKey("pk_65801d4211efccf3128d74101254e7637e655356");
        htMapFragment = (HTMapFragment) getSupportFragmentManager().findFragmentById(R.id.htMapfragment);
        htMapFragment.disableCourierInfoLayout(true);
        mHyperTrackClient = HyperTrackClient.getInstance(this);
        retrieveIntentData();
    }

    private void retrieveIntentData() {

        Intent intent = getIntent();
        //String action = intent.getAction();
        Uri data = intent.getData();
        if(data != null)
            getTripId(data);
    }

    private void getTripId(Uri data) {

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching trip data ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String uriString = data.getPath();
        String uriId = uriString.substring(7, uriString.indexOf(":"));
        Toast.makeText(Trip.this, "Uri ID: " + uriId, Toast.LENGTH_LONG).show();

        getHyperTrackTripId(uriId);
        // /trips/29:ahsdhfasfif
    }

    public String getTokenFromSharedPreferences() {
        SharedPreferences sharedpreferences = getSharedPreferences(HTConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.getString(HTConstants.USER_AUTH_TOKEN, "None");
    }

    private void getHyperTrackTripId(String metaTripId) {

        String url = "https://meta-api-staging.herokuapp.com/api/v1/trips/" + metaTripId + "/";
        HTConstants.setPublishableApiKey(getTokenFromSharedPreferences());
        Log.d("Request", "Url: " + url + " Token: " + getTokenFromSharedPreferences());

        HTCustomGetRequest<UserTrip> requestObject =
                new HTCustomGetRequest<UserTrip>(url, UserTrip.class, new Response.Listener<UserTrip>() {
                    @Override
                    public void onResponse(UserTrip response) {
                        Log.d("Response", "Inside onResponse");
                        Toast.makeText(Trip.this, "HyperTrack Trip ID: " + response.getHypertrackTripId(), Toast.LENGTH_LONG).show();
                        mHyperTrackClient.setId(response.getHypertrackTripId());
                        mProgressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", "Inside onError");
                        mProgressDialog.dismiss();
                    }
                });

        MetaApplication.getInstance().addToRequestQueue(requestObject);

    }
}
