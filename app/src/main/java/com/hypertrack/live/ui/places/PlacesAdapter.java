package com.hypertrack.live.ui.places;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hypertrack.live.R;
import com.hypertrack.live.models.PlaceModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.MyViewHolder> {


    private List<PlaceModel> dataset = new ArrayList<>();
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
        final PlaceModel item = dataset.get(position);

        holder.icon.setImageResource(item.isRecent ? R.drawable.history : R.drawable.ic_place);
        holder.name.setText(item.primaryText);
        holder.address.setText(item.secondaryText);

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

    public PlaceModel getItem(int position) {
        return dataset.isEmpty() ? null : dataset.get(position);
    }

    public void addAll(Collection<PlaceModel> items) {
        dataset.addAll(items);
    }

    public void clear() {
        dataset.clear();
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter<?> adapter, View view, int position);
    }
}