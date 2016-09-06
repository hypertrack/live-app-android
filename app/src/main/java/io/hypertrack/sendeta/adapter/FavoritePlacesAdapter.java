package io.hypertrack.sendeta.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.callback.FavoritePlaceOnClickListener;
import io.hypertrack.sendeta.model.MetaPlace;
import io.hypertrack.sendeta.model.User;
import io.hypertrack.sendeta.store.UserStore;

/**
 * Created by piyush on 10/06/16.
 */
public class FavoritePlacesAdapter extends RecyclerView.Adapter<FavoritePlacesAdapter.PlacesViewHolder> {

    private MetaPlace home;
    private MetaPlace work;
    private List<MetaPlace> otherPlaces;
    private FavoritePlaceOnClickListener listener;

    public void setHome(MetaPlace home) {
        this.home = home;
    }

    public void setWork(MetaPlace work) {
        this.work = work;
    }

    public void setOtherPlaces(List<MetaPlace> otherPlaces) {
        this.otherPlaces = otherPlaces;
    }

    public FavoritePlacesAdapter(MetaPlace home, MetaPlace work, List<MetaPlace> otherPlaces, FavoritePlaceOnClickListener listener) {
        this.home = home;
        this.work = work;
        this.otherPlaces = otherPlaces;
        this.listener = listener;
    }

    private FavoritePlacesAdapter() {}

    @Override
    public PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_place, parent, false);
        return new PlacesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PlacesViewHolder holder, int position) {
        if (this.isHomeRow(position)) {
            holder.icon.setImageResource(R.drawable.ic_home);
            if (this.home != null) {
                holder.title.setText(home.getName());
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(home.getAddress());
                holder.deleteIcon.setVisibility(View.VISIBLE);
            } else {
                holder.title.setText("Add Home");
                holder.description.setVisibility(View.GONE);
                holder.deleteIcon.setVisibility(View.GONE);
            }
        } else if (this.isWorkRow(position)) {
            holder.icon.setImageResource(R.drawable.ic_work);
            if (this.work != null) {
                holder.title.setText(work.getName());
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(work.getAddress());
                holder.deleteIcon.setVisibility(View.VISIBLE);
            } else {
                holder.title.setText("Add Work");
                holder.description.setVisibility(View.GONE);
                holder.deleteIcon.setVisibility(View.GONE);
            }
        } else if (this.isAddNewRow(position)) {
            holder.icon.setImageResource(R.drawable.ic_favorite_hollow);
            holder.title.setText("Add new place");
            holder.description.setVisibility(View.GONE);
            holder.deleteIcon.setVisibility(View.GONE);
        } else {
            MetaPlace place = this.otherPlaces.get(position - 2);
            holder.icon.setImageResource(R.drawable.ic_favorite);
            holder.title.setText(place.getName());
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(place.getAddress());
            holder.deleteIcon.setVisibility(View.VISIBLE);
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
        protected ImageView deleteIcon;

        public PlacesViewHolder(View view) {
            super(view);
            this.icon = (ImageView) view.findViewById(R.id.item_place_icon);
            this.title = (TextView) view.findViewById(R.id.item_place_title);
            this.description = (TextView) view.findViewById(R.id.item_place_desc);
            this.deleteIcon = (ImageView) view.findViewById(R.id.item_place_delete);

            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() == RecyclerView.NO_POSITION)
                        return;

                    itemToBeDeletedAtPosition(getAdapterPosition());
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() == RecyclerView.NO_POSITION)
                        return;

                    itemClickedAtPosition(getAdapterPosition());
                }
            });
        }
    }

    private int rowCount() {
        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return 0;
        }

        int defaultRow = 3;
        return defaultRow + this.otherPlaces.size();
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

    private void itemClickedAtPosition(int position) {
        if (this.isHomeRow(position)) {
            if (this.home == null) {
                if (this.listener != null) {
                    this.listener.OnAddHomeClick();
                }
            } else {
                if (this.listener != null) {
                    this.listener.OnEditHomeClick(home);
                }
            }
        } else if (this.isWorkRow(position)) {
            if (this.work == null) {
                if (this.listener != null) {
                    this.listener.OnAddWorkClick();
                }
            } else {
                if (this.listener != null) {
                    this.listener.OnEditWorkClick(work);
                }
            }
        } else if (this.isAddNewRow(position)) {
            if (this.listener != null) {
                this.listener.OnAddPlaceClick();
            }
        } else {
            if (this.listener != null) {
                this.listener.OnEditPlaceClick(this.otherPlaces.get(position - 2));
            }
        }
    }

    private void itemToBeDeletedAtPosition(int position) {

        if (this.isHomeRow(position)) {
            if (this.home != null) {
                if (this.listener != null) {
                    this.listener.OnDeletePlace(home);
                }
            }
        } else if (this.isWorkRow(position)) {
            if (this.work != null) {
                if (this.listener != null) {
                    this.listener.OnDeletePlace(work);
                }
            }
        } else if (!this.isAddNewRow(position)) {
            if (this.listener != null) {
                this.listener.OnDeletePlace(this.otherPlaces.get(position - 2));
            }
        }
    }
}
