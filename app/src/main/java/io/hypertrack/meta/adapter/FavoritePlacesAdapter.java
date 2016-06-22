package io.hypertrack.meta.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.hypertrack.meta.R;
import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.UserStore;

/**
 * Created by piyush on 10/06/16.
 */
public class FavoritePlacesAdapter extends RecyclerView.Adapter<FavoritePlacesAdapter.PlacesViewHolder> {

    @Override
    public PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_place, parent, false);
        return new PlacesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlacesViewHolder holder, int position) {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        if (this.isHomeRow(position)) {
            if (user.hasHome()) {
                holder.title.setText(user.getHome().getName());
                holder.description.setText(user.getHome().getAddress());
            } else {
                holder.title.setText("Add Home");
                holder.description.setText("");
            }
        } else if (this.isWorkRow(position)) {
            if (user.hasWork()) {
                holder.title.setText(user.getWork().getName());
                holder.description.setText(user.getWork().getAddress());
            } else {
                holder.title.setText("Add Work");
                holder.description.setText("");
            }
        } else if (this.isAddNewRow(position)) {
            holder.title.setText("Add new place");
            holder.description.setText("");
        } else {
            MetaPlace place = user.getOtherPlaces().get(position - 2);
            holder.title.setText(place.getName());
            holder.description.setText(place.getAddress());
        }
    }

    @Override
    public int getItemCount() {
        return this.rowCount();
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

    private int rowCount() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return 0;
        }

        int defaultRow = 3;
        return defaultRow + user.getOtherPlaces().size();
    }

    private boolean isHomeRow(int position) {
        return position == 0;
    }

    private boolean isWorkRow(int position) {
        return position == 1;
    }

    private boolean isAddNewRow(int position) {
        return (position == this.rowCount() - 1);
    }
}
