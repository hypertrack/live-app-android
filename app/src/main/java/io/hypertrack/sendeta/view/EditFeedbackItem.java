package io.hypertrack.sendeta.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.internal.common.util.DateTimeUtility;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.internal.common.util.Utils;
import com.hypertrack.lib.internal.consumer.utils.AnimationUtils;
import com.hypertrack.lib.internal.consumer.view.RippleView;
import com.hypertrack.lib.internal.transmitter.models.ActivitySegment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.ActivityFeedbackModel;
import io.hypertrack.sendeta.network.retrofit.HyperTrackService;
import io.hypertrack.sendeta.network.retrofit.HyperTrackServiceGenerator;
import io.hypertrack.sendeta.store.SharedPreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Aman on 13/09/17.
 */

public class EditFeedbackItem extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private static final String TAG = EditFeedbackItem.class.getSimpleName();
    SupportMapFragment supportMapFragment;
    GoogleMap mMap;
    LinearLayout segmentDetailLayout, segmentPlaceLayout;
    Spinner activityName;
    TextView segmentPlace;
    Button segmentTime;
    RippleView confirmActivity, deleteActivity;
    ActivitySegment activitySegment;
    boolean isActivityNameChanged;
    List<String> items = new ArrayList<String>() {
        {
            add("Select Activity");
            add("Automotive");
            add("Cycling");
            add("On Foot");
            add("Stationary");
            add("Walking");
            add("Running");
        }
    };

    List<String> itemsCopy = new ArrayList<String>() {
        {
            add("automotive");
            add("cycling");
            add("onfoot");
            add("stationary");
            add("walking");
            add("running");
        }
    };

    TextView startDate, endDate, startTime, endTime;
    Calendar startDateTime, endDateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_edit_item);
        initUI();
        if (getIntent() != null) {
            activitySegment = (ActivitySegment) getIntent().getSerializableExtra("activity");
        }
        setdata();
    }

    private void setdata() {
        if (activitySegment != null) {
            segmentTime.setText(activitySegment.formatTime(activitySegment.getStartedAt()) + " to " + activitySegment.formatTime(new Date()));
            String activityType = Utils.toProperCase(activitySegment.getActivityType().toString());
            if (activitySegment.getStartLocation() != null) {
                segmentPlace.setText(activitySegment.getStartLocation().getLatLng().toString());
            } else {
                segmentPlaceLayout.setVisibility(View.GONE);
            }

            int index = -1;


            if (!HTTextUtils.isEmpty(activitySegment.getActivityType().toString())) {
                index = itemsCopy.indexOf(activitySegment.getActivityType().toString());
            }

            if (index != -1) {
                activityName.setSelection(index + 1);
            } else {
                activityName.setSelection(0);
            }
        }
    }

    private void initUI() {
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.segment_map);
        supportMapFragment.getMapAsync(this);

        initActivityNameSpinner();

        segmentDetailLayout = (LinearLayout) findViewById(R.id.segment_detail_layout);
        segmentTime = (Button) findViewById(R.id.segment_time);
        segmentTime.setOnClickListener(this);
        segmentPlaceLayout = (LinearLayout) findViewById(R.id.segment_place_layout);
        segmentPlace = (TextView) findViewById(R.id.segment_place);
        segmentPlaceLayout.setOnClickListener(this);
        deleteActivity = (RippleView) findViewById(R.id.delete);
        confirmActivity = (RippleView) findViewById(R.id.confirm);
        deleteActivity.setOnClickListener(this);
        confirmActivity.setOnClickListener(this);
    }

    private void initActivityNameSpinner() {
        activityName = (Spinner) findViewById(R.id.activity_name);
        activityName.setOnItemSelectedListener(activityNameSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        activityName.setAdapter(adapter);
    }

    private AdapterView.OnItemSelectedListener activityNameSpinner = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            isActivityNameChanged = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    @Override
    public void onClick(View v) {
        if (v == segmentTime) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Edit Start/End time");
            View editView = getLayoutInflater().inflate(R.layout.edit_segment_time, null);
            startDate = (TextView) editView.findViewById(R.id.start_date);
            startTime = (TextView) editView.findViewById(R.id.start_time);
            endDate = (TextView) editView.findViewById(R.id.end_date);
            endTime = (TextView) editView.findViewById(R.id.end_time);
            startDate.setText(activitySegment.formatDate(activitySegment.getStartedAt()));
            endDate.setText(activitySegment.formatDate(activitySegment.getEndedAt()));
            startTime.setText(activitySegment.formatTime(activitySegment.getStartedAt()));
            endTime.setText(activitySegment.formatTime(activitySegment.getEndedAt()));
            builder.setView(editView);
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startDateTime = null;
                    endDateTime = null;
                }
            });
            builder.show();
        } else if (v == segmentPlaceLayout) {

        } else if (v == deleteActivity) {
            ActivityFeedbackModel feedback = new ActivityFeedbackModel(activitySegment, ActivityFeedbackModel.ACTIVITY_DELETED);
            sendActivityFeedback(feedback);

        } else if (v == confirmActivity) {
            ActivityFeedbackModel feedback = new ActivityFeedbackModel();
            if (!HTTextUtils.isEmpty(activitySegment.getLookupId()))
                feedback.setLookupId(activitySegment.getLookupId());
            feedback.setUserID(HyperTrack.getUserId());
            if (startDateTime != null) {
                feedback.setEditedStartedAt(DateTimeUtility.getFormattedTime(startDateTime.getTime()));
            }
            if (endDateTime != null)
                feedback.setEditedEndedAt(DateTimeUtility.getFormattedTime(endDateTime.getTime()));
            if (isActivityNameChanged) {
                feedback.setEditedType(((String) activityName.getSelectedItem()).toLowerCase());
            }
            feedback.setFeedbackType(ActivityFeedbackModel.ACTIVITY_EDITED);
            sendActivityFeedback(feedback);
        }
    }

    public void editStartDate(View view) {

        Calendar calendar = Calendar.getInstance();
        if (startDateTime != null)
            calendar = startDateTime;
        else {
            calendar.setTime(activitySegment.getStartedAt());
        }

        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                startDateTime = Calendar.getInstance();
                startDateTime.setTime(activitySegment.getStartedAt());
                startDateTime.set(year, month, dayOfMonth);
                startDate.setText(activitySegment.formatDate(startDateTime.getTime()));

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    public void editStartTime(View view) {
        Calendar calendar = Calendar.getInstance();
        if (startDateTime != null)
            calendar = startDateTime;
        else {
            calendar.setTime(activitySegment.getStartedAt());
        }

        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                startDateTime = Calendar.getInstance();
                startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startDateTime.set(Calendar.MINUTE, minute);
                startTime.setText(activitySegment.formatTime(startDateTime.getTime()));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePicker.show();
    }

    public void editEndDate(View view) {
        Calendar calendar = Calendar.getInstance();
        if (endDateTime != null)
            calendar = endDateTime;
        else {
            calendar.setTime(activitySegment.getEndedAt());
        }

        DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                endDateTime = Calendar.getInstance();
                endDateTime.setTime(activitySegment.getEndedAt());
                endDateTime.set(year, month, dayOfMonth);
                endDate.setText(activitySegment.formatDate(endDateTime.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    public void editEndTime(View view) {
        Calendar calendar = Calendar.getInstance();
        if (endDateTime != null)
            calendar = endDateTime;
        else {
            calendar.setTime(activitySegment.getEndedAt());
        }
        calendar.setTime(activitySegment.getEndedAt());
        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                endDateTime = Calendar.getInstance();
                endDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endDateTime.set(Calendar.MINUTE, minute);
                endTime.setText(activitySegment.formatTime(endDateTime.getTime()));
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePicker.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        AnimationUtils.expand(segmentDetailLayout);
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);
        LatLng latLng;
        latLng = activitySegment.getStartLocation().getLatLng();
        if (latLng != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
    }

    private boolean areBoundsTooSmall(LatLngBounds bounds, int minDistanceInMeter) {
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude,
                bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < minDistanceInMeter;
    }

    private void updateMapPadding() {
        if (mMap != null) {
            int top = getResources().getDimensionPixelSize(R.dimen.io_ht_lib_consumer_map_fragment_default_top_bottom_padding);
            int left = getResources().getDimensionPixelSize(R.dimen.io_ht_lib_consumer_map_fragment_default_padding);
            int right = getResources().getDimensionPixelSize(R.dimen.io_ht_lib_consumer_map_fragment_default_padding);
            int bottom = segmentDetailLayout.getMeasuredHeight();
            mMap.setPadding(left, top, right, bottom);
        }
    }


    @Override
    public void onMapLoaded() {

        //Add all lat lng of polyline to LatLngBounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //Set width of polyline according to device screen density
        float[] anchors = new float[]{0.5f, 0.5f};
        int count = 0;
        //Add source and expected place marker to map
        if (activitySegment.getStartLocation() != null) {
            MarkerOptions startMarkerOption = new MarkerOptions().
                    position(activitySegment.getStartLocation().getLatLng()).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_source_place_marker)).
                    anchor(anchors[0], anchors[1]);
            mMap.addMarker(startMarkerOption);
            builder.include(activitySegment.getStartLocation().getLatLng());
            count++;
        }
        if (activitySegment.getEndLocation() != null) {
            MarkerOptions endMarkerOptions = new MarkerOptions().
                    position(activitySegment.getEndLocation().getLatLng()).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_end_point));
            mMap.addMarker(endMarkerOptions);
            builder.include(activitySegment.getEndLocation().getLatLng());
            count++;
        }

        float zoom = mMap.getCameraPosition().zoom;
        if (zoom < 16f)
            zoom = 16f;
        CameraUpdate cameraUpdate;
        LatLngBounds bounds = builder.build();
        if (count > 1) {
            // Set bounds for map
            if (areBoundsTooSmall(bounds, 400)) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), zoom);
            } else {
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
            }
            mMap.animateCamera(cameraUpdate, 1500, null);
        } else if (count == 1) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), zoom), 1000, null);
        }

        updateMapPadding();
    }

    private void sendActivityFeedback(final ActivityFeedbackModel activityFeedbackModel) {
        HyperTrackService sendActivityFeedback = HyperTrackServiceGenerator.createService(HyperTrackService.class, this);
        Call<ActivityFeedbackModel> call = sendActivityFeedback.sendActivityFeedback(activityFeedbackModel);
        call.enqueue(new Callback<ActivityFeedbackModel>() {
            @Override
            public void onResponse(Call<ActivityFeedbackModel> call, Response<ActivityFeedbackModel> response) {
                if (response.isSuccessful()) {
                    SharedPreferenceManager.setActivityFeedbackLookupId(activityFeedbackModel.getLookupId(), activityFeedbackModel.getFeedbackType());
                    Toast.makeText(EditFeedbackItem.this, "Feedback Submitted", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onResponse:Activity Feedback Success ");

                    finish();
                }
            }

            @Override
            public void onFailure(Call<ActivityFeedbackModel> call, Throwable t) {
                Log.d(TAG, "onFailure: Activity Feedback Failure ");
                finish();
            }
        });
    }

}
