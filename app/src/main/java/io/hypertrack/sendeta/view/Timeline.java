package io.hypertrack.sendeta.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.common.util.TextUtils;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Segment;
import io.hypertrack.sendeta.model.UserEvent;
import io.hypertrack.sendeta.model.UserTimelineData;
import io.hypertrack.sendeta.store.TimelineManager;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class Timeline extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = Timeline.class.getSimpleName();
    private RecyclerView timelineRecyclerView;
    private TimelineAdapter timelineAdapter;
    private List<Segment> sanitizeSegments = new ArrayList<>();
    private TimelineManager timelineManager;
    private SlidingUpPanelLayout slidingPaneLayout;
    private SupportMapFragment supportMapFragment;
    private CompactCalendarView mCompactCalendarView;
    private AppBarLayout mAppBarLayout;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", /*Locale.getDefault()*/Locale.ENGLISH);
    private SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
    private Date selectedDate;
    private  boolean isExpanded = false;
    private ImageView arrow;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private UserTimelineData userTimelineData;
    private TextView userName,timelineStatus;
    private ProgressBar progressBar;
    private RelativeLayout topBar;
    private CardView topBarCardView;
    private String userID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        userID = getIntent().getStringExtra("user_id");
        if(TextUtils.isEmpty(userID)){
            userID = HyperTrack.getUserId();
        }

        selectedDate = new Date();

        initUI();

        timelineManager = TimelineManager.getTimelineManager(this);

        setTimelineData();
    }

    private void initUI(){
        //  topBar= (RelativeLayout) findViewById(R.id.top_bar);
        slidingPaneLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_panel_layout);
        slidingPaneLayout.setAnchorPoint(0.4f);
        slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
        SlideListener slideListener = new SlideListener();
        slidingPaneLayout.addPanelSlideListener(slideListener);
        slidingPaneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
            }
        });

        topBarCardView = (CardView) findViewById(R.id.top_bar_card_view);

        //Set Panel Height when view is topBarCardView has been drawn on scree.
        topBarCardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                topBarCardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Log.d(TAG, "initUI: "+ topBarCardView.getHeight()+" ,  "+ topBarCardView.getMeasuredHeight()+"  , "+ topBarCardView.getMeasuredHeightAndState());
                slidingPaneLayout.setPanelHeight(topBarCardView.getHeight() - 8);
            }
        });

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        supportMapFragment.getMapAsync(this);

        userName = (TextView) findViewById(R.id.user_name);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        timelineStatus = (TextView) findViewById(R.id.timeline_status);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbarTitle = (TextView) findViewById(R.id.toolbar_title_text);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the CompactCalendarView
        mCompactCalendarView = (CompactCalendarView) findViewById(R.id.compactcalendar_view);

        mCompactCalendarView.setLocale(TimeZone.getDefault(), Locale.ENGLISH);

        mCompactCalendarView.setShouldDrawDaysHeader(true);

        mCompactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                setTitle(dateFormat.format(dateClicked));
                selectedDate  = dateClicked;
                hideCalendar();
                setTimelineData();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                setDateFormatMonth(firstDayOfNewMonth);
            }
        });

        timelineRecyclerView = (RecyclerView) findViewById(R.id.timeline_recycler_view);
        slidingPaneLayout.setScrollableView(timelineRecyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        timelineRecyclerView.setLayoutManager(linearLayoutManager);
        timelineAdapter = new TimelineAdapter(sanitizeSegments,this);
        timelineRecyclerView.setAdapter(timelineAdapter);

    }

    private void setDateFormatMonth(Date date){
        setTitle(dateFormatMonth.format(date));
        if (mCompactCalendarView != null) {
            mCompactCalendarView.setCurrentDate(date);
        }
    }

    public void setCurrentDate(Date date) {

        setTitle(dateFormat.format(date));
        if (mCompactCalendarView != null) {
            mCompactCalendarView.setCurrentDate(date);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void setTitle(CharSequence title) {
        toolbar.setTitle(title);
        //toolbarTitle.setText(title);
    }

    private void setTimelineData(){
        timelineStatus.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        sanitizeSegments.clear();
        timelineAdapter.notifyDataSetChanged();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(selectedDate);

        timelineManager.getTimelineData(userID, date, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {
                progressBar.setVisibility(View.GONE);
                timelineStatus.setVisibility(View.VISIBLE);
                timelineStatus.setText("No Placeline Activity");
                if(response != null){

                    userTimelineData = (UserTimelineData) response.getResponseObject();
                    if(userTimelineData != null){
                        userName.setText(userTimelineData.getName());
                        sanitizeTimelineData(userTimelineData);
                        if(userTimelineData.getSegmentList().size()>0){
                            timelineStatus.setText("Placeline Activity");
                            slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
                        else{
                            slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    }
                }

            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                progressBar.setVisibility(View.GONE);
                timelineStatus.setText("No Placeline Activity");
                timelineStatus.setVisibility(View.VISIBLE);
            }
        });

    }

    private void sanitizeTimelineData(UserTimelineData userTimelineData){
        List<Segment> segmentList = sanitizeSegments(userTimelineData.getSegmentList());
        addMissingSegment(segmentList);
        sanitizeSegments.clear();
        sanitizeSegments.addAll(addMissingSegment(segmentList));
        timelineAdapter.notifyDataSetChanged();
    }

    private List<Segment> sanitizeSegments(List<Segment> segmentList){
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

    private List<Segment> addMissingSegment(List<Segment> segmentList){

        List<Segment> result = new ArrayList<>();
        int size = segmentList.size();
        result.add(segmentList.get(0));

        for(int i = 1 ; i < size ; i++ ){

            Segment previousSegment = (i > 0) ? segmentList.get(i-1) : null ;
            Segment currentSegment = segmentList.get(i);
            long diff = currentSegment.getStartedAt().getTime() - previousSegment.getEndedAt().getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

            if(minutes > 0){
                segmentList.add(i,createNoInfoSegment(previousSegment,currentSegment,TimeUnit.MILLISECONDS.toSeconds(diff)));
                size++;
                i++;
            }
        }

        return segmentList;
    }

    private Segment createNoInfoSegment(Segment previous, Segment currentSegment,long duration){
        Segment segment = new Segment();
        segment.setStartedAt(previous.getEndedAt());
        segment.setEndedAt(currentSegment.getStartedAt());
        segment.setType(Segment.SEGMENT_TYPE_NO_INFORMATION);
        segment.setDuration((double) duration);
        return segment;
    }

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
                } else if (segment.getStartedAt() != null && last && userTimelineData.getLastHeartbeatAt() != null ) {
                    cleanedSegments.add(segment);
                }
            } else if (last) {
                if (segment.getStartedAt() != null && last && userTimelineData.getLastHeartbeatAt() != null ) {
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
            if(userTimelineData.getLastHeartbeatAt() != null)
                segment.setEndedAt(userTimelineData.getLastHeartbeatAt());
            else {
                segment.setEndedAt(new Date());
            }
        }
        return segment;
    }

    private boolean isValidSegment(Segment segment,int  index,List<Segment> segments) {
        boolean first = (index == 0);
        boolean last = (index == segments.size() - 1);
        Segment previous = (index > 0) ? segments.get(index - 1) : null;

        if (first) {
            if (segment.getStartedAt() != null && segment.getEndedAt() != null) {
                return true;
            }
            return !!(segment.getStartedAt() != null && last && userTimelineData.getLastHeartbeatAt() != null);

        } else if (last) {
            if (segment.getStartedAt() != null && last && userTimelineData.getLastHeartbeatAt() != null) {
                return this.isAfterPrevSegment(previous.getEndedAt(), segment.getStartedAt());
            }
            return (segment.getStartedAt() != null && this.isAfterPrevSegment(previous.getEndedAt(), segment.getStartedAt()));
        } else {
            if (segment.getStartedAt() != null && segment.getEndedAt() != null) {
                //let isGreaterThanMinute = (moment(segment.ended_at).diff(moment(segment.started_at), 'minutes') > 0);
                return this.isAfterPrevSegment(previous.getEndedAt(), segment.getStartedAt());
            }
            return false;
        }
    }



    private boolean isAfterPrevSegment(Date date1,Date date2) {

        if(date1.compareTo(date2) <= 0){
            return true;
        }
        return  false;
    }



    private void sanitizeActions(List<Action> actionList){

    }

    private void sanitizeEvents(List<UserEvent> userEventList){

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        // Set current date to today
        selectedDate = new Date();
        setCurrentDate(selectedDate);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.choose_date) {
            if (isExpanded) {
                hideCalendar();
            } else {
                showCalendar();
            }
            return true;
        }
        else if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCalendar(){
        setDateFormatMonth(selectedDate);
        ViewCompat.animate(arrow).rotation(180).start();
        mAppBarLayout.setExpanded(true, true);
        isExpanded = true;

    }


    private void hideCalendar(){
        setCurrentDate(selectedDate);
        ViewCompat.animate(arrow).rotation(0).start();
        mAppBarLayout.setExpanded(false, true);
        isExpanded = false;
    }

    class SlideListener implements SlidingUpPanelLayout.PanelSlideListener {

        @Override
        public void onPanelSlide(View panel, float slideOffset) {

        }

        @Override
        public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
           /* if(mAppBarLayout !=null ) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    ObjectAnimator objectAnimator = new ObjectAnimator.ofFloat(mAppBarLayout,View.TRANSLATION_Y,-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).;

                    mAppBarLayout.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                } else {
                    mAppBarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();

                }
            }*/
        }
    }

    @Override
    public void onBackPressed() {

        if(isExpanded){
            hideCalendar();
            return;
        }
        else if(slidingPaneLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingPaneLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
            return;
        }
        super.onBackPressed();
    }
}
