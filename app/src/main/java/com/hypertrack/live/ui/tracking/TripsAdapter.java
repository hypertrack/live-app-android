package com.hypertrack.live.ui.tracking;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hypertrack.live.R;
import com.hypertrack.sdk.views.dao.Trip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.MyViewHolder> {

    private static final CharacterStyle STYLE_NORMAL = new StyleSpan(Typeface.NORMAL);
    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);

    private final List<Trip> dataset = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private int selectedPos = RecyclerView.NO_POSITION;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tripTitle;
        TextView tripValue;
        ImageView tripIcon;

        MyViewHolder(View v) {
            super(v);
            tripTitle = v.findViewById(R.id.trip_title);
            tripValue = v.findViewById(R.id.trip_value);
            tripIcon = v.findViewById(R.id.trip_icon);
        }
    }

    @Override
    public TripsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_trip_item, parent, false);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Trip item = dataset.get(position);

        int origin = R.drawable.starting_position;
        int destination = R.drawable.destination;
        if (item.getStatus().equals("completed")) {
            origin = R.drawable.departure_sd_c;
            destination = R.drawable.arrival_sd_c;
        }

        if (item.getDestination() == null) {
            holder.tripTitle.setText(R.string.trip_started_from);
            String valueText = "";
            if (item.getStartDate() != null) {
                valueText = DATE_FORMAT.format(item.getStartDate());
            }
            holder.tripValue.setText(valueText);
            holder.tripIcon.setImageResource(origin);
        } else {
            holder.tripTitle.setText(R.string.trip_to);
            if (!TextUtils.isEmpty(item.getDestination().getAddress())) {
                holder.tripValue.setText(item.getDestination().getAddress());
            } else {
                String latLng = String.format(holder.itemView.getContext().getString(R.string.lat_lng),
                        item.getDestination().getLatitude(), item.getDestination().getLongitude()
                );
                holder.tripValue.setText(latLng);
            }
            holder.tripIcon.setImageResource(destination);
        }
        holder.itemView.setSelected(selectedPos == position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedPos != position) {
                    selectedPos = position;
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(TripsAdapter.this, view, position);
                    }
                    notifyDataSetChanged();
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public Trip getItem(int position) {
        return dataset.isEmpty() ? null : dataset.get(position);
    }

    public void setSelection(int position) {
        if (selectedPos != position) {
            selectedPos = position;
            notifyDataSetChanged();
        }
    }

    public void addAll(Collection<Trip> items) {
        dataset.addAll(items);
    }

    public void clear() {
        dataset.clear();
    }

    public void update(Collection<Trip> items) {
        dataset.clear();
        dataset.addAll(items);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter<?> adapter, View view, int position);
    }
}