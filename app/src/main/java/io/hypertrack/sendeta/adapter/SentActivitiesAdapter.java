package io.hypertrack.sendeta.adapter;

import android.content.Context;
import android.graphics.Color;
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

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.callback.UserActivitiesOnClickListener;
import io.hypertrack.sendeta.model.UserActivityModel;

/**
 * Created by piyush on 03/09/16.
 */
public class SentActivitiesAdapter extends RecyclerView.Adapter<SentActivitiesAdapter.SentActivitiesViewHolder> {

    private Context mContext;
    private ArrayList<UserActivityModel> userActivities = new ArrayList<>();
    private UserActivitiesOnClickListener listener;
    private boolean showMapSummary = false;

    protected HashSet<MapView> mMapViews = new HashSet<>();

    public SentActivitiesAdapter(Context mContext, ArrayList<UserActivityModel> userActivities, UserActivitiesOnClickListener listener) {
        this.mContext = mContext;
        this.userActivities = userActivities != null ? userActivities : new ArrayList<UserActivityModel>();
        this.listener = listener;
    }

    public SentActivitiesAdapter(Context mContext, ArrayList<UserActivityModel> userActivities, UserActivitiesOnClickListener listener, boolean showMapSummary) {
        this.mContext = mContext;
        this.userActivities = userActivities != null ? userActivities : new ArrayList<UserActivityModel>();
        this.listener = listener;
        this.showMapSummary = showMapSummary;
    }

    public void setUserActivities(ArrayList<UserActivityModel> userActivities) {
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
        FrameLayout.LayoutParams parentLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        parentLayoutParams.setMargins(mContext.getResources().getDimensionPixelSize(R.dimen.margin_high), 0,
                mContext.getResources().getDimensionPixelSize(R.dimen.margin_high), 0);
        holder.activityParentLayout.setLayoutParams(parentLayoutParams);

        UserActivityModel activity = userActivities.get(position);
        if (activity != null) {

            if (!TextUtils.isEmpty(activity.getTitle())) {
                holder.activityTitle.setText(activity.getTitle());
                holder.activityTitle.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(activity.getSubtitle())) {
                holder.activitySubtitle.setText(activity.getSubtitle());
                holder.activitySubtitleLayout.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(activity.getDate())) {
                holder.activityDate.setText(activity.getDate());
                holder.activityDate.setVisibility(View.VISIBLE);
            }

            holder.activityLayoutMainIcon.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(activity.getEndAddress())) {
                holder.activityEndAddress.setText(activity.getEndAddress());
                holder.endAddressLayoutIcon.setVisibility(View.VISIBLE);
                holder.activityEndAddressLayout.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(activity.getEndTime())) {
                holder.activityEndTime.setText(activity.getEndTime());
                holder.activityEndTime.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(activity.getStartAddress())) {
                holder.activityStartAddress.setText(activity.getStartAddress());
                holder.startAddressLayoutIcon.setVisibility(View.VISIBLE);
                holder.activityStartAddressLayout.setVisibility(View.VISIBLE);

                if (holder.activityEndAddressLayout.getVisibility() == View.VISIBLE) {
                    holder.startEndIconVerticalSeparator.setVisibility(View.VISIBLE);
                    holder.startEndAddressHorizontalSeparator.setVisibility(View.VISIBLE);
                }
            }

            if (!TextUtils.isEmpty(activity.getStartTime())) {
                holder.activityStartTime.setText(activity.getStartTime());
                holder.activityStartTime.setVisibility(View.VISIBLE);
            }

            if (activity.isInProcess()) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(0, 0, 0, 0);
                holder.activityAddressIconsLayout.setLayoutParams(layoutParams);

            } else {
                if (showMapSummary) {
                    holder.setMapSummaryView();
                }

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                int margin = mContext.getResources().getDimensionPixelSize(R.dimen.margin_very_low);
                layoutParams.setMargins(0, margin, 0, margin);
                holder.activityAddressIconsLayout.setLayoutParams(layoutParams);
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
        private LinearLayout activityParentLayout;
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

            activityParentLayout = (LinearLayout) itemView.findViewById(R.id.item_user_activity_parent);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    // Check if a valid list item has been clicked
                    if (listener != null && position != RecyclerView.NO_POSITION &&
                            userActivities.size() > position && userActivities.get(position) != null) {

                        // Check if the clicked activity is InProcess or not
                        if (userActivities.get(position).isInProcess()) {
                            listener.OnInProcessActivityClicked(position, userActivities.get(position));
                        } else {
                            listener.OnHistoryActivityClicked(position, userActivities.get(position));
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
                    UserActivityModel activity = userActivities.get(position);

                    if (activity != null) {
                        // Since the mapView is re-used, need to remove pre-existing mapView features.
                        map.clear();

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        int points = 0;

                        // Update the mapView feature data and camera position.
                        if (activity.getEndLocation() != null) {
                            map.addMarker(new MarkerOptions().position(activity.getEndLocation())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_task_summary_end_marker)))
                                    .setAnchor(0.5f, 0.5f);

                            builder.include(activity.getEndLocation());
                            points++;
                        }

                        if (activity.getStartLocation() != null) {
                            map.addMarker(new MarkerOptions().position(activity.getStartLocation())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_task_summary_start_marker)))
                                    .setAnchor(0.5f, 0.5f);

                            builder.include(activity.getStartLocation());
                            points++;
                        }

                        List<LatLng> polyline = activity.getPolyline();
                        if (polyline != null && !polyline.isEmpty()) {
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
                        } else if (points > 0) {
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
