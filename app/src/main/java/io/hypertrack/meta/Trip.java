package io.hypertrack.meta;

import android.content.Intent;
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

public class Trip extends AppCompatActivity {

    private HTMapFragment htMapFragment;
    private HyperTrackClient mHyperTrackClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        //"cb50db86ff63f556f7856d7690ebc305a7a27c69"
        HyperTrack.setPublishableApiKey("cb50db86ff63f556f7856d7690ebc305a7a27c69");
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

        String uriString = data.getPath();
        String uriId = uriString.substring(7, uriString.indexOf(":"));
        Toast.makeText(Trip.this, "Uri ID: " + uriId, Toast.LENGTH_LONG).show();

        getHyperTrackTripId(uriId);
        // /trips/29:ahsdhfasfif
    }

    private void getHyperTrackTripId(String metaTripId) {

        String url = "https://meta-api-staging.herokuapp.com/api/v1/trips/" + metaTripId + "/";

        HTCustomGetRequest<UserTrip> requestObject =
                new HTCustomGetRequest<UserTrip>(url, UserTrip.class, new Response.Listener<UserTrip>() {
                    @Override
                    public void onResponse(UserTrip response) {
                        Log.d("Response", "Inside onResponse");
                        Toast.makeText(Trip.this, "HyperTrack Trip ID: " + response.getHypertrackTripId(), Toast.LENGTH_LONG).show();
                        mHyperTrackClient.setId(response.getHypertrackTripId());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", "Inside onError");
                    }
                });

        MetaApplication.getInstance().addToRequestQueue(requestObject);

    }
}
