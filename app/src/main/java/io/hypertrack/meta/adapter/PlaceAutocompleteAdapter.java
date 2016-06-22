/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.hypertrack.meta.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.hypertrack.meta.R;
import io.hypertrack.meta.model.MetaPlace;
import io.hypertrack.meta.view.Home;

/**
 * Adapter that handles Autocomplete requests from the Places Geo Data API.
 * {@link AutocompletePrediction} results from the API are frozen and stored directly in this
 * adapter. (See {@link AutocompletePrediction#freeze()}.)
 * <p>
 * Note that this adapter requires a valid {@link com.google.android.gms.common.api.GoogleApiClient}.
 * The API client must be maintained in the encapsulating Activity, including all lifecycle and
 * connection states. The API client must be connected with the {@link Places#GEO_DATA_API} API.
 */
public class PlaceAutocompleteAdapter
        extends RecyclerView.Adapter<PlaceAutocompleteAdapter.AutocompleteViewHolder> implements Filterable {

    private static final String TAG = "PlaceAutocompAdapter";
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private static final CharacterStyle STYLE_NORMAL = new StyleSpan(Typeface.NORMAL);

    private Context context;
    /**
     * Current results returned by this adapter.
     */
    private ArrayList<AutocompletePrediction> mResultList;

    /**
     * Handles autocomplete requests.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * The bounds used for Places Geo Data autocomplete API requests.
     */
    private LatLngBounds mBounds;

    /**
     * Saved places
     */
    private List<MetaPlace> favorites;

    private List<MetaPlace> filteredFavorites;

    /**
     * The autocomplete filter used to restrict queries to a specific set of place types.
     */
    private AutocompleteFilter mPlaceFilter;

    private boolean isSearching;

    private String filterString;

    public void setFilterString(String filterString) {
        this.filterString = filterString.toLowerCase();

        if (!filterString.isEmpty()) {
            this.getFilter().filter(filterString);
            this.filterFavorites();
            this.isSearching = true;
        } else {
            this.isSearching = false;
            this.filteredFavorites = null;
            this.mResultList = null;
        }

        notifyDataSetChanged();
    }

    private void filterFavorites() {
        this.filteredFavorites = new ArrayList<>();

        Iterator<MetaPlace> it = this.favorites.iterator();
        while (it.hasNext()) {
            if (it.next().getName().toLowerCase().contains(this.filterString)) {
                this.filteredFavorites.add(it.next());
            }
        }
    }

    /**
     * The onItemClickListener used to listen to a list item selection
     */
    private AdapterView.OnItemClickListener itemClickListener;

    /**
     * Initializes with a resource for text rows and autocomplete query bounds.
     *
     * @see ArrayAdapter#ArrayAdapter(Context, int)
     */
    public PlaceAutocompleteAdapter(Context context, GoogleApiClient mGoogleApiClient, AdapterView.OnItemClickListener itemClickListener, List<MetaPlace> favorites) {
        super();
        this.context = context;
        this.mGoogleApiClient = mGoogleApiClient;
        this.itemClickListener = itemClickListener;
        this.favorites = favorites;
    }

    private PlaceAutocompleteAdapter() {

    }

    /**
     * Sets the bounds for all subsequent queries.
     */
    public void setBounds(LatLngBounds bounds) {
        mBounds = bounds;
    }

    @Override
    public AutocompleteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_place, parent, false);
        return new AutocompleteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AutocompleteViewHolder holder, int position) {
        if (!this.isSearching) {
            MetaPlace place = this.favorites.get(position);

            holder.header.setText(place.getName());
            holder.description.setText(place.getAddress());
        } else {
            if (this.isFilteredPlace(position)) {
                MetaPlace place = this.filteredFavorites.get(position);

                holder.header.setText(place.getName());
                holder.description.setText(place.getAddress());
            } else {
                final AutocompletePrediction item = mResultList.get(position - this.filteredPlacesCount());

                holder.header.setText(item.getPrimaryText(STYLE_BOLD));
                holder.description.setText(item.getSecondaryText(STYLE_NORMAL));
            }
        }
    }

    private int filteredPlacesCount() {
        if (this.filteredFavorites == null) {
            return 0;
        }

        return this.filteredFavorites.size();
    }

    @Override
    public int getItemCount() {
        if (!this.isSearching) {
            return this.favorites != null ? this.favorites.size() : 0;
        }

        return this.combinedResultsCount();
    }

    private int combinedResultsCount() {
        int count = this.filteredPlacesCount();

        if (mResultList != null) {
            count = count + mResultList.size();
        }

        return count;
    }

    private boolean isFilteredPlace(int position) {
        int count = this.filteredPlacesCount();
        return count > 0 && position < count;
    }

    public AutocompletePrediction getItem(int position) {
        if (mResultList != null && mResultList.size() > 0) {
            return mResultList.get(position);
        }

        return null;
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<AutocompletePrediction> googleList;
                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    googleList = getAutocomplete(constraint);
                    if (googleList != null) {
                        // The API successfully returned results.
                        results.values = googleList;
                        results.count = googleList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    Log.d(TAG, "Received results");
                    mResultList = (ArrayList<AutocompletePrediction>) results.values;

                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    Log.d(TAG, "no results found");
                    mResultList = null;

                    notifyItemRangeRemoved(0, 0);
                }

                ((Home) context).processPublishedResults(mResultList);
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                } else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    /**
     * Submits an autocomplete query to the Places Geo Data Autocomplete API.
     * Results are returned as frozen AutocompletePrediction objects, ready to be cached.
     * objects to store the MetaPlace ID and description that the API returns.
     * Returns an empty list if no results were found.
     * Returns null if the API client is not available or the query did not complete
     * successfully.
     * This method MUST be called off the main UI thread, as it will block until data is returned
     * from the API, which may include a network request.
     *
     * @param constraint Autocomplete query string
     * @return Results from the autocomplete API or null if the query was not successful.
     * @see Places#GEO_DATA_API#getAutocomplete(CharSequence)
     * @see AutocompletePrediction#freeze()
     */
    private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint) {
        if (mGoogleApiClient.isConnected()) {
            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                                    mBounds, mPlaceFilter);

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                autocompletePredictions.release();
                return null;
            }

            Log.d(TAG, "Query complete: Received " + autocompletePredictions.getCount() + " predictions");

            // Freeze the results immutable representation that can be stored safely.
            return DataBufferUtils.freezeAndClose(autocompletePredictions);
        }
        Log.e(TAG, "Google API client not connected for autocomplete query.");
        return null;
    }

    public class AutocompleteViewHolder extends RecyclerView.ViewHolder{
        public TextView header;
        public TextView description;
        public ImageView icon;

        public AutocompleteViewHolder(final View view) {
            super(view);
            header = (TextView) view.findViewById(R.id.item_place_title);
            description = (TextView) view.findViewById(R.id.item_place_desc);
            icon = (ImageView) view.findViewById(R.id.item_place_icon);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(null, view, getAdapterPosition(), getItemId());
                }
            });
        }
    }
}
