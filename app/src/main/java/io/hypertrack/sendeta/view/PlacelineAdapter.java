package io.hypertrack.sendeta.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.skyfishjy.library.RippleBackground;

import java.util.Date;
import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Segment;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class PlacelineAdapter extends RecyclerView.Adapter<PlacelineAdapter.PlacelineView> {

    private List<Segment> segmentList;
    private Context context;
    private int lastDay;
    //private RippleBackground rippleBackground;
    private Date currentDate;

    public PlacelineAdapter(List<Segment> segmentList, Context context) {
        this.segmentList = segmentList;
        this.context = context;
    }

    @Override
    public PlacelineView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.placeline_segment, parent, false);
        return new PlacelineView(view);
    }

    @Override
    public int getItemCount() {
        return segmentList.size();
    }

    @Override
    public void onBindViewHolder(final PlacelineView holder, int position) {

        final Segment segment = segmentList.get(position);

        if (position % 2 == 0) {
            String text = segment.formatTime(segment.getStartedAt());
            holder.topText.setText(text);
            lastDay = segment.getStartedAt().getDay();
            holder.topText.setVisibility(View.VISIBLE);

            String bottomText = segment.formatTime(segment.getEndedAt());
            holder.bottomText.setText(bottomText);
            holder.bottomText.setVisibility(View.VISIBLE);

        } else if (position == getItemCount() - 1) {
            String bottomText = segment.formatTime(segment.getEndedAt());
            holder.bottomText.setText(bottomText);
            holder.bottomText.setVisibility(View.VISIBLE);

        } else {
            holder.topText.setVisibility(View.GONE);
            holder.bottomText.setVisibility(View.GONE);

        }
        if (segment.isStop()) {
            holder.segmentBarLayout.setVisibility(View.INVISIBLE);
            holder.segmentAddress.setVisibility(View.VISIBLE);
            holder.segmentAddress.setText(HTTextUtils.isEmpty(segment.getPlace().getLocality()) ?
                    segment.getPlace().getDisplayString() : segment.getPlace().getLocality());

            holder.segmentIcon.setImageResource(R.drawable.ic_stop);
            holder.segmentAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                    R.drawable.ic_keyboard_arrow_right_black_24dp, 0);

            if (segment.location != null && !HTTextUtils.isEmpty(segment.location.getActivity()) &&
                    segment.location.getActivity().equalsIgnoreCase("unknown")) {
                holder.segmentTypeText.setText(segment.location.getActivity());
            } else {
                holder.segmentTypeText.setText("Stop");
            }
            holder.duration.setText(segment.getFormatedDuration());
            holder.duration.setVisibility(View.VISIBLE);
            holder.segmentBar.setVisibility(View.INVISIBLE);

            holder.segmentAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.segmentAddress.setText(HTTextUtils.isEmpty(segment.getPlace().getLocality()) ?
                            segment.getPlace().getDisplayString() : segment.getPlace().getLocality());
                    if (holder.segmentAddress.getTag() != null &&
                            holder.segmentAddress.getTag().toString().equalsIgnoreCase("close")) {
                        holder.segmentAddress.setText(segment.getPlace().getDisplayString());
                        holder.segmentAddress.setMaxLines(5);
                        holder.segmentAddress.setTag("open");
                        holder.segmentAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                                R.drawable.ic_keyboard_arrow_up_black_24dp, 0);
                    } else {
                        holder.segmentAddress.setText(HTTextUtils.isEmpty(segment.getPlace().getLocality()) ?
                                segment.getPlace().getDisplayString() : segment.getPlace().getLocality());
                        holder.segmentAddress.setTag("close");
                        holder.segmentAddress.setMaxLines(1);
                        holder.segmentAddress.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                                R.drawable.ic_keyboard_arrow_right_black_24dp, 0);
                    }

                }
            });

        } else if (segment.isTrip()) {

            holder.segmentBar.setImageResource(R.drawable.trip_background);
            holder.segmentBarLayout.setVisibility(View.VISIBLE);

            if (!HTTextUtils.isEmpty(segment.getActivityType()) &&
                    !segment.getActivityType().equalsIgnoreCase("unknown")) {

                holder.segmentTypeText.setText(segment.getActivityType().substring(0, 1).toUpperCase()
                        + segment.getActivityType().substring(1, segment.getActivityType().length()));
            } else
                holder.segmentTypeText.setText("Trip");

            holder.duration.setText(segment.getDistanceAndDuration());
            holder.duration.setVisibility(View.VISIBLE);
            holder.segmentIcon.setImageResource(R.drawable.ic_trip);
            holder.segmentAddress.setVisibility(View.GONE);
            holder.segmentBar.setVisibility(View.VISIBLE);

        } else if (segment.isLocationVoid()) {
            holder.segmentBar.setImageResource(R.drawable.no_location_background);
            holder.segmentBar.setVisibility(View.VISIBLE);
            holder.segmentBarLayout.setVisibility(View.INVISIBLE);
            holder.segmentTypeText.setText("Location Disabled");
            holder.duration.setText(segment.getFormatedDuration());
            holder.segmentIcon.setImageResource(R.drawable.ic_no_location);
            holder.segmentAddress.setVisibility(View.GONE);
            holder.duration.setVisibility(View.VISIBLE);

        } else if (segment.isNoInformation()) {
            holder.segmentBar.setImageResource(R.drawable.no_location_background);
            holder.segmentBar.setVisibility(View.VISIBLE);
            holder.segmentBarLayout.setVisibility(View.INVISIBLE);
            holder.segmentTypeText.setText("No Information");
            holder.duration.setText(segment.getFormatedDuration());
            holder.segmentIcon.setImageResource(R.drawable.ic_no_information);
            holder.segmentAddress.setVisibility(View.GONE);
            holder.duration.setVisibility(View.VISIBLE);
        }
        if (segment.isStop() && (position == getItemCount() - 1) &&
                currentDate.getDay() == new Date().getDay()) {

            holder.currentLocationRipple.setVisibility(View.VISIBLE);
            holder.currentLocationRipple.startRippleAnimation();
        } else {
            holder.currentLocationRipple.stopRippleAnimation();
            holder.currentLocationRipple.setVisibility(View.GONE);
        }
/*
        if(!segment.isStop() && position+1<segmentList.size()&&!segmentList.get(position+1).isStop() ) {
            if (rippleBackground != null)
                rippleBackground.setVisibility(View.GONE);
        }*/
    }

    public void setCurrentDate(Date date) {
        currentDate = date;
    }

    private int pixelToDP(int yourdpmeasure) {
        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                yourdpmeasure,
                r.getDisplayMetrics()
        );
        return px;
    }

    public class PlacelineView extends RecyclerView.ViewHolder {

        ImageView segmentBar, segmentIcon;
        TextView topText, bottomText, segmentTypeText, segmentAddress, duration;
        RelativeLayout segmentBarLayout;
        RippleBackground currentLocationRipple;

        public PlacelineView(View itemView) {
            super(itemView);
            segmentBar = (ImageView) itemView.findViewById(R.id.segment_bar);
            segmentIcon = (ImageView) itemView.findViewById(R.id.segment_icon);
            topText = (TextView) itemView.findViewById(R.id.top_text);
            bottomText = (TextView) itemView.findViewById(R.id.bottom_text);
            segmentTypeText = (TextView) itemView.findViewById(R.id.segment_type_text);
            segmentAddress = (TextView) itemView.findViewById(R.id.segment_address);
            duration = (TextView) itemView.findViewById(R.id.duration);
            segmentBarLayout = (RelativeLayout) itemView.findViewById(R.id.segment_bar_layout);
            currentLocationRipple = (RippleBackground) itemView.findViewById(R.id.current_location_ripple);
        }

    }
}
