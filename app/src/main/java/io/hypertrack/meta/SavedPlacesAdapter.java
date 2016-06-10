package io.hypertrack.meta;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import io.hypertrack.meta.model.Place;

/**
 * Created by piyush on 10/06/16.
 */
public class SavedPlacesAdapter extends RecyclerView.Adapter<SavedPlacesAdapter.PlacesViewHolder> {

    private ArrayList<Place> savedPlacesList;

    public SavedPlacesAdapter(ArrayList<Place> savedPlacesList) {
        this.savedPlacesList = savedPlacesList;
    }

    @Override
    public PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_place, parent, false);
        return new PlacesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlacesViewHolder holder, int position) {

        final Place place = savedPlacesList.get(position);

        holder.title.setText(place.getName());
        holder.description.setText(place.getAddress());
    }

    @Override
    public int getItemCount() {
        return savedPlacesList != null ? savedPlacesList.size() : 0;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        protected ImageView icon;
        protected TextView title;
        protected TextView description;

        public PlacesViewHolder(View view) {
            super(view);
            this.icon = (ImageView) view.findViewById(R.id.item_place_icon);
            this.title = (TextView) view.findViewById(R.id.item_place_title);
            this.description = (TextView) view.findViewById(R.id.item_place_desc);
        }
    }
}
