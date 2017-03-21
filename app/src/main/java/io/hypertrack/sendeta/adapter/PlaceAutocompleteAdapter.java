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

package io.hypertrack.sendeta.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.adapter.callback.PlaceAutoCompleteOnClickListener;
import io.hypertrack.sendeta.model.OnboardingUser;
import io.hypertrack.sendeta.model.UserPlace;
import io.hypertrack.sendeta.view.Home;

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
    private List<UserPlace> favorites;

    private List<UserPlace> filteredFavorites;

    /**
     * The autocomplete filter used to restrict queries to a specific set of place types.
     */
    private AutocompleteFilter mPlaceFilter;

    private boolean isSearching;

    private String filterString;

    private PlaceAutoCompleteOnClickListener listener;
    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {

            try {
                if (!places.getStatus().isSuccess()) {
                    // Request did not complete successfully
                    Log.d(TAG, "UserPlace query did not complete. Error: " + places.getStatus().toString());
                    places.release();
                    if (listener != null) {
                        listener.OnError();
                    }
                }

                if (places.getCount() == 0) {
                    Log.d(TAG, "Places is empty");
                    places.release();
                    if (listener != null) {
                        listener.OnError();
                    }
                }

                // Get the UserPlace object from the buffer.
                final Place place = places.get(0);

                Log.i(TAG, "UserPlace details received: " + place.getName());

                if (listener != null) {
                    listener.OnSuccess(new UserPlace(place));
                }
                places.release();

            } catch (Exception e) {
                if (listener != null) {
                    listener.OnError();
                }
            }
        }
    };

    /**
     * Initializes with a resource for text rows and autocomplete query bounds.
     *
     * @see ArrayAdapter#ArrayAdapter(Context, int)
     */
    public PlaceAutocompleteAdapter(Context context, GoogleApiClient mGoogleApiClient, PlaceAutoCompleteOnClickListener listener) {
        super();
        this.context = context;
        this.mGoogleApiClient = mGoogleApiClient;
        this.favorites = new ArrayList<>();
        this.listener = listener;
    }

    private PlaceAutocompleteAdapter() {

    }

    public void setSearching(boolean isSearching) {
        this.isSearching = isSearching;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString.toLowerCase();

        if (!filterString.isEmpty()) {
            getFilter().filter(filterString);
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

        Iterator<UserPlace> it = this.favorites.iterator();
        while (it.hasNext()) {
            UserPlace place = it.next();

            if (!TextUtils.isEmpty(place.getName()) && place.getName().toLowerCase().contains(this.filterString)) {
                this.filteredFavorites.add(place);
            }
        }
    }

    public void refreshFavorites(List<UserPlace> favorites) {
        if (this.favorites == null) {
            this.favorites = favorites;
        }

        this.favorites.clear();
        this.favorites.addAll(favorites);
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
            UserPlace place = this.favorites.get(position);

            if (place.isHome()) {
                holder.icon.setImageResource(R.drawable.ic_home);
            } else if (place.isWork()) {
                holder.icon.setImageResource(R.drawable.ic_work);
            } else if (isRecent(position)) {
                holder.icon.setImageResource(R.drawable.ic_access_time);
            } else {
                holder.icon.setImageResource(R.drawable.ic_favorite);
            }

            holder.header.setText(place.getName());
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(place.getAddress());
        } else {
            if (this.isFilteredPlace(position)) {
                UserPlace place = this.filteredFavorites.get(position);

                if (place.isHome()) {
                    holder.icon.setImageResource(R.drawable.ic_home);
                } else if (place.isWork()) {
                    holder.icon.setImageResource(R.drawable.ic_work);
                } else if (isRecent(position)) {
                    holder.icon.setImageResource(R.drawable.ic_access_time);
                } else {
                    holder.icon.setImageResource(R.drawable.ic_favorite);
                }


                holder.header.setText(place.getName());
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(place.getAddress());
            } else {
                final AutocompletePrediction item = mResultList.get(position - this.filteredPlacesCount());
                holder.icon.setImageResource(R.drawable.ic_marker_gray);
                holder.header.setText(item.getPrimaryText(STYLE_BOLD));
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(item.getSecondaryText(STYLE_NORMAL));
            }
        }
    }

    private boolean isRecent(int position) {
        return position >= (getItemCount() - OnboardingUser.sharedOnboardingUser().getRecentSearch().size());
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
        if (mResultList != null && mResultList.size() > 0 && position < mResultList.size()) {
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
                if (constraint != null && constraint.length() > 0) {
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
                if (!isSearching) {
                    if (context instanceof Home) {
                        ((Home) context).processPublishedResults(favorites != null && favorites.size() > 0);
                    } /*else if (context instanceof RequestETA) {
                        ((RequestETA) context).processPublishedResults(favorites != null && favorites.size() > 0);
                    }*/
                    return;
                }

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

                if (context instanceof Home) {
                    ((Home) context).processPublishedResults(mResultList != null && mResultList.size() > 0);
                } /*else if (context instanceof RequestETA) {
                    ((RequestETA) context).processPublishedResults(favorites != null && favorites.size() > 0);
                }*/
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
     * objects to store the UserPlace ID and description that the API returns.
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

    private void itemClickedAtPosition(int position) {

        if (!this.isSearching) {
            if (this.favorites.size() > 0 && this.favorites.size() > position) {
                UserPlace place = this.favorites.get(position);
                if (this.listener != null) {
                    this.listener.OnSuccess(place);
                }
            } else {
                if (this.listener != null) {
                    this.listener.OnError();
                }
            }
        } else {
            if (this.isFilteredPlace(position)) {
                if (this.filteredFavorites.size() > 0 && this.filteredFavorites.size() > position) {
                    UserPlace place = this.filteredFavorites.get(position);
                    if (this.listener != null) {
                        this.listener.OnSuccess(place);
                    }
                } else {
                    if (this.listener != null) {
                        this.listener.OnError();
                    }
                }
            } else {
                final AutocompletePrediction item = getItem(position - this.filteredPlacesCount());
                if (item != null) {
                    final String placeId = item.getPlaceId();

                    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, placeId);
                    placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
                }
            }
        }
    }

    public class AutocompleteViewHolder extends RecyclerView.ViewHolder {
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
                    itemClickedAtPosition(getAdapterPosition());
                }
            });
        }
    }
}
