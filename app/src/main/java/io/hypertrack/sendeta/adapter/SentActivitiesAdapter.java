package io.hypertrack.sendeta.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.hypertrack.lib.common.model.HTPlace;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.common.model.HTTaskDisplay;
import io.hypertrack.lib.consumer.utils.HTMapUtils;
import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.callback.UserActivitiesOnClickListener;
import io.hypertrack.sendeta.model.UserActivity;
import io.hypertrack.sendeta.util.HyperTrackTaskUtils;

/**
 * Created by piyush on 03/09/16.
 */
public class SentActivitiesAdapter extends RecyclerView.Adapter<SentActivitiesAdapter.SentActivitiesViewHolder> {

    private Context mContext;
    private ArrayList<UserActivity> userActivities;
    private UserActivitiesOnClickListener listener;
    private boolean showMapSummary = false;

    protected HashSet<MapView> mMapViews = new HashSet<>();

    public SentActivitiesAdapter(Context mContext, ArrayList<UserActivity> userActivities, UserActivitiesOnClickListener listener) {
        this.mContext = mContext;
        this.userActivities = userActivities != null ? userActivities : new ArrayList<UserActivity>();
        this.listener = listener;
    }

    public SentActivitiesAdapter(Context mContext, ArrayList<UserActivity> userActivities, UserActivitiesOnClickListener listener, boolean showMapSummary) {
        this.mContext = mContext;
        this.userActivities = userActivities != null ? userActivities : new ArrayList<UserActivity>();
        this.listener = listener;
        this.showMapSummary = showMapSummary;
    }

    public void setUserActivities(ArrayList<UserActivity> userActivities) {
        this.userActivities = userActivities;
        notifyDataSetChanged();
    }

    @Override
    public SentActivitiesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_activity, parent, false);
        SentActivitiesViewHolder holder = new SentActivitiesViewHolder(view);

        if (showMapSummary) {
            mMapViews.add(holder.mapSummaryView);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(final SentActivitiesViewHolder holder, int position) {
        int size = mContext.getResources().getDimensionPixelSize(R.dimen.icon_size);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
        params.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.margin_xxxhigh),
                mContext.getResources().getDimensionPixelSize(R.dimen.margin_low), 0, 0);
        holder.activityLayoutMainIcon.setLayoutParams(params);

        UserActivity activity = userActivities.get(position);
        if (activity != null) {

            HTTask task = activity.getTaskDetails();
            if (task != null) {

                if (task.getTaskDisplay() != null) {
                    Integer resId = HyperTrackTaskUtils.getTaskDisplayStatus(task.getTaskDisplay());
                    if (resId != null) {
                        holder.activityTitle.setText(mContext.getString(resId));
                        holder.activityTitle.setVisibility(View.VISIBLE);
                    } else if (!TextUtils.isEmpty(task.getTaskDisplay().getStatusText())) {
                        holder.activityTitle.setText(task.getTaskDisplay().getStatusText());
                        holder.activityTitle.setVisibility(View.VISIBLE);
                    }
                }

                if (!TextUtils.isEmpty(task.getStatus())) {
                    String taskStatus = task.getStatus();
                    switch (taskStatus) {
                        case HTTask.TASK_STATUS_NOT_STARTED:
                        case HTTask.TASK_STATUS_DISPATCHING:
                        case HTTask.TASK_STATUS_DRIVER_ON_THE_WAY:
                        case HTTask.TASK_STATUS_DRIVER_ARRIVING:
                        case HTTask.TASK_STATUS_DRIVER_ARRIVED:
                        case HTTask.TASK_STATUS_NO_LOCATION:
                        case HTTask.TASK_STATUS_LOCATION_LOST:
                        case HTTask.TASK_STATUS_CONNECTION_LOST:
                        case HTTask.TASK_STATUS_COMPLETED:
                        default:
                            holder.activityLayoutMainIcon.setImageResource(R.drawable.ic_sent_activity_icon);
                            holder.activityLayoutMainIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.app_background_theme_light));
                            break;

                        case HTTask.TASK_STATUS_CANCELED:
                        case HTTask.TASK_STATUS_ABORTED:
                        case HTTask.TASK_STATUS_SUSPENDED:
                            holder.activityLayoutMainIcon.setImageResource(R.drawable.ic_sent_activity_icon);
                            holder.activityLayoutMainIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.app_background_theme));
                            break;
                    }
                }

                HTPlace destination = task.getDestination();
                if (destination != null) {
                    if (!TextUtils.isEmpty(destination.getAddress())) {
                        holder.activityEndAddress.setText(destination.getAddress());
                        holder.endAddressLayoutIcon.setVisibility(View.VISIBLE);
                        holder.activityEndAddressLayout.setVisibility(View.VISIBLE);
                    }
                }

                if (activity.isInProcess()) {
                    HTTaskDisplay taskDisplay = task.getTaskDisplay();

                    if (taskDisplay != null) {
                        String formattedTime = HyperTrackTaskUtils.getFormattedTimeString(mContext,
                                HyperTrackTaskUtils.getTaskDisplayETA(taskDisplay));
                        if (!TextUtils.isEmpty(formattedTime)) {
                            holder.activitySubtitle.setText(formattedTime + " away");
                            holder.activitySubtitleLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(0, 0, 0, 0);
                    holder.activityAddressIconsLayout.setLayoutParams(layoutParams);

                } else {

                    if (showMapSummary) {
                        holder.setMapSummaryView();
                    }

                    String formattedSubtitle = HyperTrackTaskUtils.getFormattedTaskDurationAndDistance(mContext, task);
                    if (!TextUtils.isEmpty(formattedSubtitle)) {
                        holder.activitySubtitle.setText(formattedSubtitle);
                        holder.activitySubtitleLayout.setVisibility(View.VISIBLE);
                    }

                    String formattedDate = HyperTrackTaskUtils.getTaskDateString(task);
                    if (!TextUtils.isEmpty(formattedDate)) {
                        holder.activityDate.setText(formattedDate);
                        holder.activityDate.setVisibility(View.VISIBLE);
                    }

                    String startLocationString =
                            task.getStartLocation() != null ? task.getStartLocation().getDisplayString() : null;
                    if (!TextUtils.isEmpty(startLocationString)) {
                        holder.activityStartAddress.setText(startLocationString);
                        holder.startAddressLayoutIcon.setVisibility(View.VISIBLE);
                        holder.activityStartAddressLayout.setVisibility(View.VISIBLE);

                        if (holder.activityEndAddressLayout.getVisibility() == View.VISIBLE) {
                            holder.startEndIconVerticalSeparator.setVisibility(View.VISIBLE);
                            holder.startEndAddressHorizontalSeparator.setVisibility(View.VISIBLE);
                        }
                    }

                    if (!TextUtils.isEmpty(task.getTaskStartTimeDisplayString())) {
                        holder.activityStartTime.setText(task.getTaskStartTimeDisplayString());
                        holder.activityStartTime.setVisibility(View.VISIBLE);
                    }

                    if (!TextUtils.isEmpty(task.getTaskEndTimeDisplayString())) {
                        holder.activityEndTime.setText(task.getTaskEndTimeDisplayString());
                        holder.activityEndTime.setVisibility(View.VISIBLE);
                    }

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    int margin = mContext.getResources().getDimensionPixelSize(R.dimen.margin_very_low);
                    layoutParams.setMargins(0, margin, 0, margin);
                    holder.activityAddressIconsLayout.setLayoutParams(layoutParams);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return userActivities.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }

    public class SentActivitiesViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        private TextView activityTitle, activitySubtitle, activityDate, activityStartAddress,
                activityEndAddress, activityStartTime, activityEndTime;
        private ImageView activityLayoutMainIcon, startAddressLayoutIcon, endAddressLayoutIcon;
        private View startEndIconVerticalSeparator, startEndAddressHorizontalSeparator;
        private LinearLayout activitySubtitleLayout, activityAddressIconsLayout,
                activityStartAddressLayout, activityEndAddressLayout;
        private MapView mapSummaryView;
        private GoogleMap map;

        public SentActivitiesViewHolder(View itemView) {
            super(itemView);
            if (showMapSummary) {
                mapSummaryView = (MapView) itemView.findViewById(R.id.item_user_activity_map_summary);
                initializeMapView();
            }

            activityTitle = (TextView) itemView.findViewById(R.id.item_user_activity_title);
            activitySubtitle = (TextView) itemView.findViewById(R.id.item_user_activity_subtitle_text);
            activityDate = (TextView) itemView.findViewById(R.id.item_user_activity_date);
            activityStartAddress = (TextView) itemView.findViewById(R.id.item_user_activity_start_address);
            activityEndAddress = (TextView) itemView.findViewById(R.id.item_user_activity_end_address);
            activityStartTime = (TextView) itemView.findViewById(R.id.item_user_activity_start_time);
            activityEndTime = (TextView) itemView.findViewById(R.id.item_user_activity_end_time);

            activityLayoutMainIcon = (ImageView) itemView.findViewById(R.id.item_user_activity_icon);
            startAddressLayoutIcon = (ImageView) itemView.findViewById(R.id.item_user_activity_start_icon);
            endAddressLayoutIcon = (ImageView) itemView.findViewById(R.id.item_user_activity_end_icon);

            startEndAddressHorizontalSeparator = itemView.findViewById(R.id.item_user_activity_horizontal_separator);
            startEndIconVerticalSeparator = itemView.findViewById(R.id.item_user_activity_vertical_separator);

            activitySubtitleLayout = (LinearLayout) itemView.findViewById(R.id.item_user_activity_subtitle_layout);
            activityAddressIconsLayout = (LinearLayout) itemView.findViewById(R.id.item_user_activity_icons_layout);
            activityStartAddressLayout = (LinearLayout) itemView.findViewById(R.id.item_user_activity_start_address_layout);
            activityEndAddressLayout = (LinearLayout) itemView.findViewById(R.id.item_user_activity_end_address_layout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    // Check if a valid list item has been clicked
                    if (listener != null && position != RecyclerView.NO_POSITION &&
                            userActivities.size() > position && userActivities.get(position) != null) {

                        // Check if the clicked activity is InProcess or not
                        if (userActivities.get(position).isInProcess()) {
                            listener.OnInProcessActivityClicked(userActivities.get(position));
                        } else {
                            listener.OnHistoryActivityClicked(userActivities.get(position));
                        }
                    }
                }
            });
        }

        public void setMapSummaryView() {
            if (map != null) {
                updateMapContents(map);
                mapSummaryView.setClickable(false);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

            MapsInitializer.initialize(mContext);

            map.getUiSettings().setMapToolbarEnabled(false);

            if (map != null) {
                updateMapContents(map);
                mapSummaryView.setClickable(false);
            }
        }

        protected void updateMapContents(GoogleMap map) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {

                if (userActivities.size() > getAdapterPosition() && userActivities.get(position) != null) {
                    UserActivity activity = userActivities.get(position);

                    if (activity != null && activity.getTaskDetails() != null) {
                        HTTask taskDetails = activity.getTaskDetails();

                        // Since the mapView is re-used, need to remove pre-existing mapView features.
                        map.clear();

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        int points = 0;

                        // Update the mapView feature data and camera position.
                        if (taskDetails.getDestination() != null && taskDetails.getDestination().getLocation() != null) {
                            double[] coordinates = taskDetails.getDestination().getLocation().getCoordinates();
                            LatLng destination = new LatLng(coordinates[1], coordinates[0]);

                            map.addMarker(new MarkerOptions().position(destination)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_task_summary_end_marker)));

                            builder.include(destination);
                            points++;
                        }

                        if (taskDetails.getStartLocation() != null) {
                            double[] coordinates = taskDetails.getStartLocation().getCoordinates();
                            LatLng source = new LatLng(coordinates[1], coordinates[0]);

                            map.addMarker(new MarkerOptions().position(source)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_task_summary_start_marker)));

                            builder.include(source);
                            points++;
                        }

                        String encodedPolyline = taskDetails.getEncodedPolyline();

                        if (!TextUtils.isEmpty(encodedPolyline)) {
                            List<LatLng> polyline = HTMapUtils.decode(encodedPolyline);
                            PolylineOptions options = new PolylineOptions().width(8).color(Color.parseColor("#0A61C2"));
                            options.addAll(polyline);

                            map.addPolyline(options);

                            for (LatLng latLng : polyline) {
                                builder.include(latLng);
                                points++;
                            }
                        }

                        mapSummaryView.setVisibility(View.VISIBLE);

                        CameraUpdate cameraUpdate = null;
                        if (points == 1) {
                            cameraUpdate = CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 13f);
                        } else if (points > 0){
                            cameraUpdate = CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 9);
                        }

                        if (cameraUpdate != null) {
                            map.moveCamera(cameraUpdate);
                            return;
                        }
                    }
                }
            }

            mapSummaryView.setVisibility(View.GONE);
        }

        /**
         * Initialises the MapView by calling its lifecycle methods.
         */
        public void initializeMapView() {
            if (mapSummaryView != null) {
                // Initialise the MapView
                mapSummaryView.onCreate(null);
                // Set the map ready callback to receive the GoogleMap object
                mapSummaryView.getMapAsync(this);
            }
        }
    }
}
