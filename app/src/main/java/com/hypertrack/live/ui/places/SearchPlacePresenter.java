package com.hypertrack.live.ui.places;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.hypertrack.live.ui.tracking.TrackingFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class SearchPlacePresenter {
    private static final String TAG = "SearchPlacePresenter";

    private final View view;
    private final SearchPlaceState state;
    private final PlacesClient placesClient;
    private AutocompleteSessionToken token;

    public SearchPlacePresenter(Context context, View view) {
        this.view = view;
        this.state = new SearchPlaceState();

        placesClient = Places.createClient(context);
    }

    public void setMapDestinationModeEnable(boolean enable) {
        state.mapDestinationMode = enable;
    }

    public void search(String query) {
        if (!state.mapDestinationMode) {
            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
            // and once again when the user makes a selection (for example when calling selectPlace()).
            token = AutocompleteSessionToken.newInstance();

            // Use the builder to create a FindAutocompletePredictionsRequest.
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setTypeFilter(TypeFilter.GEOCODE)
                    .setSessionToken(token)
                    .setQuery(query)
                    .build();

            placesClient.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
                @Override
                public void onSuccess(FindAutocompletePredictionsResponse response) {
                    view.updateList(response.getAutocompletePredictions());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            });
        }

    }

    public void selectPlace(String placeId) {
        view.showProgressBar();

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
        );
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, fields)
                .setSessionToken(token)
                .build();
        placesClient.fetchPlace(request)
                .addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
            @Override
            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                view.hideProgressBar();
                view.updateList(Collections.<AutocompletePrediction>emptyList());

                Intent intent = new Intent();
                intent.putExtra(SearchPlaceFragment.SELECTED_PLACE_KEY, fetchPlaceResponse.getPlace());
                view.onResult(TrackingFragment.AUTOCOMPLETE_REQUEST_CODE, intent);
                view.close();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                view.hideProgressBar();
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            }
        });
    }

    public interface View {

        void onResult(int requestCode, @Nullable Intent data);

        void updateAddress(String address);

        void updateList(List<AutocompletePrediction> list);

        void showProgressBar();

        void hideProgressBar();

        void close();
    }
}
