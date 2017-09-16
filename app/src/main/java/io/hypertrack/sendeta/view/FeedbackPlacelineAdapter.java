package io.hypertrack.sendeta.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.internal.common.util.Utils;
import com.hypertrack.lib.internal.transmitter.models.ActivitySegment;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.model.ActivityFeedbackModel;

/**
 * Created by Aman on 13/09/17.
 */

public class FeedbackPlacelineAdapter extends RecyclerView.Adapter<FeedbackPlacelineAdapter.FeedbackPlacelineView> {

    private List<ActivitySegment> segmentList;
    private Context mContext;
    private int selectedPosition = -1;
    private ItemClickListener itemClickListener;


    public FeedbackPlacelineAdapter(List<ActivitySegment> segmentList, Context mContext, ItemClickListener itemClickListener) {
        this.segmentList = segmentList;
        this.mContext = mContext;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public FeedbackPlacelineView onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_item, parent, false);
        return new FeedbackPlacelineView(v);
    }

    @Override
    public void onBindViewHolder(final FeedbackPlacelineView holder, int position) {
        final ActivitySegment segment = segmentList.get(position);
        int color = R.color.white;
        if (!HTTextUtils.isEmpty(segment.getFeedbackType())) {
            switch (segment.getFeedbackType()) {
                case ActivityFeedbackModel.ACTIVITY_ACCURATE:
                    color = android.R.color.holo_green_light;
                    break;
                case ActivityFeedbackModel.ACTIVITY_DELETED:
                    color = android.R.color.holo_red_light;
                    break;
                case ActivityFeedbackModel.ACTIVITY_ADDED:
                    color = android.R.color.holo_blue_light;
                    break;
                case ActivityFeedbackModel.ACTIVITY_EDITED:
                    color = R.color.lighter_gray;
                    break;
            }
        }
        holder.segmentRelativeLayout.setBackgroundColor(ContextCompat.getColor(mContext, color));
        String startTime = segment.formatTime(segment.getStartedAt());
        String endTime = segment.formatTime(segment.getEndedAt());
        if (HTTextUtils.isEmpty(endTime))
            holder.segmentStartTime.setText("Now");
        else
            holder.segmentStartTime.setText(endTime);
        holder.segmentEndTime.setText(startTime);
        holder.segmentStartTime.setVisibility(View.VISIBLE);

        holder.segmentName.setText(Utils.toProperCase(segment.getActivityType().toString()));

        holder.detail.setVisibility(View.VISIBLE);
        int icon;
        switch (segment.getActivityType().toString()) {
            case "automotive":
                icon = R.drawable.ic_driving;
                break;
            case "walking":
                icon = R.drawable.ic_walk;
                break;
            case "stationary":
                icon = R.drawable.ic_stop;
                break;
            default:
                icon = R.drawable.ic_trip;
        }
        holder.segmentIcon.setImageResource(icon);

        final int finalPosition = position;
        holder.segmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClickListener(finalPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return segmentList.size();
    }

    public class FeedbackPlacelineView extends RecyclerView.ViewHolder {

        ImageView segmentIcon, segmentDividerIcon;
        TextView segmentStartTime, segmentEndTime, segmentName, detail;
        CardView segmentLayout;
        RelativeLayout segmentRelativeLayout;

        public FeedbackPlacelineView(View itemView) {
            super(itemView);
            segmentLayout = (CardView) itemView.findViewById(R.id.segment_layout);
            segmentRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.segment_relative_layout);
            segmentIcon = (ImageView) itemView.findViewById(R.id.segment_icon);
            segmentDividerIcon = (ImageView) itemView.findViewById(R.id.segment_divider_icon);
            segmentStartTime = (TextView) itemView.findViewById(R.id.segment_start_time);
            segmentName = (TextView) itemView.findViewById(R.id.segment_name);
            segmentEndTime = (TextView) itemView.findViewById(R.id.segment_end_time);
            detail = (TextView) itemView.findViewById(R.id.detail);
        }
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
    }

    public interface ItemClickListener {
        public void onItemClickListener(int position);
    }
}
