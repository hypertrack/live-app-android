package io.hypertrack.meta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

import io.hypertrack.lib.common.HyperTrack;
import io.hypertrack.lib.consumer.network.HTConsumerClient;
import io.hypertrack.lib.consumer.view.HTMapFragment;
import io.hypertrack.meta.BuildConfig;
import io.hypertrack.meta.R;

public class Trip extends AppCompatActivity {

    private static final String TAG = Trip.class.getSimpleName();
    private HTMapFragment htMapFragment;
    private HTConsumerClient mHyperTrackClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        HyperTrack.setPublishableApiKey(BuildConfig.API_KEY, getApplicationContext());
        HyperTrack.setLogLevel(Log.VERBOSE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        htMapFragment = (HTMapFragment) getSupportFragmentManager().findFragmentById(R.id.htMapfragment);
        htMapFragment.setDriverInfoLayoutVisibility(true);
        mHyperTrackClient = HTConsumerClient.getInstance(this);
        retrieveIntentData();
    }

    private void retrieveIntentData() {

//        trackTrip("8796");
//        return;

        /*
        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            String tripId = extras.getString("trip_id");
            if (!TextUtils.isEmpty(tripId)) {
                trackTrip(tripId);
            } else {
                Uri data = intent.getData();
                if(data != null)
                    setUpConsumerClientForTrackingTrip(data);
            }
        }*/
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
        trackTrip(hyperTrackIdSegmentToken[0]);
    }

    private void trackTrip(String tripId) {
//        mHyperTrackClient.trackTrip(tripId, this, new HTStatusCallBack() {
//            @Override
//            public void onSuccess(String s) {
//                Log.v(TAG, "Tracking successful.");
//
//                if (mHyperTrackClient.getStatus().equalsIgnoreCase(HTConsumerClient.ORDER_STATUS_DELIVERED)) {
//                    Toast.makeText(Trip.this, "The Trip has ended", Toast.LENGTH_LONG).show();
//                    drawPolyline();
//                }
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.w(TAG, "Couldn't be tracked.");
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                startActivityAfterCleanup(Home.class);
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void startActivityAfterCleanup(Class<?> cls) {
        Intent intent = new Intent(getApplicationContext(), cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityIfNeeded(intent, 0);
        finish();
    }


    private void drawPolyline() {
        String encodedLine = mHyperTrackClient.getTaskStore().getTask().getEncodedPolyline();
        if (TextUtils.isEmpty(encodedLine))
            return;

        List<LatLng> list = PolyUtil.decode(encodedLine);
        PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE);
        options.addAll(list);

        htMapFragment.getMap().addPolyline(options);

        if (list.size() >= 2) {

            LatLngBounds.Builder b = new LatLngBounds.Builder();
            Log.v(TAG, "Setting up bounds for polyline");
            Log.v(TAG, "including " + list.get(0));
            b.include(list.get(0));

            Log.v(TAG, "including " + list.get(list.size() - 1));
            b.include(list.get(list.size() - 1));
            LatLngBounds bounds = b.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
            htMapFragment.getMap().moveCamera(cu);//Camera(cu, 1000, null);
        }
    }

}
