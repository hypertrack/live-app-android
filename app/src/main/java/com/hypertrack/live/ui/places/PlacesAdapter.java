package com.hypertrack.live.ui.places;

import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.hypertrack.live.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.MyViewHolder> {

    private static final CharacterStyle STYLE_NORMAL = new StyleSpan(Typeface.NORMAL);

    private List<AutocompletePrediction> dataset = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView icon;
        TextView name;
        TextView address;

        MyViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            name = v.findViewById(R.id.name);
            address = v.findViewById(R.id.address);
        }
    }

    @Override
    public PlacesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_place_item, parent, false);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final AutocompletePrediction item = dataset.get(position);

        holder.name.setText(item.getPrimaryText(STYLE_NORMAL));
        holder.address.setText(item.getSecondaryText(STYLE_NORMAL));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(PlacesAdapter.this, view, position);
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public AutocompletePrediction getItem(int position) {
        return dataset.isEmpty() ? null : dataset.get(position);
    }

    public void addAll(Collection<AutocompletePrediction> items) {
        dataset.addAll(items);
    }

    public void clear() {
        dataset.clear();
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter<?> adapter, View view, int position);
    }
}