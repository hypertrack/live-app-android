package io.hypertrack.meta;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hypertrack.android.sdk.base.network.HyperTrack;
import com.hypertrack.android.sdk.base.network.HyperTrackClient;
import com.hypertrack.android.sdk.base.view.HTMapFragment;

import java.util.List;

import io.hypertrack.meta.model.UserTrip;
import io.hypertrack.meta.network.HTCustomGetRequest;
import io.hypertrack.meta.util.HTConstants;

public class Trip extends AppCompatActivity {

    private static final String TAG = Trip.class.getSimpleName();
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
            setUpConsumerClientForTrackingTrip(data);
        //getTripId(data);
    }

    private void setUpConsumerClientForTrackingTrip(Uri data) {

        String uriString = data.getPath();
        Log.v(TAG, uriString);

        List<String> segments = data.getPathSegments();
        for (String s:segments) {
            Log.v(TAG, s);
        }

        String hyperTrackIdSegment = segments.get(1);
        String hyperTrackIdSegmentToken[] = hyperTrackIdSegment.split(":");
        for (int i=0; i < hyperTrackIdSegmentToken.length; i++) {
            Log.v(TAG, hyperTrackIdSegmentToken[i]);
        }

        Log.d(TAG, "HyperTrack Trip ID: " + hyperTrackIdSegmentToken[0]);
        mHyperTrackClient.setId(hyperTrackIdSegmentToken[0]);
    }

}
