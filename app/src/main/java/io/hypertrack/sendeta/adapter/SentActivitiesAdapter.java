package io.hypertrack.sendeta.adapter;

import android.content.Context;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;

import io.hypertrack.lib.common.model.HTPlace;
import io.hypertrack.lib.common.model.HTTask;
import io.hypertrack.lib.common.model.HTTaskDisplay;
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

    public SentActivitiesAdapter(Context mContext, ArrayList<UserActivity> userActivities, UserActivitiesOnClickListener listener) {
        this.mContext = mContext;
        this.userActivities = userActivities != null ? userActivities : new ArrayList<UserActivity>();
        this.listener = listener;
    }

    public void setUserActivities(ArrayList<UserActivity> userActivities) {
        this.userActivities = userActivities;
        notifyDataSetChanged();
    }

    @Override
    public SentActivitiesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user_activity, parent, false);
        return new SentActivitiesViewHolder(view);
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

                    if (holder.mapSummaryView != null) {
                        holder.mapSummaryView.onCreate(null);
                        holder.mapSummaryView.onResume();
                        holder.mapSummaryView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                MapsInitializer.initialize(mContext);
                                holder.map = googleMap;
//                                if (currentFriend.getLatitude() != null && currentFriend.getLongitude() != null) {
//                                    LatLng coordinates = new LatLng(currentFriend.getLatitude(), currentFriend.getLongitude());
//                                    googleMap.addMarker(new MarkerOptions().position(coordinates)
//                                            .title(currentFriend.getNickname()));
//                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
//                                }
                            }
                        });
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

    /**
     * Displays a {@link } on a
     * {@link com.google.android.gms.maps.GoogleMap}.
     * Adds a marker and centers the camera on the NamedLocation with the normal map type.
     */
    private static void setMapLocation(GoogleMap map, UserActivity activity) {
        // Add a marker for this item and set the camera
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(data.location, 13f));
//        map.addMarker(new MarkerOptions().position(data.location));

        // Set the map type back to normal.
//        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public int getItemCount() {
        return userActivities.size();
    }

    @Override
    public void onViewRecycled(SentActivitiesViewHolder holder) {
        super.onViewRecycled(holder);
        // Cleanup MapView here?
        if (holder.map != null) {
            holder.map.clear();
            holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
        }
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
//            mapSummaryView = (MapView) itemView.findViewById(R.id.item_user_activity_map_summary);
//            initializeMapView();

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

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(mContext);
            map = googleMap;
//            NamedLocation data = (NamedLocation) mapSummaryView.getTag();
//            if (data != null) {
//                setMapLocation(map, data);
//            }
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
