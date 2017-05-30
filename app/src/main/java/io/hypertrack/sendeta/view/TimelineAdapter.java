package io.hypertrack.sendeta.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hypertrack.lib.internal.common.util.TextUtils;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.Segment;

/**
 * Created by Aman Jain on 24/05/17.
 */

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineView> {

    private List<Segment> segmentList;
    private Context context;
    private int lastDay;

    public TimelineAdapter(List<Segment> segmentList, Context context) {
        this.segmentList = segmentList;
        this.context = context;
    }

    @Override
    public TimelineView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeline_segment, parent, false);
        return new TimelineView(view);
    }

    @Override
    public int getItemCount() {
        return segmentList.size();
    }

    @Override
    public void onBindViewHolder(final TimelineView holder, int position) {

        final Segment segment = segmentList.get(position);

        if (position == 0) {
            String text = segment.formatTime(segment.getStartedAt()) + " | " + segment.formatDate(segment.getStartedAt());
            holder.topText.setText(text);
            lastDay = segment.getStartedAt().getDay();
            holder.topText.setVisibility(View.VISIBLE);
        }
        else {
            holder.topText.setVisibility(View.GONE);
        }

        if ((position == getItemCount() - 1) || lastDay != segment.getEndedAt().getDay()) {
            String text = segment.formatTime(segment.getEndedAt()) + " | " + segment.formatDate(segment.getEndedAt());
            holder.bottomText.setText(text);
            lastDay = segment.getEndedAt().getDay();
        } else {
            String text = segment.formatTime(segment.getEndedAt());
            holder.bottomText.setText(text);
        }

        if (segment.isStop()) {

            holder.lineType.setBackgroundResource(R.drawable.stop_background);

            holder.middleText.setText( TextUtils.isEmpty(segment.getPlace().getLocality()) ?
                    segment.getPlace().getDisplayString() : segment.getPlace().getLocality() );

            holder.middleText.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,android.R.drawable.arrow_down_float,0);

            holder.detail.setText(segment.getFormatedDuration());
            holder.middleText.setTextColor(ContextCompat.getColor(context, R.color.stop));

            holder.middleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.middleText.setText(TextUtils.isEmpty(segment.getPlace().getLocality()) ?
                            segment.getPlace().getDisplayString() : segment.getPlace().getLocality());
                    if(holder.middleText.getTag() != null &&  holder.middleText.getTag().toString().equalsIgnoreCase("close")) {
                        holder.middleText.setText(segment.getPlace().getDisplayString());
                        holder.middleText.setMaxLines(5);
                        holder.middleText.setTag("open");
                        holder.middleText.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,android.R.drawable.arrow_up_float,0);
                    }
                    else {
                        holder.middleText.setText(TextUtils.isEmpty(segment.getPlace().getLocality()) ?
                                segment.getPlace().getDisplayString() : segment.getPlace().getLocality());
                        holder.middleText.setTag("close");
                        holder.middleText.setMaxLines(1);
                        holder.middleText.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,android.R.drawable.arrow_down_float,0);
                    }

                }
            });

        } else if (segment.isTrip()) {

            holder.lineType.setBackgroundResource(R.drawable.trip_background);
            holder.middleText.setText("Trip");
            holder.detail.setText(segment.getDistanceAndDuration());
            holder.middleText.setTextColor(ContextCompat.getColor(context, R.color.trip));

        } else if (segment.isLocationVoid()) {

            holder.lineType.setBackgroundResource(R.drawable.no_location_background);
            holder.middleText.setText("Location Unavailable");
            holder.detail.setText(segment.getFormatedDuration());
            holder.middleText.setTextColor(ContextCompat.getColor(context, R.color.no_information));

        }else if(segment.isNoInformation()){

            holder.lineType.setBackgroundResource(R.drawable.no_location_background);
            holder.middleText.setText("No Information");
            holder.detail.setText(segment.getFormatedDuration());
            holder.middleText.setTextColor(ContextCompat.getColor(context, R.color.no_information));

        }

    }

    public class TimelineView extends RecyclerView.ViewHolder {

        View lineType;
        TextView topText, bottomText, middleText, detail;

        public TimelineView(View itemView) {
            super(itemView);
            lineType = itemView.findViewById(R.id.line_type);
            topText = (TextView) itemView.findViewById(R.id.top_text);
            bottomText = (TextView) itemView.findViewById(R.id.bottom_text);
            middleText = (TextView) itemView.findViewById(R.id.middle_text);
            detail = (TextView) itemView.findViewById(R.id.detail);
        }
    }
}
