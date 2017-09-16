package io.hypertrack.sendeta.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hypertrack.lib.HyperTrack;
import com.hypertrack.lib.callbacks.HyperTrackCallback;
import com.hypertrack.lib.internal.consumer.view.RippleView;
import com.hypertrack.lib.internal.transmitter.models.ActivitySegment;
import com.hypertrack.lib.models.ErrorResponse;
import com.hypertrack.lib.models.SuccessResponse;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

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

public class FeedbackPlaceline extends AppCompatActivity {

    private static final String TAG = FeedbackPlaceline.class.getSimpleName();
    public RecyclerView recyclerView;
    private CalendarView calendarView;
    private TextView date;
    private ImageView arrow;

    private FeedbackPlacelineAdapter feedbackPlacelineAdapter;

    private AppBarLayout appBarLayout;

    boolean isExpanded = false;
    private Date selectedDate;

    private List<ActivitySegment> activitySegmentList = new ArrayList<>();

    private ProgressDialog progressDialog;

    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, /*Locale.getDefault()*/Locale.ENGLISH);
    private Paint p = new Paint();

    private ItemTouchHelper itemTouchHelper;

    private LinkedHashMap<String, String> activityFeedbackLookupIds = new LinkedHashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_feedback_placeline);
        initUI();
        selectedDate = new Date();
        setCurrentDate(selectedDate);
        calendarView.setMaxDate(System.currentTimeMillis());
    }

    private void initUI() {
        initCalendarView();
        initProgressDialog();
        recyclerView = (RecyclerView) findViewById(R.id.feedback_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        feedbackPlacelineAdapter = new FeedbackPlacelineAdapter(activitySegmentList, this, new FeedbackPlacelineAdapter.ItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
                Intent intent = new Intent(FeedbackPlaceline.this, EditFeedbackItem.class);
                intent.putExtra("activity", activitySegmentList.get(position));
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(feedbackPlacelineAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityFeedbackLookupIds = SharedPreferenceManager.getActivityFeedbackLookupId();
        getPlacelineData();
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            //awesome code when user grabs recycler card to reorder
            return false;
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            Log.d(TAG, "clearView: ");
            super.clearView(recyclerView, viewHolder);
            //awesome code to run when user drops card and completes reorder
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            if (direction == ItemTouchHelper.RIGHT) {
                Log.d(TAG, "onSwiped: right");
                ActivitySegment activitySegment = activitySegmentList.get(viewHolder.getAdapterPosition());
                ActivityFeedbackModel feedback = new ActivityFeedbackModel(activitySegment, ActivityFeedbackModel.ACTIVITY_ACCURATE);
                sendActivityFeedback(feedback);
                activitySegment.setFeedbackType(ActivityFeedbackModel.ACTIVITY_ACCURATE);

            } else if (direction == ItemTouchHelper.LEFT) {
                Log.d(TAG, "onSwiped: Left");
                ActivitySegment activitySegment = activitySegmentList.get(viewHolder.getAdapterPosition());
                ActivityFeedbackModel feedback = new ActivityFeedbackModel(activitySegment, ActivityFeedbackModel.ACTIVITY_DELETED);
                sendActivityFeedback(feedback);
                activitySegment.setFeedbackType(ActivityFeedbackModel.ACTIVITY_DELETED);
            }
            itemTouchHelper.attachToRecyclerView(null);
            itemTouchHelper.attachToRecyclerView(recyclerView);
            feedbackPlacelineAdapter.notifyDataSetChanged();

        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            Bitmap icon;
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;

                if (dX > 0) {
                    p.setColor(Color.parseColor("#388E3C"));
                    RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                    c.drawRect(background, p);
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_accurate_activity);
                    RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                    c.drawBitmap(icon, null, icon_dest, p);
                } else {
                    p.setColor(Color.parseColor("#D32F2F"));
                    RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    c.drawRect(background, p);
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_segment);
                    RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                    c.drawBitmap(icon, null, icon_dest, p);
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    };

    private void sendActivityFeedback(final ActivityFeedbackModel activityFeedbackModel) {
        HyperTrackService sendActivityFeedback = HyperTrackServiceGenerator.createService(HyperTrackService.class, this);
        Call<ActivityFeedbackModel> call = sendActivityFeedback.sendActivityFeedback(activityFeedbackModel);
        call.enqueue(new Callback<ActivityFeedbackModel>() {
            @Override
            public void onResponse(Call<ActivityFeedbackModel> call, Response<ActivityFeedbackModel> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FeedbackPlaceline.this, "Feedback Submitted", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onResponse:Activity Feedback Success ");
                    SharedPreferenceManager.setActivityFeedbackLookupId(activityFeedbackModel.getLookupId(), activityFeedbackModel.getFeedbackType());
                }
            }

            @Override
            public void onFailure(Call<ActivityFeedbackModel> call, Throwable t) {
                Log.d(TAG, "onFailure: Activity Feedback Failure ");
            }
        });
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(true);
    }

    private void showProgress() {
        if (progressDialog == null)
            initProgressDialog();
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null)
            progressDialog.cancel();
    }

    private void initCalendarView() {
        calendarView = (CalendarView) findViewById(R.id.calendar_view);
        calendarView.setOnDateChangeListener(dateChangeListener);

        date = (TextView) findViewById(R.id.date_selector);
        arrow = (ImageView) findViewById(R.id.arrow);

        RippleView dateSelectorLayout = (RippleView) findViewById(R.id.date_selector_layout);
        dateSelectorLayout.setOnClickListener(dateSelectorOnClick);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        appBarLayout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (isExpanded) {
                            toggleCalendarView();
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    private CalendarView.OnDateChangeListener dateChangeListener = new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
            try {
                DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
                Date date = dateFormat1.parse(dayOfMonth + "-" + (month + 1) + "-" + year);
                selectedDate = date;
                setDate(dateFormat.format(date));
                dateSelectorOnClick.onClick(null);
                activityFeedbackLookupIds = SharedPreferenceManager.getActivityFeedbackLookupId();
                getPlacelineData();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };

    private View.OnClickListener dateSelectorOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            toggleCalendarView();
        }
    };

    private void toggleCalendarView() {
        ViewCompat.animate(arrow).rotation(isExpanded ? 0 : -180).start();
        appBarLayout.setExpanded(!isExpanded, true);
        isExpanded = !isExpanded;
        if (isExpanded)
            calendarView.setDate(selectedDate.getTime(), false, false);
    }

    public void setDate(CharSequence title) {
        date.setText(title);
    }

    //Set current date in toolbar
    public void setCurrentDate(Date date) {
        setDate(dateFormat.format(date));
    }

    //Get PlacelineData from Server
    private void getPlacelineData() {
        showProgress();
        HyperTrack.getActivitySegment(selectedDate, new HyperTrackCallback() {
            @Override
            public void onSuccess(@NonNull SuccessResponse response) {

                hideProgress();

                if (response.getResponseObject() == null) {
                    Toast.makeText(FeedbackPlaceline.this, "No Activity", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ActivitySegment> activitySegments = (List<ActivitySegment>) response.getResponseObject();

                if (activitySegments.isEmpty()) {
                    Toast.makeText(FeedbackPlaceline.this, "No Activity", Toast.LENGTH_SHORT).show();
                    return;
                }

                activitySegmentList.clear();
                activitySegmentList.addAll(activitySegments);
                for (ActivitySegment activitySegment : activitySegmentList) {
                    activitySegment.setFeedbackType(activityFeedbackLookupIds.get(activitySegment.getLookupId()));
                }
                feedbackPlacelineAdapter.notifyDataSetChanged();

            }

            @Override
            public void onError(@NonNull ErrorResponse errorResponse) {
                hideProgress();
                activitySegmentList.clear();
                feedbackPlacelineAdapter.notifyDataSetChanged();
                Toast.makeText(FeedbackPlaceline.this, errorResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
