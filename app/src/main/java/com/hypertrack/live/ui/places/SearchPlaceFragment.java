package com.hypertrack.live.ui.places;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.tracking.TrackingFragment;
import com.hypertrack.live.utils.HTTextWatcher;

import java.util.List;

public class SearchPlaceFragment extends Fragment implements SearchPlacePresenter.View {

    public static final String SELECTED_PLACE_KEY = "selected_place";

    private SearchPlacePresenter presenter;

    private EditText search;
    private View destinationOnMap;
    private View setOnMap;
    private View share;
    private PlacesAdapter placesAdapter;
    private LoaderDecorator loader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_place, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new SearchPlacePresenter(getActivity(), this);
        loader = new LoaderDecorator(getContext());

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        getActivity().setTitle(R.string.where_are_you_going);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        search = view.findViewById(R.id.search);
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    presenter.setMapDestinationModeEnable(false);
                }
            }
        });
        search.addTextChangedListener(new HTTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                presenter.search(charSequence.toString());
            }
        });
        view.findViewById(R.id.no_destination).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResult(TrackingFragment.AUTOCOMPLETE_REQUEST_CODE, new Intent());
                close();
            }
        });
        destinationOnMap = view.findViewById(R.id.destination_on_map);
        setOnMap = view.findViewById(R.id.set_on_map);
        setOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.search("");
                presenter.setMapDestinationModeEnable(true);
                destinationOnMap.setVisibility(View.VISIBLE);
                onResult(TrackingFragment.SET_ON_MAP_REQUEST_CODE, new Intent());
            }
        });
        share = view.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.share();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView locations = view.findViewById(R.id.locations);
        locations.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(view.getContext(),
                layoutManager.getOrientation());
        locations.addItemDecoration(dividerItemDecoration);
        locations.setLayoutManager(layoutManager);
        placesAdapter = new PlacesAdapter();
        placesAdapter.setOnItemClickListener(new PlacesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView.Adapter<?> adapter, View view, int position) {
                setOnMap.setVisibility(View.GONE);
                presenter.selectPlace(placesAdapter.getItem(position).getPlaceId());
            }
        });
        locations.setAdapter(placesAdapter);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResult(int requestCode, @Nullable Intent data) {
        setOnMap.setVisibility(View.GONE);
        (getActivity().getSupportFragmentManager().findFragmentByTag(TrackingFragment.class.getSimpleName()))
                .onActivityResult(requestCode, Activity.RESULT_OK, data);
    }

    @Override
    public void updateAddress(String address) {
        search.setText(address);
    }

    @Override
    public void updateList(List<AutocompletePrediction> list) {
        placesAdapter.clear();
        placesAdapter.addAll(list);
        placesAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgressBar() {
        loader.start();
    }

    @Override
    public void hideProgressBar() {
        loader.stop();
    }

    @Override
    public void close() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
