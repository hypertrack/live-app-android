package io.hypertrack.sendeta.view;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.internal.consumer.utils.TimeAwarePolylineUtils;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.PlacelineData;
import io.hypertrack.sendeta.model.Segment;
import io.hypertrack.sendeta.store.ActionManager;
import io.hypertrack.sendeta.store.PlacelineManager;
import io.hypertrack.sendeta.store.SharedPreferenceManager;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class Placeline extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = Placeline.class.getSimpleName();
    private RecyclerView placelineRecyclerView;
    private PlacelineAdapter placelineAdapter;
    private List<Segment> sanitizeSegments = new ArrayList<>();
    private PlacelineManager placelineManager;
    private SupportMapFragment supportMapFragment;
    private CompactCalendarView mCompactCalendarView;
    private AppBarLayout mAppBarLayout;
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, /*Locale.getDefault()*/Locale.ENGLISH);
    private SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    private SimpleDateFormat format = new SimpleDateFormat("EEEE, MMM d", Locale.ENGLISH);
    private Date selectedDate;
    private boolean isExpanded = false;
    private ImageView arrow;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private PlacelineData placelineData;
    private TextView placelineStatus;
    private ProgressBar progressBar;
    private FrameLayout progressLayout;
    private RelativeLayout topBar;
    private String userID;
    private Handler handler;
    private Runnable runnable;
    private int FETCH_TIME = 30 * 1000;
    private GoogleMap mMap;
    private DashedLine dashedLine;
    private TextView dateSelector;
    private RelativeLayout toolbarHeader;

    int previousIndex = -1;
    private FloatingActionButton floatingActionButton;
    private boolean isFirstTime = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeline);

        userID = getIntent().getStringExtra("user_id");
        if (HTTextUtils.isEmpty(userID)) {
            userID = HyperTrack.getUserId();
        }

        selectedDate = new Date();

        initUI();

        placelineManager = PlacelineManager.getPlacelineManager(this);

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                getPlacelineData();
            }
        };

        setPlacelineData();

        setCurrentDate(selectedDate);
        if (!SharedPreferenceManager.hasRequestedForBackgroundTracking()) {
            startHyperTrackTracking(true);
            SharedPreferenceManager.setRequestedForBackgroundTracking();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        getPlacelineData();
        handler.postDelayed(runnable, FETCH_TIME);

    }

    private void initUI() {
        initMapFragment();
        initToolbar();
        initCalendarView();
        initProgressLayout();
        initBottomLayout();
    }

    //Initialize Map Fragment
    private void initMapFragment() {
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        supportMapFragment.getMapAsync(this);
    }

    //Initialize toolbar
    private void initToolbar() {

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarHeader = (RelativeLayout) findViewById(R.id.toolbar_header);
        toolbarHeader.setOnClickListener(dateSelectorOnClickListener);

        dateSelector = (TextView) findViewById(R.id.date_selector);
        arrow = (ImageView) findViewById(R.id.arrow);

      /*  placelineText = (TextView) findViewById(R.id.placeline_text);
        SpannableStringBuilder cs = new SpannableStringBuilder(getString(R.string.your_placeline));
        cs.setSpan(new SuperscriptSpan(), 10, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        cs.setSpan(new RelativeSizeSpan(0.4f), 10, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        placelineText.setText(cs);*/
    }

    //Initialize Calendar View
    private void initCalendarView() {
        // Set up the CompactCalendarView
        mCompactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);

        mCompactCalendarView.setLocale(TimeZone.getDefault(), Locale.ENGLISH);

        mCompactCalendarView.setShouldDrawDaysHeader(true);

        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                setDate(dateFormat.format(dateClicked));
                selectedDate = dateClicked;
                hideCalendar();
                setPlacelineData();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                setDateInMonthFormat(firstDayOfNewMonth);
            }
        });
    }

    private void initBottomLayout() {

        dashedLine = (DashedLine) findViewById(R.id.dashed_line);

        placelineRecyclerView = (RecyclerView) findViewById(R.id.placeline_recycler_view);
        placelineRecyclerView.addItemDecoration(new OverlapDecoration());

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        placelineRecyclerView.setLayoutManager(linearLayoutManager);

        ViewCompat.setNestedScrollingEnabled(placelineRecyclerView, false);

        // Add effects like viewpager item scroll
        final SnapHelper snapHelperTop = new LinearSnapHelper();
        snapHelperTop.attachToRecyclerView(placelineRecyclerView);
        placelineRecyclerView.setOnFlingListener(snapHelperTop);

        placelineRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View view = snapHelperTop.findSnapView(linearLayoutManager);
                    int index = recyclerView.getChildAdapterPosition(view);
                    if (index != 0 && index != placelineAdapter.getItemCount() - 1)
                        animateMap(index - 1);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        placelineAdapter = new PlacelineAdapter(sanitizeSegments);
        placelineAdapter.setCurrentDate(selectedDate);
        placelineRecyclerView.setAdapter(placelineAdapter);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Placeline.this, Home.class);
                intent.putExtra("class_from", Placeline.class.getSimpleName());
                startActivity(intent);
                //overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
    }

    private void initProgressLayout() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressLayout = (FrameLayout) findViewById(R.id.progress_layout);
        placelineStatus = (TextView) findViewById(R.id.placeline_status);
    }

    //Click listener for date selector
    private View.OnClickListener dateSelectorOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isExpanded) {
                hideCalendar();
            } else {
                showCalendar();
            }
        }
    };

    //Animate Map with polyline and marker when item scrolls in recyclerview
    private void animateMap(int index) {
        if (previousIndex != index && index >= 0 && index < sanitizeSegments.size()) {

            mMap.clear();
            Segment segment = sanitizeSegments.get(index);

            //Check segment if trip show polyline, source and expected markers on map
            // if stop then show stop marker.
            if (segment.isTrip()) {
                List<LatLng> latLngList = new ArrayList<LatLng>();

                if (!HTTextUtils.isEmpty(segment.getTimeAwarePolyline())) {

                    latLngList = TimeAwarePolylineUtils.
                            getLatLngList(segment.getTimeAwarePolyline());

                } else {
                    if (segment.getStartLocation() != null &&
                            segment.getStartLocation().getGeoJSONLocation() != null) {

                        latLngList.add(segment.getStartLocation().getGeoJSONLocation().getLatLng());

                    }
                    if (segment.getEndLocation() != null &&
                            segment.getEndLocation().getGeoJSONLocation() != null) {

                        latLngList.add(segment.getEndLocation().getGeoJSONLocation().getLatLng());
                    }
                }

                //Add all lat lng of polyline to LatLngBounds
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : latLngList) {
                    builder.include(latLng);
                }

                // Set bounds for map
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels / 2;
                int padding = (int) (width * 0.12);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

                //Set width of polyline according to device screen density
                float density = getResources().getDisplayMetrics().density;
                int polylineWidth = (int) (3 * density);

                //Add polyline to map
                PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngList).
                        color(Color.BLACK).width(polylineWidth);
                mMap.addPolyline(polylineOptions);
                mMap.animateCamera(cameraUpdate, 1500, null);

                float[] anchors = new float[]{0.5f, 0.5f};

                //Add source and expected place marker to map
                if (latLngList.size() > 0) {

                    MarkerOptions startMarkerOption = new MarkerOptions().
                            position(latLngList.get(0)).
                            icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_source_place_marker)).
                            anchor(anchors[0], anchors[1]);
                    mMap.addMarker(startMarkerOption);

                    MarkerOptions endMarkerOptions = new MarkerOptions().
                            position(latLngList.get(latLngList.size() - 1)).
                            icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ht_expected_place_marker)).
                            anchor(anchors[0], anchors[1]);
                    mMap.addMarker(endMarkerOptions);
                }

            } else if (segment.isStop()) {

                LatLng latLng = segment.getPlace().getLocation().getLatLng();

                MarkerOptions markerOptions = new MarkerOptions().
                        position(latLng).
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_stop_point));
                mMap.addMarker(markerOptions);

                float zoom = mMap.getCameraPosition().zoom;
                if (zoom < 16f)
                    zoom = 16f;

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            }

            previousIndex = index;
        }
    }

    //Overlap items from bottom to top in recycler view
    public class OverlapDecoration extends RecyclerView.ItemDecoration {

        private final static int vertOverlap = -30;

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, vertOverlap, 0, 0);
        }
    }

    //Set date in month format in toolbar
    private void setDateInMonthFormat(Date date) {
        setDate(dateFormatMonth.format(date));
        if (mCompactCalendarView != null) {
            mCompactCalendarView.setCurrentDate(date);
        }
    }

    //Set current date in toolbar
    public void setCurrentDate(Date date) {
        setDate(dateFormat.format(date));
        if (mCompactCalendarView != null) {
            mCompactCalendarView.setCurrentDate(date);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    public void setDate(CharSequence title) {
        dateSelector.setText(title);
    }

    private void setPlacelineData() {
        sanitizeSegments.clear();
        placelineAdapter.setCurrentDate(selectedDate);
        placelineAdapter.notifyDataSetChanged();
        if (handler != null)
            handler.removeCallbacks(runnable);
        progressLayout.setVisibility(View.VISIBLE);
        previousIndex = -1;
        getPlacelineData();
    }

    //Get PlacelineData from Server
    private void getPlacelineData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(selectedDate);
        placelineManager.getPlacelineData(userID, date, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {

                progressLayout.setVisibility(View.GONE);

                if (response != null) {
                    placelineData = (PlacelineData) response.getResponseObject();
                    if (placelineData != null) {
                        if (mMap != null)
                            mMap.clear();
                        sanitizePlacelineData(placelineData);
                        if (sanitizeSegments.size() > 0) {
                            placelineStatus.setVisibility(View.GONE);
                            dashedLine.setVisibility(View.VISIBLE);
                            if (isFirstTime) {
                                animateMap(sanitizeSegments.size() - 1);
                                placelineRecyclerView.scrollToPosition(sanitizeSegments.size());
                                isFirstTime = false;
                            } else if (previousIndex == -1) {
                                animateMap(0);
                            } else {
                                int index = previousIndex;
                                previousIndex = -1;
                                animateMap(index);
                            }
                        } else {
                            placelineStatus.setVisibility(View.VISIBLE);
                            dashedLine.setVisibility(View.GONE);
                        }

                    }
                }
                handler.postDelayed(runnable, FETCH_TIME);
            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                progressLayout.setVisibility(View.GONE);
                if (sanitizeSegments == null || sanitizeSegments.size() == 0)
                    placelineStatus.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sanitizePlacelineData(PlacelineData placelineData) {
        List<Segment> segmentList = sanitizeSegments(placelineData.getSegmentList());
        sanitizeSegments.clear();
        sanitizeSegments.addAll(addMissingSegment(segmentList));
        placelineAdapter.setCurrentDate(selectedDate);
        placelineAdapter.notifyDataSetChanged();
    }

    //Sanitize segment list to remove any invalid segment
    private List<Segment> sanitizeSegments(List<Segment> segmentList) {
        List<Segment> sanitizeSegments = new ArrayList<>();
        List<Segment> cleanedSegments = cleanSegmentsNotEnded(segmentList);

        int size = cleanedSegments.size();
        for (int i = 0; i < size; i++) {

            Segment segment = cleanedSegments.get(i);

            if (this.isValidSegment(segment, i, cleanedSegments)) {
                Segment displaySegment = getDisplaySegment(segment, i, cleanedSegments);
                sanitizeSegments.add(displaySegment);
            }
        }

        return sanitizeSegments;
    }

    //Add missing segment between two segments if there is difference of 1 minute between
    // endtime of former segment and start time of latter segment
    private List<Segment> addMissingSegment(List<Segment> segmentList) {

        List<Segment> result = new ArrayList<>();
        int size = segmentList.size();
        if (size > 0)
            result.add(segmentList.get(0));

        for (int i = 1; i < size; i++) {

            Segment previousSegment = (i > 0) ? segmentList.get(i - 1) : null;
            Segment currentSegment = segmentList.get(i);
            long diff = currentSegment.getStartedAt().getTime() - previousSegment.getEndedAt().getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

            if (minutes > 0) {
                segmentList.add(i, createNoInfoSegment(previousSegment, currentSegment, TimeUnit.MILLISECONDS.toSeconds(diff)));
                size++;
                i++;
            }
        }

        return segmentList;
    }

    //Create missing segment of type No information
    private Segment createNoInfoSegment(Segment previous, Segment currentSegment, long duration) {
        Segment segment = new Segment();
        segment.setStartedAt(previous.getEndedAt());
        segment.setEndedAt(currentSegment.getStartedAt());
        segment.setType(Segment.SEGMENT_TYPE_NO_INFORMATION);
        segment.setDuration((double) duration);
        return segment;
    }

    //Sanitize segment list to remove those segment whose endtime is null except the last segment
    private List<Segment> cleanSegmentsNotEnded(List<Segment> segmentList) {
        List<Segment> cleanedSegments = new ArrayList<>();
        int size = segmentList.size();
        for (int i = 0; i < size; i++) {
            Segment segment = segmentList.get(i);
            boolean first = (i == 0);
            boolean last = (i == size - 1);
            if (first) {
                if (segment.getStartedAt() != null && segment.getEndedAt() != null) {
                    cleanedSegments.add(segment);
                } else if (segment.getStartedAt() != null && last && placelineData.getLastHeartbeatAt() != null) {
                    cleanedSegments.add(segment);
                }
            } else if (last) {
                if (segment.getStartedAt() != null && last && placelineData.getLastHeartbeatAt() != null) {
                    cleanedSegments.add(segment);
                }
            } else if (segment.getStartedAt() != null && segment.getEndedAt() != null) {
                cleanedSegments.add(segment);
            }
        }
        return cleanedSegments;
    }

    private Segment getDisplaySegment(Segment segment, int index, List<Segment> segments) {
        boolean last = (index == segments.size() - 1);

        if (last && segment.getEndedAt() == null) {
            if (placelineData.getLastHeartbeatAt() != null)
                segment.setEndedAt(placelineData.getLastHeartbeatAt());
            else {
                segment.setEndedAt(new Date());
            }
        }
        return segment;
    }

    //Check for valid segment
    private boolean isValidSegment(Segment segment, int index, List<Segment> segments) {
        boolean first = (index == 0);
        boolean last = (index == segments.size() - 1);
        Segment previous = (index > 0) ? segments.get(index - 1) : null;

        if (first) {
            if (segment.getStartedAt() != null && segment.getEndedAt() != null) {
                return true;
            }
            return (segment.getStartedAt() != null && last && placelineData.getLastHeartbeatAt() != null);

        } else if (last) {
            if (segment.getStartedAt() != null && last && placelineData.getLastHeartbeatAt() != null) {
                return isAfterPrevSegment(previous.getEndedAt(), segment.getStartedAt());
            }
            return (segment.getStartedAt() != null && isAfterPrevSegment(previous.getEndedAt(), segment.getStartedAt()));
        } else {
            if (segment.getStartedAt() != null && segment.getEndedAt() != null) {
                return isAfterPrevSegment(previous.getEndedAt(), segment.getStartedAt());
            }
            return false;
        }
    }

    //Check if end time of first segment is greater than start time of next segment
    private boolean isAfterPrevSegment(Date date1, Date date2) {

        if (date1.getTime() - date2.getTime() <= 1000)
            return true;
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_1));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        //mMap.moveCamera();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideCalendar();
            }
        });
    }

    private void showCalendar() {
        setDateInMonthFormat(selectedDate);
        ViewCompat.animate(arrow).rotation(-90).start();
        mAppBarLayout.setExpanded(true, true);
        isExpanded = true;
    }

    private void hideCalendar() {
        setCurrentDate(selectedDate);
        mAppBarLayout.setExpanded(false, true);
        ViewCompat.animate(arrow).rotation(0).start();
        isExpanded = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (ActionManager.getSharedManager(this).getHyperTrackAction() != null)
            return false;

        getMenuInflater().inflate(R.menu.menu_placeline, menu);
        MenuItem menuItem = menu.findItem(R.id.tracking_toogle);
        if (SharedPreferenceManager.isTrackingON()) {
            menuItem.setTitle(getString(R.string.stop_tracking));
            if (!HyperTrack.isTracking())
                startHyperTrackTracking(false);
        } else {
            menuItem.setTitle(getString(R.string.start_tracking));
            if (HyperTrack.isTracking())
                stopHyperTrackTracking();
        }

        // Hide menu items if user is on an Action
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tracking_toogle:
                // Check if clicked item is Resume tracking
                if (getString(R.string.start_tracking).equalsIgnoreCase(item.getTitle().toString())) {
                    // Start Tracking the user
                    if (!HyperTrack.isTracking())
                        startHyperTrackTracking(true);
                    item.setTitle(R.string.stop_tracking);

                } else {
                    // Stop Tracking the user
                    if (HyperTrack.isTracking())
                        stopHyperTrackTracking();
                    item.setTitle(R.string.start_tracking);
                }
                return true;
            case R.id.edit_profile:
                startActivity(new Intent(this, Profile.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startHyperTrackTracking(final boolean byUser) {
        // HACK: Check if user is tracking currently or not
        // Only for existing users because Permission and Location Settings have been checked here
        if (!HyperTrack.isTracking()) {
            HyperTrack.startTracking(null);
            if (byUser) {
                SharedPreferenceManager.setTrackingON();
                supportInvalidateOptionsMenu();
            }

        } else if (byUser) {
            SharedPreferenceManager.setTrackingON();
            supportInvalidateOptionsMenu();
        }
        getPlacelineData();
    }

    private void stopHyperTrackTracking() {
        HyperTrack.stopTracking();
        SharedPreferenceManager.setTrackingOFF();
        supportInvalidateOptionsMenu();
        getPlacelineData();
    }

    @Override
    public void onBackPressed() {

        if (isExpanded) {
            hideCalendar();
            return;
        }
        super.onBackPressed();
    }

}
